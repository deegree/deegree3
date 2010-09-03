//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.client.mdeditor.io.xml;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.configuration.ConfigurationManager;
import org.deegree.client.mdeditor.io.DataHandler;
import org.deegree.client.mdeditor.io.DataIOException;
import org.deegree.client.mdeditor.model.DataGroup;
import org.deegree.client.mdeditor.model.Dataset;
import org.deegree.client.mdeditor.model.FormGroup;
import org.deegree.commons.utils.StringPair;
import org.slf4j.Logger;

/**
 * handles form groups storded in files
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class XMLDataHandler extends DataHandler {

    private static final Logger LOG = getLogger( XMLDataHandler.class );

    static final String FILE_SUFFIX = ".xml";

    static final String DG_ELEM = "DataGroup";

    static final String DS_ELEM = "Dataset";

    static final String ELEM_ELEM = "Element";

    static final String GRP_ELEM = "Group";

    static final String VALUE_ELEM = "value";

    static final String ID_ELEM = "id";

    private static Map<String, Dataset> datasetCache = new HashMap<String, Dataset>();

    // TODO
    // private static Map<String, List<DataGroup>> dataGroupCache = new HashMap<String, List<DataGroup>>();

    @Override
    public List<StringPair> getItems( String grpId, String referenceLabel )
                            throws ConfigurationException {
        List<StringPair> items = new ArrayList<StringPair>();
        File f = new File( ConfigurationManager.getConfiguration().getDataDir(), grpId );
        if ( f.exists() && f.isDirectory() ) {
            File[] listFiles = f.listFiles();
            for ( int i = 0; i < listFiles.length; i++ ) {
                String label = listFiles[i].getName();
                String value = listFiles[i].getName();
                try {
                    if ( referenceLabel != null ) {
                        label = replaceProperties( referenceLabel, DataReader.readDataGroup( listFiles[i] ) );
                    }
                    items.add( new StringPair( value, label ) );
                } catch ( Exception e ) {
                    LOG.debug( "Could not read file " + listFiles[i].getAbsolutePath(), e );
                    LOG.error( "Could not read file " + listFiles[i].getAbsolutePath() + ": ", e.getMessage() );
                }
            }
        }
        return items;
    }

    private String replaceProperties( String referenceLabel, Map<String, Object> map ) {
        String replaced = referenceLabel;
        for ( String path : map.keySet() ) {
            String step = path;
            if ( path.indexOf( '/' ) > -1 ) {
                step = path.substring( path.indexOf( '/' ) + 1 );
            }
            replaced = replaced.replace( "${" + step + "}", (CharSequence) map.get( path ) );

        }
        replaced = replaced.replaceAll( "\\$\\{[\\/\\w]*\\}", "" );
        return replaced;
    }

    @Override
    public List<Dataset> getDatasets()
                            throws ConfigurationException, DataIOException {
        List<Dataset> datasets = new ArrayList<Dataset>();
        File d = ConfigurationManager.getConfiguration().getDataDir();
        if ( d.exists() && d.isDirectory() ) {
            File[] listFiles = d.listFiles();
            for ( int i = 0; i < listFiles.length; i++ ) {
                String fileName = listFiles[i].getName();
                if ( listFiles[i].isFile() && fileName.endsWith( FILE_SUFFIX ) ) {
                    datasets.add( getDataset( fileName.substring( 0, fileName.indexOf( FILE_SUFFIX ) ) ) );
                }
            }
        }
        return datasets;
    }

    @Override
    public Dataset getDataset( String id )
                            throws DataIOException, ConfigurationException {
        if ( datasetCache.containsKey( id ) ) {
            return datasetCache.get( id );
        }
        String fileName = id;
        if ( !fileName.endsWith( FILE_SUFFIX ) ) {
            fileName = fileName + FILE_SUFFIX;
        }
        File f = new File( ConfigurationManager.getConfiguration().getDataDir(), fileName );
        Dataset dataset = DataReader.readDataset( id, f );
        datasetCache.put( id, dataset );
        return dataset;
    }

    @Override
    public String writeDataset( String id, List<FormGroup> formGroups, Map<String, List<DataGroup>> dataGroups )
                            throws DataIOException, ConfigurationException {
        if ( datasetCache.containsKey( id ) ) {
            datasetCache.remove( id );
        }
        return DataWriter.writeDataset( id, formGroups, dataGroups );
    }

    @Override
    public void deleteDataGroup( String grpId, String id )
                            throws ConfigurationException {
        String fileName = id;
        if ( !id.endsWith( FILE_SUFFIX ) ) {
            fileName = fileName + FILE_SUFFIX;
        }
        File f = new File( ConfigurationManager.getConfiguration().getDataDir(), grpId + File.separatorChar + fileName );
        if ( f.exists() && f.isFile() ) {
            LOG.debug( "Delete file " + id + " from group " + grpId );
            f.delete();
            if ( datasetCache.containsKey( id ) ) {
                datasetCache.remove( id );
            }
        }
    }

    @Override
    public List<DataGroup> getDataGroups( String grpId )
                            throws ConfigurationException {
        List<DataGroup> dataGroups = new ArrayList<DataGroup>();
        File f = new File( ConfigurationManager.getConfiguration().getDataDir(), grpId );
        if ( f.exists() && f.isDirectory() ) {
            File[] listFiles = f.listFiles();
            for ( int i = 0; i < listFiles.length; i++ ) {
                if ( listFiles[i].isFile() ) {
                    try {
                        DataGroup dg = new DataGroup( listFiles[i].getName(), DataReader.readDataGroup( listFiles[i] ) );
                        dataGroups.add( dg );
                    } catch ( Exception e ) {
                        LOG.debug( "Could not read file " + listFiles[i].getAbsolutePath(), e );
                        LOG.error( "Could not read file " + listFiles[i].getAbsolutePath() + ": ", e.getMessage() );
                    }

                }
            }
        }
        return dataGroups;
    }

    @Override
    public DataGroup getDataGroup( String grpId, String id )
                            throws ConfigurationException {
        String fileName = id;
        if ( !id.endsWith( FILE_SUFFIX ) ) {
            fileName = fileName + FILE_SUFFIX;
        }
        File f = new File( ConfigurationManager.getConfiguration().getDataDir(), grpId + File.separatorChar + fileName );
        if ( f.exists() && f.isFile() ) {
            LOG.debug( "Read file " + id + " from group " + grpId );
            try {
                return new DataGroup( f.getName(), DataReader.readDataGroup( f ) );
            } catch ( Exception e ) {
                LOG.debug( "Could not read file " + f.getAbsolutePath(), e );
                LOG.error( "Could not read file " + f.getAbsolutePath() + ": ", e.getMessage() );
            }
        }
        return null;
    }

    @Override
    public String writeDataGroup( String id, FormGroup formGroup )
                            throws DataIOException, ConfigurationException {
        return DataWriter.writeDataGroup( id, formGroup );
    }

    @Override
    public void deleteDataset( String id )
                            throws ConfigurationException {
        String fileName = id;
        if ( !id.endsWith( FILE_SUFFIX ) ) {
            fileName = fileName + FILE_SUFFIX;
        }
        File f = new File( ConfigurationManager.getConfiguration().getDataDir(), fileName );
        if ( f.exists() && f.isFile() ) {
            LOG.debug( "Delete dataset with " + id );
            f.delete();
        }
    }

}
