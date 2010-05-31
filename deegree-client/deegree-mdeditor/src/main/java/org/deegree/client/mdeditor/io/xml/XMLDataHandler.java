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
import java.util.List;
import java.util.Map;

import javax.faces.component.UISelectItem;

import org.deegree.client.mdeditor.configuration.Configuration;
import org.deegree.client.mdeditor.gui.GuiUtils;
import org.deegree.client.mdeditor.io.DataHandler;
import org.deegree.client.mdeditor.io.DataIOException;
import org.deegree.client.mdeditor.model.FormGroup;
import org.deegree.client.mdeditor.model.DataGroup;
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

    static final String FILE_SUFFIX = ".xml";

    private static final Logger LOG = getLogger( XMLDataHandler.class );

    @Override
    public List<UISelectItem> getSelectItems( String grpId, String referenceLabel ) {
        List<UISelectItem> items = new ArrayList<UISelectItem>();
        String dir = Configuration.getFilesDirURL() + grpId;
        File f = new File( dir );
        if ( f.exists() && f.isDirectory() ) {
            File[] listFiles = f.listFiles();
            for ( int i = 0; i < listFiles.length; i++ ) {
                String label = listFiles[i].getName();
                String value = listFiles[i].getName();
                try {
                    if ( referenceLabel != null ) {
                        label = replaceProperties( referenceLabel, DataReader.read( listFiles[i] ) );
                    }
                    UISelectItem item = new UISelectItem();
                    item.setId( GuiUtils.getUniqueId() );
                    item.setItemLabel( label );
                    item.setItemValue( value );
                    items.add( item );
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
    public List<String> getDatasetIds() {
        List<String> datasets = new ArrayList<String>();
        String dir = Configuration.getFilesDirURL();
        File d = new File( dir );
        if ( d.exists() && d.isDirectory() ) {
            File[] listFiles = d.listFiles();
            for ( int i = 0; i < listFiles.length; i++ ) {
                String fileName = listFiles[i].getName();
                if ( listFiles[i].isFile() && fileName.endsWith( FILE_SUFFIX ) ) {
                    datasets.add( fileName.substring( 0, fileName.indexOf( FILE_SUFFIX ) ) );
                }
            }
        }
        return datasets;
    }

    @Override
    public Map<String, Object> getDataset( String id )
                            throws DataIOException {
        String fileName = id;
        if ( !fileName.endsWith( FILE_SUFFIX ) ) {
            fileName = fileName + FILE_SUFFIX;
        }
        return DataReader.read( new File( Configuration.getFilesDirURL(), fileName ) );
    }

    @Override
    public void deleteDataGroup( String grpId, String id ) {
        String fileName = id;
        if ( !id.endsWith( FILE_SUFFIX ) ) {
            fileName = fileName + FILE_SUFFIX;
        }
        File f = new File( Configuration.getFilesDirURL() + grpId, fileName );
        if ( f.exists() && f.isFile() ) {
            LOG.debug( "Delete file " + id + " from group " + grpId );
            f.delete();
        }
    }

    @Override
    public DataGroup getDataGroup( String grpId, String id ) {
        String fileName = id;
        if ( !id.endsWith( FILE_SUFFIX ) ) {
            fileName = fileName + FILE_SUFFIX;
        }
        File f = new File( Configuration.getFilesDirURL() + grpId, fileName );
        if ( f.exists() && f.isFile() ) {
            LOG.debug( "Read file " + id + " from group " + grpId );
            try {
                return new DataGroup( f.getName(), DataReader.read( f ) );
            } catch ( Exception e ) {
                LOG.debug( "Could not read file " + f.getAbsolutePath(), e );
                LOG.error( "Could not read file " + f.getAbsolutePath() + ": ", e.getMessage() );
            }
        }
        return null;
    }

    @Override
    public List<DataGroup> getDataGroups( String grpId ) {
        List<DataGroup> dataGroups = new ArrayList<DataGroup>();
        String dir = Configuration.getFilesDirURL() + grpId;
        File f = new File( dir );
        if ( f.exists() && f.isDirectory() ) {
            File[] listFiles = f.listFiles();
            for ( int i = 0; i < listFiles.length; i++ ) {
                if ( listFiles[i].isFile() ) {
                    try {
                        DataGroup dg = new DataGroup( listFiles[i].getName(), DataReader.read( listFiles[i] ) );
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
    public String writeDataGroup( String id, FormGroup formGroup )
                            throws DataIOException {
        return DataWriter.writeDataGroup( id, formGroup );
    }

    @Override
    public String writeDataset( String id, List<FormGroup> formGroups )
                            throws DataIOException {
        return DataWriter.writeDataset( id, formGroups );
    }

    @Override
    public void deleteDataset( String id ) {
        String fileName = id;
        if ( !id.endsWith( FILE_SUFFIX ) ) {
            fileName = fileName + FILE_SUFFIX;
        }
        File f = new File( Configuration.getFilesDirURL(), fileName );
        if ( f.exists() && f.isFile() ) {
            LOG.debug( "Delete dataset with " + id );
            f.delete();
        }
    }

}
