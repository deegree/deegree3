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
package org.deegree.model.coverage.grid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deegree.datatypes.parameter.GeneralParameterValueIm;
import org.deegree.datatypes.parameter.InvalidParameterNameException;
import org.deegree.datatypes.parameter.InvalidParameterValueException;
import org.deegree.datatypes.parameter.ParameterNotFoundException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.LonLatEnvelope;
import org.deegree.ogcwebservices.wcs.configuration.File;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageOffering;

/**
 * This reader enables creation of <tt>GridCoverage</tt>s from more than one source. This will be
 * used for example for tiled images.
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class CompoundGridCoverageReader extends AbstractGridCoverageReader {

    private static final ILogger LOGGER = LoggerFactory.getLogger( CompoundGridCoverageReader.class );

    /**
     * @param source
     * @param description
     * @param envelope
     * @param format
     */
    public CompoundGridCoverageReader( File[] source, CoverageOffering description, Envelope envelope, Format format ) {
        super( source, description, envelope, format );
    }

    /**
     * Read the grid coverage from the current stream position, and move to the next grid coverage.
     *
     * @param parameters
     *            An optional set of parameters. Should be any or all of the parameters returned by
     *            {@link "org.opengis.coverage.grid.Format#getReadParameters"}.
     * @return A new {@linkplain GridCoverage grid coverage} from the input source.
     * @throws InvalidParameterNameException
     *             if a parameter in <code>parameters</code> doesn't have a recognized name.
     * @throws InvalidParameterValueException
     *             if a parameter in <code>parameters</code> doesn't have a valid value.
     * @throws ParameterNotFoundException
     *             if a parameter was required for the operation but was not provided in the
     *             <code>parameters</code> list.
     * @throws IOException
     *             if a read operation failed for some other input/output reason, including
     *             {@link java.io.FileNotFoundException} if no file with the given <code>name</code>
     *             can be found, or {@link javax.imageio.IIOException} if an error was thrown by the
     *             underlying image library.
     */
    public GridCoverage read( GeneralParameterValueIm[] parameters )
                            throws InvalidParameterNameException, InvalidParameterValueException,
                            ParameterNotFoundException, IOException {

        File[] files = (File[]) source;
        List<GridCoverage> list = new ArrayList<GridCoverage>( files.length );
        for ( int i = 0; i < files.length; i++ ) {
            GridCoverageReader gcr = createGridCoverageReader( files[i] );
            if ( gcr != null ) {
                GridCoverage gc = gcr.read( parameters );
                if ( gc != null ) {
                    list.add( gc );
                }
            }
        }
        return createGridCoverage( list );
    }

    /**
     * creates a GridCoverage compound from the GridCoverages contained in the passed List. It is
     * assumed that all GridCoverages in the List are of the same type and that the list contains at
     * least one GridCoverage
     *
     * @param list
     * @return a GridCoverage compound from the GridCoverages contained in the passed List.
     */
    private GridCoverage createGridCoverage( List<GridCoverage> list ) {

        GridCoverage gc = null;
        if ( list != null && list.size() > 0 ) {
            gc = list.get( 0 );
            if ( gc instanceof ImageGridCoverage ) {
                ImageGridCoverage[] tmp = new ImageGridCoverage[list.size()];
                tmp = list.toArray( tmp );
                gc = new ImageGridCoverage( description, envelope, tmp );
            } else if ( gc instanceof ByteGridCoverage ) {
                ByteGridCoverage[] tmp = new ByteGridCoverage[list.size()];
                tmp = list.toArray( tmp );
                gc = new ByteGridCoverage( description, envelope, tmp );
            } else if ( gc instanceof ShortGridCoverage ) {
                ShortGridCoverage[] tmp = new ShortGridCoverage[list.size()];
                tmp = list.toArray( tmp );
                gc = new ShortGridCoverage( description, envelope, tmp );
            } else if ( gc instanceof FloatGridCoverage ) {
                FloatGridCoverage[] tmp = new FloatGridCoverage[list.size()];
                tmp = list.toArray( tmp );
                gc = new FloatGridCoverage( description, envelope, tmp );
            }
        }

        return gc;
    }

    /**
     * creates a GridCoverageReader depending on the native format of the data source
     *
     * @param file
     * @return a GridCoverageReader depending on the native format of the data source
     * @throws IOException
     */
    private GridCoverageReader createGridCoverageReader( File file )
                            throws IOException, InvalidParameterValueException {

        // calculate and set LonLatBoundingBox for the GC CoverageOffering
        // as source of the returned GridCoverage
        LonLatEnvelope lle = calcLonLatEnvelope( file.getEnvelope(), file.getCrs().getIdentifier() );
        CoverageOffering desc = (CoverageOffering) description.clone();
        desc.setLonLatEnvelope( lle );

        Envelope env = envelope.createIntersection( file.getEnvelope() );
        GridCoverageReader gcr = null;
        if ( env != null ) {
            if ( format.getName().equalsIgnoreCase( "GEOTIFF" ) ) {
                gcr = new GeoTIFFGridCoverageReader( file, desc, env, format );
            } else if ( isImageFormat( format ) ) {
                gcr = new ImageGridCoverageReader( file, desc, env, format );
            } else {
                throw new IOException( "not supported file format: " + format.getName() );
            }
        } else {
            LOGGER.logInfo( "no data available for BBOX: ", file.getEnvelope() );
        }

        return gcr;
    }

    /**
     * returns true if the passed format is an image format
     *
     * @param format
     * @return <code>true</code> if the passed format is an image format
     */
    private boolean isImageFormat( Format format ) {
        String frmt = format.getName().toUpperCase();
        return frmt.equalsIgnoreCase( "png" ) || frmt.equalsIgnoreCase( "bmp" ) || frmt.equalsIgnoreCase( "tif" )
               || frmt.equalsIgnoreCase( "tiff" ) || frmt.equalsIgnoreCase( "gif" ) || frmt.equalsIgnoreCase( "jpg" )
               || frmt.equalsIgnoreCase( "jpeg" ) || frmt.indexOf( "ECW" ) > -1;
    }

    /**
     * This method is an implementation dummy, it doensn't actually do anything.
     */
    public void dispose()
                            throws IOException {
        // throwin nottin
    }

}
