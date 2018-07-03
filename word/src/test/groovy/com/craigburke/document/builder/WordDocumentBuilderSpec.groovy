package com.craigburke.document.builder

import com.craigburke.document.core.dom.block.Document

import com.craigburke.document.core.builder.DocumentBuilder

/**
 * WordDocument tests
 * @author Craig Burke
 */
class WordDocumentBuilderSpec extends BaseBuilderSpec {

    DocumentBuilder getBuilderInstance(OutputStream out) {
        new WordDocumentBuilder(out)
    }

    Document getDocument(byte[] data) {
        WordDocumentLoader.load(data)
    }

    @Override
    String getFileExtension() {
        'docx'
    }

}
