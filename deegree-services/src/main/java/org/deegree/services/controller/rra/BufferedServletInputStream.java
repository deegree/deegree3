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
package org.deegree.services.controller.rra;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

/**
 * This is a buffered {@link ServletInputStream}.
 *
 * It will buffer all data that is read from the original stream. After the stream is reseted, read() will return the
 * buffered data. If all buffered data is returned, new data is retrieved from the original stream.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
class BufferedServletInputStream extends ServletInputStream {

    /**
     * buffers data that should be re-submitted after a reset
     */
    private ByteArrayInputStream resubmitBuffer;

    /**
     * buffers all data read from the original InputStream
     */
    private ByteArrayOutputStream completeBuffer;

    private InputStream stream;

    /**
     * @param stream
     */
    public BufferedServletInputStream( InputStream stream ) {
        this.stream = stream;
        this.completeBuffer = new ByteArrayOutputStream();
        this.resubmitBuffer = new ByteArrayInputStream( new byte[] {} );
    }

    @Override
    public int read()
                            throws IOException {
        int value = resubmitBuffer.read();
        if ( value == -1 ) { // end of resubmitBuffer
            value = stream.read();
            completeBuffer.write( value );
        }
        return value;
    }

    @Override
    public synchronized void reset()
                            throws IOException {
        resubmitBuffer = new ByteArrayInputStream( completeBuffer.toByteArray() );
    }
}
