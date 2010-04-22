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
package org.deegree.gml.geometry.validation;

import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.validation.GeometryValidationEventHandler;
import org.deegree.geometry.validation.GeometryValidator;
import org.deegree.gml.GMLDocumentIdContext;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.geometry.GML3GeometryReader;
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
public class GML3GeometryValidator extends XMLAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( GML3GeometryValidator.class );

    private GML3GeometryReader geomParser;

    private XMLStreamReaderWrapper xmlStream;

    private GMLValidationEventHandler gmlErrorHandler;

    /**
     * 
     * @param version
     *            either {@link GMLVersion#GML_30}, {@link GMLVersion#GML_31} or {@link GMLVersion#GML_32}
     * @param xmlStream
     * @param gmlErrorHandler
     * @param defaultCoordDim
     *            defaultValue for coordinate dimension, only used when a posList is parsed and no dimension information
     *            from CRS is available (unknown CRS)
     */
    public GML3GeometryValidator( GMLVersion version, XMLStreamReaderWrapper xmlStream,
                                  GMLValidationEventHandler gmlErrorHandler, int defaultCoordDim ) {
        this.xmlStream = xmlStream;
        geomParser = new GML3GeometryReader( version, new GeometryFactory(),
                                             new GMLDocumentIdContext( GMLVersion.GML_31 ), defaultCoordDim );
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

    private void validateGeometryElement()
                            throws UnknownCRSException {
        Location location = xmlStream.getLocation();
        LOG.debug( "Validating GML geometry element ('" + xmlStream.getLocalName() + "') at line: "
                   + location.getLineNumber() + ", column: " + location.getColumnNumber() + "." );

        GMLElementIdentifier identifier = new GMLElementIdentifier( xmlStream );
        ValidationEventRedirector eventRedirector = new ValidationEventRedirector( gmlErrorHandler, identifier );
        GeometryValidator geometryValidator = new GeometryValidator( eventRedirector );
        try {
            geometryValidator.validateGeometry( geomParser.parse( xmlStream, new CRS( "EPSG:28992" ) ) );
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
            return gmlErrorHandler.curveDiscontinuity( curve, segmentIdx, affectedGeometryParticles,
                                                       getAffectedElements() );
        }

        @Override
        public boolean curvePointDuplication( Curve curve, Point point, List<Object> affectedGeometryParticles ) {
            return gmlErrorHandler.curvePointDuplication( curve, point, affectedGeometryParticles,
                                                          getAffectedElements() );
        }

        @Override
        public boolean curveSelfIntersection( Curve curve, Point location, List<Object> affectedGeometryParticles ) {
            return gmlErrorHandler.curveSelfIntersection( curve, location, affectedGeometryParticles,
                                                          getAffectedElements() );
        }

        @Override
        public boolean exteriorRingCW( PolygonPatch patch, List<Object> affectedGeometryParticles ) {
            return gmlErrorHandler.exteriorRingCW( patch, affectedGeometryParticles, getAffectedElements() );
        }

        @Override
        public boolean interiorRingCCW( PolygonPatch patch, List<Object> affectedGeometryParticles ) {
            return gmlErrorHandler.interiorRingCCW( patch, affectedGeometryParticles, getAffectedElements() );
        }

        @Override
        public boolean interiorRingIntersectsExterior( PolygonPatch patch, int ringIdx,
                                                       List<Object> affectedGeometryParticles ) {
            return gmlErrorHandler.interiorRingIntersectsExterior( patch, ringIdx, affectedGeometryParticles,
                                                                   getAffectedElements() );
        }

        @Override
        public boolean interiorRingOutsideExterior( PolygonPatch patch, int ringIdx,
                                                    List<Object> affectedGeometryParticles ) {
            return gmlErrorHandler.interiorRingOutsideExterior( patch, ringIdx, affectedGeometryParticles,
                                                                getAffectedElements() );
        }

        @Override
        public boolean interiorRingTouchesExterior( PolygonPatch patch, int ringIdx,
                                                    List<Object> affectedGeometryParticles ) {
            return gmlErrorHandler.interiorRingTouchesExterior( patch, ringIdx, affectedGeometryParticles,
                                                                getAffectedElements() );
        }

        @Override
        public boolean interiorRingsIntersect( PolygonPatch patch, int ring1Idx, int ring2Idx,
                                               List<Object> affectedGeometryParticles ) {
            return gmlErrorHandler.interiorRingsIntersect( patch, ring1Idx, ring2Idx, affectedGeometryParticles,
                                                           getAffectedElements() );
        }

        @Override
        public boolean interiorRingsTouch( PolygonPatch patch, int ring1Idx, int ring2Idx,
                                           List<Object> affectedGeometryParticles ) {
            return gmlErrorHandler.interiorRingsTouch( patch, ring1Idx, ring2Idx, affectedGeometryParticles,
                                                       getAffectedElements() );
        }

        @Override
        public boolean interiorRingsWithin( PolygonPatch patch, int ring1Idx, int ring2Idx,
                                            List<Object> affectedGeometryParticles ) {
            return gmlErrorHandler.interiorRingsWithin( patch, ring1Idx, ring2Idx, affectedGeometryParticles,
                                                        getAffectedElements() );
        }

        @Override
        public boolean ringNotClosed( Ring ring, List<Object> affectedGeometryParticles ) {
            return gmlErrorHandler.ringNotClosed( ring, affectedGeometryParticles, getAffectedElements() );
        }

        @Override
        public boolean ringSelfIntersection( Ring ring, Point location, List<Object> affectedGeometryParticles ) {
            return gmlErrorHandler.ringSelfIntersection( ring, location, affectedGeometryParticles,
                                                         getAffectedElements() );
        }

        private List<GMLElementIdentifier> getAffectedElements() {
            return Collections.singletonList( topLevelGeometryElement );
        }
    }
}
