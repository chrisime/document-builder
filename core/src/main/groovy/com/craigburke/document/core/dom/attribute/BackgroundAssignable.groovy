package com.craigburke.document.core.dom.attribute

import com.craigburke.document.core.dom.block.Paragraph

trait BackgroundAssignable implements ParentAware {

    Color background

    void setBackground(String value) {
        if (value) {
            background = background ?: new Color()
            background.color = value
        }
    }

    void setBackground(Color color) {
        this.@background = color
    }

    void setNodeBackground(List<Map> nodeProperties) {
        if (parent instanceof BackgroundAssignable && !(parent instanceof Paragraph)) {
            if (!background && parent.background) {
                setBackground("#${parent.background.hex}")
            }
        }
        nodeProperties.each {
            if (it.background) setBackground(it.background)
        }

    }

}
