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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.Base64;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.services.jaxb.wps.ComplexFormatType;
import org.deegree.services.jaxb.wps.ComplexInputDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ComplexInputImpl} with a value that is given inline in the execute request document.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class EmbeddedComplexInput extends ComplexInputImpl {

    private static final Logger LOG = LoggerFactory.getLogger( EmbeddedComplexInput.class );

    private OMElement dataElement;

    /**
     * Creates a new {@link ComplexInputImpl} instance from a <code>wps:ComplexData</code> embedded in an execute
     * request document.
     * 
     * @param definition
     *            corresponding input definition from process description
     * @param title
     *            optional title supplied with the input parameter, may be null
     * @param summary
     *            optional narrative description supplied with the input parameter, may be null
     * @param format
     *            the XML schema, format, and encoding of the complex value
     * @param dataElement
     *            <code>wps:ComplexData</code> element from execute request document
     */
    public EmbeddedComplexInput( ComplexInputDefinition definition, LanguageString title, LanguageString summary,
                                 ComplexFormatType format, OMElement dataElement ) {
        super( definition, title, summary, format );
        this.dataElement = dataElement;
    }

    @Override
    public OMElement getValueAsElement() {
        return dataElement;
    }

    @Override
    public InputStream getValueAsBinaryStream() {

        String textValue = dataElement.getText();

        ByteArrayInputStream is = null;

        if ( "base64".equals( getEncoding() ) ) {
            LOG.debug( "Performing base64 decoding of embedded ComplexInput: " + textValue );
            is = new ByteArrayInputStream( Base64.decode( textValue ) );
        } else {
            LOG.warn( "Unsupported encoding '" + getEncoding() + "'." );
            is = new ByteArrayInputStream( textValue.getBytes() );
        }

        return is;
    }

    @Override
    public XMLStreamReader getValueAsXMLStream()
                            throws XMLStreamException {
        XMLStreamReader xmlReader = dataElement.getFirstElement().getXMLStreamReaderWithoutCaching();
        xmlReader.next();
        return xmlReader;
    }

    @Override
    public String toString() {
        return super.toString() + " (EmbeddedComplexInput/ComplexData), value (element name)='"
               + dataElement.getQName() + "'" + ", mimeType='" + getMimeType() + "', encoding='" + getEncoding()
               + "', schema='" + getSchema() + "'";
    }
}
