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
package org.deegree.client.mdeditor.controller;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.component.UISelectItem;

import org.deegree.client.mdeditor.config.Configuration;
import org.deegree.client.mdeditor.gui.GuiUtils;
import org.deegree.client.mdeditor.model.FormGroupInstance;
import org.slf4j.Logger;

/**
 * handles all jobs concerning reading and writing form groups
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class FormGroupHandler {

    private static final Logger LOG = getLogger( FormGroupHandler.class );

    /**
     * creates select items out of the instances of a form group
     * 
     * TODO: is this the best place to create gui elements???
     * 
     * @param grpId
     *            the id of the group
     * @param referenceLabel
     *            the pattern describing the label
     * @return a list of all available instances of the form group with the given grpId
     */
    public static List<UISelectItem> getSelectItems( String grpId, String referenceLabel ) {
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
                        label = replaceProperties( referenceLabel, DatasetReader.read( listFiles[i].getAbsolutePath() ) );
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

    private static String replaceProperties( String referenceLabel, Map<String, Object> map ) {
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

    /**
     * @return a list of all datasets
     */
    public static List<String> getDatasets() {
        List<String> datasets = new ArrayList<String>();
        String dir = Configuration.getFilesDirURL();
        File f = new File( dir );
        if ( f.exists() && f.isDirectory() ) {
            File[] listFiles = f.listFiles();
            for ( int i = 0; i < listFiles.length; i++ ) {
                String fileName = listFiles[i].getName();
                if ( listFiles[i].isFile() && fileName.endsWith( ".xml" ) ) {
                    datasets.add( fileName.substring( 0, fileName.indexOf( ".xml" ) ) );
                }
            }
        }
        return datasets;
    }

    /**
     * @param grpId
     *            the id of the group
     * @return a list of all form group instances of the group with the given id
     */
    public static List<FormGroupInstance> getFormGroupInstances( String grpId ) {
        List<FormGroupInstance> fgInstances = new ArrayList<FormGroupInstance>();
        String dir = Configuration.getFilesDirURL() + grpId;
        File f = new File( dir );
        if ( f.exists() && f.isDirectory() ) {
            File[] listFiles = f.listFiles();
            for ( int i = 0; i < listFiles.length; i++ ) {
                if ( listFiles[i].isFile() ) {
                    try {
                        FormGroupInstance instance = new FormGroupInstance( listFiles[i].getName(),
                                                                            DatasetReader.read( listFiles[i] ) );
                        fgInstances.add( instance );
                    } catch ( Exception e ) {
                        LOG.debug( "Could not read file " + listFiles[i].getAbsolutePath(), e );
                        LOG.error( "Could not read file " + listFiles[i].getAbsolutePath() + ": ", e.getMessage() );
                    }

                }
            }
        }
        return fgInstances;

    }

    /**
     * Delete the instance of the form group with the given id and name
     * 
     * @param grpId
     *            the id of the group
     * @param name
     *            the name of the file (with format suffix)
     */
    public static void deleteInstance( String grpId, String name ) {
        String dir = Configuration.getFilesDirURL() + grpId + "/" + name;
        File f = new File( dir );
        if ( f.exists() && f.isFile() ) {
            LOG.debug( "Delete file " + name + " from group " + grpId );
            f.delete();
        }
    }

    /**
     * @param grpId
     *            the id of the group
     * @param name
     *            the name of the file (with format suffix)
     * @return the form group of the form group with the given id and name; null, if the instance could not be read
     */
    public static FormGroupInstance getFormGroupInstance( String grpId, String name ) {
        String dir = Configuration.getFilesDirURL() + grpId + "/" + name;
        File f = new File( dir );
        if ( f.exists() && f.isFile() ) {
            LOG.debug( "Read file " + name + " from group " + grpId );
            try {
                return new FormGroupInstance( f.getName(), DatasetReader.read( f ) );
            } catch ( Exception e ) {
                LOG.debug( "Could not read file " + f.getAbsolutePath(), e );
                LOG.error( "Could not read file " + f.getAbsolutePath() + ": ", e.getMessage() );
            }
        }
        return null;
    }

}
