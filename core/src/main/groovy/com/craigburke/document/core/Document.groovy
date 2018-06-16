package com.craigburke.document.core

import static com.craigburke.document.core.UnitUtil.inchToPoint

/**
 * Document node
 * @author Craig Burke
 */
class Document extends BlockNode {
    static Margin defaultMargin = new Margin(top: 72, bottom: 72, left: 72, right: 72)

    private static final String PORTRAIT = 'portrait'
    private static final String LANDSCAPE = 'landscape'

    int pageCount
    int width = inchToPoint(PaperSize.LETTER.width).toInteger()
    int height = inchToPoint(PaperSize.LETTER.height).toInteger()
    String orientation = PORTRAIT

    def template
    def header
    def footer

    private Map templateMap

    Map getTemplateMap() {
        if (templateMap == null) {
            loadTemplateMap()
        }
        templateMap
    }

    private void loadTemplateMap() {
        templateMap = [:]
        if (template && template instanceof Closure) {
            def templateDelegate = new Expando()
            templateDelegate.metaClass.methodMissing = { name, args ->
                templateMap[name] = args[0]
            }
            template.delegate = templateDelegate
            template()
        }
    }

    List<BlockNode> children = []
    List<EmbeddedFont> embeddedFonts = []

    /**
     * Set width and height of the document.
     *
     * @param arg name of a standard paper size ("a4", "letter", "legal")
     */
    void setSize(String arg) {
        setSize(PaperSize.get(arg))
    }

    /**
     * Set width and height of the document.
     *
     * @param arg a Dimension instance
     */
    void setSize(Dimension arg) {
        width = inchToPoint(arg.width).toInteger()
        height = inchToPoint(arg.height).toInteger()
    }

    /**
     * Set width and height of the document.
     *
     * @param args width, height
     */
    void setSize(List<Number> args) {
        width = args[0].toInteger()
        height = args[1].toInteger()
    }

    /**
     * Set document orientation.
     *
     * @param arg "portrait" or "landscape"
     */
    void setOrientation(String arg) {
        arg = arg.toLowerCase()
        if (arg != PORTRAIT && arg != LANDSCAPE) {
            throw new IllegalArgumentException("invalid orientation: $arg, only '$PORTRAIT' or '$LANDSCAPE' allowed")
        }
        if (this.@orientation != arg) {
            this.@orientation = arg
            def tmp = width
            width = height
            height = tmp
        }
    }

    boolean isLandscape() {
        this.orientation == LANDSCAPE
    }
}
