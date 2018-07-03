package com.craigburke.document.core.dom.block.table

import groovy.transform.CompileStatic
import ox.softeng.document.core.dsl.CellApi

import com.craigburke.document.core.dom.BaseNode
import com.craigburke.document.core.dom.attribute.BackgroundAssignable
import com.craigburke.document.core.dom.attribute.Margin
import com.craigburke.document.core.dom.block.BlockNode
import com.craigburke.document.core.dom.block.Table
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

/**
 * An individual cell for the Table node
 * @author Craig Burke
 */
@CompileStatic
class Cell extends BlockNode<Row, BaseNode> implements BackgroundAssignable, CellApi {
    BigDecimal width
    Integer colspan = 1
    
    Integer rowspan = 1
    Integer rowsSpanned = 0
    BigDecimal rowspanHeight = 0

    List<Table> findAllChildTables() {
        children.findAll {it instanceof Table} as List<Table>
    }

    Table getTable() {
        parent.parent
    }

    @Override
    Margin getDefaultMargin() {
        Margin.NONE
    }

    @Override
    void setNodeProperties(List<Map> nodePropertiesMap) {
        super.setNodeProperties(nodePropertiesMap)
        setNodeBackground(nodePropertiesMap)
    }

    @Override
    Cell getCell() {
        this
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    static Cell create(Map attributes) {
        new Cell(attributes)
    }

}
