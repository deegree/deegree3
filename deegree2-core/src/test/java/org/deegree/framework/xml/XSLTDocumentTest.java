//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/framework/xml/XSLTDocumentTest.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
----------------------------------------------------------------------------*/

package org.deegree.framework.xml;

import java.net.URL;

import javax.xml.transform.TransformerException;

import junit.framework.TestCase;
import alltests.Configuration;

public class XSLTDocumentTest extends TestCase {

    private static final String XSL_FILE = "csw/example/deegree/dublincore/xslt/dc_getrecords_out.xsl";

    private static final String XML_FILE = "input.xml";

    private XMLFragment input;

    private XSLTDocument sheet;

    protected void setUp()
                            throws Exception {
        super.setUp();
        URL inputURL = this.getClass().getResource( XML_FILE );
        input = new XSLTDocument();
        input.load( inputURL );
        sheet = new XSLTDocument();
        URL sheetURL = new URL( Configuration.getResourceDir(), XSL_FILE );
        sheet.load( sheetURL );
    }

    protected void tearDown()
                            throws Exception {
        super.tearDown();
    }

    public void testXSLTWithJavaExtension()
                            throws TransformerException {
        sheet.transform( input );
    }

}

/***************************************************************************************************
 * <code>
 Changes to this class. What the people have been up to:

 $Log$
 Revision 1.3  2007/02/12 09:43:35  wanhoff
 added footer, corrected header

 </code>
 **************************************************************************************************/

