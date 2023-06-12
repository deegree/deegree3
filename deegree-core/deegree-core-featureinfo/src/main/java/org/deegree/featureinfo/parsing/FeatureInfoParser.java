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

import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import org.deegree.feature.FeatureCollection;

/**
 * Responsible for parsing 'feature collections'.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public interface FeatureInfoParser {

	/**
	 * @param featureInfoToParse the feature info to parse, never <code>null</code>
	 * @param csvLayerNames a comma separated list of layer names, should not be
	 * <code>null</code>
	 * @return a feature collection containingall features that could be reconstructed or
	 * synthesized, never <code>null</code>
	 * @throws XMLStreamException if the content could not be parsed as feature collection
	 */
	FeatureCollection parseAsFeatureCollection(InputStream featureInfoToParse, String csvLayerNames)
			throws XMLStreamException;

}