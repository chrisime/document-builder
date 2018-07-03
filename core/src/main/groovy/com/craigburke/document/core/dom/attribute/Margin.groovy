package com.craigburke.document.core.dom.attribute

import groovy.transform.AutoClone
import groovy.transform.CompileStatic

/**
 * Margin settings used for block nodes
 * @author Craig Burke
 */
@AutoClone
@CompileStatic
class Margin {

    static final Margin NONE = new Margin(top: 0, right: 0, bottom: 0, left: 0)

    BigDecimal top
    BigDecimal bottom
    BigDecimal left
    BigDecimal right

    void setDefaults(Margin defaultMargin) {
        top = (top == null) ? defaultMargin.top : top
        bottom = (bottom == null) ? defaultMargin.bottom : bottom
        left = (left == null) ? defaultMargin.left : left
        right = (right == null) ? defaultMargin.right : right
    }

    def leftShift(Map<String, Object> properties) {
        properties?.each { key, value -> this[key] = value }
    }

    def leftShift(Margin margin) {
        top = margin.top
        bottom = margin.bottom
        left = margin.left
        right = margin.right
    }

    void setTop(Integer top) {
        this.top = top
    }

    void setBottom(Integer bottom) {
        this.bottom = bottom
    }

    void setLeft(Integer left) {
        this.left = left
    }

    void setRight(Integer right) {
        this.right = right
    }

    Map<String, BigDecimal> toMap() {
        [top: top, right: right, bottom: bottom, left: left]
    }

}
