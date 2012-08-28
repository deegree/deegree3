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

package org.deegree.services.controller.exception.serializer;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.deegree.protocol.ows.exception.OWSException;

/**
 * Writes {@link OWSException}s to the {@link HttpServletResponse}.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface ExceptionSerializer<T extends OWSException> {

    /**
     * Serializes the given exception using the specified encoding.
     * 
     * @param response
     *            servlet response, must not be <code>null</code>
     * @param exception
     *            exception to be serialized, must not be <code>null</code>
     * @throws IOException
     */
    public void serializeException( HttpServletResponse response, T exception )
                            throws IOException;

    /**
     * An implementation of this method shall format the given exception and write it to the stream.
     * 
     * @param outputStream
     *            to write the implementation specific format to.
     * @param exception
     *            exception to write
     * @param requestedEncoding
     *            of the stream
     * @throws IOException
     *             if an error occurred while writing to the stream.
     */
    public void serializeException( OutputStream outputStream, T exception, String requestedEncoding )
                            throws IOException;

}
