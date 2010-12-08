//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.protocol.wps.client.input;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.protocol.wps.client.param.ComplexFormat;

/**
 * {@link ExecutionInput} that encapsulates a binary value.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class BinaryInput extends ExecutionInput {

    private ComplexFormat complexAttributes;

    private URL url;

    private InputStream inputStream;

    private boolean isWebAccessible;

    /**
     * Creates a new {@link BinaryInput} instance.
     * 
     * @param id
     *            parameter identifier, must not be <code>null</code>
     * @param url
     *            URL for accessing the binary resource, must not be <code>null</code>
     * @param isWebAccessible
     *            if true, the data will be submitted to the process as reference, otherwise it will be encoded in the
     *            request
     * @param mimeType
     *            mime type of the binary resource, may be <code>null</code> (unspecified)
     * @param encoding
     *            encoding to be used for the binary data, may be <code>null</code> (unspecified)
     */
    public BinaryInput( CodeType id, URL url, boolean isWebAccessible, String mimeType, String encoding ) {
        super( id );
        this.url = url;
        this.isWebAccessible = isWebAccessible;
        this.complexAttributes = new ComplexFormat( mimeType, encoding, null );
    }

    /**
     * Creates a new {@link BinaryInput} instance.
     * 
     * @param id
     *            parameter identifier, must not be <code>null</code>
     * @param inputStream
     *            binary stream, must not be <code>null</code>
     * @param mimeType
     *            mime type of the binary resource, may be <code>null</code> (unspecified)
     * @param encoding
     *            encoding to be used for the binary data, may be <code>null</code> (unspecified)
     */
    public BinaryInput( CodeType id, InputStream inputStream, String mimeType, String encoding ) {
        super( id );
        this.inputStream = inputStream;
        this.complexAttributes = new ComplexFormat( mimeType, encoding, null );
    }

    /**
     * Returns the format of the input.
     * 
     * @return the format of the input, never <code>null</code>
     */
    public ComplexFormat getFormat() {
        return complexAttributes;
    }

    /**
     * Returns the value as a binary stream.
     * 
     * @return the value as a binary stream, never <code>null</code>
     * @throws IOException
     *             if accessing the data fails
     */
    public InputStream getAsBinaryStream()
                            throws IOException {
        if ( inputStream != null ) {
            return inputStream;
        }
        return url.openStream();
    }

    @Override
    public URL getWebAccessibleURL() {
        return isWebAccessible ? url : null;
    }
}
