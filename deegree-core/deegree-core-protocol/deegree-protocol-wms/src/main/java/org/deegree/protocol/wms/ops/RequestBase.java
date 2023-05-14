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
package org.deegree.protocol.wms.ops;

import static org.deegree.commons.utils.CollectionUtils.unzip;
import static org.deegree.commons.utils.MapUtils.DEFAULT_PIXEL_SIZE;
import static org.deegree.commons.utils.StringUtils.splitEscaped;
import static org.deegree.protocol.wms.ops.SLDParser.parse;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.StringReader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.utils.StringUtils;
import org.deegree.commons.utils.Triple;
import org.deegree.filter.OperatorFilter;
import org.deegree.layer.LayerRef;
import org.deegree.style.StyleRef;
import org.slf4j.Logger;

/**
 * @author <a href="mailto:wanhoff@lat-lon.de">Jeronimo Wanhoff</a>
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public abstract class RequestBase {

	private static final Logger LOG = getLogger(RequestBase.class);

	protected LinkedList<OperatorFilter> filters = null;

	protected LinkedList<LayerRef> layers = new LinkedList<LayerRef>();

	protected LinkedList<StyleRef> styles = new LinkedList<StyleRef>();

	protected HashMap<String, List<?>> dimensions = new HashMap<String, List<?>>();

	protected double pixelSize = DEFAULT_PIXEL_SIZE;

	public abstract double getScale();

	public abstract List<LayerRef> getLayers();

	public List<OperatorFilter> getFilters() {
		return filters;
	}

	protected void handleSLD(String sld, String sldBody) throws OWSException {

		XMLInputFactory xmlfac = XMLInputFactory.newInstance();
		Triple<LinkedList<LayerRef>, LinkedList<StyleRef>, LinkedList<OperatorFilter>> triple = null;
		if (sld != null) {
			try {
				triple = parse(xmlfac.createXMLStreamReader(sld, new URL(sld).openStream()), this);
			}
			catch (ParseException e) {
				LOG.trace("Stack trace:", e);
				throw new OWSException(
						"The embedded dimension value in the SLD parameter value was invalid: " + e.getMessage(),
						"InvalidDimensionValue", "sld");
			}
			catch (Throwable e) {
				LOG.trace("Stack trace:", e);
				throw new OWSException("Error when parsing the SLD parameter: " + e.getMessage(),
						"InvalidParameterValue", "sld");
			}
		}
		if (sldBody != null) {
			try {
				triple = parse(xmlfac.createXMLStreamReader(new StringReader(sldBody)), this);
			}
			catch (ParseException e) {
				LOG.trace("Stack trace:", e);
				throw new OWSException(
						"The embedded dimension value in the SLD_BODY parameter value was invalid: " + e.getMessage(),
						"InvalidDimensionValue", "sld_body");
			}
			catch (Throwable e) {
				LOG.trace("Stack trace:", e);
				throw new OWSException("Error when parsing the SLD_BODY parameter: " + e.getMessage(),
						"InvalidParameterValue", "sld_body");
			}
		}

		// if layers are referenced, clear the other layers out, else leave all in
		if (triple != null && !layers.isEmpty()) {
			// it might be in SLD that a layer has multiple styles, so we need to map to a
			// list here
			HashMap<String, LinkedList<Triple<LayerRef, StyleRef, OperatorFilter>>> lays = new HashMap<String, LinkedList<Triple<LayerRef, StyleRef, OperatorFilter>>>();

			ListIterator<LayerRef> it = triple.first.listIterator();
			ListIterator<StyleRef> st = triple.second.listIterator();
			ListIterator<OperatorFilter> ft = triple.third.listIterator();
			while (it.hasNext()) {
				LayerRef lRef = it.next();
				StyleRef sRef = st.next();
				OperatorFilter f = ft.next();
				if (!layers.contains(lRef)) {
					it.remove();
					st.remove();
					ft.remove();
				}
				else {
					String name = lRef.getName();
					LinkedList<Triple<LayerRef, StyleRef, OperatorFilter>> list = lays.get(name);
					if (list == null) {
						list = new LinkedList<Triple<LayerRef, StyleRef, OperatorFilter>>();
						lays.put(name, list);
					}

					if (sRef.getName() == null || sRef.getName().isEmpty()) {
						sRef = new StyleRef("default");
					}

					list.add(new Triple<LayerRef, StyleRef, OperatorFilter>(lRef, sRef, f));
				}
			}

			this.styles.clear();
			this.filters = new LinkedList<OperatorFilter>();

			LinkedList<LayerRef> tmpLayers = new LinkedList<LayerRef>();

			// to get the order right, in case it's different from the SLD order
			for (LayerRef lRef : layers) {
				LinkedList<Triple<LayerRef, StyleRef, OperatorFilter>> l = lays.get(lRef.getName());
				if (l == null) {
					throw new OWSException("The SLD NamedLayer " + lRef.getName() + " is invalid.",
							"InvalidParameterValue", "layers");
				}

				Triple<ArrayList<LayerRef>, ArrayList<StyleRef>, ArrayList<OperatorFilter>> t = unzip(l);
				tmpLayers.addAll(t.first);
				this.styles.addAll(t.second);
				this.filters.addAll(t.third);
			}
			this.layers.clear();
			this.layers.addAll(tmpLayers);
		}
		else {
			if (triple != null) {
				this.layers = triple.first;
				this.styles = triple.second;
				this.filters = triple.third;
			}
		}
	}

	/**
	 * @return returns a map with the requested dimension values
	 */
	public HashMap<String, List<?>> getDimensions() {
		return dimensions;
	}

	/**
	 * @param name
	 * @param values
	 */
	public void addDimensionValue(String name, List<?> values) {
		dimensions.put(name, values);
	}

	protected void handlePixelSize(Map<String, String> map) {
		String psize = map.get("PIXELSIZE");
		if (psize != null) {
			try {
				pixelSize = Double.parseDouble(psize) / 1000;
			}
			catch (NumberFormatException e) {
				LOG.warn("The value of PIXELSIZE could not be parsed as a number.");
				LOG.trace("Stack trace:", e);
			}
		}
		else {
			String key = "RES";
			String pdpi = map.get(key);

			if (pdpi == null) {
				key = "DPI";
				pdpi = map.get(key);
			}
			if (pdpi == null) {
				key = "MAP_RESOLUTION";
				pdpi = map.get(key);
			}
			if (pdpi == null) {
				for (String word : splitEscaped(map.get("FORMAT_OPTIONS"), ';', 0)) {
					List<String> keyValue = StringUtils.splitEscaped(word, ':', 2);

					if ("dpi".equalsIgnoreCase(keyValue.get(0))) {
						key = "FORMAT_OPTIONS=dpi";
						pdpi = keyValue.size() == 1 ? null : StringUtils.unescape(keyValue.get(1));
						break;
					}
				}
			}
			if (pdpi == null) {
				key = "X-DPI";
				pdpi = map.get(key);
			}
			if (pdpi != null) {
				try {
					pixelSize = 0.0254d / Double.parseDouble(pdpi);
				}
				catch (Exception e) {
					LOG.warn("The value of {} could not be parsed as a number.", key);
					LOG.trace("Stack trace:", e);
				}
			}
		}
	}

}
