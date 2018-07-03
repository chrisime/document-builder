package com.craigburke.document.core.dom.block.table

import groovy.transform.CompileStatic
import ox.softeng.document.core.dsl.RowApi

import com.craigburke.document.core.dom.attribute.BackgroundAssignable
import com.craigburke.document.core.dom.attribute.Margin
import com.craigburke.document.core.dom.block.BlockNode
import com.craigburke.document.core.dom.block.Table
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

/**
 * Table row node
 * @author Craig Burke
 */
@CompileStatic
class Row extends BlockNode<Table, Cell> implements BackgroundAssignable, RowApi {

    Integer width

    List<Cell> getCells() {
        children
    }

    Integer getNumberOfColumns() {
        cells.size()
    }

    Row addToCells(Integer indexToAddAt, Cell cell) {
        addToChildren(indexToAddAt, cell) as Row
    }

    Table getTable() {
        parent
    }

    Cell cell(Integer index) {
        children[index]
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
    Row getRow() {
        this
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    static Row create(Map attributes) {
        new Row(attributes)
    }

}
