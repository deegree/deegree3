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

package org.deegree.feature.persistence.sql.converter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.tom.gml.GMLReferenceResolver;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.sql.MappedAppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.gml.reference.FeatureReference;

/**
 * {@link ParticleConverter} for {@link Feature} particles.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class FeatureParticleConverter implements ParticleConverter<Feature> {

	private final SQLIdentifier fkColumn;

	private final SQLIdentifier hrefColumn;

	private final GMLReferenceResolver resolver;

	private final FeatureType valueFt;

	private final MappedAppSchema schema;

	private final String fidPrefix;

	public FeatureParticleConverter(SQLIdentifier fkColumn, SQLIdentifier hrefColumn, GMLReferenceResolver resolver,
			FeatureType valueFt, MappedAppSchema schema) {
		this.fkColumn = fkColumn;
		this.hrefColumn = hrefColumn;
		this.resolver = resolver;
		this.valueFt = valueFt;
		this.schema = schema;

		if (valueFt != null && schema.getSubtypes(valueFt).length == 0
				&& schema.getFtMapping(valueFt.getName()) != null) {
			fidPrefix = schema.getFtMapping(valueFt.getName()).getFidMapping().getPrefix();
		}
		else {
			fidPrefix = null;
		}
	}

	@Override
	public String getSelectSnippet(String tableAlias) {
		if (hrefColumn != null) {
			return tableAlias + "." + hrefColumn;
		}
		return tableAlias + "." + fkColumn;
	}

	@Override
	public Feature toParticle(ResultSet rs, int colIndex) throws SQLException {

		Object value = rs.getObject(colIndex);
		if (value == null) {
			return null;
		}
		if (hrefColumn != null) {
			return new FeatureReference(resolver, "" + value, null);
		}
		if (fidPrefix != null) {
			return new FeatureReference(resolver, "#" + fidPrefix + value, null);
		}
		return new FeatureReference(resolver, "#" + value, null);
	}

	@Override
	public String getSetSnippet(Feature particle) {
		return "?,?";
	}

	@Override
	public void setParticle(PreparedStatement stmt, Feature particle, int paramIndex) throws SQLException {
		// TODO currently hardcoded in InsertRowManager and related classes
	}

}