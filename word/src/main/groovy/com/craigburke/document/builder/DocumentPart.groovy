package com.craigburke.document.builder

import groovy.transform.Canonical
import groovy.transform.CompileStatic

/**
 * OOXML Document Part
 * @author Craig Burke
 */
@CompileStatic
@Canonical
class DocumentPart {
    DocumentPartType   type
    List<Relationship> relationships = []
    List               images        = []
}
