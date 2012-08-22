//$Header: /raid/cvs-repos/cvsroot/lgv_3g/src/de/latlon/lgv3d/WPVSClientConfig.java,v 1.1 2007/03/09 10:40:46 ap Exp $
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

package org.deegree.portal.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.ogcwebservices.OWSUtils;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.wpvs.capabilities.Dataset;
import org.deegree.ogcwebservices.wpvs.capabilities.WPVSCapabilities;
import org.deegree.ogcwebservices.wpvs.capabilities.WPVSCapabilitiesDocument;
import org.xml.sax.SAXException;

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WPVSClientConfig {

    private final String[] initialBBox;

    private final String initialBBoxString;

    private final String wpvsBaseURL;

    private final int viewHeight;

    private final int viewWidth;

    private final int overViewHeight;

    private final int overViewWidth;

    private final String[] availableDatasets;

    private final WPVSCapabilities WPVS_Capabilities;

    private final String WMS_GetMap_Fragment;

    private static WPVSClientConfig clientConfig;

    private final Properties clientOptions;

    private final String defaultCRS;

    private final String propertieFile = "wpvsclient.properties";

    private final String elevationModel;

    private final String serviceIdentification;

    private final int distanceAboveSeaLevel;

    private final int initialPitch;

    private final int initialRoll;

    private final int initialYaw;

    private final int initialHeight;

    private final int initialDistance;

    private final double poi_x;

    private final double poi_y;

    private static ILogger LOG = LoggerFactory.getLogger( WPVSClientConfig.class );

    private WPVSClientConfig() throws SAXException, InvalidCapabilitiesException, IOException {
        // Singleton pattern
        clientOptions = new Properties();
        try {
            InputStream clientProps = WPVSClientConfig.class.getResourceAsStream( "/" + propertieFile );
            if ( clientProps == null ) {
                clientProps = WPVSClientConfig.class.getResourceAsStream( propertieFile );
            }
            clientOptions.load( clientProps );
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
        }

        String value = clientOptions.getProperty( "initialBBox" );
        if ( value == null || "".equals( value.trim() ) ) {
            LOG.logError( "The property 'initialBBox' can not be found or has no value. Please insert it into the wpvsclient.properties." );
            throw new IllegalArgumentException(
                                                "The property 'initialBBox' can not be found or has no value. Please insert it into the wpvsclient.properties." );
        }

        initialBBox = value.split( "," );
        if ( initialBBox.length != 4 ) {
            LOG.logError( "The property 'initialBBox' must have exactly 4 values seperated by commata (',')." );
            throw new IllegalArgumentException(
                                                "The property 'initialBBox' must have exactly 4 values seperated by commata (',')." );

        }
        initialBBoxString = value;

        String wpvsAddress = clientOptions.getProperty( "wpvsService" );
        if ( wpvsAddress == null ) {
            LOG.logError( "The property 'wpvsService' can not be found or has no value. Please insert it into the wpvsclient.properties." );
            throw new IllegalArgumentException(
                                                "The property 'wpvsService' can not be found or has no value. Please insert it into the wpvsclient.properties." );
        }

        wpvsBaseURL = OWSUtils.validateHTTPGetBaseURL( wpvsAddress );

        URL wpvsAddressURL = null;
        try {
            wpvsAddressURL = new URL( wpvsAddress + "request=GetCapabilities&service=WPVS" );
        } catch ( MalformedURLException e ) {
            throw new IllegalArgumentException( "The value (" + wpvsAddress
                                                + ") of property 'WPVS_Capabilities' is not a valid URL because: "
                                                + e.getMessage() );
        }
        WPVSCapabilitiesDocument capsDoc = new WPVSCapabilitiesDocument();
        try {
            capsDoc.load( wpvsAddressURL );
        } catch ( IOException e ) {
            LOG.logError( "Error while contacting the wpvs at location: '" + wpvsAddress
                          + "' please make sure, the server is a valid wpvs." );
            throw e;
        }
        WPVS_Capabilities = (WPVSCapabilities) capsDoc.parseCapabilities();

        String serviceID = WPVS_Capabilities.getServiceIdentification().getName();
        if ( serviceID == null ) {
            serviceIdentification = "Unknown WPVS service";
        } else {
            serviceIdentification = serviceID;
        }

        // find the datasets and a default crs.
        Dataset ds = WPVS_Capabilities.getDataset();
        if ( ds == null ) {
            throw new IllegalArgumentException(
                                                "Found no rootdataset in the capabilitiesdocument, this may not be, please make sure, the server at location: "
                                                                        + wpvsBaseURL + " is a valid wpvs." );
        }
        List<Dataset> foundSets = new ArrayList<Dataset>();
        //foundSets.add( ds );
        findDataSets( ds, foundSets );
        availableDatasets = new String[foundSets.size()];
        String elevationModeltmp = null;
        for ( int i = 0; i < foundSets.size(); ++i ) {
            availableDatasets[i] = foundSets.get( i ).getName();
            if ( elevationModeltmp == null && foundSets.get( i ).getElevationModel() != null ) {
                LOG.logInfo( "Using elevationModel of dataset: " + availableDatasets[i] );
                elevationModeltmp = foundSets.get( i ).getElevationModel().getName();
            }

        }

        if ( elevationModeltmp == null ) {
            LOG.logInfo( "Found no elevationModel in the capabilitiesdocument, though correct it is a little awkward, please make sure, the server at location: "
                         + wpvsBaseURL + " is configured correctly." );
        }

        elevationModel = elevationModeltmp;

        value = clientOptions.getProperty( "defaultCRS" );
        if ( value == null || "".equals( value.trim() ) ) {
            LOG.logInfo( "The property 'defaultCRS' can not be found or has no value. Trying to find in datasets." );
            CoordinateSystem[] crs = ds.getCrs();
            if ( crs == null || crs.length == 0 ) {
                LOG.logInfo( "No crs's found in datasets, setting defaultCRS to EPSG:4326." );
                defaultCRS = "EPGS:4326";
            } else {
                defaultCRS = crs[0].getFormattedString();
            }
        } else {
            defaultCRS = value.trim();
        }

        value = clientOptions.getProperty( "WMS_GetMap_Fragment" );
        if ( value == null ) {
            value = clientOptions.getProperty( "wmsDefaultLayer" );
            if ( value == null ) {
                String message = "No WMS_GetMap_Fragment was defined, and no wms default Layer was defined, please specify either one (WMS_GetMap_Fragment or wmsDefaultLayer) in wpvsclient.properties no Overview image will be available";
                LOG.logInfo( message );
                WMS_GetMap_Fragment = null;
            } else {
                String layer = value;
                value = clientOptions.getProperty( "wmsVersion" );
                if ( value == null ) {
                    LOG.logInfo( "No WMS_GetMap_Fragment was defined, and no wms version was found defined,thus assuming version 1.3.0" );
                    value = "1.3.0";
                }
                WMS_GetMap_Fragment = wpvsAddress
                                      + "SERVICE=WMS&VERSION="
                                      + value
                                      + "&REQUEST=GetMap&FORMAT=image/png&TRANSPARENT=true&BGCOLOR=0xFFFFFF&EXCEPTIONS=application/vnd.ogc.se_inimage&STYLES=&LAYERS="
                                      + layer + "&CRS=" + defaultCRS;

                LOG.logInfo( "The property WMS_GetMap_Fragment could not be found or has no value. Using the default wpvs server adress as the base wms overview server. Request will be:\n"
                             + WMS_GetMap_Fragment );
            }
        } else {
            WMS_GetMap_Fragment = value;
        }

        value = clientOptions.getProperty( "viewHeight" );
        if ( value == null ) {
            LOG.logInfo( "The property 'viewHeight' can not be found or has no value. Setting viewHeight to a value of 600." );
            viewHeight = 600;
        } else {
            viewHeight = Integer.parseInt( value );
        }

        value = clientOptions.getProperty( "viewWidth" );
        if ( value == null ) {
            LOG.logInfo( "The property 'viewWidth' can not be found or has no value. Setting viewWidth to a value of 800." );
            viewWidth = 800;
        } else {
            viewWidth = Integer.parseInt( value );
        }

        value = clientOptions.getProperty( "overViewHeight" );
        if ( value == null ) {
            LOG.logInfo( "The property 'overViewHeight' can not be found or has no value. Setting overViewHeight to a value of 150." );
            overViewHeight = 150;
        } else {
            overViewHeight = Integer.parseInt( value );
        }

        value = clientOptions.getProperty( "overViewWidth" );
        if ( value == null ) {
            LOG.logInfo( "The property 'overViewWidth' can not be found or has no value. Setting overViewWidth to a value of 150." );
            overViewWidth = 150;
        } else {
            overViewWidth = Integer.parseInt( value );
        }

        value = clientOptions.getProperty( "distanceAboveSeaLevel" );
        if ( value == null ) {
            LOG.logInfo( "The property 'distanceAboveSeaLevel' can not be found or has no value. Setting initialElevation to a value of 150." );
            distanceAboveSeaLevel = 0;
        } else {
            distanceAboveSeaLevel = Integer.parseInt( value );
        }
        value = clientOptions.getProperty( "initialHeight" );
        if ( value == null ) {
            LOG.logInfo( "The property 'initialHeight' can not be found or has no value. Setting initialElevation to a value of 150." );
            initialHeight = 150;
        } else {
            initialHeight = Integer.parseInt( value );
        }
        value = clientOptions.getProperty( "poi_x" );
        if ( value == null ) {
            LOG.logInfo( "The property 'poi_x' can not be found or has no value. Setting poi_x to a value of 424453." );
            poi_x = 424453;
        } else {
            poi_x = Double.parseDouble( value );
        }

        value = clientOptions.getProperty( "poi_y" );
        if ( value == null ) {
            LOG.logInfo( "The property 'poi_y' can not be found or has no value. Setting poi_y to a value of 150." );
            poi_y = 4512876;
        } else {
            poi_y = Double.parseDouble( value );
        }

        value = clientOptions.getProperty( "initialPitch" );
        if ( value == null ) {
            LOG.logInfo( "The property 'initialPitch' can not be found or has no value. Setting initialPitch to a value of 50." );
            initialPitch = 50;
        } else {
            initialPitch = Integer.parseInt( value );
        }

        value = clientOptions.getProperty( "initialRoll" );
        if ( value == null ) {
            LOG.logInfo( "The property 'initialRoll' can not be found or has no value. Setting initialRoll to a value of 0." );
            initialRoll = 0;
        } else {
            initialRoll = Integer.parseInt( value );
        }

        value = clientOptions.getProperty( "initialYaw" );
        if ( value == null ) {
            LOG.logInfo( "The property 'initialYaw' can not be found or has no value. Setting initialYaw to a value of 180." );
            initialYaw = 180;
        } else {
            initialYaw = Integer.parseInt( value );
        }

        value = clientOptions.getProperty( "initialDistance" );
        if ( value == null ) {
            LOG.logInfo( "The property 'initialDistance' can not be found or has no value. Setting initialDistance to a value of 1000." );
            initialDistance = 1000;
        } else {
            initialDistance = Integer.parseInt( value );
        }

    }

    private void findDataSets( Dataset rootSet, List<Dataset> foundSets ) {
        if ( rootSet != null ) {
            Dataset[] childSets = rootSet.getDatasets();
            for ( Dataset ds : childSets ) {
                if ( !foundSets.contains( ds ) ) {
                    if( ds.getQueryable() ){
                        foundSets.add( ds );
                    }
                    findDataSets( ds, foundSets );
                }
            }
        }
    }

    /**
     * @return a WPVSClientConfig instance following a singleton pattern.
     * @throws SAXException
     *             if the creation of the capabilities document fails
     * @throws IOException
     *             if the wpvs capabilities document cannot be read
     * @throws InvalidCapabilitiesException
     *             if the wpvs capabilitiesdocument cannot be parsed.
     */
    public static synchronized WPVSClientConfig getInstance()
                            throws InvalidCapabilitiesException, IOException, SAXException {
        if ( clientConfig == null ) {
            clientConfig = new WPVSClientConfig();
        }
        return clientConfig;
    }

    /**
     *
     * @return initial bounding box for GetView request
     */
    public final String[] getInitialBBox() {
        return initialBBox;
    }

    /**
     *
     * @return WMS GetMap request fragment for overview map
     */
    public final String getWmsGetMapFragment() {
        return WMS_GetMap_Fragment;
    }

    /**
     *
     * @return capabilities of the WPVS
     */
    public final WPVSCapabilities getWpvsCapabilities() {
        return WPVS_Capabilities;
    }

    /**
     * @return the overViewHeight.
     */
    public final int getOverViewHeight() {
        return overViewHeight;
    }

    /**
     * @return the overViewWidth.
     */
    public final int getOverViewWidth() {
        return overViewWidth;
    }

    /**
     * @return the viewHeight.
     */
    public final int getViewHeight() {
        return viewHeight;
    }

    /**
     * @return the viewWidth.
     */
    public final int getViewWidth() {
        return viewWidth;
    }

    /**
     * @return the wpvsBaseURL.
     */
    public final String getWpvsBaseURL() {
        return wpvsBaseURL;
    }

    /**
     * @return the availableDatasets.
     */
    public final String[] getAvailableDatasets() {
        return availableDatasets;
    }

    /**
     * @return the defaultCRS.
     */
    public final String getDefaultCRS() {
        return defaultCRS;
    }

    /**
     * @return the elevationModel.
     */
    public final String getElevationModel() {
        return elevationModel;
    }

    /**
     * @return the serviceIdentification.
     */
    public final String getServiceIdentification() {
        return serviceIdentification;
    }

    /**
     * @return the initialElevation.
     */
    public final int getDistanceAboveSeaLevel() {
        return distanceAboveSeaLevel;
    }

    /**
     * @return the initialBBoxString.
     */
    public final String getInitialBBoxAsString() {
        return initialBBoxString;
    }

    /**
     * @return the initialPitch.
     */
    public final int getInitialPitch() {
        return initialPitch;
    }

    /**
     * @return the initialDistance.
     */
    public final int getInitialDistance() {
        return initialDistance;
    }

    /**
     * @return the initialRoll.
     */
    public final int getInitialRoll() {
        return initialRoll;
    }

    /**
     * @return the initialYaw.
     */
    public final int getInitialYaw() {
        return initialYaw;
    }

    /**
     * @return the initialHeight.
     */
    public final int getInitialHeight() {
        return initialHeight;
    }

    /**
     * @return the poi_x.
     */
    public final double getPOIX() {
        return poi_x;
    }

    /**
     * @return the poi_y.
     */
    public final double getPOIY() {
        return poi_y;
    }

}
