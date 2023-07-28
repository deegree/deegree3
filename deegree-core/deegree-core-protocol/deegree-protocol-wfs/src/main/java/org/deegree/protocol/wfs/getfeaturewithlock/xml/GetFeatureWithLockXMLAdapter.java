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

package org.deegree.protocol.wfs.getfeaturewithlock.xml;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;

import java.math.BigInteger;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ResolveParams;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.i18n.Messages;
import org.deegree.protocol.wfs.AbstractWFSRequestXMLAdapter;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.xml.GetFeatureXMLAdapter;
import org.deegree.protocol.wfs.getfeaturewithlock.GetFeatureWithLock;
import org.deegree.protocol.wfs.query.Query;
import org.deegree.protocol.wfs.query.StandardPresentationParams;

/**
 * Adapter between XML <code>GetFeatureWithLock</code> requests and
 * {@link GetFeatureWithLock} objects.
 * <p>
 * Supported versions:
 * <ul>
 * <li>WFS 1.0.0</li>
 * <li>WFS 1.1.0</li>
 * <li>WFS 2.0.0</li>
 * </ul>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class GetFeatureWithLockXMLAdapter extends AbstractWFSRequestXMLAdapter {

	/**
	 * Parses a WFS <code>GetFeatureWithLock</code> document into a
	 * {@link GetFeatureWithLock} object.
	 * @return parsed {@link GetFeatureWithLock} request
	 * @throws Exception
	 * @throws XMLParsingException if a syntax error occurs in the XML
	 * @throws MissingParameterException if the request version is unsupported
	 * @throws InvalidParameterValueException if a parameter contains a syntax error
	 */
	public GetFeatureWithLock parse() throws Exception {

		Version version = determineVersion110Safe();

		GetFeatureWithLock result = null;
		if (VERSION_100.equals(version)) {
			result = parse100();
		}
		else if (VERSION_110.equals(version)) {
			result = parse110();
		}
		else if (VERSION_200.equals(version)) {
			result = parse200();
		}
		else {
			throw new Exception("Version " + version
					+ " is not supported for parsing (for now). Only 1.1.0 and 1.0.0 are supported.");
		}

		return result;
	}

	public GetFeatureWithLock parse100() {

		GetFeatureXMLAdapter getFeatureXMLAdapter = new GetFeatureXMLAdapter();
		getFeatureXMLAdapter.setRootElement(rootElement);
		GetFeature gf = getFeatureXMLAdapter.parse100();

		BigInteger expiryInMinutes = getNodeAsBigInt(rootElement, new XPath("@expiry", nsContext), null);
		BigInteger expiryInSeconds = convertToSeconds(expiryInMinutes);

		StandardPresentationParams presentationParams = gf.getPresentationParams();
		ResolveParams resolveParams = gf.getResolveParams();
		List<Query> queries = gf.getQueries();
		return new GetFeatureWithLock(VERSION_100, null, presentationParams, resolveParams, queries, expiryInSeconds,
				null);
	}

	/**
	 * Parses a WFS 1.1.0 <code>GetFeatureWithLock</code> document into a
	 * {@link GetFeatureWithLock} object.
	 * @return a GetFeatureWithLock instance
	 */
	public GetFeatureWithLock parse110() {

		GetFeatureXMLAdapter getFeatureXMLAdapter = new GetFeatureXMLAdapter();
		getFeatureXMLAdapter.setRootElement(rootElement);
		GetFeature gf = getFeatureXMLAdapter.parse110();

		BigInteger expiryInMinutes = getNodeAsBigInt(rootElement, new XPath("@expiry", nsContext), null);
		BigInteger expiryInSeconds = convertToSeconds(expiryInMinutes);

		StandardPresentationParams presentationParams = gf.getPresentationParams();
		ResolveParams resolveParams = gf.getResolveParams();
		List<Query> queries = gf.getQueries();
		return new GetFeatureWithLock(VERSION_110, null, presentationParams, resolveParams, queries, expiryInSeconds,
				null);
	}

	public GetFeatureWithLock parse200() throws OWSException {

		GetFeatureXMLAdapter getFeatureXMLAdapter = new GetFeatureXMLAdapter();
		getFeatureXMLAdapter.setRootElement(rootElement);
		GetFeature gf = getFeatureXMLAdapter.parse200();

		// <xsd:attribute name="expiry" type="xsd:positiveInteger" default="300"/>
		BigInteger expiryInSeconds = getNodeAsBigInt(rootElement, new XPath("@expiry", nsContext), null);

		// <xsd:attribute name="lockAction" type="wfs:AllSomeType" default="ALL"/>
		String lockActionStr = rootElement.getAttributeValue(new QName("lockAction"));
		Boolean lockAll = parseLockAction(lockActionStr);

		String handle = gf.getHandle();
		StandardPresentationParams presentationParams = gf.getPresentationParams();
		ResolveParams resolveParams = gf.getResolveParams();
		List<Query> queries = gf.getQueries();
		return new GetFeatureWithLock(VERSION_200, handle, presentationParams, resolveParams, queries, expiryInSeconds,
				lockAll);
	}

	private BigInteger convertToSeconds(BigInteger expiryInMinutes) {
		if (expiryInMinutes == null) {
			return null;
		}
		return expiryInMinutes.multiply(BigInteger.valueOf(60));
	}

	private Boolean parseLockAction(String lockActionStr) {
		Boolean lockAll = null;
		if (lockActionStr != null) {
			if ("ALL".equals(lockActionStr)) {
				lockAll = true;
			}
			else if ("SOME".equals(lockActionStr)) {
				lockAll = false;
			}
			else {
				String msg = Messages.get("WFS_UNKNOWN_LOCK_ACTION", lockActionStr);
				throw new XMLParsingException(this, rootElement, msg);
			}
		}
		return lockAll;
	}

}
