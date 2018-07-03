package com.craigburke.document.core.unit

/**
 * Category that allows for the use of typographic units like inches
 * @author Craig Burke
 */
@Category(Number)
class UnitCategory {
    BigDecimal getInches() { this * UnitUtil.POINTS_PER_INCH }
    BigDecimal getInch() { this * UnitUtil.POINTS_PER_INCH }
    BigDecimal getCentimeters() { this * UnitUtil.POINTS_PER_CENTIMETER }
    BigDecimal getCentimeter() { this * UnitUtil.POINTS_PER_CENTIMETER }
    BigDecimal getCm() { this * UnitUtil.POINTS_PER_CENTIMETER }
    BigDecimal getPt() { this }
    BigDecimal getPx() { this }
}
