package com.craigburke.document.builder.render

import com.craigburke.document.builder.PdfDocument

/**
 * Trait shared by render elements
 */
trait Renderable {

    float       startX
    PdfDocument pdfDocument

    int renderCount = 0

    abstract void parse(float maxHeight)

    abstract boolean getFullyParsed()

    abstract float getTotalHeight()

    abstract float getParsedHeight()

    abstract float getRenderedHeight()
    abstract void renderElement(float startY)

    void render(float startY) {
        float currentX = pdfDocument.x
        float currentY = pdfDocument.y
        pdfDocument.y = startY
        pdfDocument.x = startX
        renderElement(startY)
        pdfDocument.x = currentX
        pdfDocument.y = currentY
        renderCount = renderCount + 1
    }

    float getCurrentHeight() {
        renderedHeight ?: parsedHeight
    }

    boolean getOnFirstPage() {
        (renderCount <= 1)
    }

}
