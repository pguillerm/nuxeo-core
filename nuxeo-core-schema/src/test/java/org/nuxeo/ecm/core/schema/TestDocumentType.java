/*
 * Copyright (c) 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 * $Id: JOOoConvertPluginImpl.java 18651 2007-05-13 20:28:53Z sfermigier $
 */

package org.nuxeo.ecm.core.schema;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestDocumentType {

    @Test
    public void testTrivialDocumentType() {
        DocumentType docType = new DocumentTypeImpl((DocumentType) null, "doc type");

        assertEquals("doc type", docType.getName());

        assertTrue(docType.isFile());
        assertFalse(docType.isFolder());
        assertFalse(docType.isOrdered());
    }

    @Test
    public void testFileDocumentType() {
        DocumentType docType = new DocumentTypeImpl((DocumentType) null, "doc type", null,
                null);

        assertTrue(docType.isFile());
        assertFalse(docType.isFolder());
        assertFalse(docType.isOrdered());
    }

    @Test
    public void testFolderDocumentType() {
        DocumentType docType = new DocumentTypeImpl((DocumentType) null, "doc type", null,
                new String[]{"Folderish"});

        assertFalse(docType.isFile());
        assertTrue(docType.isFolder());
        assertFalse(docType.isOrdered());
    }

    @Test
    public void testOrderedFolderDocumentType() {
        DocumentType docType = new DocumentTypeImpl((DocumentType) null, "doc type", null,
                new String[]{"Folderish", "Orderable"});

        assertFalse(docType.isFile());
        assertTrue(docType.isFolder());
        assertTrue(docType.isOrdered());
    }

}
