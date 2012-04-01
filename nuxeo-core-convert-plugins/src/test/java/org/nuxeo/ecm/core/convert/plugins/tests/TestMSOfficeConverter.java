/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.ecm.core.convert.plugins.tests;

import org.junit.Test;

public class TestMSOfficeConverter extends BaseConverterTest {

    // Word POI tests fails in surefire
    @Test
    public void testWordConverter() throws Exception {
        doTestTextConverter("application/msword", "msoffice2text", "hello.doc");
    }

    @Test
    public void testWordArabicConverter() throws Exception {
        doTestArabicTextConverter("application/msword", "msoffice2text", "wikipedia-internet-ar.doc");
    }

    @Test
    public void testPptConverter() throws Exception {
        doTestTextConverter("application/vnd.ms-powerpoint", "msoffice2text",
                "hello.ppt");
    }

    @Test
    public void testDocxConverter() throws Exception {
        doTestTextConverter(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "msoffice2text", "hello.docx");
    }

    @Test
    public void testPptxConverter() throws Exception {
        doTestTextConverter(
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "msoffice2text", "hello.pptx");
    }

    @Test
    public void testExcelConverter() throws Exception {
        doTestTextConverter("application/vnd.ms-excel", "xl2text", "hello.xls");
    }

    @Test
    public void testXlsxConverter() throws Exception {
        doTestTextConverter(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "xlx2text", "hello.xlsx");
    }

    @Test
    public void testAnyToTextConverterWord() throws Exception {
        doTestAny2TextConverter("application/msword", "msoffice2text",
                "hello.doc");
    }

    @Test
    public void testAnyToTextDocxConverter() throws Exception {
        doTestAny2TextConverter("application/msword", "msoffice2text",
                "hello.docx");
    }

    @Test
    public void testAnyToTextExcelConverter() throws Exception {
        doTestAny2TextConverter("application/vnd.ms-excel", "xl2text",
                "hello.xls");
    }

    @Test
    public void testAnyToTextXlsxConverter() throws Exception {
        doTestAny2TextConverter(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "xlx2text", "hello.xlsx");
    }
}
