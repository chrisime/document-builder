package com.craigburke.document.core.dom.attribute

trait Alignable implements ParentAware {
    Align align = Align.LEFT

    void setAlign(String value) {
        align = Align.valueOf(value.toUpperCase())
    }

    void setAlign(Align align) {
        this.@align = align
    }
}
