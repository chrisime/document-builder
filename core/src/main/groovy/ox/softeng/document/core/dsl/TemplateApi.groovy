package ox.softeng.document.core.dsl

import groovy.transform.CompileStatic

/**
 * @since 21/06/2018
 */
@CompileStatic
class TemplateApi {

    Map templateMap = [:]

    TemplateApi document(Map attributes) {
        methodMissing('document', attributes)
    }

    TemplateApi paragraph(Map attributes) {
        methodMissing('paragraph', attributes)
    }

    TemplateApi table(Map attributes) {
        methodMissing('table', attributes)
    }

    TemplateApi row(Map attributes) {
        methodMissing('row', attributes)
    }

    TemplateApi cell(Map attributes) {
        methodMissing('cell', attributes)
    }

    TemplateApi heading(Map attributes) {
        methodMissing('heading', attributes)
    }

    TemplateApi heading1(Map attributes) {
        methodMissing('heading1', attributes)
    }

    TemplateApi heading2(Map attributes) {
        methodMissing('heading2', attributes)
    }

    TemplateApi heading3(Map attributes) {
        methodMissing('heading3', attributes)
    }

    TemplateApi heading4(Map attributes) {
        methodMissing('heading4', attributes)
    }

    TemplateApi heading5(Map attributes) {
        methodMissing('heading5', attributes)
    }

    TemplateApi heading6(Map attributes) {
        methodMissing('heading6', attributes)
    }

    TemplateApi text(Map attributes) {
        methodMissing('text', attributes)
    }

    TemplateApi image(Map attributes) {
        methodMissing('image', attributes)
    }

    TemplateApi link(Map attributes) {
        methodMissing('link', attributes)
    }

    TemplateApi methodMissing(String name, def args) {
        templateMap[name] = args instanceof Object[] ? ((Object[]) args)[0] : args
        this
    }

}
