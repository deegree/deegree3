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

package org.deegree.services.wps.output;

import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Identifies this {@link ProcessletOutput} to have a complex value, i.e. an object encoded in XML or a raw binary
 * stream.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public interface ComplexOutput extends ProcessletOutput {

    /**
     * Returns a stream for writing binary output.
     * 
     * @return stream for writing binary output, never <code>null</code>
     */
    public OutputStream getBinaryOutputStream();

    /**
     * Returns a stream for for writing XML output. The stream is already initialized with a
     * {@link XMLStreamWriter#writeStartDocument()}.
     * 
     * @return a stream for writing XML output, never <code>null</code>
     * @throws XMLStreamException
     */
    public XMLStreamWriter getXMLStreamWriter()
                            throws XMLStreamException;

    /**
     * Returns the requested mime type for the complex value, it is guaranteed that the mime type is supported for this
     * parameter (according to the process description).
     * 
     * @return the requested mime type, never <code>null</code> (as each complex output format has a default mime type)
     */
    public String getRequestedMimeType();

    /**
     * Returns the requested XML format for the complex value (specified by a schema URL), it is guaranteed that the
     * format is supported for this parameter (according to the process description).
     * 
     * @return the requested schema (XML format), may be <code>null</code> (as a complex output format may omit schema
     *         information)
     */
    public String getRequestedSchema();

    /**
     * Returns the requested encoding for the complex value, it is guaranteed that the encoding is supported for this
     * parameter (according to the process description).
     * 
     * @return the requested encoding, may be <code>null</code> (as a complex output format may omit encoding
     *         information)
     */
    public String getRequestedEncoding();
}
