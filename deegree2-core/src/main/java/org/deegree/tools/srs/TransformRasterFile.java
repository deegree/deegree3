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

import java.awt.image.BufferedImage;
import java.util.Properties;

import javax.media.jai.InterpolationNearest;

import org.deegree.datatypes.CodeList;
import org.deegree.framework.util.ImageUtils;
import org.deegree.model.coverage.grid.ImageGridCoverage;
import org.deegree.model.coverage.grid.WorldFile;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.SupportedFormats;
import org.deegree.ogcwebservices.SupportedSRSs;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageOffering;
import org.deegree.ogcwebservices.wcs.describecoverage.DomainSet;
import org.deegree.ogcwebservices.wcs.describecoverage.RangeSet;
import org.deegree.ogcwebservices.wcs.describecoverage.SpatialDomain;

/**
 * Tool to transform raster files from one CRS to another.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class TransformRasterFile {

    private final static float DEFAULT_IMAGE_QUALITY = 0.9f;

    private final static int DEFAULT_PP_GRID_SIZE = 5;

    private final static int DEFAULT_POLY_ORDER = 3;

    private static void transformRasterFile( String inFile, String inCRS, String outFile, String outCRS,
                                             Float imageQuality, int ppgridsize, int polynomOrder )
                            throws Exception {

        System.out.println( "Loading raster " + inFile );
        BufferedImage image = ImageUtils.loadImage( inFile );

        CoordinateSystem sourceCRS = CRSFactory.create( inCRS );
        CoordinateSystem targetCRS = CRSFactory.create( outCRS );

        WorldFile worldFile = WorldFile.readWorldFile( inFile, WorldFile.TYPE.CENTER, image );
        Envelope inEnvelope = worldFile.getEnvelope();

        // create minimal CoverageOffering for ImageGridCoverage
        // most parts are not used
        DomainSet ds = new DomainSet( new SpatialDomain( new Envelope[] { inEnvelope } ) );
        RangeSet rs = new RangeSet( "", "" );
        CodeList[] dummyCodeList = new CodeList[] { new CodeList( "", new String[] {} ) };
        CodeList[] nativeSRSCodeList = new CodeList[] { new CodeList( "", new String[] { inCRS } ) };

        SupportedSRSs supSRSs = new SupportedSRSs( dummyCodeList, dummyCodeList, dummyCodeList, nativeSRSCodeList );

        SupportedFormats supFormats = new SupportedFormats( dummyCodeList );

        CoverageOffering coverageOffering = new CoverageOffering( "", "", "", null, null, null, ds, rs, supSRSs,
                                                                  supFormats, null, null );

        ImageGridCoverage igc = new ImageGridCoverage( coverageOffering, inEnvelope, image );

        GeoTransformer gt = new GeoTransformer( targetCRS );

        Envelope outEnvelope = gt.transform( inEnvelope, sourceCRS, true );

        // calculate new output size
        // use square pixels for output, ie. the aspect ratio changes
        double deltaX = outEnvelope.getWidth();
        double deltaY = outEnvelope.getHeight();
        double diagSize = Math.sqrt( deltaX * deltaX + deltaY * deltaY );
        // pixelSize for calculation of the new image size
        double pixelSize = diagSize / Math.sqrt( Math.pow( image.getWidth(), 2 ) + Math.pow( image.getHeight(), 2 ) );
        int height = (int) ( deltaY / pixelSize + 0.5 );
        int width = (int) ( deltaX / pixelSize + 0.5 );
        // realPixelSize for center type world files, etc.
        double realPixelSize = diagSize
                               / Math.sqrt( Math.pow( image.getWidth() - 1, 2 ) + Math.pow( image.getHeight() - 1, 2 ) );

        System.out.println( "Transforming raster from " + inCRS + " to " + outCRS );
        igc = (ImageGridCoverage) gt.transform( igc, outEnvelope, width, height, ppgridsize, polynomOrder,
                                                new InterpolationNearest() );

        image = igc.getAsImage( -1, -1 );

        System.out.println( "Saving raster " + outFile );
        ImageUtils.saveImage( image, outFile, imageQuality );

        // save new WorldFile
        WorldFile outWorldFile = new WorldFile( realPixelSize, realPixelSize, 0.0f, 0.0f, outEnvelope );
        String basename = outFile.substring( 0, outFile.lastIndexOf( "." ) );
        WorldFile.writeWorldFile( outWorldFile, basename );

    }

    private static void printHelpAndExit() {
        System.out.println( "Usage: java [...] org.deegree.tools.srs.TransformRasterFile " );
        System.out.println( "                  -inFile filename -inCRS crs " );
        System.out.println( "                  [-outFile filename] -outCRS crs" );
        System.out.println( "                  [-imageQuality 0.X]" );
        System.out.println( "                  -passpointGridSize 5 -polynomOrder 3 " );
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
            String ext = inFilename.substring( inFilename.lastIndexOf( "." ) );
            outFilename = inFilename.substring( 0, inFilename.lastIndexOf( "." ) );
            outFilename = outFilename + "." + outCRS + ext;
        }

        String imageQualityString = (String) map.get( "-imageQuality" );
        float imageQuality = DEFAULT_IMAGE_QUALITY;
        if ( imageQualityString != null ) {
            imageQuality = Float.valueOf( imageQualityString );
        }
        String ppgridsizeString = (String) map.get( "-passpointGridSize" );
        int ppgridsize = DEFAULT_PP_GRID_SIZE;
        if ( ppgridsizeString != null ) {
            ppgridsize = Integer.parseInt( ppgridsizeString );
        }

        String polynomOrderString = (String) map.get( "-polynomOrder" );
        int polynomOrder = DEFAULT_POLY_ORDER;
        if ( polynomOrderString != null ) {
            polynomOrder = Integer.parseInt( polynomOrderString );
        }

        try {
            transformRasterFile( inFilename, inCRS, outFilename, outCRS, imageQuality, ppgridsize, polynomOrder );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

}
