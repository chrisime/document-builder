package com.craigburke.document.builder

import com.craigburke.document.core.builder.DocumentBuilder
import com.craigburke.document.core.builder.RenderState
import com.craigburke.document.core.dom.Image
import com.craigburke.document.core.dom.LineBreak
import com.craigburke.document.core.dom.PageBreak
import com.craigburke.document.core.dom.attribute.Font
import com.craigburke.document.core.dom.attribute.HeaderFooterOptions
import com.craigburke.document.core.dom.attribute.TextBlockType
import com.craigburke.document.core.dom.block.BlockNode
import com.craigburke.document.core.dom.block.Paragraph
import com.craigburke.document.core.dom.block.Table
import com.craigburke.document.core.dom.block.table.Cell
import com.craigburke.document.core.dom.block.table.Row
import com.craigburke.document.core.dom.text.Heading
import com.craigburke.document.core.dom.text.Link
import com.craigburke.document.core.dom.text.Text
import com.craigburke.document.core.dom.text.TextNode
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j

import java.time.OffsetDateTime

import static com.craigburke.document.core.unit.UnitUtil.pointToEigthPoint
import static com.craigburke.document.core.unit.UnitUtil.pointToEmu
import static com.craigburke.document.core.unit.UnitUtil.pointToHalfPoint
import static com.craigburke.document.core.unit.UnitUtil.pointToTwip

/**
 * Builder for Word documents
 * @author Craig Burke
 */
@Slf4j
@InheritConstructors
class WordDocumentBuilder extends DocumentBuilder<WordDocument> {

    private static final String PAGE_NUMBER_PLACEHOLDER = '##pageNumber##'
    private static final Map    RUN_TEXT_OPTIONS        = ['xml:space': 'preserve']

    @Override
    WordDocument createDocument(Map attributes) {
        document = new WordDocument(attributes)
        document
    }

    @Override
    void writeDocument() {
        document.configureForWriting(out)
        HeaderFooterOptions headerFooterOptions = new HeaderFooterOptions(pageNumber: PAGE_NUMBER_PLACEHOLDER,
                                                                          pageCount: document.pageCount,
                                                                          dateGenerated: OffsetDateTime.now())

        Map header = renderHeader(headerFooterOptions)
        Map footer = renderFooter(headerFooterOptions)

        renderState = RenderState.PAGE
        document.generateDocument { baseMarkupBuilderDocument ->
            w.document {
                w.body {
                    document.children.each { child ->
                        if (child instanceof TextBlockType) {
                            addTextBlockType(baseMarkupBuilderDocument, child)
                        } else if (child instanceof PageBreak) {
                            addPageBreak(baseMarkupBuilderDocument)
                        } else if (child instanceof Table) {
                            addTable(baseMarkupBuilderDocument, child)
                        } else {
                            log.warn('Unknown child in document: {}', child.getClass())
                        }
                    }
                    w.sectPr {
                        w.pgSz('w:h': pointToTwip(document.height),
                               'w:w': pointToTwip(document.width),
                               'w:orient': document.orientation)
                        w.pgMar('w:bottom': pointToTwip(document.margin.bottom).toInteger(),
                                'w:top': pointToTwip(document.margin.top).toInteger(),
                                'w:right': pointToTwip(document.margin.right).toInteger(),
                                'w:left': pointToTwip(document.margin.left).toInteger(),
                                'w:footer': pointToTwip(footer ? footer.node.margin.bottom : 0.0),
                                'w:header': pointToTwip(header ? header.node.margin.top : 0.0))

                        if (header)
                            w.headerReference('r:id': header.id, 'w:type': 'default')
                        if (footer)
                            w.footerReference('r:id': footer.id, 'w:type': 'default')
                    }
                }
            }
        }

        renderState = RenderState.CUSTOM
        document.write()
    }

