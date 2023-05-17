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

package org.deegree.protocol.wfs.getfeature.kvp;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.tom.ResolveParams;
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
import org.deegree.filter.projection.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.xml.Filter100XMLDecoder;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.i18n.Messages;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.query.BBoxQuery;
import org.deegree.protocol.wfs.query.FeatureIdQuery;
import org.deegree.protocol.wfs.query.FilterQuery;
import org.deegree.protocol.wfs.query.Query;
import org.deegree.protocol.wfs.query.StandardPresentationParams;
import org.deegree.protocol.wfs.query.kvp.QueryKVPAdapter;

/**
 * Adapter between KVP <code>GetFeature</code> requests and {@link GetFeature} objects.
 * <p>
 * Supported WFS versions:
 * <ul>
 * <li>1.0.0</li>
 * <li>1.1.0</li>
 * <li>2.0.0</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 */
public class GetFeatureKVPAdapter extends QueryKVPAdapter {

	/**
	 * Parses a normalized KVP-map as a WFS {@link GetFeature} request.
	 * @param kvpParams normalized KVP-map; keys must be uppercase, each key only has one
	 * associated value
	 * @param nsMap only for 1.0.0 version; the prefix-namespace map given in the
	 * NamespaceHints in the configuration
	 * @return parsed {@link GetFeature} request
	 * @throws Exception
	 */
	public static GetFeature parse(Map<String, String> kvpParams, Map<String, String> nsMap) throws Exception {

		Version version = Version.parseVersion(KVPUtils.getRequired(kvpParams, "VERSION"));

		GetFeature result = null;
		if (VERSION_100.equals(version)) {
			result = parse100(kvpParams, nsMap);
		}
		else if (VERSION_110.equals(version)) {
			result = parse110(kvpParams);
		}
		else if (VERSION_200.equals(version)) {
			result = parse200(kvpParams);
		}
		else {
			String msg = Messages.get("UNSUPPORTED_VERSION", version,
					Version.getVersionsString(VERSION_100, VERSION_110, VERSION_200));
			throw new InvalidParameterValueException(msg);
		}
		return result;
	}

