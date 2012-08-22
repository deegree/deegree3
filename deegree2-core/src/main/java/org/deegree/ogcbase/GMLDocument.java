// $HeadURL$
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
package org.deegree.ogcbase;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

import org.deegree.datatypes.time.TimeIndeterminateValue;
import org.deegree.datatypes.time.TimePosition;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.coverage.grid.Grid;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Position;
import org.w3c.dom.Element;

/**
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 1.1
 */
public class GMLDocument extends XMLFragment {

    private static final long serialVersionUID = -4974669697699282588L;

    private static URI GMLNS = CommonNamespaces.GMLNS;

    /**
     * creates a <tt>Point</tt> from the passed <pos> element containing a GML pos.
     * 
     * @param element
     * @return created <tt>Point</tt>
     * @throws InvalidGMLException
     */
    public static Point parsePos( Element element )
                            throws InvalidGMLException {
        String tmp = XMLTools.getAttrValue( element, null, "dimension", null );
        int dim = 0;
        if ( tmp != null ) {
            dim = Integer.parseInt( tmp );
        }
        tmp = XMLTools.getStringValue( element );
        double[] vals = StringTools.toArrayDouble( tmp, ", " );
        if ( dim != 0 ) {
            if ( vals.length != dim ) {
                throw new InvalidGMLException( "dimension must be equal to the number of "
                                               + "coordinate values defined in pos element." );
            }
        } else {
            dim = vals.length;
        }

        Position pos = null;
        if ( dim == 3 ) {
            pos = GeometryFactory.createPosition( vals[0], vals[1], vals[2] );
        } else {
            pos = GeometryFactory.createPosition( vals[0], vals[1] );
        }

        return GeometryFactory.createPoint( pos, null );
    }

    /**
     * creates a <tt>Envelope</tt> from the passed element. Because deegree geometry implementation doesn't use CRS for
     * envelopes the srsName attribute of the passed element is ignored.
     * 
     * @param element
     * @return created <tt>Envelope</tt>
     * @throws InvalidGMLException
     * @throws UnknownCRSException
     */
    public static Envelope parseEnvelope( Element element )
                            throws InvalidGMLException, UnknownCRSException {

        String srs = XMLTools.getAttrValue( element, null, "srsName", null );
        CoordinateSystem crs = null;
        if ( srs != null ) {
            crs = CRSFactory.create( srs );
        }

        ElementList el = XMLTools.getChildElements( "pos", GMLNS, element );
        if ( el == null || el.getLength() != 2 ) {
            throw new InvalidGMLException( "A lonLatEnvelope must contain two gml:pos elements" );
        }
        Point min = parsePos( el.item( 0 ) );
        Point max = parsePos( el.item( 1 ) );

        return GeometryFactory.createEnvelope( min.getPosition(), max.getPosition(), crs );
    }

    /**
     * creates a <tt>TimePosition</tt> object from the passed element.
     * 
     * @param element
     * @return created <tt>TimePosition</tt>
     * @throws XMLParsingException
     * @throws InvalidGMLException
     */
    public static TimePosition parseTimePosition( Element element )
                            throws XMLParsingException, InvalidGMLException {
        try {
            String calendarEraName = XMLTools.getAttrValue( element, null, "calendarEraName", null );
            String s = XMLTools.getAttrValue( element, null, "frame", null );
            URI frame = null;
            if ( s != null ) {
                frame = new URI( s );
            }
            String indeterminatePosition = XMLTools.getAttrValue( element, null, "indeterminatePosition", null );
            TimeIndeterminateValue tiv = new TimeIndeterminateValue( indeterminatePosition );
            String tmp = XMLTools.getStringValue( element );
            Calendar cal = null;

            if ( frame != null && !frame.toString().equals( "#ISO-8601" ) ) {
                throw new InvalidGMLException( "just #ISO-8601 is supported as frame for TimePosition." );
            }

            cal = TimeTools.createCalendar( tmp );

            return new TimePosition( tiv, calendarEraName, frame, cal );
        } catch ( URISyntaxException e ) {
            throw new XMLParsingException( "couldn't parse timePosition frame\n" + StringTools.stackTraceToString( e ) );
        }
    }

    /**
     * creates a <tt>Grid</tt> instance from the passed <tt>Element</tt>
     * 
     * @param element
     * @return instance of <tt>Grid</tt>
     * @throws InvalidGMLException
     */
    public static Grid parseGrid( Element element )
                            throws InvalidGMLException {
        Grid grid = null;
        try {
            String path = "gml:limits/gml:GridEnvelope/gml:low";
            String lo = XMLTools.getRequiredNodeAsString( element, path, nsContext );
            double[] low = StringTools.toArrayDouble( lo, " ,;" );
            path = "gml:limits/gml:GridEnvelope/gml:high";
            String hi = XMLTools.getRequiredNodeAsString( element, path, nsContext );
            double[] high = StringTools.toArrayDouble( hi, " ,;" );
            Position posLo = GeometryFactory.createPosition( low );
            Position posHi = GeometryFactory.createPosition( high );
            Envelope env = GeometryFactory.createEnvelope( posLo, posHi, null );
            String[] axis = XMLTools.getNodesAsStrings( element, "axisName/text()", nsContext );
            grid = new Grid( env, axis );
        } catch ( Exception e ) {
            throw new InvalidGMLException( e.getMessage() );
        }
        return grid;
    }

}
