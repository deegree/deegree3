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

import static org.deegree.commons.ows.metadata.DescriptionConverter.fromJaxb;
import static org.deegree.geometry.metadata.SpatialMetadataConverter.fromJaxb;
import static org.deegree.layer.config.ConfigUtils.parseDimensions;
import static org.deegree.layer.persistence.coverage.CoverageFeatureTypeBuilder.buildFeatureType;

import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.utils.DoublePair;
import org.deegree.coverage.Coverage;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.layer.config.ConfigUtils;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.layer.persistence.base.jaxb.ScaleDenominatorsType;
import org.deegree.layer.persistence.coverage.jaxb.CoverageLayerType;

/**
 * Builds basic layer metadata info from jaxb beans and coverage.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class LayerMetadataBuilder {

	static LayerMetadata buildLayerMetadata(CoverageLayerType lay, Coverage cov) {
		SpatialMetadata smd = fromJaxb(lay.getEnvelope(), lay.getCRS());
		Description desc = fromJaxb(lay.getTitle(), lay.getAbstract(), lay.getKeywords());
		LayerMetadata md = new LayerMetadata(lay.getName(), desc, smd);
		md.setDimensions(parseDimensions(md.getName(), lay.getDimension()));
		md.setMapOptions(ConfigUtils.parseLayerOptions(lay.getLayerOptions()));
		md.setMetadataId(lay.getMetadataSetId());

		md.getFeatureTypes().add(buildFeatureType());

		if (smd.getEnvelope() == null) {
			smd.setEnvelope(cov.getEnvelope());
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

}
