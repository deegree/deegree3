/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2022 by:
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
package org.deegree.services.wfs.format.csv;

import static org.deegree.protocol.wfs.getfeature.ResultType.RESULTS;

import org.deegree.protocol.wfs.describefeaturetype.DescribeFeatureType;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.ResultType;
import org.deegree.protocol.wfs.getgmlobject.GetGmlObject;
import org.deegree.protocol.wfs.getpropertyvalue.GetPropertyValue;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.wfs.WebFeatureService;
import org.deegree.services.wfs.format.Format;
import org.deegree.services.wfs.format.csv.request.CsvGetFeatureHandler;

/**
 * {@link Format} implementation for CSV.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CsvFormat implements Format {

	private final CsvGetFeatureHandler csvGetFeatureHandler;

	/**
	 * Instantiate {@link CsvFormat}
	 * @param webFeatureService the {@link WebFeatureService} using this format, never
	 * <code>null</code>
	 */
	public CsvFormat(WebFeatureService webFeatureService) {
		this.csvGetFeatureHandler = new CsvGetFeatureHandler(webFeatureService);
	}

	@Override
	public void destroy() {
		// nothing to do
	}

	@Override
	public void doGetFeature(GetFeature request, HttpResponseBuffer response) throws Exception {
		ResultType type = request.getPresentationParams().getResultType();
		if (type == RESULTS || type == null) {
			this.csvGetFeatureHandler.doGetFeatureResults(request, response);
		}
		else {
			throw new UnsupportedOperationException("GetFeature with RESULTTYPE=HITS for CSV is not supported");
		}
	}

	@Override
	public void doDescribeFeatureType(DescribeFeatureType request, HttpResponseBuffer response, boolean isSoap)
			throws Exception {
		throw new UnsupportedOperationException("DescribeFeatureType for GeoJSON is not supported");
	}

	@Override
	public void doGetGmlObject(GetGmlObject request, HttpResponseBuffer response) throws Exception {
		throw new UnsupportedOperationException("GetGmlObject for GeoJSON is not supported");
	}

	@Override
	public void doGetPropertyValue(GetPropertyValue getPropertyValue, HttpResponseBuffer response) throws Exception {
		throw new UnsupportedOperationException("GetPropertyValue for GeoJSON is not supported");
	}

}
