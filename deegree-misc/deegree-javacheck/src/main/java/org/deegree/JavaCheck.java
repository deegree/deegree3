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
package org.deegree;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Checks for a suitable Java installation (>= 1.6.0 Update 4).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class JavaCheck {

    private static void alert() {

        String msg = "You need to have Java SDK 1.6 (Update 4) or later installed in order to run this software.";
        String msg2 = "See http://wiki.deegree.org/deegreeWiki/deegree3/SystemRequirements for details.";
        System.err.println( msg );

        try {
            Frame window = new Frame( "Invisible frame" );
            window.pack();
            Dialog d = new Dialog( window, "Incompatible Java installation", true );
            d.setLayout( new GridLayout( 3, 1 ) );

            // Create an OK button
            Button ok = new Button( "OK" );
            ok.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    System.exit( 5 );
                }
            } );

            d.add( new Label( msg, Label.CENTER ) );
            d.add( new Label( msg2, Label.CENTER ) );
            d.add( ok );

            // Show dialog
            d.pack();

            d.setLocation( ( Toolkit.getDefaultToolkit().getScreenSize().width / 2 ) - ( d.getSize().width / 2 ),
                           ( java.awt.Toolkit.getDefaultToolkit().getScreenSize().height / 2 )
                                                   - ( d.getSize().height / 2 ) );

            d.setVisible( true );

        } catch ( Throwable t ) {
            t.printStackTrace();
            System.exit( 5 );
        }
    }

    public static void main( String[] args ) {

        String javaVersion = System.getProperty( "java.version" );

        String[] parts = javaVersion.split( "\\." );
        if ( parts.length != 3 ) {
            System.err.println( "Java VM version (" + javaVersion + ") does not have expected format. Skipping check." );
        }

        int first = Integer.parseInt( parts[0] );
        if ( first < 1 ) {
            alert();
        } else if ( first > 1 ) {
            return;
        }

        int second = Integer.parseInt( parts[1] );
        if ( second < 6 ) {
            alert();
        } else if ( first > 6 ) {
            return;
        }

        String third = parts[2];
        String[] thirdParts = third.split( "_" );
        int third1 = Integer.parseInt( thirdParts[0] );
        if ( third1 > 0 ) {
            return;
        }

        if ( thirdParts.length < 1 ) {
            alert();
        }
        String update = thirdParts[1];
        String[] updateParts = update.split( "-" );
        int updateFirst = Integer.parseInt( updateParts[0] );
        if ( updateFirst < 4 ) {
            alert();
        }
    }
}