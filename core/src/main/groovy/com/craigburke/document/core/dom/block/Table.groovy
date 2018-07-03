package com.craigburke.document.core.dom.block

import com.craigburke.document.core.dom.attribute.BackgroundAssignable
import com.craigburke.document.core.dom.attribute.Margin
import com.craigburke.document.core.dom.block.table.Cell
import com.craigburke.document.core.dom.block.table.Row
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import ox.softeng.document.core.dsl.TabularApi

/**
 * Table node which contains children of children
 * @author Craig Burke
 */
@CompileStatic
class Table extends BlockNode<BlockNode, Row> implements BackgroundAssignable, TabularApi {
    static Margin DEFAULT_MARGIN = new Margin(top: 12, bottom: 12, left: 0, right: 0)

    BigDecimal       padding = 10.0
    BigDecimal       width
    List<BigDecimal> columns = []

    List<Row> getRows() {
        children
    }

    Row getRowAt(Integer index) {
        children[index]
    }

    Row row(Integer index) {
        children[index]
    }

    int getColumnCount() {
        if (columns) {
            columns.size()
        } else {
            rows ? rows.max { it.getNumberOfColumns() }.getNumberOfColumns() : 0
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    void normalizeColumnWidths() {
        updateRowspanColumns()

        width = Math.min(width ?: maxWidth, maxWidth.toDouble())

        if (!columns)
            columnCount.times { columns << 1.0 }

        List<BigDecimal> columnWidths = computeColumnWidths()

        rows.each { row ->
            int columnWidthIndex = 0
            row.cells.eachWithIndex { column, index ->
                int endIndex = columnWidthIndex + column.colspan - 1
                BigDecimal missingBorderWidth = (column.colspan - 1) * border.size
                column.width = (columnWidths[columnWidthIndex..endIndex].sum() as BigDecimal) + missingBorderWidth
                columnWidthIndex += column.colspan
                column.findAllChildTables().each { it.normalizeColumnWidths() }
            }
        }
    }

    void updateRowspanColumns() {
        Set<Cell> updatedColumns = [] as Set

        rows.eachWithIndex { row, rowIndex ->
            row.cells.eachWithIndex { column, columnIndex ->
                if (column.rowspan > 1 && !updatedColumns.contains(column)) {
                    int rowspanEnd = Math.min(rows.size() - 1, rowIndex + column.rowspan - 1)
                    (rowIndex + 1..rowspanEnd).each { i ->
                        getRowAt(i).addToCells(columnIndex, column)
                    }
                    updatedColumns << column
                }
            }
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    List<BigDecimal> computeColumnWidths() {
        BigDecimal relativeTotal = columns.sum() as BigDecimal
        BigDecimal totalBorderWidth = (columnCount + 1) * border.size
        BigDecimal totalCellWidth = width - totalBorderWidth

        List<BigDecimal> columnWidths = []
        columns.eachWithIndex { column, index ->
            if (index == columns.size() - 1) {
                columnWidths << totalCellWidth - ((columnWidths.sum() ?: 0) as BigDecimal)
            } else {
                columnWidths << (Math.ceil((columns[index] / relativeTotal) * totalCellWidth) as BigDecimal)
            }
        }
        columnWidths

    }

    @Override
    Margin getDefaultMargin() {
        DEFAULT_MARGIN
    }

    @Override
    void setNodeProperties(List<Map> nodePropertiesMap) {
        super.setNodeProperties(nodePropertiesMap)
        setNodeBackground(nodePropertiesMap)
    }

    private BigDecimal getMaxWidth() {
        if (parent instanceof Cell) {
            Table outerTable = parent.getTable()
            return parent.width - (outerTable.padding * 2)
        }
        (parent as Document).width - parent.margin.left - parent.margin.right
    }

    @Override
    Table getTable() {
        this
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    static Table create(Map attributes) {
        new Table(attributes)
    }

}
