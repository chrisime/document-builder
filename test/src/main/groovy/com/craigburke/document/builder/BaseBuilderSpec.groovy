package com.craigburke.document.builder

import com.craigburke.document.core.builder.DocumentBuilder
import com.craigburke.document.core.dom.block.Document
import com.craigburke.document.core.dom.block.Table
import spock.lang.Shared
import spock.lang.Unroll

import java.time.format.DateTimeFormatter

/**
 * Base class for individual builder tests
 * @author Craig Burke
 */
abstract class BaseBuilderSpec extends BaseSpec {

    @Shared
    ByteArrayOutputStream out
    @Shared
    DocumentBuilder builder
    @Shared
    byte[] imageData = getClass().classLoader.getResource('test/images/cheeseburger.jpg')?.bytes

    @Shared
        testMargins = [
            [top: 0, bottom: 0, left: 0, right: 0],
            [top: 2 * 72, bottom: 3 * 72, left: 1.25 * 72, right: 2.5 * 72],
            [top: 72 / 4, bottom: 72 / 2, left: 72 / 4, right: 72 / 2]
        ]

    byte[] getData() {
        out.toByteArray()
    }

    abstract DocumentBuilder getBuilderInstance(OutputStream out)

    abstract Document getDocument(byte[] data)

    abstract String getFileExtension()

    def setup() {
        println "--- ${specificationContext.currentIteration.name} ---"
        out = new ByteArrayOutputStream()
        builder = getBuilderInstance(out)
    }

    @Unroll
    def "set document margins"() {
        when:
        builder.create {
            document(margin: [top: margin.top, bottom: margin.bottom, left: margin.left, right: margin.right]) {
                paragraph 'Content'
            }
        }

        def document = getDocument(data)

        then:
        document.margin.left == margin.left

        and:
        document.margin.right == margin.right

        and:
        document.margin.top == margin.top

        and:
        document.margin.bottom == margin.bottom

        where:
        margin << testMargins
    }

    @Unroll
    def "set paragraph margins"() {
        when:
        builder.create {
            document {
                paragraph(margin: currentMargin) {
                    text 'Foo'
                }
            }
        }

        def paragraph = getDocument(data).children[0]

        then:
        paragraph.margin.left == currentMargin.left

        and:
        paragraph.margin.right >= currentMargin.right

        and:
        paragraph.margin.top == currentMargin.top

        where:
        currentMargin << testMargins
    }

    def "create a simple table"() {
        when:
        builder.create {
            document {
                table {
                    row {
                        cell {
                            text 'FOOBAR'
                        }
                    }
                }
            }
        }

        Table table = getDocument(data).children[0] as Table

        then:
        table.row(0).cell(0).child(0).text == 'FOOBAR'
    }

    def "set table options"() {
        when:
        builder.create {
            document {
                table(width: 403.px, columns: [100.px, 300.px], border: [size: 1.px]) {
                    row {
                        cell('Cell 1')
                        cell('Cell 2')
                    }
                }
            }
        }

        def table = getDocument(data).children[0]

        then:
        table.width == 403

        and:
        table.children[0].children[0].width == 100

        and:
        table.children[0].children[1].width == 300
    }

    def "set paragraph text"() {
        when:
        builder.create {
            document {
                paragraph 'Foo'
                paragraph('Foo') {
                    text 'Ba'
                    text 'r'
                }
                paragraph {
                    text 'B'
                    text 'a'
                    text 'r'
                }
            }
        }

        def paragraphs = getDocument(data).children

        then:
        paragraphs[0].text == 'Foo'

        and:
        paragraphs[1].text == 'FooBar'

        and:
        paragraphs[2].text == 'Bar'
    }

    def "create a table with multiple columns"() {
        when:
        builder.create {
            document {
                table {
                    row {
                        cell 'Cell1'
                        cell 'Cell2'
                        cell {
                            text 'Cell3'
                        }
                    }

                }
            }
        }

        then:
        notThrown(Exception)
    }

    def "create a table with lots of rows"() {
        when:
        builder.create {
            document {
                table {
                    50.times {i ->
                        row {
                            cell {
                                text 'TEST ' * (i + 1)
                            }
                            cell {
                                text 'FOO ' * (i + 1)
                            }
                            cell {
                                text 'BAR ' * (i + 1)
                            }
                        }
                    }
                }
            }
        }

        then:
        notThrown(Exception)
    }

