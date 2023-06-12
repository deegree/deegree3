/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.protocol.wms.client;

import static org.deegree.cs.coordinatesystems.GeographicCRS.WGS84;
import static org.deegree.protocol.i18n.Messages.get;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WMS111CapabilitiesAdapter} for documents that comply to the <a
 * href="http://www.opengeospatial.org/standards/wms>WMS 1.1.1</a> specification.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class WMS111CapabilitiesAdapter extends WMSCapabilitiesAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(WMS111CapabilitiesAdapter.class);

	/**
	 * Create a new {@link WMS111CapabilitiesAdapter} from the passed root element.
	 * @param root the capabilies doument, must not be <code>null</code> throws
	 * {@link IllegalArgumentException} if root is null
	 */
	public WMS111CapabilitiesAdapter(OMElement root) {
		if (root == null)
			throw new IllegalArgumentException("Capablities root element must not be null!");
		setRootElement(root);
	}

	@Override
	protected Envelope parseLatLonBoundingBox(OMElement elem) {
		double[] min = new double[2];
		double[] max = new double[2];

		while (elem.getLocalName().equals("Layer")) {
			OMElement bbox = getElement(elem, new XPath("LatLonBoundingBox", null));
			if (bbox != null) {
				try {
					min[0] = Double.parseDouble(bbox.getAttributeValue(new QName("minx")));
					min[1] = Double.parseDouble(bbox.getAttributeValue(new QName("miny")));
					max[0] = Double.parseDouble(bbox.getAttributeValue(new QName("maxx")));
					max[1] = Double.parseDouble(bbox.getAttributeValue(new QName("maxy")));
					return new GeometryFactory().createEnvelope(min, max, CRSManager.getCRSRef(WGS84));
				}
				catch (NumberFormatException nfe) {
					LOG.warn(get("WMSCLIENT.SERVER_INVALID_NUMERIC_VALUE", nfe.getLocalizedMessage()));
				}
			}
			else {
				elem = (OMElement) elem.getParent();
			}
		}
		return null;
	}

	@Override
	protected String getPrefix() {
		return "";
	}

	@Override
	protected String getLayerCRSElementName() {
		return "SRS";
	}

	@Override
	protected Version getServiceVersion() {
		return new Version(1, 1, 1);
	}

	@Override
	protected String getExtendedCapabilitiesRootXPath() {
		return "//WMT_MS_Capabilities/Capability/VendorSpecificCapabilities";
	}

}
