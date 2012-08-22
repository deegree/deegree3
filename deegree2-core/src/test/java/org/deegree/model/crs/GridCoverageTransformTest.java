//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/model/crs/GridCoverageTransformTest.java $
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
package org.deegree.model.crs;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.media.jai.Interpolation;

import junit.framework.TestCase;

import org.deegree.model.coverage.grid.AbstractGridCoverage;
import org.deegree.model.coverage.grid.ImageGridCoverage;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;

/**
 *
 *
 *
 * @version $Revision: 18195 $
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: mschneider $
 *
 * @version 1.0. $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 *
 */
public class GridCoverageTransformTest extends TestCase {

    /**
     * JUnitTest to test the transformation from 4326 to 31467.
     * @throws CRSTransformationException if the tranform run into problems
     * @throws UnknownCRSException if the 4326 or 31468 are not found.
     */
    public void testTransformGC1()
                            throws CRSTransformationException, UnknownCRSException {

        BufferedImage img = new BufferedImage( 1000, 1000, BufferedImage.TYPE_INT_ARGB );
        Graphics g = img.getGraphics();
        g.setColor( Color.BLACK );
        g.drawLine( 0, 500, 1000, 500 );
        g.drawLine( 200, 0, 200, 1000 );
        g.dispose();

        // ImageUtils.saveImage( img, "e:/temp/test1.tif", 1 );

        CoordinateSystem crs = CRSFactory.create( "EPSG:4326" );
        Envelope bbox = GeometryFactory.createEnvelope( 6, 48, 12, 54, crs );

        AbstractGridCoverage gc = new ImageGridCoverage( null, bbox, crs, false, img );

        GeoTransformer gt = new GeoTransformer( "EPSG:31467" );
        gc = (AbstractGridCoverage) gt.transform( gc, 4, 2, (Interpolation) null );

        // ImageUtils.saveImage( gc.getAsImage( -1, -1 ), "e:/temp/test.tif", 1 );

    }

}
