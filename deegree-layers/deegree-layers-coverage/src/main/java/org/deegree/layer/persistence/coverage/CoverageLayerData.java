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
package org.deegree.layer.persistence.coverage;

import static java.lang.Integer.MAX_VALUE;
import static org.deegree.coverage.rangeset.RangeSetBuilder.createBandRangeSetFromRaster;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.LinkedList;

import org.deegree.commons.utils.Triple;
import org.deegree.coverage.filter.raster.RasterFilter;
import org.deegree.coverage.rangeset.RangeSet;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.geom.Grid;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.coverage.raster.utils.CoverageTransform;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.layer.LayerData;
import org.deegree.rendering.r2d.RasterRenderer;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.style.styling.RasterStyling;
import org.deegree.style.styling.Styling;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class CoverageLayerData implements LayerData {

    private static final Logger LOG = getLogger( CoverageLayerData.class );

    private AbstractRaster raster;

    private final Envelope bbox;

    private final int width;

    private final int height;

    private final InterpolationType interpol;

    private final RangeSet filter;

    private final Style style;

    private final FeatureType featureType;

    private CoverageDimensionHandler dimensionHandler;

    public CoverageLayerData( AbstractRaster raster, Envelope bbox, int width, int height, InterpolationType interpol,
                              RangeSet filter, Style style, FeatureType featureType ) {
        this( raster, bbox, width, height, interpol, filter, style, featureType, null );

    }

    public CoverageLayerData( AbstractRaster raster, Envelope bbox, int width, int height, InterpolationType interpol,
                              RangeSet filter, Style style, FeatureType featureType,
                              CoverageDimensionHandler dimensionHandler ) {
        this.raster = raster;
        this.bbox = bbox;
        this.width = width;
        this.height = height;
        this.interpol = interpol;
        this.filter = filter;
        this.style = style;
        this.featureType = featureType;
        this.dimensionHandler = dimensionHandler;
    }

    @Override
    public void render( RenderContext context ) {
        try {
            // prevent transformation if not intersects
            ICRS bboxCRS = bbox.getCoordinateSystem();
            ICRS rasterCRS = raster.getCoordinateSystem();
            Envelope rasterBBox = raster.getEnvelope();
            Envelope workEnv = rasterBBox;

            if ( rasterBBox == null || rasterCRS == null || bboxCRS == null ) {
                // do not render, if no data is available for intersection check
                return;
            }

            if ( !rasterCRS.equals( bboxCRS ) ) {
                GeometryTransformer dstTransf = new GeometryTransformer( bboxCRS );
                workEnv = dstTransf.transform( rasterBBox, rasterCRS );
            }

            if ( !workEnv.intersects( bbox ) ) {
                // intersection is empty or no overlap
                return;
            }

            RasterRenderer renderer = context.getRasterRenderer();

            AbstractRaster result;
            result = CoverageTransform.transform( raster, bbox, Grid.fromSize( width, height, MAX_VALUE, bbox ),
                                                  interpol.toString() );

            if ( filter != null ) {
                RangeSet cbr = createBandRangeSetFromRaster( null, null, result );
                result = new RasterFilter( result ).apply( cbr, filter );
            }

            LinkedList<Triple<Styling, LinkedList<Geometry>, String>> list = style == null
                                                                             || style.isDefault() ? null
                                                                                                  : style.evaluate( null,
                                                                                                                    null );
            if ( list != null && list.size() > 0 ) {
                for ( Triple<Styling, LinkedList<Geometry>, String> t : list ) {
                    renderer.render( (RasterStyling) t.first, result );
                }
            } else {
                renderer.render( null, result );
            }
        } catch ( Throwable e ) {
            LOG.trace( "Stack trace:", e );
            LOG.error( "Unable to render raster: {}", e.getLocalizedMessage() );
        }
    }

    @Override
    public FeatureCollection info() {
        CoverageFeatureInfoHandler handler = new CoverageFeatureInfoHandler( raster, bbox, featureType, interpol,
                                                                             dimensionHandler );
        return handler.handleFeatureInfo();
    }

}
