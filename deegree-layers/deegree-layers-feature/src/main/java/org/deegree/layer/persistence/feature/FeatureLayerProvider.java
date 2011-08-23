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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.utils.DoublePair;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.filter.Filter;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.layer.LayerMetadata;
import org.deegree.layer.persistence.LayerStore;
import org.deegree.layer.persistence.LayerStoreProvider;
import org.deegree.layer.persistence.SingleLayerStore;
import org.deegree.layer.persistence.feature.jaxb.FeatureLayer;
import org.deegree.layer.persistence.feature.jaxb.ScaleDenominatorsType;

/**
 * @author stranger
 * 
 */
public class FeatureLayerProvider implements LayerStoreProvider {

    private static final URL SCHEMA_URL = FeatureLayerProvider.class.getResource( "/META-INF/schemas/layers/feature/3.1.0/feature.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @Override
    public LayerStore create( URL configUrl )
                            throws ResourceInitException {
        String pkg = "org.deegree.layer.persistence.feature.jaxb";
        try {
            FeatureLayer lay = (FeatureLayer) unmarshall( pkg, SCHEMA_URL, configUrl, workspace );

            QName featureType = lay.getFeatureType();

            XMLInputFactory fac = XMLInputFactory.newInstance();
            XMLStreamReader reader = fac.createXMLStreamReader( new DOMSource( lay.getFilter() ) );
            nextElement( reader );
            nextElement( reader );
            Filter filter = Filter110XMLDecoder.parse( reader );
            reader.close();

            FeatureStoreManager mgr = workspace.getSubsystemManager( FeatureStoreManager.class );
            String fsRef = lay.getFeatureStoreId();
            FeatureStore fs = mgr.get( fsRef );
            if ( fs == null ) {
                throw new ResourceInitException( "Feature layer config was invalid, feature store with id " + fsRef
                                                 + " is not available." );
            }

            LayerMetadata md = new LayerMetadata();
            List<ICRS> crs = new ArrayList<ICRS>();
            if ( lay.getCRS() != null ) {
                String[] ss = lay.getCRS().split( "\\s" );
                for ( String s : ss ) {
                    crs.add( CRSManager.lookup( s ) );
                }
            }
            md.setCoordinateSystems( crs );
            ScaleDenominatorsType denoms = lay.getScaleDenominators();
            if ( denoms != null ) {
                md.setScaleDenominators( new DoublePair( denoms.getMin(), denoms.getMax() ) );
            }
            org.deegree.layer.persistence.feature.FeatureLayer l;
            l = new org.deegree.layer.persistence.feature.FeatureLayer( md, fs, featureType, filter, null, null );
            return new SingleLayerStore( l );
        } catch ( Throwable e ) {
            throw new ResourceInitException( "Could not parse layer configuration file.", e );
        }
    }

    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { FeatureStoreManager.class };
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
