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
package org.deegree.protocol.wps.client.process;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.protocol.wps.client.WPSClient;
import org.deegree.protocol.wps.client.input.BBoxInput;
import org.deegree.protocol.wps.client.input.BinaryInput;
import org.deegree.protocol.wps.client.input.ExecutionInput;
import org.deegree.protocol.wps.client.input.LiteralInput;
import org.deegree.protocol.wps.client.input.XMLInput;

/**
 * Abstract base clase for {@link Process} execution contexts.
 * <p>
 * NOTE: This class is not thread-safe.
 * </p>
 * 
 * @see Process
 * @see ProcessExecution
 * @see RawProcessExecution
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class AbstractProcessExecution {

    /** Associated WPS client instance. */
    protected final WPSClient client;

    /** Associated process instance. */
    protected final Process process;

    /** List of inputs, may be empty, but never <code>null</code> */
    protected final List<ExecutionInput> inputs = new ArrayList<ExecutionInput>();

    /**
     * Creates a new {@link AbstractProcessExecution} instance.
     * 
     * @param client
     *            associated WPS client instance, must not be <code>null</code>
     * @param process
     *            associated process instance, must not be <code>null</code>
     */
    protected AbstractProcessExecution( WPSClient client, Process process ) {
        this.client = client;
        this.process = process;
    }

    /**
     * Adds a literal input parameter.
     * 
     * @param id
     *            identifier of the input parameter, must not be <code>null</code>
     * @param idCodeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (for identifiers without codespace)
     * @param value
     *            value of the literal input, must not be <code>null</code>
     * @param type
     *            data type in which the value should be considered, may be <code>null</code> (this means it matches the
     *            data type as defined by the process description)
     * @param uom
     *            unit of measure of the value, may be <code>null</code> (this means it matches the data type as defined
     *            by the process description)
     */
    public void addLiteralInput( String id, String idCodeSpace, String value, String type, String uom ) {
        inputs.add( new LiteralInput( new CodeType( id, idCodeSpace ), value, type, uom ) );
    }

    /**
     * Adds a bounding box input parameter.
     * 
     * @param id
     *            identifier of the input parameter, must not be <code>null</code>
     * @param idCodeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (for identifiers without codespace)
     * @param lower
     *            coordinates of the lower point, must not be <code>null</code>
     * @param upper
     *            coordinates of the upper point, must not be <code>null</code> and length must match lower point
     * @param crs
     *            coordinate system, may be <code>null</code> (indicates that the default crs from the parameter
     *            description applies)
     */
    public void addBBoxInput( String id, String idCodeSpace, double[] lower, double[] upper, String crs ) {
        inputs.add( new BBoxInput( new CodeType( id, idCodeSpace ), lower, upper, crs ) );
    }

    /**
     * Adds an XML-valued complex input parameter.
     * 
     * @param id
     *            identifier of the input parameter, must not be <code>null</code>
     * @param idCodeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (for identifiers without codespace)
     * @param url
     *            {@link URL} reference to the xml resource, must not be <code>null</code>
     * @param byRef
     *            if true, the parameter will be passed by reference to the server, otherwise it will be nested in the
     *            Execute request. If true, the url needs to be web-accessible (e.g. not a file URL)
     * @param mimeType
     *            mime type, may be <code>null</code> (indicates that the default mime type from the parameter
     *            description applies)
     * @param encoding
     *            encoding, may be <code>null</code> (indicates that the default encoding from the parameter description
     *            applies)
     * @param schema
     *            schema, may be <code>null</code> (indicates that the default schema from the parameter description
     *            applies)
     */
    public void addXMLInput( String id, String idCodeSpace, URL url, boolean byRef, String mimeType, String encoding,
                             String schema ) {
        inputs.add( new XMLInput( new CodeType( id, idCodeSpace ), url, byRef, mimeType, encoding, schema ) );
    }

    /**
     * Adds an XML-valued complex input parameter.
     * 
     * @param id
     *            identifier of the input parameter, must not be <code>null</code>
     * @param idCodeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (for identifiers without codespace)
     * @param reader
     *            {@link XMLStreamReader} to the xml data, must not be <code>null</code> and point to the START_ELEMENT
     *            event
     * @param mimeType
     *            mime type, may be <code>null</code> (indicates that the default mime type from the parameter
     *            description applies)
     * @param encoding
     *            encoding, may be <code>null</code> (indicates that the default encoding from the parameter description
     *            applies)
     * @param schema
     *            schema, may be <code>null</code> (indicates that the default schema from the parameter description
     *            applies)
     */
    public void addXMLInput( String id, String idCodeSpace, XMLStreamReader reader, String mimeType, String encoding,
                             String schema ) {
        inputs.add( new XMLInput( new CodeType( id, idCodeSpace ), reader, mimeType, encoding, schema ) );
    }

    /**
     * Adds a binary-valued complex input parameter.
     * 
     * @param id
     *            identifier of the input parameter, must not be <code>null</code>
     * @param idCodeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (for identifiers without codespace)
     * @param url
     *            {@link URL} reference to the binary resource, must not be <code>null</code> (and must not be
     *            web-accessible)
     * @param byRef
     *            if true, the parameter will be passed by reference to the server, otherwise it will be nested in the
     *            Execute request. If true, the url needs to be web-accessible (e.g. not a file URL)
     * @param mimeType
     *            mime type, may be <code>null</code> (indicates that the default mime type from the parameter
     *            description applies)
     * @param encoding
     *            encoding, may be <code>null</code> (indicates that the default encoding from the parameter description
     *            applies)
     */
    public void addBinaryInput( String id, String idCodeSpace, URL url, boolean byRef, String mimeType, String encoding ) {
        inputs.add( new BinaryInput( new CodeType( id, idCodeSpace ), url, byRef, mimeType, encoding ) );
    }

    /**
     * Adds a binary-valued complex input parameter.
     * 
     * @param id
     *            identifier of the input parameter, must not be <code>null</code>
     * @param idCodeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (for identifiers without codespace)
     * @param inputStream
     *            input stream to the binary data, must not be <code>null</code>
     * @param mimeType
     *            mime type, may be <code>null</code> (indicates that the default mime type from the parameter
     *            description applies)
     * @param encoding
     *            encoding, may be <code>null</code> (indicates that the default encoding from the parameter description
     *            applies)
     */
    public void addBinaryInput( String id, String idCodeSpace, InputStream inputStream, String mimeType, String encoding ) {
        inputs.add( new BinaryInput( new CodeType( id, idCodeSpace ), inputStream, mimeType, encoding ) );
    }
}