    private Map renderHeader(HeaderFooterOptions options) {
        Map headerMap = [:]
        if (headerClosure) {
            renderState = RenderState.HEADER
            headerMap.node = buildHeaderNode(options)
            headerMap.id = document.generateDocumentPart(BasicDocumentPartTypes.HEADER) { builder ->
                w.hdr {
                    renderHeaderFooterNode(builder, headerMap.node as BlockNode)
                }
            }
        }
        headerMap
    }

    private Map renderFooter(HeaderFooterOptions options) {
        Map footerMap = [:]
        if (footerClosure) {
            renderState = RenderState.FOOTER
            footerMap.node = buildFooterNode(options)
            footerMap.id = document.generateDocumentPart(BasicDocumentPartTypes.FOOTER) { builder ->
                w.hdr {
                    renderHeaderFooterNode(builder, footerMap.node as BlockNode)
                }
            }
        }
        footerMap
    }

    private void renderHeaderFooterNode(baseMarkupBuilderDocument, BlockNode node) {
        if (node instanceof Paragraph) {
            addTextBlockType(baseMarkupBuilderDocument, node)
        } else if (node instanceof Table) {
            addTable(baseMarkupBuilderDocument, node)
        }
    }

    private void addPageBreak(baseMarkupBuilderDocument) {
        baseMarkupBuilderDocument.w.p {
            w.r {
                w.br('w:type': 'page')
            }
        }
    }

    private int calculateSpacingAfter(TextBlockType node) {
        BigDecimal totalSpacing = 0.0G

        switch (renderState) {
            case RenderState.PAGE:
                totalSpacing = node.margin.bottom
                List items = (node.parent as BlockNode).children
                int index = items.findIndexOf { it == node }

                if (index != items.size() - 1) {
                    def nextSibling = items[index + 1]
                    if (nextSibling instanceof BlockNode) {
                        totalSpacing += nextSibling.margin.top
                    }
                }
                break

            case RenderState.HEADER:
                totalSpacing = node.margin.bottom
                break

            case RenderState.FOOTER:
                totalSpacing = 0
            case RenderState.CUSTOM:
                break
        }
        pointToTwip(totalSpacing)
    }

    private int calculateSpacingBefore(TextBlockType node) {
        BigDecimal totalSpacing = 0.0G

        switch (renderState) {
            case RenderState.PAGE:
                totalSpacing = node.margin.top
                List items = (node.parent as BlockNode).children
                int index = items.findIndexOf { it == node }
                if (index > 0) {
                    def previousSibling = items[index - 1]
                    if (previousSibling instanceof Table) {
                        totalSpacing += previousSibling.margin.bottom
                    }
                }
                break

            case RenderState.HEADER:
                totalSpacing = 0
                break

            case RenderState.FOOTER:
                totalSpacing = node.margin.top
                break
        }

        pointToTwip(totalSpacing)
    }

    private void addTextBlockType(baseMarkupBuilderDocument, TextBlockType textBlockType) {
        baseMarkupBuilderDocument.w.p {
            w.pPr {
                if (textBlockType.background)
                    w.shd('w:fill': textBlockType.background.hex, 'w:color': textBlockType.background.hex,
                          'w:val': 'solid')
                if (textBlockType instanceof Heading && stylesEnabled)
                    w.pStyle 'w:val': "Heading${textBlockType.level}"

                String lineRule = textBlockType.lineSpacing ? 'exact' : 'auto'
                BigDecimal lineValue = textBlockType.lineSpacing ? pointToTwip(textBlockType.lineSpacing)
                                                                 : (textBlockType.lineSpacingMultiplier * 240)

                w.spacing('w:before': calculateSpacingBefore(textBlockType),
                          'w:after': calculateSpacingAfter(textBlockType),
                          'w:lineRule': lineRule,
                          'w:line': lineValue)
                w.ind('w:start': pointToTwip(textBlockType.margin.left.toInteger()),
                      'w:left': pointToTwip(textBlockType.margin.left.toInteger()),
                      'w:right': pointToTwip(textBlockType.margin.right.toInteger()),
                      'w:end': pointToTwip(textBlockType.margin.right.toInteger()))
                w.jc('w:val': textBlockType.align.value)

                if (textBlockType instanceof Heading)
                    w.outlineLvl('w:val': "${textBlockType.level - 1}")
            }

            String paragraphLinkId = UUID.randomUUID()
            if (textBlockType.ref)
                w.bookmarkStart('w:id': paragraphLinkId, 'w:name': textBlockType.ref)

            if (textBlockType instanceof Paragraph) {
                textBlockType.children.each { child ->
                    switch (child.getClass()) {
                        case Link:
                            addLink(baseMarkupBuilderDocument, child as Link)
                            break
                        case Text:
                            addTextRun(baseMarkupBuilderDocument, child as Text)
                            break
                        case Image:
                            addImageRun(baseMarkupBuilderDocument, child as Image)
                            break
                        case LineBreak:
                            addLineBreakRun(baseMarkupBuilderDocument)
                            break
                    }
                }
            } else if (textBlockType instanceof Heading) {
                addTextRun(baseMarkupBuilderDocument, textBlockType)
            }

            if (textBlockType.ref)
                w.bookmarkEnd('w:id': paragraphLinkId)
        }
    }

