package com.craigburke.document.core.builder

import com.craigburke.document.core.dom.Image
import com.craigburke.document.core.dom.attribute.Align
import com.craigburke.document.core.dom.attribute.EmbeddedFont
import com.craigburke.document.core.dom.attribute.HeaderFooterOptions
import com.craigburke.document.core.dom.block.BlockNode
import com.craigburke.document.core.dom.block.Document
import com.craigburke.document.core.dom.block.Paragraph
import com.craigburke.document.core.dom.block.Table
import com.craigburke.document.core.dom.block.table.Cell
import com.craigburke.document.core.dom.block.table.Row
import com.craigburke.document.core.dom.text.Heading
import com.craigburke.document.core.dom.text.Link
import com.craigburke.document.core.dom.text.Text

import com.craigburke.document.core.test.TestBuilder

import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * Builder core tests
 * @author Craig Burke
 */
class BuilderSpec extends Specification {

    @Shared
    TestBuilder builder

    @Shared
    byte[] imageData = getClass().classLoader.getResource('cheeseburger.jpg')?.bytes

    def setup() {
        OutputStream out = new ByteArrayOutputStream()
        builder = new TestBuilder(out)
    }

    def "create empty document"() {
        when:
        def result = builder.create {
            document()
        }

        then:
        result.document
    }

    def "use file in builder constructor"() {
        File testFile = new File('test')

        when:
        def fileBuilder = new TestBuilder(new FileOutputStream(testFile))

        then:
        fileBuilder.out != null

        and:
        fileBuilder.out.getClass() == FileOutputStream

        cleanup:
        testFile?.delete()
    }

    def "create default letter size document"() {
        when:
        def result = builder.create {
            document(margin: [top: 2.cm, bottom: 1.cm]) {
                paragraph(align: 'center', font: [size: 24.pt]) {
                    text 'ISO 216'
                }
            }
        }

        then:
        result.document.width == 612 // 8.5 inch * 72 DPI
        result.document.height == 792 // 11 inch * 72 DPI
    }

    def "create A4 document"() {
        when:
        def result = builder.create {
            document(size: 'A4', margin: [top: 2.cm, bottom: 1.cm]) {
                paragraph(align: 'center', font: [size: 24.pt]) {
                    text 'ISO 216'
                }
            }
        }

        then:
        result.document.width.toInteger() == 595 // 8.27 inch * 72 DPI
        result.document.height.toInteger() == 842 // 11.7 inch * 72 DPI
    }

    def "create document with custom size"() {
        when:
        def result = builder.create {
            document(size: [14.8.cm, 21.cm], margin: [top: 2.cm, bottom: 1.cm]) {
                paragraph(align: 'center', font: [size: 24.pt]) {
                    text 'ISO 216'
                }
            }
        }

        then:
        result.document.width.toInteger() == 419 // 8.27 inch * 72 DPI
        result.document.height.toInteger() == 595 // 11.7 inch * 72 DPI
    }

    def "use landscape orientation"() {
        when:
        def result = builder.create {
            document(size: 'A4', orientation: 'landscape', margin: [top: 2.cm, bottom: 1.cm]) {
                paragraph(align: 'center', font: [size: 24.pt]) {
                    text 'Landscape'
                }
            }
        }

        then:
        result.document.width.toInteger() == 842 // 11.7 inch * 72 DPI
        result.document.height.toInteger() == 595 // 8.27 inch * 72 DPI
    }

    def "use typographic units"() {
        when:
        builder.create {
            document(margin: [top: 2.inches, bottom: 1.inch]) {
                paragraph(font: [size: 12.pt]) {
                    text 'Foo'
                }
                table(border: [size: 2.px]) {
                    row {
                        cell 'Bar'
                    }
                }
            }
        }

        then:
        notThrown(Exception)
    }

    def "load embedded font"() {
        when:
        def result = builder.create {
            document {
                addFont('/open-sans.ttf', name: 'Open Sans', bold: true)
            }
        }

        EmbeddedFont embeddedFont = result.document.embeddedFonts.first()

        then:
        embeddedFont.name == 'Open Sans'
    }

