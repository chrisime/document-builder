package com.craigburke.document.builder

import com.craigburke.document.builder.render.TableRenderer
import com.craigburke.document.builder.test.RendererTestBase
import com.craigburke.document.core.dom.attribute.Font
import com.craigburke.document.core.dom.attribute.Margin
import com.craigburke.document.core.dom.block.Document
import com.craigburke.document.core.dom.block.Paragraph
import com.craigburke.document.core.dom.block.Table
import com.craigburke.document.core.dom.block.table.Cell
import com.craigburke.document.core.dom.block.table.Row
import spock.lang.Ignore
import spock.lang.Shared

/**
 * Tables element tests
 * @author Craig Burke
 */
@Ignore
class TableRendererSpec extends RendererTestBase {

    @Shared Table table
    @Shared TableRenderer tableRenderer
    @Shared float defaultRowHeight
    @Shared int rowCount = 2

    def setup() {
        table = new Table(margin:Margin.NONE, padding:20, border:[size:3], columns:[1])
        Paragraph paragraph = makeParagraph(5)
        paragraph.margin = Margin.NONE
        tableRenderer = makeTableElement(table, paragraph, rowCount)
        defaultRowHeight = ((defaultLineHeight * 5f) + (table.padding * 2f) + (table.border.size)).floatValue()
    }

    def "parse first row"() {
        float firstRowHeight = defaultRowHeight + table.border.size

        when:
        tableRenderer.parse(firstRowHeight)

        then:
        tableRenderer.parsedHeight == firstRowHeight

        and:
        tableRenderer.parseStart == 0

        and:
        tableRenderer.parseEnd == 0
    }

    def "parse part of first row"() {
        float partialRowHeight = table.padding + (defaultLineHeight * 3) + table.border.size

        when:
        tableRenderer.parse(partialRowHeight)

        then:
        tableRenderer.parseStart == 0

        and:
        tableRenderer.parseEnd == 0

        and:
        tableRenderer.parsedHeight == partialRowHeight
    }

    def "parse all rows"() {
        float totalHeight = (rowCount * defaultRowHeight) + table.border.size

        when:
        tableRenderer.parse(totalHeight)

        then:
        tableRenderer.parsedHeight == totalHeight

        and:
        tableRenderer.fullyParsed
    }

    private TableRenderer makeTableElement(Table table, Paragraph paragraph, int rows) {
        makeDocument()
        Document tableDocument = document
        table.parent = tableDocument
        int cellCount = table.columns.size()

        rows.times {
            Row row = new Row(font:new Font())
            table.addToChildren(row)
            cellCount.times {
                Cell cell = new Cell(font:new Font())
                row.addToChildren(cell)
                makeParagraph(paragraph, cell)
            }
        }
        table.updateRowspanColumns()
        table.normalizeColumnWidths()

        new TableRenderer(table, document, 0)
    }
}
