package com.craigburke.document.builder

import com.craigburke.document.builder.render.Renderable
import com.craigburke.document.builder.render.TableRenderer
import com.craigburke.document.builder.render.TextBlockRenderer
import com.craigburke.document.core.builder.DocumentBuilder
import com.craigburke.document.core.builder.RenderState
import com.craigburke.document.core.dom.PageBreak
import com.craigburke.document.core.dom.attribute.HeaderFooterOptions
import com.craigburke.document.core.dom.attribute.TextBlockType
import com.craigburke.document.core.dom.block.BlockNode
import com.craigburke.document.core.dom.block.Paragraph
import com.craigburke.document.core.dom.block.Table
import com.craigburke.document.core.dom.text.Heading
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder
import org.apache.pdfbox.pdmodel.PDDocumentCatalog
import org.apache.pdfbox.pdmodel.common.PDMetadata

import java.time.OffsetDateTime

/**
 * Builder for PDF documents
 * @author Craig Burke
 */
@Slf4j
@InheritConstructors
class PdfDocumentBuilder extends DocumentBuilder<PdfDocument> {

    @Override
    PdfDocument createDocument(Map attributes) {
        document = new PdfDocument(attributes)
        document
    }

    @Override
    void writeDocument() {
        document.x = document.margin.left
        document.y = document.margin.top

        document.children.each { child ->
            if (child instanceof TextBlockType) {
                addTextBlockType(child)
            } else if (child instanceof PageBreak) {
                document.addPage()
            } else if (child instanceof Table) {
                addTable(child)
            } else {
                log.warn('Unknown child in document: {}', child.getClass())
            }
        }

        addHeaderFooter()
        addMetadata()

        document.saveAndClosePdf(out)
    }

    void addTextBlockType(TextBlockType textBlockType) {
        if (renderState == RenderState.PAGE) {
            BigDecimal pageWidth = document.width - document.margin.left - document.margin.right
            BigDecimal maxLineWidth = pageWidth - textBlockType.margin.left - textBlockType.margin.right
            BigDecimal renderStartX = document.margin.left + textBlockType.margin.left

            document.x = renderStartX
            document.scrollDownPage(textBlockType.margin.top)

            TextBlockRenderer paragraphRenderer =
              new TextBlockRenderer(textBlockType, document, renderStartX, maxLineWidth)

            while (!paragraphRenderer.fullyParsed) {
                paragraphRenderer.parse(document.remainingPageHeight)
                paragraphRenderer.render(document.y)
                if (paragraphRenderer.fullyParsed) {
                    document.scrollDownPage(paragraphRenderer.renderedHeight)
                } else {
                    document.addPage()
                }
            }
            document.scrollDownPage(textBlockType.margin.bottom)
        }
    }

    void addTable(Table table) {
        if (renderState == RenderState.PAGE) {
            document.x = table.margin.left + document.margin.left
            document.scrollDownPage(table.margin.top)
            TableRenderer tableRenderer = new TableRenderer(table, document, document.x)
            while (!tableRenderer.fullyParsed) {
                tableRenderer.parse(document.remainingPageHeight)
                tableRenderer.render(document.y)

                if (tableRenderer.fullyParsed) {
                    document.scrollDownPage(tableRenderer.renderedHeight)
                } else {
                    document.addPage()
                }
            }
            document.scrollDownPage(table.margin.bottom)
        }
    }

    private void addHeaderFooter() {
        int pageCount = document.pages.size()
        HeaderFooterOptions options = new HeaderFooterOptions(pageCount: pageCount, dateGenerated: OffsetDateTime.now())

        (1..pageCount).each { int pageNumber ->
            document.pageNumber = pageNumber
            options.pageNumber = pageNumber

            if (headerClosure) {
                renderState = RenderState.HEADER
                BlockNode header = buildHeaderNode(options)
                renderHeaderFooter(header)
            }
            if (footerClosure) {
                renderState = RenderState.FOOTER
                BlockNode footer = buildFooterNode(options)
                renderHeaderFooter(footer)
            }
        }

        renderState = RenderState.PAGE
    }

    private void renderHeaderFooter(BlockNode headerFooter) {
        float startX = document.margin.left + headerFooter.margin.left
        float startY

        if (renderState == RenderState.HEADER) {
            startY = headerFooter.margin.top
        } else {
            float pageBottom = document.pageBottomY + document.margin.bottom
            startY = (pageBottom - getElementHeight(headerFooter) - headerFooter.margin.bottom).floatValue()
        }

        Renderable renderer =
          headerFooter instanceof Paragraph ? new TextBlockRenderer(headerFooter, document, startX, document.width)
                                            : new TableRenderer(headerFooter as Table, document, startX)

        renderer.parse(document.height)
        renderer.render(startY)
    }

    private float getElementHeight(element) {
        float width = document.width - document.margin.right - document.margin.left

        if (element instanceof Paragraph) {
            new TextBlockRenderer(element, document, 0, width).totalHeight
        } else if (element instanceof Table) {
            new TableRenderer(element, document, 0).totalHeight
        } else {
            0
        }
    }

    private void addMetadata() {
        ByteArrayOutputStream xmpOut = new ByteArrayOutputStream()
        MarkupBuilder xml = new MarkupBuilder(xmpOut.newWriter())

        xml.document(marginTop: "${document.margin.top}", marginBottom: "${document.margin.bottom}",
                     marginLeft: "${document.margin.left}", marginRight: "${document.margin.right}") {

            delegate = xml
            resolveStrategy = Closure.DELEGATE_FIRST

            document.children.each { child ->
                if (child instanceof Paragraph) {
                    addParagraphToMetadata(delegate, child)
                } else if (child instanceof Heading) {
                    addHeadingToMetadata(delegate, child)
                } else if (child instanceof Table) {
                    addTableToMetadata(delegate, child)
                }
            }
        }

        PDDocumentCatalog catalog = document.pdDocument.documentCatalog
        InputStream inputStream = new ByteArrayInputStream(xmpOut.toByteArray())

        PDMetadata metadata = new PDMetadata(document.pdDocument, inputStream)
        catalog.metadata = metadata
    }

    private static void addHeadingToMetadata(xml, Heading heading) {
        xml.heading(marginTop: "${heading.margin.top}",
                    marginBottom: "${heading.margin.bottom}",
                    marginLeft: "${heading.margin.left}",
                    marginRight: "${heading.margin.right}")
    }

    private static void addParagraphToMetadata(xml, Paragraph paragraphNode) {
        xml.paragraph(marginTop: "${paragraphNode.margin.top}",
                      marginBottom: "${paragraphNode.margin.bottom}",
                      marginLeft: "${paragraphNode.margin.left}",
                      marginRight: "${paragraphNode.margin.right}") {
            paragraphNode.getAllImages().each {
                xml.image()
            }
        }
    }

    private void addTableToMetadata(xml, Table tableNode) {
        xml.table(columns: tableNode.columnCount, width: tableNode.width, borderSize: tableNode.border.size) {
            delegate = xml
            resolveStrategy = Closure.DELEGATE_FIRST

            tableNode.rows.each { rowNode ->
                row {
                    rowNode.cells.each { cellNode ->
                        cell(width: "${cellNode.width ?: 0}")
                    }
                }
            }
        }
    }

}
