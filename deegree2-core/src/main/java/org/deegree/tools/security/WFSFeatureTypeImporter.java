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

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.SecurityTransaction;
import org.deegree.security.drm.UnknownException;
import org.deegree.security.drm.model.User;

/**
 * Tool for adding all requestable featuretypes of a WFS into deegree's user and rights management
 * system
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WFSFeatureTypeImporter {

    private static final ILogger LOG = LoggerFactory.getLogger( WFSFeatureTypeImporter.class );

    private Configuration configuration;

    private SecurityAccessManager manager;

    /**
     *
     * @param configuration
     */
    public WFSFeatureTypeImporter( Configuration configuration ) {
        this.configuration = configuration;
    }

    /**
     *
     * @param param
     * @throws Exception
     */
    public WFSFeatureTypeImporter( Map<String, String> param ) throws Exception {
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
     * start reading, parsing WFSCapabilites and adding requestable featuretypes into rights DB
     *
     * @throws Exception
     */
    public void perform()
                            throws Exception {

        // initialize access to rights DB
        User user = setUp();

        URL url = new URL( configuration.getWfsAddress() + "?request=GetCapabilities&service=WFS" );
        WFSCapabilitiesDocument doc = new WFSCapabilitiesDocument();
        doc.load( url );

        WFSCapabilities caps = (WFSCapabilities) doc.parseCapabilities();
        WFSFeatureType[] fts = caps.getFeatureTypeList().getFeatureTypes();
        for ( int i = 0; i < fts.length; i++ ) {
            addFeatureTypeToRightsDB( fts[i], user );
        }
    }

    /**
     *
     * @param ft
     * @param user
     * @throws UnauthorizedException
     * @throws GeneralSecurityException
     */
    private void addFeatureTypeToRightsDB( WFSFeatureType ft, User user )
                            throws UnauthorizedException, GeneralSecurityException {
        QualifiedName qn = ft.getName();
        SecurityTransaction transaction = manager.acquireTransaction( user );
        try {
            transaction.getSecuredObjectByName( qn.getFormattedString(), "Featuretype" );
        } catch ( UnknownException e ) {
            LOG.logInfo( "add featuretype: " + qn.getFormattedString() );
            transaction.registerSecuredObject( "Featuretype", qn.getFormattedString(), ft.getTitle() );
            return;
        } finally {
            manager.commitTransaction( transaction );
        }

        LOG.logInfo( "skip featuretype: " + qn.getFormattedString() + " because it is already registered to rights DB" );

    }

    private static void printHelp() {
        System.out.println( "following parameters must be set: " );
        System.out.println( "-WFSAddress : must be a valid URL to a WFS" );
        System.out.println( "-Driver : JDBC database driver class" );
        System.out.println( "-URL : JDBC URL of the rights managment DB " );
        System.out.println( "-DBUserName : name of DB-user" );
        System.out.println( "-DBUserPassword : password of DB-user" );
        System.out.println( "-SecAdminPassword : password of rights managment admin" );
        System.out.println();
        System.out.println( "example:" );
        System.out.println( "java -classpath .;$ADD LIBS HERE org.deegree.tools.security.WFSFeatureTypeImporter " );
        System.out.println( "          -WFSAddress http://demo.deegree.org/deegree-wfs/services " );
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
        WFSFeatureTypeImporter imp = new WFSFeatureTypeImporter( map );
        imp.perform();
        System.exit( 0 );
    }

    /**
     * <code>Configuration</code> bean to hold relevant data of an underlying datasource.
     *
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     *
     * @author last edited by: $Author$
     *
     * @version $Revision$, $Date$
     *
     */
    public class Configuration {

        private String wfsAddress;

        private String secDBDriver;

        String secDBURL;

        private String secDBUserPw;

        private String secDBUserName;

        private String secAdminPw;

        /**
         * @param wfsAddress
         * @param secDBDriver
         * @param secDBURL
         * @param secDBUserName
         * @param secDBUserPw
         * @param secAdminPw
         */
        public Configuration( String wfsAddress, String secDBDriver, String secDBURL, String secDBUserName,
                              String secDBUserPw, String secAdminPw ) {
            this.wfsAddress = wfsAddress;
            this.secDBDriver = secDBDriver;
            this.secDBURL = secDBURL;
            this.secDBUserName = secDBUserName;
            this.secDBUserPw = secDBUserPw;
            this.secAdminPw = secAdminPw;
        }

        /**
         * @param map
         *            map with commandline options (eg. key: "-URL", value: "http://...")
         * @throws Exception
         */
        public Configuration( Map<String, String> map ) throws Exception {
            validate( map );
            wfsAddress = map.get( "-WFSAddress" );
            secDBDriver = map.get( "-Driver" );
            secDBURL = map.get( "-URL" );
            secDBUserName = map.get( "-DBUserName" );
            secDBUserPw = map.get( "-DBUserPassword" );
            secAdminPw = map.get( "-SecAdminPassword" );
        }

        private void validate( Map<String, String> map )
                                throws Exception {
            if ( map.get( "-WFSAddress" ) == null ) {
                throw new Exception( "Parameter -WFSAddress must be set" );
            }
            try {
                new URL( map.get( "-WFSAddress" ) );
            } catch ( Exception e ) {
                throw new Exception( "Parameter -WFSAddress must be a valid URL" );
            }
            if ( map.get( "-Driver" ) == null ) {
                throw new Exception( "Parameter -Driver must be set" );
            }
            if ( map.get( "-URL" ) == null ) {
                throw new Exception( "Parameter -URL must be set" );
            }
            if ( map.get( "-DBUserName" ) == null ) {
                throw new Exception( "Parameter -DBUserName must be set" );
            }
            if ( map.get( "-DBUserPassword" ) == null ) {
                throw new Exception( "Parameter -DBUserPassword must be set" );
            }
            if ( map.get( "-SecAdminPassword" ) == null ) {
                throw new Exception( "Parameter -SecAdminPassword must be set" );
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
         * @return address/URL of the WFS
         */
        public String getWfsAddress() {
            return wfsAddress;
        }

        /**
         *
         * @return rights management admin password
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
         * @return rights db user's passowrod
         */
        public String getSecDBUserPw() {
            return secDBUserPw;
        }

    }

}