    @Ignore
    def "Image can be loaded from URL using url method"() {
        when:
        def result = builder.create {
            document {
                paragraph {
                    image("https://www.google.co.uk/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png")
                }
            }
        }

        then:
        notThrown(UnknownHostException)

        and:
        Paragraph paragraph = result.document.children[0]
        Image image = paragraph.children[0]
        image.data != null
        image.width == 272
        image.height == 92
    }

    @Ignore
    def "Image can be loaded from URL"() {
        when:
        def result = builder.create {
            document {
                paragraph {
                    image(url: "https://www.google.co.uk/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png")
                }
            }
        }

        then:
        notThrown(UnknownHostException)

        and:
        Paragraph paragraph = result.document.children[0]
        Image image = paragraph.children[0]
        image.data != null
        image.width == 272
        image.height == 92
    }

    def "Image should have correct aspect ratio if only width is specified using data method"() {
        when:
        def result = builder.create {
            document {
                paragraph {
                    image(imageData, width: 250.px) // cheeseburger.jpg is 500x431
                }
            }
        }

        then:
        Paragraph paragraph = result.document.children[0]
        Image image = paragraph.children[0]
        image.width.toInteger() == 250
        image.height.toInteger() == 215
    }

    def "Image should have correct aspect ratio if only width is specified"() {
        when:
        def result = builder.create {
            document {
                paragraph {
                    image(data: imageData, width: 250.px) // cheeseburger.jpg is 500x431
                }
            }
        }

        then:
        Paragraph paragraph = result.document.children[0]
        Image image = paragraph.children[0]
        image.width.toInteger() == 250
        image.height.toInteger() == 215
    }

    def "Image should have correct aspect ratio if only height is specified"() {
        when:
        def result = builder.create {
            document {
                paragraph {
                    image(data: imageData, height: 216.px) // cheeseburger.jpg is 500x431
                }
            }
        }

        then:
        Paragraph paragraph = result.document.children[0]
        Image image = paragraph.children[0]
        image.width.toInteger() == 250
        image.height.toInteger() == 216
    }

    def "create a simple paragraph"() {
        when:
        def result = builder.create {
            document {
                paragraph 'FOO BAR!'
            }
        }

        Paragraph paragraph = result.document.children[0]

        then:
        paragraph.text == 'FOO BAR!'
    }

    def "create paragraphs with aligned text"() {
        when:
        def result = builder.create {
            document {
                paragraph 'default'
                paragraph 'left', align: Align.LEFT
                paragraph 'center', align: Align.CENTER
                paragraph 'right', align: Align.RIGHT
            }
        }

        Paragraph paragraph1 = result.document.children[0]
        Paragraph paragraph2 = result.document.children[1]
        Paragraph paragraph3 = result.document.children[2]
        Paragraph paragraph4 = result.document.children[3]

        then:
        paragraph1.align == Align.LEFT

        and:
        paragraph2.align == Align.LEFT

        and:
        paragraph3.align == Align.CENTER

        and:
        paragraph4.align == Align.RIGHT
    }

    def "create paragraph with correct hierarchy"() {
        when:
        def result = builder.create {
            document {
                paragraph {
                    text 'FOO'
                    text 'BAR'
                }
            }
        }

        Document document = result.document
        Paragraph paragraph = document.children[0]
        Text text1 = paragraph.children[0]
        Text text2 = paragraph.children[1]

        then:
        document.children == [paragraph]

        and:
        paragraph.text == 'FOOBAR'
        paragraph.children == [text1, text2]

        and:
        paragraph.parent == document

        and:
        text1.parent == paragraph

        and:
        text2.parent == paragraph
    }

