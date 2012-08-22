//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2006 by: M.O.S.S. Computer Grafik Systeme GmbH
 Hohenbrunner Weg 13
 D-82024 Taufkirchen
 http://www.moss.de/

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 ---------------------------------------------------------------------------*/
package org.deegree.io.sdeapi;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeState;
import com.esri.sde.sdk.client.SeVersion;

/**
 * <code>SDEConnection</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SDEConnection {

    private SeConnection connection;

    private SeVersion version;

    private SeState state;

    /**
     * @param sdeServer
     * @param sdeInstance
     * @param sdeDatabase
     * @param sdeVersion
     * @param sdeUser
     * @param sdePassword
     */
    public SDEConnection( final String sdeServer, final int sdeInstance, final String sdeDatabase,
                          final String sdeVersion, final String sdeUser, final String sdePassword ) {

        try {
            connection = new SeConnection( sdeServer, sdeInstance, sdeDatabase, sdeUser, sdePassword );
            if ( null == sdeVersion || 0 == sdeVersion.length() ) {
                version = new SeVersion( connection );
            } else {
                try {
                    version = new SeVersion( connection, sdeVersion );
                } catch ( SeException dne ) {
                    version = new SeVersion( connection, SeVersion.SE_QUALIFIED_DEFAULT_VERSION_NAME );
                    version.setDescription( sdeVersion );
                    version.setName( sdeVersion );
                    version.setParentName( SeVersion.SE_QUALIFIED_DEFAULT_VERSION_NAME );
                    version.create( false, version );
                }
            }
            reserve();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * @return the connection
     */
    public SeConnection getConnection() {
        return connection;
    }

    /**
     * @return the version
     */
    public SeVersion getVersion() {
        return version;
    }

    /**
     * @return the state
     */
    public SeState getState() {
        return state;
    }

    /**
     * @return if closed
     */
    public boolean isClosed() {
        if ( null == connection ) {
            return true;
        }
        return connection.isClosed();
    }

    /**
     * 
     */
    public void close() {
        if ( !isClosed() ) {
            release();
            try {
                connection.close();
            } catch ( Exception e ) {
                // just eat'em, it's good for the health
            }
            connection = null;
        }
    }

    private void reserve() {
        try {
            SeState verState = new SeState( connection, version.getStateId() );
            state = new SeState( connection );
            if ( verState.isOpen() ) {
                verState.close();
            }
            state.create( verState.getId() );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    private void release() {
        try {
            version.changeState( state.getId() );
            state.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}