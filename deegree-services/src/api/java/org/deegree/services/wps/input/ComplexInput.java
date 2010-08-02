//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.services.wps.input;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * A complex data structure {@link ProcessletInput} parameter, i.e. an object encoded in XML or a raw binary stream.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public interface ComplexInput extends ProcessletInput {

    /**
     * Returns the mime type of the input.
     * 
     * @return the mime type of the input, may be <code>null</code>
     */
    public String getMimeType();

    /**
     * Returns the encoding information supplied with the input.
     * 
     * @return the encoding information supplied with the input, may be <code>null</code>
     */
    public String getEncoding();

    /**
     * Returns the schema URL supplied with the input.
     * 
     * @return the schema URL supplied with the input, may be <code>null</code>
     */
    public String getSchema();

    /**
     * Returns an {@link InputStream} for accessing the complex value as a raw stream of bytes (usually for binary
     * input).
     * <p>
     * NOTE: Never use this method if the input parameter is encoded in XML -- use {@link #getValueAsXMLStream()}
     * instead. Otherwise erroneous behaviour has to be expected (if the input value is given embedded in the execute
     * request document).
     * </p>
     * 
     * @see #getValueAsXMLStream()
     * @return the input value as a raw stream of bytes
     * @throws IOException
     *             if accessing the value fails
     */
    public InputStream getValueAsBinaryStream()
                            throws IOException;

    /**
     * Returns an {@link XMLStreamReader} for accessing the complex value as an XML event stream.
     * <p>
     * NOTE: Never use this method if the input parameter is a binary value -- use {@link #getValueAsBinaryStream()}
     * instead.
     * </p>
     * The returned stream will point at the first START_ELEMENT event of the data.
     * 
     * @return the input value as an XML event stream, current event is START_ELEMENT (the root element of the data
     *         object)
     * @throws IOException
     *             if accessing the value fails
     * @throws XMLStreamException
     */
    public XMLStreamReader getValueAsXMLStream()
                            throws IOException, XMLStreamException;
}
