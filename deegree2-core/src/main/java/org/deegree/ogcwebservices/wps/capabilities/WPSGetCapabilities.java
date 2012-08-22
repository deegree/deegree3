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
package org.deegree.ogcwebservices.wps.capabilities;

import java.util.Map;

import org.deegree.framework.util.KVP2Map;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OperationNotSupportedException;
import org.deegree.ogcwebservices.getcapabilities.GetCapabilities;
import org.w3c.dom.Element;

/**
 * WPSGetCapabilitiesRequest.java
 *
 *
 * Created on 08.03.2006. 22:15:18h
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @author last edited by: $Author:wanhoff$
 *
 * @version $Revision$, $Date:20.03.2007$
 */
public class WPSGetCapabilities extends GetCapabilities {

    /**
     *
     */
    private static final long serialVersionUID = -3610123737583333339L;

    /**
     *
     * @param id
     * @param version
     * @param updateSequence
     * @param acceptVersions
     * @param sections
     * @param acceptFormats
     * @param vendoreSpec
     */
    protected WPSGetCapabilities( String id, String version, String updateSequence, String[] acceptVersions,
                                  String[] sections, String[] acceptFormats, Map<String, String> vendoreSpec ) {
        super( id, version, updateSequence, acceptVersions, sections, acceptFormats, vendoreSpec );
    }

    /**
     * creates a <tt>WFSGetCapabilitiesRequest</tt> object.
     *
     * @param id
     *            id of the request
     * @param version
     * @param updateSequence
     * @param acceptVersions
     * @param sections
     * @param acceptFormats
     * @param vendoreSpec
     * @return the bean created from given strings.
     */
    public static WPSGetCapabilities create( String id, String version, String updateSequence, String[] acceptVersions,
                                             String[] sections, String[] acceptFormats, Map<String, String> vendoreSpec ) {
        return new WPSGetCapabilities( id, version, updateSequence, acceptVersions, sections, acceptFormats,
                                       vendoreSpec );
    }

    /**
     * creates a WPS GetCapabilities request class representation from a key-value-pair encoded request
     *
     * @param id
     * @param request
     * @return the bean created from the request
     * @throws InvalidParameterValueException
     * @throws MissingParameterValueException
     */
    public static WPSGetCapabilities create( String id, String request )
                            throws InvalidParameterValueException, MissingParameterValueException {
        Map<String, String> map = KVP2Map.toMap( request );
        map.put( "ID", id );
        return create( map );
    }

    /**
     * creates a WPS GetCapabilities request class representation form a key-value-pair encoded request
     *
     * @param request
     * @return the bean created from the given request
     * @throws InvalidParameterValueException
     * @throws MissingParameterValueException
     */
    public static WPSGetCapabilities create( Map<String, String> request )
                            throws InvalidParameterValueException, MissingParameterValueException {

        String id = request.remove( "ID" );
        String service = request.remove( "SERVICE" );
        if ( null == service ) {
            throw new MissingParameterValueException( "WPSGetCapabilities", "'service' parameter is missing" );
        }
        if ( !"WPS".equals( service ) ) {
            throw new InvalidParameterValueException( "WPSGetCapabilities", "service attribute must equal 'WPS'" );
        }
        String updateSeq = request.remove( "UPDATESEQUENCE" );
        String version = request.remove( "VERSION" );

        // accept versions, sections, accept formats not supported
        return new WPSGetCapabilities( id, version, updateSeq, null, null, null, request );
    }

    /**
     * XML-coded get capabilities request not supported.
     *
     * @see "OGC 05-007r4 Subclause 8.2"
     *
     * @param id
     * @param element
     * @return the bean created from the given xml document.
     * @throws OGCWebServiceException
     * @throws MissingParameterValueException
     * @throws InvalidParameterValueException
     */
    public static WPSGetCapabilities create( String id, Element element )
                            throws OGCWebServiceException {
        throw new OperationNotSupportedException(
                                                  "HTTP post transfer of XML encoded get capabilities request not supported." );

    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.ogcwebservices.OGCWebServiceRequest#getServiceName()
     */
    public String getServiceName() {
        return "WPS";
    }

}
