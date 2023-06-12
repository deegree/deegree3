package org.deegree.feature.persistence.sql.mapper;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import javax.xml.namespace.QName;

import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class MappingContextManagerTest {

	@Test
	public void columnName_InLength() throws Exception {
		MappingContextManager mappingContextManager = new MappingContextManager(Collections.<String, String>emptyMap(),
				10, false);
		MappingContext mappingContext = mappingContextManager.newContext(new QName("first"), "id");

		MappingContext parent = mappingContextManager.mapOneToOneElement(mappingContext, new QName("parent"));
		assertThat(parent.getColumn(), is("parent"));

		MappingContext shortName = mappingContextManager.mapOneToOneElement(parent, new QName("sn"));
		assertThat(shortName.getColumn(), is("parent_sn"));
	}

	@Test
	public void columnName_NotInLength_1() throws Exception {
		MappingContextManager mappingContextManager = new MappingContextManager(Collections.<String, String>emptyMap(),
				10, false);
		MappingContext mappingContext = mappingContextManager.newContext(new QName("first"), "id");
		MappingContext parent = mappingContextManager.mapOneToOneElement(mappingContext, new QName("parent"));

		createMappings(mappingContextManager, parent, "100");

		MappingContext columnLongerWithSmallId = mappingContextManager.mapOneToOneElement(parent, new QName("longer"));
		// parent_longer, last id = 100
		assertThat(columnLongerWithSmallId.getColumn().length(), is(10));
		assertThat(columnLongerWithSmallId.getColumn(), is("parent_101"));
	}

	@Test
	public void columnName_NotInLength_2() throws Exception {
		MappingContextManager mappingContextManager = new MappingContextManager(Collections.<String, String>emptyMap(),
				10, false);
		MappingContext mappingContext = mappingContextManager.newContext(new QName("first"), "id");
		MappingContext parent = mappingContextManager.mapOneToOneElement(mappingContext, new QName("parent"));

		createMappings(mappingContextManager, parent, "1000000");

		MappingContext columnLongerWithLargeId = mappingContextManager.mapOneToOneElement(parent, new QName("longer"));
		// parent_longer, last id = 1000000
		assertThat(columnLongerWithLargeId.getColumn().length(), is(10));
		assertThat(columnLongerWithLargeId.getColumn(), is("pa_1000001"));
	}

	@Test
	public void columnName_LengthExactlyTheId() throws Exception {
		MappingContextManager mappingContextManager = new MappingContextManager(Collections.<String, String>emptyMap(),
				3, false);
		MappingContext mappingContext = mappingContextManager.newContext(new QName("first"), "id");
		MappingContext parent = mappingContextManager.mapOneToOneElement(mappingContext, new QName("parent"));

		createMappings(mappingContextManager, parent, "99");
		// parent_longer, last id = 100
		MappingContext columnLongerWithLargeId = mappingContextManager.mapOneToOneElement(parent, new QName("longer"));
		assertThat(columnLongerWithLargeId.getColumn().length(), is(3));
		assertThat(columnLongerWithLargeId.getColumn(), is("100"));
	}

	@Test
	public void columnName_MaxVerySmall() {
		MappingContextManager mappingContextManager = new MappingContextManager(Collections.<String, String>emptyMap(),
				1, false);
		MappingContext mappingContext = mappingContextManager.newContext(new QName("first"), "id");

		createMappings(mappingContextManager, mappingContext, "9");
		MappingContext first = mappingContextManager.mapOneToOneElement(mappingContext, new QName("first"));
		assertThat(first.getColumn().length(), is(1));
	}

	private void createMappings(MappingContextManager mappingContextManager, MappingContext mappingContext,
			String idOfLastMapping) {
		int indexOfMapping = 1;
		MappingContext newMappingContext;
		do {
			newMappingContext = mappingContextManager.mapOneToOneElement(mappingContext,
					new QName("map" + indexOfMapping++));
		}
		while (!newMappingContext.getColumn().endsWith(idOfLastMapping));
	}

}