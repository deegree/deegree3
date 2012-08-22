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

package org.deegree.portal.owswatch;

import java.io.Serializable;
import java.util.Calendar;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.deegree.ogcwebservices.OGCWebServiceException;

/**
 * Executes the tests on a certain service. It also calls the validator and logs the result in the protocol file This
 * class extends Thread. On call of start() will execute the functionality of the class as described above
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ServiceInvoker extends Thread implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 8445955567617597483L;

    private ServiceConfiguration serviceConfig = null;

    private ServiceLog serviceLog = null;

    /**
     * @param serviceconfig
     * @param serviceLog
     */
    public ServiceInvoker( ServiceConfiguration serviceconfig, ServiceLog serviceLog ) {
        this.serviceConfig = serviceconfig;
        this.serviceLog = serviceLog;
    }

    /**
     * Executes the test in a separate thread
     *
     */
    public void executeTestThreaded() {
        start();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        executeTest();
    }

    /**
     * Executes the test in the main thread
     *
     */
    public void executeTest() {

        ValidatorResponse tmpResponse = null;
        try {
            HttpMethodBase method = serviceConfig.getHttpMethodBase();
            tmpResponse = executeHttpMethod( method );
            method.releaseConnection();
        } catch ( OGCWebServiceException e ) {
            tmpResponse = new ValidatorResponse( "Page Unavailable: " + e.getLocalizedMessage(),
                                                 Status.RESULT_STATE_PAGE_UNAVAILABLE );
            tmpResponse.setLastLapse( -1 );
            tmpResponse.setLastTest( Calendar.getInstance().getTime() );
        }

        serviceLog.addMessage( tmpResponse, serviceConfig );
    }

    /**
     * @param method
     * @return The response OGCWebServiceResponseData
     * @throws OGCWebServiceException
     */
    protected ValidatorResponse executeHttpMethod( HttpMethodBase method )
                            throws OGCWebServiceException {

        HttpClient client = new HttpClient();
        HttpConnectionManagerParams cmParams = client.getHttpConnectionManager().getParams();
        cmParams.setConnectionTimeout( serviceConfig.getTimeout() * 1000 );
        client.getHttpConnectionManager().setParams( cmParams );
        // Provide custom retry handler is necessary
        method.getParams().setParameter( HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler( 2, false ) );
        ValidatorResponse response = null;
        try {
            long startTime = System.currentTimeMillis();
            int statusCode = client.executeMethod( method );
            long lapse = System.currentTimeMillis() - startTime;
            response = serviceConfig.getValidator().validateAnswer( method, statusCode );

            response.setLastLapse( lapse );
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis( startTime );
            response.setLastTest( date.getTime() );
        } catch ( Exception e ) {
            throw new OGCWebServiceException( e.getLocalizedMessage() );
        } finally {
            method.releaseConnection();
        }
        return response;
    }

    /**
     * @return ServiceConfiguration
     */
    public ServiceConfiguration getServiceConfig() {
        return serviceConfig;
    }

    /**
     * @return ServiceLog
     */
    public ServiceLog getServiceLog() {
        return serviceLog;
    }
}
