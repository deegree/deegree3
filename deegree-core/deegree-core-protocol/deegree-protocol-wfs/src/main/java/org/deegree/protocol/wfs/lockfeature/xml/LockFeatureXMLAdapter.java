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

package org.deegree.protocol.wfs.lockfeature.xml;

import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.filter.Filter;
import org.deegree.filter.xml.Filter100XMLDecoder;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.protocol.i18n.Messages;
import org.deegree.protocol.wfs.AbstractWFSRequestXMLAdapter;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.lockfeature.LockFeature;
import org.deegree.protocol.wfs.query.FilterQuery;
import org.deegree.protocol.wfs.query.Query;
import org.deegree.protocol.wfs.query.xml.QueryXMLAdapter;

/**
 * Adapter between XML <code>LockFeature</code> requests and {@link LockFeature} objects.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class LockFeatureXMLAdapter extends AbstractWFSRequestXMLAdapter {

	/**
	 * Parses a WFS <code>LockFeature</code> document into a {@link LockFeature} object.
	 * <p>
	 * Supported versions:
	 * <ul>
	 * <li>WFS 1.0.0</li>
	 * <li>WFS 1.1.0</li>
	 * <li>WFS 2.0.0</li>
	 * </ul>
	 * @return parsed {@link LockFeature} request
	 * @throws Exception
	 * @throws XMLParsingException if a syntax error occurs in the XML
	 * @throws MissingParameterException if the request version is unsupported
	 * @throws InvalidParameterValueException if a parameter contains a syntax error
	 */
	public LockFeature parse() throws Exception {

		Version version = determineVersion110Safe();

		LockFeature result = null;
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
			String msg = Messages.get("UNSUPPORTED_VERSION", version,
					Version.getVersionsString(VERSION_100, VERSION_110, VERSION_200));
			throw new InvalidParameterValueException(msg);
		}
		return result;
	}

	/**
	 * Parses a WFS 1.0.0 <code>LockFeature</code> document into a {@link LockFeature}
	 * object.
	 * @return corresponding {@link LockFeature} instance
	 */
	@SuppressWarnings("boxing")
	public LockFeature parse100() {

		String handle = getNodeAsString(rootElement, new XPath("@handle", nsContext), null);
		BigInteger expiryInMinutes = getNodeAsBigInt(rootElement, new XPath("@expiry", nsContext), null);
		BigInteger expiryInSeconds = convertToSeconds(expiryInMinutes);

		String lockActionStr = rootElement.getAttributeValue(new QName("lockAction"));
		Boolean lockAll = parseLockAction(lockActionStr);

		List<OMElement> lockElements = getRequiredElements(rootElement, new XPath("wfs:Lock", nsContext));
		List<Query> queries = new ArrayList<Query>(lockElements.size());
		for (OMElement lockElement : lockElements) {
			queries.add(parseLock100(lockElement));
		}
		return new LockFeature(VERSION_100, handle, queries, expiryInSeconds, lockAll, null);
	}

	private Query parseLock100(OMElement lockElement) {

		String handle = getNodeAsString(lockElement, new XPath("@handle", nsContext), null);
		// TODO can there be an alias for the typeName ??
		TypeName typeName = new TypeName(getRequiredNodeAsQName(lockElement, new XPath("@typeName", nsContext)), null);

		Filter filter = null;
		OMElement filterEl = lockElement.getFirstChildWithName(new QName(OGCNS, "Filter"));
		if (filterEl != null) {
			try {
				// TODO remove usage of wrapper (necessary at the moment to work around
				// problems with AXIOM's
				// XMLStreamReader)
				XMLStreamReader xmlStream = new XMLStreamReaderWrapper(filterEl.getXMLStreamReaderWithoutCaching(),
						null);
				// skip START_DOCUMENT
				xmlStream.nextTag();
				// TODO use filter 1.0.0 parser
				filter = Filter100XMLDecoder.parse(xmlStream);
			}
			catch (XMLStreamException e) {
				e.printStackTrace();
				throw new XMLParsingException(this, filterEl, e.getMessage());
			}
		}
		return new FilterQuery(handle, new TypeName[] { typeName }, null, null, null, null, filter);
	}

	/**
	 * Parses a WFS 1.1.0 <code>LockFeature</code> document into a {@link LockFeature}
	 * object.
	 * @return corresponding {@link LockFeature} instance
	 */
	@SuppressWarnings("boxing")
	public LockFeature parse110() {

		String handle = getNodeAsString(rootElement, new XPath("@handle", nsContext), null);
		BigInteger expiryInMinutes = getNodeAsBigInt(rootElement, new XPath("@expiry", nsContext), null);
		BigInteger expiryInSeconds = convertToSeconds(expiryInMinutes);
		String lockActionStr = rootElement.getAttributeValue(new QName("lockAction"));
		Boolean lockAll = parseLockAction(lockActionStr);

		List<OMElement> lockElements = getRequiredElements(rootElement, new XPath("wfs:Lock", nsContext));
		List<Query> queries = new ArrayList<Query>(lockElements.size());
		for (OMElement lockElement : lockElements) {
			queries.add(parseLock110(lockElement));
		}
		return new LockFeature(VERSION_110, handle, queries, expiryInSeconds, lockAll, null);
	}

	private Query parseLock110(OMElement lockElement) {

		String handle = getNodeAsString(lockElement, new XPath("@handle", nsContext), null);
		// TODO can there be an alias for the typeName ??
		TypeName typeName = new TypeName(getRequiredNodeAsQName(lockElement, new XPath("@typeName", nsContext)), null);

		Filter filter = null;
		OMElement filterEl = lockElement.getFirstChildWithName(new QName(OGCNS, "Filter"));
		if (filterEl != null) {
			try {
				// TODO remove usage of wrapper (necessary at the moment to work around
				// problems with AXIOM's
				// XMLStreamReader)
				XMLStreamReader xmlStream = new XMLStreamReaderWrapper(filterEl.getXMLStreamReaderWithoutCaching(),
						null);
				// skip START_DOCUMENT
				xmlStream.nextTag();
				filter = Filter110XMLDecoder.parse(xmlStream);
			}
			catch (XMLStreamException e) {
				e.printStackTrace();
				throw new XMLParsingException(this, filterEl, e.getMessage());
			}
		}
		return new FilterQuery(handle, new TypeName[] { typeName }, null, null, null, null, filter);
	}

	/**
	 * Parses a WFS 2.0.0 <code>LockFeature</code> document into a {@link LockFeature}
	 * object.
	 * @return corresponding {@link LockFeature} instance
	 * @throws OWSException
	 */
	@SuppressWarnings("boxing")
	public LockFeature parse200() throws OWSException {

		String handle = getNodeAsString(rootElement, new XPath("@handle", nsContext), null);
		BigInteger expiry = getNodeAsBigInt(rootElement, new XPath("@expiry", nsContext), null);
		String lockActionStr = rootElement.getAttributeValue(new QName("lockAction"));
		Boolean lockAll = parseLockAction(lockActionStr);
		String lockId = rootElement.getAttributeValue(new QName("lockId"));

		List<Query> queries = new ArrayList<Query>();
		@SuppressWarnings("unchecked")
		Iterator<OMElement> childElIter = rootElement.getChildElements();
		QueryXMLAdapter queryXMLAdapter = new QueryXMLAdapter();
		queryXMLAdapter.setRootElement(rootElement);
		while (childElIter.hasNext()) {
			OMElement childEl = childElIter.next();
			Query query = queryXMLAdapter.parseAbstractQuery200(childEl);
			queries.add(query);
		}

		return new LockFeature(VERSION_200, handle, queries, expiry, lockAll, lockId);
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

	private BigInteger convertToSeconds(BigInteger expiryInMinutes) {
		if (expiryInMinutes == null) {
			return null;
		}
		return expiryInMinutes.multiply(BigInteger.valueOf(60));
	}

}
