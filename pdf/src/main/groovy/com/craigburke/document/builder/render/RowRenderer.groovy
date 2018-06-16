package com.craigburke.document.builder.render

import com.craigburke.document.builder.PdfDocument
import com.craigburke.document.core.Cell
import com.craigburke.document.core.Row
import com.craigburke.document.core.Table
import org.apache.pdfbox.pdmodel.PDPageContentStream

/**
 * Rendering element for the Row node
 * @author Craig Burke
 */
class RowRenderer implements Renderable {

    Row row
    List<CellRenderer> cellRenderers = []
    float renderedHeight = 0

    RowRenderer(Row row, PdfDocument pdfDocument, float startX) {
        this.row = row
        this.startX = startX
        this.pdfDocument = pdfDocument

        Table table = row.parent
        float columnX = (startX + table.border.size).floatValue()
        row.children.each { Cell column ->
            cellRenderers << new CellRenderer(column, pdfDocument, columnX)
            columnX += column.width + table.border.size
        }
    }

    void parse(float height) {
        cellRenderers*.parse(height)
        cellRenderers*.currentRowHeight = parsedHeight
    }

    boolean getFullyParsed() {
        cellRenderers.every { it.fullyParsed }
    }

    float getTotalHeight() {
        cellRenderers*.totalHeight.max() + table.border.size
    }

    float getParsedHeight() {
        float parsedHeight = (float) cellRenderers*.parsedHeight.max() ?: 0.0F
        if (fullyParsed && parsedHeight > 0) {
            parsedHeight += table.border.size
        }
        parsedHeight
    }

    void renderElement(float startY) {
        if (parsedHeight == 0) {
            return
        }

        renderBackgrounds(startY)
        renderBorders(startY)
        cellRenderers*.render(startY)
        renderedHeight = parsedHeight
    }

    private Table getTable() {
        row.parent
    }

    float getTableBorderOffset() {
        table.border.size.floatValue() / 2f
    }

    private void renderBackgrounds(float startY) {
        float backgroundStartY = (startY + parsedHeight).floatValue()
        if (!firstRow) {
            backgroundStartY += tableBorderOffset
        }
        if (!fullyParsed) {
            backgroundStartY -= table.border.size
        }

        float translatedStartY = pdfDocument.translateY(backgroundStartY)
        PDPageContentStream contentStream = pdfDocument.contentStream

        cellRenderers.each { CellRenderer columnElement ->
            Cell column = columnElement.cell
            if (column.background) {
                boolean isLastColumn = (column == column.parent.children.last())
                contentStream.setNonStrokingColor(*column.background.rgb)
                float startX = (columnElement.startX - tableBorderOffset).floatValue()
                float width = (column.width + (isLastColumn ? table.border.size : tableBorderOffset)).floatValue()
                float height = (parsedHeight - (fullyParsed ? 0 : tableBorderOffset)).floatValue()
                height += ((fullyParsed && !onFirstPage) ? table.border.size : 0)
                contentStream.addRect(startX, translatedStartY, width, height)
                contentStream.fill()
            }
        }
    }

    private void renderBorders(float startY) {
        if (!table.border.size) {
            return
        }

        float translatedYTop = pdfDocument.translateY(startY - tableBorderOffset)
        float translatedYBottom = pdfDocument.translateY(startY + parsedHeight)
        float rowStartX = (startX - tableBorderOffset).floatValue()
        float rowEndX = (startX + table.width).floatValue()

        PDPageContentStream contentStream = pdfDocument.contentStream
        setBorderOptions(contentStream)

        if (firstRow || isTopOfPage(startY)) {
            contentStream.moveTo(rowStartX, translatedYTop)
            contentStream.lineTo(rowEndX, translatedYTop)
            contentStream.stroke()
        }

        cellRenderers.eachWithIndex { columnElement, i ->
            if (i == 0) {
                float firstLineStartX = (columnElement.startX - table.border.size).floatValue()
                contentStream.moveTo(firstLineStartX, translatedYTop)
                contentStream.lineTo(firstLineStartX, translatedYBottom)
                contentStream.stroke()
            }
            float columnStartX = (columnElement.startX - table.border.size).floatValue()
            float columnEndX = (columnElement.startX + columnElement.cell.width + tableBorderOffset).floatValue()

            contentStream.moveTo(columnEndX, translatedYTop)
            contentStream.lineTo(columnEndX, translatedYBottom)
            contentStream.stroke()

            if (fullyParsed && columnElement.onLastRowspanRow) {
                contentStream.moveTo(columnStartX, translatedYBottom)
                contentStream.lineTo(columnEndX, translatedYBottom)
                contentStream.stroke()
            }
        }
    }

    private setBorderOptions(PDPageContentStream contentStream) {
        def borderColor = table.border.color.rgb
        contentStream.setStrokingColor(*borderColor)
        contentStream.setLineWidth(table.border.size)
    }

    boolean isTopOfPage(float y) {
        (y == pdfDocument.document.margin.top)
    }

    boolean isFirstRow() {
        (row == row.parent.children.first())
    }

}
