/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2017 by:
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
package org.deegree.feature.persistence.sql.id;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.MappedAppSchema;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureType;
import org.junit.Test;

/**
 * {@link IdAnalyzer} tests for checking the correct deriving of a feature type from a
 * feature id.
 */
public class IdAnalyzerTest {

	private IdAnalyzer idAnalyzer;

	@Test
	public void analyzeFeatureIds() {
		idAnalyzer = setupAnalyzerScenario("APP_FEATURE1_", "APP_FEATURE2_", "APP_FEATURE3_");
		assertEquals("APP_FEATURE1_", analyzeFeatureType("APP_FEATURE1_1"));
		assertEquals("APP_FEATURE2_", analyzeFeatureType("APP_FEATURE2_1"));
		assertEquals("APP_FEATURE3_", analyzeFeatureType("APP_FEATURE3_1"));
	}

	@Test
	public void analyzeGeometryIds() {
		idAnalyzer = setupAnalyzerScenario("APP_FEATURE1_", "APP_FEATURE2_", "APP_FEATURE3_");
		assertEquals("APP_FEATURE1_", analyzeFeatureType("APP_FEATURE1_1_APP_GEOM"));
		assertEquals("APP_FEATURE2_", analyzeFeatureType("APP_FEATURE2_1_APP_GEOM"));
		assertEquals("APP_FEATURE3_", analyzeFeatureType("APP_FEATURE3_1_APP_GEOM"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void analyzeNoMatch() {
		idAnalyzer = setupAnalyzerScenario("APP_FEATURE_");
		analyzeFeatureType("BPP_FEATURE_1");
	}

	@Test
	public void analyzeFeatureIdMultiplePrefixMatches() {
		// these tests used to fail with Java 8 (see
		// https://github.com/deegree/deegree3/issues/848)
		idAnalyzer = setupAnalyzerScenario("APP_FEATURE_", "APP_FEATURE_X_");
		assertEquals("APP_FEATURE_X_", analyzeFeatureType("APP_FEATURE_X_1"));
		// actual production case that used to fail
		idAnalyzer = setupAnalyzerScenario("IMRO_GEOMETRIESTRUCTUURVISIEOBJECT_",
				"IMRO_GEOMETRIESTRUCTUURVISIEOBJECT_P_");
		assertEquals("IMRO_GEOMETRIESTRUCTUURVISIEOBJECT_P_",
				analyzeFeatureType("IMRO_GEOMETRIESTRUCTUURVISIEOBJECT_P_1"));
	}

	private String analyzeFeatureType(final String fid) {
		return idAnalyzer.analyze(fid).getFeatureType().getName().getLocalPart();
	}

	private IdAnalyzer setupAnalyzerScenario(final String... idPrefixes) {
		final FeatureType[] fts = new FeatureType[idPrefixes.length];
		final FeatureTypeMapping[] ftMappings = new FeatureTypeMapping[idPrefixes.length];
		int i = 0;
		for (final String idPrefix : idPrefixes) {
			fts[i] = buildFeatureType(idPrefix);
			ftMappings[i] = buildFeatureTypeMapping(fts[i], idPrefix);
			i++;
		}
		final MappedAppSchema schema = new MappedAppSchema(fts, null, null, null, ftMappings, null, null, null, false,
				null, null, null);
		return new IdAnalyzer(schema);
	}

	private FeatureType buildFeatureType(final String localName) {
		return new GenericFeatureType(buildQName(localName), Collections.<PropertyType>emptyList(), false);
	}

	private FeatureTypeMapping buildFeatureTypeMapping(final FeatureType ft, final String fidPrefix) {
		final FIDMapping fidMapping = buildFidMapping(fidPrefix);
		return new FeatureTypeMapping(ft.getName(), new TableName(ft.getName().getLocalPart()), fidMapping,
				Collections.<Mapping>emptyList(), Collections.emptyList());
	}

	private FIDMapping buildFidMapping(final String fidPrefix) {
		final List<Pair<SQLIdentifier, BaseType>> fidColumns = new ArrayList<Pair<SQLIdentifier, BaseType>>();
		fidColumns.add(new Pair<SQLIdentifier, BaseType>(new SQLIdentifier("id"), BaseType.INTEGER));
		return new FIDMapping(fidPrefix, "_", fidColumns, null);
	}

	private QName buildQName(final String localPart) {
		return new QName("http://www.deegree.org/app", localPart, "app");
	}

}
