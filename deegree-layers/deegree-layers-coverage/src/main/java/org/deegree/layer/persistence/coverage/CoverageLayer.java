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

import static org.deegree.coverage.raster.interpolation.InterpolationType.BILINEAR;
import static org.deegree.coverage.raster.interpolation.InterpolationType.NEAREST_NEIGHBOR;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.coverage.rangeset.RangeSet;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.MultiResolutionRaster;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.geometry.Envelope;
import org.deegree.layer.AbstractLayer;
import org.deegree.layer.LayerQuery;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.rendering.r2d.context.MapOptions.Interpolation;
import org.deegree.style.StyleRef;
import org.deegree.style.se.unevaluated.Style;
import org.slf4j.Logger;

/**
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 *
 * @version $Revision: $, $Date: $
 */
public class CoverageLayer extends AbstractLayer {

    private static final Logger LOG = getLogger( CoverageLayer.class );

    private final AbstractRaster raster;

    private final MultiResolutionRaster multiraster;

    private final CoverageDimensionHandler dimensionHandler;

    public CoverageLayer( LayerMetadata md, AbstractRaster raster, MultiResolutionRaster multiraster ) {
        super( md );
        this.raster = raster;
        this.multiraster = multiraster;
        dimensionHandler = new CoverageDimensionHandler( md.getDimensions() );
    }

    @Override
    public CoverageLayerData mapQuery( LayerQuery query, List<String> headers )
                            throws OWSException {
        try {
            Envelope bbox = query.getEnvelope();
            RangeSet filter = dimensionHandler.getDimensionFilter( query.getDimensions(), headers );
            Style style = resolveStyleRef( query.getStyle() );
            // handle SLD/SE scale settings
            style = style == null ? null : style.filter( query.getScale() );

            Interpolation fromRequest = query.getRenderingOptions().getInterpolation( getMetadata().getName() );
            InterpolationType interpol = determineInterpolation( fromRequest );

            AbstractRaster raster = this.raster;
            if ( raster == null ) {
                raster = multiraster.getRaster( query.getResolution() );
            }

            return new CoverageLayerData( raster, bbox, query.getWidth(), query.getHeight(), interpol, filter, style,
                                          getMetadata().getFeatureTypes().get( 0 ) );
        } catch ( OWSException e ) {
            throw e;
        } catch ( Throwable e ) {
            LOG.warn( "Unable to prepare rendering of raster layer: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
        return null;
    }

    private InterpolationType determineInterpolation( Interpolation fromRequest ) {
        InterpolationType interpol = NEAREST_NEIGHBOR;
        if ( fromRequest != null ) {
            switch ( fromRequest ) {
            case BICUBIC:
                LOG.warn( "Raster API does not support bicubic interpolation, using bilinear instead." );
            case BILINEAR:
                interpol = BILINEAR;
                break;
            case NEARESTNEIGHBOR:
            case NEARESTNEIGHBOUR:
                interpol = NEAREST_NEIGHBOR;
                break;
            }
        }
        return interpol;
    }

    @Override
    public CoverageLayerData infoQuery( LayerQuery query, List<String> headers )
                            throws OWSException {
        try {
            int layerRadius = -1;
            if ( getMetadata().getMapOptions() != null ) {
                layerRadius = getMetadata().getMapOptions().getFeatureInfoRadius();
            }
            final Envelope bbox = query.calcClickBox( layerRadius > -1 ? layerRadius : query.getLayerRadius() );

            RangeSet filter = dimensionHandler.getDimensionFilter( query.getDimensions(), headers );

            StyleRef ref = query.getStyle();
            if ( !ref.isResolved() ) {
                ref.resolve( getMetadata().getStyles().get( ref.getName() ) );
            }
            Style style = ref.getStyle();
            // handle SLD/SE scale settings
            style = style == null ? null : style.filter( query.getScale() );

            AbstractRaster raster = this.raster;
            if ( raster == null ) {
                raster = multiraster.getRaster( query.getResolution() );
            }

            return new CoverageLayerData( raster, bbox, query.getWidth(), query.getHeight(),
                                          InterpolationType.NEAREST_NEIGHBOR, filter, style,
                                          getMetadata().getFeatureTypes().get( 0 ) , dimensionHandler );
        } catch ( OWSException e ) {
            throw e;
        } catch ( Throwable e ) {
            LOG.warn( "Unable to prepare feature info of raster layer: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
        return null;
    }

}
