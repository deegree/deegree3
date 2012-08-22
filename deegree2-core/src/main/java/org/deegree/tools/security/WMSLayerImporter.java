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
package org.deegree.tools.security;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocument;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocumentFactory;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.SecurityTransaction;
import org.deegree.security.drm.UnknownException;
import org.deegree.security.drm.model.User;

/**
 * Tool for adding all layers (which can be requested) of a WMS into deegree's user and rights
 * management system
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WMSLayerImporter {

    private static final ILogger LOG = LoggerFactory.getLogger( WMSLayerImporter.class );

    private Configuration configuration;

    private SecurityAccessManager manager;

    /**
     *
     * @param configuration
     */
    public WMSLayerImporter( Configuration configuration ) {
        this.configuration = configuration;
    }

    /**
     *
     * @param param
     * @throws IllegalArgumentException
     *             if map is missing a required parameter
     */
    public WMSLayerImporter( Map<String, String> param ) throws IllegalArgumentException {
        this.configuration = new Configuration( param );
    }

    /**
     * initializes access to the security and rights db
     *
     * @throws GeneralSecurityException
     * @return admin user
     */
    private User setUp()
                            throws GeneralSecurityException {
        Properties properties = new Properties();
        properties.setProperty( "driver", configuration.getSecDBDriver() );
        properties.setProperty( "url", configuration.secDBURL );
        properties.setProperty( "user", configuration.getSecDBUserName() );
        properties.setProperty( "password", configuration.getSecDBUserPw() );
        System.out.println( properties );
        try {
            manager = SecurityAccessManager.getInstance();
        } catch ( GeneralSecurityException e ) {
            try {
                System.out.println( properties );
                SecurityAccessManager.initialize( "org.deegree.security.drm.SQLRegistry", properties, 60 * 1000 );
                manager = SecurityAccessManager.getInstance();
            } catch ( GeneralSecurityException e1 ) {
                e1.printStackTrace();
            }
        }
        User user = manager.getUserByName( "SEC_ADMIN" );
        user.authenticate( configuration.getSecAdminPw() );
        return user;
    }

    /**
     * start reading, parsing WMSCapabilites and adding requestable layers into rights DB
     *
     * @throws Exception
     */
    public void perform()
                            throws Exception {

        // initialize access to rights DB
        User user = setUp();

        URL url = new URL( configuration.getWmsAddress() + "?request=GetCapabilities&service=WMS" );
        WMSCapabilitiesDocument doc = WMSCapabilitiesDocumentFactory.getWMSCapabilitiesDocument( url );

        WMSCapabilities caps = (WMSCapabilities) doc.parseCapabilities();
        Layer layer = caps.getLayer();
        traverseLayer( layer, user );
    }

    /**
     *
     * @param layer
     * @throws GeneralSecurityException
     * @throws UnauthorizedException
     */
    private void traverseLayer( Layer layer, User user )
                            throws UnauthorizedException, GeneralSecurityException {
        if ( layer.getName() != null ) {
            // just layers having a name can be considered because just these layers
            // can be requests in a GetMap or GetFeatureInfo request
            addLayerToRightsDB( layer, user );
        }
        Layer[] layers = layer.getLayer();
        if ( layers != null ) {
            for ( int i = 0; i < layers.length; i++ ) {
                traverseLayer( layers[i], user );
            }
        }
    }

    /**
     *
     * @param layer
     * @param user
     * @throws UnauthorizedException
     * @throws GeneralSecurityException
     */
    private void addLayerToRightsDB( Layer layer, User user )
                            throws UnauthorizedException, GeneralSecurityException {

        SecurityTransaction transaction = manager.acquireTransaction( user );
        try {
            transaction.getSecuredObjectByName( layer.getName(), "Layer" );
        } catch ( UnknownException e ) {
            LOG.logInfo( "add layer: " + layer.getName() );
            transaction.registerSecuredObject( "Layer", layer.getName(), layer.getTitle() );
            return;
        } finally {
            manager.commitTransaction( transaction );
        }

        LOG.logInfo( "skip layer: " + layer.getName() + " because it is already registered to rights DB" );

    }

    private static void printHelp() {
        System.out.println( "following parameters must be set: " );
        System.out.println( "-WMSAddress : must be a valid URL to a WMS" );
        System.out.println( "-Driver : JDBC database driver class" );
        System.out.println( "-URL : JDBC URL of the rights managment DB " );
        System.out.println( "-DBUserName : name of DB-user" );
        System.out.println( "-DBUserPassword : password of DB-user" );
        System.out.println( "-SecAdminPassword : password of rights managment admin" );
        System.out.println();
        System.out.println( "example:" );
        System.out.println( "java -classpath .;$ADD LIBS HERE org.deegree.tools.security.WMSLayerImporter " );
        System.out.println( "          -WMSAddress http://demo.deegree.org/deegree-wms/services " );
        System.out.println( "          -Driver org.postgresql.Driver -URL jdbc:postgresql://localhost:5432/security " );
        System.out.println( "          -DBUserName postgres -DBUserPassword postgres -SecAdminPassword JOSE67" );
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main( String[] args )
                            throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        for ( int i = 0; i < args.length; i += 2 ) {
            if ( args[i].equals( "-h" ) || args[i].equals( "-?" ) ) {
                printHelp();
                return;
            }
            map.put( args[i], args[i + 1] );
        }
        WMSLayerImporter imp = new WMSLayerImporter( map );
        imp.perform();
        System.exit( 0 );
    }

    /**
     *
     * <code>Configuration</code> which holds values for a given layer.
     *
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     *
     * @author last edited by: $Author$
     *
     * @version $Revision$, $Date$
     *
     */
    public class Configuration {

        private String wmsAddress;

        private String secDBDriver;

        String secDBURL;

        private String secDBUserPw;

        private String secDBUserName;

        private String secAdminPw;

        /**
         *
         * @param wmsAddress
         *            the address of the remote wms.
         * @param secDBDriver
         *            the type of database
         * @param secDBURL
         *            database url
         * @param secDBUserName
         *            of the database user.
         * @param secDBUserPw
         *            password of the database user.
         * @param secAdminPw
         *            password of the security administrator.
         */
        public Configuration( String wmsAddress, String secDBDriver, String secDBURL, String secDBUserName,
                              String secDBUserPw, String secAdminPw ) {
            this.wmsAddress = wmsAddress;
            this.secDBDriver = secDBDriver;
            this.secDBURL = secDBURL;
            this.secDBUserName = secDBUserName;
            this.secDBUserPw = secDBUserPw;
            this.secAdminPw = secAdminPw;
        }

        /**
         *
         * @param map
         *            containing the the arguments given to the main.
         * @throws IllegalArgumentException
         *             if one of the required parameters was not set.
         */
        public Configuration( Map<String, String> map ) throws IllegalArgumentException {
            validate( map );
            wmsAddress = map.get( "-WMSAddress" );
            secDBDriver = map.get( "-Driver" );
            secDBURL = map.get( "-URL" );
            secDBUserName = map.get( "-DBUserName" );
            secDBUserPw = map.get( "-DBUserPassword" );
            secAdminPw = map.get( "-SecAdminPassword" );
        }

        private void validate( Map<String, String> map )
                                throws IllegalArgumentException {
            if ( map.get( "-WMSAddress" ) == null ) {
                throw new IllegalArgumentException( "Parameter -WMSAddress must be set" );
            }
            try {
                new URL( map.get( "-WMSAddress" ) );
            } catch ( Exception e ) {
                throw new IllegalArgumentException( "Parameter -WMSAddress must be a valid URL" );
            }
            if ( map.get( "-Driver" ) == null ) {
                throw new IllegalArgumentException( "Parameter -Driver must be set" );
            }
            if ( map.get( "-URL" ) == null ) {
                throw new IllegalArgumentException( "Parameter -URL must be set" );
            }
            if ( map.get( "-DBUserName" ) == null ) {
                throw new IllegalArgumentException( "Parameter -DBUserName must be set" );
            }
            if ( map.get( "-DBUserPassword" ) == null ) {
                throw new IllegalArgumentException( "Parameter -DBUserPassword must be set" );
            }
            if ( map.get( "-SecAdminPassword" ) == null ) {
                throw new IllegalArgumentException( "Parameter -SecAdminPassword must be set" );
            }
        }

        /**
         *
         * @return database driver class
         */
        public String getSecDBDriver() {
            return secDBDriver;
        }

        /**
         *
         * @return database URL
         */
        public String getSecDBURL() {
            return secDBURL;
        }

        /**
         *
         * @return address/URL of the WMS
         */
        public String getWmsAddress() {
            return wmsAddress;
        }

        /**
         *
         * @return rights management administrator password
         */
        public String getSecAdminPw() {
            return secAdminPw;
        }

        /**
         *
         * @return rights db user name
         */
        public String getSecDBUserName() {
            return secDBUserName;
        }

        /**
         *
         * @return rights database user's password
         */
        public String getSecDBUserPw() {
            return secDBUserPw;
        }

    }

}