    private static boolean isStylesEnabled() {
        false
    }

    private void addLink(baseMarkupBuilderDocument, Link link) {
        if (link.url.startsWith('#')) {
            baseMarkupBuilderDocument.w.hyperlink('w:anchor': link.url[1..-1]) {
                addTextRun(baseMarkupBuilderDocument, link)
            }
        } else {
            String id = document.addLink(link.url, currentDocumentPart)
            baseMarkupBuilderDocument.w.hyperlink('r:id': id) {
                addTextRun(baseMarkupBuilderDocument, link)
            }
        }
    }

    private void addLineBreakRun(baseMarkupBuilderDocument) {
        baseMarkupBuilderDocument.w.r {
            w.br()
        }
    }

    private DocumentPartType getCurrentDocumentPart() {
        switch (renderState) {
            case RenderState.PAGE:
                BasicDocumentPartTypes.DOCUMENT
                break
            case RenderState.HEADER:
                BasicDocumentPartTypes.HEADER
                break
            case RenderState.FOOTER:
                BasicDocumentPartTypes.FOOTER
                break
            default:
                throw new IllegalStateException("Unexpected render state!")
        }
    }

    private void addImageRun(baseMarkupBuilderDocument, Image image) {
        String blipId = document.addImage(image.hashName, image.data, currentDocumentPart)

        BigDecimal widthInEmu = pointToEmu(image.width)
        BigDecimal heightInEmu = pointToEmu(image.height)
        String imageDescription = "Image: ${image.hashName}"

        baseMarkupBuilderDocument.w.r {
            w.drawing {
                wp.inline(distT: 0, distR: 0, distB: 0, distL: 0) {
                    wp.extent(cx: widthInEmu, cy: heightInEmu)
                    wp.docPr(id: 1, name: imageDescription, descr: image.hashName)
                    a.graphic {
                        a.graphicData(uri: 'http://schemas.openxmlformats.org/drawingml/2006/picture') {
                            pic.pic {
                                pic.nvPicPr {
                                    pic.cNvPr(id: 0, name: imageDescription, descr: image.hashName)
                                    pic.cNvPicPr {
                                        a.picLocks(noChangeAspect: 'true')
                                    }
                                }
                                pic.blipFill {
                                    a.blip('r:embed': blipId)
                                    a.stretch {
                                        a.fillRect()
                                    }
                                }
                                pic.spPr {
                                    a.xfrm {
                                        a.off(x: 0, y: 0)
                                        a.ext(cx: widthInEmu, cy: heightInEmu)
                                    }
                                    a.prstGeom(prst: 'rect') {
                                        a.avLst()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void addTable(baseMarkupBuilderDocument, Table table) {
        baseMarkupBuilderDocument.w.tbl {
            w.tblPr {
                w.tblW('w:w': pointToTwip(table.width), 'w:type': 'dxa')
                w.tblBorders {
                    def properties = ['top', 'right', 'bottom', 'left', 'insideH', 'insideV']
                    properties.each { String property ->
                        w."${property}"('w:sz': pointToEigthPoint(table.border.size),
                                        'w:color': table.border.color.hex,
                                        'w:val': (table.border.size == 0 ? 'none' : 'single'))
                    }
                }
            }

            if (table.columns) {
                w.tblGrid {
                    List<BigDecimal> columnWidths = table.computeColumnWidths()
                    columnWidths.each { BigDecimal columnWidth ->
                        w.gridCol('w:w': pointToTwip(columnWidth).longValue())
                    }
                }
            }

            table.children.each { Row row ->
                w.tr {
                    row.children.each { Cell column ->
                        if (column.rowsSpanned == 0) {
                            addColumn(baseMarkupBuilderDocument, column)
                        } else {
                            addMergeColumn(baseMarkupBuilderDocument)
                        }
                        column.rowsSpanned++
                    }
                }
            }
        }
    }

    private void addColumn(baseMarkupBuilderDocument, Cell column) {
        Table table = column.table

        baseMarkupBuilderDocument.w.tc {
            w.tcPr {
                w.vAlign('w:val': 'top')
                w.tcW('w:w': pointToTwip(column.width - (table.padding * 2)).toInteger(), 'w:type': 'dxa')
                w.tcMar {
                    w.top('w:w': pointToTwip(table.padding).toInteger(), 'w:type': 'dxa')
                    w.bottom('w:w': pointToTwip(table.padding).toInteger(), 'w:type': 'dxa')
                    w.left('w:w': pointToTwip(table.padding).toInteger(), 'w:type': 'dxa')
                    w.right('w:w': pointToTwip(table.padding).toInteger(), 'w:type': 'dxa')
                }
                if (column.background)
                    w.shd('w:val': 'clear', 'w:color': 'auto', 'w:fill': column.background.hex)
                if (column.colspan > 1)
                    w.gridSpan('w:val': column.colspan)
                if (column.rowspan > 1)
                    w.vMerge('w:val': 'restart')
            }

            column.children.each {
                if (it instanceof Paragraph) {
                    addTextBlockType(baseMarkupBuilderDocument, it)
                } else {
                    addTable(baseMarkupBuilderDocument, it as Table)
                    w.p()
                }
            }

            if (!column.children)
                w.p()
        }

    }

    private void addMergeColumn(baseMarkupBuilderDocument) {
        baseMarkupBuilderDocument.w.tc {
            w.tcPr {
                w.vMerge()
            }
            w.p()
        }
    }

    private void addTextRun(baseMarkupBuilderDocument, TextNode text) {
        String id = UUID.randomUUID()

        if (text.ref)
            baseMarkupBuilderDocument.w.bookmarkStart('w:id': id, 'w:name': text.ref)

        Font font = text.font

        baseMarkupBuilderDocument.w.r {
            w.rPr {
                w.rFonts('w:ascii': font.family)
                if (font.bold) {
                    w.b()
                }
                if (font.italic) {
                    w.i()
                }
                if (font.underline) {
                    w.u('w:val': 'single')
                }
                if (text.background) {
                    w.shd('w:fill': text.background.hex, 'w:color': text.background.hex, 'w:val': 'solid')
                }
                w.color('w:val': font.color.hex)
                w.sz('w:val': pointToHalfPoint(font.size).toInteger())
            }

            if (renderState == RenderState.PAGE)
                w.t(text.value, RUN_TEXT_OPTIONS)
            else
                parseHeaderFooterText(baseMarkupBuilderDocument, text.value)
        }

        if (text.ref)
            baseMarkupBuilderDocument.w.bookmarkEnd('w:id': id)
    }

    private static void parseHeaderFooterText(baseMarkupBuilderDocument, String text) {
        String[] textParts = text.split(PAGE_NUMBER_PLACEHOLDER)
        textParts.eachWithIndex { String part, int index ->
            if (index != 0)
                baseMarkupBuilderDocument.w.pgNum()
            baseMarkupBuilderDocument.w.t(part, RUN_TEXT_OPTIONS)
        }
    }

}
