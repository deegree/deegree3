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
package org.deegree.services.wfs.format;

import org.deegree.protocol.wfs.describefeaturetype.DescribeFeatureType;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getgmlobject.GetGmlObject;
import org.deegree.protocol.wfs.getpropertyvalue.GetPropertyValue;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.wfs.WebFeatureService;

/**
 * Implementations provide input/output formats for the {@link WebFeatureService}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public interface Format {

	/**
	 * Called by the {@link WebFeatureService} to indicate that this {@link Format} is
	 * taken out of service.
	 */
	public void destroy();

	/**
	 * Invoked by the {@link WebFeatureService} to perform a
	 * <code>DescribeFeatureType</code> request for this format.
	 * @param request request to be performed, never <code>null</code>
	 * @param response sink for writing the response, never <code>null</code>
	 */
	public void doDescribeFeatureType(DescribeFeatureType request, HttpResponseBuffer response, boolean isSoap)
			throws Exception;

	/**
	 * Invoked by the {@link WebFeatureService} to perform a <code>GetFeature</code>
	 * request for this format.
	 * @param request request to be performed, never <code>null</code>
	 * @param response sink for writing the response, never <code>null</code>
	 */
	public void doGetFeature(GetFeature request, HttpResponseBuffer response) throws Exception;

	/**
	 * Invoked by the {@link WebFeatureService} to perform a <code>GetGmlObject</code>
	 * request for this format.
	 * @param request request to be performed, never <code>null</code>
	 * @param response sink for writing the response, never <code>null</code>
	 * @throws Exception
	 */
	public void doGetGmlObject(GetGmlObject request, HttpResponseBuffer response) throws Exception;

	/**
	 * Invoked by the {@link WebFeatureService} to perform a <code>GetPropertyValue</code>
	 * request for this format.
	 * @param request request to be performed, never <code>null</code>
	 * @param response sink for writing the response, never <code>null</code>
	 * @throws Exception
	 */
	public void doGetPropertyValue(GetPropertyValue getPropertyValue, HttpResponseBuffer response) throws Exception;

}