	@SuppressWarnings("boxing")
	private static GetFeature parse100(Map<String, String> kvpParams, Map<String, String> nsMap) throws Exception {

		NamespaceBindings nsContext = new NamespaceBindings();
		if (nsMap != null) {
			for (String key : nsMap.keySet()) {
				nsContext.addNamespace(key, nsMap.get(key));
			}
		}

		StandardPresentationParams presentationParams = parseStandardPresentationParameters100(kvpParams);

		// optional: 'PROPERTYNAME'
		String propertyStr = kvpParams.get("PROPERTYNAME");
		PropertyName[][] propertyNames = getPropertyNames(propertyStr, nsContext);

		// optional: FEATUREVERSION
		String featureVersion = kvpParams.get("FEATUREVERSION");

		// mandatory: TYPENAME, but optional if FEATUREID is specified
		String typeStrList = kvpParams.get("TYPENAME");
		TypeName[] typeNames = getTypeNames100(typeStrList);

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

		// optional: SRSNAME (not specified in WFS 1.0.0, deegree extension)
		String srsName = kvpParams.get("SRSNAME");
		ICRS srs = null;
		if (srsName != null) {
			srs = CRSManager.getCRSRef(srsName);
		}

		List<Query> queries = new ArrayList<Query>();

		if ((featureIdStr != null && bboxStr != null) || (featureIdStr != null && filterStr != null)
				|| (bboxStr != null && filterStr != null)) {
			// TODO make new exception
			throw new Exception("The FEATUREID, BBOX and FILTER keywords are mutually exclusive!");
		}

		if (featureIdStr != null) {
			if (typeStrList == null && propertyNames == null) {
				queries.add(new FeatureIdQuery(null, null, featureVersion, srs, null, null, featureIds));
			}
			else {
				for (int i = 0; i < featureIds.length; i++) {
					String[] fids = new String[] { featureIds[i] };
					TypeName[] typeName = new TypeName[0];
					if (typeStrList != null) {
						typeName = new TypeName[] { typeNames[i] };
					}
					PropertyName[] projectionClauses = null;
					if (propertyNames != null) {
						if (propertyNames.length > 1) {
							projectionClauses = propertyNames[i];
						}
						else {
							projectionClauses = propertyNames[0];
						}
					}
					queries.add(new FeatureIdQuery(null, typeName, featureVersion, srs, projectionClauses, null, fids));
				}
			}
		}
		else if (bboxStr != null) {
			if (typeNames == null) {
				// TODO make new exception
				throw new Exception("The TYPENAME keyword is mandatory if BBOX is present!");
			}

			String[] coordList = bboxStr.split(",");
			ICRS bboxCrs = null;
			if (coordList.length % 2 == 1) {
				bboxCrs = CRSManager.getCRSRef(coordList[coordList.length - 1]);
			}

			Envelope bbox = createEnvelope(bboxStr, bboxCrs);
			for (int i = 0; i < typeNames.length; i++) {
				TypeName typeName = typeNames[i];
				PropertyName[] projectionClauses = null;
				if (propertyNames != null) {
					projectionClauses = propertyNames[i];
				}
				queries.add(new BBoxQuery(null, new TypeName[] { typeName }, featureVersion, srs, projectionClauses,
						null, bbox));
			}
		}
		else if (filterStr != null || typeNames != null) {
			if (typeNames == null) {
				// TODO make new exception
				throw new Exception("The FILTER element requires the TYPENAME element");
			}

			int length = typeNames.length;

			String[] filters = getFilters(filterStr);

			for (int i = 0; i < length; i++) {
				Filter filter = null;
				if (filters != null) {

					StringReader sr = new StringReader(filters[i]);
					XMLAdapter adapter = new XMLAdapter(sr);
					XMLStreamReaderWrapper streamWrapper = new XMLStreamReaderWrapper(
							adapter.getRootElement().getXMLStreamReaderWithoutCaching(), adapter.getSystemId());
					try {
						streamWrapper.nextTag();
						filter = Filter100XMLDecoder.parse(streamWrapper);
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
				if (propertyNames != null) {
					queries.add(new FilterQuery(null, new TypeName[] { typeNames[i] }, featureVersion, srs,
							propertyNames[i], null, filter));
				}
				else {
					queries.add(new FilterQuery(null, new TypeName[] { typeNames[i] }, featureVersion, srs, null, null,
							filter));
				}
			}
		}
		return new GetFeature(VERSION_100, null, presentationParams, null, queries);
	}

	@SuppressWarnings("boxing")
	private static GetFeature parse110(Map<String, String> kvpParams) throws Exception {

		StandardPresentationParams presentationParams = parseStandardPresentationParameters110(kvpParams);
		ResolveParams resolveParams = parseStandardResolveParameters110(kvpParams);

		// optional: 'NAMESPACE'
		Map<String, String> nsBindings = extractNamespaceBindings110(kvpParams);
		if (nsBindings == null) {
			nsBindings = Collections.emptyMap();
		}

		NamespaceBindings nsContext = new NamespaceBindings();
		if (nsBindings != null) {
			for (String key : nsBindings.keySet()) {
				nsContext.addNamespace(key, nsBindings.get(key));
			}
		}

		// optional: SRSNAME
		String srsName = kvpParams.get("SRSNAME");
		ICRS srs = null;
		if (srsName != null) {
			srs = CRSManager.getCRSRef(srsName);
		}

		// optional: 'PROPERTYNAME'
		String propertyStr = kvpParams.get("PROPERTYNAME");
		PropertyName[][] propertyNames = getPropertyNames(propertyStr, nsContext);

		// optional: SORTBY
		String sortbyStr = kvpParams.get("SORTBY");
		SortProperty[] sortBy = getSortBy(sortbyStr, nsContext);

		// optional: FEATUREVERSION
		String featureVersion = kvpParams.get("FEATUREVERSION");

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

		// optional: 'PROPTRAVXLINKDEPTH'
		String propTravXlinkDepth = kvpParams.get("PROPTRAVXLINKDEPTH");
		String[][] ptxDepthAr = null;
		if (propTravXlinkDepth != null) {
			ptxDepthAr = parseParamList(propTravXlinkDepth);
		}

		// optional: 'PROPTRAVXLINKEXPIRY'
		String propTravXlinkExpiry = kvpParams.get("PROPTRAVXLINKEXPIRY");
		Integer[][] ptxExpAr = null;
		if (propTravXlinkExpiry != null) {
			ptxExpAr = parseParamListAsInts(propTravXlinkDepth);
		}

		propertyNames = getXLinkPropNames(propertyNames, ptxDepthAr, ptxExpAr);
		List<Query> queries = new ArrayList<Query>();

		if ((featureIdStr != null && bboxStr != null) || (featureIdStr != null && filterStr != null)
				|| (bboxStr != null && filterStr != null)) {
			// TODO make new exception
			throw new Exception("The FEATUREID, BBOX and FILTER keywords are mutually exclusive!");
		}

		if (featureIdStr != null) {
			if (typeStrList == null && propertyNames == null) {
				queries.add(new FeatureIdQuery(null, null, featureVersion, srs, null, sortBy, featureIds));
			}
			else {
				for (int i = 0; i < featureIds.length; i++) {
					String[] fid = new String[] { featureIds[i] };
					TypeName[] typeName = new TypeName[0];
					if (typeStrList != null) {
						typeName = new TypeName[] { typeNames[i] };
					}
					PropertyName[] projectionClauses = null;
					if (propertyNames != null) {
						projectionClauses = propertyNames[i];
					}
					queries
						.add(new FeatureIdQuery(null, typeName, featureVersion, srs, projectionClauses, sortBy, fid));
				}
			}
		}
		else if (bboxStr != null) {
			if (typeNames == null) {
				// TODO make new exception
				throw new Exception("The TYPENAME keyword is mandatory if BBOX is present!");
			}

			String[] coordList = bboxStr.split(",");

			// NOTE: Contradiction between spec and CITE tests (for omitted crsUri)
			// - WFS 1.1.0 spec, 14.3.3: coordinates should be in WGS84
			// - CITE tests, wfs:wfs-1.1.0-Basic-GetFeature-tc8.1: If no CRS reference is
			// provided, a service-defined
			// default value must be assumed.
			ICRS bboxCrs = null;
			if (coordList.length % 2 == 1) {
				bboxCrs = CRSManager.getCRSRef(coordList[coordList.length - 1]);
			}

			Envelope bbox = createEnvelope(bboxStr, bboxCrs);
			for (int i = 0; i < typeNames.length; i++) {
				TypeName typeName = typeNames[i];
				PropertyName[] projectionClauses = null;
				if (propertyNames != null) {
					projectionClauses = propertyNames[i];
				}
				queries.add(new BBoxQuery(null, new TypeName[] { typeName }, featureVersion, srs, projectionClauses,
						sortBy, bbox));
			}
		}
		else if (filterStr != null || typeNames != null) {
			if (typeNames == null) {
				// TODO make new exception
				throw new Exception("The FILTER element requires the TYPENAME element");
			}

			int length = typeNames.length;

			String[] filters = getFilters(filterStr);

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
				if (propertyNames != null) {
					queries.add(new FilterQuery(null, new TypeName[] { typeNames[i] }, featureVersion, srs,
							propertyNames[i], sortBy, filter));
				}
				else {
					queries.add(new FilterQuery(null, new TypeName[] { typeNames[i] }, featureVersion, srs, null,
							sortBy, filter));
				}
			}
		}
		return new GetFeature(VERSION_110, null, presentationParams, resolveParams, queries);
	}

	private static GetFeature parse200(Map<String, String> kvpParams) throws Exception {
		StandardPresentationParams presentationParams = parseStandardPresentationParameters200(kvpParams);
		ResolveParams resolveParams = parseStandardResolveParameters200(kvpParams);
		List<Query> queries = parseQueries200(kvpParams, resolveParams);
		return new GetFeature(VERSION_200, null, presentationParams, resolveParams, queries);
	}

}
