package org.deegree.feature.persistence.sql.mapper;

import org.junit.Test;

import javax.xml.namespace.QName;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GmlReferenceDataTest {

	private static final QName FEATURETYPE_A_NAME = new QName("http://test.de/schema", "FeatureA", "te");

	private static final QName PROP_A1_NAME = new QName("http://test.de/schema", "prop_A1", "te");

	private static final QName PROP_A3_NAME = new QName("http://test.de/schema", "prop_A3", "te");

	private static final QName PROP_COMPLEX_A4_NAME = new QName("http://test.de/schema", "complex_A4", "te");

	private static final QName PROP_COMPLEX_A4_1_NAME = new QName("http://test.de/schema", "prop_A4_1", "te");

	private static final QName PROP_COMPLEX_A4_3_NAME = new QName("http://test.de/schema", "prop_A4_3", "te");

	private static final QName FEATURETYPE_B_NAME = new QName("http://test.de/schema", "FeatureB", "te");

	private static final QName PROP_B1_NAME = new QName("http://test.de/schema", "prop_B1", "te");

	private static final QName PROP_B3_NAME = new QName("http://test.de/schema", "prop_B3", "te");

	@Test
	public void test_NoSample() throws Exception {
		URL resource = getClass().getResource("data/sampleValues_simple_1.xml");
		GmlReferenceData gmlReferenceData = new GmlReferenceData(resource);

		boolean hasMaxOne1 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_B_NAME,
				Collections.singletonList(PROP_B1_NAME));
		assertThat(hasMaxOne1, is(false));
	}

	@Test
	public void test_NoProperty() throws Exception {
		URL resource = getClass().getResource("data/sampleValues_simple_1.xml");
		GmlReferenceData gmlReferenceData = new GmlReferenceData(resource);

		boolean hasMaxOne2 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_A_NAME,
				Collections.singletonList(PROP_A3_NAME));
		assertThat(hasMaxOne2, is(true));
	}

	@Test
	public void test_Simple_1() throws Exception {
		URL resource = getClass().getResource("data/sampleValues_simple_1.xml");
		GmlReferenceData gmlReferenceData = new GmlReferenceData(resource);

		boolean hasMaxOne1 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_A_NAME,
				Collections.singletonList(PROP_A1_NAME));
		assertThat(hasMaxOne1, is(true));

		boolean hasMaxOne3 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_A_NAME,
				Collections.singletonList(PROP_A3_NAME));
		assertThat(hasMaxOne3, is(true));
	}

	@Test
	public void test_Simple_N() throws Exception {
		URL resource = getClass().getResource("data/sampleValues_simple_N.xml");
		GmlReferenceData gmlReferenceData = new GmlReferenceData(resource);

		boolean hasMaxOne1 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_A_NAME,
				Collections.singletonList(PROP_A1_NAME));
		assertThat(hasMaxOne1, is(false));

		boolean hasMaxOne3 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_A_NAME,
				Collections.singletonList(PROP_A3_NAME));
		assertThat(hasMaxOne3, is(false));
	}

	@Test
	public void test_Complex_1() throws Exception {
		URL resource = getClass().getResource("data/sampleValues_complex_1.xml");
		GmlReferenceData gmlReferenceData = new GmlReferenceData(resource);

		boolean hasMaxOneB1 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_A_NAME,
				Collections.singletonList(PROP_COMPLEX_A4_NAME));
		assertThat(hasMaxOneB1, is(true));

		boolean hasMaxOneA4_1 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_A_NAME,
				asList(PROP_COMPLEX_A4_NAME, PROP_COMPLEX_A4_1_NAME));
		assertThat(hasMaxOneA4_1, is(true));

		boolean hasMaxOneA4_3 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_A_NAME,
				asList(PROP_COMPLEX_A4_NAME, PROP_COMPLEX_A4_3_NAME));
		assertThat(hasMaxOneA4_3, is(true));
	}

	@Test
	public void test_Complex_N() throws Exception {
		URL resource = getClass().getResource("data/sampleValues_complex_N.xml");
		GmlReferenceData gmlReferenceData = new GmlReferenceData(resource);

		boolean hasMaxOneB1 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_A_NAME,
				Collections.singletonList(PROP_COMPLEX_A4_NAME));
		assertThat(hasMaxOneB1, is(false));

		boolean hasMaxOneA4_1 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_A_NAME,
				asList(PROP_COMPLEX_A4_NAME, PROP_COMPLEX_A4_1_NAME));
		assertThat(hasMaxOneA4_1, is(false));

		boolean hasMaxOneA4_3 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_A_NAME,
				asList(PROP_COMPLEX_A4_NAME, PROP_COMPLEX_A4_3_NAME));
		assertThat(hasMaxOneA4_3, is(true));
	}

	@Test
	public void test_Reference_1() throws Exception {
		URL resource = getClass().getResource("data/sampleValues_reference_1.xml");
		GmlReferenceData gmlReferenceData = new GmlReferenceData(resource);

		boolean hasMaxOneB1 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_B_NAME,
				Collections.singletonList(PROP_B1_NAME));
		assertThat(hasMaxOneB1, is(false));

		boolean hasMaxOneB3 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_B_NAME,
				Collections.singletonList(PROP_B3_NAME));
		assertThat(hasMaxOneB3, is(false));

		boolean hasMaxOneA1 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_A_NAME,
				Collections.singletonList(PROP_A1_NAME));
		assertThat(hasMaxOneA1, is(false));

		boolean hasMaxOneA3 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_A_NAME,
				Collections.singletonList(PROP_A3_NAME));
		assertThat(hasMaxOneA3, is(false));
	}

	@Test
	public void test_Reference_N() throws Exception {
		URL resource = getClass().getResource("data/sampleValues_reference_N.xml");
		GmlReferenceData gmlReferenceData = new GmlReferenceData(resource);

		boolean hasMaxOneB1 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_B_NAME,
				Collections.singletonList(PROP_B1_NAME));
		assertThat(hasMaxOneB1, is(false));

		boolean hasMaxOneB3 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_B_NAME,
				Collections.singletonList(PROP_B3_NAME));
		assertThat(hasMaxOneB3, is(false));

		boolean hasMaxOneA1 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_A_NAME,
				Collections.singletonList(PROP_A1_NAME));
		assertThat(hasMaxOneA1, is(false));

		boolean hasMaxOneA3 = gmlReferenceData.hasZeroOrOneProperty(FEATURETYPE_A_NAME,
				Collections.singletonList(PROP_A3_NAME));
		assertThat(hasMaxOneA3, is(false));
	}

	@Test
	public void test_Inspire() throws Exception {
		URL resource = getClass().getResource("data/Inspire-Adress.xml");
		GmlReferenceData gmlReferenceData = new GmlReferenceData(resource);

		QName AdressFeatureTypeName = new QName("http://inspire.ec.europa.eu/schemas/ad/4.0", "Address", "ad");
		QName position = new QName("http://test.de/schema", "position", "te");

		List<QName> posSpec = new ArrayList<>();
		posSpec.add(position);
		posSpec.add(new QName("http://test.de/schema", "GeographicPosition", "te"));
		posSpec.add(new QName("http://test.de/schema", "specification", "te"));

		boolean hasMaxOnePosition = gmlReferenceData.hasZeroOrOneProperty(AdressFeatureTypeName,
				Collections.singletonList(position));
		assertThat(hasMaxOnePosition, is(true));

		boolean hasMaxOnePosSpec = gmlReferenceData.hasZeroOrOneProperty(AdressFeatureTypeName, posSpec);
		assertThat(hasMaxOnePosSpec, is(true));
	}

	@Test
	public void test_Inspire_hasProperty() throws Exception {
		URL resource = getClass().getResource("data/Inspire-Adress.xml");
		GmlReferenceData gmlReferenceData = new GmlReferenceData(resource);

		QName AdressFeatureTypeName = new QName("http://inspire.ec.europa.eu/schemas/ad/4.0", "Address", "ad");
		QName inspireId = new QName("http://inspire.ec.europa.eu/schemas/ad/4.0", "inspireId", "ad");

		List<QName> posSpec = new ArrayList<>();
		posSpec.add(inspireId);
		posSpec.add(new QName("http://inspire.ec.europa.eu/schemas/base/3.3", "Identifier", "ad"));
		posSpec.add(new QName("http://inspire.ec.europa.eu/schemas/base/3.3", "localId", "ad"));

		boolean hasProperty = gmlReferenceData.hasProperty(AdressFeatureTypeName, Collections.singletonList(inspireId));
		assertThat(hasProperty, is(true));

		boolean hasPropertyPosSpec = gmlReferenceData.hasZeroOrOneProperty(AdressFeatureTypeName, posSpec);
		assertThat(hasPropertyPosSpec, is(true));

		QName unknown = new QName("http://test.de/schema", "unknown", "te");
		boolean hasPropertyUnknown = gmlReferenceData.hasProperty(AdressFeatureTypeName,
				Collections.singletonList(unknown));
		assertThat(hasPropertyUnknown, is(false));
	}

	@Test
	public void test_Inspire_isPropertyNilled() throws Exception {
		URL resource = getClass().getResource("data/Inspire-Adress.xml");
		GmlReferenceData gmlReferenceData = new GmlReferenceData(resource);

		QName adressFeatureTypeName = new QName("http://inspire.ec.europa.eu/schemas/ad/4.0", "Address", "ad");
		QName inspireId = new QName("http://inspire.ec.europa.eu/schemas/ad/4.0", "inspireId", "ad");

		List<QName> versionId = new ArrayList<>();
		versionId.add(inspireId);
		versionId.add(new QName("http://inspire.ec.europa.eu/schemas/base/3.3", "Identifier", "ad"));
		versionId.add(new QName("http://inspire.ec.europa.eu/schemas/base/3.3", "versionId", "ad"));

		boolean propertyIsNilled = gmlReferenceData.isPropertyNilled(adressFeatureTypeName,
				Collections.singletonList(inspireId));
		assertThat(propertyIsNilled, is(false));

		boolean propertyIsNilledVersionId = gmlReferenceData.isPropertyNilled(adressFeatureTypeName, versionId);
		assertThat(propertyIsNilledVersionId, is(true));

		QName status = new QName("http://inspire.ec.europa.eu/schemas/ad/4.0", "status", "ad");
		boolean propertyIsNilledStatus = gmlReferenceData.isPropertyNilled(adressFeatureTypeName,
				Collections.singletonList(status));
		assertThat(propertyIsNilledStatus, is(true));
	}

}