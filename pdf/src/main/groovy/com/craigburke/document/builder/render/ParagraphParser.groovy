package com.craigburke.document.builder.render

import com.craigburke.document.core.dom.LineBreak
import com.craigburke.document.core.dom.attribute.Font
import com.craigburke.document.core.dom.block.text.TextBlock
import com.craigburke.document.core.dom.text.Text

import com.craigburke.document.builder.PdfFont

import org.apache.pdfbox.pdmodel.font.PDFont

/**
 * Parses the paragraph content so it can be split into lines
 * @author Craig Burke
 */
class ParagraphParser {

    static List<ParagraphLine> getLines(TextBlock paragraph, float maxLineWidth) {
        def lines = []

        def currentChunk = []
        def paragraphChunks = []
        paragraphChunks << currentChunk

        paragraph.children.each { child ->
            if (child.getClass() == LineBreak) {
                currentChunk = []
                paragraphChunks << currentChunk
            } else {
                currentChunk << child
            }
        }

        paragraphChunks.each { lines += parseParagraphChunk(it, paragraph, maxLineWidth) }
        lines
    }

    private static List<ParagraphLine> parseParagraphChunk(chunk, TextBlock paragraph, float maxLineWidth) {
        def chunkLines = []

        ParagraphLine currentLine = new ParagraphLine(paragraph, maxLineWidth)
        chunkLines << currentLine

        PDFont pdfFont

        chunk.each { node ->
            if (node instanceof Text) {
                Font font = node.font
                pdfFont = PdfFont.getFont(font)
                String remainingText = node.value

                while (remainingText) {
                    BigDecimal textWidth = pdfFont.getStringWidth(remainingText) / 1000 * font.size

                    if (currentLine.contentWidth + textWidth > maxLineWidth) {
                        String text = getTextUntilBreak(remainingText, pdfFont, font.size, currentLine.remainingWidth)
                        int nextPosition = text.size()
                        remainingText = remainingText[nextPosition..-1].trim()
                        int elementWidth = pdfFont.getStringWidth(text) / 1000 * font.size
                        currentLine.contentWidth += elementWidth

                        currentLine.elements << new TextElement(pdfFont: pdfFont, text: text,
                                node: node, width: elementWidth)

                        currentLine = new ParagraphLine(paragraph, maxLineWidth)
                        chunkLines << currentLine
                    } else {
                        currentLine.elements << new TextElement(pdfFont: pdfFont, text: remainingText,
                                node: node, width: textWidth)
                        remainingText = ''
                        currentLine.contentWidth += textWidth
                    }

                }
            } else {
                if (currentLine.remainingWidth < node.width) {
                    currentLine = new ParagraphLine(paragraph, maxLineWidth)
                    chunkLines << currentLine
                }
                currentLine.contentWidth += node.width
                currentLine.elements << new ImageElement(node: node)
            }
        }

        chunkLines
    }

    private static String getTextUntilBreak(String text, PDFont font, BigDecimal fontSize, BigDecimal width) {
        String result = ''
        String previousResult = ''
        boolean spaceBreakpointFound = false

        String[] words = text.split()*.trim()

        int wordIndex = 0
        BigDecimal resultWidth = 0
        while (words && resultWidth < width && wordIndex < words.size()) {
            result += (wordIndex == 0 ? '' : ' ') + words[wordIndex]
            resultWidth = getTextWidth(result, font, fontSize)

            if (resultWidth == width) {
                spaceBreakpointFound = true
                break
            } else if (resultWidth < width) {
                spaceBreakpointFound = true
            } else if (resultWidth > width) {
                result = previousResult
                break
            }
            wordIndex++
            previousResult = result
        }

        if (!spaceBreakpointFound) {
            // Fall back to breaking line in the middle of a word
            int currentCharacter = 0
            while (getTextWidth(result, font, fontSize) < width) {
                result += text[currentCharacter]
                currentCharacter++
            }
            result = result.subSequence(0, result.length() - 1)
        }

        result
    }

    private static BigDecimal getTextWidth(String text, PDFont font, BigDecimal fontSize) {
        font.getStringWidth(text) / 1000 * fontSize
    }

}

