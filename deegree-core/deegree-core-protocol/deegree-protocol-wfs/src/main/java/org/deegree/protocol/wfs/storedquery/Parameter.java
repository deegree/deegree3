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
package org.deegree.protocol.wfs.storedquery;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.LanguageString;

/**
 * A parameter definition in a {@link StoredQueryDefinition}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class Parameter {

	private final String name;

	private final QName type;

	private final List<LanguageString> titles;

	private final List<LanguageString> abstracts;

	private final List<OMElement> metadata;

	public Parameter(String name, QName type, List<LanguageString> titles, List<LanguageString> abstracts,
			List<OMElement> metadata) {
		this.name = name;
		this.type = type;
		this.titles = titles;
		this.abstracts = abstracts;
		this.metadata = metadata;
	}

	public String getName() {
		return name;
	}

	public QName getType() {
		return type;
	}

	public List<LanguageString> getTitles() {
		return titles;
	}

	public List<LanguageString> getAbstracts() {
		return abstracts;
	}

	public List<OMElement> getMetadata() {
		return metadata;
	}

}
