package com.craigburke.document.core.dom.attribute

trait ColorAssignable {
    Color color = new Color()

    void setColor(String value) {
        color.color = value
    }
}
