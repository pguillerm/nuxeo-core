/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     ataillefer
 */

package org.nuxeo.ecm.core.io.impl;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import junit.framework.TestCase;

import org.dom4j.io.DocumentSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.google.inject.Inject;

/**
 * Tests TypedExportedDocument.
 * 
 * @author <a href="mailto:ataillefer@nuxeo.com">Antoine Taillefer</a>
 */
@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@RepositoryConfig(repositoryName = "default", type = BackendType.H2, init = TypedExportedDocumentRepositoryInit.class, user = "Administrator", cleanup = Granularity.METHOD)
public class TestTypedExportedDocument extends TestCase {

    /** The Constant transformerFactory. */
    protected static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    /** The session. */
    @Inject
    protected CoreSession session;

    /**
     * Test typed exported document.
     * 
     * @throws Exception the exception
     */
    @Test
    public void testTypedExportedDocument() throws Exception {

        DocumentModel doc = session.getDocument(new PathRef("/"
                + TypedExportedDocumentRepositoryInit.TEST_DOC_NAME));
        ExportedDocument exportedDoc = new TypedExportedDocumentImpl(doc);

        // Check system elements.
        assertEquals("File", exportedDoc.getType());
        assertEquals("testDoc", exportedDoc.getPath().toString());

        // Get w3c Document
        org.dom4j.Document dom4jDocument = exportedDoc.getDocument();
        Document document = dom4jToW3c(dom4jDocument);

        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new CoreNamespaceContext());

        // Check dublincore schema
        Node schemaNode = (Node) xpath.evaluate("//schema[@name='dublincore']",
                document, XPathConstants.NODE);
        assertNotNull(schemaNode);

        Node fieldNode = (Node) xpath.evaluate("//dc:title[@type='string']",
                document, XPathConstants.NODE);
        assertNotNull(fieldNode);

        fieldNode = (Node) xpath.evaluate("//dc:created[@type='date']",
                document, XPathConstants.NODE);
        assertNotNull(fieldNode);

        fieldNode = (Node) xpath.evaluate("//dc:creator[@type='string']",
                document, XPathConstants.NODE);
        assertNotNull(fieldNode);

        fieldNode = (Node) xpath.evaluate("//dc:modified[@type='date']",
                document, XPathConstants.NODE);
        assertNotNull(fieldNode);

        fieldNode = (Node) xpath.evaluate(
                "//dc:lastContributor[@type='string']", document,
                XPathConstants.NODE);
        assertNotNull(fieldNode);

        fieldNode = (Node) xpath.evaluate(
                "//dc:contributors[@type='scalarList']", document,
                XPathConstants.NODE);
        assertNotNull(fieldNode);

        fieldNode = (Node) xpath.evaluate("//dc:subjects[@type='scalarList']",
                document, XPathConstants.NODE);
        assertNotNull(fieldNode);

        // Check file schema
        schemaNode = (Node) xpath.evaluate("//schema[@name='file']", document,
                XPathConstants.NODE);
        assertNotNull(schemaNode);

        fieldNode = (Node) xpath.evaluate("//file:filename[@type='string']",
                document, XPathConstants.NODE);
        assertNotNull(fieldNode);

        fieldNode = (Node) xpath.evaluate("//file:content[@type='content']",
                document, XPathConstants.NODE);
        assertNotNull(fieldNode);

    }

    /**
     * Transforms a dom4j document to a w3c Document.
     * 
     * @param dom4jdoc the org.dom4j.Document document
     * @return the org.w3c.dom.Document document
     * @throws TransformerException the transformer exception
     */
    protected final Document dom4jToW3c(org.dom4j.Document dom4jdoc)
            throws TransformerException {

        SAXSource source = new DocumentSource(dom4jdoc);
        DOMResult result = new DOMResult();

        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(source, result);

        return (Document) result.getNode();
    }

    /**
     * The NamespaceContext for Nuxeo basic core schemas.
     */
    @SuppressWarnings("rawtypes")
    protected final class CoreNamespaceContext implements NamespaceContext {

        public String getNamespaceURI(String prefix) {

            if ("dc".equals(prefix)) {
                return "http://www.nuxeo.org/ecm/schemas/dublincore/";
            } else if ("file".equals(prefix)) {
                return "http://www.nuxeo.org/ecm/schemas/file/";
            } else {
                return XMLConstants.NULL_NS_URI;
            }
        }

        // Unused => dummy
        public String getPrefix(String namespace) {
            return null;
        }

        // Unused => dummy
        public Iterator getPrefixes(String namespace) {
            return null;
        }
    }

}
