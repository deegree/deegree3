//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

 Occam Labs Schmitz & Schneider GbR
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.layer.persistence.feature;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.feature.persistence.FeatureStores.getCombinedEnvelope;
import static org.deegree.geometry.metadata.SpatialMetadataConverter.fromJaxb;
import static org.deegree.layer.config.ConfigUtils.parseDimensions;
import static org.deegree.layer.config.ConfigUtils.parseStyles;
import static org.deegree.protocol.ows.metadata.DescriptionConverter.fromJaxb;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.layer.Layer;
import org.deegree.layer.config.ConfigUtils;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.persistence.LayerStoreProvider;
import org.deegree.layer.persistence.MultipleLayerStore;
import org.deegree.layer.persistence.base.jaxb.ScaleDenominatorsType;
import org.deegree.layer.persistence.feature.jaxb.FeatureLayerType;
import org.deegree.layer.persistence.feature.jaxb.FeatureLayers;
import org.deegree.layer.persistence.feature.jaxb.FeatureLayers.AutoLayers;
import org.deegree.protocol.ows.metadata.Description;
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
public class FeatureLayerProvider implements LayerStoreProvider {

    private static final Logger LOG = getLogger( FeatureLayerProvider.class );

    private static final URL SCHEMA_URL = FeatureLayerProvider.class.getResource( "/META-INF/schemas/layers/feature/3.2.0/feature.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    private MultipleLayerStore createInAutoMode( AutoLayers auto )
                            throws ResourceInitException {
        LOG.debug( "Creating feature layers for all feature types automatically." );

        Map<String, Layer> map = new LinkedHashMap<String, Layer>();
        FeatureStoreManager mgr = workspace.getSubsystemManager( FeatureStoreManager.class );
        String id = auto.getFeatureStoreId();
        FeatureStore store = mgr.get( id );
        if ( store == null ) {
            throw new ResourceInitException( "Feature layer config was invalid, feature store with id " + id
                                             + " is not available." );
        }
        StyleStoreManager smgr = workspace.getSubsystemManager( StyleStoreManager.class );
        id = auto.getStyleStoreId();
        StyleStore sstore = smgr.get( id );
        if ( id != null && sstore == null ) {
            throw new ResourceInitException( "Feature layer config was invalid, style store with id " + id
                                             + " is not available." );
        }

        for ( FeatureType ft : store.getSchema().getFeatureTypes() ) {
            String name = ft.getName().getLocalPart();
            List<ICRS> crs = new ArrayList<ICRS>();
            Envelope envelope = null;
            try {
                envelope = store.getEnvelope( ft.getName() );
            } catch ( Throwable e ) {
                LOG.debug( "Could not get envelope from feature store: {}", e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            }
            if ( envelope != null ) {
                crs.add( envelope.getCoordinateSystem() );
            }
            SpatialMetadata smd = new SpatialMetadata( envelope, crs );
            LayerMetadata md = new LayerMetadata( name, null, smd );
            md.getFeatureTypes().add( ft );
            Map<String, Style> styles = new LinkedHashMap<String, Style>();
            if ( sstore != null ) {
                for ( Style s : sstore.getAll( name ) ) {
                    styles.put( s.getName(), s );
                    if ( !styles.containsKey( "default" ) ) {
                        styles.put( "default", s );
                    }
                }
            }
            if ( !styles.containsKey( "default" ) ) {
                styles.put( "default", new Style() );
            }
            md.setStyles( styles );
            Layer l = new FeatureLayer( md, store, ft.getName(), null );
            map.put( name, l );
        }

        return new MultipleLayerStore( map );
    }

    @Override
    public MultipleLayerStore create( URL configUrl )
                            throws ResourceInitException {
        String pkg = "org.deegree.layer.persistence.feature.jaxb";
        try {
            FeatureLayers lays = (FeatureLayers) unmarshall( pkg, SCHEMA_URL, configUrl, workspace );

            if ( lays.getAutoLayers() != null ) {
                return createInAutoMode( lays.getAutoLayers() );
            }

            LOG.debug( "Creating configured feature layers only." );

            FeatureStoreManager mgr = workspace.getSubsystemManager( FeatureStoreManager.class );
            String id = lays.getFeatureStoreId();
            FeatureStore store = mgr.get( id );
            if ( store == null ) {
                throw new ResourceInitException( "Feature layer config was invalid, feature store with id " + id
                                                 + " is not available." );
            }

            Map<String, Layer> map = new LinkedHashMap<String, Layer>();
            for ( FeatureLayerType lay : lays.getFeatureLayer() ) {
                QName featureType = lay.getFeatureType();

                OperatorFilter filter = null;
                XMLInputFactory fac = XMLInputFactory.newInstance();
                if ( lay.getFilter() != null ) {
                    XMLStreamReader reader = fac.createXMLStreamReader( new DOMSource( lay.getFilter() ) );
                    nextElement( reader );
                    nextElement( reader );
                    filter = (OperatorFilter) Filter110XMLDecoder.parse( reader );
                    reader.close();
                }

                SpatialMetadata smd = fromJaxb( lay.getEnvelope(), lay.getCRS() );
                Description desc = fromJaxb( lay.getTitle(), lay.getAbstract(), lay.getKeywords() );
                LayerMetadata md = new LayerMetadata( lay.getName(), desc, smd );
                md.setMapOptions( ConfigUtils.parseLayerOptions( lay.getLayerOptions() ) );
                md.setDimensions( parseDimensions( md.getName(), lay.getDimension() ) );
                if ( featureType != null ) {
                    md.getFeatureTypes().add( store.getSchema().getFeatureType( featureType ) );
                } else {
                    md.getFeatureTypes().addAll( Arrays.asList( store.getSchema().getFeatureTypes() ) );
                }

                if ( smd.getEnvelope() == null ) {
                    if ( featureType != null ) {
                        smd.setEnvelope( store.getEnvelope( featureType ) );
                    } else {
                        smd.setEnvelope( getCombinedEnvelope( store ) );
                    }
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
                Layer l = new FeatureLayer( md, store, featureType, filter );
                map.put( lay.getName(), l );
            }
            return new MultipleLayerStore( map );
        } catch ( Throwable e ) {
            throw new ResourceInitException( "Could not parse layer configuration file.", e );
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { FeatureStoreManager.class, StyleStoreManager.class };
    }

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/layers/feature";
    }

    @Override
    public URL getConfigSchema() {
        return SCHEMA_URL;
    }

}
