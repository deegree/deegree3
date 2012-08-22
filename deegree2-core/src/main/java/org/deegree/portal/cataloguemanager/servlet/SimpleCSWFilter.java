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
package org.deegree.portal.cataloguemanager.servlet;

import java.io.IOException;
import java.net.URI;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.deegree.datatypes.QualifiedName;
import org.deegree.enterprise.servlet.ServletRequestWrapper;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Expression;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyIsNullOperation;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.ogcwebservices.OGCRequestFactory;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.csw.discovery.GetRecords;
import org.deegree.ogcwebservices.csw.discovery.Query;
import org.deegree.ogcwebservices.csw.discovery.XMLFactory;
import org.deegree.ogcwebservices.csw.manager.Transaction;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SimpleCSWFilter implements Filter {

    private static ILogger LOG = LoggerFactory.getLogger( SimpleCSWFilter.class );

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init( FilterConfig config )
                            throws ServletException {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     * javax.servlet.FilterChain)
     */
    public void doFilter( ServletRequest req, ServletResponse res, FilterChain chain )
                            throws IOException, ServletException {

        String userName = null;
        if ( ( (HttpServletRequest) req ).getUserPrincipal() != null ) {
            userName = ( (HttpServletRequest) req ).getUserPrincipal().getName();
        }
        LOG.logInfo( "user name: ", userName );
        if ( "cmEditor".equals( userName ) || "cmAdmin".equals( userName ) ) {
            chain.doFilter( req, res );
        } else {
            ServletRequestWrapper requestWrapper = null;

            if ( req instanceof ServletRequestWrapper ) {
                LOG.logDebug( "OWSProxySerlvetFilter: the incoming request is actually an org.deegree.enterprise.servlet.RequestWrapper, so not creating new instance." );
                requestWrapper = (ServletRequestWrapper) req;
            } else {
                requestWrapper = new ServletRequestWrapper( (HttpServletRequest) req );
            }
            LOG.logDebug( "ConfigurableOWSProxyServletFilter: GetContentype(): " + requestWrapper.getContentType() );

            OGCWebServiceRequest owsReq = null;
            try {
                owsReq = OGCRequestFactory.create( requestWrapper );
            } catch ( OGCWebServiceException e ) {
                LOG.logError( "OWSProxyServletFilter: Couln't create an OGCWebserviceRequest because: "
                              + e.getMessage(), e );
                throw new ServletException( e.getMessage() );
            }
            if ( owsReq instanceof Transaction ) {
                throw new ServletException( "user: " + userName + " is not allowed to perform CSW transactions" );
            } else if ( owsReq instanceof GetRecords && !"cmUser".equals( userName ) && !"cmEditor".equals( userName )
                        && !"cmAdmin".equals( userName ) ) {
                //owsReq = addFilter( (GetRecords) owsReq );
            }

            try {
                XMLFragment doc = null;
                if ( owsReq instanceof Transaction ) {
                    doc = org.deegree.ogcwebservices.csw.manager.XMLFactory.export( (Transaction) owsReq );
                } else if ( owsReq instanceof GetRecords ) {
                    doc = XMLFactory.exportWithVersion( (GetRecords) owsReq );
                }
                if ( doc != null ) {
                    requestWrapper.setInputStreamAsByteArray( doc.getAsString().getBytes() );
                }
            } catch ( Exception e ) {
                throw new ServletException( e );
            }
            chain.doFilter( requestWrapper, res );
        }
    }

    private GetRecords addFilter( GetRecords casreq ) {
        Query query = casreq.getQuery();
        ComplexFilter qFilter = (ComplexFilter) query.getContraint();
        QualifiedName qn = new QualifiedName( "AccessConstraints",
                                              URI.create( "http://www.opengis.net/cat/csw/apiso/1.0" ) );
        Expression exp1 = new PropertyName( qn );

        Operation op = new PropertyIsNullOperation( (PropertyName) exp1 );
        org.deegree.model.filterencoding.Filter filter = new ComplexFilter( op );
        if ( qFilter instanceof ComplexFilter ) {
            filter = new ComplexFilter( qFilter, (ComplexFilter) filter, OperationDefines.AND );
        }

        // substitue query by a new one using the re-created filter
        query = new Query( query.getElementSetName(), query.getElementSetNameTypeNamesList(),
                           query.getElementSetNameVariables(), query.getElementNamesAsPropertyPaths(), filter,
                           query.getSortProperties(), query.getTypeNamesAsList(), query.getDeclaredTypeNameVariables() );

        casreq.setQuery( query );

        // if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
        // try {
        // XMLFactory.export( casreq ).prettyPrint( System.out );
        // } catch ( Exception e ) {
        // }
        // }
        try {
            XMLFactory.export( casreq ).prettyPrint( System.out );
        } catch ( Exception e ) {
        }
        return casreq;
    }
}
