package org.deegree.commons.xml.xpath;

import org.apache.axiom.om.xpath.AXIOMXPath;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

public class XPathTest {

    /**
     * @param args
     * @throws JaxenException 
     */
    public static void main( String[] args ) throws JaxenException {
        
        XPath xpath = new AXIOMXPath("app:name");
        xpath.addNamespace("app", "http://www.deegree.org/app");

        System.out.println ("xpath: '" + xpath + "'");

    }

}
