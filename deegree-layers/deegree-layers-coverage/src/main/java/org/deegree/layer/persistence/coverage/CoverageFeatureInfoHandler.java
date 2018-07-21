//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.layer.persistence.coverage;

import org.apache.logging.log4j.Logger;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.geom.Grid;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeature;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Envelope;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Integer.MAX_VALUE;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.deegree.coverage.raster.utils.CoverageTransform.transform;

/**
 * Responsible for creating coverage feature info responses.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class CoverageFeatureInfoHandler {

    private static final Logger LOG = getLogger( CoverageFeatureInfoHandler.class );

    private AbstractRaster raster;

    private Envelope bbox;

    private FeatureType featureType;

    private InterpolationType interpol;

    CoverageFeatureInfoHandler( AbstractRaster raster, Envelope bbox, FeatureType featureType,
                                InterpolationType interpol ) {
        this.raster = raster;
        this.bbox = bbox;
        this.featureType = featureType;
        this.interpol = interpol;
    }

    FeatureCollection handleFeatureInfo() {
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

            Feature f = new GenericFeature( featureType, null, props, null );
            col.add( f );
            return col;
        } catch ( Throwable e ) {
            LOG.trace( "Stack trace:", e );
            LOG.error( "Unable to create raster feature info: {}", e.getLocalizedMessage() );
        }
        return null;
    }

}
