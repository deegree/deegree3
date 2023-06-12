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
package org.deegree.metadata.ebrim;

import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 */
public class AdhocQuery extends RegistryObject {

	public AdhocQuery(OMElement record) {
		super(record);
	}

	public AdhocQuery(XMLStreamReader xmlStream) {
		super(xmlStream);
	}

	/**
	 * @return the queryLanguage
	 */
	public String getQueryLanguage() {
		return adapter.getRequiredNodeAsString(adapter.getRootElement(), new XPath("./@queryLanguage", ns));
	}

	/**
	 * @return the queryExpression
	 */
	public OMElement getQueryExpression() {
		return adapter.getElement(adapter.getRootElement(), new XPath("./rim:QueryExpression", ns));
	}

	// TODO: let adhocquery implement query interface
	public ReturnableElement getElementSetName() {
		String reAsString = adapter.getNodeAsString(adapter.getRootElement(),
				new XPath("./rim:QueryExpression/csw:Query/csw:ElementSetName", ns), null);
		ReturnableElement re = ReturnableElement.summary;
		try {
			re = ReturnableElement.valueOf(reAsString);
		}
		catch (Exception e) {
			//
		}
		return re;
	}

}
