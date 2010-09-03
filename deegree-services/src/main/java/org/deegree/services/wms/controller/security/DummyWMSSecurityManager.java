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
package org.deegree.services.wms.controller.security;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.protocol.ows.capabilities.GetCapabilities;
import org.deegree.protocol.wms.WMSConstants.WMSRequestType;
import org.deegree.services.authentication.InvalidCredentialsException;
import org.deegree.services.authentication.SecurityException;
import org.deegree.services.controller.Credentials;
import org.deegree.services.wms.controller.ops.GetFeatureInfo;
import org.deegree.services.wms.controller.ops.GetFeatureInfoSchema;
import org.deegree.services.wms.controller.ops.GetLegendGraphic;
import org.deegree.services.wms.controller.ops.GetMap;
import org.slf4j.Logger;

/**
 * Dummy implementation of {@link WMSSecurityManager} to handle incoming requests with a security barrier. <br>
 * There are three Users with different operations responsible to reach. But this is the rightsManagement, actually, and
 * should be moved to a backend later.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(debug = "logs general information when processing security")
public class DummyWMSSecurityManager implements WMSSecurityManager {

    private static final Logger LOG = getLogger( DummyWMSSecurityManager.class );

    private static List<WMSRequestType> WMS_OPERATIONS_1 = new ArrayList<WMSRequestType>( 3 );

    private static List<WMSRequestType> WMS_OPERATIONS_2 = new ArrayList<WMSRequestType>( 2 );

    private static List<WMSRequestType> WMS_OPERATIONS_3 = new ArrayList<WMSRequestType>();

    private static Map<Credentials, List<WMSRequestType>> credRepository = new HashMap<Credentials, List<WMSRequestType>>(
                                                                                                                           3 );

    private int counter;

    private static final int authenticationTrys = 1;

    static {

        WMS_OPERATIONS_1.add( WMSRequestType.GetMap );
        WMS_OPERATIONS_1.add( WMSRequestType.GetCapabilities );
        WMS_OPERATIONS_1.add( WMSRequestType.GetFeatureInfo );

        WMS_OPERATIONS_2.add( WMSRequestType.GetFeatureInfo );

        for ( WMSRequestType req : WMSRequestType.values() ) {
            WMS_OPERATIONS_3.add( req );
        }

        credRepository.put( new Credentials( "User1", "pass1" ), WMS_OPERATIONS_1 );
        credRepository.put( new Credentials( "User2", "pass2" ), WMS_OPERATIONS_2 );
        credRepository.put( new Credentials( "User3", "pass3" ), WMS_OPERATIONS_3 );
    }

    /**
     * Creates a new instance of {@link DummyWMSSecurityManager}.
     */
    public DummyWMSSecurityManager() {
        // working process, will be modified in the future
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.services.controller.wms.security.WMSSecurityManager#preprocess(org.deegree.services.controller.wms
     * .ops.GetMap, org.deegree.services.controller.Credentials)
     */
    @Override
    public GetMap preprocess( GetMap getMap, Credentials creds )
                            throws SecurityException {

        boolean fit = false;
        if ( creds != null ) {

            for ( Credentials s : credRepository.keySet() ) {

                LOG.debug( "" + s.getUser() + " " + s.getPassword() );
                if ( s.getUser().equals( ( creds.getUser() ) ) && s.getPassword().equals( creds.getPassword() ) ) {
                    LOG.debug( "there are credentials available" );
                    for ( WMSRequestType op : credRepository.get( s ) ) {
                        if ( op.equals( WMSRequestType.GetMap ) || op.equals( WMSRequestType.map ) ) {
                            fit = true;
                            // set the counter back to 0
                            counter = 0;
                            return getMap;
                        }
                    }

                } else {

                    fit = false;
                }

            }

        } else {
            counter = 0;
        }

        if ( counter == authenticationTrys ) {
            LOG.debug( "ForbiddenException" );
            // set the counter back to 0
            counter = 0;
            throw new InvalidCredentialsException();
        }
        counter++;

        if ( fit == false ) {
            LOG.debug( "SecurityException" );
            throw new SecurityException();
        }

        return null;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.services.controller.wms.security.WMSSecurityManager#preprocess(org.deegree.services.controller.wms
     * .ops.GetFeatureInfo, org.deegree.services.controller.Credentials)
     */
    @Override
    public GetFeatureInfo preprocess( GetFeatureInfo gfi, Credentials creds )
                            throws SecurityException {
        boolean fit = false;
        if ( creds != null ) {

            for ( Credentials s : credRepository.keySet() ) {

                LOG.debug( "" + s.getUser() + " " + s.getPassword() );
                if ( s.getUser().equals( ( creds.getUser() ) ) && s.getPassword().equals( creds.getPassword() ) ) {
                    LOG.debug( "there are credentials available" );
                    for ( WMSRequestType op : credRepository.get( s ) ) {
                        if ( op.equals( WMSRequestType.GetFeatureInfo ) ) {
                            fit = true;
                            // set the counter back to 0
                            counter = 0;
                            LOG.debug( "counterYES: " + counter );
                            return gfi;
                        }
                    }

                } else {

                    fit = false;
                }

            }

        } else {
            counter = 0;
            LOG.debug( "counterNO: " + counter );
        }

        if ( counter == authenticationTrys ) {
            LOG.debug( "ForbiddenException" );
            // set the counter back to 0
            counter = 0;
            throw new InvalidCredentialsException();
        }
        counter++;
        LOG.debug( "counterNO/YES: " + counter );

        if ( fit == false ) {
            LOG.debug( "SecurityException" );
            throw new SecurityException();
        }

        return null;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.services.controller.wms.security.WMSSecurityManager#preprocess(org.deegree.protocol.ows.capabilities
     * .GetCapabilities, org.deegree.services.controller.Credentials)
     */
    @Override
    public GetCapabilities preprocess( GetCapabilities getCapas, Credentials creds )
                            throws SecurityException {

        boolean fit = false;
        if ( creds != null ) {

            for ( Credentials s : credRepository.keySet() ) {

                LOG.debug( "" + s.getUser() + " " + s.getPassword() );
                if ( s.getUser().equals( ( creds.getUser() ) ) && s.getPassword().equals( creds.getPassword() ) ) {
                    LOG.debug( "there are credentials available" );
                    for ( WMSRequestType op : credRepository.get( s ) ) {
                        if ( op.equals( WMSRequestType.GetCapabilities ) || op.equals( WMSRequestType.capabilities ) ) {
                            fit = true;
                            // set the counter back to 0
                            counter = 0;
                            LOG.debug( "counterYES: " + counter );
                            return getCapas;
                        }
                    }

                } else {

                    fit = false;
                }

            }

        } else {
            counter = 0;
            LOG.debug( "counterNO: " + counter );
        }

        if ( counter == authenticationTrys ) {
            LOG.debug( "ForbiddenException" );
            // set the counter back to 0
            counter = 0;
            throw new InvalidCredentialsException();
        }
        counter++;
        LOG.debug( "counterNO/YES: " + counter );

        if ( fit == false ) {
            LOG.debug( "SecurityException" );
            throw new SecurityException();
        }

        return null;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.services.controller.wms.security.WMSSecurityManager#preprocess(org.deegree.services.controller.wms
     * .ops.GetLegendGraphic, org.deegree.services.controller.Credentials)
     */
    @Override
    public GetLegendGraphic preprocess( GetLegendGraphic glg, Credentials creds )
                            throws SecurityException {
        boolean fit = false;
        if ( creds != null ) {

            for ( Credentials s : credRepository.keySet() ) {

                LOG.debug( "" + s.getUser() + " " + s.getPassword() );
                if ( s.getUser().equals( ( creds.getUser() ) ) && s.getPassword().equals( creds.getPassword() ) ) {
                    LOG.debug( "there are credentials available" );
                    for ( WMSRequestType op : credRepository.get( s ) ) {
                        if ( op.equals( WMSRequestType.GetLegendGraphic ) ) {
                            fit = true;
                            // set the counter back to 0
                            counter = 0;
                            LOG.debug( "counterYES: " + counter );
                            return glg;
                        }
                    }

                } else {

                    fit = false;
                }

            }

        } else {
            counter = 0;
            LOG.debug( "counterNO: " + counter );
        }

        if ( counter == authenticationTrys ) {
            LOG.debug( "ForbiddenException" );
            // set the counter back to 0
            counter = 0;
            throw new InvalidCredentialsException();
        }
        counter++;
        LOG.debug( "counterNO/YES: " + counter );

        if ( fit == false ) {
            LOG.debug( "SecurityException" );
            throw new SecurityException();
        }

        return null;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.services.controller.wms.security.WMSSecurityManager#preprocess(org.deegree.services.controller.wms
     * .ops.GetFeatureInfoSchema, org.deegree.services.controller.Credentials)
     */
    @Override
    public GetFeatureInfoSchema preprocess( GetFeatureInfoSchema getFeatureInfoSchema, Credentials creds )
                            throws java.lang.SecurityException {
        boolean fit = false;
        if ( creds != null ) {

            for ( Credentials s : credRepository.keySet() ) {

                LOG.debug( "" + s.getUser() + " " + s.getPassword() );
                if ( s.getUser().equals( ( creds.getUser() ) ) && s.getPassword().equals( creds.getPassword() ) ) {
                    LOG.debug( "there are credentials available" );
                    for ( WMSRequestType op : credRepository.get( s ) ) {
                        if ( op.equals( WMSRequestType.GetFeatureInfoSchema ) ) {
                            fit = true;
                            // set the counter back to 0
                            counter = 0;
                            LOG.debug( "counterYES: " + counter );
                            return getFeatureInfoSchema;
                        }
                    }

                } else {

                    fit = false;
                }

            }

        } else {
            counter = 0;
            LOG.debug( "counterNO: " + counter );
        }

        if ( counter == authenticationTrys ) {
            LOG.debug( "ForbiddenException" );
            // set the counter back to 0
            counter = 0;
            throw new InvalidCredentialsException();
        }
        counter++;
        LOG.debug( "counterNO/YES: " + counter );

        if ( fit == false ) {
            LOG.debug( "SecurityException" );
            throw new SecurityException();
        }

        return null;

    }
}
