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
package org.deegree.layer.persistence.feature;

import static org.deegree.commons.ows.metadata.DescriptionConverter.fromJaxb;
import static org.deegree.feature.persistence.FeatureStores.getCombinedEnvelope;
import static org.deegree.geometry.metadata.SpatialMetadataConverter.fromJaxb;
import static org.deegree.layer.config.ConfigUtils.parseDimensions;
import static org.deegree.layer.config.ConfigUtils.parseLayerOptions;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.DoublePair;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.persistence.base.jaxb.ScaleDenominatorsType;
import org.deegree.layer.persistence.feature.jaxb.FeatureLayerType;
import org.slf4j.Logger;

/**
 * Builds layer metadata objects from jaxb config for feature layers.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class LayerMetadataBuilder {

	private static final Logger LOG = getLogger(LayerMetadataBuilder.class);

	static LayerMetadata buildMetadata(FeatureLayerType lay, QName featureType, FeatureStore store)
			throws FeatureStoreException {
		SpatialMetadata smd = fromJaxb(lay.getEnvelope(), lay.getCRS());
		Description desc = fromJaxb(lay.getTitle(), lay.getAbstract(), lay.getKeywords());
		LayerMetadata md = new LayerMetadata(lay.getName(), desc, smd);
		md.setMapOptions(parseLayerOptions(lay.getLayerOptions()));
		md.setDimensions(parseDimensions(md.getName(), lay.getDimension()));
		md.setMetadataId(lay.getMetadataSetId());
		if (featureType != null) {
			md.getFeatureTypes().add(store.getSchema().getFeatureType(featureType));
		}
		else {
			md.getFeatureTypes().addAll(Arrays.asList(store.getSchema().getFeatureTypes()));
		}

		if (smd.getEnvelope() == null) {
			if (featureType != null) {
				smd.setEnvelope(store.getEnvelope(featureType));
			}
			else {
				smd.setEnvelope(getCombinedEnvelope(store));
			}
		}
		if (smd.getCoordinateSystems() == null || smd.getCoordinateSystems().isEmpty()) {
			List<ICRS> crs = new ArrayList<ICRS>();
			crs.add(smd.getEnvelope().getCoordinateSystem());
			smd.setCoordinateSystems(crs);
		}

		ScaleDenominatorsType denoms = lay.getScaleDenominators();
		if (denoms != null) {
			md.setScaleDenominators(new DoublePair(denoms.getMin(), denoms.getMax()));
		}

		return md;
	}

	static LayerMetadata buildMetadataForAutoMode(FeatureStore store, FeatureType ft, String name) {
		List<ICRS> crs = new ArrayList<ICRS>();
		Envelope envelope = null;
		try {
			envelope = store.getEnvelope(ft.getName());
		}
		catch (Throwable e) {
			LOG.debug("Could not get envelope from feature store: {}", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
		}
		if (envelope != null) {
			crs.add(envelope.getCoordinateSystem());
		}
		SpatialMetadata smd = new SpatialMetadata(envelope, crs);
		Description desc = new Description(name, Collections.singletonList(new LanguageString(name, null)), null, null);
		LayerMetadata md = new LayerMetadata(name, desc, smd);
		md.getFeatureTypes().add(ft);
		return md;
	}

}