    def "create table with the correct heirarchy"() {
        when:
        def result = builder.create {
            document {
                table {
                    row {
                        cell('FOO')
                        cell {
                            text 'BAR'
                        }
                    }
                }
            }
        }

        Document document = result.document
        Table table = document.children[0]

        Row row = table.children[0]

        Cell column1 = row.children[0]
        Cell column2 = row.children[1]

        Paragraph paragraph1 = column1.children[0]
        Paragraph paragraph2 = column2.children[0]

        Text text1 = paragraph1.children[0]
        Text text2 = paragraph2.children[0]

        then:
        table.parent == document

        and:
        table.children == [row]
        row.parent == table
        row.children == [column1, column2]

        and:
        column1.parent == row
        column1.children == [paragraph1]
        paragraph1.parent == column1

        and:
        column2.parent == row
        column2.children == [paragraph2]
        paragraph2.parent == column2

        and:
        text1.value == 'FOO'
        text1.parent == paragraph1
        paragraph1.children == [text1]

        and:
        text2.value == 'BAR'
        text2.parent == paragraph2
        paragraph2.children == [text2]
    }

    def "column widths are calculated"() {
        when:
        def result = builder.create {
            document {
                table(width: 250, padding: 0, border: [size: 0]) {
                    row {
                        cell 'FOOBAR'
                        cell 'BLAH'
                    }
                }
            }
        }

        Table table = result.document.children[0]
        Cell column1 = table.children[0].children[0]
        Cell column2 = table.children[0].children[1]

        then:
        table.width == 250

        and:
        column1.width == 125

        and:
        column2.width == 125
    }

    def "override or inherit font settings"() {
        when:
        def result = builder.create {
            document(font: [family: 'Helvetica', color: '#121212']) {

                paragraph(font: [family: 'Courier', color: '#333333']) {
                    text 'Paragraph override'
                }
                paragraph 'Inherit doc font'

                paragraph {
                    text 'Text override', font: [family: 'Times-Roman', color: '#FFFFFF']
                }

                table(font: [family: 'Courier', color: '#111111']) {
                    row {
                        cell('Override')
                    }
                }

                table {
                    row {
                        cell('Default font')
                    }
                }

            }
        }

        Document document = result.document

        Text paragraph1 = document.children[0].children[0]
        Text paragraph2 = document.children[1].children[0]
        Text paragraph3 = document.children[2].children[0]

        Paragraph table1 = document.children[3].children[0].children[0].children[0]
        Paragraph table2 = document.children[4].children[0].children[0].children[0]

        then:
        paragraph1.font.family == 'Courier'

        and:
        paragraph2.font.family == 'Helvetica'

        and:
        paragraph3.font.family == 'Times-Roman'

        and:
        table1.font.family == 'Courier'

        and:
        table2.font.family == 'Helvetica'
    }

    def "create a table with that contains an image and text"() {
        when:
        builder.create {
            document {
                table {
                    row {
                        cell {
                            image(data: imageData, width: 500.px, height: 431.px)
                            lineBreak()
                            text 'A cheeseburger'
                        }
                    }

                }
            }
        }

        then:
        notThrown(Exception)
    }

    def "background color cascades for tables "() {
        given:
        String[] backgroundColors = ['#000000', '#111111', '#333333']

        when:
        Document result = builder.create {
            document {
                table(background: backgroundColors[0]) {
                    row {
                        cell '1.1'
                        cell '1.2'
                    }
                }

                table {
                    row(background: backgroundColors[1]) {
                        cell '2.1'
                        cell '2.2'
                    }
                }

                table {
                    row {
                        cell '3-1', background: backgroundColors[2]
                        cell '3-2'
                    }
                }
            }
        }.document

        Table table1 = result.children[0]
        Table table2 = result.children[1]
        Table table3 = result.children[2]

        then:
        table1.background.hex == backgroundColors[0] - '#'
        table1.children[0].background.hex == backgroundColors[0] - '#'
        table1.children[0].children.each {Cell column ->
            assert column.background.hex == backgroundColors[0] - '#'
        }

        and:
        table2.background == null
        table2.children[0].background.hex == backgroundColors[1] - '#'
        table2.children[0].children.each {Cell column ->
            assert column.background.hex == backgroundColors[1] - '#'
        }

        and:
        table3.background == null
        table3.children[0].background == null
        table3.children[0].children[0].background.hex == backgroundColors[2] - '#'
        table3.children[0].children[1].background == null
    }


