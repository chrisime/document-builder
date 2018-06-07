package com.craigburke.document.builder

import com.craigburke.document.core.dom.Image
import com.craigburke.document.core.dom.block.Document
import com.craigburke.document.core.dom.block.Table
import com.craigburke.document.core.dom.block.table.Cell
import com.craigburke.document.core.dom.block.table.Row
import com.craigburke.document.core.dom.block.text.TextBlock

import org.apache.pdfbox.pdmodel.PDDocument

/**
 * Creates a Document object based on byte content of Pdf file
 * @author Craig Burke
 */
class PdfDocumentLoader {

    static Document load(byte[] data) {
        PDDocument pdfDoc = PDDocument.load(new ByteArrayInputStream(data))
        Document document = new Document(element: pdfDoc)

        def metaData = new XmlParser().parse(pdfDoc.documentCatalog.metadata.createInputStream())

        document.margin.top = new BigDecimal(metaData.'@marginTop')
        document.margin.bottom = new BigDecimal(metaData.'@marginBottom')
        document.margin.left = new BigDecimal(metaData.'@marginLeft')
        document.margin.right = new BigDecimal(metaData.'@marginRight')

        metaData.each {
            if (it.name() == 'paragraph') {
                this.loadParagraph(document, it)
            } else {
                this.loadTable(document, it)
            }
        }

        loadChildren(document)
        pdfDoc.close()
        document
    }

    private static loadParagraph(Document document, paragraphNode) {
        def paragraph = new TextBlock(parent: document)
        paragraph.margin.top = new BigDecimal(paragraphNode.'@marginTop')
        paragraph.margin.bottom = new BigDecimal(paragraphNode.'@marginBottom')
        paragraph.margin.left = new BigDecimal(paragraphNode.'@marginLeft')
        paragraph.margin.right = new BigDecimal(paragraphNode.'@marginRight')

        paragraphNode.image.each {
            paragraph.children << new Image(parent: paragraph)
        }

        document.children << paragraph
    }

    private static loadTable(Document document, tableNode) {
        def table = new Table(parent: document, width: new BigDecimal(tableNode.'@width'))
        tableNode.row.each { rowNode ->
            Row row = new Row()
            rowNode.cell.each { cellNode ->
                def cell = new Cell(width: new BigDecimal(cellNode.'@width'))
                cell.children << new TextBlock()
                row.children << cell
            }
            table.children << row
        }
        document.children << table
    }

    private static void loadChildren(Document document) {
        // Set content and margins based on text position
        def extractor = new PdfContentExtractor(document)
        File extractedFile = new File('testPdf')
        extractedFile.withWriter { writer ->
            extractor.writeText(document.element, writer)
        }
        extractedFile.delete()
    }

}
