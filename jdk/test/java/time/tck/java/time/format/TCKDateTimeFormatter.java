/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Copyright (c) 2008-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package tck.java.time.format;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ThaiBuddhistChronology;
import java.time.format.DateTimeFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.SignStyle;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test DateTimeFormatter.
 */
@Test(groups={"tck"})
public class TCKDateTimeFormatter {

    private static final ZoneOffset OFFSET_PONE = ZoneOffset.ofHours(1);
    private static final ZoneOffset OFFSET_PTHREE = ZoneOffset.ofHours(3);
    private static final ZoneId ZONE_PARIS = ZoneId.of("Europe/Paris");

    private static final DateTimeFormatter BASIC_FORMATTER = DateTimeFormatter.ofPattern("'ONE'd");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("'ONE'yyyy MM dd");

    private DateTimeFormatter fmt;

    @BeforeMethod
    public void setUp() {
        fmt = new DateTimeFormatterBuilder().appendLiteral("ONE")
                                            .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
                                            .toFormatter();
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_withLocale() {
        DateTimeFormatter base = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        DateTimeFormatter test = base.withLocale(Locale.GERMAN);
        assertEquals(test.getLocale(), Locale.GERMAN);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_withLocale_null() {
        DateTimeFormatter base = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        base.withLocale((Locale) null);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_withChronology() {
        DateTimeFormatter test = fmt;
        assertEquals(test.getChronology(), null);
        test = test.withChronology(IsoChronology.INSTANCE);
        assertEquals(test.getChronology(), IsoChronology.INSTANCE);
        test = test.withChronology(null);
        assertEquals(test.getChronology(), null);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_withZone() {
        DateTimeFormatter test = fmt;
        assertEquals(test.getZone(), null);
        test = test.withZone(ZoneId.of("Europe/Paris"));
        assertEquals(test.getZone(), ZoneId.of("Europe/Paris"));
        test = test.withZone(ZoneOffset.UTC);
        assertEquals(test.getZone(), ZoneOffset.UTC);
        test = test.withZone(null);
        assertEquals(test.getZone(), null);
    }

    //-----------------------------------------------------------------------
    // print
    //-----------------------------------------------------------------------
    @DataProvider(name="print")
    Object[][] data_format() {
        LocalDate ld = LocalDate.of(2008, 6, 30);
        LocalTime lt = LocalTime.of(11, 30);
        LocalDateTime ldt = LocalDateTime.of(2008, 6, 30, 11, 30);
        OffsetTime ot = OffsetTime.of(LocalTime.of(11, 30), OFFSET_PONE);
        OffsetDateTime odt = OffsetDateTime.of(LocalDateTime.of(2008, 6, 30, 11, 30), OFFSET_PONE);
        ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.of(2008, 6, 30, 11, 30), ZONE_PARIS);
        Instant instant = Instant.ofEpochSecond(3600);
        return new Object[][] {
                {null, null, ld, "2008::"},
                {null, null, lt, ":11:"},
                {null, null, ldt, "2008:11:"},
                {null, null, ot, ":11:+01:00"},
                {null, null, odt, "2008:11:+01:00"},
                {null, null, zdt, "2008:11:+02:00Europe/Paris"},
                {null, null, instant, "::"},

                {null, ZONE_PARIS, ld, "2008::"},
                {null, ZONE_PARIS, lt, ":11:"},
                {null, ZONE_PARIS, ldt, "2008:11:"},
                {null, ZONE_PARIS, ot, ":11:+01:00"},
                {null, ZONE_PARIS, odt, "2008:12:+02:00Europe/Paris"},
                {null, ZONE_PARIS, zdt, "2008:11:+02:00Europe/Paris"},
                {null, ZONE_PARIS, instant, "1970:02:+01:00Europe/Paris"},

                {null, OFFSET_PTHREE, ld, "2008::"},
                {null, OFFSET_PTHREE, lt, ":11:"},
                {null, OFFSET_PTHREE, ldt, "2008:11:"},
                {null, OFFSET_PTHREE, ot, ":11:+01:00"},
                {null, OFFSET_PTHREE, odt, "2008:13:+03:00"},
                {null, OFFSET_PTHREE, zdt, "2008:12:+03:00"},
                {null, OFFSET_PTHREE, instant, "1970:04:+03:00"},

                {ThaiBuddhistChronology.INSTANCE, null, ld, "2551::"},
                {ThaiBuddhistChronology.INSTANCE, null, lt, ":11:"},
                {ThaiBuddhistChronology.INSTANCE, null, ldt, "2551:11:"},
                {ThaiBuddhistChronology.INSTANCE, null, ot, ":11:+01:00"},
                {ThaiBuddhistChronology.INSTANCE, null, odt, "2551:11:+01:00"},
                {ThaiBuddhistChronology.INSTANCE, null, zdt, "2551:11:+02:00Europe/Paris"},
                {ThaiBuddhistChronology.INSTANCE, null, instant, "::"},

                {ThaiBuddhistChronology.INSTANCE, ZONE_PARIS, ld, "2551::"},
                {ThaiBuddhistChronology.INSTANCE, ZONE_PARIS, lt, ":11:"},
                {ThaiBuddhistChronology.INSTANCE, ZONE_PARIS, ldt, "2551:11:"},
                {ThaiBuddhistChronology.INSTANCE, ZONE_PARIS, ot, ":11:+01:00"},
                {ThaiBuddhistChronology.INSTANCE, ZONE_PARIS, odt, "2551:12:+02:00Europe/Paris"},
                {ThaiBuddhistChronology.INSTANCE, ZONE_PARIS, zdt, "2551:11:+02:00Europe/Paris"},
                {ThaiBuddhistChronology.INSTANCE, ZONE_PARIS, instant, "1970:02:+01:00Europe/Paris"},
        };
    }

    @Test(dataProvider="print")
    public void test_print_Temporal(Chronology overrideChrono, ZoneId overrideZone, Temporal temporal, String expected) {
        DateTimeFormatter test = new DateTimeFormatterBuilder()
                .optionalStart().appendValue(YEAR, 4).optionalEnd()
                .appendLiteral(':').optionalStart().appendValue(HOUR_OF_DAY, 2).optionalEnd()
                .appendLiteral(':').optionalStart().appendOffsetId().optionalStart().appendZoneRegionId().optionalEnd().optionalEnd()
                .toFormatter(Locale.ENGLISH)
                .withChronology(overrideChrono).withZone(overrideZone);
        String result = test.format(temporal);
        assertEquals(result, expected);
    }

    @Test
    public void test_print_Temporal_simple() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        String result = test.format(LocalDate.of(2008, 6, 30));
        assertEquals(result, "ONE30");
    }

    @Test(expectedExceptions=DateTimeException.class)
    public void test_print_Temporal_noSuchField() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        test.format(LocalTime.of(11, 30));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_print_Temporal_null() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        test.format((TemporalAccessor) null);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_print_TemporalAppendable() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        StringBuilder buf = new StringBuilder();
        test.formatTo(LocalDate.of(2008, 6, 30), buf);
        assertEquals(buf.toString(), "ONE30");
    }

    @Test(expectedExceptions=DateTimeException.class)
    public void test_print_TemporalAppendable_noSuchField() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        StringBuilder buf = new StringBuilder();
        test.formatTo(LocalTime.of(11, 30), buf);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_print_TemporalAppendable_nullTemporal() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        StringBuilder buf = new StringBuilder();
        test.formatTo((TemporalAccessor) null, buf);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_print_TemporalAppendable_nullAppendable() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        test.formatTo(LocalDate.of(2008, 6, 30), (Appendable) null);
    }

