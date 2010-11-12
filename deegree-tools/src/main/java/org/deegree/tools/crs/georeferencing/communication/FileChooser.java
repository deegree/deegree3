//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.tools.crs.georeferencing.communication;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.deegree.commons.utils.Pair;
import org.deegree.tools.rendering.viewer.ViewerFileFilter;

/**
 * Provides a convenient procedure to build a fileChooser dialog.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FileChooser {

    public final static String OPEN_KEY = "lastOpenLocation";

    public final static String LAST_EXTENSION = "lastFileExtension";

    private Preferences prefs;

    private List<Pair<List<String>, String>> supportedFiles;

    private List<ViewerFileFilter> supportedOpenFilter = new ArrayList<ViewerFileFilter>();

    private JFileChooser fileChooser;

    private Component parent;

    /**
     * 
     * @param supportedFiles
     *            an array with extensions and description.
     */
    public FileChooser( List<Pair<List<String>, String>> supportedFiles, Component parent ) {
        this.parent = parent;
        this.supportedFiles = supportedFiles;
        prefs = Preferences.userNodeForPackage( FileChooser.class );
        for ( Pair<List<String>, String> pair : supportedFiles ) {
            supportedOpenFilter.add( new ViewerFileFilter( pair.first, pair.second ) );
        }

        fileChooser = createFileChooser( supportedOpenFilter );

    }

    private JFileChooser createFileChooser( List<ViewerFileFilter> fileFilter ) {
        // Setting up the fileChooser.

        String lastLoc = prefs.get( OPEN_KEY, System.getProperty( "user.home" ) );

        File lastFile = new File( lastLoc );
        if ( !lastFile.exists() ) {
            lastFile = new File( System.getProperty( "user.home" ) );
        }
        JFileChooser fileChooser = new JFileChooser( lastFile );
        fileChooser.setMultiSelectionEnabled( false );
        if ( fileFilter != null && fileFilter.size() > 0 ) {
            // the *.* file filter is off
            fileChooser.setAcceptAllFileFilterUsed( false );
            String lastExtension = prefs.get( LAST_EXTENSION, "*" );
            FileFilter selected = fileFilter.get( 0 );
            for ( ViewerFileFilter filter : fileFilter ) {
                fileChooser.setFileFilter( filter );
                if ( filter.accepts( lastExtension ) ) {
                    selected = filter;
                }
            }

            fileChooser.setFileFilter( selected );
        }
        return fileChooser;
    }

    /**
     * 
     * @return the selected file path, could be <Code>null</Code>.
     */
    public String getSelectedFilePath() {
        String path = null;
        int result = fileChooser.showOpenDialog( parent );
        if ( JFileChooser.APPROVE_OPTION == result ) {
            File selectedFile = fileChooser.getSelectedFile();
            if ( selectedFile != null ) {
                path = selectedFile.getAbsolutePath();
            }
        }
        return path;
    }

    /**
     * 
     * @return the selected file, could be <Code>null</Code>.
     */
    public File getSelectedFile() {
        File selectedFile = null;
        int result = fileChooser.showSaveDialog( parent );
        if ( JFileChooser.APPROVE_OPTION == result ) {
            selectedFile = fileChooser.getSelectedFile();
        }
        return selectedFile;
    }

}