    def "background color does not cascade with paragraphs "() {
        given:
        String YELLOW = '#FFFF00'
        String GREY = '#333333'

        when:
        Document result = builder.create {
            document {
                paragraph(background: YELLOW) {
                    text 'FOO'
                    text 'BAR', background: GREY
                }
            }
        }.document

        Paragraph paragraph = result.children[0]
        Text text1 = paragraph.children[0]
        Text text2 = paragraph.children[1]

        then:
        paragraph.background.hex == YELLOW - '#'

        and:
        !text1.background

        and:
        text2.background.hex == GREY - '#'
    }

    def "links are created correctly"() {
        String url = 'http://www.craigburke.com'

        when:
        Document result = builder.create {
            document {
                paragraph {
                    link url
                    link 'Craig Burke', url: url
                }
            }
        }.document

        Paragraph paragraph = result.children[0]
        Link link1 = paragraph.children[0]
        Link link2 = paragraph.children[1]
        Link link3 = paragraph.children[2]

        then:
        link1.value == url
        link1.url == url

        and:
        link2.value == 'Craig Burke'
        link2.url == url
    }

    @Unroll('Template keys calculated for #description')
    def "template keys are calculated"() {
        when:
        List<String> actualKeys = node.getTemplateKeys(nodeKey)

        then:
        actualKeys.size() == expectedKeys.size()
        actualKeys.each {assert it in expectedKeys}

        where:
        node                                   | nodeKey     || expectedKeys
        new Paragraph()                        | 'paragraph' || ['paragraph']
        new Paragraph(style: 'foo')            | 'paragraph' || ['paragraph', 'paragraph.foo']

        new Text()                             | 'text'      || ['text']
        new Text(style: 'bar')                 | 'text'      || ['text', 'text.bar']

        new Table()                            | 'table'     || ['table']
        new Table(style: 'foo')                | 'table'     || ['table', 'table.foo']
        new Row()                              | 'row'       || ['row']
        new Row(style: 'foo')                  | 'row'       || ['row', 'row.foo']
        new Cell()                             | 'cell'      || ['cell']
        new Cell(style: 'foo')                 | 'cell'      || ['cell', 'cell.foo']

        new Heading(level: 1)                  | 'heading'   || ['heading', 'heading1']
        new Heading(level: 1, style: 'foobar') | 'heading'   || ['heading', 'heading1', 'heading.foobar', 'heading1.foobar']
        new Heading(level: 2)                  | 'heading'   || ['heading', 'heading2']
        new Heading(level: 2, style: 'foobar') | 'heading'   || ['heading', 'heading2', 'heading.foobar', 'heading2.foobar']
        new Heading(level: 3)                  | 'heading'   || ['heading', 'heading3']
        new Heading(level: 3, style: 'foobar') | 'heading'   || ['heading', 'heading3', 'heading.foobar', 'heading3.foobar']
        new Heading(level: 4)                  | 'heading'   || ['heading', 'heading4']
        new Heading(level: 4, style: 'foobar') | 'heading'   || ['heading', 'heading4', 'heading.foobar', 'heading4.foobar']
        new Heading(level: 5)                  | 'heading'   || ['heading', 'heading5']
        new Heading(level: 5, style: 'foobar') | 'heading'   || ['heading', 'heading5', 'heading.foobar', 'heading5.foobar']
        new Heading(level: 6)                  | 'heading'   || ['heading', 'heading6']
        new Heading(level: 6, style: 'foobar') | 'heading'   || ['heading', 'heading6', 'heading.foobar', 'heading6.foobar']

        description = "${nodeKey}${node.style ? ".${node.style}" : ''}"
    }

