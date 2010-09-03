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
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFileChooser;

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

        JFileChooser fc = new JFileChooser();
        File file = null;
        int returnVal = fc.showSaveDialog( tablePanel );
        if ( returnVal == JFileChooser.APPROVE_OPTION ) {
            file = fc.getSelectedFile();

        }
        Writer fout = null;
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
