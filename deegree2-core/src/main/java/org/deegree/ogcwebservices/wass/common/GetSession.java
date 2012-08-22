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

package org.deegree.ogcwebservices.wass.common;

import static org.deegree.i18n.Messages.get;

import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.w3c.dom.Element;

/**
 * Encapsulated data: GetSession element
 *
 * Namespace: http://www.gdi-nrw.de/session
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class GetSession extends AbstractRequest {

    private static final long serialVersionUID = 2437405317472796643L;

    /**
     * The logger enhances the quality and simplicity of Debugging within the deegree2 framework
     */
    private static final ILogger LOG = LoggerFactory.getLogger( GetSession.class );

    private AuthenticationData authenticationData = null;

    /**
     * Constructs new one from the given values.
     *
     * @param id
     *            the request id
     * @param service
     * @param version
     * @param authenticationData
     *
     */
    public GetSession( String id, String service, String version, AuthenticationData authenticationData ) {
        super( id, version, service, "GetSession" );
        this.authenticationData = authenticationData;
    }

    /**
     * Constructs new one from the given key-value-pairs.
     *
     * @param id
     *            the request id
     * @param kvp
     *            the map
     * @throws OGCWebServiceException
     */
    public GetSession( String id, Map<String, String> kvp ) throws OGCWebServiceException {
        super( id, kvp );
        URN method = new URN( kvp.get( "AUTHMETHOD" ) );
        String cred = kvp.get( "CREDENTIALS" );
        if ( !method.isWellformedGDINRW() ) {
            throw new OGCWebServiceException( get( "WASS_WRONG_AUTHMETHOD" ) );
        }
        if ( cred == null || cred.length() == 0 ) {
            throw new OGCWebServiceException( get( "WASS_NO_CREDENTIALS" ) );
        }

        authenticationData = new AuthenticationData( method, cred );
    }

    /**
     * @return Returns the authenticationData.
     */
    public AuthenticationData getAuthenticationData() {
        return authenticationData;
    }

    /**
     * @param id
     * @param documentElement
     * @return a new instance of this class
     * @throws OGCWebServiceException
     */
    public static OGCWebServiceRequest create( String id, Element documentElement )
                            throws OGCWebServiceException {
        try {
            return new SessionOperationsDocument().parseGetSession( id, documentElement );
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( e.getMessage() );
        }
    }

    /**
     * @param id
     * @param kvp
     * @return a new instance of this class
     * @throws OGCWebServiceException
     */
    public static OGCWebServiceRequest create( String id, Map<String, String> kvp )
                            throws OGCWebServiceException {
        kvp.put( "SERVICE", "WAS" );
        return new GetSession( id, kvp );
    }

}
