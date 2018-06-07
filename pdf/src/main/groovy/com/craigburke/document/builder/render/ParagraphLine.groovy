package com.craigburke.document.builder.render

import com.craigburke.document.core.dom.attribute.Font
import com.craigburke.document.core.dom.block.text.TextBlock

import com.craigburke.document.builder.PdfFont

/**
 * A paragraph line
 * @author Craig Burke
 */
class ParagraphLine {

    TextBlock paragraph
    final float maxWidth
    float contentWidth = 0
    List<Renderable> elements = []

    ParagraphLine(TextBlock paragraph, float maxWidth) {
        this.paragraph = paragraph
        this.maxWidth = maxWidth
    }

    float getRemainingWidth() {
        maxWidth - contentWidth
    }

    float getContentHeight() {
        elements.collect {
            if (it instanceof TextElement) { it.node.font.size }
            else if (it instanceof ImageElement ) { it.node.height }
            else { 0 }
        }.max() ?: paragraph.font.size
    }

    float getTotalHeight() {
        contentHeight + lineSpacing
    }

    float getLineSpacing() {
        if (paragraph.lineSpacing) {
            paragraph.lineSpacing
        }
        else {
            Font maxFont = elements
                    .findAll { it instanceof TextElement }
                    .max { it.node.font.size }?.node?.font ?: paragraph.font

            BigDecimal xHeight = PdfFont.getXHeight(maxFont)
            Math.round((maxFont.size - xHeight) * paragraph.lineSpacingMultiplier)
        }
    }

}
