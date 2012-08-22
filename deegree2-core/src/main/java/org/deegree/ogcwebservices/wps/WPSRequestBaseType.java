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
package org.deegree.ogcwebservices.wps;

import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.ogcwebservices.AbstractOGCWebServiceRequest;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;

/**
 * WPSRequestBaseType.java
 *
 * Created on 09.03.2006. 22:47:16h
 *
 * WPS operation request base, for all WPS operations except GetCapabilities. In this XML encoding,
 * no "request" parameter is included, since the element name specifies the specific operation.
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @version 1.0
 * @since 2.0
 */

public class WPSRequestBaseType extends AbstractOGCWebServiceRequest {

    /**
     *
     */
    private static final long serialVersionUID = -7671779606604745604L;

    /**
     * Service type identifier.
     */
    protected static final String service = "WPS";

    /**
     * Version identifier.
     */
    protected static final String supportedVersion = "0.4.0";

    private static final ILogger LOG = LoggerFactory.getLogger( WPSRequestBaseType.class );

    /**
     * @param version
     * @param id
     * @param vendorSpecificParameter
     */
    public WPSRequestBaseType( String version, String id, Map<String, String> vendorSpecificParameter ) {
        super( version, id, vendorSpecificParameter );
    }

    /**
     * @return Returns the version.
     */
    @Override
    public String getVersion() {
        return supportedVersion;
    }

    /**
     *
     * @param request
     * @return the version parameter.
     * @throws MissingParameterValueException
     * @throws InvalidParameterValueException
     */
    protected static String extractVersionParameter( Map<String, String> request )
                            throws MissingParameterValueException, InvalidParameterValueException {
        String version = request.get( "VERSION" );
        if ( null == version ) {
            String msg = "Version parameter must be set.";
            LOG.logError( msg );
            throw new MissingParameterValueException( "version", msg );
        } else if ( "".equals( version ) ) {
            String msg = "Version parameter must not be empty.";
            LOG.logError( msg );
            throw new InvalidParameterValueException( "version", msg );
        } else if ( !supportedVersion.equals( version ) ) {
            String msg = "Only version 0.4.0 is currently supported by this wpserver instance";
            LOG.logError( msg );
            throw new InvalidParameterValueException( "version", msg );
        }
        return version;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.ogcwebservices.OGCWebServiceRequest#getServiceName()
     */
    public String getServiceName() {
        return service;
    }
}
