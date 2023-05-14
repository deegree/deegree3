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
package org.deegree.metadata.iso.persistence.queryable;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.xml.XPath;
import org.deegree.metadata.iso.ISORecord;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class Queryable {

	private String column;

	private QueryableConverter converter;

	private XPath xpath;

	private List<QName> names;

	private boolean isMultiple;

	public Queryable(XPath xpath, List<QName> names, boolean isMultiple, String column,
			QueryableConverter converterClass) {
		this.xpath = xpath;
		this.names = names;
		this.isMultiple = isMultiple;
		this.column = column;
		this.converter = converterClass;
	}

	public String getConvertedValue(ISORecord rec) {
		return convert(rec.getStringFromXPath(xpath));
	}

	public List<String> getConvertedValues(ISORecord rec) {
		List<String> result = new ArrayList<String>();
		String[] values = rec.getStringsFromXPath(xpath);
		for (int i = 0; i < values.length; i++) {
			result.add(convert(values[i]));
		}
		return result;
	}

	private String convert(String toConvert) {
		if (converter != null && toConvert != null) {
			return converter.convert(toConvert);
		}
		return toConvert;
	}

	public String getColumn() {
		return column;
	}

	public boolean isMultiple() {
		return isMultiple;
	}

	public List<QName> getNames() {
		return names;
	}

}
