package com.craigburke.document.builder.util

import com.craigburke.document.core.dom.BaseNode
import com.craigburke.document.core.dom.attribute.Font
import com.craigburke.document.core.dom.attribute.TextBlockType
import com.craigburke.document.core.dom.block.Document
import com.craigburke.document.core.dom.block.Paragraph
import com.craigburke.document.core.dom.block.Table
import com.craigburke.document.core.dom.block.table.Cell
import com.craigburke.document.core.dom.text.Heading
import com.craigburke.document.core.dom.text.Text
import com.craigburke.document.core.dom.text.TextNode

import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.TextPosition
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Extract the content from a pdf file from paragraphs and tables. There are limitations but works for simple tests
 * It can't split text reliably when a paragraph has no top/bottom margins
 * @author Craig Burke
 */
class PdfContentExtractor extends PDFTextStripper {

    private static final Logger logger = LoggerFactory.getLogger(PdfContentExtractor)

    private tablePosition = [row: 0, cell: 0]
    private int currentChildNumber = 0
    private Document document
    private TextPosition lastPosition

    PdfContentExtractor(Document document) {
        super.setSortByPosition(true)
        this.document = document
    }

    private BaseNode getCurrentChild() {
        if (!document.children || document.children.size() < currentChildNumber) {
            null
        } else {
            document.children[currentChildNumber - 1]
        }
    }

    @Override
    void processTextPosition(TextPosition text) {
        if (text.unicode == ' ') {
            return
        }

        updateChildNumber(text)

        Font currentFont = new Font(family: text.font.baseFont, size: text.fontSize)
        TextNode textNode

        if (currentChild instanceof Paragraph) {
            textNode = processParagraph(text, currentFont)
        } else if (currentChild instanceof Table) {
            textNode = processTable(text, currentFont)
        } else if (currentChild instanceof Heading) {
            textNode = processHeading(text, currentFont)
        } else {
            logger.warn("Unexpected child in process text position: {}", currentChild.getClass())
        }

        textNode?.value += text.unicode
        lastPosition = text
    }

    private TextNode processTable(TextPosition text, Font font) {
        TextNode textNode

        Cell cell = (currentChild as Table).rows[tablePosition.row].cells[tablePosition.cell]
        Paragraph paragraph = cell.children[0]
        paragraph.font = paragraph.font ?: font.clone()

        if (!paragraph.children || isNewSection(text)) {
            textNode = getText(paragraph, font)
        } else {
            textNode = paragraph.children.last() as TextNode
        }

        textNode
    }

    private TextNode processParagraph(TextPosition text, Font font) {
        TextNode textNode

        Paragraph paragraph = currentChild as Paragraph
        if (!paragraph.children) {
            textNode = getText(paragraph, font)
            setTextBlockTypeProperties(paragraph, text, font)
        } else if (isNewSection(text)) {
            textNode = getText(paragraph, font)
        } else {
            textNode = paragraph.children.last() as TextNode
        }

        textNode
    }

    private TextNode processHeading(TextPosition text, Font font) {
        setTextBlockTypeProperties(currentChild as Heading, text, font)
        currentChild as Heading
    }

    private void setTextBlockTypeProperties(TextBlockType textBlockType, TextPosition text, Font font) {
        textBlockType.font = font.clone()
        textBlockType.margin.left = text.x - document.margin.left
        BigDecimal totalPageWidth = text.pageWidth - document.margin.right - document.margin.left
        textBlockType.margin.right = totalPageWidth - text.width - textBlockType.margin.left

        BigDecimal lineHeight = font.size + (font.size * textBlockType.lineSpacingMultiplier)
        BigDecimal textOffset = lineHeight - font.size

        BigDecimal topMargin = Math.ceil(text.y - document.margin.top - lineHeight + textOffset)
        textBlockType.margin.top = Math.round(topMargin)
    }

    private Text getText(Paragraph paragraph, Font font) {
        new Text(parent: paragraph, value: '', font: font.clone())
    }

    private void updateChildNumber(TextPosition current) {
        if (!lastPosition || (lastPosition.y != current.y && current.unicode != ' ')) {
            currentChildNumber++
            tablePosition.row = 0
            tablePosition.cell = 0
        }
    }

    private boolean isNewSection(TextPosition current) {
        boolean isNewSection = false

        if (!lastPosition) {
            isNewSection = true
        } else if (current.font.baseFont != lastPosition.font.baseFont) {
            isNewSection = true
        } else if (current.fontSizeInPt != lastPosition.fontSizeInPt) {
            isNewSection = true
        }

        isNewSection
    }

}
