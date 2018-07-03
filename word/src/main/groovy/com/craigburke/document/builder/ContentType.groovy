package com.craigburke.document.builder

import groovy.transform.CompileStatic
import groovy.transform.Immutable

/**
 * OOXML content type
 * @author Craig Burke
 */
@CompileStatic
@Immutable
class ContentType {
    String extension
    String type
}
