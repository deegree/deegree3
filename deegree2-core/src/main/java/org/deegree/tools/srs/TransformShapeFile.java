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
package org.deegree.tools.srs;

import java.util.Iterator;
import java.util.Properties;

import org.deegree.io.shpapi.shape_new.ShapeFile;
import org.deegree.io.shpapi.shape_new.ShapeFileReader;
import org.deegree.io.shpapi.shape_new.ShapeFileWriter;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;

/**
 * Tool to transform shapefiles from one CRS to another.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class TransformShapeFile {

    private static void transformShapeFile( String inFile, String inCRS, String outFile, String outCRS )
                            throws Exception {

        ShapeFile shapeFile = new ShapeFileReader( inFile, CRSFactory.create( inCRS ) ).read();
        FeatureCollection fc = shapeFile.getFeatureCollection();

        GeoTransformer gt = new GeoTransformer( outCRS );

        int i = 0;
        System.out.println( "Transforming:                  " );
        Iterator<Feature> iter = fc.iterator();
        int step = (int) Math.floor( 5 * ( fc.size() * 0.01 ) );
        step = Math.max( 1, step );
        int percentage = 0;
        while ( iter.hasNext() ) {
            Feature feature = iter.next();
            gt.transform( feature );
            if ( i++ % step == 0 ) {
                System.out.print( "\r" + ( percentage ) + ( ( ( percentage ) < 10 ) ? "  " : " " ) + "% transformed" );
                percentage += 5;
            }
        }
        System.out.println();

        ShapeFile result = new ShapeFile( fc, outFile );

        new ShapeFileWriter( result ).write();

    }

    private static void printHelpAndExit() {
        System.out.println( "Usage: java [...] org.deegree.tools.srs.TransformShapeFile " );
        System.out.println( "                  -inFile shapeBasename -inCRS crs " );
        System.out.println( "                  [-outFile shapeBasename] -outCRS crs" );
        System.exit( 1 );
    }

    /**
     * @param args
     */
    public static void main( String[] args ) {

        if ( args.length % 2 != 0 )
            printHelpAndExit();

        Properties map = new Properties();
        for ( int i = 0; i < args.length; i += 2 ) {
            map.put( args[i], args[i + 1] );
        }

        String outCRS = (String) map.get( "-outCRS" );
        if ( outCRS == null )
            printHelpAndExit();

        String inCRS = (String) map.get( "-inCRS" );
        if ( inCRS == null )
            printHelpAndExit();

        String inFilename = (String) map.get( "-inFile" );
        if ( inFilename == null )
            printHelpAndExit();

        String outFilename = (String) map.get( "-outFile" );
        if ( outFilename == null ) {
            outFilename = inFilename + "." + outCRS;
        }

        try {
            transformShapeFile( inFilename, inCRS, outFilename, outCRS );
        } catch ( Exception e ) {
            e.printStackTrace();
            System.out.println( e.getLocalizedMessage() );
            System.exit( 2 );
        }
    }

}
