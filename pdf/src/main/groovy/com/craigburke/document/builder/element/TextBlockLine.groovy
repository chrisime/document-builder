package com.craigburke.document.builder.element

import com.craigburke.document.builder.PdfFont
import com.craigburke.document.core.dom.attribute.Font
import com.craigburke.document.core.dom.attribute.TextBlockType
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

/**
 * A paragraph line
 * @author Craig Burke
 */
@CompileStatic
class TextBlockLine {

    final BigDecimal             maxWidth
    final TextBlockType          textBlock
    final List<ParagraphElement> elements

    BigDecimal contentWidth = 0.0

    TextBlockLine(TextBlockType textBlock, float maxWidth) {
        this.textBlock = textBlock
        this.maxWidth = maxWidth
        this.elements = []
    }

    BigDecimal getRemainingWidth() {
        maxWidth - contentWidth
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    BigDecimal getContentHeight() {
        elements.collect {
            if (it instanceof TextElement) {
                it.node.font.size
            } else if (it instanceof ImageElement) {
                it.node.height
            } else {
                0
            }
        }.max() ?: textBlock.font.size
    }

    BigDecimal getTotalHeight() {
        contentHeight + lineSpacing
    }

    BigDecimal getLineSpacing() {
        if (textBlock.lineSpacing) {
            textBlock.lineSpacing
        } else {
            TextElement largestElement = elements.findAll { it instanceof TextElement }.max {
                (it as TextElement).node.font.size
            } as TextElement
            Font maxFont = largestElement?.node?.font ?: textBlock.font

            BigDecimal xHeight = PdfFont.getXHeight(maxFont)
            Math.round(((maxFont.size - xHeight) * textBlock.lineSpacingMultiplier).doubleValue())
        }
    }

}
