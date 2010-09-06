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
package org.deegree.tools.crs.georeferencing.application.handler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.deegree.commons.utils.Pair;
import org.deegree.tools.crs.georeferencing.communication.FileChooser;
import org.deegree.tools.crs.georeferencing.communication.PointTableFrame;

/**
 * Handles the action to write to a FileOutputStream
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FileOutputHandler {

    public FileOutputHandler( PointTableFrame tablePanel ) {

        List<String> list = new ArrayList<String>();
        list.add( "cvs" );

        String desc = " Comma-Separated Values";
        Pair<List<String>, String> supportedFiles = new Pair<List<String>, String>( list, desc );
        List<Pair<List<String>, String>> supportedOpenFiles = new ArrayList<Pair<List<String>, String>>();
        supportedOpenFiles.add( supportedFiles );
        FileChooser fileChooser = new FileChooser( supportedOpenFiles, tablePanel );
        File file = fileChooser.getSelectedFile();

        Writer fout = null;
        if ( file != null ) {
            try {
                fout = new FileWriter( file.getAbsolutePath() );

                BufferedWriter out = new BufferedWriter( fout );
                Enumeration<?> vector = tablePanel.getModel().getDataVector().elements();
                System.out.println( "[FileOutputHandler] " + file.getAbsolutePath() );
                while ( vector.hasMoreElements() ) {
                    Enumeration<?> v = ( (Vector<?>) vector.nextElement() ).elements();
                    while ( v.hasMoreElements() ) {
                        Object o = v.nextElement();
                        if ( o != null ) {
                            System.out.println( "[FileOutputHandler] " + o );

                            out.write( o.toString() );
                        } else {
                            out.write( " " );
                        }
                        if ( v.hasMoreElements() ) {
                            out.write( ", " );
                        }

                    }
                    out.newLine();

                }
                out.close();
            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
