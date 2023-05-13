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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class RequestedLayerVisibilityInspector {

	private final InspectorMap inspectorMap;

	/**
	 * @param visibilityInspectorTypes the configuration of the visibility inspector,
	 * never <code>null</code>
	 * @param workspace the currently active workspace, never <code>null</code>
	 */
	public RequestedLayerVisibilityInspector(List<VisibilityInspectorType> visibilityInspectorTypes,
			Workspace workspace) {
		inspectorMap = initInspectorMap(workspace, visibilityInspectorTypes);
	}

	/**
	 * Checks if the layer with the passed metadata is visible or not.
	 * @param requestedLayerName the name of the requested layer, never <code>null</code>
	 * @param layerMetadata the layer metadata to check if visible or not
	 * @return <code>true</code> if the layer is visible, <code>false</code> otherwise
	 */
	public boolean isVisible(String requestedLayerName, LayerMetadata layerMetadata) {
		List<LayerVisibilityInspector> inspectors = inspectorMap.getInspector(requestedLayerName);
		for (LayerVisibilityInspector inspector : inspectors) {
			if (!inspector.isVisible(layerMetadata))
				return false;
		}

		return true;
	}

	private InspectorMap initInspectorMap(Workspace workspace, List<VisibilityInspectorType> visibilityInspectorTypes) {
		InspectorMap inspectorMap = new InspectorMap();
		if (visibilityInspectorTypes != null && !visibilityInspectorTypes.isEmpty()) {

			for (VisibilityInspectorType visibilityInspectorType : visibilityInspectorTypes) {
				LayerVisibilityInspector inspector = instantiateClass(workspace, visibilityInspectorType);
				List<String> categoryLayerIdentifiers = visibilityInspectorType.getCategoryLayerIdentifier();
				if (categoryLayerIdentifiers.isEmpty())
					inspectorMap.addInspectorForAllLayers(inspector);
				else
					inspectorMap.addInspectorPerLayer(inspector, categoryLayerIdentifiers);
			}
		}
		return inspectorMap;
	}

	private LayerVisibilityInspector instantiateClass(Workspace workspace,
			VisibilityInspectorType visibilityInspectorType) {
		String javaClass = visibilityInspectorType.getJavaClass();
		try {
			Class<?> clazz = workspace.getModuleClassLoader().loadClass(javaClass);
			return clazz.asSubclass(LayerVisibilityInspector.class).newInstance();
		}
		catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Couldn't find LayerVisibilityInspector class", e);
		}
		catch (ClassCastException e) {
			throw new IllegalArgumentException("Configured serializer class doesn't implement LayerVisibilityInspector",
					e);
		}
		catch (InstantiationException e) {
			throw new IllegalArgumentException("Could not instantiate " + javaClass, e);
		}
		catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Could not instantiate " + javaClass, e);
		}
	}

	private class InspectorMap {

		private final Map<String, List<LayerVisibilityInspector>> inspectorPerLayer = new HashMap<String, List<LayerVisibilityInspector>>();

		private final List<LayerVisibilityInspector> inspectorForAllLayers = new ArrayList<LayerVisibilityInspector>();

		List<LayerVisibilityInspector> getInspector(String layerName) {
			List<LayerVisibilityInspector> inspector = new ArrayList<LayerVisibilityInspector>();
			if (inspectorPerLayer.containsKey(layerName))
				inspector.addAll(inspectorPerLayer.get(layerName));
			inspector.addAll(inspectorForAllLayers);
			return inspector;
		}

		void addInspectorForAllLayers(LayerVisibilityInspector inspector) {
			inspectorForAllLayers.add(inspector);
		}

		void addInspectorPerLayer(LayerVisibilityInspector inspector, List<String> categoryLayerIdentifiers) {
			for (String categoryLayerIdentifier : categoryLayerIdentifiers) {
				if (!inspectorPerLayer.containsKey(categoryLayerIdentifier))
					inspectorPerLayer.put(categoryLayerIdentifier, new ArrayList<LayerVisibilityInspector>());
				inspectorPerLayer.get(categoryLayerIdentifier).add(inspector);
			}
		}

	}

}