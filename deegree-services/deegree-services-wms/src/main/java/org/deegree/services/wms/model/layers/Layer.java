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

package org.deegree.services.wms.model.layers;

import static org.deegree.cs.coordinatesystems.GeographicCRS.WGS84;
import static org.deegree.layer.dims.Dimension.parseTyped;
import static org.deegree.services.wms.MapService.prepareImage;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.cs.CRSUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.XPathEvaluator;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.layer.dims.Dimension;
import org.deegree.layer.dims.DimensionsLexer;
import org.deegree.layer.dims.DimensionsParser;
import org.deegree.protocol.wms.WMSException.InvalidDimensionValue;
import org.deegree.protocol.wms.WMSException.MissingDimensionValue;
import org.deegree.rendering.r2d.Renderer;
import org.deegree.rendering.r2d.TextRenderer;
import org.deegree.services.jaxb.wms.AbstractLayerType;
import org.deegree.services.jaxb.wms.BoundingBoxType;
import org.deegree.services.jaxb.wms.DimensionType;
import org.deegree.services.jaxb.wms.KeywordsType;
import org.deegree.services.jaxb.wms.LanguageStringType;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.controller.ops.GetFeatureInfo;
import org.deegree.services.wms.controller.ops.GetMap;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.style.styling.Styling;
import org.deegree.style.styling.TextStyling;
import org.slf4j.Logger;

