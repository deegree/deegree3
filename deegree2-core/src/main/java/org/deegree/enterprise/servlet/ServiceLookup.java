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
package org.deegree.enterprise.servlet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.deegree.enterprise.ServiceException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

/**
 * look up class for deegree service handler. The class is implemented as singleton to ensure that
 * only one instance and so just ine mapping is valid with a service instance
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class ServiceLookup {

    private static final ILogger LOG = LoggerFactory.getLogger( ServiceLookup.class );

    private static ServiceLookup lookup = null;


    private static Map<String, Class<?>> services;

    /**
     * @return static wrapper for a private constructor
     */
    public static ServiceLookup getInstance() {
        if ( lookup == null ) {
            lookup = new ServiceLookup();
        }
        return lookup;
    }

    private ServiceLookup() {
        services = new HashMap<String, Class<?>>();
    }

    /**
     * Maps a web service to the given class
     *
     * @param key
     * @param clss
     */
    public void addService( String key, Class<?> clss ) {
        services.put( key, clss );
    }

    /**
     * @param key
     *            to be removed
     * @return the handler class to which the key is mapped
     */
    public Class<?> removeService( String key ) {
        return services.remove( key );
    }

    /**
     * @param key
     *            the webservice of interest
     * @return its handler class
     */
    public Class<?> getService( String key ) {
        return services.get( key );
    }

    /**
     * @return an iterator over all the webservices names.
     */
    public Iterator<String> getIterator() {
        return services.keySet().iterator();
    }

    /**
     * @param serviceName
     * @param requestIP
     * @return the Handler of the webservice
     * @throws ServiceException
     */
    public ServiceDispatcher getHandler( String serviceName, String requestIP )
                            throws ServiceException {
        Class<?> handlerClass = services.get( serviceName );

        if ( handlerClass == null ) {
            throw new ServiceException( "Unknown service handler for " + "requested service:" //$NON-NLS-1$ //$NON-NLS-2$
                                        + serviceName );
        }
        ServiceDispatcher handler = null;
        try {
            handler = (ServiceDispatcher) handlerClass.newInstance();
            // Very dirty hack to get the ip to the handler.
            if ( handler != null ) {
                if ( "WCTS".equalsIgnoreCase( serviceName ) ) {
                    ( (WCTSHandler) handler ).setIP( requestIP );
                }
            }
            LOG.logDebug( "Dispatching request to handler '" + handlerClass.getName() + "'." ); //$NON-NLS-1$ //$NON-NLS-2$
        } catch ( Exception e ) {
            LOG.logError( "Can't initialize OGC service:" + serviceName, e ); //$NON-NLS-1$
            throw new ServiceException( "Can't initialize OGC service:" + serviceName ); //$NON-NLS-1$
        }
        return handler;
    }

    /**
     * Clear the map
     */
    public void clear() {
        services.clear();
    }

}
