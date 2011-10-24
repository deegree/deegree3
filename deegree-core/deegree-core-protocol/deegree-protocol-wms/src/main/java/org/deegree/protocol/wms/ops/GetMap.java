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
package org.deegree.protocol.wms.ops;

import static java.awt.Color.decode;
import static java.awt.Color.white;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.deegree.commons.utils.ArrayUtils.splitAsDoubles;
import static org.deegree.commons.utils.CollectionUtils.map;
import static org.deegree.commons.utils.CollectionUtils.unzipPair;
import static org.deegree.layer.dims.Dimension.parseTyped;
import static org.deegree.protocol.wms.WMSConstants.VERSION_111;
import static org.deegree.protocol.wms.WMSConstants.VERSION_130;
import static org.deegree.protocol.wms.ops.LayerRef.FROM_NAMES;
import static org.deegree.protocol.wms.ops.SLDParser.parse;
import static org.deegree.style.utils.Styles.getStyleFilters;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Color;
import java.io.StringReader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import java_cup.runtime.Symbol;

import javax.xml.stream.XMLInputFactory;

import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.CollectionUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.filter.Filter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.logical.And;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.layer.dims.DimensionLexer;
import org.deegree.layer.dims.parser;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.wms.Utils;
import org.deegree.protocol.wms.ops.GetMapExtensions.Antialias;
import org.deegree.protocol.wms.ops.GetMapExtensions.Interpolation;
import org.deegree.protocol.wms.ops.GetMapExtensions.Quality;
import org.deegree.style.se.unevaluated.Style;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetMap {

    private static final Logger LOG = getLogger( GetMap.class );

    private static GeometryFactory fac = new GeometryFactory();

    private ICRS crs;

    private Envelope bbox;

    private LinkedList<LayerRef> layers = new LinkedList<LayerRef>();

    private LinkedList<StyleRef> styles = new LinkedList<StyleRef>();

    private HashMap<String, Filter> filters = new HashMap<String, Filter>();

    private String format;

    private int width, height;

    private boolean transparent;

    private Color bgcolor = white;

    private HashMap<String, List<?>> dimensions = new HashMap<String, List<?>>();

    private double scale;

    private double pixelSize = 0.28;

    private double resolution;

    private GetMapExtensions extensions;

    private Map<String, String> parameterMap = new HashMap<String, String>();

    /**
     * @param map
     * @param version
     * @param service
     * @throws OWSException
     */
    public GetMap( Map<String, String> map, Version version, GetMapExtensions exts ) throws OWSException {
        if ( version.equals( VERSION_111 ) ) {
            parse111( map, exts );
        }
        if ( version.equals( VERSION_130 ) ) {
            parse130( map, exts );
        }
        parameterMap.putAll( map );
        try {
            scale = Utils.calcScaleWMS130( width, height, bbox, crs );
            LOG.debug( "GetMap request has a WMS 1.3.0/SLD scale of '{}'.", scale );
            resolution = max( bbox.getSpan0() / width, bbox.getSpan1() / height );
            LOG.debug( "Resolution per pixel is {}.", resolution );
        } catch ( ReferenceResolvingException e ) {
            LOG.trace( "Stack trace:", e );
            LOG.warn( "The scale of a GetMap request could not be calculated: '{}'.", e.getLocalizedMessage() );
        }
    }

    /**
     * @param service
     * @param layers
     * @param styles
     * @param width
     * @param height
     * @param boundingBox
     */
    public GetMap( Collection<LayerRef> layers, Collection<StyleRef> styles, int width, int height,
                   Envelope boundingBox, GetMapExtensions exts ) {
        this.layers.addAll( layers );
        this.styles.addAll( styles );
        this.width = width;
        this.height = height;
        this.bbox = boundingBox;
        this.crs = boundingBox.getCoordinateSystem();
        this.bgcolor = white;
        format = "image/png";
        transparent = false;
        handleVSPs( new HashMap<String, String>(), exts );
        try {
            scale = Utils.calcScaleWMS130( width, height, bbox, crs );
            LOG.debug( "GetMap request has a WMS 1.3.0/SLD scale of '{}'.", scale );
            resolution = max( bbox.getSpan0() / width, bbox.getSpan1() / height );
            LOG.debug( "Resolution per pixel is {}.", resolution );
        } catch ( ReferenceResolvingException e ) {
            LOG.trace( "Stack trace:", e );
            LOG.warn( "The scale of a GetMap request could not be calculated: '{}'.", e.getLocalizedMessage() );
        }
    }

    public GetMap( List<String> layers, int width, int height, Envelope envelope, ICRS crs, String format,
                   boolean transparent ) {
        this.layers = map( layers, FROM_NAMES );
        this.width = width;
        this.height = height;
        this.bbox = envelope;
        this.crs = crs;
        this.format = format;
        this.transparent = transparent;
    }

    public GetMap( List<Pair<String, String>> layers, int width, int height, Envelope boundingBox, String format,
                   boolean transparent ) {
        for ( Pair<String, String> layer : layers ) {
            this.layers.add( new LayerRef( layer.first ) );
            this.styles.add( new StyleRef( layer.second ) );
        }
        this.width = width;
        this.height = height;
        this.bbox = boundingBox;
        this.crs = boundingBox.getCoordinateSystem();
        this.bgcolor = white;
        this.format = format;
        this.transparent = transparent;
        try {
            scale = Utils.calcScaleWMS130( width, height, bbox, crs );
            LOG.debug( "GetMap request has a WMS 1.3.0/SLD scale of '{}'.", scale );
            resolution = max( bbox.getSpan0() / width, bbox.getSpan1() / height );
            LOG.debug( "Resolution per pixel is {}.", resolution );
        } catch ( ReferenceResolvingException e ) {
            LOG.trace( "Stack trace:", e );
            LOG.warn( "The scale of a GetMap request could not be calculated: '{}'.", e.getLocalizedMessage() );
        }
    }

    private void parse111( Map<String, String> map, GetMapExtensions exts )
                            throws OWSException {
        String c = map.get( "SRS" );
        if ( c == null || c.trim().isEmpty() ) {
            throw new OWSException( "The SRS parameter is missing.", OWSException.MISSING_PARAMETER_VALUE );
        }
        crs = getCRS111( c );

        String box = map.get( "BBOX" );
        if ( box == null || box.trim().isEmpty() ) {
            throw new OWSException( "The BBOX parameter is missing.", OWSException.MISSING_PARAMETER_VALUE );
        }

        double[] vals = splitAsDoubles( box, "," );
        // hack to work around ESRI ArcGIS Explorer localized bboxes...
        if ( vals.length == 8 ) {
            String[] ss = box.split( "," );
            vals = new double[] { parseDouble( ss[0] + "." + ss[1] ), parseDouble( ss[2] + "." + ss[3] ),
                                 parseDouble( ss[4] + "." + ss[5] ), parseDouble( ss[6] + "." + ss[7] ) };
        }
        if ( vals.length != 4 ) {
            throw new OWSException( "The bounding box parameter value " + box
                                    + " is in the wrong format (too many values).",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }

        if ( vals[2] <= vals[0] ) {
            throw new OWSException( "The maxx component of the bounding box is smaller than the minx component.",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }
        if ( vals[3] <= vals[1] ) {
            throw new OWSException( "The maxy component of the bounding box is smaller than the miny component.",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }

        bbox = fac.createEnvelope( new double[] { vals[0], vals[1] }, new double[] { vals[2], vals[3] }, crs );

        handleCommon( map, exts );
    }

    static LinkedList<StyleRef> handleKVPStyles( String ss, int numLayers )
                            throws OWSException {
        LinkedList<StyleRef> styles = new LinkedList<StyleRef>();

        // result is a list with 'default' where default styles were requested
        if ( ss.trim().isEmpty() ) {
            for ( int i = 0; i < numLayers; ++i ) {
                styles.add( new StyleRef( "default" ) );
            }
        } else {
            // to work around #split limitations
            if ( ss.startsWith( "," ) ) {
                ss = "default" + ss;
            }
            if ( ss.endsWith( "," ) ) {
                ss = ss + "default";
            }
            String[] styls = ss.split( "," );
            if ( styls.length != numLayers ) {
                throw new OWSException( styls.length + "styles have been specified, and " + numLayers
                                        + " layers (should be equal).", OWSException.INVALID_PARAMETER_VALUE );
            }

            for ( int i = 0; i < numLayers; ++i ) {
                if ( styls[i].isEmpty() || styls[i].equals( "default" ) ) {
                    styles.add( new StyleRef( "default" ) );
                } else {
                    styles.add( new StyleRef( styls[i] ) );
                }
            }
        }

        return styles;
    }

    private void handleCommon( Map<String, String> map, GetMapExtensions exts )
                            throws OWSException {
        String ls = map.get( "LAYERS" );
        String sld = map.get( "SLD" );
        String sldBody = map.get( "SLD_BODY" );

        if ( ( ls == null || ls.trim().isEmpty() ) && sld == null && sldBody == null ) {
            throw new OWSException( "The LAYERS parameter is missing.", OWSException.MISSING_PARAMETER_VALUE );
        }
        layers = ls == null ? new LinkedList<LayerRef>() : CollectionUtils.map( ls.split( "," ), LayerRef.FROM_NAMES );

        if ( layers.size() == 1 && layers.get( 0 ).getName().isEmpty() ) {
            layers.clear();
        }

        String ss = map.get( "STYLES" );
        if ( ss == null && sld == null && sldBody == null ) {
            throw new OWSException( "The STYLES parameter is missing.", OWSException.MISSING_PARAMETER_VALUE );
        }

        if ( sld == null && sldBody == null ) {
            this.styles = handleKVPStyles( ss, layers.size() );
        } else {
            // TODO think about whether STYLES has to be handled here as well
            handleSLD( sld, sldBody, layers );
        }

        String psize = map.get( "PIXELSIZE" );
        if ( psize != null ) {
            try {
                pixelSize = Double.parseDouble( psize );
            } catch ( NumberFormatException e ) {
                LOG.warn( "The value of PIXELSIZE could not be parsed as a number." );
                LOG.trace( "Stack trace:", e );
            }
        }

        format = map.get( "FORMAT" );
        if ( format == null ) {
            throw new OWSException( "The FORMAT parameter is missing.", OWSException.MISSING_PARAMETER_VALUE );
        }

        String w = map.get( "WIDTH" );
        if ( w == null ) {
            throw new OWSException( "The WIDTH parameter is missing.", OWSException.MISSING_PARAMETER_VALUE );
        }
        try {
            width = parseInt( w );
        } catch ( NumberFormatException e ) {
            throw new OWSException( "The WIDTH parameter value is not a number (was " + w + ").",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }

        String h = map.get( "HEIGHT" );
        if ( h == null ) {
            throw new OWSException( "The HEIGHT parameter is missing.", OWSException.MISSING_PARAMETER_VALUE );
        }
        try {
            height = parseInt( h );
        } catch ( NumberFormatException e ) {
            throw new OWSException( "The HEIGHT parameter value is not a number (was " + h + ").",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }
        String t = map.get( "TRANSPARENT" );
        transparent = t != null && t.equalsIgnoreCase( "true" );
        if ( transparent && ( format.indexOf( "gif" ) != -1 || format.indexOf( "png" ) != -1 ) ) {
            bgcolor = new Color( 255, 255, 255, 0 );
        } else {
            String col = map.get( "BGCOLOR" );
            if ( col != null ) {
                bgcolor = decode( col );
            }
        }

        dimensions = parseDimensionValues( map );

        handleVSPs( map, exts );
    }

    private void handleVSPs( Map<String, String> map, GetMapExtensions defaults ) {
        if ( defaults == null ) {
            defaults = new GetMapExtensions();
        }
        extensions = new GetMapExtensions();
        handleEnumVSP( Quality.class, extensions.getQualities(), Quality.NORMAL, map.get( "QUALITY" ),
                       defaults.getQualities() );
        handleEnumVSP( Interpolation.class, extensions.getInterpolations(), Interpolation.NEARESTNEIGHBOR,
                       map.get( "INTERPOLATION" ), defaults.getInterpolations() );
        handleEnumVSP( Antialias.class, extensions.getAntialiases(), Antialias.BOTH, map.get( "ANTIALIAS" ),
                       defaults.getAntialiases() );
        String maxFeatures = map.get( "MAX_FEATURES" );
        if ( maxFeatures == null ) {
            for ( LayerRef l : this.layers ) {
                Integer max = defaults.getMaxFeatures( l.getName() );
                if ( max == null ) {
                    max = 10000;
                    LOG.debug( "Using global max features setting of {}.", max );
                }
                extensions.getMaxFeatures().put( l.getName(), max );
            }
        } else {
            String[] mfs = maxFeatures.split( "," );
            Map<String, Integer> mfdefaults = defaults.getMaxFeatures();
            if ( mfs.length == this.layers.size() ) {
                for ( int i = 0; i < mfs.length; ++i ) {
                    LayerRef cur = this.layers.get( i );
                    Integer def = mfdefaults.get( cur );
                    try {
                        Integer val = Integer.valueOf( mfs[i] );
                        extensions.getMaxFeatures().put( cur.getName(), def == null ? val : min( def, val ) );
                    } catch ( NumberFormatException e ) {
                        LOG.info( "The value '{}' for MAX_FEATURES can not be parsed as a number.", mfs[i] );
                        extensions.getMaxFeatures().put( cur.getName(), def == null ? 10000 : def );
                    }
                }
            } else {
                for ( int i = 0; i < mfs.length; ++i ) {
                    LayerRef cur = this.layers.get( i );
                    Integer def = mfdefaults.get( cur );
                    if ( mfs.length <= i ) {
                        try {
                            Integer val = Integer.valueOf( mfs[i] );
                            extensions.getMaxFeatures().put( cur.getName(), def == null ? val : min( def, val ) );
                        } catch ( NumberFormatException e ) {
                            LOG.info( "The value '{}' for MAX_FEATURES can not be parsed as a number.", mfs[i] );
                            extensions.getMaxFeatures().put( cur.getName(), def == null ? 10000 : def );
                        }
                    } else {
                        extensions.getMaxFeatures().put( cur.getName(), def == null ? 10000 : def );
                    }
                }
            }
        }
    }

    private <T extends Enum<T>> void handleEnumVSP( Class<T> enumType, Map<String, T> map, T defaultVal, String vals,
                                                    Map<String, T> defaults ) {
        if ( vals == null ) {
            for ( LayerRef l : layers ) {
                T val = defaults.get( l );
                map.put( l.getName(), val == null ? defaultVal : val );
            }
        } else {
            String[] ss = vals.split( "," );
            if ( ss.length == layers.size() ) {
                for ( int i = 0; i < ss.length; ++i ) {
                    T val = defaults.get( layers.get( i ) );
                    try {
                        map.put( layers.get( i ).getName(), Enum.valueOf( enumType, ss[i].toUpperCase() ) );
                    } catch ( IllegalArgumentException e ) {
                        map.put( layers.get( i ).getName(), val == null ? defaultVal : val );
                        LOG.warn( "'{}' is not a valid value for '{}'. Using default value '{}' instead.",
                                  new Object[] { ss[i], enumType.getSimpleName(), val == null ? defaultVal : val } );
                    }
                }
            } else {
                for ( int i = 0; i < layers.size(); ++i ) {
                    T val = defaults.get( layers.get( i ) );
                    if ( ss.length <= i ) {
                        map.put( layers.get( i ).getName(), val == null ? defaultVal : val );
                    } else {
                        try {
                            map.put( layers.get( i ).getName(), Enum.valueOf( enumType, ss[i].toUpperCase() ) );
                        } catch ( IllegalArgumentException e ) {
                            map.put( layers.get( i ).getName(), val == null ? defaultVal : val );
                            LOG.warn( "'{}' is not a valid value for '{}'. Using default value '{}' instead.",
                                      new Object[] { ss[i], enumType.getSimpleName(), val == null ? defaultVal : val } );
                        }
                    }
                }
            }
        }
    }

    private void handleSLD( String sld, String sldBody, LinkedList<LayerRef> layers )
                            throws OWSException {
        XMLInputFactory xmlfac = XMLInputFactory.newInstance();
        Pair<LinkedList<LayerRef>, LinkedList<StyleRef>> pair = null;
        if ( sld != null ) {
            try {
                pair = parse( xmlfac.createXMLStreamReader( sld, new URL( sld ).openStream() ), this );
            } catch ( ParseException e ) {
                LOG.trace( "Stack trace:", e );
                throw new OWSException( "The embedded dimension value in the SLD parameter value was invalid: "
                                        + e.getMessage(), "InvalidDimensionValue", "sld" );
            } catch ( Throwable e ) {
                LOG.trace( "Stack trace:", e );
                throw new OWSException( "Error when parsing the SLD parameter: " + e.getMessage(),
                                        "InvalidParameterValue", "sld" );
            }
        }
        if ( sldBody != null ) {
            try {
                pair = parse( xmlfac.createXMLStreamReader( new StringReader( sldBody ) ), this );
            } catch ( ParseException e ) {
                LOG.trace( "Stack trace:", e );
                throw new OWSException( "The embedded dimension value in the SLD_BODY parameter value was invalid: "
                                        + e.getMessage(), "InvalidDimensionValue", "sld_body" );
            } catch ( Throwable e ) {
                LOG.trace( "Stack trace:", e );
                throw new OWSException( "Error when parsing the SLD_BODY parameter: " + e.getMessage(),
                                        "InvalidParameterValue", "sld_body" );
            }
        }

        // if layers are referenced, clear the other layers out, else leave all in
        if ( pair != null && !layers.isEmpty() ) {
            // it might be in SLD that a layer has multiple styles, so we need to map to a list here
            HashMap<String, LinkedList<Pair<LayerRef, StyleRef>>> lays = new HashMap<String, LinkedList<Pair<LayerRef, StyleRef>>>();

            ListIterator<LayerRef> it = pair.first.listIterator();
            ListIterator<StyleRef> st = pair.second.listIterator();
            while ( it.hasNext() ) {
                LayerRef l = it.next();
                StyleRef s = st.next();
                String name = l.getName();
                if ( !layers.contains( name ) ) {
                    it.remove();
                    st.remove();
                } else {
                    LinkedList<Pair<LayerRef, StyleRef>> list = lays.get( name );
                    if ( list == null ) {
                        list = new LinkedList<Pair<LayerRef, StyleRef>>();
                        lays.put( name, list );
                    }

                    list.add( new Pair<LayerRef, StyleRef>( l, s ) );
                }
            }

            // to get the order right, in case it's different from the SLD order
            for ( LayerRef name : layers ) {
                LinkedList<Pair<LayerRef, StyleRef>> l = lays.get( name );
                if ( l == null ) {
                    throw new OWSException( "The SLD NamedLayer " + name + " is invalid.", "InvalidParameterValue",
                                            "layers" );
                }
                Pair<ArrayList<LayerRef>, ArrayList<StyleRef>> p = unzipPair( l );
                this.layers.addAll( p.first );
                styles.addAll( p.second );
            }
        } else {
            if ( pair != null ) {
                this.layers = pair.first;
                styles = pair.second;
            }
        }
    }

    static HashMap<String, List<?>> parseDimensionValues( Map<String, String> map )
                            throws OWSException {
        HashMap<String, List<?>> dims = new HashMap<String, List<?>>();
        try {
            for ( Entry<String, String> entry : map.entrySet() ) {
                String key = entry.getKey();
                String val = entry.getValue();
                if ( key.equals( "TIME" ) ) {
                    dims.put( "time", (List<?>) parseTyped( parseDimensionValues( val, "time" ), true ) );
                }
                if ( key.equals( "ELEVATION" ) || key.startsWith( "DIM_" ) ) {
                    String name = key.equals( "ELEVATION" ) ? "elevation" : key.substring( 4 ).toLowerCase();
                    dims.put( name, (List<?>) parseTyped( parseDimensionValues( val, name ), false ) );
                }
            }
            return dims;
        } catch ( ParseException e ) {
            LOG.trace( "Stack trace:", e );
            throw new OWSException( "The TIME parameter value was not in ISO8601 format: " + e.getLocalizedMessage(),
                                    OWSException.INVALID_PARAMETER_VALUE );
        }
    }

    /**
     * @param value
     * @param name
     * @return the parsed list of strings or intervals
     * @throws OWSException
     */
    public static LinkedList<?> parseDimensionValues( String value, String name )
                            throws OWSException {
        parser parser = new parser( new DimensionLexer( new StringReader( value ) ) );
        try {
            Symbol sym = parser.parse();
            if ( sym.value instanceof Exception ) {
                final String msg = "The value for the " + name + " dimension parameter was invalid: "
                                   + ( (Exception) sym.value ).getMessage();
                throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE );
            }

            return (LinkedList<?>) sym.value;
        } catch ( Throwable e ) {
            LOG.trace( "Stack trace:", e );
            throw new OWSException(
                                    "The value for the " + name + " dimension parameter was invalid: " + e.getMessage(),
                                    OWSException.INVALID_PARAMETER_VALUE );
        }
    }

    private void parse130( Map<String, String> map, GetMapExtensions exts )
                            throws OWSException {
        String c = map.get( "CRS" );
        if ( c == null || c.trim().isEmpty() ) {
            throw new OWSException( "The CRS parameter is missing.", OWSException.MISSING_PARAMETER_VALUE );
        }

        String box = map.get( "BBOX" );
        if ( box == null || box.trim().isEmpty() ) {
            throw new OWSException( "The BBOX parameter is missing.", OWSException.MISSING_PARAMETER_VALUE );
        }

        double[] vals = splitAsDoubles( box, "," );
        if ( vals.length != 4 ) {
            throw new OWSException( "The value of the BBOX parameter had too many values: " + box,
                                    OWSException.INVALID_PARAMETER_VALUE );
        }

        if ( vals[2] <= vals[0] ) {
            throw new OWSException( "The maxx component of the BBOX was smaller that the minx component.",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }
        if ( vals[3] <= vals[1] ) {
            throw new OWSException( "The maxy component of the BBOX was smaller that the miny component.",
                                    OWSException.INVALID_PARAMETER_VALUE );
        }

        bbox = getCRSAndEnvelope130( c, vals );
        crs = bbox.getCoordinateSystem();

        handleCommon( map, exts );
    }

    /**
     * @return the coordinate system of the bbox
     */
    public ICRS getCoordinateSystem() {
        return crs;
    }

    /**
     * @return the bbox
     */
    public Envelope getBoundingBox() {
        return bbox;
    }

    /**
     * @return a copy of the layers list
     */
    public LinkedList<LayerRef> getLayers() {
        return new LinkedList<LayerRef>( layers );
    }

    /**
     * @return a copy of the styles list
     */
    public LinkedList<StyleRef> getStyles() {
        return new LinkedList<StyleRef>( styles );
    }

    /**
     * @return the image format
     */
    public String getFormat() {
        return format;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the transparent parameter
     */
    public boolean getTransparent() {
        return transparent;
    }

    /**
     * @return the desired background color
     */
    public Color getBgColor() {
        return bgcolor;
    }

    /**
     * @param crs
     */
    public void setCoordinateSystem( ICRS crs ) {
        this.crs = crs;
        bbox = fac.createEnvelope( bbox.getMin().getAsArray(), bbox.getMax().getAsArray(), crs );
    }

    /**
     * @return returns a map with the requested dimension values
     */
    public HashMap<String, List<?>> getDimensions() {
        return dimensions;
    }

    /**
     * @param layer
     * @param filter
     */
    public void addFilter( String layer, Filter filter ) {
        if ( filters.get( layer ) != null ) {
            Operator oldop = ( (OperatorFilter) filters.get( layer ) ).getOperator();
            Operator snd = ( (OperatorFilter) filter ).getOperator();
            filters.put( layer, new OperatorFilter( new And( oldop, snd ) ) );
        } else {
            filters.put( layer, filter );
        }
    }

    /**
     * @param name
     * @param values
     */
    public void addDimensionValue( String name, List<?> values ) {
        dimensions.put( name, values );
    }

    /**
     * @return the scale as WMS 1.3.0/SLD scale
     */
    public double getScale() {
        return scale;
    }

    /**
     * @return the value of the pixel size parameter (default is 0.28 mm).
     */
    public double getPixelSize() {
        return pixelSize;
    }

    /**
     * @return max(horizontal/vertical) resolution
     */
    public double getResolution() {
        return resolution;
    }

    /**
     * @return the get map extensions for the layers
     */
    public GetMapExtensions getExtensions() {
        return extensions;
    }

    /**
     * @return the KVP map of parameters. May not be accurate/empty, especially if this object has been created by some
     *         other means than a KVP request.
     */
    public Map<String, String> getParameterMap() {
        return parameterMap;
    }

    /**
     * @param crs
     * @return the auto crs as defined in WMS 1.1.1 spec Annex E
     */
    public static ICRS getCRS111( String crs ) {
        if ( crs.startsWith( "AUTO:" ) ) {
            String[] cs = crs.split( ":" )[1].split( "," );
            int id = Integer.parseInt( cs[0] );
            // this is not supported
            // int units = Integer.parseInt( cs[1] );
            double lon0 = Double.parseDouble( cs[2] );
            double lat0 = Double.parseDouble( cs[3] );

            return Utils.getAutoCRS( id, lon0, lat0 );
        }
        return CRSManager.getCRSRef( crs, true );
    }

    /**
     * @param crs
     * @param bbox
     * @return a new CRS
     */
    public static Envelope getCRSAndEnvelope130( String crs, double[] bbox ) {
        if ( crs.startsWith( "AUTO2:" ) ) {
            String[] cs = crs.split( ":" )[1].split( "," );
            int id = Integer.parseInt( cs[0] );
            // this is not supported
            double factor = Double.parseDouble( cs[1] );
            double lon0 = Double.parseDouble( cs[2] );
            double lat0 = Double.parseDouble( cs[3] );

            return new GeometryFactory().createEnvelope( factor * bbox[0], factor * bbox[1], factor * bbox[2],
                                                         factor * bbox[3], Utils.getAutoCRS( id, lon0, lat0 ) );
        }
        return new GeometryFactory().createEnvelope( bbox[0], bbox[1], bbox[2], bbox[3], CRSManager.getCRSRef( crs ) );
    }

    /**
     * @param name
     * @param filter
     * @param style
     * @return a new filter for the layer, fulfilling the filter parameter as well
     */
    public Filter getFilterForLayer( String name, Filter filter, Style style ) {
        Filter sldFilter = getStyleFilters( style, getScale() );

        Filter extra = filters.get( name );
        if ( extra == null ) {
            extra = sldFilter;
        } else {
            if ( sldFilter != null ) {
                Operator op1 = ( (OperatorFilter) sldFilter ).getOperator();
                Operator op2 = ( (OperatorFilter) extra ).getOperator();
                extra = new OperatorFilter( new And( op1, op2 ) );
            }
        }
        if ( filter != null ) {
            if ( extra != null ) {
                Operator op = ( (OperatorFilter) extra ).getOperator();
                Operator op2 = ( (OperatorFilter) filter ).getOperator();
                return new OperatorFilter( new And( op, op2 ) );
            }
            return filter;
        }
        return extra;
    }

}
