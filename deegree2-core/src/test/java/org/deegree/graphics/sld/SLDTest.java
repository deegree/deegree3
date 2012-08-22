//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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

package org.deegree.graphics.sld;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import alltests.Configuration;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: poth $
 *
 * @version. $Revision: 6251 $, $Date: 2007-03-19 16:59:28 +0100 (Mo, 19 Mrz 2007) $
 */
public class SLDTest extends TestCase {

    private StyledLayerDescriptor sld = null;
    private Map<String,AbstractLayer> mappedLayers = null;

    public static Test suite() {
        return new TestSuite( SLDTest.class );
    }

    @Override
    protected void setUp()
                            throws Exception {
        super.setUp();
        String s = Configuration.getSLDBaseDir().toURI().toASCIIString();
        URL url = new URL( s + '/' + Configuration.SLD1_EXAMPLE );
        sld = SLDFactory.createSLD( url );
    }

    private Map<String,AbstractLayer> mapLayers() {
        if ( mappedLayers == null ) {
            AbstractLayer[] layers = sld.getLayers();
            mappedLayers = new HashMap<String, AbstractLayer>();
            for ( int i = 0; i < layers.length; i++ ) {
                mappedLayers.put( layers[i].getName(), layers[i] );
            }
        }
        return mappedLayers;
    }

    private Map<String,AbstractStyle> mapStyles( AbstractLayer layer ) {
        AbstractStyle[] styles = layer.getStyles();
        Map<String,AbstractStyle> styleMap = new HashMap<String, AbstractStyle>();
        for ( int i = 0; i < styles.length; i++ ) {
            styleMap.put( styles[i].getName(), styles[i] );
        }
        return styleMap;
    }

    public void testNamedLayerNames() throws Exception {

        AbstractLayer[] layers = sld.getLayers();
        assertNotNull( layers );
        assertTrue( layers.length > 0 );
        Map<String,AbstractLayer> map = new HashMap<String, AbstractLayer>();
        for ( int i = 0; i < layers.length; i++ ) {
            map.put( layers[i].getName(), layers[i] );
        }
        assertNotNull( map.get( "Counties" ) );
        assertNotNull( map.get( "ElevationContours" ) );
        assertNotNull( map.get( "EnergyResources" ) );
        assertNotNull( map.get( "Lake" ) );
        assertNotNull( map.get( "Railroads" ) );
        assertNotNull( map.get( "BasicRoads" ) );
        assertNotNull( map.get( "StateBoundary" ) );
        assertNotNull( map.get( "Vegetation" ) );
        assertNotNull( map.get( "europe:major_rivers" ) );
        assertNotNull( map.get( "MyLayer" ) );
        assertNotNull( map.get( "europe:major_urban_places" ) );

    }

    public void testUserStylesNames() throws Exception {
        mapLayers();
        AbstractLayer layer = mappedLayers.get( "Counties" );
        Map<String,AbstractStyle> styleMap = mapStyles( layer );
        assertNotNull( styleMap.get( "default:Counties" ) );
        assertNotNull( styleMap.get( "GreyCounties" ) );
        assertNotNull( styleMap.get( "ColourfulCounties" ) );

        layer = mappedLayers.get( "ElevationContours" );
        styleMap = mapStyles( layer );
        assertNotNull( styleMap.get( "default:ElevationContours" ) );

        layer = mappedLayers.get( "EnergyResources" );
        styleMap = mapStyles( layer );
        assertNotNull( styleMap.get( "default:EnergyResources" ) );

        layer = mappedLayers.get( "Lake" );
        styleMap = mapStyles( layer );
        assertNotNull( styleMap.get( "default:Lake" ) );
    }

    public void testCssParameterCounties_default_Counties_Rule1() throws Exception {
        mapLayers();
        AbstractLayer layer = mappedLayers.get( "Counties" );
        Map<String,AbstractStyle> styleMap = mapStyles( layer );
        UserStyle us = (UserStyle)styleMap.get( "default:Counties" );
        Rule rule = us.getFeatureTypeStyles()[0].getRules()[0];
        PolygonSymbolizer sym = (PolygonSymbolizer)rule.getSymbolizers()[0];

        // fill parameter
        Fill fill = sym.getFill();
        Map css = fill.getCssParameters();
        CssParameter param = (CssParameter)css.get( "fill" );
        assertEquals( "#990000", param.getValue( null ) );
        param = (CssParameter)css.get( "fill-opacity" );
        assertEquals( "1.0", param.getValue( null ) );

        // stroke parameter
        Stroke stroke = sym.getStroke();
        css = stroke.getCssParameters();
        param = (CssParameter)css.get( "stroke" );
        assertEquals( "#000000", param.getValue( null ) );
        param = (CssParameter)css.get( "stroke-opacity" );
        assertEquals( "1.0", param.getValue( null ) );
        param = (CssParameter)css.get( "stroke-width" );
        assertEquals( "1", param.getValue( null ) );
        param = (CssParameter)css.get( "stroke-dasharray" );
        assertEquals( "1", param.getValue( null ) );
    }

    public void testCssParameterElevationContours_default_ElevationContours_Rule1() throws Exception {
        mapLayers();
        AbstractLayer layer = mappedLayers.get( "ElevationContours" );
        Map<String,AbstractStyle> styleMap = mapStyles( layer );
        UserStyle us = (UserStyle)styleMap.get( "default:ElevationContours" );
        Rule rule = us.getFeatureTypeStyles()[0].getRules()[0];
        LineSymbolizer sym = (LineSymbolizer)rule.getSymbolizers()[0];
        assertEquals( "app:contourLine", sym.getGeometry().getPropertyPath().getAsString() );

        Stroke stroke = sym.getStroke();
        Map css = stroke.getCssParameters();
        CssParameter param = (CssParameter)css.get( "stroke" );
        assertEquals( "#588c58", param.getValue( null ) );
        param = (CssParameter)css.get( "stroke-opacity" );
        assertEquals( "1.0", param.getValue( null ) );
        param = (CssParameter)css.get( "stroke-width" );
        assertEquals( "1", param.getValue( null ) );
        param = (CssParameter)css.get( "stroke-dasharray" );
        assertEquals( "1", param.getValue( null ) );
    }


    public void testMyLayer() throws Exception {
        mapLayers();
        UserLayer layer = (UserLayer)mappedLayers.get( "MyLayer" );
        assertEquals( "http://localhost:8082/deegreewfs/wfs", layer.getRemoteOWS().getOnlineResource().toExternalForm() );
        assertEquals( "WFS", layer.getRemoteOWS().getService() );
        FeatureTypeConstraint ftc = layer.getLayerFeatureConstraints().getFeatureTypeConstraint()[0];
        assertEquals( "Europe", ftc.getFeatureTypeName().getFormattedString() );
        assertNotNull( ftc.getFilter() );
    }


}
