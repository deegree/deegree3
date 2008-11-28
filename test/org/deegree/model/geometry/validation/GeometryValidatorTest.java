//$HeadURL$
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
package org.deegree.model.geometry.validation;

import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import junit.framework.Assert;

import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.GeometryFactoryCreator;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Ring;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.surfacepatches.PolygonPatch;
import org.deegree.model.geometry.validation.GeometryValidator;
import org.deegree.model.gml.GML311GeometryParser;
import org.deegree.model.gml.GML311GeometryParserTest;
import org.junit.Test;

/**
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GeometryValidatorTest implements ValidationEventHandler {

    private static GeometryFactory geomFac = GeometryFactoryCreator.getInstance().getGeometryFactory( "Standard" );

    private static final String BASE_DIR = "testdata/geometries/";

    @Test
    public void validateCurve()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        GeometryValidator validator = new GeometryValidator(this);
        Geometry geom = parseGeometry( "Curve.gml" );
        Assert.assertTrue( validator.validateGeometry( geom ) );
    }

    @Test
    public void validateInvalidCurve()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        GeometryValidator validator = new GeometryValidator(this);
        Geometry geom = parseGeometry( "invalid/Curve.gml" );
        Assert.assertFalse( "Discontinuity between curve segments not detected.", validator.validateGeometry( geom ) );
    }

    @Test
    public void validateRing()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        GeometryValidator validator = new GeometryValidator(this);
        Geometry geom = parseGeometry( "Ring.gml" );        
        Assert.assertTrue( "Geometry is valid.", validator.validateGeometry( geom ) );
    }    

    @Test
    public void validateInvalidRing()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        GeometryValidator validator = new GeometryValidator(this);
        Geometry geom = parseGeometry( "invalid/Ring_not_closed.gml" );        
        Assert.assertFalse( "Geometry must be recognized as invalid.", validator.validateGeometry( geom ) );
    }    
    
    @Test
    public void validatePolygon()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        GeometryValidator validator = new GeometryValidator(this);
        Geometry geom = parseGeometry( "Polygon.gml" );
        Assert.assertTrue( validator.validateGeometry( geom ) );
    }

    @Test
    public void validateInvalidPolygon()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        GeometryValidator validator = new GeometryValidator(this);
        Geometry geom = parseGeometry( "invalid/Polygon_exterior_not_closed.gml" );
        Assert.assertFalse( validator.validateGeometry( geom ) );
    }    

    @Test
    public void validateInvalidPolygon2()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        GeometryValidator validator = new GeometryValidator(this);
        Geometry geom = parseGeometry( "invalid/Polygon_interior_outside_exterior.gml" );
        Assert.assertFalse( validator.validateGeometry( geom ) );
    }
    
    @Test
    public void validateInvalidPolygon3()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        GeometryValidator validator = new GeometryValidator(this);
        Geometry geom = parseGeometry( "invalid/Polygon_interiors_touch.gml" );
        Assert.assertFalse( validator.validateGeometry( geom ) );
    }     
    
    private Geometry parseGeometry( String fileName )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryParserTest.class.getResource( BASE_DIR
                                                                                                                   + fileName ) );
        xmlReader.nextTag();
        return new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
    }

    
    
    @Override
    public boolean curveDiscontinuity( Curve curve, int segmentIdx ) {
        System.out.println ("Curve discontinuity between segment '" + segmentIdx + "' and " + (segmentIdx + 1) + ".");
        return false;
    }

    @Override
    public boolean curvePointDuplication( Curve curve, Point point ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean curveSelfIntersection( Curve curve, Point location ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean exteriorRingCW( Surface surface, PolygonPatch patch ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean interiorRingCCW( Surface surface, PolygonPatch patch ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean interiorRingIntersectsExterior( Surface surface, PolygonPatch patch, int ringIdx ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean interiorRingOutsideExterior( Surface surface, PolygonPatch patch, int ringIdx ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean interiorRingTouchesExterior( Surface surface, PolygonPatch patch, int ringIdx ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean interiorRingsIntersect( Surface surface, PolygonPatch patch, int ring1Idx, int ring2Idx ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean interiorRingsTouch( Surface surface, PolygonPatch patch, int ring1Idx, int ring2Idx ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean interiorRingsWithin( Surface surface, PolygonPatch patch, int ring1Idx, int ring2Idx ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean ringNotClosed( Ring ring ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean ringSelfIntersection( Ring ring, Point location ) {
        // TODO Auto-generated method stub
        return false;
    }
}
