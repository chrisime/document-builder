package com.craigburke.document.core.unit

import groovy.transform.CompileStatic

/**
 * Utility class for converting typographic units
 * @author Craig Burke
 */
@CompileStatic
class UnitUtil {
    static final BigDecimal POINTS_PER_INCH       = 72G
    static final BigDecimal CENTIMETER_PER_INCH   = 2.54G
    static final BigDecimal POINTS_PER_CENTIMETER = POINTS_PER_INCH / CENTIMETER_PER_INCH
    static final BigDecimal PICA_POINTS           = 6G
    static final BigDecimal TWIP_POINTS           = 20G
    static final BigDecimal EIGTH_POINTS          = 8G
    static final BigDecimal HALF_POINTS           = 2G
    static final BigDecimal EMU_POINTS            = 12700G

    static BigDecimal inchToPoint(Number inch) {
        inch * POINTS_PER_INCH
    }

    static BigDecimal pointToInch(Number point) {
        point / POINTS_PER_INCH
    }

    static BigDecimal cmToPoint(Number cm) {
        cm * POINTS_PER_CENTIMETER
    }

    static BigDecimal pointToCm(Number point) {
        point / POINTS_PER_CENTIMETER
    }

    static BigDecimal pointToPica(Number point) {
        point * PICA_POINTS
    }

    static BigDecimal picaToPoint(Number pica) {
        pica / PICA_POINTS
    }

    static BigDecimal pointToEigthPoint(Number point) {
        point * EIGTH_POINTS
    }

    static BigDecimal eightPointToPoint(Number eigthPoint) {
        eigthPoint / EIGTH_POINTS
    }

    static BigDecimal pointToHalfPoint(Number point) {
        point * HALF_POINTS
    }

    static BigDecimal halfPointToPoint(Number halfPoint) {
        halfPoint / HALF_POINTS
    }

    static BigDecimal pointToTwip(Number point) {
        point * TWIP_POINTS
    }

    static BigDecimal twipToPoint(Number twip) {
        twip / TWIP_POINTS
    }

    static BigDecimal pointToEmu(Number point) {
        point * EMU_POINTS
    }

    static BigDecimal emuToPoint(Number emu) {
        emu / EMU_POINTS
    }

    static BigDecimal inchToCm(Number inch) {
        inch * CENTIMETER_PER_INCH
    }

    static BigDecimal cmToInch(Number inch) {
        inch / CENTIMETER_PER_INCH
    }
}
