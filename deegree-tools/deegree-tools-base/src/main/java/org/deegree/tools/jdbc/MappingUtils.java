//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.tools.jdbc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.deegree.commons.tom.datetime.ISO8601Converter;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryException;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vividsolutions.jts.io.ParseException;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author: buesching $
 * 
 * @version $Revision: 1.2 $, $Date: 2011-03-07 09:32:57 $
 */
public class MappingUtils {

    private static final DecimalFormatSymbols dfs = new DecimalFormatSymbols( Locale.ENGLISH );

    private static final NumberFormat nf = new DecimalFormat( "#.####", dfs );

    /**
     * 
     * @param node
     * @return maximum x coordinate
     * @throws ParseException
     * @throws GeometryException
     */
    public static String getXMax( Node node ) {
        if ( node == null ) {
            return "";
        }
        return nf.format( getAsEnvelope( node ).getMax().get0() );
    }

    /**
     * 
     * @param node
     * @return minimum x coordinate
     * @throws ParseException
     * @throws GeometryException
     */
    public static String getXMin( Node node ) {
        if ( node == null ) {
            return "";
        }
        return nf.format( getAsEnvelope( node ).getMin().get0() );
    }

    /**
     * 
     * @param node
     * @return maximum y coordinate
     * @throws ParseException
     * @throws GeometryException
     */
    public static String getYMax( Node node )
                            throws ParseException {
        if ( node == null ) {
            return "";
        }
        return nf.format( getAsEnvelope( node ).getMax().get1() );
    }

    /**
     * 
     * @param node
     * @return minimum y coordinate
     * @throws GeometryException
     */
    public static String getYMin( Node node )
                            throws ParseException {
        if ( node == null ) {
            return "";
        }
        return nf.format( getAsEnvelope( node ).getMin().get1() );
    }

    private static Envelope getAsEnvelope( Node node ) {
        try {

            // TODO: this is a really dirty hack!!!!
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
            StringWriter sw = new StringWriter();
            t.transform( new DOMSource( node ), new StreamResult( sw ) );

            String nodeAsString = sw.toString();
            // where is the namespace binding???
            nodeAsString = nodeAsString.replaceFirst( " ", " xmlns:" + CommonNamespaces.GML_PREFIX + "=\""
                                                           + CommonNamespaces.GMLNS + "\" " );
            InputStream in = new ByteArrayInputStream( nodeAsString.getBytes() );
            XMLStreamReader stream = XMLInputFactory.newInstance().createXMLStreamReader( in );
            GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, stream );
            org.deegree.geometry.Geometry geometry = gmlReader.readGeometry();
            return geometry.getEnvelope();
            // Source source = new DOMSource( node );
            // XMLStreamReader stream = XMLInputFactory.newInstance().createXMLStreamReader( source );
            // System.out.println(StAXParsingHelper.getEventTypeString( stream.getEventType() ));
            // if(stream.getEventType() == XMLStreamConstants.START_DOCUMENT)
            // stream.nextTag();
            // System.out.println(StAXParsingHelper.getEventTypeString( stream.getEventType() ));
            // GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_32, stream );
            // org.deegree.geometry.Geometry geometry = gmlReader.readGeometry();
            // return geometry.getEnvelope();
        } catch ( Exception e ) {
            throw new IllegalArgumentException( "could not read as GMLGeometry: " + node.getNodeName(), e );
        }
    }
}
