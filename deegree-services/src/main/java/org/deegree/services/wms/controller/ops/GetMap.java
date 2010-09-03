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

package org.deegree.services.wms.controller.ops;

import static java.awt.Color.decode;
import static java.awt.Color.white;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static org.deegree.commons.utils.ArrayUtils.splitAsDoubles;
import static org.deegree.commons.utils.CollectionUtils.unzipPair;
import static org.deegree.protocol.wms.WMSConstants.VERSION_111;
import static org.deegree.protocol.wms.WMSConstants.VERSION_130;
import static org.deegree.rendering.r2d.se.parser.SymbologyParser.ELSEFILTER;
import static org.deegree.services.controller.ows.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.services.controller.ows.OWSException.LAYER_NOT_DEFINED;
import static org.deegree.services.controller.ows.OWSException.MISSING_PARAMETER_VALUE;
import static org.deegree.services.controller.ows.OWSException.STYLE_NOT_DEFINED;
import static org.deegree.services.i18n.Messages.get;
import static org.deegree.services.wms.controller.ops.GetMap.Antialias.BOTH;
import static org.deegree.services.wms.controller.ops.GetMap.Interpolation.NEARESTNEIGHBOR;
import static org.deegree.services.wms.controller.ops.GetMap.Quality.NORMAL;
import static org.deegree.services.wms.controller.sld.SLDParser.parse;
import static org.deegree.services.wms.model.Dimension.parseTyped;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Color;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
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
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.filter.Filter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.Or;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.protocol.wms.Utils;
import org.deegree.protocol.wms.dims.DimensionLexer;
import org.deegree.protocol.wms.dims.parser;
import org.deegree.rendering.r2d.se.parser.SymbologyParser.FilterContinuation;
import org.deegree.rendering.r2d.se.unevaluated.Continuation;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.rendering.r2d.se.unevaluated.Symbolizer;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.StyleRegistry;
import org.deegree.services.wms.controller.WMSController111;
import org.deegree.services.wms.controller.WMSController130;
import org.deegree.services.wms.model.layers.Layer;
import org.slf4j.Logger;

