/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2022 by:
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
package org.deegree.services.wms.visibility;

import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.services.jaxb.wms.VisibilityInspectorType;
import org.deegree.workspace.Workspace;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class RequestedLayerVisibilityInspectorTest {

	@Test
	public void testIsVisible_InspectorForOneCategoryLayerIdentifier() throws Exception {
		List<VisibilityInspectorType> visibilityInspectorTypes = createConfigurationWithOneCategoryLayerIdentifier(
				"ReqestedLayerToCheck");
		Workspace workspace = mockWorkspace();
		RequestedLayerVisibilityInspector inspector = new RequestedLayerVisibilityInspector(visibilityInspectorTypes,
				workspace);

		assertTrue(inspector.isVisible("ReqestedLayerToCheck", createLayerMetadata("IAmVisible")));
		assertFalse(inspector.isVisible("ReqestedLayerToCheck", createLayerMetadata("NotVisible")));
		assertTrue(inspector.isVisible("ReqestedLayer", createLayerMetadata("IAmVisible")));
		assertTrue(inspector.isVisible("ReqestedLayer", createLayerMetadata("NotVisible")));
	}

	@Test
	public void testIsVisible_InspectorForAllRequestedLayer() throws Exception {
		List<VisibilityInspectorType> visibilityInspectorTypes = createConfigurationWithInspectorForAll();
		Workspace workspace = mockWorkspace();
		RequestedLayerVisibilityInspector inspector = new RequestedLayerVisibilityInspector(visibilityInspectorTypes,
				workspace);

		assertTrue(inspector.isVisible("ReqestedLayerToCheck", createLayerMetadata("IAmVisible")));
		assertFalse(inspector.isVisible("ReqestedLayerToCheck", createLayerMetadata("NotVisible")));
		assertTrue(inspector.isVisible("ReqestedLayer", createLayerMetadata("IAmVisible")));
		assertFalse(inspector.isVisible("ReqestedLayer", createLayerMetadata("NotVisible")));
	}

	@Test
	public void testIsVisible_TwoInspectorsOneForAllRequestedLayerOneForTwoCategoryLayerIdentifier() throws Exception {
		List<VisibilityInspectorType> visibilityInspectorTypes = createConfigurationWithMultipleInspectors(
				"ReqestedLayerToCheck");
		Workspace workspace = mockWorkspace();
		RequestedLayerVisibilityInspector inspector = new RequestedLayerVisibilityInspector(visibilityInspectorTypes,
				workspace);

		assertTrue(inspector.isVisible("ReqestedLayerToCheck", createLayerMetadata("IAmVisible")));
		assertFalse(inspector.isVisible("ReqestedLayerToCheck", createLayerMetadata("NotVisible")));
		assertTrue(inspector.isVisible("ReqestedLayer", createLayerMetadata("IAmVisible")));
		assertFalse(inspector.isVisible("ReqestedLayer", createLayerMetadata("NotVisible")));
	}

	private LayerMetadata createLayerMetadata(String layerName) {
		return new LayerMetadata(layerName, null, null);
	}

	private List<VisibilityInspectorType> createConfigurationWithOneCategoryLayerIdentifier(
			String... categoryLayerIdentifier) {
		return createInspectors(categoryLayerIdentifier);
	}

	private List<VisibilityInspectorType> createConfigurationWithInspectorForAll() {
		return createInspectors();
	}

	private List<VisibilityInspectorType> createConfigurationWithMultipleInspectors(String... categoryLayerIdentifier) {
		List<VisibilityInspectorType> inspectorForAll = createInspectors();
		List<VisibilityInspectorType> inspector = createInspectors(categoryLayerIdentifier);
		inspectorForAll.addAll(inspector);
		return inspectorForAll;
	}

	private List<VisibilityInspectorType> createInspectors(String... categoryLayerIdentifier) {
		List<VisibilityInspectorType> inspectorTypes = new ArrayList<VisibilityInspectorType>();
		VisibilityInspectorType inspectorType = new VisibilityInspectorType();
		inspectorType.setJavaClass(LayerVisibilityInspectorTestImpl.class.getCanonicalName());
		if (categoryLayerIdentifier != null && categoryLayerIdentifier.length > 0)
			inspectorType.getCategoryLayerIdentifier().addAll(Arrays.asList(categoryLayerIdentifier));
		inspectorTypes.add(inspectorType);
		return inspectorTypes;
	}

	private Workspace mockWorkspace() {
		Workspace workspace = mock(Workspace.class);
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		when(workspace.getModuleClassLoader()).thenReturn(classLoader);
		return workspace;
	}

}