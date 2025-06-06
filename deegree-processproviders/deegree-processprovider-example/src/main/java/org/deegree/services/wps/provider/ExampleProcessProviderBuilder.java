/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.services.wps.provider;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;

/**
 * This class is responsible for building example process providers.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class ExampleProcessProviderBuilder implements ResourceBuilder<ProcessProvider> {

	private ResourceMetadata<ProcessProvider> metadata;

	public ExampleProcessProviderBuilder(ResourceMetadata<ProcessProvider> metadata) {
		this.metadata = metadata;
	}

	@Override
	public ProcessProvider build() {
		Map<String, String> processIdToReturnValue = new HashMap<String, String>();

		try {
			XMLStreamReader xmlStream = XMLInputFactory.newInstance()
				.createXMLStreamReader(metadata.getLocation().getAsStream());
			while (xmlStream.getEventType() != XMLStreamConstants.END_DOCUMENT) {
				if (xmlStream.isStartElement() && "Process".equals(xmlStream.getLocalName())) {
					String processId = xmlStream.getAttributeValue(null, "id");
					String returnValue = xmlStream.getElementText();
					processIdToReturnValue.put(processId, returnValue);
				}
				else {
					xmlStream.next();
				}
			}
		}
		catch (Exception e) {
			throw new ResourceInitException("Error parsing example process provider configuration '"
					+ metadata.getIdentifier() + "': " + e.getMessage());
		}

		return new ExampleProcessProvider(processIdToReturnValue, metadata);
	}

}
