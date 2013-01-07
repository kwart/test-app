package org.jboss.test;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * A AppTest.
 * 
 * @author Josef Cacek
 */
public class AppTest {

    @Test
    public void test() throws Exception {
        System.out.println("Check target/war-deployers-jboss-beans.xml if it contains the SECURITY_DOMAIN authenticator");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(new FileInputStream("target/war-deployers-jboss-beans.xml"));

        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new TestNamespaceContext());
        XPathExpression expr = xPath
                .compile("//bd:property[@name='authenticators']/bd:map/bd:entry[bd:key/text()='SECURITY_DOMAIN']");
        Object result = expr.evaluate(document, XPathConstants.NODESET);

        NodeList nodes = (NodeList) result;
        assertEquals(1, nodes.getLength());
    }

    static private class TestNamespaceContext implements NamespaceContext {

        public String getPrefix(String namespaceURI) {
            return null;
        }

        public Iterator<String> getPrefixes(String namespaceURI) {
            return null;
        }

        public String getNamespaceURI(String prefix) {
            if (prefix == null)
                throw new NullPointerException("Invalid Namespace Prefix");
            else if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX))
                return "urn:jboss:bean-deployer:2.0";
            else if (prefix.equals("bd"))
                return "urn:jboss:bean-deployer:2.0";
            else
                return XMLConstants.NULL_NS_URI;
        }

    }

}
