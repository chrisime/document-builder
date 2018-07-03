package com.craigburke.document.core.dom.attribute

/**
 * @since 11/06/2018
 */
trait MarginAssignable implements ParentAware {
    Margin margin = new Margin()

    abstract Margin getDefaultMargin()
}
