package com.craigburke.document.builder.util

import com.craigburke.document.core.dom.Image
import com.craigburke.document.core.dom.block.Document
import com.craigburke.document.core.dom.block.Paragraph
import com.craigburke.document.core.dom.block.Table
import com.craigburke.document.core.dom.block.table.Cell
import com.craigburke.document.core.dom.block.table.Row
import com.craigburke.document.core.dom.text.Heading

import com.craigburke.document.builder.PdfDocument

import org.apache.pdfbox.pdmodel.PDDocument

/**
 * Creates a Document object based on byte content of Pdf file
 * @author Craig Burke
 */
class PdfDocumentLoader {

    PDDocument pdfDoc

    PdfDocumentLoader(PDDocument pdfDoc) {
        this.pdfDoc = pdfDoc
    }

    PdfDocument load() {
        PdfDocument document = new PdfDocument()

        def metaData = new XmlParser().parse(pdfDoc.documentCatalog.metadata.createInputStream())

        document.margin.top = new BigDecimal(metaData.'@marginTop')
        document.margin.bottom = new BigDecimal(metaData.'@marginBottom')
        document.margin.left = new BigDecimal(metaData.'@marginLeft')
        document.margin.right = new BigDecimal(metaData.'@marginRight')

        metaData.each {
            if (it.name() == 'paragraph') {
                loadParagraph(document, it)
            } else if (it.name() == 'heading') {
                loadHeading(document, it)
            } else if (it.name() == 'table') {
                loadTable(document, it)
            }
        }

        loadChildren(document)
        pdfDoc.close()
        document
    }

    static PdfDocument load(byte[] data) {
        PdfDocumentLoader loader = new PdfDocumentLoader(PDDocument.load(new ByteArrayInputStream(data)))
        loader.load()
    }

    private void loadParagraph(Document document, Node paragraphNode) {
        Paragraph paragraph = new Paragraph(parent: document)
        paragraph.margin.top = new BigDecimal(paragraphNode.'@marginTop')
        paragraph.margin.bottom = new BigDecimal(paragraphNode.'@marginBottom')
        paragraph.margin.left = new BigDecimal(paragraphNode.'@marginLeft')
        paragraph.margin.right = new BigDecimal(paragraphNode.'@marginRight')

        paragraphNode.image.each {
            paragraph.addToChildren(new Image())
        }

    }

    private void loadHeading(Document document, Node headingNode) {
        Heading heading = new Heading(parent: document)
        heading.margin.top = new BigDecimal(headingNode.'@marginTop')
        heading.margin.bottom = new BigDecimal(headingNode.'@marginBottom')
        heading.margin.left = new BigDecimal(headingNode.'@marginLeft')
        heading.margin.right = new BigDecimal(headingNode.'@marginRight')
    }

    private void loadTable(Document document, Node tableNode) {
        def table = new Table(parent: document, width: new BigDecimal(tableNode.'@width'))
        tableNode.row.each {rowNode ->
            Row row = new Row()
            rowNode.cell.each {cellNode ->
                Cell cell = new Cell(width: new BigDecimal(cellNode.'@width'))
                cell.addToChildren(new Paragraph())
                row.addToChildren(cell)
            }
            table.addToChildren(row)
        }
    }

    private void loadChildren(Document document) {
        // Set content and margins based on text position
        def extractor = new PdfContentExtractor(document)
        File extractedFile = new File('testPdf')
        extractedFile.withWriter {writer ->
            extractor.writeText(pdfDoc, writer)
        }
        extractedFile.delete()
    }

}
