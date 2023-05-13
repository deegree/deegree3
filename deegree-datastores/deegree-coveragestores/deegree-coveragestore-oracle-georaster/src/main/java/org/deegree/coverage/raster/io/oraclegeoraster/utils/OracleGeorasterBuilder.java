/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2014 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - grit GmbH -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/

package org.deegree.coverage.raster.io.oraclegeoraster.utils;

import org.deegree.coverage.Coverage;
import org.deegree.coverage.persistence.oraclegeoraster.jaxb.OracleGeorasterConfig;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.MultiResolutionRaster;
import org.deegree.coverage.raster.io.oraclegeoraster.OracleGeorasterReader;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Oracle GeoRaster Builder
 *
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 * @since 3.4
 */
public class OracleGeorasterBuilder implements ResourceBuilder<Coverage> {

	private static final Logger LOG = LoggerFactory.getLogger(OracleGeorasterBuilder.class);

	private OracleGeorasterConfig config;

	private ResourceMetadata<Coverage> metadata;

	private Workspace workspace;

	public OracleGeorasterBuilder(OracleGeorasterConfig config, ResourceMetadata<Coverage> metadata,
			Workspace workspace) {
		this.config = config;
		this.metadata = metadata;
		this.workspace = workspace;
	}

	@Override
	public Coverage build() {
		try {
			OracleGeorasterReader rdr;
			rdr = new OracleGeorasterReader(workspace, config);

			return buildPyramidIfNeeded(rdr, rdr.getRaster());
		}
		catch (Exception e) {
			LOG.trace("Exception", e);
			throw new ResourceInitException("Could not build Oracle GeoRaster Reader: " + e.getMessage());
		}
	}

	private Coverage buildPyramidIfNeeded(OracleGeorasterReader rdr, AbstractRaster rasterLvl0) {

		if (rdr.isMultiResulution()) {
			AbstractRaster[] ary = rdr.getPyramidRaster();
			MultiResolutionRaster mrr = new MultiResolutionRaster(metadata);
			mrr.setCoordinateSystem(rasterLvl0.getCoordinateSystem());
			mrr.addRaster(rasterLvl0);
			for (int i = 0; i < ary.length; i++) {
				mrr.addRaster(ary[i]);
			}
			return mrr;
		}
		else {
			rasterLvl0.setMetadata(metadata);
			return rasterLvl0;
		}
	}

}