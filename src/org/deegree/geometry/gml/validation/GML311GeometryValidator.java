//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/feature/Feature.java $
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
package org.deegree.geometry.gml.validation;

import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.gml.GMLIdContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.gml.GML311GeometryDecoder;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.validation.GeometryValidationEventHandler;
import org.deegree.geometry.validation.GeometryValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes an XML stream as input (which should provide a GML geometry, GML feature or feature collection document) and
 * validates all contained <code>gml:_Geometry</code> elements (at all levels of the document).
 * <p>
 * The validator's reaction on topological issues can be customized by providing a {@link GMLValidationEventHandler}
 * which is also suitable for generating validation reports.
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class GML311GeometryValidator extends XMLAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( GML311GeometryValidator.class );

    private GML311GeometryDecoder geomParser;

    private XMLStreamReaderWrapper xmlStream;

    private GMLValidationEventHandler gmlErrorHandler;

    /**
     * @param xmlStream
     * @param gmlErrorHandler
     */
    public GML311GeometryValidator( XMLStreamReaderWrapper xmlStream, GMLValidationEventHandler gmlErrorHandler ) {
        this.xmlStream = xmlStream;
        geomParser = new GML311GeometryDecoder( new GeometryFactory(), new GMLIdContext() );
        this.gmlErrorHandler = gmlErrorHandler;
    }

    /**
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public void validateGeometries()
                            throws XMLStreamException, UnknownCRSException {

        while ( xmlStream.next() != END_DOCUMENT ) {
            if ( xmlStream.getEventType() == START_ELEMENT ) {
                QName elName = xmlStream.getName();
                if ( geomParser.isGeometryElement( elName ) ) {
                    validateGeometryElement();
                }
            }
        }
    }

    private void validateGeometryElement() throws UnknownCRSException {
        Location location = xmlStream.getLocation();
        LOG.debug( "Validating GML geometry element ('" + xmlStream.getLocalName() + "') at line: "
                   + location.getLineNumber() + ", column: " + location.getColumnNumber() + "." );

        GMLElementIdentifier identifier = new GMLElementIdentifier( xmlStream );
        ValidationEventRedirector eventRedirector = new ValidationEventRedirector( gmlErrorHandler, identifier );
        GeometryValidator geometryValidator = new GeometryValidator( eventRedirector );
        try {
            geometryValidator.validateGeometry( geomParser.parseAbstractGeometry(xmlStream, new CRS("EPSG:28992") ) );
        } catch ( XMLParsingException e ) {
            gmlErrorHandler.geometryParsingError( identifier, e );
        } catch ( XMLStreamException e ) {
            gmlErrorHandler.geometryParsingError( identifier, e );
        }
    }

    private class ValidationEventRedirector implements GeometryValidationEventHandler {

        private GMLValidationEventHandler gmlErrorHandler;

        private GMLElementIdentifier topLevelGeometryElement;

        public ValidationEventRedirector( GMLValidationEventHandler gmlErrorHandler,
                                          GMLElementIdentifier topLevelGeometryElement ) {
            this.gmlErrorHandler = gmlErrorHandler;
            this.topLevelGeometryElement = topLevelGeometryElement;
        }

        @Override
        public boolean curveDiscontinuity( Curve curve, int segmentIdx, List<Object> affectedGeometryParticles ) {
            gmlErrorHandler.curveDiscontinuity( curve, segmentIdx, affectedGeometryParticles, getAffectedElements() );
            return false;
        }

        @Override
        public boolean curvePointDuplication( Curve curve, Point point, List<Object> affectedGeometryParticles ) {
            gmlErrorHandler.curvePointDuplication( curve, point, affectedGeometryParticles, getAffectedElements() );
            return false;
        }

        @Override
        public boolean curveSelfIntersection( Curve curve, Point location, List<Object> affectedGeometryParticles ) {
            gmlErrorHandler.curveSelfIntersection( curve, location, affectedGeometryParticles, getAffectedElements() );
            return false;
        }

        @Override
        public boolean exteriorRingCW( PolygonPatch patch, List<Object> affectedGeometryParticles ) {
            gmlErrorHandler.exteriorRingCW( patch, affectedGeometryParticles, getAffectedElements() );
            return false;
        }

        @Override
        public boolean interiorRingCCW( PolygonPatch patch, List<Object> affectedGeometryParticles ) {
            gmlErrorHandler.interiorRingCCW( patch, affectedGeometryParticles, getAffectedElements() );
            return false;
        }

        @Override
        public boolean interiorRingIntersectsExterior( PolygonPatch patch, int ringIdx,
                                                       List<Object> affectedGeometryParticles ) {
            gmlErrorHandler.interiorRingIntersectsExterior( patch, ringIdx, affectedGeometryParticles,
                                                            getAffectedElements() );
            return false;
        }

        @Override
        public boolean interiorRingOutsideExterior( PolygonPatch patch, int ringIdx,
                                                    List<Object> affectedGeometryParticles ) {
            gmlErrorHandler.interiorRingOutsideExterior( patch, ringIdx, affectedGeometryParticles,
                                                         getAffectedElements() );
            return false;
        }

        @Override
        public boolean interiorRingTouchesExterior( PolygonPatch patch, int ringIdx,
                                                    List<Object> affectedGeometryParticles ) {
            gmlErrorHandler.interiorRingTouchesExterior( patch, ringIdx, affectedGeometryParticles,
                                                         getAffectedElements() );
            return false;
        }

        @Override
        public boolean interiorRingsIntersect( PolygonPatch patch, int ring1Idx, int ring2Idx,
                                               List<Object> affectedGeometryParticles ) {
            gmlErrorHandler.interiorRingsIntersect( patch, ring1Idx, ring2Idx, affectedGeometryParticles,
                                                    getAffectedElements() );
            return false;
        }

        @Override
        public boolean interiorRingsTouch( PolygonPatch patch, int ring1Idx, int ring2Idx,
                                           List<Object> affectedGeometryParticles ) {
            gmlErrorHandler.interiorRingsTouch( patch, ring1Idx, ring2Idx, affectedGeometryParticles,
                                                getAffectedElements() );
            return false;
        }

        @Override
        public boolean interiorRingsWithin( PolygonPatch patch, int ring1Idx, int ring2Idx,
                                            List<Object> affectedGeometryParticles ) {
            gmlErrorHandler.interiorRingsWithin( patch, ring1Idx, ring2Idx, affectedGeometryParticles,
                                                 getAffectedElements() );
            return false;
        }

        @Override
        public boolean ringNotClosed( Ring ring, List<Object> affectedGeometryParticles ) {
            gmlErrorHandler.ringNotClosed( ring, affectedGeometryParticles, getAffectedElements() );
            return false;
        }

        @Override
        public boolean ringSelfIntersection( Ring ring, Point location, List<Object> affectedGeometryParticles ) {
            gmlErrorHandler.ringSelfIntersection( ring, location, affectedGeometryParticles, getAffectedElements() );
            return false;
        }

        private List<GMLElementIdentifier> getAffectedElements() {
            return Collections.singletonList( topLevelGeometryElement );
        }
    }
}
