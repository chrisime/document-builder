package com.craigburke.document.builder

import groovy.transform.CompileStatic
import groovy.transform.Immutable

/**
 * OOXML content type override
 * @author Craig Burke
 */
@CompileStatic
@Immutable
class ContentTypeOverride {
    String partName
    String contentType
}