    def "simple paragraph header in document map"() {
        given:
        def headerFooterOptions = new HeaderFooterOptions(
            pageNumber: 1,
            pageCount: 2,
            dateGenerated: OffsetDateTime.parse('2018-06-21T15:06Z')
        )

        when:
        Document result = builder.create {
            document(header: {paragraph 'HEADER'}) {
                paragraph 'Content'
            }
        }.document

        then:
        notThrown(Exception)

        and:
        builder.headerClosure
        result.children.size() == 1

        when:
        BlockNode header = builder.buildHeaderNode(headerFooterOptions)

        then:
        header instanceof Paragraph
        (header as Paragraph).text == 'HEADER'

    }

    def "simple paragraph header in create closure"() {
        given:
        def headerFooterOptions = new HeaderFooterOptions(
            pageNumber: 1,
            pageCount: 2,
            dateGenerated: OffsetDateTime.parse('2018-06-21T15:06Z')
        )

        when:
        Document result = builder.create {
            header {
                paragraph 'HEADER'
            }
            document {
                paragraph 'Content'
            }
        }.document

        then:
        notThrown(Exception)

        and:
        builder.headerClosure
        result.children.size() == 1

        when:
        BlockNode header = builder.buildHeaderNode(headerFooterOptions)

        then:
        header instanceof Paragraph
        (header as Paragraph).text == 'HEADER'

    }

    def "paragraph header in create closure using options"() {
        given:
        def headerFooterOptions = new HeaderFooterOptions(
            pageNumber: 1,
            pageCount: 2,
            dateGenerated: OffsetDateTime.parse('2018-06-21T15:06Z')
        )

        when:
        Document result = builder.create {
            header {info ->
                paragraph "Date Generated: ${info.dateGenerated.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}"
            }
            document {
                paragraph 'Content'
            }
        }.document

        then:
        notThrown(Exception)

        and:
        builder.headerClosure
        result.children.size() == 1

        when:
        BlockNode header = builder.buildHeaderNode(headerFooterOptions)

        then:
        header instanceof Paragraph
        (header as Paragraph).text == 'Date Generated: 2018-06-21T15:06:00Z'

    }

    def "table header in create closure using options"() {
        given:
        def headerFooterOptions = new HeaderFooterOptions(
            pageNumber: 1,
            pageCount: 2,
            dateGenerated: OffsetDateTime.parse('2018-06-21T15:06Z')
        )

        when:
        Document result = builder.create {
            header {info ->
                table(border: [size: 0]) {
                    row {
                        cell "Date Generated: ${info.dateGenerated.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}"
                        cell "Page ${info.pageNumber} of ${info.pageCount}", align: 'right'
                    }
                }
            }
            document {
                paragraph 'Content'
            }
        }.document

        then:
        notThrown(Exception)

        and:
        builder.headerClosure
        result.children.size() == 1

        when:
        BlockNode header = builder.buildHeaderNode(headerFooterOptions)

        then:
        header instanceof Table

        when:
        Table table = header as Table

        then:
        table.rows.size() == 1
        table.rows.first().numberOfColumns == 2

        and:
        table.rows.first().cells[0].children.size() == 1
        table.rows.first().cells[0].children.first() instanceof Paragraph
        (table.rows.first().cells[0].children.first() as Paragraph).text == 'Date Generated: 2018-06-21T15:06:00Z'
        table.rows.first().cells[1].children.size() == 1
        table.rows.first().cells[1].children.first() instanceof Paragraph
        (table.rows.first().cells[1].children.first() as Paragraph).text == 'Page 1 of 2'
        (table.rows.first().cells[1].children.first() as Paragraph).align == Align.RIGHT

    }

