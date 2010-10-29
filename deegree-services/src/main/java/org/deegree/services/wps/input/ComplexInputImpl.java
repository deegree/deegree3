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

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.services.jaxb.wps.ComplexFormatType;
import org.deegree.services.jaxb.wps.ComplexInputDefinition;

/**
 * A complex data structure {@link ProcessletInput} parameter, e.g. an object encoded in GML or a raw binary stream.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public abstract class ComplexInputImpl extends ProcessletInputImpl implements ComplexInput {

    private String mimeType;

    private String encoding;

    private String schema;

    /**
     * Creates a new {@link ComplexInputImpl} instance.
     * 
     * @param definition
     *            corresponding input definition from process description
     * @param title
     *            optional title supplied with the input parameter, may be null
     * @param summary
     *            optional narrative description supplied with the input parameter, may be null
     * @param format
     *            the XML schema, format, and encoding of the complex value (must be compatible with the parameter
     *            definition)
     */
    protected ComplexInputImpl( ComplexInputDefinition definition, LanguageString title, LanguageString summary,
                                ComplexFormatType format ) {
        super( definition, title, summary );
        this.mimeType = format.getMimeType();
        this.encoding = format.getEncoding();
        this.schema = format.getSchema();
    }

    /**
     * Returns the mime type of the input.
     * 
     * @return the mime type of the input, may be null (if not specified in the parameter definition)
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Returns the encoding information supplied with the input.
     * 
     * @return the encoding information supplied with the input, may be null
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Returns the schema URL supplied with the input.
     * 
     * @return the schema URL supplied with the input, may be null
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Returns an {@link InputStream} for accessing the complex value as a raw stream of bytes (usually for binary
     * input).
     * <p>
     * NOTE: Never use this method if the input parameter is encoded in XML -- use {@link #getValueAsXMLStream()} or
     * {@link #getValueAsElement()} instead. Otherwise erroneous behaviour has to be expected (if the input value is
     * given embedded in the execute request document).
     * </p>
     * 
     * @see #getValueAsXMLStream()
     * @see #getValueAsElement()
     * @return the input value as a raw stream of bytes
     * @throws IOException
     *             if accessing the value fails
     */
    public abstract InputStream getValueAsBinaryStream()
                            throws IOException;

    public abstract XMLStreamReader getValueAsXMLStream()
                            throws IOException, XMLStreamException;

    /**
     * Returns an {@link OMElement} for accessing the complex value as an XML element node.
     * <p>
     * NOTE: Never use this method if the input parameter is a binary value -- use {@link #getValueAsBinaryStream()}
     * instead.
     * </p>
     * 
     * @return the input value as an XML element node
     * @throws IOException
     *             if accessing the value fails
     */
    public abstract OMElement getValueAsElement()
                            throws IOException;
}
