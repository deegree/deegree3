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
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.services.jaxb.wps.ComplexFormatType;
import org.deegree.services.jaxb.wps.ComplexInputDefinition;

/**
 * A {@link ComplexInputImpl} with a value that is given as a reference to a web-accessible resource.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class ReferencedComplexInput extends ComplexInputImpl {

    private InputReference reference;

    /**
     * Creates a new {@link ComplexInputImpl} instance from a reference to a web-accessible URI.
     *
     * @param definition
     *            corresponding input definition from process description
     * @param title
     *            optional title supplied with the input parameter, may be null
     * @param summary
     *            optional narrative description supplied with the input parameter, may be null
     * @param format
     *            the XML schema, format, and encoding of the complex value
     * @param reference
     *            provides information for retrieving the input value via the web
     */
    public ReferencedComplexInput( ComplexInputDefinition definition, LanguageString title, LanguageString summary,
                                   ComplexFormatType format, InputReference reference ) {
        super( definition, title, summary, format );
        this.reference = reference;
    }

    @Override
    public OMElement getValueAsElement()
                            throws IOException {
        return new XMLAdapter( getValueAsBinaryStream() ).getRootElement();
    }

    @Override
    public InputStream getValueAsBinaryStream()
                            throws IOException {
        return reference.openStream();
    }

    @Override
    public XMLStreamReader getValueAsXMLStream()
                            throws IOException {
        XMLStreamReader reader = null;
        try {
            reader = XMLInputFactory.newInstance().createXMLStreamReader( getValueAsBinaryStream() );
            reader.next();
        } catch ( Exception e ) {
            throw new IOException( e.getMessage(), e );
        }
        return new XMLStreamReaderWrapper( reader, XMLAdapter.DEFAULT_URL );
    }

    /**
     * Returns the URL that provides the input.
     *
     * @return the URL that provides the input.
     */
    public URL getURL() {
        return reference.getURL();
    }

    @Override
    public String toString() {
        return super.toString() + " (ReferencedComplexInput/Reference), inputReference=" + reference + ", mimeType='"
               + getMimeType() + "', encoding='" + getEncoding() + "', schema='" + getSchema() + "'";
    }
}