/**
 * <code>Layer</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(warn = "logs information about dimension handling")
public abstract class Layer {

    private static final Logger LOG = getLogger( Layer.class );

    private String name;

    private String title;

    private String abstract_;

    private String dataMetadataSetId;

    private String authorityURL, authorityIdentifier;

    private LinkedList<Pair<CodeType, LanguageStringType>> keywords;

    private Envelope bbox;

    private LinkedList<ICRS> srs;

    private DoublePair scaleHint;

    private LinkedList<Layer> children;

    private Layer parent;

    Dimension<Date> time;

    HashMap<String, Dimension<Object>> dimensions = new HashMap<String, Dimension<Object>>();

    private String internalName;

    private boolean queryable = true;

    protected MapService service;

    protected Layer( MapService service, String name, String title, Layer parent ) {
        this.service = service;
        this.name = name;
        this.title = title;
        this.parent = parent;
        keywords = new LinkedList<Pair<CodeType, LanguageStringType>>();
        srs = new LinkedList<ICRS>();
        children = new LinkedList<Layer>();
    }

    protected Layer( MapService service, AbstractLayerType layer, Layer parent ) {
        this.service = service;
        name = layer.getName();
        title = layer.getTitle();
        abstract_ = layer.getAbstract();
        dataMetadataSetId = layer.getMetadataSetId();
        List<KeywordsType> kwTypeList = layer.getKeywords();
        keywords = new LinkedList<Pair<CodeType, LanguageStringType>>();
        if ( kwTypeList != null ) {
            for ( KeywordsType kwType : kwTypeList ) {
                org.deegree.services.jaxb.wms.CodeType jaxbct = kwType.getType();
                CodeType ct = null;
                if ( jaxbct != null ) {
                    ct = new CodeType( jaxbct.getValue(), jaxbct.getCodeSpace() );
                }
                for ( LanguageStringType lst : kwType.getKeyword() ) {
                    keywords.add( new Pair<CodeType, LanguageStringType>( ct, lst ) );
                }
            }
        }
        bbox = parseBoundingBox( layer.getBoundingBox() );
        srs = parseCoordinateSystems( layer.getCRS() );
        if ( srs == null ) {
            srs = new LinkedList<ICRS>();
        }
        this.parent = parent;
        children = new LinkedList<Layer>();

        if ( layer.isQueryable() != null ) {
            queryable = layer.isQueryable();
        }

        for ( DimensionType type : layer.getDimension() ) {
            DimensionsLexer lexer = new DimensionsLexer( new ANTLRStringStream( type.getExtent() ) );
            DimensionsParser parser = new DimensionsParser( new CommonTokenStream( lexer ) );
            try {
                parser.dimensionvalues();
            } catch ( RecognitionException e ) {
                // ignore exception, error message in the parser
            }
            DimensionsParser defaultParser = null;
            if ( type.getDefaultValue() != null ) {
                lexer = new DimensionsLexer( new ANTLRStringStream( type.getDefaultValue() ) );
                defaultParser = new DimensionsParser( new CommonTokenStream( lexer ) );
                try {
                    defaultParser.dimensionvalues();
                } catch ( RecognitionException e ) {
                    // ignore exception, error message in the parser
                }
            }

            List<?> list;
            List<?> defaultList;

            try {
                if ( parser.error != null ) {
                    throw new Exception( parser.error );
                }

                list = parser.values;

                if ( defaultParser != null ) {
                    if ( defaultParser.error != null ) {
                        throw new Exception( defaultParser.error );
                    }
                    defaultList = defaultParser.values;
                } else {
                    defaultList = parser.values;
                }
            } catch ( Exception e ) {
                LOG.warn( "The dimension '{}' has not been added for layer '{}' because the error"
                                                  + " '{}' occurred while parsing the extent/default values.",
                          new Object[] { type.getName(), name, e.getLocalizedMessage() } );
                continue;
            }

            if ( type.isIsTime() ) {
                try {
                    boolean current = ( type.isCurrent() != null ) && type.isCurrent();
                    boolean nearest = ( type.isNearestValue() != null ) && type.isNearestValue();
                    boolean multiple = ( type.isMultipleValues() != null ) && type.isMultipleValues();
                    time = new Dimension<Date>( "time", (List<?>) parseTyped( defaultList, true ), current, nearest,
                                                multiple, "ISO8601", null, type.getProperty(),
                                                (List<?>) parseTyped( list, true ) );
                } catch ( ParseException e ) {
                    LOG.warn( "The TIME dimension has not been added for layer {} because the error"
                                                      + " '{}' occurred while parsing the extent/default values.",
                              name,
                              e.getLocalizedMessage() );
                }
            } else if ( type.isIsElevation() ) {
                try {
                    boolean nearest = ( type.isNearestValue() != null ) && type.isNearestValue();
                    boolean multiple = ( type.isMultipleValues() != null ) && type.isMultipleValues();
                    dimensions.put( "elevation",
                                    new Dimension<Object>( "elevation", (List<?>) parseTyped( defaultList, false ),
                                                           false, nearest, multiple, type.getUnits(),
                                                           type.getUnitSymbol() == null ? "m" : type.getUnitSymbol(),
                                                           type.getProperty(), (List<?>) parseTyped( list, false ) ) );
                } catch ( ParseException e ) {
                    // does not happen, as we're not parsing with time == true
                }
            } else {
                try {
                    boolean nearest = ( type.isNearestValue() != null ) && type.isNearestValue();
                    boolean multiple = ( type.isMultipleValues() != null ) && type.isMultipleValues();
                    Dimension<Object> dim = new Dimension<Object>(
                                                                   type.getName(),
                                                                   (List<?>) parseTyped( type.getDefaultValue(), false ),
                                                                   false, nearest, multiple, type.getUnits(),
                                                                   type.getUnitSymbol(), type.getProperty(),
                                                                   (List<?>) parseTyped( list, false ) );
                    dimensions.put( type.getName(), dim );
                } catch ( ParseException e ) {
                    // does not happen, as we're not parsing with time == true
                }
            }
        }
    }

    /**
     * @param name
     */
    public void setInternalName( String name ) {
        internalName = name;
    }

    public String getDataMetadataSetId() {
        return dataMetadataSetId;
    }

    public String getAuthorityURL() {
        return authorityURL;
    }

    public String getAuthorityIdentifier() {
        return authorityIdentifier;
    }

    public void setAuthorityURL( String authorityURL ) {
        this.authorityURL = authorityURL;
    }

    public void setAuthorityIdentifier( String authorityIdentifier ) {
        this.authorityIdentifier = authorityIdentifier;
    }

    private static Envelope parseBoundingBox( BoundingBoxType box ) {
        Envelope bbox = null;

        if ( box != null ) {
            Double[] points = box.getLowerCorner().toArray( new Double[] {} );
            double[] min = new double[points.length];
            for ( int i = 0; i < min.length; ++i ) {
                min[i] = points[i];
            }
            points = box.getUpperCorner().toArray( new Double[] {} );
            double[] max = new double[points.length];
            for ( int i = 0; i < max.length; ++i ) {
                max[i] = points[i];
            }
            ICRS crs;
            if ( box.getCrs() != null ) {
                crs = CRSManager.getCRSRef( box.getCrs() );
            } else {
                crs = CRSManager.getCRSRef( WGS84 );
            }
            bbox = new GeometryFactory().createEnvelope( min, max, crs );
        }

        return bbox;
    }

    private static LinkedList<ICRS> parseCoordinateSystems( String crs ) {
        LinkedList<ICRS> list = new LinkedList<ICRS>();
        if ( crs == null ) {
            return list;
        }

        for ( String c : crs.split( "\\s" ) ) {
            if ( !c.isEmpty() ) {
                list.add( CRSManager.getCRSRef( c ) );
            }
        }

        return list;
    }

    /**
     * @param f
     * @param evaluator
     * @param style
     * @param renderer
     * @param textRenderer
     * @param scale
     * @param resolution
     */
    public static void render( final Feature f, final XPathEvaluator<Feature> evaluator, final Style style,
                               final Renderer renderer, final TextRenderer textRenderer, final double scale,
                               final double resolution ) {
        Style s = style;
        if ( s == null ) {
            s = new Style();
        }
        s = s.filter( scale );

        LinkedList<Triple<Styling, LinkedList<Geometry>, String>> evalds = s.evaluate( f, evaluator );
        for ( Triple<Styling, LinkedList<Geometry>, String> evald : evalds ) {
            // boolean invisible = true;

            // inner: for ( Geometry g : evald.second ) {
            // if ( g instanceof Point || g instanceof MultiPoint ) {
            // invisible = false;
            // break inner;
            // }
            // if ( !( g.getEnvelope().getSpan0() < resolution && g.getEnvelope().getSpan1() < resolution ) ) {
            // invisible = false;
            // break inner;
            // }
            // }

            // if ( !invisible ) {
            if ( evald.first instanceof TextStyling ) {
                textRenderer.render( (TextStyling) evald.first, evald.third, evald.second );
            } else {
                renderer.render( evald.first, evald.second );
            }
            // } else {
            // LOG.debug( "Skipping invisible feature." );
            // }
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the internal name (used for style lookup etc.)
     */
    public String getInternalName() {
        return internalName == null ? name : internalName;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     * @return the abstract_
     */
    public String getAbstract() {
        return abstract_;
    }

    /**
     * @param abstract_
     *            the abstract_ to set
     */
    public void setAbstract( String abstract_ ) {
        this.abstract_ = abstract_;
    }

    /**
     * @return the live keywords list
     */
    public LinkedList<Pair<CodeType, LanguageStringType>> getKeywords() {
        return keywords;
    }

    /**
     * @param keywords
     *            the keywords to set (will be copied)
     */
    public void setKeywords( Collection<Pair<CodeType, LanguageStringType>> keywords ) {
        this.keywords = new LinkedList<Pair<CodeType, LanguageStringType>>( keywords );
    }

    /**
     * @return the bbox
     */
    public Envelope getBbox() {
        try {
            Envelope bbox = this.bbox;
            if ( bbox != null && bbox.getCoordinateDimension() <= 1 ) {
                bbox = null;
            }
            if ( bbox != null && bbox.getCoordinateSystem() != CRSUtils.EPSG_4326 ) {
                bbox = new GeometryTransformer( CRSUtils.EPSG_4326 ).transform( bbox );
            }
            if ( children != null && !children.isEmpty() ) {
                for ( Layer l : children ) {
                    Envelope lbox = l.getBbox();
                    if ( lbox != null && lbox.getCoordinateDimension() <= 1 ) {
                        lbox = null;
                    }
                    if ( lbox != null ) {
                        lbox = new GeometryTransformer( CRSUtils.EPSG_4326 ).transform( lbox );
                        if ( bbox == null ) {
                            bbox = lbox;
                        } else {
                            bbox = bbox.merge( lbox );
                        }
                    }
                }
            }
            return bbox;
        } catch ( TransformationException e ) {
            LOG.info( "A transformation was not possible. Most probably a bug in your setup. Message was '{}'.",
                      e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } catch ( UnknownCRSException e ) {
            LOG.info( "A crs was not known. Most probably a bug of some kind. Message was '{}'.",
                      e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
        return null;
    }

    /**
     * @param bbox
     *            the bbox to set
     */
    public void setBbox( Envelope bbox ) {
        this.bbox = bbox;
    }

    /**
     * @return the live list of srs
     */
    public LinkedList<ICRS> getSrs() {
        return srs;
    }

    /**
     * @param srs
     *            the srs to set (will be copied)
     */
    public void setSrs( Collection<ICRS> srs ) {
        this.srs = new LinkedList<ICRS>( srs );
    }

    /**
     * @return the scaleHint, SLD style
     */
    public DoublePair getScaleHint() {
        return scaleHint;
    }

    /**
     * @param scaleHint
     *            the scaleHint to set, SLD style
     */
    public void setScaleHint( DoublePair scaleHint ) {
        this.scaleHint = scaleHint;
    }

    /**
     * @return the parent layer, or null
     */
    public Layer getParent() {
        return parent;
    }

    /**
     * @param parent
     *            the parent to set
     */
    public void setParent( Layer parent ) {
        this.parent = parent;
    }

    /**
     * @return the live list of children
     */
    public LinkedList<Layer> getChildren() {
        return children;
    }

    /**
     * @param children
     *            the new children (will be copied)
     */
    public void setChildren( List<Layer> children ) {
        this.children = new LinkedList<Layer>( children );
    }

    /**
     * @param gm
     * @param style
     * @return a buffered image containing the map, and warning headers
     * @throws MissingDimensionValue
     * @throws InvalidDimensionValue
     */
    public Pair<BufferedImage, LinkedList<String>> paintMap( GetMap gm, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue {
        BufferedImage img = prepareImage( gm );
        Graphics2D g = img.createGraphics();
        LinkedList<String> list = paintMap( g, gm, style );
        g.dispose();
        return new Pair<BufferedImage, LinkedList<String>>( img, list );
    }

    /**
     * @param g
     * @param gm
     * @param style
     * @return a list of warning headers (currently only used for dimension warnings)
     * @throws MissingDimensionValue
     * @throws InvalidDimensionValue
     */
    public abstract LinkedList<String> paintMap( Graphics2D g, GetMap gm, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue;

    /**
     * @param fi
     * @param style
     * @return a collection of matching features and a list of warning headers (currently only used for dimension
     *         warnings)
     * @throws MissingDimensionValue
     * @throws InvalidDimensionValue
     */
    public abstract Pair<FeatureCollection, LinkedList<String>> getFeatures( GetFeatureInfo fi, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue;

    /**
     * @param name
     * @return null, or the layer with the given name
     */
    public Layer getChild( String name ) {
        for ( Layer l : children ) {
            if ( l.getName() != null && l.getName().equals( name ) ) {
                return l;
            }
        }
        return null;
    }

    /**
     *
     */
    public void close() {
        // nothing to do here
    }

    /**
     * @param layer
     */
    public void addOrReplace( Layer layer ) {
        ListIterator<Layer> iter = children.listIterator();
        while ( iter.hasNext() ) {
            Layer next = iter.next();
            if ( next.name != null && next.name.equals( layer.getName() ) ) {
                next.close();
                iter.set( layer );
                return;
            }
        }
        children.add( layer );
    }

    /**
     * @return true if it's data source is currently available
     */
    public boolean isAvailable() {
        return true;
    }

    /**
     * @param l
     */
    public void remove( Layer l ) {
        children.remove( l );
    }

    /**
     * @return the feature type, or null, if not applicable
     */
    public abstract FeatureType getFeatureType();

    /**
     * @return all dimensions including time and elevation (if applicable)
     */
    public Map<String, Dimension<?>> getDimensions() {
        HashMap<String, Dimension<?>> dims = new HashMap<String, Dimension<?>>();
        if ( time != null ) {
            dims.put( "time", time );
        }
        dims.putAll( dimensions );

        return dims;
    }

    /**
     * @return false, if queryable has been turned off
     */
    public boolean isQueryable() {
        return queryable;
    }

}
