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
package org.deegree.protocol.wfs.client;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.types.AppSchema;
import org.deegree.gml.GMLVersion;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.ows.http.OwsHttpResponse;

/**
 * Encapsulates the response to a WFS <code>GetFeature</code> request.
 * <p>
 * NOTE: The receiver <b>must</b> call {@link #close()} eventually, otherwise system
 * resources (connections) may not be freed.
 * </p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class GetFeatureResponse<T> {

	private final OwsHttpResponse response;

	private final AppSchema appSchema;

	private final GMLVersion gmlVersion;

	/**
	 * @param response
	 * @param appSchema
	 * @param gmlVersion
	 */
	GetFeatureResponse(OwsHttpResponse response, AppSchema appSchema, GMLVersion gmlVersion) {
		this.response = response;
		this.appSchema = appSchema;
		this.gmlVersion = gmlVersion;
	}

	/**
	 * Provides access to the raw response.
	 * @return the raw response, never <code>null</code>
	 */
	public OwsHttpResponse getAsRawResponse() {
		return response;
	}

	/**
	 * Provides access to the feature objects and WFS provided information in the
	 * response.
	 * @return WFS feature collection, never <code>null</code>
	 * @throws OWSExceptionReport
	 * @throws UnknownCRSException
	 * @throws XMLStreamException
	 * @throws XMLParsingException
	 */
	public WFSFeatureCollection<T> getAsWFSFeatureCollection()
			throws XMLParsingException, XMLStreamException, UnknownCRSException, OWSExceptionReport {
		return new WFSFeatureCollection<T>(response.getAsXMLStream(), gmlVersion, appSchema);
	}

	/**
	 * @throws IOException
	 */
	public void close() throws IOException {
		response.close();
	}

}