/**
 * <code>GetMap</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(trace = "logs stack traces", debug = "logs request scale", warn = "logs if scale could not be calculated, also logs values of invalid VSPs, like ANTIALIAS, PIXELSIZE etc.", error = "logs errors when handling dimension values")
public class GetMap {

    private static final Logger LOG = getLogger( GetMap.class );

    private static GeometryFactory fac = new GeometryFactory();

    private CRS crs;

    private Envelope bbox;

    private LinkedList<Layer> layers = new LinkedList<Layer>();

    private LinkedList<Style> styles = new LinkedList<Style>();

    private Map<Layer, Interpolation> interpolation = new HashMap<Layer, Interpolation>();

    private Map<Layer, Antialias> antialias = new HashMap<Layer, Antialias>();

    private Map<Layer, Quality> quality = new HashMap<Layer, Quality>();

    private Map<Layer, Integer> maxFeatures = new HashMap<Layer, Integer>();

    private HashMap<String, Filter> filters = new HashMap<String, Filter>();

    private String format;

    private int width, height;

    private boolean transparent;

    private Color bgcolor = white;

    private HashMap<String, List<?>> dimensions = new HashMap<String, List<?>>();

    private double scale;

    private double pixelSize = 0.28;

    private double resolution;

    /**
     * @param map
     * @param version
     * @param service
     * @throws OWSException
     */
    public GetMap( Map<String, String> map, Version version, MapService service ) throws OWSException {
        if ( version.equals( VERSION_111 ) ) {
            parse111( map, service );
        }
        if ( version.equals( VERSION_130 ) ) {
            parse130( map, service );
        }
        try {
            scale = Utils.calcScaleWMS130( width, height, bbox, crs.getWrappedCRS() );
            LOG.debug( "GetMap request has a WMS 1.3.0/SLD scale of '{}'.", scale );
            resolution = max( bbox.getSpan0() / width, bbox.getSpan1() / height );
            LOG.debug( "Resolution per pixel is {}.", resolution );
        } catch ( UnknownCRSException e ) {
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
    public GetMap( MapService service, Collection<Layer> layers, Collection<Style> styles, int width, int height,
                   Envelope boundingBox ) {
        this.layers.addAll( layers );
        this.styles.addAll( styles );
        this.width = width;
        this.height = height;
        this.bbox = boundingBox;
        this.crs = boundingBox.getCoordinateSystem();
        format = "image/png";
        transparent = true;
        handleVSPs( service, new HashMap<String, String>() );
    }

    private void parse111( Map<String, String> map, MapService service )
                            throws OWSException {
        String c = map.get( "SRS" );
        if ( c == null || c.trim().isEmpty() ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "SRS" ), MISSING_PARAMETER_VALUE );
        }
        crs = WMSController111.getCRS( c );

        String box = map.get( "BBOX" );
        if ( box == null || box.trim().isEmpty() ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "BBOX" ), MISSING_PARAMETER_VALUE );
        }

        double[] vals = splitAsDoubles( box, "," );
        // hack to work around ESRI ArcGIS Explorer localized bboxes...
        if ( vals.length == 8 ) {
            String[] ss = box.split( "," );
            vals = new double[] { parseDouble( ss[0] + "." + ss[1] ), parseDouble( ss[2] + "." + ss[3] ),
                                 parseDouble( ss[4] + "." + ss[5] ), parseDouble( ss[6] + "." + ss[7] ) };
        }
        if ( vals.length != 4 ) {
            throw new OWSException( get( "WMS.BBOX_WRONG_FORMAT", box ), INVALID_PARAMETER_VALUE );
        }

        if ( vals[2] <= vals[0] ) {
            throw new OWSException( get( "WMS.MAXX_MINX" ), INVALID_PARAMETER_VALUE );
        }
        if ( vals[3] <= vals[1] ) {
            throw new OWSException( get( "WMS.MAXY_MINY" ), INVALID_PARAMETER_VALUE );
        }

        bbox = fac.createEnvelope( new double[] { vals[0], vals[1] }, new double[] { vals[2], vals[3] }, crs );

        handleCommon( map, service );
    }

    static LinkedList<Layer> handleKVPLayers( List<String> lays, MapService service )
                            throws OWSException {
        LinkedList<Layer> layers = new LinkedList<Layer>();
        for ( String lay : lays ) {
            Layer l = service.getLayer( lay );
            if ( l == null ) {
                throw new OWSException( get( "WMS.LAYER_NOT_KNOWN", lay ), LAYER_NOT_DEFINED );
            }
            layers.add( l );
        }
        return layers;
    }

    static LinkedList<Style> handleKVPStyles( String ss, MapService service, LinkedList<Layer> layers )
                            throws OWSException {
        LinkedList<Style> styles = new LinkedList<Style>();

        // result is a list with 'default' where default styles were requested
        StyleRegistry registry = service.getStyles();
        if ( ss.trim().isEmpty() ) {
            for ( Layer layer : layers ) {
                styles.add( registry.getDefault( layer.getName() ) );
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
            if ( styls.length != layers.size() ) {
                throw new OWSException( get( "WMS.INVALID_NUMBER_OF_STYLES", layers.size(), styls.length ),
                                        INVALID_PARAMETER_VALUE );
            }

            int i = -1;
            for ( Layer l : layers ) {
                if ( styls[++i].isEmpty() || styls[i].equals( "default" ) ) {
                    styles.add( registry.getDefault( l.getName() ) );
                } else {
                    if ( !registry.hasStyle( l.getName(), styls[i] ) ) {
                        throw new OWSException( get( "WMS.UNDEFINED_STYLE", styls[i], l.getName() ), STYLE_NOT_DEFINED );
                    }
                    styles.add( registry.get( l.getName(), styls[i] ) );
                }
            }
        }

        return styles;
    }

    private void handleCommon( Map<String, String> map, MapService service )
                            throws OWSException {
        String ls = map.get( "LAYERS" );
        String sld = map.get( "SLD" );
        String sldBody = map.get( "SLD_BODY" );

        if ( ( ls == null || ls.trim().isEmpty() ) && sld == null && sldBody == null ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "LAYERS" ), MISSING_PARAMETER_VALUE );
        }
        LinkedList<String> layers = ls == null ? new LinkedList<String>()
                                              : new LinkedList<String>( asList( ls.split( "," ) ) );

        if ( layers.size() == 1 && layers.get( 0 ).isEmpty() ) {
            layers.clear();
        }

        String ss = map.get( "STYLES" );
        if ( ss == null ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "STYLES" ), MISSING_PARAMETER_VALUE );
        }

        if ( sld == null && sldBody == null ) {
            this.layers = handleKVPLayers( layers, service );
            this.styles = handleKVPStyles( ss, service, this.layers );
        } else {
            // TODO think about whether STYLES has to be handled here as well
            handleSLD( sld, sldBody, layers, service );
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
            throw new OWSException( get( "WMS.PARAM_MISSING", "FORMAT" ), MISSING_PARAMETER_VALUE );
        }

        String w = map.get( "WIDTH" );
        if ( w == null ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "WIDTH" ), MISSING_PARAMETER_VALUE );
        }
        try {
            width = parseInt( w );
        } catch ( NumberFormatException e ) {
            throw new OWSException( get( "WMS.NOT_A_NUMBER", "WIDTH", w ), INVALID_PARAMETER_VALUE );
        }

        String h = map.get( "HEIGHT" );
        if ( h == null ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "HEIGHT" ), MISSING_PARAMETER_VALUE );
        }
        try {
            height = parseInt( h );
        } catch ( NumberFormatException e ) {
            throw new OWSException( get( "WMS.NOT_A_NUMBER", "HEIGHT", h ), INVALID_PARAMETER_VALUE );
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

        handleVSPs( service, map );
    }

    private void handleVSPs( MapService service, Map<String, String> map ) {
        handleEnumVSP( Quality.class, quality, NORMAL, map.get( "QUALITY" ), service.getDefaultQualities() );
        handleEnumVSP( Interpolation.class, interpolation, NEARESTNEIGHBOR, map.get( "INTERPOLATION" ),
                       service.getDefaultInterpolations() );
        handleEnumVSP( Antialias.class, antialias, BOTH, map.get( "ANTIALIAS" ), service.getDefaultAntialiases() );
        String maxFeatures = map.get( "MAX_FEATURES" );
        if ( maxFeatures == null ) {
            for ( Layer l : this.layers ) {
                Integer max = service.getDefaultMaxFeatures().get( l );
                if ( max == null ) {
                    max = service.getGlobalMaxFeatures();
                    LOG.debug( "Using global max features setting of {}.", max );
                }
                this.maxFeatures.put( l, max );
            }
        } else {
            String[] mfs = maxFeatures.split( "," );
            Map<Layer, Integer> defaults = service.getDefaultMaxFeatures();
            if ( mfs.length == this.layers.size() ) {
                for ( int i = 0; i < mfs.length; ++i ) {
                    Layer cur = this.layers.get( i );
                    Integer def = defaults.get( cur );
                    try {
                        Integer val = Integer.valueOf( mfs[i] );
                        this.maxFeatures.put( cur, def == null ? val : min( def, val ) );
                    } catch ( NumberFormatException e ) {
                        LOG.info( "The value '{}' for MAX_FEATURES can not be parsed as a number.", mfs[i] );
                        this.maxFeatures.put( cur, def == null ? 10000 : def );
                    }
                }
            } else {
                for ( int i = 0; i < mfs.length; ++i ) {
                    Layer cur = this.layers.get( i );
                    Integer def = defaults.get( cur );
                    if ( mfs.length <= i ) {
                        try {
                            Integer val = Integer.valueOf( mfs[i] );
                            this.maxFeatures.put( cur, def == null ? val : min( def, val ) );
                        } catch ( NumberFormatException e ) {
                            LOG.info( "The value '{}' for MAX_FEATURES can not be parsed as a number.", mfs[i] );
                            this.maxFeatures.put( cur, def == null ? 10000 : def );
                        }
                    } else {
                        this.maxFeatures.put( cur, def == null ? 10000 : def );
                    }
                }
            }
        }
    }

    private <T extends Enum<T>> void handleEnumVSP( Class<T> enumType, Map<Layer, T> map, T defaultVal, String vals,
                                                    Map<Layer, T> defaults ) {
        if ( vals == null ) {
            for ( Layer l : layers ) {
                T val = defaults.get( l );
                map.put( l, val == null ? defaultVal : val );
            }
        } else {
            String[] ss = vals.split( "," );
            if ( ss.length == layers.size() ) {
                for ( int i = 0; i < ss.length; ++i ) {
                    T val = defaults.get( layers.get( i ) );
                    try {
                        map.put( layers.get( i ), Enum.valueOf( enumType, ss[i].toUpperCase() ) );
                    } catch ( IllegalArgumentException e ) {
                        map.put( layers.get( i ), val == null ? defaultVal : val );
                        LOG.warn( "'{}' is not a valid value for '{}'. Using default value '{}' instead.",
                                  new Object[] { ss[i], enumType.getSimpleName(), val == null ? defaultVal : val } );
                    }
                }
            } else {
                for ( int i = 0; i < layers.size(); ++i ) {
                    T val = defaults.get( layers.get( i ) );
                    if ( ss.length <= i ) {
                        map.put( layers.get( i ), val == null ? defaultVal : val );
                    } else {
                        try {
                            map.put( layers.get( i ), Enum.valueOf( enumType, ss[i].toUpperCase() ) );
                        } catch ( IllegalArgumentException e ) {
                            map.put( layers.get( i ), val == null ? defaultVal : val );
                            LOG.warn( "'{}' is not a valid value for '{}'. Using default value '{}' instead.",
                                      new Object[] { ss[i], enumType.getSimpleName(), val == null ? defaultVal : val } );
                        }
                    }
                }
            }
        }
    }

    private void handleSLD( String sld, String sldBody, LinkedList<String> layers, MapService service )
                            throws OWSException {
        XMLInputFactory xmlfac = XMLInputFactory.newInstance();
        Pair<LinkedList<Layer>, LinkedList<Style>> pair = null;
        if ( sld != null ) {
            try {
                pair = parse( xmlfac.createXMLStreamReader( sld, new URL( sld ).openStream() ), service, this );
            } catch ( MalformedURLException e ) {
                LOG.trace( "Stack trace:", e );
                throw new OWSException( get( "WMS.SLD_PARSE_ERROR", "SLD", e.getMessage() ), "InvalidParameterValue",
                                        "sld" );
            } catch ( XMLStreamException e ) {
                LOG.trace( "Stack trace:", e );
                throw new OWSException( get( "WMS.SLD_PARSE_ERROR", "SLD", e.getMessage() ), "InvalidParameterValue",
                                        "sld" );
            } catch ( ParseException e ) {
                LOG.trace( "Stack trace:", e );
                throw new OWSException( get( "WMS.DIMENSION_PARAMETER_INVALID", "embeddded in SLD", e.getMessage() ),
                                        "InvalidDimensionValue", "sld" );
            } catch ( IOException e ) {
                LOG.trace( "Stack trace:", e );
                throw new OWSException( get( "WMS.SLD_PARSE_ERROR", "SLD", e.getMessage() ), "InvalidParameterValue",
                                        "sld" );
            }
        }
        if ( sldBody != null ) {
            try {
                pair = parse( xmlfac.createXMLStreamReader( new StringReader( sldBody ) ), service, this );
            } catch ( XMLStreamException e ) {
                LOG.trace( "Stack trace:", e );
                throw new OWSException( get( "WMS.SLD_PARSE_ERROR", "SLD_BODY", e.getMessage() ),
                                        "InvalidParameterValue", "sld_body" );
            } catch ( ParseException e ) {
                LOG.trace( "Stack trace:", e );
                throw new OWSException( get( "WMS.DIMENSION_PARAMETER_INVALID", "embeddded in SLD", e.getMessage() ),
                                        "InvalidDimensionValue", "sld_body" );
            }
        }

        // if layers are referenced, clear the other layers out, else leave all in
        if ( pair != null && !layers.isEmpty() ) {
            // it might be in SLD that a layer has multiple styles, so we need to map to a list here
            HashMap<String, LinkedList<Pair<Layer, Style>>> lays = new HashMap<String, LinkedList<Pair<Layer, Style>>>();

            ListIterator<Layer> it = pair.first.listIterator();
            ListIterator<Style> st = pair.second.listIterator();
            while ( it.hasNext() ) {
                Layer l = it.next();
                Style s = st.next();
                String name = l.getName();
                if ( !layers.contains( name ) ) {
                    it.remove();
                    st.remove();
                } else {
                    LinkedList<Pair<Layer, Style>> list = lays.get( name );
                    if ( list == null ) {
                        list = new LinkedList<Pair<Layer, Style>>();
                        lays.put( name, list );
                    }
                    list.add( new Pair<Layer, Style>( l, s ) );
                }
            }

            // to get the order right, in case it's different from the SLD order
            for ( String name : layers ) {
                LinkedList<Pair<Layer, Style>> l = lays.get( name );
                if ( l == null ) {
                    throw new OWSException( get( "WMS.SLD_LAYER_INVALID", name ), "InvalidParameterValue", "layers" );
                }
                Pair<ArrayList<Layer>, ArrayList<Style>> p = unzipPair( l );
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
            throw new OWSException( get( "WMS.TIME_PARAMETER_NOT_ISO_FORMAT", e.getLocalizedMessage() ),
                                    INVALID_PARAMETER_VALUE );
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
                final String msg = get( "WMS.DIMENSION_PARAMETER_INVALID", name, ( (Exception) sym.value ).getMessage() );
                throw new OWSException( msg, INVALID_PARAMETER_VALUE );
            }

            return (LinkedList<?>) sym.value;
        } catch ( Exception e ) {
            LOG.error( "Unknown error", e );
            throw new OWSException( get( "WMS.DIMENSION_PARAMETER_INVALID", name, e.getLocalizedMessage() ),
                                    INVALID_PARAMETER_VALUE );
        }
    }

    private void parse130( Map<String, String> map, MapService service )
                            throws OWSException {
        String c = map.get( "CRS" );
        if ( c == null || c.trim().isEmpty() ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "CRS" ), MISSING_PARAMETER_VALUE );
        }

        String box = map.get( "BBOX" );
        if ( box == null || box.trim().isEmpty() ) {
            throw new OWSException( get( "WMS.PARAM_MISSING", "BBOX" ), MISSING_PARAMETER_VALUE );
        }

        double[] vals = splitAsDoubles( box, "," );
        if ( vals.length != 4 ) {
            throw new OWSException( get( "WMS.BBOX_WRONG_FORMAT", box ), INVALID_PARAMETER_VALUE );
        }

        if ( vals[2] <= vals[0] ) {
            throw new OWSException( get( "WMS.MAXX_MINX" ), INVALID_PARAMETER_VALUE );
        }
        if ( vals[3] <= vals[1] ) {
            throw new OWSException( get( "WMS.MAXY_MINY" ), INVALID_PARAMETER_VALUE );
        }

        bbox = WMSController130.getCRSAndEnvelope( c, vals );
        crs = bbox.getCoordinateSystem();

        handleCommon( map, service );
    }

    /**
     * @return the coordinate system of the bbox
     */
    public CRS getCoordinateSystem() {
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
    public LinkedList<Layer> getLayers() {
        return new LinkedList<Layer>( layers );
    }

    /**
     * @return a copy of the styles list
     */
    public LinkedList<Style> getStyles() {
        return new LinkedList<Style>( styles );
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
    public void setCoordinateSystem( CRS crs ) {
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
     * @param name
     * @param filter
     * @param style
     * @return a new filter for the layer, fulfilling the filter parameter as well
     */
    public Filter getFilterForLayer( String name, Filter filter, Style style ) {
        Filter sldFilter = null;
        outer: if ( style != null ) {
            LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>> rules = style.filter( getScale() ).getRules();
            for ( Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair> p : rules ) {
                if ( p.first == null ) {
                    sldFilter = null;
                    break outer;
                }
                if ( p.first instanceof FilterContinuation ) {
                    FilterContinuation contn = (FilterContinuation) p.first;
                    if ( contn.filter == ELSEFILTER ) {
                        sldFilter = null;
                        break outer;
                    }
                    if ( contn.filter == null ) {
                        sldFilter = null;
                        break outer;
                    }
                    if ( sldFilter == null ) {
                        sldFilter = contn.filter;
                    } else {
                        Operator op1 = ( (OperatorFilter) sldFilter ).getOperator();
                        Operator op2 = ( (OperatorFilter) contn.filter ).getOperator();
                        sldFilter = new OperatorFilter( new Or( op1, op2 ) );
                    }
                }
            }
        }

        Filter extra = filters.get( name );
        if ( extra == null ) {
            extra = sldFilter;
        } else {
            if ( sldFilter != null ) {
                Operator op1 = ( (OperatorFilter) sldFilter ).getOperator();
                Operator op2 = ( (OperatorFilter) extra ).getOperator();
                extra = new OperatorFilter( new Or( op1, op2 ) );
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

    /**
     * @return the scale as WMS 1.3.0/SLD scale
     */
    public double getScale() {
        return scale;
    }

    /**
     * @return the quality settings for the layers
     */
    public Map<Layer, Quality> getQuality() {
        return quality;
    }

    /**
     * @return the interpolation settings for the layers
     */
    public Map<Layer, Interpolation> getInterpolation() {
        return interpolation;
    }

    /**
     * @return the antialias settings for the layers
     */
    public Map<Layer, Antialias> getAntialias() {
        return antialias;
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
     * @return the max features settings for the layers
     */
    public Map<Layer, Integer> getMaxFeatures() {
        return maxFeatures;
    }

    /**
     * <code>Quality</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public static enum Quality {
        /***/
        LOW, /***/
        NORMAL, /***/
        HIGH
    }

    /**
     * <code>Interpolation</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public static enum Interpolation {
        /***/
        NEARESTNEIGHBOR, /***/
        NEARESTNEIGHBOUR, /***/
        BILINEAR, /***/
        BICUBIC
    }

    /**
     * <code>Antialias</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public static enum Antialias {
        /***/
        IMAGE, /***/
        TEXT, /***/
        BOTH, /***/
        NONE
    }

}
