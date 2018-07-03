package com.craigburke.document.core.builder

import groovy.transform.CompileStatic
import ox.softeng.document.core.dsl.CreateApi

import com.craigburke.document.core.dom.PageBreak
import com.craigburke.document.core.dom.attribute.HeaderFooterOptions
import com.craigburke.document.core.dom.block.BlockNode
import com.craigburke.document.core.dom.block.Document
import com.craigburke.document.core.unit.UnitCategory
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

/**
 * Document Builder base class
 * @author Craig Burke
 */
@CompileStatic
abstract class DocumentBuilder<T extends Document> implements CreateApi {

    T document
    OutputStream out
    RenderState renderState = RenderState.PAGE
    Closure headerClosure
    Closure footerClosure
    Map templateMap = [:]

    DocumentBuilder(OutputStream out) {
        this.out = out
    }

    abstract T createDocument(Map attributes)

    abstract void writeDocument()

    void close() {
        out.close()
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    DocumentBuilder create(@DelegatesTo(CreateApi) Closure closure) {
        try {
            use(UnitCategory) {
                closure.delegate = this
                closure.call()

                checkPageCount()
                writeDocument()
            }
        } finally {
            close()
        }
        this
    }

    /**
     * Use too create the document but not actually write it. Call builder.writeDocument to complete
     */
    @TypeChecked(TypeCheckingMode.SKIP)
    DocumentBuilder createWithoutWrite(@DelegatesTo(CreateApi) Closure closure) {
        use(UnitCategory) {

            closure.delegate = this
            closure.call()

            checkPageCount()
        }
        this
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    BlockNode buildHeaderNode(HeaderFooterOptions headerFooterOptions) {
        use(UnitCategory) {
            buildNode(headerFooterOptions, headerClosure)
        }
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    BlockNode buildFooterNode(HeaderFooterOptions headerFooterOptions) {
        use(UnitCategory) {
            buildNode(headerFooterOptions, footerClosure)
        }
    }

    void checkPageCount() {
        Integer numberOfPageBreaks = document.children.count {it instanceof PageBreak}.toInteger()
        document.pageCount = Math.max(numberOfPageBreaks + 1, document.pageCount ?: 0)
    }

    @Override
    void callClosure(Closure closure, Object delegate, int resolveStrategy = Closure.DELEGATE_FIRST) {
        if (closure) {
            closure.resolveStrategy = resolveStrategy
            closure.delegate = delegate
            closure.call()
        }
    }

    @Override
    DocumentBuilder getBuilder() {
        this
    }

    protected BlockNode buildNode(HeaderFooterOptions headerFooterOptions, Closure closure) {
        HeaderFooterDocument document = new HeaderFooterDocument()
        document.templateMap = templateMap
        document.setNodeProperties([:])
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = document
        closure.call(headerFooterOptions)
        document.children.first() as BlockNode
    }

}
