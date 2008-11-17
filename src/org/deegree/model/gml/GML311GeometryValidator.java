//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/feature/Feature.java $
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.model.gml;

import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.GeometryFactoryCreator;
import org.deegree.model.geometry.composite.CompositeGeometry;
import org.deegree.model.geometry.multi.MultiGeometry;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.GeometricPrimitive;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.curvesegments.CurveSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Takes an XML stream as input (which typically contains a GML feature or feature collection document) and validates
 * all contained <code>gml:_Geometry</code> elements.
 * <p>
 * Validation continues if errors are encountered.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GML311GeometryValidator extends XMLAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( GML311GeometryValidator.class );

    private GML311GeometryParser geomParser;

    private XMLStreamReaderWrapper xmlStream;

    public GML311GeometryValidator( XMLStreamReaderWrapper xmlStream ) {
        geomParser = new GML311GeometryParser( GeometryFactoryCreator.getInstance().getGeometryFactory(), xmlStream );
        this.xmlStream = xmlStream;
    }

    public void validateGeometries()
                            throws XMLStreamException {

        while ( xmlStream.next() != END_DOCUMENT ) {
            if ( xmlStream.getEventType() == START_ELEMENT ) {
                QName elName = xmlStream.getName();
                if ( geomParser.isGeometry( elName ) ) {
                    validateGeometryElement();
                }
            }
        }
    }

    private void validateGeometryElement() {
        Location location = xmlStream.getLocation();
        System.out.println( "Validating GML geometry element ('" + xmlStream.getLocalName() + "') at line: "
                            + location.getLineNumber() + ", column: " + location.getColumnNumber() + "." );
        try {
            validateGeometry(  geomParser.parseGeometry( null ) );
        } catch ( Exception e ) {
            System.out.println( "Parsing of GML geometry element ('" + xmlStream.getLocalName() + "') at line: "
                                + location.getLineNumber() + ", column: " + location.getColumnNumber() + " failed: "
                                + e.getMessage() + "." );
        }
    }

    private void validateGeometry( Geometry geom ) {
        switch ( geom.getGeometryType() ) {
        case COMPOSITE_GEOMETRY: {
            validate( (CompositeGeometry<?>) geom );
            break;
        }
        case COMPOSITE_PRIMITIVE: {
            validate( (CompositeGeometry<?>) geom );
            break;
        }
        case ENVELOPE: {
            String msg = "Internal error: envelope 'geometries' should not occur here.";
            throw new IllegalArgumentException(msg);
        }
        case MULTI_GEOMETRY: {
            validate( (MultiGeometry<?>) geom );            
            break;
        }
        case PRIMITIVE_GEOMETRY: {
            validate( (GeometricPrimitive) geom );
            break;
        }
        }
    }

    private void validate( GeometricPrimitive geom ) {
        switch (geom.getPrimitiveType()) {
        case Point: {
            System.out.println ("Point geometry. No validation necessary.");
            break;
        }
        case Curve: {
            System.out.println ("Curve geometry. Validating segment continuity.");
            Curve curve = (Curve) geom;
            Point lastSegmentEndPoint = null;
            for (CurveSegment segment : curve.getCurveSegments()) {
                if (lastSegmentEndPoint != null) {
                    Point startPoint = segment.getStartPoint();
                    System.out.println (startPoint);
                }
            }
            break;
        }
        case Surface: {
            System.out.println ("Surface geometry. Validating using JTS.");
            break;
        }
        case Solid: {
            String msg = "Validation of solids is not available";
            throw new IllegalArgumentException(msg);
        }
        }
    }

    private void validate( CompositeGeometry<?> geom ) {
        for ( GeometricPrimitive geometricPrimitive : geom ) {
            validate( geometricPrimitive );
        }
    }

    private void validate( MultiGeometry<?> geom ) {
        for ( Geometry member : geom ) {
            validateGeometry( member );
        }
    }
}
