/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.feature.persistence.sql;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.jdbc.TableName;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.feature.persistence.sql.rules.GeometryMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.filter.expression.ValueReference;
import org.deegree.sqldialect.filter.DBField;
import org.deegree.sqldialect.filter.MappingExpression;
import org.deegree.sqldialect.filter.PropertyNameMapping;
import org.deegree.sqldialect.filter.TableAliasManager;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class SQLPropertyNameMapperTest {

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorFtMapping_Null() throws Exception {
		FeatureTypeMapping ftMapping = null;
		new SQLPropertyNameMapper(mockFeatureStore(), ftMapping, false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorFtMappingList_Null() throws Exception {
		List<FeatureTypeMapping> ftMapping = null;
		new SQLPropertyNameMapper(mockFeatureStore(), ftMapping, false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorFtMappingList_Empty() throws Exception {
		List<FeatureTypeMapping> ftMapping = new ArrayList<FeatureTypeMapping>();
		new SQLPropertyNameMapper(mockFeatureStore(), ftMapping, false);
	}

	@Test
	public void testGetSpatialMapping() throws Exception {
		ValueReference propName = new ValueReference("app:ftType2/app:geometry", nsContext());
		List<FeatureTypeMapping> ftMapping = createFeatureTypeMappings(propName);
		SQLPropertyNameMapper mapper = new SQLPropertyNameMapper(mockFeatureStore(), ftMapping, false);
		PropertyNameMapping spatialMapping = mapper.getSpatialMapping(propName, mockAliasManager());

		assertThat(spatialMapping, notNullValue());
	}

	@Test
	public void testGetSpatialMapping_withMissingNamespaceBinding() throws Exception {
		ValueReference propName = new ValueReference("app:ftType2/app:geometry",
				CommonNamespaces.getNamespaceContext());
		List<FeatureTypeMapping> ftMapping = createFeatureTypeMappings(propName);
		SQLPropertyNameMapper mapper = new SQLPropertyNameMapper(mockFeatureStore(), ftMapping, false);
		PropertyNameMapping spatialMapping = mapper.getSpatialMapping(propName, mockAliasManager());

		assertThat(spatialMapping, notNullValue());
	}

	@Test
	public void testGetSpatialMapping_withMissingNamespaceBindingAndPrefix() throws Exception {
		ValueReference propName = new ValueReference("ftType2/geometry", CommonNamespaces.getNamespaceContext());
		List<FeatureTypeMapping> ftMapping = createFeatureTypeMappings(propName);
		SQLPropertyNameMapper mapper = new SQLPropertyNameMapper(mockFeatureStore(), ftMapping, false);
		PropertyNameMapping spatialMapping = mapper.getSpatialMapping(propName, mockAliasManager());

		assertThat(spatialMapping, notNullValue());
	}

	private List<FeatureTypeMapping> createFeatureTypeMappings(ValueReference valueReference) {
		List<FeatureTypeMapping> ftMapping = new ArrayList<FeatureTypeMapping>();
		ftMapping.add(mockFeatureTypeMapping("ftType1", "http://www.deegree.org/app", valueReference));
		ftMapping.add(mockFeatureTypeMapping("ftType2", "http://www.deegree.org/app", valueReference));
		return ftMapping;
	}

	private SQLFeatureStore mockFeatureStore() {
		return mock(SQLFeatureStore.class);
	}

	private FeatureTypeMapping mockFeatureTypeMapping(String featureTypeName, String featureTypeNamespace,
			ValueReference valueReference) {
		QName featureType = new QName(featureTypeNamespace, featureTypeName, "app");
		FeatureTypeMapping mockedFtMapping = mock(FeatureTypeMapping.class);
		when(mockedFtMapping.getFtTable()).thenReturn(new TableName("table"));
		when(mockedFtMapping.getFeatureType()).thenReturn(featureType);
		List<Mapping> mappings = new ArrayList<Mapping>();
		MappingExpression mappingExpression = new DBField("column");
		Mapping mapping = new GeometryMapping(valueReference, true, mappingExpression, GeometryType.GEOMETRY, null,
				null);
		mappings.add(mapping);
		when(mockedFtMapping.getMappings()).thenReturn(mappings);
		return mockedFtMapping;
	}

	private TableAliasManager mockAliasManager() {
		return mock(TableAliasManager.class);
	}

	private NamespaceBindings nsContext() {
		NamespaceBindings nsContext = CommonNamespaces.getNamespaceContext();
		nsContext.addNamespace("app", "http://www.deegree.org/app");
		return nsContext;
	}

}