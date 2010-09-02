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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.deegree.tools.crs.georeferencing.communication.PointTableFrame;

/**
 * Handles the action to read from a file.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FileInputHandler {

    private Vector data;

    public FileInputHandler( String choosedFile, PointTableFrame tablePanel ) {
        data = new Vector();

        FileReader fr = null;
        try {
            fr = new FileReader( choosedFile );
        } catch ( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader( fr );
        String s = "";
        try {
            while ( ( s = br.readLine() ) != null ) {
                Vector v = new Vector();
                String[] a = s.split( ", " );
                for ( String splitted : a ) {
                    v.add( splitted );
                }
                data.add( v );
                // System.out.println( "[FileInputHandler] dataWhile " + data );
            }
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // System.out.println( "[FileInputHandler] data " + data );
        tablePanel.getModel().setDataVector( data, tablePanel.getColumnNamesAsVector() );
        tablePanel.getModel().fireTableDataChanged();

    }
}
