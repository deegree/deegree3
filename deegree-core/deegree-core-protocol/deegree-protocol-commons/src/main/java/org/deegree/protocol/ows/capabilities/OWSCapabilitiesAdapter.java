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
package org.deegree.protocol.ows.capabilities;

import java.util.List;

import org.deegree.commons.ows.metadata.OperationsMetadata;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;

/**
 * Base interface for {@link XMLAdapter} implementations that extract metadata from OWS
 * capabilities documents.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public interface OWSCapabilitiesAdapter {

	/**
	 * Extracts the {@link ServiceIdentification} from the underlying XML document.
	 * @return service identification, can be <code>null</code> (if document does not
	 * contain such a section)
	 * @throws XMLParsingException if the <code>ServiceIdentification</code>
	 * section/information can not be parsed
	 */
	ServiceIdentification parseServiceIdentification() throws XMLParsingException;

	/**
	 * Extracts the {@link ServiceProvider} from the underlying XML document.
	 * @return service provider, can be <code>null</code> (if document does not contain
	 * such a section)
	 * @throws XMLParsingException if the <code>ServiceProvider</code> section/information
	 * can not be parsed
	 */
	ServiceProvider parseServiceProvider() throws XMLParsingException;

	/**
	 * Extracts the {@link OperationsMetadata} from the underlying XML document.
	 * @return operations metadata, can be <code>null</code> (if document does not contain
	 * such a section)
	 * @throws XMLParsingException if the <code>OperationsMetadata</code>
	 * section/information can not be parsed
	 */
	OperationsMetadata parseOperationsMetadata() throws XMLParsingException;

	/**
	 * Extracts the supported languages from the underlying XML document.
	 * @return supported languages, can be <code>null</code> (if document does not contain
	 * such a section)
	 * @throws XMLParsingException if the <code>Languages</code> section/information can
	 * not be parsed
	 */
	List<String> parseLanguages() throws XMLParsingException;

}
