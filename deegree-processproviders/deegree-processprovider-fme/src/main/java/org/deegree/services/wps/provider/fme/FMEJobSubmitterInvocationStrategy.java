/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2022 by:
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
package org.deegree.services.wps.provider.fme;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.deegree.process.jaxb.java.ComplexFormatType;
import org.deegree.process.jaxb.java.ComplexOutputDefinition;
import org.deegree.process.jaxb.java.LanguageStringType;
import org.deegree.process.jaxb.java.ProcessDefinition.OutputParameters;

/**
 * Default {@link FMEInvocationStrategy}.
 *
 * @author <a href="schneider@occamlabs.de">Markus Schneider</a>
 */
class FMEJobSubmitterInvocationStrategy implements FMEInvocationStrategy {

	@Override
	public OutputParameters getOutputParameters() {
		ComplexOutputDefinition response = new ComplexOutputDefinition();
		org.deegree.process.jaxb.java.CodeType id = new org.deegree.process.jaxb.java.CodeType();
		id.setValue("FMEResponse");
		LanguageStringType title = new LanguageStringType();
		title.setValue("Response from FME (Job Submitter Service)");
		response.setTitle(title);
		ComplexFormatType fmtType = new ComplexFormatType();
		fmtType.setMimeType("application/xml");
		response.setDefaultFormat(fmtType);
		response.setIdentifier(id);
		OutputParameters parameters = new OutputParameters();
		parameters.getProcessOutput()
			.add(new JAXBElement<ComplexOutputDefinition>(new QName(""), ComplexOutputDefinition.class, response));
		return parameters;
	}

}
