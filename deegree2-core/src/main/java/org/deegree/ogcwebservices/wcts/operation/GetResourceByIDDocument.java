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

package org.deegree.ogcwebservices.wcts.operation;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wcts.WCTService;
import org.w3c.dom.Element;

/**
 * <code>GetResourceByIDDocument</code> creates a bean-representation of a xml-dom GetRepository item request.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class GetResourceByIDDocument extends org.deegree.owscommon_1_1_0.operations.GetResourceByIDDocument {

    private static ILogger LOG = LoggerFactory.getLogger( GetResourceByIDDocument.class );
    private static final long serialVersionUID = 2724928639257998459L;
    private final GetResourceByID resourceById;


    /**
     * @param id of the request
     * @param rootElement of the request
     * @throws OGCWebServiceException if a mandatory item is missing, or the element could not be parsed.
     * @throws IllegalArgumentException if the rootElement is <code>null</code>
     */
    public GetResourceByIDDocument( String id, Element rootElement ) throws OGCWebServiceException, IllegalArgumentException {
        if( rootElement == null ){
            throw new IllegalArgumentException( Messages.getMessage( "WCTS_ROOT_ELEMENT_NOT_SET" ) );
        }
        setRootElement( rootElement );
        try {
            String service = parseService();
            if ( !"WCTS".equalsIgnoreCase( service ) ) {
                throw new OGCWebServiceException( Messages.getMessage( "WCTS_ILLEGAL_SERVICE" ),
                                                  ExceptionCode.INVALIDPARAMETERVALUE );
            }
            String version = parseVersion();
            if ( !WCTService.version.equalsIgnoreCase( version ) ) {
                throw new OGCWebServiceException( Messages.getMessage( "WCTS_ILLEGAL_VERSION", WCTService.version ),
                                                  ExceptionCode.INVALIDPARAMETERVALUE );
            }

            resourceById = new GetResourceByID( version,id, parseResourceIDs(), parseOutputFormats() );
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( e.getMessage(), ExceptionCode.MISSINGPARAMETERVALUE );
        }

    }

    /**
     * @return the resourceById bean.
     */
    public final GetResourceByID getResourceById() {
        return resourceById;
    }



}
