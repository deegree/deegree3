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
import static org.deegree.coverage.raster.utils.CoverageTransform.transform;
import static org.slf4j.LoggerFactory.getLogger;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Triple;
import org.deegree.coverage.filter.raster.RasterFilter;
import org.deegree.coverage.rangeset.RangeSet;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.geom.Grid;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.coverage.raster.utils.CoverageTransform;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeature;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
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

    public CoverageLayerData( AbstractRaster raster, Envelope bbox, int width, int height, InterpolationType interpol,
                              RangeSet filter, Style style, FeatureType featureType ) {
        this.raster = raster;
        this.bbox = bbox;
        this.width = width;
        this.height = height;
        this.interpol = interpol;
        this.filter = filter;
        this.style = style;
        this.featureType = featureType;
    }

    @Override
    public void render( RenderContext context ) {
        try {
            RasterRenderer renderer = context.getRasterRenderer();

            raster = CoverageTransform.transform( this.raster, bbox, Grid.fromSize( width, height, MAX_VALUE, bbox ),
                                                  interpol.toString() );

            if ( filter != null ) {
                RangeSet cbr = createBandRangeSetFromRaster( null, null, raster );
                raster = new RasterFilter( raster ).apply( cbr, filter );
            }

            LinkedList<Triple<Styling, LinkedList<Geometry>, String>> list = style == null || style.isDefault() ? null
                                                                                                               : style.evaluate( null,
                                                                                                                                 null );
            if ( list != null && list.size() > 0 ) {
                for ( Triple<Styling, LinkedList<Geometry>, String> t : list ) {
                    renderer.render( (RasterStyling) t.first, raster );
                }
            } else {
                renderer.render( null, raster );
            }
        } catch ( Throwable e ) {
            LOG.trace( "Stack trace:", e );
            LOG.error( "Unable to render raster: {}", e.getLocalizedMessage() );
        }
    }

    @Override
    public FeatureCollection info() {
        try {
            SimpleRaster res = transform( raster, bbox, Grid.fromSize( 1, 1, MAX_VALUE, bbox ), interpol.toString() ).getAsSimpleRaster();
            RasterData data = res.getRasterData();
            GenericFeatureCollection col = new GenericFeatureCollection();
            List<Property> props = new LinkedList<Property>();
            DataType dataType = data.getDataType();
            switch ( dataType ) {
            case SHORT:
            case USHORT: {
                PrimitiveValue val = new PrimitiveValue( new BigDecimal( 0xffff & data.getShortSample( 0, 0, 0 ) ),
                                                         new PrimitiveType( BaseType.DECIMAL ) );
                props.add( new GenericProperty( featureType.getPropertyDeclarations().get( 0 ), val ) );
                break;
            }
            case BYTE: {
                // TODO unknown why this always yields 0 values for eg. satellite images/RGB/ARGB
                for ( int i = 0; i < data.getBands(); ++i ) {
                    PrimitiveValue val = new PrimitiveValue( new BigDecimal( 0xff & data.getByteSample( 0, 0, i ) ),
                                                             new PrimitiveType( BaseType.DECIMAL ) );
                    props.add( new GenericProperty( featureType.getPropertyDeclarations().get( 0 ), val ) );
                }
                break;
            }
            case DOUBLE:
            case INT:
            case UNDEFINED:
                LOG.warn( "The raster is of type '{}', this is handled as float currently.", dataType );
            case FLOAT:
                props.add( new GenericProperty( featureType.getPropertyDeclarations().get( 0 ),
                                                new PrimitiveValue( new BigDecimal( data.getFloatSample( 0, 0, 0 ) ),
                                                                    new PrimitiveType( BaseType.DECIMAL ) ) ) );
                break;
            }

            Feature f = new GenericFeature( featureType, null, props, null, null );
            col.add( f );
            return col;
        } catch ( Throwable e ) {
            LOG.trace( "Stack trace:", e );
            LOG.error( "Unable to create raster feature info: {}", e.getLocalizedMessage() );
        }
        return null;
    }

}
