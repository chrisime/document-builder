package com.craigburke.document.core.dom.attribute

import groovy.transform.CompileStatic

@CompileStatic
enum Align {
    LEFT('left'),
    RIGHT('right'),
    CENTER('center')

    final String value

    Align(String value) {
        this.value = value
    }
}
