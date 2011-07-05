package org.deegree.layers;

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

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.cs.CRSUtils;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.XPathEvaluator;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.protocol.wms.WMSException.InvalidDimensionValue;
import org.deegree.protocol.wms.WMSException.MissingDimensionValue;
import org.deegree.rendering.r2d.Renderer;
import org.deegree.rendering.r2d.TextRenderer;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.rendering.r2d.styling.Styling;
import org.deegree.rendering.r2d.styling.TextStyling;
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
public abstract class AbstractLayer implements Layer {

    private static final Logger LOG = getLogger( AbstractLayer.class );

    private Envelope bbox;

    private LinkedList<Layer> children = new LinkedList<Layer>();

    private Layer parent;

    Dimension<Date> time;

    HashMap<String, Dimension<Object>> dimensions = new HashMap<String, Dimension<Object>>();

    private LayerMetadata metadata;

    protected AbstractLayer( LayerMetadata md, Layer parent ) {
        this.metadata = md;
        this.parent = parent;
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

    @Override
    public Envelope getEnvelope() {
        return bbox;
    }

    /**
     * @return the bbox
     */
    public Envelope getAggregatedEnvelope() {
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
                    Envelope lbox = l.getEnvelope();
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
     * @return the parent layer, or null
     */
    @Override
    public Layer getParent() {
        return parent;
    }

    /**
     * @param parent
     *            the parent to set
     */
    @Override
    public void setParent( Layer parent ) {
        this.parent = parent;
    }

    /**
     * @return the live list of children
     */
    @Override
    public List<Layer> getChildren() {
        return children;
    }

    /**
     * @param children
     *            the new children (will be copied)
     */
    @Override
    public void setChildren( List<Layer> children ) {
        this.children = new LinkedList<Layer>( children );
    }

    @Override
    public LayerMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void setEnvelope( Envelope envelope ) {
        this.bbox = envelope;
    }

    @Override
    public Pair<FeatureCollection, LinkedList<String>> getFeatures( RenderingInfo info, Style style )
                            throws MissingDimensionValue, InvalidDimensionValue {
        return new Pair<FeatureCollection, LinkedList<String>>( new GenericFeatureCollection(),
                                                                new LinkedList<String>() );
    }

    @Override
    public FeatureType getFeatureType() {
        return null;
    }

}
