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
package org.deegree.services.csw.security;

import static org.deegree.protocol.csw.CSWConstants.CSWRequestType.DescribeRecord;
import static org.deegree.protocol.csw.CSWConstants.CSWRequestType.GetCapabilities;
import static org.deegree.protocol.csw.CSWConstants.CSWRequestType.GetRecordById;
import static org.deegree.protocol.csw.CSWConstants.CSWRequestType.GetRecords;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.protocol.csw.CSWConstants.CSWRequestType;
import org.deegree.protocol.ows.capabilities.GetCapabilities;
import org.deegree.services.authentication.InvalidCredentialsException;
import org.deegree.services.authentication.SecurityException;
import org.deegree.services.authentication.soapauthentication.FailedAuthentication;
import org.deegree.services.controller.Credentials;
import org.deegree.services.csw.describerecord.DescribeRecord;
import org.deegree.services.csw.getrecordbyid.GetRecordById;
import org.deegree.services.csw.getrecords.GetRecords;
import org.deegree.services.csw.transaction.Transaction;
import org.slf4j.Logger;

/**
 * Dummy implementation of {@link CSWSecurityManager} to handle incoming requests with a security barrier. <br>
 * There are three Users with different operations responsible to reach. But this is the rightsManagement, actually, and
 * should be moved to a backend later.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DummyCSWSecurityManager implements CSWSecurityManager {

    private static final Logger LOG = getLogger( DummyCSWSecurityManager.class );

    private static List<CSWRequestType> CSW_OPERATIONS_1 = new ArrayList<CSWRequestType>( 3 );

    private static List<CSWRequestType> CSW_OPERATIONS_2 = new ArrayList<CSWRequestType>( 2 );

    private static List<CSWRequestType> CSW_OPERATIONS_3 = new ArrayList<CSWRequestType>( 4 );

    private static List<CSWRequestType> CSW_OPERATIONS_4 = new ArrayList<CSWRequestType>( 5 );

    private static Map<Credentials, List<CSWRequestType>> credRepository = new HashMap<Credentials, List<CSWRequestType>>(
                                                                                                                           3 );

    private static final int authenticationTrys = 1;

    private int counter = 0;
    static {

        CSW_OPERATIONS_1.add( GetCapabilities );
        CSW_OPERATIONS_1.add( DescribeRecord );
        CSW_OPERATIONS_1.add( GetRecordById );

        CSW_OPERATIONS_2.add( GetCapabilities );
        CSW_OPERATIONS_2.add( DescribeRecord );

        CSW_OPERATIONS_3.add( GetRecords );
        CSW_OPERATIONS_3.add( GetCapabilities );
        CSW_OPERATIONS_3.add( DescribeRecord );
        CSW_OPERATIONS_3.add( GetRecordById );

        for ( CSWRequestType req : CSWRequestType.values() ) {
            CSW_OPERATIONS_4.add( req );
        }

        credRepository.put( new Credentials( "User1", "pass1" ), CSW_OPERATIONS_1 );
        credRepository.put( new Credentials( "User2", "pass2" ), CSW_OPERATIONS_2 );
        credRepository.put( new Credentials( "User3", "pass3" ), CSW_OPERATIONS_3 );
        credRepository.put( new Credentials( "SuperUser", "superPasswd4" ), CSW_OPERATIONS_4 );
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.services.controller.csw.security.CSWSecurityManager#preprocess(org.deegree.protocol.ows.capabilities
     * .GetCapabilities, org.deegree.services.controller.Credentials)
     */
    @Override
    public GetCapabilities preprocess( GetCapabilities getCapabilities, Credentials creds, boolean isSoap )
                            throws SecurityException {

        // boolean fit = false;
        // if ( creds != null ) {
        //
        // for ( Credentials s : credRepository.keySet() ) {
        //
        // LOG.info( "" + s.getUser() + " " + s.getPassword() );
        // if ( s.getUser().equals( ( creds.getUser() ) ) && s.getPassword().equals( creds.getPassword() ) ) {
        // LOG.debug( "there are credentials available" );
        // for ( CSWRequestType op : credRepository.get( s ) ) {
        // if ( op.equals( CSWRequestType.GetCapabilities ) ) {
        // fit = true;
        // // set the counter back to 0
        // counter = 0;
        // LOG.debug( "counterYES: " + counter );
        // return getCapabilities;
        // }
        // }
        //
        // } else {
        //
        // fit = false;
        // }
        //
        // }
        // if ( isSoap && fit == false ) {
        // throw new FailedAuthentication();
        // }
        //
        // } else {
        // counter = 0;
        // LOG.info( "counterNO: " + counter );
        // }
        //
        // if ( counter == authenticationTrys ) {
        // LOG.info( "ForbiddenException" );
        // // set the counter back to 0
        // counter = 0;
        // throw new InvalidCredentialsException();
        // }
        // counter++;
        // LOG.info( "counterNO/YES: " + counter );
        //
        // if ( fit == false ) {
        // LOG.info( "SecurityException" );
        // throw new SecurityException();
        // }

        return getCapabilities;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.services.controller.csw.security.CSWSecurityManager#preprocess(org.deegree.services.controller.csw
     * .describerecord.DescribeRecord, org.deegree.services.controller.Credentials)
     */
    @Override
    public DescribeRecord preprocess( DescribeRecord describeRecord, Credentials creds, boolean isSoap )
                            throws SecurityException {
        boolean fit = false;
        if ( creds != null ) {

            for ( Credentials s : credRepository.keySet() ) {

                LOG.debug( "" + s.getUser() + " " + s.getPassword() );
                if ( s.getUser().equals( ( creds.getUser() ) ) && s.getPassword().equals( creds.getPassword() ) ) {
                    LOG.debug( "there are credentials available" );
                    for ( CSWRequestType op : credRepository.get( s ) ) {
                        if ( op.equals( CSWRequestType.DescribeRecord ) ) {
                            fit = true;
                            // set the counter back to 0
                            counter = 0;
                            LOG.debug( "counterYES: " + counter );
                            return describeRecord;
                        }
                    }

                } else {

                    fit = false;
                }

            }
            if ( isSoap && fit == false ) {
                throw new FailedAuthentication();
            }

        } else {
            counter = 0;
            LOG.debug( "counterNO: " + counter );
        }

        if ( counter == authenticationTrys ) {
            LOG.debug( "ForbiddenException" );
            // set the counter back to 0
            // counter = 0;
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
     * org.deegree.services.controller.csw.security.CSWSecurityManager#preprocess(org.deegree.services.controller.csw
     * .getrecordbyid.GetRecordById, org.deegree.services.controller.Credentials)
     */
    @Override
    public GetRecordById preprocess( GetRecordById getRecordById, Credentials creds, boolean isSoap )
                            throws SecurityException {
        boolean fit = false;
        if ( creds != null ) {

            for ( Credentials s : credRepository.keySet() ) {

                LOG.debug( "" + s.getUser() + " " + s.getPassword() );
                if ( s.getUser().equals( ( creds.getUser() ) ) && s.getPassword().equals( creds.getPassword() ) ) {
                    LOG.debug( "there are credentials available" );
                    for ( CSWRequestType op : credRepository.get( s ) ) {
                        if ( op.equals( CSWRequestType.GetRecordById ) ) {
                            fit = true;
                            // set the counter back to 0
                            counter = 0;
                            LOG.debug( "counterYES: " + counter );
                            return getRecordById;
                        }
                    }

                } else {

                    fit = false;
                }

            }
            if ( isSoap && fit == false ) {
                throw new FailedAuthentication();
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
     * org.deegree.services.controller.csw.security.CSWSecurityManager#preprocess(org.deegree.services.controller.csw
     * .getrecords.GetRecords, org.deegree.services.controller.Credentials)
     */
    @Override
    public GetRecords preprocess( GetRecords getRecords, Credentials creds, boolean isSoap )
                            throws SecurityException {
        boolean fit = false;
        if ( creds != null ) {

            for ( Credentials s : credRepository.keySet() ) {

                LOG.debug( "" + s.getUser() + " " + s.getPassword() );
                if ( s.getUser().equals( ( creds.getUser() ) ) && s.getPassword().equals( creds.getPassword() ) ) {
                    LOG.debug( "there are credentials available" );
                    for ( CSWRequestType op : credRepository.get( s ) ) {
                        if ( op.equals( CSWRequestType.GetRecords ) ) {
                            fit = true;
                            // set the counter back to 0
                            counter = 0;
                            LOG.debug( "counterYES: " + counter );
                            return getRecords;
                        }
                    }

                } else {

                    fit = false;
                }

            }
            if ( isSoap && fit == false ) {
                throw new FailedAuthentication();
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
     * org.deegree.services.controller.csw.security.CSWSecurityManager#preprocess(org.deegree.services.controller.csw
     * .transaction.Transaction, org.deegree.services.controller.Credentials)
     */
    @Override
    public Transaction preprocess( Transaction transaction, Credentials creds, boolean isSoap )
                            throws SecurityException {
        boolean fit = false;
        if ( creds != null ) {

            for ( Credentials s : credRepository.keySet() ) {

                LOG.debug( "" + s.getUser() + " " + s.getPassword() );
                if ( s.getUser().equals( ( creds.getUser() ) ) && s.getPassword().equals( creds.getPassword() ) ) {
                    LOG.debug( "there are credentials available" );
                    for ( CSWRequestType op : credRepository.get( s ) ) {
                        if ( op.equals( CSWRequestType.Transaction ) ) {
                            fit = true;
                            // set the counter back to 0
                            counter = 0;
                            LOG.debug( "counterYES: " + counter );
                            return transaction;
                        }
                    }

                } else {

                    fit = false;
                }

            }
            if ( isSoap && fit == false ) {
                throw new FailedAuthentication();
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
