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
package org.deegree.wpsclient.gui.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import org.deegree.commons.tom.ows.CodeType;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@FacesConverter(forClass = CodeType.class)
public class CodeTypeConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext ctx, UIComponent component, String value) throws ConverterException {
		if (value != null && value.length() > 0) {
			int begIndex = value.indexOf("{");
			int endIndex = value.lastIndexOf("}");
			if (begIndex > -1 && endIndex > 0) {
				String code = value.substring(endIndex + 1);
				String codeSpace = value.substring(begIndex + 1, endIndex);
				return new CodeType(code, codeSpace);
			}
			return new CodeType(value);
		}
		return null;

	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		CodeType ct = (CodeType) value;
		return ct.getCodeSpace() != null ? "{" + ct.getCodeSpace() + "}" : "" + ct.getCode();
	}

}
