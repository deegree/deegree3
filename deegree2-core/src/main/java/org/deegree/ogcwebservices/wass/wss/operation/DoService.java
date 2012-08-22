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

package org.deegree.ogcwebservices.wass.wss.operation;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wass.common.AbstractRequest;
import org.deegree.ogcwebservices.wass.common.AuthenticationData;
import org.deegree.ogcwebservices.wass.common.URN;
import org.w3c.dom.Element;

/**
 * The <code>DoService</code> class represents (a bean) a DoService Operation which is send by a client (or other
 * server) which is checked by the wss for the right credentials and than send to the requested
 * serviceprovider. In the case that a client not has the right credentials a ServiceException is
 * thrown. The Specification does mention the fact that ther might be another response for example:
 * A client orders A and B but only has the credentials for A -> should we return A and not B or
 * nothing at all. We do the last, the client gets nothing.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class DoService extends AbstractRequest {

    private static final long serialVersionUID = -8538267299180579690L;

    /**
     * The logger enhances the quality and simplicity of Debugging within the deegree2 framework
     */
    private static final ILogger LOG = LoggerFactory.getLogger( DoService.class );

    private AuthenticationData authenticationData = null;

    private String dcp = null;

    private ArrayList<RequestParameter> requestParameters = null;

    private String payload = null;

    private URI facadeURL = null;

    /**
     * @param id the request id
     * @param service
     * @param version
     * @param authenticationData
     * @param dcp
     * @param requestParameters
     * @param payload
     * @param facadeURL
     */
    public DoService( String id, String service, String version, AuthenticationData authenticationData,
                     String dcp, ArrayList<RequestParameter> requestParameters, String payload,
                     URI facadeURL ) {
        super( id, version, service, "DoService" );
        this.authenticationData = authenticationData;
        this.dcp = dcp;
        this.requestParameters = requestParameters;
        this.payload = payload;
        this.facadeURL = facadeURL;
    }

    /**
     * @param id the request id
     * @param keyValues
     */
    public DoService( String id, Map<String, String> keyValues ) {
        super( id, keyValues );

        LOG.logDebug( keyValues.toString() );

        this.authenticationData = new AuthenticationData( new URN( keyValues.get( "AUTHMETHOD" ) ),
                                                          keyValues.get( "CREDENTIALS" ) );
        this.dcp = keyValues.get( "DCP" );
        this.payload = keyValues.get( "SERVICEREQUEST" );
        try {
            this.facadeURL = new URI( keyValues.get( "FACADEURL" ) );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }

        requestParameters = new ArrayList<RequestParameter>();

        String requestParams = keyValues.get( "REQUESTPARAMS" );
        List<String> params = StringTools.toList( requestParams, ",", false );

        String requestParamValues = keyValues.get( "REQUESTPARAMVALUES" );
        List<String> paramValues = StringTools.toList( requestParamValues, ",", false );

        for ( int i = 0; i < params.size(); ++i ) {
            this.requestParameters.add( new RequestParameter( params.get( i ), paramValues.get( i ) ) );
        }

    }

    /**
     * @return Returns the authenticationData.
     */
    public AuthenticationData getAuthenticationData() {
        return authenticationData;
    }

    /**
     * @return Returns the dcp.
     */
    public String getDcp() {
        return dcp;
    }

    /**
     * @return Returns the facadeURL.
     */
    public URI getFacadeURL() {
        return facadeURL;
    }

    /**
     * @return Returns the payload.
     */
    public String getPayload() {
        return payload;
    }

    /**
     * @return Returns the requestParameters.
     */
    public ArrayList<RequestParameter> getRequestParameters() {
        return requestParameters;
    }

    /**
     * @param id
     * @param documentElement
     * @return a new instance of this class
     * @throws OGCWebServiceException
     */
    public static OGCWebServiceRequest create( String id, Element documentElement ) throws OGCWebServiceException {
        try {
            return new DoServiceDocument().parseDoService( id, documentElement );
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( e.getMessage() );
        }
    }

    /**
     * @param id
     * @param kvp
     * @return a new instance of this class
     */
    public static OGCWebServiceRequest create( String id, Map<String, String> kvp ) {
        return new DoService( id, kvp );
    }
}
