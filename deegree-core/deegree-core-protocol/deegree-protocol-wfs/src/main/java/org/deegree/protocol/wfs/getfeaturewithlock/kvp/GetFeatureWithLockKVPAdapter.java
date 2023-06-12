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

package org.deegree.protocol.wfs.getfeaturewithlock.kvp;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.deegree.commons.tom.ResolveParams;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.protocol.i18n.Messages;
import org.deegree.protocol.wfs.AbstractWFSRequestKVPAdapter;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.kvp.GetFeatureKVPAdapter;
import org.deegree.protocol.wfs.getfeaturewithlock.GetFeatureWithLock;
import org.deegree.protocol.wfs.query.Query;
import org.deegree.protocol.wfs.query.StandardPresentationParams;

/**
 * Adapter between KVP <code>GetFeatureWithLock</code> requests and
 * {@link GetFeatureWithLock} objects.
 * <p>
 * Supported versions:
 * <ul>
 * <li>WFS 1.0.0</li>
 * <li>WFS 1.1.0</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:schneider@lat-lon.de">Andrei Ionita</a>
 */
public class GetFeatureWithLockKVPAdapter extends AbstractWFSRequestKVPAdapter {

	/**
	 * Parses a normalized KVP-map as a WFS {@link GetFeatureWithLock} request.
	 * @param kvpParams normalized KVP-map; keys must be uppercase, each key only has one
	 * associated value
	 * @return parsed {@link GetFeatureWithLock} request
	 * @throws Exception
	 */
	public static GetFeatureWithLock parse(Map<String, String> kvpParams) throws Exception {

		Version version = Version.parseVersion(KVPUtils.getRequired(kvpParams, "VERSION"));

		GetFeatureWithLock result = null;
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
			String msg = Messages.get("UNSUPPORTED_VERSION", version, Version.getVersionsString(VERSION_110));
			throw new InvalidParameterValueException(msg);
		}
		return result;
	}

	private static GetFeatureWithLock parse100(Map<String, String> kvpParams) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("boxing")
	private static GetFeatureWithLock parse110(Map<String, String> kvpParams) throws Exception {

		GetFeature gf = GetFeatureKVPAdapter.parse(kvpParams, null);

		// optional: EXPIRY
		String expiryStr = kvpParams.get("EXPIRY");
		BigInteger expiryInMinutes = null;
		if (expiryStr != null) {
			expiryInMinutes = new BigInteger(expiryStr);
		}
		BigInteger expiryInSeconds = convertToSeconds(expiryInMinutes);

		StandardPresentationParams presentationParams = gf.getPresentationParams();
		ResolveParams resolveParams = gf.getResolveParams();
		List<Query> queries = gf.getQueries();
		return new GetFeatureWithLock(VERSION_110, null, presentationParams, resolveParams, queries, expiryInSeconds,
				null);
	}

	@SuppressWarnings("boxing")
	private static GetFeatureWithLock parse200(Map<String, String> kvpParams) throws Exception {

		GetFeature gf = GetFeatureKVPAdapter.parse(kvpParams, null);

		// optional LOCKACTION
		Boolean lockAll = parseLockAction(kvpParams.get("LOCKACTION"));

		// optional: EXPIRY
		String expiryStr = kvpParams.get("EXPIRY");
		BigInteger expiryInSeconds = null;
		if (expiryStr != null) {
			expiryInSeconds = new BigInteger(expiryStr);
		}

		StandardPresentationParams presentationParams = gf.getPresentationParams();
		ResolveParams resolveParams = gf.getResolveParams();
		List<Query> queries = gf.getQueries();
		return new GetFeatureWithLock(VERSION_200, null, presentationParams, resolveParams, queries, expiryInSeconds,
				lockAll);
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
