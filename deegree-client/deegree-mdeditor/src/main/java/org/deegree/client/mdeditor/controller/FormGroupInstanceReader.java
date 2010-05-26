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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.component.UISelectItem;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.deegree.client.mdeditor.config.Configuration;
import org.deegree.client.mdeditor.gui.GuiUtils;
import org.deegree.client.mdeditor.model.FormGroupInstance;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class FormGroupInstanceReader {

    private static final Logger LOG = getLogger( FormGroupInstanceReader.class );

    public static List<UISelectItem> getSelectItems( String grpId, String referenceLabel )
                            throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        List<UISelectItem> items = new ArrayList<UISelectItem>();
        String dir = Configuration.getFilesDirURL() + grpId;
        File f = new File( dir );
        if ( f.exists() && f.isDirectory() ) {
            File[] listFiles = f.listFiles();
            for ( int i = 0; i < listFiles.length; i++ ) {
                String label = listFiles[i].getName();
                String value = listFiles[i].getName();
                if ( referenceLabel != null ) {
                    label = replaceProperties( referenceLabel, DatasetReader.read( listFiles[i].getAbsolutePath() ) );
                }
                UISelectItem item = new UISelectItem();
                item.setId( GuiUtils.getUniqueId() );
                item.setItemLabel( label );
                item.setItemValue( value );
                items.add( item );
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

    public static List<FormGroupInstance> getFormGroupInstances( String grpId )
                            throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        List<FormGroupInstance> fgInstances = new ArrayList<FormGroupInstance>();
        String dir = Configuration.getFilesDirURL() + grpId;
        File f = new File( dir );
        if ( f.exists() && f.isDirectory() ) {
            File[] listFiles = f.listFiles();
            for ( int i = 0; i < listFiles.length; i++ ) {
                if ( listFiles[i].isFile() ) {
                    FormGroupInstance instance = new FormGroupInstance( listFiles[i].getName(),
                                                                        DatasetReader.read( listFiles[i] ) );
                    fgInstances.add( instance );

                }
            }
        }
        return fgInstances;

    }

    public static void deleteInstance( String grpId, String fileName ) {
        String dir = Configuration.getFilesDirURL() + grpId + "/" + fileName;
        File f = new File( dir );
        if ( f.exists() && f.isFile() ) {
            LOG.debug( "Delete file " + fileName + " from group " + grpId );
            f.delete();
        }
    }

    public static FormGroupInstance getFormGroupInstance( String grpId, String fileName )
                            throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
        String dir = Configuration.getFilesDirURL() + grpId + "/" + fileName;
        File f = new File( dir );
        if ( f.exists() && f.isFile() ) {
            LOG.debug( "Read file " + fileName + " from group " + grpId );
            return new FormGroupInstance( f.getName(), DatasetReader.read( f ) );
        }
        return null;
    }

}
