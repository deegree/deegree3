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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;

import org.deegree.crs.components.Unit;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;

/**
 * The <code>DemoCRSTransform</code> is a sa(i)mple application for using deegree coordinate systems and their
 * transformations.
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 */
public class DemoCRSTransform {

    private static ILogger LOG = LoggerFactory.getLogger( DemoCRSTransform.class );

    private CoordinateSystem sourceCRS;

    private CoordinateSystem targetCRS;

    /**
     * Construct a demo crs with following coordinate systems.
     *
     * @param sourceCRS
     * @param targetCRS
     */
    public DemoCRSTransform( CoordinateSystem sourceCRS, CoordinateSystem targetCRS ) {
        this.sourceCRS = sourceCRS;
        this.targetCRS = targetCRS;
    }

    /**
     * This method transforms the given coordinate (in the sourceCRS) into a coordinate of the targetCRS and back.
     *
     * @param coordinate
     *            to be transformed.
     * @param withInverse
     *            true if the inverse has to be calculated.
     */
    public void doTransform( Point3d coordinate, boolean withInverse ) {
        GeoTransformer gt = new GeoTransformer( targetCRS );

        // point to transform
        Point point = GeometryFactory.createPoint( coordinate.x, coordinate.y, coordinate.z, sourceCRS );

        outputPoint( "The original point in crs: " + sourceCRS.getIdentifier() + ": ", point, sourceCRS );
        Point pp = null;
        try {
            pp = (Point) gt.transform( point );
        } catch ( IllegalArgumentException e ) {
            LOG.logError( e.getMessage(), e );
        } catch ( CRSTransformationException e ) {
            LOG.logError( e.getMessage(), e );
        }
        outputPoint( "The transformed point in crs: " + targetCRS.getIdentifier() + ": ", pp, targetCRS );
        if ( withInverse ) {

            // transform back to source CRS
            gt = new GeoTransformer( sourceCRS );
            try {
                point = (Point) gt.transform( pp );
            } catch ( IllegalArgumentException e ) {
                LOG.logError( e.getMessage(), e );
            } catch ( CRSTransformationException e ) {
                LOG.logError( e.getMessage(), e );
            }
            outputPoint( "The inversed transformed point in crs: " + sourceCRS.getIdentifier() + ": ", point, sourceCRS );
        }

    }

    private void outputPoint( String outputString, Point coord, CoordinateSystem currentCRS ) {
        double resultX = coord.getX();
        double resultY = coord.getY();
        double resultZ = coord.getZ();
        Unit[] allUnits = currentCRS.getAxisUnits();
        System.out.println( outputString + resultX + allUnits[0] + ", " + resultY + allUnits[1]
                            + ( ( currentCRS.getDimension() == 3 ) ? ", " + resultZ + allUnits[2] : "" ) );

    }

