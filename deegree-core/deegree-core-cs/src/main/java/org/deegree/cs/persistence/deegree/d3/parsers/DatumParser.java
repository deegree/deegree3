/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.cs.persistence.deegree.d3.parsers;

import static org.deegree.commons.xml.stax.XMLStreamUtils.moveReaderToFirstMatch;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.cs.persistence.deegree.d3.DeegreeCRSStore.CRS_NS;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.cs.CRSResource;
import org.deegree.cs.components.GeodeticDatum;
import org.deegree.cs.components.IEllipsoid;
import org.deegree.cs.components.IPrimeMeridian;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.deegree.cs.i18n.Messages;
import org.deegree.cs.persistence.AbstractCRSStore.RESOURCETYPE;
import org.deegree.cs.persistence.deegree.d3.DeegreeCRSStore;
import org.deegree.cs.refs.components.EllipsoidRef;
import org.deegree.cs.refs.components.PrimeMeridianRef;
import org.slf4j.Logger;

/**
 * Stax-based configuration parser for datum objects.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 */
public class DatumParser extends DefinitionParser {

	private static final Logger LOG = getLogger(DatumParser.class);

	private static final QName DATUM_ELEM = new QName(CRS_NS, "GeodeticDatum");

	private static final QName ROOT = new QName(CRS_NS, "DatumDefinitions");

	/**
	 * @param provider
	 * @param configURL
	 */
	public DatumParser(DeegreeCRSStore provider, URL configURL) {
		super(provider, configURL);
	}

	/**
	 * @param datumID
	 * @return the
	 * @throws CRSConfigurationException
	 */
	public GeodeticDatum getGeodeticDatumForId(String datumID) throws CRSConfigurationException {
		if (datumID == null || "".equals(datumID.trim())) {
			return null;
		}
		String tmpDatumID = datumID.trim();
		GeodeticDatum result = getStore().getCachedIdentifiable(GeodeticDatum.class, tmpDatumID);
		if (result == null) {
			try {
				result = parseDatum(getConfigReader());
				while (result != null && !result.hasId(tmpDatumID, false, true)) {
					result = parseDatum(getConfigReader());
				}

			}
			catch (XMLStreamException e) {
				throw new CRSConfigurationException(e);
			}
		}
		return result;
	}

	/**
	 * @param reader to
	 * @return the next datum on the stream.
	 * @throws XMLStreamException
	 */
	protected GeodeticDatum parseDatum(XMLStreamReader reader) throws XMLStreamException {
		if (reader == null || !moveReaderToFirstMatch(reader, DATUM_ELEM)) {
			LOG.debug("Could not get datum, no more definitions left.");
			return null;
		}

		// get the identifiable.
		CRSResource id = parseIdentifiable(reader);

		// get the ellipsoid.
		String ellipsID = XMLStreamUtils.getRequiredText(reader, new QName(CRS_NS, "UsedEllipsoid"), true);
		if (ellipsID == null || ellipsID.trim().length() == 0) {
			throw new CRSConfigurationException(
					Messages.getMessage("CRS_STAX_CONFIG_DATUM_HAS_NO_ELLIPSOID", reader.getLocation()));
		}
		IEllipsoid ellipsoid = new EllipsoidRef(store.getResolver(RESOURCETYPE.ELLIPSOID), '#' + ellipsID, null);

		// get the primemeridian if any.
		String pMeridianID = XMLStreamUtils.getText(getConfigReader(), new QName(CRS_NS, "UsedPrimeMeridian"), null,
				true);
		IPrimeMeridian pMeridian = null;
		if (pMeridianID != null && pMeridianID.trim().length() > 0) {
			pMeridian = new PrimeMeridianRef(store.getResolver(RESOURCETYPE.PM), '#' + pMeridianID, null);
		}
		nextElement(reader);// end document

		return getStore().addIdToCache(new GeodeticDatum(ellipsoid, pMeridian, id), false);
	}

	@Override
	protected QName expectedRootName() {
		return ROOT;
	}

	public GeodeticDatum getObject(String uri, String baseURL) {
		return getGeodeticDatumForId(uri);
	}

}
