/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

package org.deegree.protocol.wfs.lockfeature.kvp;

import static java.util.Collections.singletonList;
import static org.deegree.commons.tom.ows.Version.getVersionsString;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;

import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.filter.Filter;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.i18n.Messages;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.lockfeature.LockFeature;
import org.deegree.protocol.wfs.query.BBoxQuery;
import org.deegree.protocol.wfs.query.FeatureIdQuery;
import org.deegree.protocol.wfs.query.FilterQuery;
import org.deegree.protocol.wfs.query.Query;
import org.deegree.protocol.wfs.query.kvp.QueryKVPAdapter;

/**
 * Adapter between KVP <code>LockFeature</code> requests and {@link LockFeature} objects.
 * <p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class LockFeatureKVPAdapter extends QueryKVPAdapter {

	/**
	 * Parses a normalized KVP-map as a WFS {@link LockFeature} request.
	 * <p>
	 * Supported versions:
	 * <ul>
	 * <li>WFS 1.0.0</li>
	 * <li>WFS 1.1.0</li>
	 * <li>WFS 2.0.0</li>
	 * </ul>
	 * @param kvpParams normalized KVP-map; keys must be uppercase, each key only has one
	 * associated value
	 * @return parsed {@link LockFeature} request
	 * @throws Exception
	 */
	public static LockFeature parse(Map<String, String> kvpParams) throws Exception {

		Version version = Version.parseVersion(KVPUtils.getRequired(kvpParams, "VERSION"));

		LockFeature result = null;
		if (VERSION_100.equals(version)) {
			result = parse100(kvpParams);
		}
		else if (VERSION_110.equals(version)) {
			result = parse110(kvpParams);
		}
		else if (VERSION_200.equals(version)) {
			result = parse200(kvpParams);
		}
		else {
			String msg = Messages.get("UNSUPPORTED_VERSION", version,
					getVersionsString(VERSION_100, VERSION_110, VERSION_200));
			throw new InvalidParameterValueException(msg);
		}
		return result;
	}

	private static LockFeature parse100(Map<String, String> kvpParams) throws Exception {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("boxing")
	private static LockFeature parse110(Map<String, String> kvpParams) throws Exception {
		// optional: 'NAMESPACE'
		Map<String, String> nsBindings = extractNamespaceBindings110(kvpParams);

		NamespaceBindings nsContext = new NamespaceBindings();
		if (nsBindings != null) {
			for (String key : nsBindings.keySet()) {
				nsContext.addNamespace(key, nsBindings.get(key));
			}
		}

		// optional: EXPIRY
		String expiryStr = kvpParams.get("EXPIRY");
		BigInteger expiryInMinutes = null;
		if (expiryStr != null) {
			expiryInMinutes = new BigInteger(expiryStr);
		}
		BigInteger expiryInSeconds = convertToSeconds(expiryInMinutes);

		// optional: LOCKACTION
		Boolean lockAll = parseLockAction(kvpParams.get("LOCKACTION"));

		// mandatory: TYPENAME, but optional if FEATUREID is specified
		String typeStrList = kvpParams.get("TYPENAME");
		TypeName[] typeNames = getTypeNames(typeStrList, nsBindings);

		// optional: FEATUREID
		String featureIdStr = kvpParams.get("FEATUREID");
		String[] featureIds = null;
		if (featureIdStr != null) {
			featureIds = featureIdStr.split(",");
		}
		// optional: BBOX
		String bboxStr = kvpParams.get("BBOX");

		// optional: FILTER
		String filterStr = kvpParams.get("FILTER");

		if ((featureIdStr != null && bboxStr != null) || (featureIdStr != null && filterStr != null)
				|| (bboxStr != null && filterStr != null)) {
			// TODO make new exception
			throw new Exception("The FEATUREID, BBOX and FILTER keywords are mutually exclusive!");
		}

		if (featureIdStr != null) {
			Query query = new FeatureIdQuery(null, typeNames, null, null, null, null, featureIds);
			return new LockFeature(VERSION_110, null, singletonList(query), expiryInSeconds, lockAll, null);
		}

		if (bboxStr != null) {
			if (typeNames == null) {
				// TODO make new exception
				throw new Exception("The TYPENAME keyword is mandatory if BBOX is present!");
			}

			String[] coordList = bboxStr.split(",");
			ICRS srs = null; // TODO should this be EPSG:4326 or WGS:84 by default ??
			if (coordList.length % 2 == 1) {
				srs = CRSManager.getCRSRef(coordList[coordList.length - 1]);
			}

			Envelope bbox = createEnvelope(bboxStr, srs);
			Query bboxQuery = new BBoxQuery(null, typeNames, null, srs, null, null, bbox);
			return new LockFeature(VERSION_110, null, singletonList(bboxQuery), expiryInSeconds, lockAll, null);
		}

		if (filterStr != null || typeNames != null) {
			if (typeNames == null) {
				// TODO make new exception
				throw new Exception("The FILTER element requires the TYPENAME element");
			}

			int length = typeNames.length;
			String[] filters = getFilters(filterStr);
			List<Query> queries = new ArrayList<Query>(length);

			for (int i = 0; i < length; i++) {
				Filter filter = null;
				if (filters != null) {
					StringReader sr = new StringReader(filters[i]);
					XMLAdapter adapter = new XMLAdapter(sr);
					XMLStreamReaderWrapper streamWrapper = new XMLStreamReaderWrapper(
							adapter.getRootElement().getXMLStreamReaderWithoutCaching(), adapter.getSystemId());
					try {
						streamWrapper.nextTag();
						filter = Filter110XMLDecoder.parse(streamWrapper);
					}
					catch (XMLParsingException e) {
						e.printStackTrace();
						// TODO raise exception
					}
					catch (XMLStreamException e) {
						e.printStackTrace();
						// TODO raise exception
					}
				}
				queries.add(new FilterQuery(null, new TypeName[] { typeNames[i] }, null, null, null, null, filter));
			}
			return new LockFeature(VERSION_110, null, queries, expiryInSeconds, lockAll, null);

		}
		return null;
	}

	private static LockFeature parse200(Map<String, String> kvpParams) throws Exception {

		String handle = null;

		List<Query> queries = parseQueries200(kvpParams, null);

		// optional: EXPIRY
		String expiryStr = kvpParams.get("EXPIRY");
		BigInteger expiryInSeconds = null;
		if (expiryStr != null) {
			expiryInSeconds = new BigInteger(expiryStr);
		}

		// optional: LOCKACTION
		Boolean lockAll = parseLockAction(kvpParams.get("LOCKACTION"));

		// optional: LOCKID
		String existingLockId = kvpParams.get("LOCKID");

		return new LockFeature(VERSION_200, handle, queries, expiryInSeconds, lockAll, existingLockId);
	}

	private static BigInteger convertToSeconds(BigInteger expiryInMinutes) {
		if (expiryInMinutes == null) {
			return null;
		}
		return expiryInMinutes.multiply(BigInteger.valueOf(60));
	}

	private static Boolean parseLockAction(String lockActionString) {
		Boolean lockAll = null;
		if (lockActionString != null) {
			if ("SOME".equals(lockActionString)) {
				lockAll = false;
			}
			else if ("ALL".equals(lockActionString)) {
				lockAll = true;
			}
			else {
				String msg = "Invalid value (=" + lockActionString
						+ ") for lock action parameter. Valid values are 'ALL' or 'SOME'.";
				throw new InvalidParameterValueException(msg, "lockAction");
			}
		}
		return lockAll;
	}

}
