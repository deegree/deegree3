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
import java.io.InputStream;

import org.deegree.datatypes.parameter.GeneralParameterValueIm;
import org.deegree.datatypes.parameter.InvalidParameterNameException;
import org.deegree.datatypes.parameter.InvalidParameterValueException;
import org.deegree.datatypes.parameter.ParameterNotFoundException;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.wcs.configuration.File;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageOffering;

/**
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class RawDataReader extends AbstractGridCoverageReader {

    /**
     * @param source
     * @param description
     * @param format
     */
    public RawDataReader( File source, CoverageOffering description, Format format ) {
        super( source, description, source.getEnvelope(), format );
    }

    /**
     * @param source
     * @param description
     * @param envelope
     * @param format
     */
    public RawDataReader( InputStream source, CoverageOffering description, Envelope envelope, Format format ) {
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
        return null;
    }

    /**
     * Allows any resources held by this object to be released. The result of calling any other
     * method subsequent to a call to this method is undefined. It is important for applications to
     * call this method when they know they will no longer be using this
     * <code>GridCoverageReader</code>. Otherwise, the reader may continue to hold on to
     * resources indefinitely.
     *
     * @throws IOException
     *             if an error occured while disposing resources (for example while closing a file).
     */
    public void dispose()
                            throws IOException {
        if ( source instanceof InputStream ) {
            ( (InputStream) source ).close();
        }
    }

}