    def "simple paragraph footer in document map"() {
        given:
        def headerFooterOptions = new HeaderFooterOptions(
            pageNumber: 1,
            pageCount: 2,
            dateGenerated: OffsetDateTime.parse('2018-06-21T15:06Z')
        )

        when:
        Document result = builder.create {
            document(footer: {
                paragraph 'footer'
            }) {
                paragraph 'Content'
            }
        }.document

        then:
        notThrown(Exception)

        and:
        builder.footerClosure
        result.children.size() == 1

        when:
        BlockNode footer = builder.buildFooterNode(headerFooterOptions)

        then:
        footer instanceof Paragraph
        (footer as Paragraph).text == 'footer'

    }

    def "simple paragraph footer in create closure"() {
        given:
        def headerFooterOptions = new HeaderFooterOptions(
            pageNumber: 1,
            pageCount: 2,
            dateGenerated: OffsetDateTime.parse('2018-06-21T15:06Z')
        )

        when:
        Document result = builder.create {
            footer {
                paragraph 'footer'
            }
            document {
                paragraph 'Content'
            }
        }.document

        then:
        notThrown(Exception)

        and:
        builder.footerClosure
        result.children.size() == 1

        when:
        BlockNode footer = builder.buildFooterNode(headerFooterOptions)

        then:
        footer instanceof Paragraph
        (footer as Paragraph).text == 'footer'

    }

    def "paragraph footer in create closure using options"() {
        given:
        def headerFooterOptions = new HeaderFooterOptions(
            pageNumber: 1,
            pageCount: 2,
            dateGenerated: OffsetDateTime.parse('2018-06-21T15:06Z')
        )

        when:
        Document result = builder.create {
            footer {info ->
                paragraph "Date Generated: ${info.dateGenerated.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}"
            }
            document {

                paragraph 'Content'
            }
        }.document

        then:
        notThrown(Exception)

        and:
        builder.footerClosure
        result.children.size() == 1

        when:
        BlockNode footer = builder.buildFooterNode(headerFooterOptions)

        then:
        footer instanceof Paragraph
        (footer as Paragraph).text == 'Date Generated: 2018-06-21T15:06:00Z'

    }

    def "table footer in document closure using options"() {
        given:
        def headerFooterOptions = new HeaderFooterOptions(
            pageNumber: 1,
            pageCount: 2,
            dateGenerated: OffsetDateTime.parse('2018-06-21T15:06Z')
        )

        when:
        Document result = builder.create {
            footer {info ->
                table(border: [size: 0]) {
                    row {
                        cell "Date Generated: ${info.dateGenerated.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}"
                        cell "Page ${info.pageNumber} of ${info.pageCount}", align: 'right'
                    }
                }
            }
            document {
                paragraph 'Content'
            }
        }.document

        then:
        notThrown(Exception)

        and:
        builder.footerClosure
        result.children.size() == 1

        when:
        BlockNode footer = builder.buildFooterNode(headerFooterOptions)

        then:
        footer instanceof Table

        when:
        Table table = footer as Table

        then:
        table.rows.size() == 1
        table.rows.first().numberOfColumns == 2

        and:
        table.rows.first().cells[0].children.size() == 1
        table.rows.first().cells[0].children.first() instanceof Paragraph
        (table.rows.first().cells[0].children.first() as Paragraph).text == 'Date Generated: 2018-06-21T15:06:00Z'
        table.rows.first().cells[1].children.size() == 1
        table.rows.first().cells[1].children.first() instanceof Paragraph
        (table.rows.first().cells[1].children.first() as Paragraph).text == 'Page 1 of 2'
        (table.rows.first().cells[1].children.first() as Paragraph).align == Align.RIGHT

    }

