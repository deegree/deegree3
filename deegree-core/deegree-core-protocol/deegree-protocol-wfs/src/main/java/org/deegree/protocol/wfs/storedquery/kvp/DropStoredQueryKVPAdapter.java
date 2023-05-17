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

package org.deegree.protocol.wfs.storedquery.kvp;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;

import java.util.Map;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.protocol.i18n.Messages;
import org.deegree.protocol.wfs.AbstractWFSRequestKVPAdapter;
import org.deegree.protocol.wfs.storedquery.DropStoredQuery;

/**
 * Adapter between KVP <code>DropStoredQuery</code> requests and {@link DropStoredQuery}
 * objects.
 * <p>
 * Supported versions:
 * <ul>
 * <li>WFS 2.0.0</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class DropStoredQueryKVPAdapter extends AbstractWFSRequestKVPAdapter {

	/**
	 * Parses a normalized KVP-map as a WFS {@link DropStoredQuery} request.
	 * @param kvpParams normalized KVP-map; keys must be uppercase, each key only has one
	 * associated value
	 * @return parsed {@link DropStoredQuery} request
	 * @throws MissingParameterException if the request version is unsupported
	 * @throws InvalidParameterValueException if a parameter contains a syntax error
	 */
	public static DropStoredQuery parse(Map<String, String> kvpParams)
			throws MissingParameterException, InvalidParameterValueException {

		Version version = Version.parseVersion(KVPUtils.getRequired(kvpParams, "VERSION"));
		if (!(VERSION_200.equals(version))) {
			String msg = Messages.get("UNSUPPORTED_VERSION", version, Version.getVersionsString(VERSION_200));
			throw new InvalidParameterValueException(msg);
		}

		String id = KVPUtils.getRequired(kvpParams, "STOREDQUERY_ID");
		return new DropStoredQuery(version, null, id);
	}

}
