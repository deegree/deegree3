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
package org.deegree.filter.xml;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.stax.StAXParsingHelper.getRequiredAttributeValue;
import static org.deegree.commons.xml.stax.StAXParsingHelper.require;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.uom.Measure;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.i18n.Messages;
import org.deegree.filter.spatial.BBOX;
import org.deegree.filter.spatial.Beyond;
import org.deegree.filter.spatial.Contains;
import org.deegree.filter.spatial.Crosses;
import org.deegree.filter.spatial.DWithin;
import org.deegree.filter.spatial.Disjoint;
import org.deegree.filter.spatial.Equals;
import org.deegree.filter.spatial.Intersects;
import org.deegree.filter.spatial.Overlaps;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.filter.spatial.Touches;
import org.deegree.filter.spatial.Within;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.gml.GML21GeometryDecoder;
import org.deegree.geometry.gml.GML311GeometryDecoder;

/**
 * The <code>Filter100XMLDecoder</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class Filter100XMLDecoder extends Filter110XMLDecoder {

    @Override
    public SpatialOperator parseSpatialOperator( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        return parseSpatialOperatorCore( xmlStream );
    }

    private static SpatialOperator parseSpatialOperatorCore( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        SpatialOperator spatialOperator = null;

        require( xmlStream, START_ELEMENT );
        // check if element name is a valid spatial operator element name
        SpatialOperator.SubType type = elementNameToSpatialOperatorType.get( xmlStream.getName() );
        if ( type == null ) {
            String msg = Messages.getMessage( "FILTER_PARSER_UNEXPECTED_ELEMENT", xmlStream.getName(),
                                              elemNames( SpatialOperator.SubType.class,
                                                         spatialOperatorTypeToElementName ) );
            throw new XMLParsingException( xmlStream, msg );
        }

        xmlStream.nextTag();

        // TODO remove this after GML parser is adapted
        XMLStreamReaderWrapper wrapper = new XMLStreamReaderWrapper( xmlStream, null );
        GML311GeometryDecoder geomParser = new GML21GeometryDecoder();

        // always first parameter: 'ogc:PropertyName'
        PropertyName param1 = parsePropertyName( xmlStream );
        xmlStream.nextTag();

        try {
            switch ( type ) {
            case BBOX: {
                // second parameter: 'gml:Envelope'
                xmlStream.require( START_ELEMENT, GML_NS, "Box" );
                Envelope param2 = geomParser.parseEnvelope( wrapper );
                spatialOperator = new BBOX( param1, param2 );
                break;
            }
            case BEYOND: {
                // second parameter: 'gml:_Geometry'
                Geometry param2 = geomParser.parse( wrapper );
                // third parameter: 'ogc:Distance'
                xmlStream.nextTag();
                xmlStream.require( START_ELEMENT, OGC_NS, "Distance" );
                String distanceUnits = getRequiredAttributeValue( xmlStream, "units" );
                xmlStream.nextTag();
                Measure distance = new Measure( distanceUnits, null );
                spatialOperator = new Beyond( param1, param2, distance );
                break;
            }
            case INTERSECTS: {
                // second parameter: 'gml:_Geometry' or 'gml:Envelope'
                Geometry param2 = geomParser.parseGeometryOrEnvelope( wrapper );
                spatialOperator = new Intersects( param1, param2 );
                break;
            }
            case CONTAINS: {
                // second parameter: 'gml:_Geometry' or 'gml:Envelope'
                Geometry param2 = geomParser.parseGeometryOrEnvelope( wrapper );
                spatialOperator = new Contains( param1, param2 );
                break;
            }
            case CROSSES: {
                // second parameter: 'gml:_Geometry' or 'gml:Envelope'
                Geometry param2 = geomParser.parseGeometryOrEnvelope( wrapper );
                spatialOperator = new Crosses( param1, param2 );
                break;
            }
            case DISJOINT: {
                // second parameter: 'gml:_Geometry'
                Geometry param2 = geomParser.parse( wrapper );
                spatialOperator = new Disjoint( param1, param2 );
                break;
            }
            case DWITHIN: {
                // second parameter: 'gml:_Geometry'
                Geometry param2 = geomParser.parse( wrapper );
                // third parameter: 'ogc:Distance'
                xmlStream.nextTag();
                xmlStream.require( START_ELEMENT, OGC_NS, "Distance" );
                String distanceUnits = getRequiredAttributeValue( xmlStream, "units" );
                Measure distance = new Measure( distanceUnits, null );
                spatialOperator = new DWithin( param1, param2, distance );
                xmlStream.nextTag();
                break;
            }
            case EQUALS: {
                // second parameter: 'gml:_Geometry'
                Geometry param2 = geomParser.parse( wrapper, null );
                spatialOperator = new Equals( param1, param2 );
                break;
            }
            case OVERLAPS: {
                // second parameter: 'gml:_Geometry' or 'gml:Envelope'
                Geometry param2 = geomParser.parseGeometryOrEnvelope( wrapper );
                spatialOperator = new Overlaps( param1, param2 );
                break;
            }
            case TOUCHES: {
                // second parameter: 'gml:_Geometry' or 'gml:Envelope'
                Geometry param2 = geomParser.parseGeometryOrEnvelope( wrapper );
                spatialOperator = new Touches( param1, param2 );
                break;
            }
            case WITHIN: {
                // second parameter: 'gml:_Geometry' or 'gml:Envelope'
                Geometry param2 = geomParser.parseGeometryOrEnvelope( wrapper );
                spatialOperator = new Within( param1, param2 );
            }
            }
        } catch ( UnknownCRSException e ) {
            throw new XMLParsingException( xmlStream, e.getMessage() );
        }
        xmlStream.nextTag();
        return spatialOperator;
    }

}
