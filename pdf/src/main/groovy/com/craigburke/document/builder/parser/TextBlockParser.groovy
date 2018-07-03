package com.craigburke.document.builder.parser

import com.craigburke.document.builder.PdfFont
import com.craigburke.document.builder.element.ImageElement
import com.craigburke.document.builder.element.TextBlockLine
import com.craigburke.document.builder.element.TextElement
import com.craigburke.document.core.dom.BaseNode
import com.craigburke.document.core.dom.Image
import com.craigburke.document.core.dom.LineBreak
import com.craigburke.document.core.dom.attribute.Font
import com.craigburke.document.core.dom.attribute.TextBlockType
import com.craigburke.document.core.dom.block.Paragraph
import com.craigburke.document.core.dom.text.Heading
import com.craigburke.document.core.dom.text.TextNode
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.pdfbox.pdmodel.font.PDFont

/**
 * Parses the paragraph content so it can be split into lines
 * @author Craig Burke
 */
@Slf4j
@CompileStatic
class TextBlockParser {

    private TextBlockLine       currentLine
    private List<TextBlockLine> chunkLines
    private TextBlockType       textBlockType
    private float               maxLineWidth
    private PDFont              pdfFont

    static List<TextBlockLine> getLines(TextBlockType textBlockType, float maxLineWidth) {
        TextBlockParser parser = new TextBlockParser(textBlockType: textBlockType, maxLineWidth: maxLineWidth)

        if (textBlockType instanceof Paragraph) {
            parser.parseParagraph()
        } else if (textBlockType instanceof Heading) {
            parser.parseHeading()
        } else {
            log.warn('Unexpected textblock type: {}', textBlockType.getClass().canonicalName)
            []
        }
    }

    List<TextBlockLine> parseParagraph() {
        List<TextBlockLine> lines = []

        List<List<BaseNode>> paragraphChunks = []
        Paragraph paragraph = textBlockType as Paragraph

        List<BaseNode> currentChunk = []
        paragraphChunks << currentChunk

        paragraph.children.each { child ->
            if (child instanceof LineBreak) {
                currentChunk = []
                paragraphChunks << currentChunk
            } else {
                currentChunk << child
            }
        }

        paragraphChunks.each { lines += parseParagraphChunk(it) }
        lines
    }

    List<TextBlockLine> parseHeading() {
        chunkLines = []

        currentLine = new TextBlockLine(textBlockType, maxLineWidth)
        chunkLines << currentLine

        parseText(textBlockType as Heading)
        chunkLines
    }

    private List<TextBlockLine> parseParagraphChunk(List<BaseNode> chunk) {
        chunkLines = []

        currentLine = new TextBlockLine(textBlockType, maxLineWidth)
        chunkLines << currentLine

        chunk.each { BaseNode node ->
            if (node instanceof TextNode) {
                parseText(node)
            } else if (node instanceof Image) {
                if (currentLine.remainingWidth < node.width) {
                    currentLine = new TextBlockLine(textBlockType, maxLineWidth)
                    chunkLines << currentLine
                }
                currentLine.contentWidth += node.width
                currentLine.elements << new ImageElement(node: node as Image)
            } else {
                log.warn('Unexpected paragraph child: {}', node.getClass().canonicalName)
            }
        }

        chunkLines
    }

    private void parseText(TextNode node) {
        Font font = node.font
        pdfFont = PdfFont.getFont(font)
        String remainingText = node.value

        while (remainingText) {
            BigDecimal textWidth = pdfFont.getStringWidth(remainingText) / 1000 * font.size

            if (currentLine.contentWidth + textWidth > maxLineWidth) {
                String text = getTextUntilBreak(remainingText, font.size, currentLine.remainingWidth)
                int nextPosition = text.size()
                remainingText = remainingText[nextPosition..-1].trim()
                BigDecimal elementWidth = pdfFont.getStringWidth(text) / 1000 * font.size
                currentLine.contentWidth += elementWidth

                currentLine.elements << new TextElement(pdfFont: pdfFont, text: text,
                                                        node: node, width: elementWidth.intValue())

                currentLine = new TextBlockLine(currentLine.textBlock, maxLineWidth)
                chunkLines << currentLine
            } else {
                currentLine.elements << new TextElement(pdfFont: pdfFont, text: remainingText,
                                                        node: node, width: textWidth.intValue())
                remainingText = ''
                currentLine.contentWidth += textWidth
            }
        }
    }

    private String getTextUntilBreak(String text, BigDecimal fontSize, BigDecimal width) {
        String result = ''
        String previousResult = ''
        boolean spaceBreakpointFound = false

        String[] words = text.split()*.trim()

        int wordIndex = 0
        BigDecimal resultWidth = 0
        while (words && resultWidth < width && wordIndex < words.size()) {
            result += (wordIndex == 0 ? '' : ' ') + words[wordIndex]
            resultWidth = getTextWidth(result, fontSize)

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
            while (getTextWidth(result, fontSize) < width) {
                result += text[currentCharacter]
                currentCharacter++
            }
            result = result.subSequence(0, result.length() - 1)
        }

        result
    }

    private BigDecimal getTextWidth(String text, BigDecimal fontSize) {
        pdfFont.getStringWidth(text) / 1000 * fontSize
    }

}

