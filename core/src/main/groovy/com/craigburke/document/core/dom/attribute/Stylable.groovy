package com.craigburke.document.core.dom.attribute

trait Stylable implements ParentAware {
    Font font
    String style

    Font cloneParentFont() {
        parent instanceof Stylable ? parent.font.clone() : new Font()
    }

    abstract void setNodeFont(List<Map> nodeProperties)
}