    //-----------------------------------------------------------------------
    // parse(CharSequence)
    //-----------------------------------------------------------------------
    @Test
    public void test_parse_CharSequence() {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        TemporalAccessor result = test.parse("ONE30");
        assertEquals(result.isSupported(DAY_OF_MONTH), true);
        assertEquals(result.getLong(DAY_OF_MONTH), 30L);
        assertEquals(result.isSupported(HOUR_OF_DAY), false);
    }

    @Test
    public void test_parse_CharSequence_resolved() {
        DateTimeFormatter test = DateTimeFormatter.ISO_DATE;
        TemporalAccessor result = test.parse("2012-06-30");
        assertEquals(result.isSupported(YEAR), true);
        assertEquals(result.isSupported(MONTH_OF_YEAR), true);
        assertEquals(result.isSupported(DAY_OF_MONTH), true);
        assertEquals(result.isSupported(HOUR_OF_DAY), false);
        assertEquals(result.getLong(YEAR), 2012L);
        assertEquals(result.getLong(MONTH_OF_YEAR), 6L);
        assertEquals(result.getLong(DAY_OF_MONTH), 30L);
        assertEquals(result.query(LocalDate::from), LocalDate.of(2012, 6, 30));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_parse_CharSequence_null() {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        test.parse((String) null);
    }

    //-----------------------------------------------------------------------
    // parse(CharSequence)
    //-----------------------------------------------------------------------
    @Test
    public void test_parse_CharSequence_ParsePosition() {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        ParsePosition pos = new ParsePosition(3);
        TemporalAccessor result = test.parse("XXXONE30XXX", pos);
        assertEquals(pos.getIndex(), 8);
        assertEquals(pos.getErrorIndex(), -1);
        assertEquals(result.isSupported(DAY_OF_MONTH), true);
        assertEquals(result.getLong(DAY_OF_MONTH), 30L);
        assertEquals(result.isSupported(HOUR_OF_DAY), false);
    }

    @Test
    public void test_parse_CharSequence_ParsePosition_resolved() {
        DateTimeFormatter test = DateTimeFormatter.ISO_DATE;
        ParsePosition pos = new ParsePosition(3);
        TemporalAccessor result = test.parse("XXX2012-06-30XXX", pos);
        assertEquals(pos.getIndex(), 13);
        assertEquals(pos.getErrorIndex(), -1);
        assertEquals(result.isSupported(YEAR), true);
        assertEquals(result.isSupported(MONTH_OF_YEAR), true);
        assertEquals(result.isSupported(DAY_OF_MONTH), true);
        assertEquals(result.isSupported(HOUR_OF_DAY), false);
        assertEquals(result.getLong(YEAR), 2012L);
        assertEquals(result.getLong(MONTH_OF_YEAR), 6L);
        assertEquals(result.getLong(DAY_OF_MONTH), 30L);
        assertEquals(result.query(LocalDate::from), LocalDate.of(2012, 6, 30));
    }

    @Test(expectedExceptions=DateTimeParseException.class)
    public void test_parse_CharSequence_ParsePosition_parseError() {
        DateTimeFormatter test = DateTimeFormatter.ISO_DATE;
        ParsePosition pos = new ParsePosition(3);
        try {
            test.parse("XXX2012XXX", pos);
            fail();
        } catch (DateTimeParseException ex) {
            assertEquals(ex.getErrorIndex(), 7);
            throw ex;
        }
    }

    @Test(expectedExceptions=IndexOutOfBoundsException.class)
    public void test_parse_CharSequence_ParsePosition_indexTooBig() {
        DateTimeFormatter test = DateTimeFormatter.ISO_DATE;
        test.parse("Text", new ParsePosition(5));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_parse_CharSequence_ParsePosition_nullText() {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        test.parse((CharSequence) null, new ParsePosition(0));
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_parse_CharSequence_ParsePosition_nullParsePosition() {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        test.parse("Text", (ParsePosition) null);
    }

    //-----------------------------------------------------------------------
    // parse(Query)
    //-----------------------------------------------------------------------
    @Test
    public void test_parse_Query_String() throws Exception {
        LocalDate result = DATE_FORMATTER.parse("ONE2012 07 27", LocalDate::from);
        assertEquals(result, LocalDate.of(2012, 7, 27));
    }

    @Test
    public void test_parse_Query_CharSequence() throws Exception {
        LocalDate result = DATE_FORMATTER.parse(new StringBuilder("ONE2012 07 27"), LocalDate::from);
        assertEquals(result, LocalDate.of(2012, 7, 27));
    }

    @Test(expectedExceptions=DateTimeParseException.class)
    public void test_parse_Query_String_parseError() throws Exception {
        try {
            DATE_FORMATTER.parse("ONE2012 07 XX", LocalDate::from);
        } catch (DateTimeParseException ex) {
            assertEquals(ex.getMessage().contains("could not be parsed"), true);
            assertEquals(ex.getMessage().contains("ONE2012 07 XX"), true);
            assertEquals(ex.getParsedString(), "ONE2012 07 XX");
            assertEquals(ex.getErrorIndex(), 11);
            throw ex;
        }
    }

    @Test(expectedExceptions=DateTimeParseException.class)
    public void test_parse_Query_String_parseErrorLongText() throws Exception {
        try {
            DATE_FORMATTER.parse("ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789", LocalDate::from);
        } catch (DateTimeParseException ex) {
            assertEquals(ex.getMessage().contains("could not be parsed"), true);
            assertEquals(ex.getMessage().contains("ONEXXX6789012345678901234567890123456789012345678901234567890123..."), true);
            assertEquals(ex.getParsedString(), "ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789");
            assertEquals(ex.getErrorIndex(), 3);
            throw ex;
        }
    }

    @Test(expectedExceptions=DateTimeParseException.class)
    public void test_parse_Query_String_parseIncomplete() throws Exception {
        try {
            DATE_FORMATTER.parse("ONE2012 07 27SomethingElse", LocalDate::from);
        } catch (DateTimeParseException ex) {
            assertEquals(ex.getMessage().contains("could not be parsed"), true);
            assertEquals(ex.getMessage().contains("ONE2012 07 27SomethingElse"), true);
            assertEquals(ex.getParsedString(), "ONE2012 07 27SomethingElse");
            assertEquals(ex.getErrorIndex(), 13);
            throw ex;
        }
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_parse_Query_String_nullText() throws Exception {
        DATE_FORMATTER.parse((String) null, LocalDate::from);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_parse_Query_String_nullRule() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        test.parse("30", (TemporalQuery<?>) null);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_parseBest_firstOption() throws Exception {
        DateTimeFormatter test = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm[XXX]");
        TemporalAccessor result = test.parseBest("2011-06-30 12:30+03:00", ZonedDateTime::from, LocalDateTime::from);
        LocalDateTime ldt = LocalDateTime.of(2011, 6, 30, 12, 30);
        assertEquals(result, ZonedDateTime.of(ldt, ZoneOffset.ofHours(3)));
    }

    @Test
    public void test_parseBest_secondOption() throws Exception {
        DateTimeFormatter test = DateTimeFormatter.ofPattern("yyyy-MM-dd[ HH:mm[XXX]]");
        TemporalAccessor result = test.parseBest("2011-06-30", ZonedDateTime::from, LocalDate::from);
        assertEquals(result, LocalDate.of(2011, 6, 30));
    }

    @Test(expectedExceptions=DateTimeParseException.class)
    public void test_parseBest_String_parseError() throws Exception {
        DateTimeFormatter test = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm[XXX]");
        try {
            test.parseBest("2011-06-XX", ZonedDateTime::from, LocalDateTime::from);
        } catch (DateTimeParseException ex) {
            assertEquals(ex.getMessage().contains("could not be parsed"), true);
            assertEquals(ex.getMessage().contains("XX"), true);
            assertEquals(ex.getParsedString(), "2011-06-XX");
            assertEquals(ex.getErrorIndex(), 8);
            throw ex;
        }
    }

    @Test(expectedExceptions=DateTimeParseException.class)
    public void test_parseBest_String_parseErrorLongText() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        try {
            test.parseBest("ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789", ZonedDateTime::from, LocalDate::from);
        } catch (DateTimeParseException ex) {
            assertEquals(ex.getMessage().contains("could not be parsed"), true);
            assertEquals(ex.getMessage().contains("ONEXXX6789012345678901234567890123456789012345678901234567890123..."), true);
            assertEquals(ex.getParsedString(), "ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789");
            assertEquals(ex.getErrorIndex(), 3);
            throw ex;
        }
    }

    @Test(expectedExceptions=DateTimeParseException.class)
    public void test_parseBest_String_parseIncomplete() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        try {
            test.parseBest("ONE30SomethingElse", ZonedDateTime::from, LocalDate::from);
        } catch (DateTimeParseException ex) {
            assertEquals(ex.getMessage().contains("could not be parsed"), true);
            assertEquals(ex.getMessage().contains("ONE30SomethingElse"), true);
            assertEquals(ex.getParsedString(), "ONE30SomethingElse");
            assertEquals(ex.getErrorIndex(), 5);
            throw ex;
        }
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_parseBest_String_nullText() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        test.parseBest((String) null, ZonedDateTime::from, LocalDate::from);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_parseBest_String_nullRules() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        test.parseBest("30", (TemporalQuery<?>[]) null);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void test_parseBest_String_zeroRules() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        test.parseBest("30", new TemporalQuery<?>[0]);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void test_parseBest_String_oneRule() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        test.parseBest("30", LocalDate::from);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_parseUnresolved_StringParsePosition() {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        ParsePosition pos = new ParsePosition(0);
        TemporalAccessor result = test.parseUnresolved("ONE30XXX", pos);
        assertEquals(pos.getIndex(), 5);
        assertEquals(pos.getErrorIndex(), -1);
        assertEquals(result.getLong(DAY_OF_MONTH), 30L);
    }

    @Test
    public void test_parseUnresolved_StringParsePosition_parseError() {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        ParsePosition pos = new ParsePosition(0);
        TemporalAccessor result = test.parseUnresolved("ONEXXX", pos);
        assertEquals(pos.getIndex(), 0);
        assertEquals(pos.getErrorIndex(), 3);
        assertEquals(result, null);
    }

    @Test
    public void test_parseUnresolved_StringParsePosition_duplicateFieldSameValue() {
        DateTimeFormatter test = new DateTimeFormatterBuilder()
                .appendValue(MONTH_OF_YEAR).appendLiteral('-').appendValue(MONTH_OF_YEAR).toFormatter();
        ParsePosition pos = new ParsePosition(3);
        TemporalAccessor result = test.parseUnresolved("XXX6-6", pos);
        assertEquals(pos.getIndex(), 6);
        assertEquals(pos.getErrorIndex(), -1);
        assertEquals(result.getLong(MONTH_OF_YEAR), 6);
    }

    @Test
    public void test_parseUnresolved_StringParsePosition_duplicateFieldDifferentValue() {
        DateTimeFormatter test = new DateTimeFormatterBuilder()
                .appendValue(MONTH_OF_YEAR).appendLiteral('-').appendValue(MONTH_OF_YEAR).toFormatter();
        ParsePosition pos = new ParsePosition(3);
        TemporalAccessor result = test.parseUnresolved("XXX6-7", pos);
        assertEquals(pos.getIndex(), 3);
        assertEquals(pos.getErrorIndex(), 5);
        assertEquals(result, null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_parseUnresolved_StringParsePosition_nullString() {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        ParsePosition pos = new ParsePosition(0);
        test.parseUnresolved((String) null, pos);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_parseUnresolved_StringParsePosition_nullParsePosition() {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        test.parseUnresolved("ONE30", (ParsePosition) null);
    }

    @Test(expectedExceptions=IndexOutOfBoundsException.class)
    public void test_parseUnresolved_StringParsePosition_invalidPosition() {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        ParsePosition pos = new ParsePosition(6);
        test.parseUnresolved("ONE30", pos);
    }

    //-----------------------------------------------------------------------
    //-----------------------------------------------------------------------
    @Test
    public void test_toFormat_format() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        Format format = test.toFormat();
        String result = format.format(LocalDate.of(2008, 6, 30));
        assertEquals(result, "ONE30");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_toFormat_format_null() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        Format format = test.toFormat();
        format.format(null);
    }

    @Test(expectedExceptions=IllegalArgumentException.class)
    public void test_toFormat_format_notTemporal() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        Format format = test.toFormat();
        format.format("Not a Temporal");
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_toFormat_parseObject_String() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        Format format = test.toFormat();
        TemporalAccessor result = (TemporalAccessor) format.parseObject("ONE30");
        assertEquals(result.isSupported(DAY_OF_MONTH), true);
        assertEquals(result.getLong(DAY_OF_MONTH), 30L);
    }

    @Test(expectedExceptions=ParseException.class)
    public void test_toFormat_parseObject_String_parseError() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        Format format = test.toFormat();
        try {
            format.parseObject("ONEXXX");
        } catch (ParseException ex) {
            assertEquals(ex.getMessage().contains("ONEXXX"), true);
            assertEquals(ex.getErrorOffset(), 3);
            throw ex;
        }
    }

    @Test(expectedExceptions=ParseException.class)
    public void test_toFormat_parseObject_String_parseErrorLongText() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        Format format = test.toFormat();
        try {
            format.parseObject("ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789");
        } catch (DateTimeParseException ex) {
            assertEquals(ex.getMessage().contains("ONEXXX6789012345678901234567890123456789012345678901234567890123..."), true);
            assertEquals(ex.getParsedString(), "ONEXXX67890123456789012345678901234567890123456789012345678901234567890123456789");
            assertEquals(ex.getErrorIndex(), 3);
            throw ex;
        }
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_toFormat_parseObject_String_null() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        Format format = test.toFormat();
        format.parseObject((String) null);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_toFormat_parseObject_StringParsePosition() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        Format format = test.toFormat();
        ParsePosition pos = new ParsePosition(0);
        TemporalAccessor result = (TemporalAccessor) format.parseObject("ONE30XXX", pos);
        assertEquals(pos.getIndex(), 5);
        assertEquals(pos.getErrorIndex(), -1);
        assertEquals(result.isSupported(DAY_OF_MONTH), true);
        assertEquals(result.getLong(DAY_OF_MONTH), 30L);
    }

    @Test
    public void test_toFormat_parseObject_StringParsePosition_parseError() throws Exception {
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        Format format = test.toFormat();
        ParsePosition pos = new ParsePosition(0);
        TemporalAccessor result = (TemporalAccessor) format.parseObject("ONEXXX", pos);
        assertEquals(pos.getIndex(), 0);  // TODO: is this right?
        assertEquals(pos.getErrorIndex(), 3);
        assertEquals(result, null);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_toFormat_parseObject_StringParsePosition_nullString() throws Exception {
        // SimpleDateFormat has this behavior
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        Format format = test.toFormat();
        ParsePosition pos = new ParsePosition(0);
        format.parseObject((String) null, pos);
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_toFormat_parseObject_StringParsePosition_nullParsePosition() throws Exception {
        // SimpleDateFormat has this behavior
        DateTimeFormatter test = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        Format format = test.toFormat();
        format.parseObject("ONE30", (ParsePosition) null);
    }

    @Test
    public void test_toFormat_parseObject_StringParsePosition_invalidPosition_tooBig() throws Exception {
        // SimpleDateFormat has this behavior
        DateTimeFormatter dtf = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        ParsePosition pos = new ParsePosition(6);
        Format test = dtf.toFormat();
        assertNull(test.parseObject("ONE30", pos));
        assertTrue(pos.getErrorIndex() >= 0);
    }

    @Test
    public void test_toFormat_parseObject_StringParsePosition_invalidPosition_tooSmall() throws Exception {
        // SimpleDateFormat throws StringIndexOutOfBoundException
        DateTimeFormatter dtf = fmt.withLocale(Locale.ENGLISH).withSymbols(DateTimeFormatSymbols.STANDARD);
        ParsePosition pos = new ParsePosition(-1);
        Format test = dtf.toFormat();
        assertNull(test.parseObject("ONE30", pos));
        assertTrue(pos.getErrorIndex() >= 0);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_toFormat_Query_format() throws Exception {
        Format format = BASIC_FORMATTER.toFormat();
        String result = format.format(LocalDate.of(2008, 6, 30));
        assertEquals(result, "ONE30");
    }

    @Test
    public void test_toFormat_Query_parseObject_String() throws Exception {
        Format format = DATE_FORMATTER.toFormat(LocalDate::from);
        LocalDate result = (LocalDate) format.parseObject("ONE2012 07 27");
        assertEquals(result, LocalDate.of(2012, 7, 27));
    }

    @Test(expectedExceptions=ParseException.class)
    public void test_toFormat_parseObject_StringParsePosition_dateTimeError() throws Exception {
        Format format = DATE_FORMATTER.toFormat(LocalDate::from);
        format.parseObject("ONE2012 07 32");
    }

    @Test(expectedExceptions=NullPointerException.class)
    public void test_toFormat_Query() throws Exception {
        BASIC_FORMATTER.toFormat(null);
    }

}
