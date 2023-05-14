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
package org.deegree.geometry.metadata;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.SimpleGeometryFactory;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class SpatialMetadataTest {

	private static final SimpleGeometryFactory GEOM_FACTORY = new SimpleGeometryFactory();

	@Test
	public void testMerge_Null() throws Exception {
		SpatialMetadata spatialMetadata = new SpatialMetadata(firstEnvelope(), firstCrsList());

		SpatialMetadata merged = spatialMetadata.merge(null);

		assertThat(merged.getEnvelope(), isEnvelope(firstEnvelope()));
		assertThat(merged.getCoordinateSystems(), is(firstCrsList()));
	}

	@Test
	public void testMerge_EnvelopeNullInSource() throws Exception {
		SpatialMetadata spatialMetadata = new SpatialMetadata(null, firstCrsList());

		SpatialMetadata merged = spatialMetadata.merge(new SpatialMetadata(firstEnvelope(), firstCrsList()));

		assertThat(merged.getEnvelope(), isEnvelope(firstEnvelope()));
		assertThat(merged.getCoordinateSystems(), is(firstCrsList()));
	}

	@Test
	public void testMerge_CrsNullInSource() throws Exception {
		SpatialMetadata spatialMetadata = new SpatialMetadata(firstEnvelope(), null);

		SpatialMetadata merged = spatialMetadata.merge(new SpatialMetadata(firstEnvelope(), firstCrsList()));

		assertThat(merged.getEnvelope(), isEnvelope(firstEnvelope()));
		assertThat(merged.getCoordinateSystems(), is(firstCrsList()));
	}

	@Test
	public void testMerge_EnvelopesDifferent() throws Exception {
		SpatialMetadata spatialMetadata = new SpatialMetadata(firstEnvelope(), firstCrsList());

		SpatialMetadata merged = spatialMetadata.merge(new SpatialMetadata(secondEnvelope(), firstCrsList()));

		assertThat(merged.getEnvelope(), isEnvelope(firstEnvelope().merge(secondEnvelope())));
		assertThat(merged.getCoordinateSystems(), is(firstCrsList()));
	}

	@Test
	public void testMerge_CrsListDifferent() throws Exception {
		SpatialMetadata spatialMetadata = new SpatialMetadata(firstEnvelope(), firstCrsList());

		SpatialMetadata merged = spatialMetadata.merge(new SpatialMetadata(firstEnvelope(), secondCrsList()));

		assertThat(merged.getEnvelope(), isEnvelope(firstEnvelope()));
		assertThat(merged.getCoordinateSystems(), hasItems(firstCrsList().toArray(new ICRS[] {})));
		assertThat(merged.getCoordinateSystems(), hasItems(secondCrsList().toArray(new ICRS[] {})));
	}

	@Test
	public void testMerge_CheckMergedAreUntouched() throws Exception {
		SpatialMetadata spatialMetadata = new SpatialMetadata(firstEnvelope(), firstCrsList());
		SpatialMetadata spatialMetadataToMerge = new SpatialMetadata(secondEnvelope(), secondCrsList());
		SpatialMetadata merged = spatialMetadata.merge(spatialMetadataToMerge);

		assertThat(spatialMetadata.getEnvelope(), isEnvelope(firstEnvelope()));
		assertThat(spatialMetadata.getCoordinateSystems(), is(firstCrsList()));

		assertThat(spatialMetadataToMerge.getEnvelope(), isEnvelope(secondEnvelope()));
		assertThat(spatialMetadataToMerge.getCoordinateSystems(), is(secondCrsList()));

		assertThat(merged.getEnvelope(), isEnvelope(firstEnvelope().merge(secondEnvelope())));
		assertThat(merged.getCoordinateSystems(), hasItems(firstCrsList().toArray(new ICRS[] {})));
		assertThat(merged.getCoordinateSystems(), hasItems(secondCrsList().toArray(new ICRS[] {})));
	}

	private Envelope firstEnvelope() throws UnknownCRSException {
		return GEOM_FACTORY.createEnvelope(10, 53, 11, 54, CRSManager.lookup("EPSG:4326"));
	}

	private Envelope secondEnvelope() throws UnknownCRSException {
		return GEOM_FACTORY.createEnvelope(10, 52, 11, 54, CRSManager.lookup("EPSG:4326"));
	}

	private List<ICRS> firstCrsList() throws UnknownCRSException {
		return asCrsList("EPSG:4326", "EPSG:25833");
	}

	private List<ICRS> secondCrsList() throws UnknownCRSException {
		return asCrsList("EPSG:900913");
	}

	private List<ICRS> asCrsList(String... crsNames) throws UnknownCRSException {
		List<ICRS> crs = new ArrayList<ICRS>();
		for (String crsName : crsNames) {
			crs.add(CRSManager.lookup(crsName));
		}
		return crs;
	}

	private Matcher<Envelope> isEnvelope(final Envelope envelope) {
		return new BaseMatcher<Envelope>() {
			@Override
			public boolean matches(Object item) {
				Envelope itemEnvelope = (Envelope) item;
				return itemEnvelope.equals(envelope);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("Expected: " + envelope);
			}
		};
	}

}