    def "add an image"() {
        when:
        builder.create {
            document {
                paragraph {
                    image(data: imageData, width: 500.px, height: 431.px)
                }
            }
        }

        then:
        notThrown(Exception)
    }

    def "paragraph header"() {
        when:
        builder.create {
            document(header: {
                paragraph 'HEADER'
            }) {
                paragraph 'Content'
            }
        }

        then:
        notThrown(Exception)
    }

    def "paragraph footer"() {
        when:
        builder.create {
            document(footer: {paragraph 'FOOTER'}) {
                paragraph 'Content'
            }
        }

        then:
        notThrown(Exception)
    }

    def "paragraph header and footer"() {
        when:
        builder.create {
            document(header: {paragraph 'HEADER'}, footer: {paragraph 'FOOTER'}) {
                paragraph 'Content'
            }
        }

        then:
        notThrown(Exception)
    }

    def "table header"() {
        when:
        builder.create {
            document(header: {table {row {cell 'HEADER'}}}) {
                paragraph 'Content'
            }
        }

        then:
        notThrown(Exception)
    }

    def "table footer"() {
        when:
        builder.create {
            document(footer: {table {row {cell 'FOOTER'}}}) {
                paragraph 'Content'
            }
        }

        then:
        notThrown(Exception)
    }

    def "table within table"() {
        when:
        builder.create {
            document {
                table {
                    row {
                        cell 'OUTER TABLE'
                        cell {
                            table {
                                row {
                                    cell 'INNER TABLE'
                                }
                            }
                        }
                    }
                }
            }
        }

        then:
        notThrown(Exception)
    }

    def "table with rowspan"() {
        when:
        builder.create {
            document {
                table {
                    row {
                        cell 'FOO\nBAR', rowspan: 3
                        cell('COL1-2')
                    }
                    row {
                        cell('COL2-1')
                    }
                    row {
                        cell('COL3-1')
                    }
                    row {
                        cell('COL4-1')
                        cell('COL4-2')
                    }
                }
            }
        }

        then:
        notThrown(Exception)
    }

    void 'test table in table rendering'() {
        given:
        File testFile = new File("/tmp/table_in_table.${getFileExtension()}")
        if (testFile.exists()) testFile.delete()

        expect:
        !testFile.exists()

        when:
        DocumentBuilder builder = getBuilderInstance(new FileOutputStream(testFile))

        builder.create {
            document(font: [family: 'Helvetica', size: 14.pt], margin: [top: 0.75.inches], pageCount: 8) {

                heading1 "Tables in Table"

                heading2 'Tables in table', font: [italic: true]

                heading3 'Simple'
                table {
                    row {
                        cell {
                            text 'Cell1-1'
                        }
                        cell {
                            table {
                                row {
                                    cell 'INNER-1'
                                    cell 'INNER-2'
                                }
                            }
                        }
                    }
                    row {
                        cell 'Cell2-1'
                        cell 'Cell2-2'
                    }
                }

                heading3 'Complex'
                table(columns: [1, 2]) {
                    row {
                        cell {
                            text 'Backgrounds'
                        }
                        cell {
                            table(background: '#6495ED') {
                                row {
                                    cell 'Cell1-1'
                                    cell 'Cell1-1'
                                }
                                row(background: '#FFFFFF') {
                                    cell 'Cell2-1'
                                    cell 'Cell2-2'
                                }
                                row {
                                    cell 'Cell3-1', background: '#FFD700'
                                    cell 'Cell3-2'
                                }
                            }
                        }
                    }
                    row {
                        cell 'Single entry'
                        cell 'Some text', font: [bold: true]
                    }

                    row {
                        cell 'Row spanning'
                        cell {
                            table {
                                row {
                                    cell 'Cell1-1', rowspan: 2
                                    cell 'Cell1-2'
                                    cell 'Cell1-3'
                                }
                                row {
                                    cell 'Cell2-1'
                                    cell 'Cell2-2'
                                }
                                row {
                                    cell 'Cell3-1'
                                    cell 'Cell3-2'
                                    cell 'Cell3-3'
                                }
                            }
                        }
                    }
                    row {
                        cell 'Column Widths'
                        cell {
                            table(columns: [1, 2, 3]) {
                                row {
                                    cell 'Cell1-1'
                                    cell 'Cell1-2'
                                    cell 'Cell1-3'
                                }
                            }
                        }
                    }
                    row {
                        cell 'Column Spanning'
                        cell {
                            table {
                                row {
                                    cell 'Cell1', colspan: 2
                                    cell 'Cell1-2'
                                }
                                row {
                                    cell 'Cell2-1'
                                    cell 'Cell2-2'
                                    cell 'Cell2-3'
                                }
                            }
                        }
                    }
                }
            }
        }

        then:
        noExceptionThrown()
        testFile.exists()
        testFile.size() != 0
    }

