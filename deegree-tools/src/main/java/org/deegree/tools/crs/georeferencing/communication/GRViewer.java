//$HeadURL$$
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.deegree.rendering.r3d.opengl.JOGLChecker;
import org.deegree.tools.annotations.Tool;
import org.deegree.tools.crs.georeferencing.application.Controller;
import org.deegree.tools.crs.georeferencing.application.ParameterStore;
import org.deegree.tools.crs.georeferencing.model.Scene2D;
import org.deegree.tools.crs.georeferencing.model.Scene2DImplWMS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Main class that calls all the relevant objects to build the georeferencing viewer.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@Tool("Initializes the georeferencing tool. ")
public class GRViewer {
    private static final Logger LOG = LoggerFactory.getLogger( GRViewer.class );

    /**
     * @param args
     * @throws IOException
     */
    public static void main( String[] args )
                            throws IOException {
        if ( args.length == 0 ) {
            outputHelp();
        }
        Map<String, String> params = new HashMap<String, String>( 6 );
        for ( int i = 0; i < args.length; i++ ) {
            String arg = args[i];
            if ( arg != null && !"".equals( arg.trim() ) ) {
                arg = arg.trim();
                if ( arg.equalsIgnoreCase( "-?" ) || arg.equalsIgnoreCase( "-h" ) ) {
                    outputHelp();
                } else {
                    if ( i + 1 < args.length ) {
                        String val = args[++i];
                        if ( val != null ) {
                            params.put( arg, val.trim() );
                        } else {
                            System.out.println( "Invalid value for parameter: " + arg );
                        }
                    } else {
                        System.out.println( "No value for parameter: " + arg );
                    }
                }
            }
        }
        String geoRefSource = params.get( "-geoRefSource" );
        if ( geoRefSource == null || "".equals( geoRefSource.trim() ) ) {
            LOG.error( "No source found for the georeferencing map (-geoRefSource parameter)" );
            System.exit( 1 );
        }
        String geoRefCRS = params.get( "-geoRefCRS" );
        if ( geoRefCRS == null || "".equals( geoRefCRS.trim() ) ) {
            LOG.error( "No CRS found for the georeferencing map (-geoRefCRS parameter)" );
            System.exit( 1 );
        }
        String geoRefFormat = params.get( "-geoRefFormat" );
        if ( geoRefFormat == null || "".equals( geoRefFormat.trim() ) ) {
            LOG.error( "No format found for the georeferencing map (-geoRefFormat parameter)" );
            System.exit( 1 );
        }
        String geoRefLayers = params.get( "-geoRefLayers" );
        if ( geoRefLayers == null || "".equals( geoRefLayers.trim() ) ) {
            LOG.error( "No Layers found for the georeferencing map (-geoRefLayers parameter)" );
            System.exit( 1 );
        }
        String geoRefBBox = params.get( "-geoRefBBox" );
        if ( geoRefBBox == null || "".equals( geoRefBBox.trim() ) ) {
            LOG.error( "No boundingBox found for the georeferencing map (-geoRefBBox parameter)" );
            System.exit( 1 );
        }

        String source3d = params.get( "-source3d" );
        if ( source3d == null || "".equals( source3d.trim() ) ) {
            LOG.error( "No source for referencing found (-source3d parameter)" );
            System.exit( 1 );
        }

        LOG.info( "Checking for JOGL." );
        JOGLChecker.check();
        LOG.info( "JOGL check ok." );

        System.out.println( "[MAIN] " + geoRefSource + "\n" + geoRefCRS + "\n" + geoRefFormat + "\n" + geoRefLayers
                            + "\n" + geoRefBBox + "\n" + source3d );

        ParameterStore store = new ParameterStore( geoRefSource, geoRefCRS, geoRefFormat, geoRefLayers, geoRefBBox,
                                                   source3d );

        GRViewerGUI gui = new GRViewerGUI();

        Scene2D scene2d = new Scene2DImplWMS();

        new Controller( gui, scene2d, store );

        gui.setVisible( true );

    }

    private static void outputHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append( "The GRViewer program should help to georeference a scene which has no spatial coordinate system.\n" );
        sb.append( "to a scene with real worldCoordinates.\n" );
        sb.append( "Following parameters are supported:\n" );
        sb.append( "-geoRefSource the source of the georeferencing map, e.g. http://localhost:8080/deegree-wms-cite\n" );
        sb.append( "-geoRefCRS the CRS of the georeferencing map, e.g. EPSG:4326\n" );
        sb.append( "-geoRefFormat the output format of the georeferencing map, e.g. image/png \n" );
        sb.append( "-geoRefLayers the layers of the georeferncing map that should be requested - look into the GetCapabilities document.\n" );
        sb.append( "-geoRefBBox the boundingbox of the request in format \'minX minY maxX maxY\'.\n" );
        sb.append( "-source3d the source of the scene that should be georeferenced /path/of/the_3d_scene (CityGML file at the moment) .\n" );
        sb.append( "-?|-h output this text\n" );
        System.out.println( sb.toString() );
        System.exit( 1 );
    }

}
