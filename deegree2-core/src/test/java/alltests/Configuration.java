//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/alltests/Configuration.java $
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
package alltests;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

/**
 * Configuration
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author last edited by: $Author: mschneider $
 *
 * @version $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 */
public final class Configuration {

    private static ILogger LOG = LoggerFactory.getLogger( Configuration.class );
    /* general configuration */
    public static URL getBaseDir()
                            throws MalformedURLException {
        return new File( System.getProperty( Messages.getString( "Configuration.basedir" ) ) ).toURL();
    }

    private static Properties userBuildProperties;

    static {
        try {
            userBuildProperties = new Properties();
            URL propertiesUrl = new URL( Configuration.getBaseDir(), "build.properties" );
            userBuildProperties.load( propertiesUrl.openStream() );
        } catch ( Exception e ) {
            LOG.logError( "Can't load ant user build.properties:" + e.getMessage(), e );
        }
    };

    public static final URL getResourceDir()
                            throws MalformedURLException {
        return new URL( getBaseDir(), Messages.getString( "Configuration.resource.dir" ) );
    }

    public static final String PROTOCOL = userBuildProperties.getProperty(
                                                                           userBuildProperties.getProperty(
                                                                                                            "default.server",
                                                                                                            "tomcat" )
                                                                                                   + ".schema", "http" );

    public static final String HOST = userBuildProperties.getProperty(
                                                                       userBuildProperties.getProperty(
                                                                                                        "default.server",
                                                                                                        "tomcat" )
                                                                                               + ".host", "localhost" );

    public static final int PORT = Integer.parseInt( userBuildProperties.getProperty(
                                                                                      userBuildProperties.getProperty(
                                                                                                                       "default.server",
                                                                                                                       "tomcat" )
                                                                                                              + ".port",
                                                                                      "8080" ) );

    public static final String WEB_DIR = Messages.getString( "Configuration.web" );

    public static final String GENERATED_DIR = "output";

    public static final String REQUESTS_DIR = "requests";

    public static final String DATASTORE_DIR = "datastore";

    /* WFS specific configuration */
    public static final String WFS_WEB_CONTEXT = Messages.getString( "Configuration.wfs.webcontext" );

    public static final String WFS_SERVLET = Messages.getString( "Configuration.wfs.ogcservice.name" );

    public static final String WFS_CONFIGURATION = Messages.getString( "Configuration.wfs.configuration" );

    public static final URL getWFSBaseDir()
                            throws MalformedURLException {
        return new URL( getResourceDir(), Messages.getString( "Configuration.wfs.basedir" ) );
    }

    /* CSW specific configuration */
    public static final String CSW_WEB_CONTEXT = Messages.getString( "Configuration.csw.webcontext" );

    public static final String CSW_CAPABILITIES_GENERATED = Messages.getString( "Configuration.csw.capabilities.generated" );

    public static final String CSW_CONFIGURATION_EXAMPLE = Messages.getString( "Configuration.csw.configuration.example" );

    public static final String CSW_SERVLET = Messages.getString( "Configuration.csw.ogcservice.name" );

    public static final String CSW_INTERNALWFS_FILE = Messages.getString( "Configuration.csw.internalwfs.configuration" );

    public static final URL getCSWBaseDir()
                            throws MalformedURLException {
        return new URL( getResourceDir(), Messages.getString( "Configuration.csw.basedir" ) );
    }

    /* WCS specific configuration */
    public static final String WCS_WEB_CONTEXT = Messages.getString( "Configuration.wcs.webcontext" );

    public static final String WCS_CONFIGURATION_FILE = Messages.getString( "Configuration.wcs.configuration" );

    public static final String WCS_SERVLET = Messages.getString( "Configuration.wcs.ogcservice.name" );

    public static final URL getWCSBaseDir()
                            throws MalformedURLException {
        return new URL( getResourceDir(), Messages.getString( "Configuration.wcs.basedir" ) );
    }

    /* WAS specific configuration */
    public static final String WAS_WEB_CONTEXT = Messages.getString( "Configuration.was.webcontext" );

    public static final String WAS_CONFIGURATION_FILE = Messages.getString( "Configuration.was.configuration" );

    public static final String WAS_SERVLET = Messages.getString( "Configuration.was.ogcservice.name" );

    public static final URL getWASBaseDir()
                            throws MalformedURLException {
        return new URL( getResourceDir(), Messages.getString( "Configuration.was.basedir" ) );
    }

    public static URL getWASConfigurationURL()
                            throws MalformedURLException {
        return new URL( getWASBaseDir(), WAS_CONFIGURATION_FILE );
    }

    /* WMS specific configuration */
    public static final String WMS_WEB_CONTEXT = Messages.getString( "Configuration.wms.webcontext" );

