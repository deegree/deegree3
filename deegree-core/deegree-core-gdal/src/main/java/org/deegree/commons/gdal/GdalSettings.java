/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.commons.gdal;

import static java.util.Collections.synchronizedMap;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.gdal.jaxb.GDALSettings;
import org.deegree.commons.gdal.jaxb.GDALSettings.GDALOption;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.workspace.Destroyable;
import org.deegree.workspace.Initializable;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.gdal.gdal.gdal;
import org.gdal.osr.SpatialReference;
import org.slf4j.Logger;

/**
 * {@link Initializable} for GDAL JNI access.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
public class GdalSettings implements Initializable, Destroyable {

    private static final Logger LOG = getLogger( GdalSettings.class );

    private static final URL CONFIG_SCHEMA = GdalSettings.class.getResource( "/META-INF/schemas/commons/gdal/3.4.0/gdal.xsd" );

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.commons.gdal.jaxb";

    private static final String configFileName = "gdal.xml";

    private static boolean registerCalledSuccessfully;

    private final Map<Integer, SpatialReference> epsgCodeToSpatialReference = synchronizedMap( new HashMap<Integer, SpatialReference>() );

    private GdalDatasetPool pool;

    @Override
    public void destroy( Workspace workspace ) {
        pool.shutdown();
    }

    @Override
    public void init( final Workspace workspace ) {
        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "GDAL JNI adapter." );
        LOG.info( "--------------------------------------------------------------------------------" );        
        GDALSettings settings = getGdalConfigOptions( workspace );
        if ( settings == null ) {
            LOG.info( "No " + configFileName + " in workspace. Not initializing GDAL JNI adapter." );
            return;
        } else {
            registerGdal( settings );
        }
        LOG.info( "GDAL JNI adapter initialized successfully." );
    }

    private void registerGdal( GDALSettings settings ) {
        if ( registerOnceQuietly() ) {
            for ( GDALOption gdalConfigOption : settings.getGDALOption() ) {
                LOG.info( "GDAL: " + gdalConfigOption.getName() + "=" + gdalConfigOption.getValue().trim() );
                gdal.SetConfigOption( gdalConfigOption.getName(), gdalConfigOption.getValue().trim() );
            }
            int activeDatasets = settings.getOpenDatasets().intValue();
            LOG.info( "Max number of open GDAL datasets: " + activeDatasets );
            pool = new GdalDatasetPool( activeDatasets );
        }
    }

    private boolean registerOnceQuietly() {
        if ( registerCalledSuccessfully ) {
            return true;
        }
        try {
            gdal.AllRegister();
            registerCalledSuccessfully = true;
            return true;
        } catch ( Exception e ) {
            LOG.error( "Registration of GDAL JNI adapter failed: " + e.getMessage(), e );
        }
        return false;
    }

    public GdalDatasetPool getDatasetPool() {
        return pool;
    }

    private GDALSettings getGdalConfigOptions( final Workspace ws ) {
        File configFile = new File( ( (DefaultWorkspace) ws ).getLocation(), configFileName );
        if ( configFile.exists() ) {
            LOG.info( "Using '" + configFileName + "' from workspace for GDAL settings." );
            try {
                return readGdalConfigOptions( configFile, ws );
            } catch ( Exception e ) {
                LOG.error( "Error reading GDALSettings file: " + e.getMessage() );
            }
        }
        return null;
    }

    private GDALSettings readGdalConfigOptions( File configFile, Workspace ws )
                            throws FileNotFoundException, JAXBException {
        InputStream is = null;
        try {
            is = new FileInputStream( configFile );
            return (GDALSettings) JAXBUtils.unmarshall( CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA, is, ws );
        } finally {
            IOUtils.closeQuietly( is );
        }
    }

    public SpatialReference getCrsAsWkt( int epsgCode ) {
        SpatialReference sr = epsgCodeToSpatialReference.get( epsgCode );
        if ( sr == null ) {
            synchronized ( this ) {
                sr = new SpatialReference();
                int importFromEPSG = sr.ImportFromEPSG( epsgCode );
                if ( importFromEPSG != 0 ) {
                    throw new RuntimeException( "Cannot import EPSG:" + epsgCode + " from GDAL." );
                }
                epsgCodeToSpatialReference.put( epsgCode, sr );
            }
        }
        return sr;
    }

    public SpatialReference getCrs84() {
        SpatialReference sr = new SpatialReference();
        int importFromEPSG = sr.ImportFromWkt(
                        "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AXIS[\"Longitude\",EAST],AXIS[\"Latitude\",NORTH]]" );
        if ( importFromEPSG != 0 ) {
            throw new RuntimeException( "Cannot import CRS:84 from GDAL." );
        }
        return sr;
    }
}
