package com.craigburke.document.builder

import groovy.transform.CompileStatic
import groovy.transform.Immutable

/**
 * OOXML Relationship
 * @author Craig Burke
 */
@CompileStatic
@Immutable
class Relationship {
    String id
    String type
    String target
    String targetMode
}
