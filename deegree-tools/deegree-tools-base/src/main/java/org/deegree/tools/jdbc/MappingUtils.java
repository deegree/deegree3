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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import org.deegree.commons.utils.time.DateUtils;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryException;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.deegree.geometry.standard.primitive.DefaultPoint;
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

    private static final NumberFormat nf = new DecimalFormat( "#.##", dfs );

    private static final com.vividsolutions.jts.io.WKTReader jtsReader = new com.vividsolutions.jts.io.WKTReader();

    private static final AbstractDefaultGeometry defGeom = new DefaultPoint( null, null, null,
                                                                             new double[] { 0.0, 0.0 } );

    /**
     * 
     * @param node
     * @return maximum x coordinate
     * @throws ParseException
     * @throws GeometryException
     */
    public static String getXMax( Node node )
                            throws ParseException {
        if ( node == null ) {
            return "";
        }
        Envelope envelope = getAsEnvelope( node );
        if ( envelope == null )
            return "";
        return nf.format( envelope.getMax().get( 0 ) );
    }

    /**
     * 
     * @param node
     * @return minimum x coordinate
     * @throws ParseException
     * @throws GeometryException
     */
    public static String getXMin( Node node )
                            throws ParseException {
        if ( node == null ) {
            return "";
        }
        Envelope envelope = getAsEnvelope( node );
        if ( envelope == null )
            return "";
        return nf.format( envelope.getMin().get0() );
    }

    private static Envelope getAsEnvelope( Node node )
                            throws ParseException {
        String textContent = getStringValue( node );
        com.vividsolutions.jts.geom.Geometry read = jtsReader.read( textContent );
        AbstractDefaultGeometry createFromJTS = defGeom.createFromJTS( read, null );
        return createFromJTS.getEnvelope();
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
        Envelope envelope = getAsEnvelope( node );
        if ( envelope == null )
            return "";
        return nf.format( envelope.getMax().get1() );
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
        Envelope envelope = getAsEnvelope( node );
        if ( envelope == null )
            return "";
        return nf.format( envelope.getMin().get1() );
    }

    /**
     * 
     * @param date
     *            ISO 8601 formated date
     * @param offset
     *            days; could be a positive or a negative value
     * @return date plus/minus offset
     * @throws java.text.ParseException
     */
    public static String addDateOffeset( Node date, int offset )
                            throws java.text.ParseException {
        long off = ( (long) offset ) * 24 * 60 * 60 * 1000;
        long t = createDate( getStringValue( date ) ).getTime();
        return DateUtils.formatISO8601DateWOMS( new Date( t + off ) );
    }

    /**
     * Returns the text contained in the specified element.
     * 
     * @param node
     *            current element
     * @return the textual contents of the element
     */
    private static String getStringValue( Node node ) {
        NodeList children = node.getChildNodes();
        StringBuffer sb = new StringBuffer( children.getLength() * 500 );
        if ( node.getNodeValue() != null ) {
            sb.append( node.getNodeValue().trim() );
        }
        if ( node.getNodeType() != Node.ATTRIBUTE_NODE ) {
            for ( int i = 0; i < children.getLength(); i++ ) {
                if ( children.item( i ).getNodeType() == Node.TEXT_NODE
                     || children.item( i ).getNodeType() == Node.CDATA_SECTION_NODE ) {
                    sb.append( children.item( i ).getNodeValue() );
                }
            }
        }
        return sb.toString();
    }

    public static Date createDate( String isoDate )
                            throws java.text.ParseException {
        return DateUtils.parseISO8601Date( isoDate );
    }

}
