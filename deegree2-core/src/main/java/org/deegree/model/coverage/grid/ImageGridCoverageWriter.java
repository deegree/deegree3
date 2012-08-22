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
import java.io.OutputStream;
import java.util.Map;

import org.deegree.datatypes.parameter.GeneralParameterValueIm;
import org.deegree.datatypes.parameter.InvalidParameterNameException;
import org.deegree.datatypes.parameter.InvalidParameterValueException;
import org.deegree.datatypes.parameter.OperationParameterIm;
import org.deegree.datatypes.parameter.ParameterNotFoundException;
import org.deegree.framework.util.ImageUtils;

/**
 * This class encapsulates functionality for writing a <tt>GridCoverage</tt> as a GeoTIFF to a
 * defined destination. Ths destination will be given as an <tt>OutputStream</tt>. The current
 * implementation is limited to support <tt>ImageGridCoverage</tt>s to be written as GeoTIFF.
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class ImageGridCoverageWriter extends AbstractGridCoverageWriter {

    /**
     * @param destination
     * @param metadata
     * @param subNames
     * @param currentSubname
     * @param format
     */
    public ImageGridCoverageWriter( Object destination, Map<String, Object> metadata, String[] subNames,
                                    String currentSubname, Format format ) {
        super( destination, metadata, subNames, currentSubname, format );
    }

    /**
     * Writes the specified grid coverage.
     *
     * @param coverage
     *            The {@linkplain GridCoverage grid coverage} to write.
     * @param parameters
     *            An optional set of parameters. Should be any or all of the parameters returned by
     *            {@link "org.opengis.coverage.grid.Format#getWriteParameters"}.
     * @throws InvalidParameterNameException
     *             if a parameter in <code>parameters</code> doesn't have a recognized name.
     * @throws InvalidParameterValueException
     *             if a parameter in <code>parameters</code> doesn't have a valid value.
     * @throws ParameterNotFoundException
     *             if a parameter was required for the operation but was not provided in the
     *             <code>parameters</code> list.
     * @throws IOException
     *             if the export failed for some other input/output reason, including
     *             {@link javax.imageio.IIOException} if an error was thrown by the underlying image
     *             library.
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

        OutputStream out = (OutputStream) destination;
        ImageGridCoverage igc = (ImageGridCoverage) coverage;
        ImageUtils.saveImage( igc.getAsImage( width, height ), out, format.getName(), 1f );

    }

    /**
     * Allows any resources held by this object to be released. The result of calling any other
     * method subsequent to a call to this method is undefined. It is important for applications to
     * call this method when they know they will no longer be using this
     * <code>GridCoverageWriter</code>. Otherwise, the writer may continue to hold on to
     * resources indefinitely.
     *
     * @throws IOException
     *             if an error occured while disposing resources (for example while flushing data
     *             and closing a file).
     */
    public void dispose()
                            throws IOException {
        ( (OutputStream) destination ).close();
    }

}
