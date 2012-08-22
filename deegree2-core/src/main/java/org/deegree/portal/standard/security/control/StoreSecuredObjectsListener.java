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
package org.deegree.portal.standard.security.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCException;
import org.deegree.enterprise.control.RPCMember;
import org.deegree.enterprise.control.RPCMethodCall;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.SecurityTransaction;
import org.deegree.security.drm.model.SecuredObject;

/**
 * This <code>Listener</code> reacts on 'storeSecuredObjects' events, extracts the contained
 * Layer/FeatureType definitions and updates the <code>SecurityManager</code> accordingly.
 *
 * Access constraints:
 * <ul>
 * <li>only users that have the 'SEC_ADMIN'-role are allowed</li>
 * </ul>
 *
 * @author <a href="mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class StoreSecuredObjectsListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( StoreSecuredObjectsListener.class );

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.enterprise.control.WebListener#actionPerformed(org.deegree.enterprise.control.FormEvent)
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        // keys are Strings (types), values are ArrayLists (which contain Strings)
        Map<String,ArrayList<String>> newObjectTypes = new HashMap<String,ArrayList<String>>();
        // keys are Strings (types), values are ArrayLists (which contain Integers)
        Map<String,ArrayList<Integer>> oldObjectTypes = new HashMap<String, ArrayList<Integer>>();

        SecurityAccessManager manager = null;
        SecurityTransaction transaction = null;

        try {
            RPCWebEvent ev = (RPCWebEvent) event;
            RPCMethodCall rpcCall = ev.getRPCMethodCall();
            RPCParameter[] params = rpcCall.getParameters();

            for ( int i = 0; i < params.length; i++ ) {
                if ( !( params[0].getValue() instanceof RPCStruct ) ) {
                    throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_STRUCT" ) );
                }
                RPCStruct struct = (RPCStruct) params[i].getValue();

                // extract details of one SecuredObject
                RPCMember idRPC = struct.getMember( "id" );
                RPCMember nameRPC = struct.getMember( "name" );
                RPCMember typeRPC = struct.getMember( "type" );

                int id;
                String name = null;
                String type = null;

                // extract id
                if ( idRPC == null ) {
                    throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_MEMBER", "object", "id" ) );
                }
                if ( !( idRPC.getValue() instanceof String ) ) {
                    throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_MEMBER", "id", "string" ) );
                }
                try {
                    id = Integer.parseInt( ( (String) idRPC.getValue() ) );
                } catch ( NumberFormatException e ) {
                    throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_MEMBER", "id", "integer" ) );
                }
                // extract name
                if ( nameRPC != null ) {
                    if ( !( nameRPC.getValue() instanceof String ) ) {
                        throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_MEMBER", "name", "string" ) );
                    }
                    name = (String) nameRPC.getValue();
                }
                // extract type
                if ( typeRPC != null ) {
                    if ( !( typeRPC.getValue() instanceof String ) ) {
                        throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_MEMBER", "type", "string" ) );
                    }
                    type = (String) typeRPC.getValue();

                }
                if ( name == null ) {
                    throw new GeneralSecurityException( Messages.getMessage( "IGEO_STD_SEC_MISSING_MEMBER",
                                                                             "SecuredObject", "name" ) );
                }
                if ( type == null ) {
                    throw new GeneralSecurityException( Messages.getMessage( "IGEO_STD_SEC_MISSING_MEMBER",
                                                                             "SecuredObject", "type" ) );
                }

                // new or existing SecuredObject?
                if ( id == -1 ) {
                    ArrayList<String> list = newObjectTypes.get( type );
                    if ( list == null ) {
                        list = new ArrayList<String>( 20 );
                        newObjectTypes.put( type, list );
                    }
                    list.add( name );
                } else {
                    ArrayList<Integer> list = oldObjectTypes.get( type );
                    if ( list == null ) {
                        list = new ArrayList<Integer>( 20 );
                        oldObjectTypes.put( type, list );
                    }
                    list.add( new Integer( id ) );
                }
            }

            // get Transaction and perform access check
            manager = SecurityAccessManager.getInstance();
            transaction = SecurityHelper.acquireTransaction( this );
            SecurityHelper.checkForAdminRole( transaction );

            // remove deleted Layers
            SecuredObject[] obsoleteLayers = getObjectsToDelete(
                                                                 oldObjectTypes.get( ClientHelper.TYPE_LAYER ),
                                                                 transaction.getAllSecuredObjects( ClientHelper.TYPE_LAYER ) );
            for ( int i = 0; i < obsoleteLayers.length; i++ ) {
                transaction.deregisterSecuredObject( obsoleteLayers[i] );
            }

            // register new Layers
            ArrayList newLayerList = newObjectTypes.get( ClientHelper.TYPE_LAYER );
            if ( newLayerList != null ) {
                Iterator it = newLayerList.iterator();
                while ( it.hasNext() ) {
                    String name = (String) it.next();
                    transaction.registerSecuredObject( ClientHelper.TYPE_LAYER, name, name );
                }
            }

            // remove deleted FeatureTypes
            SecuredObject[] obsoleteFeatureTypes = getObjectsToDelete(
                                                                       oldObjectTypes.get( ClientHelper.TYPE_FEATURETYPE ),
                                                                       transaction.getAllSecuredObjects( ClientHelper.TYPE_FEATURETYPE ) );
            for ( int i = 0; i < obsoleteFeatureTypes.length; i++ ) {
                transaction.deregisterSecuredObject( obsoleteFeatureTypes[i] );
            }

            // register new FeatureTypes
            ArrayList newFeatureTypeList = newObjectTypes.get( ClientHelper.TYPE_FEATURETYPE );
            if ( newFeatureTypeList != null ) {
                Iterator it = newFeatureTypeList.iterator();
                while ( it.hasNext() ) {
                    String name = (String) it.next();
                    transaction.registerSecuredObject( ClientHelper.TYPE_FEATURETYPE, name, name );
                }
            }

            manager.commitTransaction( transaction );
            transaction = null;

            getRequest().setAttribute( "MESSAGE", Messages.getMessage( "IGEO_STD_SEC_SUCCESS_INITSECOBJEDITOR" ) );
        } catch ( RPCException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", Messages.getMessage( "IGEO_STD_SEC_ERROR_CHANGE_REQ", e.getMessage() ) );
            setNextPage( "error.jsp" );
            LOG.logError( e.getMessage(), e );
        } catch ( GeneralSecurityException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", Messages.getMessage( "IGEO_STD_SEC_ERROR_CHANGE", e.getMessage() ) );
            setNextPage( "error.jsp" );
            LOG.logError( e.getMessage(), e );
        } finally {
            if ( manager != null && transaction != null ) {
                try {
                    manager.abortTransaction( transaction );
                } catch ( GeneralSecurityException ex ) {
                    LOG.logError( ex.getMessage() );
                }
            }
        }
    }

    private SecuredObject[] getObjectsToDelete( ArrayList<Integer> remainingObjects, SecuredObject[] presentObjects ) {
        Set<Integer> lookup = new HashSet<Integer>();
        ArrayList<SecuredObject> deleteList = new ArrayList<SecuredObject>( 10 );
        if ( remainingObjects != null ) {
            lookup = new HashSet<Integer>( remainingObjects );
        }
        for ( int i = 0; i < presentObjects.length; i++ ) {
            if ( !lookup.contains( new Integer( presentObjects[i].getID() ) ) ) {
                deleteList.add( presentObjects[i] );
            }
        }
        return deleteList.toArray( new SecuredObject[deleteList.size()] );
    }
}
