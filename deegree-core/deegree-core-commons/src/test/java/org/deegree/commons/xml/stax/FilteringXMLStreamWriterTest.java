//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.commons.xml.stax;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class FilteringXMLStreamWriterTest {

    private final static String app = "http://www.deegree.org/app";

    private final static String nix = "http://www.deegree.org/nix";

    private final static String alles = "http://www.deegree.org/alles";

    private static final NamespaceBindings nsBindings = new NamespaceBindings();
    static {
        nsBindings.addNamespace( "app", app );
        nsBindings.addNamespace( "nix", nix );
        nsBindings.addNamespace( "alles", alles );
    }

    private void writeDocument( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartDocument();
        writer.setPrefix( "app", app );
        writer.setPrefix( "nix", nix );
        writer.setPrefix( "alles", alles );
        writer.writeStartElement( app, "a" );
        writer.writeNamespace( "app", app );
        writer.writeNamespace( "nix", nix );
        writer.writeNamespace( "alles", alles );
        writer.writeStartElement( app, "b" );
        writer.writeStartElement( nix, "c" );
        writer.writeStartElement( app, "d" );
        writer.writeEndElement();
        writer.writeStartElement( alles, "e" );
        writer.writeCharacters( "sometext" );
        writer.writeEndElement();
        writer.writeStartElement( app, "b" );
        writer.writeStartElement( nix, "c" );
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.close();
    }

    private XMLStreamWriter getWriter( List<String> paths, OutputStream stream )
                            throws Exception {
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter( stream );
        writer = new IndentingXMLStreamWriter( writer );
        List<XPath> xpaths = new ArrayList<XPath>();
        for ( String s : paths ) {
            xpaths.add( new XPath( s, nsBindings ) );
        }
        writer = new FilteringXMLStreamWriter( writer, xpaths );
        return writer;
    }

    @Test
    public void testFilteringOneXPath()
                            throws Exception {
        List<String> list = new ArrayList<String>();
        list.add( "/app:a/app:b/nix:c/app:b" );
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLStreamWriter writer = getWriter( list, bos );
        writeDocument( writer );
        byte[] actual = bos.toByteArray();
        byte[] expected = IOUtils.toByteArray( FilteringXMLStreamWriterTest.class.getResourceAsStream( "filteringxpathone.xml" ) );
        Assert.assertArrayEquals( expected, actual );
    }

    @Test
    public void testFilteringMultipleXPaths()
                            throws Exception {
        List<String> list = new ArrayList<String>();
        list.add( "/app:a/app:b/nix:c/app:d" );
        list.add( "/app:a/app:b/nix:c/app:b" );
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLStreamWriter writer = getWriter( list, bos );
        writeDocument( writer );
        byte[] actual = bos.toByteArray();
        byte[] expected = IOUtils.toByteArray( FilteringXMLStreamWriterTest.class.getResourceAsStream( "filteringxpathmultiple.xml" ) );
        Assert.assertArrayEquals( expected, actual );
    }

    @Test
    public void testFilteringMultipleXPathsWithText()
                            throws Exception {
        List<String> list = new ArrayList<String>();
        list.add( "/app:a/app:b/nix:c/alles:e" );
        list.add( "/app:a/app:b/nix:c/app:b" );
        list.add( "/app:a/app:b/nix:c/falsch:d" );
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLStreamWriter writer = getWriter( list, bos );
        writeDocument( writer );
        byte[] actual = bos.toByteArray();
        byte[] expected = IOUtils.toByteArray( FilteringXMLStreamWriterTest.class.getResourceAsStream( "filteringxpathmultiplewithtext.xml" ) );
        Assert.assertArrayEquals( expected, actual );
    }

    @Test(expected = XMLStreamException.class)
    public void testFilteringOneXPathWithoutMatchingRootElement()
                            throws Exception {
        List<String> list = new ArrayList<String>();
        list.add( "/ap:a/app:c" );
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLStreamWriter writer = getWriter( list, bos );
        writeDocument( writer );
        byte[] actual = bos.toByteArray();
        Assert.assertEquals( 0, actual.length );
    }

    @Test
    public void testFilteringXPathSetPrefixBug()
                            throws Exception {
        final XMLAdapter input = new XMLAdapter( FilteringXMLStreamWriterTest.class.getResourceAsStream( "filtering_xpath_set_prefix.xml" ) );
        final List<String> list = new ArrayList<String>();
        list.add( "/app:a/nix:d" );
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final XMLStreamWriter writer = getWriter( list, bos );
        input.getRootElement().serialize( writer );
        writer.close();
        byte[] actual = bos.toByteArray();
        byte[] expected = IOUtils.toByteArray( FilteringXMLStreamWriterTest.class.getResourceAsStream( "filtering_xpath_set_prefix_expected.xml" ) );
        Assert.assertArrayEquals( expected, actual );
    }
}
