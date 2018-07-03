package com.craigburke.document.core.dom.attribute

import groovy.transform.CompileStatic

@CompileStatic
enum ImageType {
    PNG('png'),
    JPG('jpg')

    final String value

    ImageType(String value) {
        this.value = value
    }
}
