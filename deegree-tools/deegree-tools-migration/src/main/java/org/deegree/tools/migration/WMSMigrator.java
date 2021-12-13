//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.tools.migration;

import java.io.File;

import org.deegree.commons.annotations.Tool;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
@Tool(value = "Converts a 3.1 or earlier WMS configuration to 3.2 style configurations. Currently only works for feature layers.")
public class WMSMigrator {

    public static void main( String[] args ) {
        if ( args.length == 0 ) {
            System.out.println( "You must specify the workspace location." );
            return;
        }

        File wsloc = new File( args[0] );
        if ( !wsloc.isDirectory() ) {
            System.out.println( "Workspace location does not exist or is not a directory." );
            return;
        }

        Workspace ws = null;
        try {

            ws = new DefaultWorkspace( wsloc );
            ws.initAll();
            new FeatureLayerExtractor( ws ).extract();
            ThemeExtractor.transform(ws);
        } catch ( Throwable e ) {
            System.out.println( "There was a problem transforming the configuration." );
            e.printStackTrace();
        } finally {
            if ( ws != null ) {
                ws.destroy();
            }
        }
    }

}
