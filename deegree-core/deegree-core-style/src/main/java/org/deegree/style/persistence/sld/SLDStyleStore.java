/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

 Occam Labs Schmitz & Schneider GbR
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.style.persistence.sld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deegree.style.persistence.StyleStore;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;

/**
 * SLD style store resource implementation.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class SLDStyleStore implements StyleStore {

	private final Map<String, LinkedList<Style>> styles;

	private HashMap<String, Style> stylesByName = new HashMap<String, Style>();

	private ResourceMetadata<StyleStore> metadata;

	public SLDStyleStore(Map<String, LinkedList<Style>> styles, ResourceMetadata<StyleStore> metadata) {
		this.styles = styles;
		this.metadata = metadata;
		for (List<Style> l : styles.values()) {
			for (Style s : l) {
				if (s.getName() != null) {
					stylesByName.put(s.getName(), s);
				}
			}
		}
	}

	@Override
	public void init() {
		// nothing to do
	}

	@Override
	public void destroy() {
		// nothing to do
	}

	@Override
	public Style getStyle(String styleName) {
		return stylesByName.get(styleName);
	}

	@Override
	public Style getStyle(String layerName, String styleName) {
		if (layerName == null) {
			return getStyle(styleName);
		}
		if (styleName == null) {
			styleName = "default";
		}
		List<Style> l = styles.get(layerName);
		if (l != null) {
			for (Style s : l) {
				if (s.getName() != null) {
					if (s.getName().equalsIgnoreCase(styleName)) {
						return s;
					}
				}
			}
		}
		return null;
	}

	@Override
	public List<Style> getAll(String layerName) {
		if (layerName == null) {
			return getAll();
		}
		return styles.get(layerName);
	}

	@Override
	public List<Style> getAll() {
		List<Style> list = new ArrayList<Style>();
		Collections.addAll(list, styles.values().toArray(new Style[0]));
		return list;
	}

	@Override
	public ResourceMetadata<? extends Resource> getMetadata() {
		return metadata;
	}

}
