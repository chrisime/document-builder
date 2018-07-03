package com.craigburke.document.builder

import com.craigburke.document.core.dom.Image
import com.craigburke.document.core.dom.attribute.Font
import com.craigburke.document.core.dom.block.Paragraph
import com.craigburke.document.core.dom.block.Table
import com.craigburke.document.core.dom.block.table.Cell
import com.craigburke.document.core.dom.block.table.Row
import com.craigburke.document.core.dom.text.Text

import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph

import static com.craigburke.document.core.unit.UnitUtil.twipToPoint

/**
 * Creates a Document object based on byte content of Word file
 * @author Craig Burke
 */
class WordDocumentLoader {

    XWPFDocument xwpfDocument

    WordDocumentLoader(XWPFDocument xwpfDocument) {
        this.xwpfDocument = xwpfDocument
    }

    WordDocument load() {
        WordDocument document = new WordDocument()
        def documentMargin = xwpfDocument.document.body.sectPr.pgMar
        document.margin.top = twipToPoint(documentMargin.top)
        document.margin.bottom = twipToPoint(documentMargin.bottom)
        document.margin.left = twipToPoint(documentMargin.left)
        document.margin.right = twipToPoint(documentMargin.right)

        loadParagraphs(document)
        loadTables(document)
        document
    }

    static WordDocument load(byte[] data) {
        WordDocumentLoader loader = new WordDocumentLoader(new XWPFDocument(new ByteArrayInputStream(data)))
        loader.load()
    }

    private loadParagraphs(WordDocument document) {
        document.children = getParagraphs(xwpfDocument.paragraphs)
    }

    private loadTables(WordDocument document) {

        xwpfDocument.tables.each {tableItem ->
            Table table = new Table(width: twipToPoint(tableItem.width), parent: document)

            tableItem.rows.each { rowItem ->
                Row row = new Row(parent: table)
                rowItem.tableCells.each { columnItem ->
                    Cell column = new Cell(parent: row)
                    BigInteger padding = columnItem.CTTc.tcPr.tcMar.left.w
                    BigInteger width = columnItem.CTTc.tcPr.tcW.w
                    column.width = twipToPoint(width + (padding * 2))

                    column.children = getParagraphs(columnItem.paragraphs)
                }
            }
        }
    }

    private List getParagraphs(List<XWPFParagraph> paragraphs) {
        def items = []

        paragraphs.each {paragraph ->
            if (paragraph.runs) {
                Paragraph p = new Paragraph()
                p.margin.bottom = twipToPoint(paragraph.spacingAfter)
                p.margin.top = twipToPoint(paragraph.spacingBefore)
                def indent = paragraph.CTP.PPr.ind
                p.margin.left = twipToPoint(indent?.left ?: 0 as BigDecimal).toInteger()
                p.margin.right = twipToPoint(indent?.right ?: 0 as BigDecimal).toInteger()

                items << p

                paragraph.runs.each { run ->
                    Font font = new Font(family: run.fontFamily, size: run.fontSize)
                    p.font = p.font ?: font

                    if (run.embeddedPictures) {
                        p.addToChildren(new Image(data: run.embeddedPictures[0].pictureData.data))
                    } else {
                        def text = new Text(value: run.toString())
                        text.font = font
                        p.addToChildren(text)
                    }
                }
            }

        }

        items
    }

}
