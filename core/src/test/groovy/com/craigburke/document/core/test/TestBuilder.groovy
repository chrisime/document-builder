package com.craigburke.document.core.test

import com.craigburke.document.core.dom.block.Document

import com.craigburke.document.core.builder.DocumentBuilder

import groovy.transform.InheritConstructors

/**
 * Basic implementation of a document builder for testing
 * @author Craig Burke
 */
@InheritConstructors
class TestBuilder extends DocumentBuilder<TestDocument> {
    @Override
    TestDocument createDocument(Map attributes) {
        document = new TestDocument(attributes)
        document
    }

    @Override
    void writeDocument() {
    }
}

class TestDocument extends Document {

}
