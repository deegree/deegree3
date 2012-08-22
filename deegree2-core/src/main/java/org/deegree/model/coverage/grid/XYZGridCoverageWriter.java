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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import org.deegree.datatypes.parameter.GeneralParameterValueIm;
import org.deegree.datatypes.parameter.InvalidParameterNameException;
import org.deegree.datatypes.parameter.InvalidParameterValueException;
import org.deegree.datatypes.parameter.OperationParameterIm;
import org.deegree.datatypes.parameter.ParameterNotFoundException;
import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.graphics.transformation.WorldToScreenTransform;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.processing.raster.converter.Image2RawData;

/**
 * Implementation of {@link "org.opengis.coverage.grid.GridCoverageWriter"} for writing a GridCoverage as XYZ coordinate
 * tuples to a defined destioation
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class XYZGridCoverageWriter extends AbstractGridCoverageWriter {

    private static NumberFormat nf = new DecimalFormat( "##########.###" );

    /**
     *
     * @param destination
     * @param metadata
     * @param subNames
     * @param currentSubname
     * @param format
     */
    public XYZGridCoverageWriter( Object destination, Map<String, Object> metadata, String[] subNames,
                                  String currentSubname, Format format ) {
        super( destination, metadata, subNames, currentSubname, format );

    }

    /**
     * disposes all resources assigned to a GMLGridCoverageWriter instance. For most cases this will be IO-resources
     *
     * @throws IOException
     */
    public void dispose()
                            throws IOException {
        // nothing happening here...
    }

    /**
     * @param coverage
     * @param parameters
     *            must contain the servlet URL within the first field; all other fields must contain the required
     *            parameters for a valid GetCoverage request
     * @throws InvalidParameterNameException
     * @throws InvalidParameterValueException
     * @throws ParameterNotFoundException
     * @throws IOException
     */
    public void write( GridCoverage coverage, GeneralParameterValueIm[] parameters )
                            throws InvalidParameterNameException, InvalidParameterValueException,
                            ParameterNotFoundException, IOException {

        int width = -1;
        int height = -1;
        for ( int i = 0; i < parameters.length; i++ ) {
            OperationParameterIm op = (OperationParameterIm) parameters[i].getDescriptor();
            String name = op.getName();
            if ( name.equalsIgnoreCase( "WIDTH" ) ) {
                Object o = op.getDefaultValue();
                width = ( (Integer) o ).intValue();
            } else if ( name.equalsIgnoreCase( "HEIGHT" ) ) {
                Object o = op.getDefaultValue();
                height = ( (Integer) o ).intValue();
            }
        }

        PrintWriter pw = new PrintWriter( (OutputStream) destination );

        BufferedImage im = ( (AbstractGridCoverage) coverage ).getAsImage( width, height );
        Image2RawData i2r = new Image2RawData( im );
        float[][] data = i2r.parse();

        Envelope env = coverage.getEnvelope();

        GeoTransform gt = new WorldToScreenTransform( env.getMin().getX(), env.getMin().getY(), env.getMax().getX(),
                                                      env.getMax().getY(), 0, 0, im.getWidth() - 1, im.getHeight() - 1 );
        double offset = 0;
        double scaleFactor = 1;
        if ( metadata.get( "offset" ) != null ) {
            offset = (Double) metadata.get( "offset" );
        }
        if ( metadata.get( "scaleFactor" ) != null ) {
            scaleFactor = (Double) metadata.get( "scaleFactor" );
        }

        for ( int r = 0; r < data.length; r++ ) {
            for ( int c = 0; c < data[r].length; c++ ) {
                double x = gt.getSourceX( c );
                double y = gt.getSourceY( r );
                pw.print( x );
                pw.print( ' ' );
                pw.print( y );
                pw.print( ' ' );
                double d = ( data[r][c] / scaleFactor ) - offset;
                pw.print( nf.format( d ).replace( ',', '.' ) );
                pw.print( "\n" );
            }
        }
        pw.flush();
    }

}
