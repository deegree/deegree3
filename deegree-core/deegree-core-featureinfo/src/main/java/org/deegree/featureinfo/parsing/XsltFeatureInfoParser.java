/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.featureinfo.parsing;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XsltUtils;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.DynamicAppSchema;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;

/**
 * Responsible for parsing 'feature collections' with a xslt file.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class XsltFeatureInfoParser implements FeatureInfoParser {

	private static final Logger LOG = getLogger(XsltFeatureInfoParser.class);

	private static final XMLInputFactory XML_FACTORY = XMLInputFactory.newInstance();

	private final URL xsltFile;

	private final GMLVersion targetGmlVersion;

	/**
	 * @param xsltFile the xslt file used to transform the feature info xml, never
	 * <code>null</code>
	 * @param targetGmlVersion the gml version the xslt is transforming to, never
	 * <code>null</code>
	 **/
	public XsltFeatureInfoParser(URL xsltFile, GMLVersion targetGmlVersion) {
		this.xsltFile = xsltFile;
		this.targetGmlVersion = targetGmlVersion;
	}

	@Override
	public FeatureCollection parseAsFeatureCollection(InputStream featureInfoToParse, String csvLayerNames)
			throws XMLStreamException {
		XMLStreamReader transformedReader = transform(featureInfoToParse);
		return readAsGmlFeatureCollection(transformedReader);
	}

	private XMLStreamReader transform(InputStream featureInfoToParse) throws XMLStreamException {
		LOG.debug("Apply xslt transformation {}.", xsltFile);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			XsltUtils.transform(featureInfoToParse, xsltFile, outputStream);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
			return XML_FACTORY.createXMLStreamReader(inputStream);
		}
		catch (Exception e) {
			LOG.warn("Unable to transform remote feature info xml stream: {}.", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
			throw new XMLStreamException("Unable to transform remote feature info xml stream.");
		}
	}

	private FeatureCollection readAsGmlFeatureCollection(XMLStreamReader transformedReader) throws XMLStreamException {
		try {
			GMLStreamReader reader = GMLInputFactory.createGMLStreamReader(targetGmlVersion, transformedReader);
			reader.setApplicationSchema(new DynamicAppSchema());
			return reader.readFeatureCollection();
		}
		catch (XMLParsingException e) {
			LOG.warn("Unable to read transfomed feature info xml stream: {}.", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
			throw new XMLStreamException("Unable to read transfomed feature info xml stream.");
		}
		catch (UnknownCRSException e) {
			LOG.warn("Unable to read transfomed feature info xml stream: {}.", e.getLocalizedMessage());
			LOG.trace("Stack trace:", e);
			throw new XMLStreamException("Unable to read transfomed feature info xml stream.");
		}
	}

}