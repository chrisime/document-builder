package com.craigburke.document.builder

import com.craigburke.document.core.dom.block.Document

import com.craigburke.document.builder.util.PdfDocumentLoader
import com.craigburke.document.core.builder.DocumentBuilder

/**
 * PdfDocument tests
 * @author Craig Burke
 */
class PdfDocumentBuilderSpec extends BaseBuilderSpec {

    DocumentBuilder getBuilderInstance(OutputStream out) {
        new PdfDocumentBuilder(out)
    }

    Document getDocument(byte[] data) {
        PdfDocumentLoader.load(data)
    }

    @Override
    String getFileExtension() {
        'pdf'
    }
}
