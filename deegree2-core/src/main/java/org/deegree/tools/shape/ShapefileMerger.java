//$HeadURL$
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

 Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.tools.shape;

import java.io.File;
import java.io.IOException;

import org.deegree.io.dbaseapi.DBaseException;
import org.deegree.io.shpapi.HasNoDBaseFileException;
import org.deegree.io.shpapi.ShapeFile;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;

/**
 * ...
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class ShapefileMerger {

    private FeatureCollection mergedFeatures;

    private File outputFile;

    /**
     *
     * @param args
     * @throws IOException
     * @throws HasNoDBaseFileException
     * @throws DBaseException
     */
    public ShapefileMerger( String[] args ) throws IOException, HasNoDBaseFileException, DBaseException {

        if ( this.mergedFeatures == null ) {
            this.mergedFeatures = FeatureFactory.createFeatureCollection( "dummy", 1000000 );
        }

        for ( int i = 1; i < args.length; i++ ) {

            String s = new File( args[i] ).getAbsolutePath();
            ShapeFile shp = new ShapeFile( s );
            System.out.println( "Opened: " + s );

            for ( int j = 0; j < shp.getRecordNum(); j++ ) {
                mergedFeatures.add( shp.getFeatureByRecNo( j + 1 ) );
            }
            shp.close();
            System.gc();
        }

        this.outputFile = new File( args[0] );

    }

    /**
     *
     * @return merged featurecollection
     */
    public FeatureCollection getMergedFeatures() {
        return this.mergedFeatures;
    }

    private void save()
                            throws IOException {

        if ( this.mergedFeatures != null ) {
            String s = this.outputFile.getAbsolutePath();
            ShapeFile shp = new ShapeFile( s, "rw" );
            try {
                shp.writeShape( this.mergedFeatures );
                System.out.println( "Saved: " + s );
            } catch ( Exception e ) {
                throw new IOException( "Could not save merged FeatureCollection: " + e.getLocalizedMessage() );
            }
            shp.close();
        }
    }

    /**
     * @param args
     */
    public static void main( String[] args ) {

        if ( args.length < 3 ) {
            System.out.println( "Usage: java -classpath .;libs/deegree2.jar;libs/jaxen-1.1-beta-8.jar;libs/jts-1.8.jar;libs/log4j-1.2.9.jar org.deegree.tools.shape.ShapefileMerger <out_shapefile> <in_shape_1> <in_shape2> ... <in_shape_n>" );
            System.exit( 0 );
        }

        ShapefileMerger shpMerger = null;
        try {
            shpMerger = new ShapefileMerger( args );
            shpMerger.save();

        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit( 1 );
        }

    }

}
