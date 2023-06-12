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

package org.deegree.coverage.raster.io.oraclegeoraster;

import java.net.URL;

import org.deegree.coverage.Coverage;
import org.deegree.coverage.persistence.CoverageProvider;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

/**
 * Provider for Oracle GeoRaster coverages.
 *
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 * @since 3.4
 */
public class OracleGeorasterProvider extends CoverageProvider {

	private static final URL CONFIG_SCHEMA = OracleGeorasterProvider.class
		.getResource("/META-INF/schemas/datasource/coverage/oraclegeoraster/oraclegeoraster.xsd");

	@Override
	public String getNamespace() {
		return "http://www.deegree.org/datasource/coverage/oraclegeoraster";
	}

	@Override
	public ResourceMetadata<Coverage> createFromLocation(Workspace workspace, ResourceLocation<Coverage> location) {
		return new OracleGeorasterMetadata(workspace, location, this);
	}

	@Override
	public URL getSchema() {
		return CONFIG_SCHEMA;
	}

}