    public static final String WMS_CONFIGURATION_FILE = Messages.getString( "Configuration.wms.configuration" );

    public static final String WMS_SERVLET = Messages.getString( "Configuration.wms.ogcservice.name" );

    public static final URL getWMSBaseDir()
                            throws MalformedURLException {
        return new URL( getResourceDir(), Messages.getString( "Configuration.wms.basedir" ) );
    }

    public static final URL getWMSConfDir()
                            throws MalformedURLException {
        return new URL( getResourceDir(), Messages.getString( "Configuration.wms.conf" ) );
    }


    /* WMPS specific configuration */
    public static final String WMPS_WEB_CONTEXT = Messages.getString( "Configuration.wmps.webcontext" );

    public static final String WMPS_CONFIGURATION_FILE = Messages.getString( "Configuration.wmps.configuration" );

    public static final String WMPS_SERVLET = Messages.getString( "Configuration.wmps.ogcservice.name" );

    public static final URL getWMPSBaseDir()
                            throws MalformedURLException {
        return new URL( getResourceDir(), Messages.getString( "Configuration.wmps.basedir" ) );
    }

    public static final URL getWMPSConfDir()
                            throws MalformedURLException {
        return new URL( getResourceDir(), Messages.getString( "Configuration.wmps.conf" ) );
    }

    /* GML specific configuration */
    public static final String GML_COMPLEX_EXAMPLE = Messages.getString( "Configuration.gml.complexexample" );

    public static final URL getGMLBaseDir()
                            throws MalformedURLException {
        return new URL( getResourceDir(), Messages.getString( "Configuration.gml.basedir" ) );
    }

    /* owsProxy configuration */
    public static final String OWSPROXY_WMSCONFIGURATION_EXAMPLE = Messages.getString( "Configuration.owsproxy.configurationwms" );

    public static final String OWSPROXY_WFSCONFIGURATION_EXAMPLE = Messages.getString( "Configuration.owsproxy.configurationwfs" );

    public static final String OWSPROXY_CSWCONFIGURATION_EXAMPLE = Messages.getString( "Configuration.owsproxy.configurationcsw" );

    public static final URL getOwsProxyBaseDir()
                            throws MalformedURLException {
        return new URL( getResourceDir(), Messages.getString( "Configuration.owsproxy.basedir" ) );
    }

    /* SLD configuration */
    public static final String SLD1_EXAMPLE = Messages.getString( "Configuration.sld.example1" );

    public static final URL getSLDBaseDir()
                            throws MalformedURLException {
        return new URL( getResourceDir(), Messages.getString( "Configuration.sld.basedir" ) );
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////


    /**
     * @return "http://demo.deegree.org/deegree-wfs/services"
     * @throws MalformedURLException
     */
    public static URL getWFSURL()
                            throws MalformedURLException {
        return new URL( "http://demo.deegree.org/deegree-wfs/services" );
    }


    /**
     * @return "http://demo.deegree.org/deegree-csw/services"
     * @throws MalformedURLException
     */
    public static URL getCSWURL()
                            throws MalformedURLException {
        return new URL( "http://demo.deegree.org/deegree-csw/services" );
    }

    public static URL getWFSConfigurationURL()
                            throws MalformedURLException {
        return new URL( getWFSBaseDir(), WFS_CONFIGURATION );
    }

    public static URL getGeneratedWFSCapabilitiesURL()
                            throws MalformedURLException {
        URL generatedDir = new URL( getWFSBaseDir(), GENERATED_DIR + "/" );
        return new URL( generatedDir, "wfs_capabilities.xml" );
    }

    public static URL getCSWConfigurationURL()
                            throws MalformedURLException {
        return new URL( getCSWBaseDir(), CSW_CONFIGURATION_EXAMPLE );
    }

    public static URL getCSWInternalWFSConfigurationURL()
                            throws MalformedURLException {
        return new URL( getCSWBaseDir(), CSW_INTERNALWFS_FILE );
    }

    public static URL getGeneratedCSWCapabilitiesURL()
                            throws MalformedURLException {
        URL generatedDir = new URL( getCSWBaseDir(), GENERATED_DIR + "/" );
        return new URL( generatedDir, CSW_CAPABILITIES_GENERATED );
    }

    public static URL getWCSConfigurationURL()
                            throws MalformedURLException {
        return new URL( getWCSBaseDir(), WCS_CONFIGURATION_FILE );
    }

    public static URL getWMSConfigurationURL()
                            throws MalformedURLException {
        return new URL( getWMSBaseDir(), WMS_CONFIGURATION_FILE );
    }

    public static URL getWMPSConfigurationURL()
                            throws MalformedURLException {
        return new URL( getWMPSBaseDir(), WMPS_CONFIGURATION_FILE );
    }


    private Configuration() {

    }

}
