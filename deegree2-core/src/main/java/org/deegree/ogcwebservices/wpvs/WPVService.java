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

package org.deegree.ogcwebservices.wpvs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.trigger.TriggerProvider;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.wpvs.configuration.WPVSConfiguration;
import org.deegree.ogcwebservices.wpvs.operation.Get3DFeatureInfo;
import org.deegree.ogcwebservices.wpvs.operation.GetView;
import org.deegree.ogcwebservices.wpvs.operation.WPVSGetCapabilities;

/**
 * A <code>WPVService</code> deligates the clients Requests. If the rquest is an instance of
 * GetView a (configured) GetViewHandler is instantiated to create an perspective image.
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * $Revision$, $Date$
 *
 */
public class WPVService implements OGCWebService {

    private static final TriggerProvider TP = TriggerProvider.create( WPVService.class );

    private static final ILogger LOG = LoggerFactory.getLogger( WPVService.class );

    private WPVSConfiguration configuration;

    private ConcurrentHashMap<String, GetViewHandler> getViewHandlers;

    private String defaultSplitter;

    /**
     * Creates a new instance of <code>WPVService</code> from the configuration.
     *
     * @param configuration
     */
    WPVService( WPVSConfiguration configuration ) {
        this.configuration = configuration;
        HashMap<String, String> configuredHandlers = HandlerMapping.getConfiguredGetViewHandlers();
        Set<String> keys = configuredHandlers.keySet();
        defaultSplitter = configuration.getDeegreeParams().getDefaultSplitter();
        // if no defaulthandler in the configuration QUAD will be the Defaultsplitter
        getViewHandlers = new ConcurrentHashMap<String, GetViewHandler>();
        for ( String key : keys ) {
            try {
                getViewHandlers.put( key, createHandler( configuredHandlers.get( key ) ) );
            } catch ( OGCWebServiceException e ) {
                LOG.logError( e.getLocalizedMessage(), e );
            }
        }
        // an error occurred while instantiating or no handlers in the bundle or the defaultsplitter
        // was not in the bundle
        if ( getViewHandlers.isEmpty() || !getViewHandlers.containsKey( defaultSplitter ) ) {
            getViewHandlers.put( defaultSplitter, new DefaultGetViewHandler( this ) );
        }

    }

    /**
     * Returns the capabilities of this service.
     *
     * @return the capabilities, this is an instance of <code>WPVSCapabilities</code>
     */
    public OGCCapabilities getCapabilities() {
        return this.configuration;
    }

    /**
     * Performs the handling of the passed OGCWebServiceEvent directly and returns the result to the
     * calling class/ method.
     *
     * @param request
     *            WFS request to perform
     *
     * @throws OGCWebServiceException
     */
    public Object doService( OGCWebServiceRequest request )
                            throws OGCWebServiceException {

        request = (OGCWebServiceRequest) TP.doPreTrigger( this, request )[0];

        Object response = null;

        if ( request instanceof WPVSGetCapabilities ) {
            // TODO implement partial responses (if only certain sections are requested)
            response = getCapabilities();
        } else if ( request instanceof GetView ) {

            String splitter = ( (GetView) request ).getVendorSpecificParameter( "SPLITTER" );
            if ( splitter == null || splitter.trim().equals( "" ) )
                splitter = defaultSplitter;
            GetViewHandler gvh = getViewHandlers.get( splitter );
            if ( gvh == null ) {
                gvh = getViewHandlers.get( defaultSplitter );
            }
            // if ( "QUAD".equals( ) {
            // gvh = createHandler( HandlerMapping.getString( "WPVService.GETVIEW.QUAD" ) );
            // } else {
            // gvh = createHandler( HandlerMapping.getString( "WPVService.GETVIEW.BOX" ) );
            // }
            response = gvh.handleRequest( (GetView) request );

        } else if ( request instanceof Get3DFeatureInfo ) {
            Get3DFeatureInfoHandler get3DFeatureInfoHandler = new DefaultGet3DFeatureInfoHandler( this );
            response = get3DFeatureInfoHandler.handleRequest( (Get3DFeatureInfo) request );

        } else {

            throw new OGCWebServiceException( getClass().getName(), "Unknown request type: "
                                                                    + request.getClass().getName() );
        }

        return TP.doPostTrigger( this, response )[0];
    }

    @SuppressWarnings("unchecked")
    private GetViewHandler createHandler( String className )
                            throws OGCWebServiceException {

        // describes the signature of the required constructor
        // Class[] cl = new Class[1];
        // cl[0] = WPVService.class;

        // set parameter to submitt to the constructor
        // Object[] o = new Object[1];
        // o[0] = this;

        GetViewHandler handler = null;

        try {
            // get constructor
            Class creator = Class.forName( className );
            Constructor<GetViewHandler> con = creator.getConstructor( WPVService.class );

            // call constructor and instantiate a new DataStore
            handler = con.newInstance( this );
        } catch ( ClassCastException cce ) {
            throw new OGCWebServiceException( "The requested class " + className + " is not of type GetViewHandler! \n"
                                              + cce.toString() );
        } catch ( ClassNotFoundException cce ) {
            throw new OGCWebServiceException( "Couldn't instantiate " + className + "! \n" + cce.toString() );
        } catch ( NoSuchMethodException nsme ) {
            throw new OGCWebServiceException( "Couldn't instantiate " + className + "! \n" + nsme.toString() );
        } catch ( InstantiationException ie ) {
            throw new OGCWebServiceException( "Couldn't instantiate " + className + "! \n" + ie.toString() );
        } catch ( IllegalAccessException iae ) {
            throw new OGCWebServiceException( "Couldn't instantiate " + className + "! \n" + iae.toString() );
        } catch ( InvocationTargetException ite ) {
            throw (OGCWebServiceException) ite.getCause();
        } catch ( Exception e ) {
            throw new OGCWebServiceException( "Couldn't instantiate " + className + "! \n" + e.toString() );
        }

        return handler;
    }

    /**
     * Returns a GetViewHandler which is configured to handle the given kind of splitter.
     *
     * @param splitter
     *            the name of the splitter (supported are QUAD and BBOX)
     * @return the configured GetViewHandler or the default GetViewHandler if the given String is
     *         null or empty or not known.
     */
    public GetViewHandler getGetViewHandler( String splitter ) {
        if ( splitter == null || splitter.trim().equals( "" ) )
            return getViewHandlers.get( defaultSplitter );
        GetViewHandler gvh = getViewHandlers.get( splitter );
        if ( gvh == null ) {
            gvh = getViewHandlers.get( defaultSplitter );
        }
        return gvh;
    }

    /**
     * @return the configuration
     */
    protected WPVSConfiguration getConfiguration() {
        return configuration;
    }

}
