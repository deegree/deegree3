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
package org.deegree.ogcwebservices.wfs.operation;

import java.util.Map;

import org.deegree.framework.util.KVP2Map;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.getcapabilities.GetCapabilities;
import org.deegree.ogcwebservices.wfs.WFService;
import org.w3c.dom.Element;

/**
 * Represents a GetCapabilities request to a web feature service.
 * <p>
 * The GetCapabilities request is used to query a capabilities document from a web feature service.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WFSGetCapabilities extends GetCapabilities {

    private static final long serialVersionUID = 3581485156939911513L;

    /**
     * Creates a new <code>WFSGetCapabilities</code> instance. with a wfs version of {@link WFService#VERSION}.
     *
     * @param id
     *            request identifier
     * @param updateSeq
     * @param acceptVersions
     * @param sections
     * @param acceptFormats
     * @param vendoreSpec
     */
    WFSGetCapabilities( String id, String updateSeq, String[] acceptVersions, String[] sections,
                        String[] acceptFormats, Map<String, String> vendoreSpec ) {
        this( id, WFService.VERSION, updateSeq, acceptVersions, sections, acceptFormats, vendoreSpec );
    }

    /**
     * Creates a new <code>WFSGetCapabilities</code> instance.
     *
     * @param id
     *            request identifier
     * @param version
     *            the version of the request. (e.g 1.0.0 or 1.1.0)
     * @param updateSeq
     * @param acceptVersions
     * @param sections
     * @param acceptFormats
     * @param vendoreSpec
     */
    WFSGetCapabilities( String id, String version, String updateSeq, String[] acceptVersions, String[] sections,
                        String[] acceptFormats, Map<String, String> vendoreSpec ) {
        super( id, version, updateSeq, acceptVersions, sections, acceptFormats, vendoreSpec );
    }

    /**
     * Creates a <code>WFSGetCapabilities</code> instance from a document that contains the DOM representation of the
     * request.
     *
     * @param id
     * @param root
     *            element that contains the DOM representation of the request
     * @return transaction instance
     * @throws OGCWebServiceException
     */
    public static WFSGetCapabilities create( String id, Element root )
                            throws OGCWebServiceException {
        WFSGetCapabilitiesDocument doc = new WFSGetCapabilitiesDocument();
        doc.setRootElement( root );
        WFSGetCapabilities request;
        try {
            request = doc.parse( id );
        } catch ( Exception e ) {
            throw new OGCWebServiceException( "WFSGetCapabilities", e.getMessage() );
        }
        return request;
    }

    /**
     * Creates a new <code>WFSGetCapabilities</code> instance from the given key-value pair encoded request.
     *
     * @param id
     *            request identifier
     * @param request
     * @return new <code>WFSGetCapabilities</code> request
     * @throws InvalidParameterValueException
     * @throws MissingParameterValueException
     */
    public static WFSGetCapabilities create( String id, String request )
                            throws MissingParameterValueException, InvalidParameterValueException {
        Map<String, String> map = KVP2Map.toMap( request );
        map.put( "ID", id );
        return create( map );
    }

    /**
     * Creates a new <code>WFSGetCapabilities</code> request from the given map.
     *
     * @param request
     * @return new <code>WFSGetCapabilities</code> request
     * @throws MissingParameterValueException
     * @throws InvalidParameterValueException
     */
    public static WFSGetCapabilities create( Map<String, String> request )
                            throws MissingParameterValueException, InvalidParameterValueException {

        String service = getRequiredParam( "SERVICE", request );
        if ( !service.equals( "WFS" ) ) {
            throw new InvalidParameterValueException( "WFSGetCapabilities", "Parameter 'service' must be 'WFS'." );
        }
        String version = request.get( "VERSION" );
        String av = request.get( "ACCEPTVERSIONS" );
        String[] acceptVersions = av == null ? null : av.split( "," );
        String sec = request.get( "SECTIONS" );
        String[] sections = sec == null ? null : sec.split( "," );
        String updateSequence = getParam( "UPDATESEQUENCE", request, "" );
        String af = request.get( "ACCEPTFORMATS" );
        String[] acceptFormats = af == null ? new String[] { "text/xml" } : af.split( "," );

        // TODO generate unique request id
        String id = null;
        if ( version == null || "".equals( version.trim() ) ) {
            return new WFSGetCapabilities( id, updateSequence, acceptVersions, sections, acceptFormats, request );
        }
        return new WFSGetCapabilities( id, version, updateSequence, acceptVersions, sections, acceptFormats, request );
    }

    /**
     * Returns the service name (WFS).
     *
     * @return the service name (WFS).
     */
    public String getServiceName() {
        return "WFS";
    }
}
