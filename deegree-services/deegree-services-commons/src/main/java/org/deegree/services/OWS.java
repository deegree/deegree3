/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.commons.fileupload.FileItem;
import org.deegree.commons.tom.ows.Version;
import org.deegree.services.authentication.SecurityException;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.exception.serializer.ExceptionSerializer;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.workspace.Resource;

/**
 * Implementations are OGC web services.
 *
 * @see OWSProvider
 * @see OGCFrontController
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public interface OWS extends Resource {

	/**
	 * Called by the {@link OGCFrontController} to allow this {@link OWS} to handle an
	 * OGC-KVP request.
	 * @param normalizedKVPParams request parameters (keys are uppercased), never
	 * <code>null</code>
	 * @param request provides access to all information of the original HTTP request
	 * (NOTE: may be GET or POST), never <code>null</code>
	 * @param response response that is sent to the client, never <code>null</code>
	 * @param multiParts A list of multiparts contained in the request. If the request was
	 * not a multipart request the list will be <code>null</code>. If multiparts were
	 * found, the requestDoc will be the first (xml-lized) {@link FileItem} in the list.
	 * @throws ServletException
	 * @throws IOException
	 * @throws SecurityException
	 */
	public void doKVP(Map<String, String> normalizedKVPParams, HttpServletRequest request, HttpResponseBuffer response,
			List<FileItem> multiParts) throws ServletException, IOException, SecurityException;

	/**
	 * Called by the {@link OGCFrontController} to allow this {@link OWS} to handle an
	 * OGC-XML request.
	 * @param xmlStream provides access to the XML request, cursor points to the
	 * START_ELEMENT event of the root element, never <code>null</code>
	 * @param request provides access to all information of the original HTTP request
	 * (NOTE: may be GET or POST), never <code>null</code>
	 * @param response response that is sent to the client, never <code>null</code>
	 * @param multiParts A list of multiparts contained in the request. If the request was
	 * not a multipart request the list will be <code>null</code>. If multiparts were
	 * found, the xmlStream will provide access to the first (xml-lized) {@link FileItem}
	 * in the list of multi parts
	 * @throws ServletException
	 * @throws IOException
	 * @throws SecurityException
	 */
	public void doXML(XMLStreamReader xmlStream, HttpServletRequest request, HttpResponseBuffer response,
			List<FileItem> multiParts) throws ServletException, IOException, SecurityException;

	/**
	 * Called by the {@link OGCFrontController} to allow this {@link OWS} to handle an
	 * OGC-SOAP request.
	 * @param soapDoc <code>XMLAdapter</code> for parsing the SOAP request document, never
	 * <code>null</code>
	 * @param request provides access to all information of the original HTTP request
	 * (NOTE: may be GET or POST), never <code>null</code>
	 * @param response response that is sent to the client, never <code>null</code>
	 * @param multiParts A list of multiparts contained in the request. If the request was
	 * not a multipart request the list will be <code>null</code>. If multiparts were
	 * found, the requestDoc will be the first (xml-lized) {@link FileItem} in the list.
	 * @param factory initialized to the soap version of the request, never
	 * <code>null</code>
	 * @throws ServletException
	 * @throws IOException if an IOException occurred
	 * @throws SecurityException
	 */
	public void doSOAP(SOAPEnvelope soapDoc, HttpServletRequest request, HttpResponseBuffer response,
			List<FileItem> multiParts, SOAPFactory factory) throws ServletException, IOException, SecurityException;

	/**
	 * Returns the {@link ExceptionSerializer} for the given request version.
	 * @param requestVersion version of the request for which the exception has to be
	 * produced, may be <code>null</code> (implies that the serializer and mime type for
	 * the highest supported version shall be returned)
	 * @return never <code>null</code>
	 */
	public XMLExceptionSerializer getExceptionSerializer(Version requestVersion);

}