    /**
     * a starter method to transform a given point or a serie of points read from a file.
     *
     * @param args
     * @throws UnknownCRSException
     * @throws IOException
     *             if the buffered reader could not read from the file
     */
    public static void main( String[] args )
                            throws UnknownCRSException, IOException {
        Map<String, String> params = new HashMap<String, String>( 5 );
        for ( int i = 0; i < args.length; i++ ) {
            String arg = args[i];
            if ( arg != null && !"".equals( arg.trim() ) ) {
                arg = arg.trim().toLowerCase();
                if ( arg.equalsIgnoreCase( "-?" ) || arg.equalsIgnoreCase( "-h" ) ) {
                    outputHelp();
                } else {
                    if ( i + 1 < args.length ) {
                        String val = args[++i];
                        if ( val != null ) {
                            params.put( arg, val.trim() );
                        } else {
                            LOG.logInfo( "Invalid value for parameter: " + arg );
                        }
                    } else {
                        LOG.logInfo( "No value for parameter: " + arg );
                    }
                }
            }
        }

        /**
         * add crs and point here if using eclipse to start.
         */
        // String sourceCRS = "EPSG:25832";
        // String targetCRS = "EPSG:31466";
        // String coord = "370766.738,5685588.661";

        String sourceCRS = "";
        String targetCRS = "";
        String coord = "";

        if ( "".equals( sourceCRS ) ) {
            sourceCRS = params.get( "-sourcecrs" );
            if ( sourceCRS == null || "".equals( sourceCRS.trim() ) ) {
                LOG.logError( "No source CRS given (-sourceCRS parameter)" );
                System.exit( 1 );
            }
        }
        if ( "".equals( targetCRS ) ) {
            targetCRS = params.get( "-targetcrs" );
            if ( targetCRS == null || "".equals( targetCRS.trim() ) ) {
                LOG.logError( "No target CRS given (-targetCRS parameter)" );
                System.exit( 1 );
            }
        }

        CoordinateSystem source = CRSFactory.create( sourceCRS );
        CoordinateSystem target = CRSFactory.create( targetCRS );

        DemoCRSTransform demo = new DemoCRSTransform( source, target );

        String sourceFile = params.get( "-sourcefile" );
        if ( "".equals( coord ) && sourceFile != null && !"".equals( sourceFile.trim() ) ) {
            String coordSep = params.get( "-coordsep" );
            if ( coordSep == null || "".equals( coordSep ) ) {
                LOG.logInfo( "No coordinates separator given (-coordSep parameter), therefore using ' ' (a space) as separator" );
                coordSep = " ";
            }
            BufferedReader br = new BufferedReader( new FileReader( sourceFile ) );
            String coords = br.readLine();

            int lineCount = 1;
            final int sourceDim = source.getDimension();
            List<Point3d> coordinateList = new LinkedList<Point3d>();
            while ( coords != null ) {
                if ( !coords.startsWith( "#" ) ) {
                    String[] coordinates = coords.split( coordSep );
                    if ( coordinates.length != sourceDim ) {
                        LOG.logError( lineCount
                                      + ") Each line must contain the number of coordinates fitting the dimension of the source crs ("
                                      + sourceDim + ") seperated by a '" + coordSep + "'." );
                    } else {
                        Point3d from = new Point3d();
                        from.x = Double.parseDouble( coordinates[0].replace( ",", "." ) );
                        from.y = Double.parseDouble( coordinates[1].replace( ",", "." ) );
                        if ( sourceDim == 3 ) {
                            from.z = Double.parseDouble( coordinates[2].replace( ",", "." ) );
                        }
                        coordinateList.add( from );
                    }
                }
                coords = br.readLine();
                lineCount++;
            }
            if ( coordinateList.size() == 0 ) {
                LOG.logError( "No valid points found in file: " + sourceFile );
            } else {
                long time = System.currentTimeMillis();
                for ( Point3d c : coordinateList ) {
                    demo.doTransform( c, false );
                }
                System.out.println( "Transformation took: " + ( ( System.currentTimeMillis() - time ) / 1000. )
                                    + " seconds" );
            }
        } else {
            if ( "".equals( coord ) ) {
                coord = params.get( "-coord" );
                if ( coord == null || "".equals( coord.trim() ) ) {
                    LOG.logError( "No coordinate(s) to transform, use the -coord or the -sourceFile parameter to define a coordinate (list)." );
                    System.exit( 1 );
                }
            }
            String[] splitter = coord.split( "," );
            if ( splitter == null || splitter.length == 1 || splitter.length > 3 ) {
                LOG.logError( "A coordinate must be comma separated and may only have two or three ordinates e.g. -coord \"3.1415 , 2.7182\"" );
                System.exit( 1 );
            }
            double x = Double.parseDouble( splitter[0] );
            double y = Double.parseDouble( splitter[1] );
            double z = ( splitter.length == 3 ) ? Double.parseDouble( splitter[2] ) : 0;
            demo.doTransform( new Point3d( x, y, z ), true );
        }
    }

    private static void outputHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append( "The DemoCRSTransform program can be used to transform a single given coordinate or a set of coordinates read from a file.\n" );
        sb.append( "Following parameters are supported:\n" );
        sb.append( "-sourceCRS (required) the name of the source crs, e.g. EPSG:4326.\n" );
        sb.append( "-targetCRS (required) the name of the target crs, e.g. EPSG:31467.\n" );
        sb.append( "[-coord] parameter defining a coordinate (comma separated) in the source crs, e.g. '3.1415 , 2.7182' \n" );
        sb.append( "[-sourceFile] a /path/of/a_list_of_coordinates.txt containing a list of coordinate pairs/triples. If supplied the -coordSep (the separator between the ordinates will also be evalutated)..\n" );
        sb.append( "[-coordSep] (only valid with -sourceFile) defining a separator between the coords in the file e.g. a ';' or ',' if omitted a space is assumed.\n" );
        sb.append( "-?|-h output this text\n" );
        System.out.println( sb.toString() );
        System.exit( 1 );
    }

}