    void 'test outputting document'() {
        given:
        File testFile = new File("/tmp/full_test.${getFileExtension()}")
        if (testFile.exists()) testFile.delete()

        expect:
        !testFile.exists()

        when:
        DocumentBuilder builder = getBuilderInstance(new FileOutputStream(testFile))

        String[] COLORS = ['#FF0000', '#FF7F00', '#FFFF00', '#00FF00', '#0000FF', '#4B0082', '#8B00FF']

        builder.create {
            header {info ->
                paragraph 'Author: Spock'
            }
            footer {info ->
                table(border: [size: 0]) {
                    row {
                        cell "Date Generated: ${info.dateGenerated.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}"
                        cell "Page ${info.pageNumber} of ${info.pageCount}", align: 'right'
                    }
                }
            }
            template {
                heading(font: [underline: true])
                link(font: [underline: true])
            }
            document(font: [family: 'Helvetica', size: 14.pt], margin: [top: 0.75.inches], pageCount: 8) {

                heading1 "Groovy Document Builder v.1.0.0", font: [color: '#990000', size: 22.pt]

                heading2 "Paragraphs"

                section {
                    heading3('Fancy colours and sizing')
                    paragraph {
                        font.size = 42.pt
                        "Hello World".eachWithIndex {letter, index ->
                            font.color = COLORS[index % COLORS.size()]
                            text letter
                            font.size--
                        }
                        lineBreak()
                        text "Current font size is ${font.size}pt"
                    }

                    paragraph "Back to default font and aligned to the right", align: 'right'

                    paragraph(margin: [left: 1.25.inches, right: 1.inch, top: 0.25.inches, bottom: 0.25.inches]) {
                        font << [family: 'Times-Roman', bold: true, italic: true, color: '#333333']
                        text "A paragraph with a different font and margins"
                    }
                }
                section {
                    heading3 'Images'

                    paragraph(align: 'center') {
                        image(data: imageData, width: 250.px, height: 125.px)
                        lineBreak()
                        text "Figure 1: Groovy Logo", font: [italic: true, size: 9.pt]
                    }
                }

                pageBreak()

                section {
                    heading3 'Text play inside paragraphs'

                    paragraph 'Paragraph 1'
                    paragraph {
                        text 'Paragraph '
                        text '2', font: [bold: true, size: 22.pt, color: '#FF0000']
                    }
                }

                section {
                    heading3 'Styles'

                    paragraph(font: [family: 'Courier', size: 12.pt]) {
                        text 'Paragraph text with '
                        text 'custom styles', font: [color: '#FF0000']
                    }

                    paragraph 'Default style'
                }

                section {
                    heading3 'Links'
                    paragraph {
                        text 'Some text which is telling us something using a sensible link and display text. See'
                        text ' '
                        link 'http://www.google.co.uk', 'Google'
                        text ' '
                        text 'for more information.'
                    }
                    paragraph {
                        text 'Some text which is telling us something using a link without display text. See '
                        link ' http://www.google.co.uk '
                        text 'for more information.'

                    }
                }

                pageBreak()

                heading2 'Tables'

                section {
                    heading3 'Alignment, Play and Padding', font: [italic: true]

                    section {
                        heading4 'Alignment'
                        table(width: 6.inches, padding: 4.px, border: [size: 3.px, color: '#990000']) {
                            row {
                                cell('Left Aligned', width: 1.5.inches, align: 'left')
                                cell('Center Aligned', width: 2.inches, align: 'center')
                                cell(align: 'right') {
                                    text 'Right Aligned'
                                }
                            }
                        }

                    }
                    heading4 'Text Play'
                    table {
                        row {
                            cell 'Cell1'
                            cell {
                                text 'Cell'
                                text '2'
                            }
                        }
                    }

                    heading4 'Padding'

                    table(width: 6.inches, padding: 20.px, border: [size: 3.px, color: '#FF0000']) {
                        row {
                            cell 'Cell1'
                            cell 'Cell2', align: 'right'
                        }
                    }

                }
                pageBreak()

                heading3 'Column widths'

                table(columns: [1, 2, 3]) {
                    row {
                        cell 'Cell1-1'
                        cell 'Cell1-2'
                        cell 'Cell1-3'
                    }
                }

                heading3 'Backgrounds'

                table(background: '#6495ED') {
                    row {
                        cell 'Cell1-1'
                        cell 'Cell1-1'
                    }
                    row(background: '#FFFFFF') {
                        cell 'Cell2-1'
                        cell 'Cell2-2'
                    }
                    row {
                        cell 'Cell3-1', background: '#FFD700'
                        cell 'Cell3-2'
                    }
                }

                pageBreak()

                section {
                    heading3 'Spanning', font: [italic: true]
                    heading4 'Column Spanning'

                    table {
                        row {
                            cell 'Cell1', colspan: 2
                            cell 'Cell1-2'
                        }
                        row {
                            cell 'Cell2-1'
                            cell 'Cell2-2'
                            cell 'Cell2-3'
                        }
                    }

                    heading4 'Row spanning'

                    table {
                        row {
                            cell 'Cell1-1', rowspan: 2
                            cell 'Cell1-2'
                            cell 'Cell1-3'
                        }
                        row {
                            cell 'Cell2-1'
                            cell 'Cell2-2'
                        }
                        row {
                            cell 'Cell3-1'
                            cell 'Cell3-2'
                            cell 'Cell3-3'
                        }
                    }

                }

                pageBreak()

                heading3 'Tables in table', font: [italic: true]

                heading4 'Simple'
                table {
                    row {
                        cell {
                            text 'Cell1-1'
                        }
                        cell {
                            table {
                                row {
                                    cell 'INNER-1'
                                    cell 'INNER-2'
                                }
                            }
                        }
                    }
                    row {
                        cell 'Cell2-1'
                        cell 'Cell2-2'
                    }
                }

                heading4 'Complex'
                table(columns: [1, 2]) {
                    row {
                        cell {
                            text 'Backgrounds'
                        }
                        cell {
                            table(background: '#6495ED') {
                                row {
                                    cell 'Cell1-1'
                                    cell 'Cell1-1'
                                }
                                row(background: '#FFFFFF') {
                                    cell 'Cell2-1'
                                    cell 'Cell2-2'
                                }
                                row {
                                    cell 'Cell3-1', background: '#FFD700'
                                    cell 'Cell3-2'
                                }
                            }
                        }
                    }
                    row {
                        cell 'Single entry'
                        cell 'Some text', font: [bold: true]
                    }

                    row {
                        cell 'Row spanning'
                        cell {
                            table {
                                row {
                                    cell 'Cell1-1', rowspan: 2
                                    cell 'Cell1-2'
                                    cell 'Cell1-3'
                                }
                                row {
                                    cell 'Cell2-1'
                                    cell 'Cell2-2'
                                }
                                row {
                                    cell 'Cell3-1'
                                    cell 'Cell3-2'
                                    cell 'Cell3-3'
                                }
                            }
                        }
                    }
                    row {
                        cell 'Column Widths'
                        cell {
                            table(columns: [1, 2, 3]) {
                                row {
                                    cell 'Cell1-1'
                                    cell 'Cell1-2'
                                    cell 'Cell1-3'
                                }
                            }
                        }
                    }
                    row {
                        cell 'Column Spanning'
                        cell {
                            table {
                                row {
                                    cell 'Cell1', colspan: 2
                                    cell 'Cell1-2'
                                }
                                row {
                                    cell 'Cell2-1'
                                    cell 'Cell2-2'
                                    cell 'Cell2-3'
                                }
                            }
                        }
                    }
                }




                pageBreak()

                heading2 'Headings Play'

                section {
                    heading2 '1.1 First Section', font: [color: '#333333']
                    paragraph 'First section content'

                    heading3 '1.1.1 Subsection'
                    heading4 '1.1.1.1 Subsection'
                    heading5 '1.1.1.1.1 Subsection'
                    heading6 '1.1.1.1.1.1 Subsection'

                    heading2 '1.2 Second Section'
                }
            }
        }

        then:
        noExceptionThrown()
        testFile.exists()
        testFile.size() != 0
    }


}