    def "create document with header and footer"() {
        given:
        def headerFooterOptions = new HeaderFooterOptions(
            pageNumber: 1,
            pageCount: 2,
            dateGenerated: OffsetDateTime.parse('2018-06-21T15:06Z')
        )

        when:
        Document result = builder.create {
            header {info ->
                paragraph "Date Generated: ${info.dateGenerated.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}"
            }
            footer {info ->
                table(border: [size: 0]) {
                    row {
                        cell "Date Generated: ${info.dateGenerated.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)}"
                        cell "Page ${info.pageNumber} of ${info.pageCount}", align: 'right'
                    }
                }
            }
            document {
                paragraph 'Content'
            }
        }.document

        then:
        notThrown(Exception)

        and:
        builder.headerClosure
        result.children.size() == 1

        when:
        BlockNode header = builder.buildHeaderNode(headerFooterOptions)

        then:
        header instanceof Paragraph
        (header as Paragraph).text == 'Date Generated: 2018-06-21T15:06:00Z'

        and:
        builder.footerClosure
        result.children.size() == 1

        when:
        BlockNode footer = builder.buildFooterNode(headerFooterOptions)

        then:
        footer instanceof Table

        when:
        Table table = footer as Table

        then:
        table.rows.size() == 1
        table.rows.first().numberOfColumns == 2

        and:
        table.rows.first().cells[0].children.size() == 1
        table.rows.first().cells[0].children.first() instanceof Paragraph
        (table.rows.first().cells[0].children.first() as Paragraph).text == 'Date Generated: 2018-06-21T15:06:00Z'
        table.rows.first().cells[1].children.size() == 1
        table.rows.first().cells[1].children.first() instanceof Paragraph
        (table.rows.first().cells[1].children.first() as Paragraph).text == 'Page 1 of 2'
        (table.rows.first().cells[1].children.first() as Paragraph).align == Align.RIGHT

    }

    void 'define global template in attributes as closure'() {
        given:
        def customTemplate = {
            'document' font: [family: 'Helvetica', size: 13.pt], margin: [top: 1.5.inches]
            'paragraph' font: [color: '#333333']
            'paragraph.myStyle' font: [bold: true]
        }

        when:
        Document result = builder.create {
            document(template: customTemplate) {
                paragraph 'Hello'
                paragraph 'Paragraph with style applied', style: 'myStyle'
            }
        }.document
        Paragraph p1 = (result.children[0] as Paragraph)
        Paragraph p2 = (result.children[1] as Paragraph)

        then:
        result.font.family == 'Helvetica'
        result.font.size == 13
        result.margin.top == 108 // inches
        result.margin.bottom == 72
        result.margin.left == 72
        result.margin.right == 72

        and:
        p1.font.family == 'Helvetica'
        p1.font.size == 13
        p1.font.color.hex == '333333'

        and:
        p2.font.family == 'Helvetica'
        p2.font.size == 13
        p2.font.color.hex == '333333'
        p2.font.bold
    }

    void 'define global template as closure in create'() {
        when:
        Document result = builder.create {
            template {
                document font: [family: 'Helvetica', size: 13.pt], margin: [top: 1.5.inches]
                paragraph font: [color: '#333333']
                'paragraph.myStyle' font: [bold: true]
            }
            document {
                paragraph 'Hello'
                paragraph 'Paragraph with style applied', style: 'myStyle'
            }
        }.document
        Paragraph p1 = (result.children[0] as Paragraph)
        Paragraph p2 = (result.children[1] as Paragraph)

        then:
        result.font.family == 'Helvetica'
        result.font.size == 13
        result.margin.top == 108 // inches
        result.margin.bottom == 72
        result.margin.left == 72
        result.margin.right == 72

        and:
        p1.font.family == 'Helvetica'
        p1.font.size == 13
        p1.font.color.hex == '333333'

        and:
        p2.font.family == 'Helvetica'
        p2.font.size == 13
        p2.font.color.hex == '333333'
        p2.font.bold
    }

    void 'define global template as closure in create after document defined'() {
        when:
        builder.create {
            document {
                paragraph 'Hello'
                paragraph 'Paragraph with style applied', style: 'myStyle'
            }
            template {
                document font: [family: 'Helvetica', size: 13.pt], margin: [top: 1.5.inches]
                paragraph font: [color: '#333333']
                'paragraph.myStyle' font: [bold: true]
            }
        }

        then:
        thrown(IllegalStateException)
    }

}
