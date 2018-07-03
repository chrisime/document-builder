package com.craigburke.document.core.dom.attribute

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

/**
 * Font embedded within a document
 * @author Craig Burke
 */
@CompileStatic
@Canonical
class EmbeddedFont {
    File file
    InputStream inputStream
    String name
    boolean italic = false
    boolean bold = false

    @TypeChecked(TypeCheckingMode.SKIP)
    static EmbeddedFont create(Map attributes) {
        new EmbeddedFont(attributes)
    }
}
