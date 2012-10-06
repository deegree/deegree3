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

import static org.deegree.commons.ows.metadata.DescriptionConverter.fromJaxb;
import static org.deegree.commons.tom.primitive.BaseType.DECIMAL;
import static org.deegree.geometry.metadata.SpatialMetadataConverter.fromJaxb;
import static org.deegree.layer.config.ConfigUtils.parseDimensions;
import static org.deegree.layer.config.ConfigUtils.parseStyles;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.coverage.Coverage;
import org.deegree.coverage.persistence.CoverageBuilderManager;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.MultiResolutionRaster;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericAppSchema;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.layer.Layer;
import org.deegree.layer.config.ConfigUtils;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.LayerStoreProvider;
import org.deegree.layer.persistence.MultipleLayerStore;
import org.deegree.layer.persistence.SingleLayerStore;
import org.deegree.layer.persistence.base.jaxb.ScaleDenominatorsType;
import org.deegree.layer.persistence.coverage.jaxb.CoverageLayerType;
import org.deegree.layer.persistence.coverage.jaxb.CoverageLayers;
import org.deegree.layer.persistence.coverage.jaxb.CoverageLayers.AutoLayers;
import org.deegree.style.persistence.StyleStore;
import org.deegree.style.persistence.StyleStoreManager;
import org.deegree.style.se.unevaluated.Style;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class CoverageLayerProvider implements LayerStoreProvider {

    private static final Logger LOG = getLogger( CoverageLayerProvider.class );

    private static final URL CONFIG_SCHEMA = CoverageLayerProvider.class.getResource( "/META-INF/schemas/layers/coverage/3.2.0/coverage.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    private LayerStore createFromAutoLayers( AutoLayers cfg )
                            throws ResourceInitException {
        String cid = cfg.getCoverageStoreId();
        String sid = cfg.getStyleStoreId();
        CoverageBuilderManager mgr = workspace.getSubsystemManager( CoverageBuilderManager.class );
        StyleStoreManager smgr = workspace.getSubsystemManager( StyleStoreManager.class );
        Coverage cov = mgr.get( cid );
        StyleStore sstore = smgr.get( sid );
        if ( cov == null ) {
            throw new ResourceInitException( "Coverage store with id " + cid + " is not available." );
        }
        if ( sid != null && sstore == null ) {
            throw new ResourceInitException( "Style store with id " + sid + " is not available." );
        }

        SpatialMetadata smd = new SpatialMetadata( cov.getEnvelope(),
                                                   Collections.singletonList( cov.getCoordinateSystem() ) );
        Description desc = new Description( cid, Collections.singletonList( new LanguageString( cid, null ) ), null,
                                            null );
        LayerMetadata md = new LayerMetadata( cid, desc, smd );

        // add standard coverage feature type to list of feature types
        List<PropertyType> pts = new LinkedList<PropertyType>();
        pts.add( new SimplePropertyType( new QName( "http://www.deegree.org/app", "value", "app" ), 0, -1, DECIMAL,
                                         null, null ) );
        FeatureType featureType = new GenericFeatureType( new QName( "http://www.deegree.org/app", "data", "app" ),
                                                          pts, false );
        // needed to get the back reference to the schema into the featureType (it's a strange mechanism indeed)
        new GenericAppSchema( new FeatureType[] { featureType }, null, null, null, null, null );
        md.getFeatureTypes().add( featureType );

        if ( sstore != null ) {
            for ( Style s : sstore.getAll( cid ) ) {
                md.getStyles().put( s.getName(), s );
            }
        }

        Layer l = new CoverageLayer( md, cov instanceof AbstractRaster ? (AbstractRaster) cov : null,
                                     cov instanceof MultiResolutionRaster ? (MultiResolutionRaster) cov : null );
        return new SingleLayerStore( l );
    }

    @Override
    public LayerStore create( URL configUrl )
                            throws ResourceInitException {
        try {
            CoverageLayers cfg;
            cfg = (CoverageLayers) JAXBUtils.unmarshall( "org.deegree.layer.persistence.coverage.jaxb", CONFIG_SCHEMA,
                                                         configUrl, workspace );
            if ( cfg.getAutoLayers() != null ) {
                LOG.debug( "Using auto configuration for coverage layers." );
                return createFromAutoLayers( cfg.getAutoLayers() );
            }

            LOG.debug( "Using manual configuration for coverage layers." );

            Map<String, Layer> map = new HashMap<String, Layer>();

            CoverageBuilderManager mgr = workspace.getSubsystemManager( CoverageBuilderManager.class );
            Coverage cov = mgr.get( cfg.getCoverageStoreId() );

            if ( cov == null ) {
                throw new ResourceInitException( "Coverage store with id '" + cfg.getCoverageStoreId()
                                                 + "' is not available." );
            }

            for ( CoverageLayerType lay : cfg.getCoverageLayer() ) {
                SpatialMetadata smd = fromJaxb( lay.getEnvelope(), lay.getCRS() );
                Description desc = fromJaxb( lay.getTitle(), lay.getAbstract(), lay.getKeywords() );
                LayerMetadata md = new LayerMetadata( lay.getName(), desc, smd );
                md.setDimensions( parseDimensions( md.getName(), lay.getDimension() ) );
                md.setMapOptions( ConfigUtils.parseLayerOptions( lay.getLayerOptions() ) );
                md.setMetadataId( lay.getMetadataSetId() );
                // add standard coverage feature type to list of feature types
                List<PropertyType> pts = new LinkedList<PropertyType>();
                pts.add( new SimplePropertyType( new QName( "http://www.deegree.org/app", "value", "app" ), 0, -1,
                                                 DECIMAL, null, null ) );
                FeatureType featureType = new GenericFeatureType( new QName( "http://www.deegree.org/app", "data",
                                                                             "app" ), pts, false );
                // needed to get the back reference to the schema into the featureType (it's a strange mechanism indeed)
                new GenericAppSchema( new FeatureType[] { featureType }, null, null, null, null, null );
                md.getFeatureTypes().add( featureType );

                if ( smd.getEnvelope() == null ) {
                    smd.setEnvelope( cov.getEnvelope() );
                }
                if ( smd.getCoordinateSystems() == null || smd.getCoordinateSystems().isEmpty() ) {
                    List<ICRS> crs = new ArrayList<ICRS>();
                    crs.add( smd.getEnvelope().getCoordinateSystem() );
                    smd.setCoordinateSystems( crs );
                }

                ScaleDenominatorsType denoms = lay.getScaleDenominators();
                if ( denoms != null ) {
                    md.setScaleDenominators( new DoublePair( denoms.getMin(), denoms.getMax() ) );
                }

                Pair<Map<String, Style>, Map<String, Style>> p = parseStyles( workspace, lay.getName(),
                                                                              lay.getStyleRef() );
                md.setStyles( p.first );
                md.setLegendStyles( p.second );
                Layer l = new CoverageLayer( md, cov instanceof AbstractRaster ? (AbstractRaster) cov : null,
                                             cov instanceof MultiResolutionRaster ? (MultiResolutionRaster) cov : null );
                map.put( lay.getName(), l );
            }

            return new MultipleLayerStore( map );
        } catch ( Throwable e ) {
            throw new ResourceInitException( "Error while creating coverage layers: " + e.getLocalizedMessage(), e );
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { CoverageBuilderManager.class, StyleStoreManager.class };
    }

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/layers/coverage";
    }

    @Override
    public URL getConfigSchema() {
        return CONFIG_SCHEMA;
    }

}
