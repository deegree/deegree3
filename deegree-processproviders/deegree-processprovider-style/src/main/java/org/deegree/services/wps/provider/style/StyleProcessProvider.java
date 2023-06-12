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
package org.deegree.services.wps.provider.style;

import static java.math.BigInteger.ONE;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.process.jaxb.java.ComplexFormatType;
import org.deegree.process.jaxb.java.ComplexInputDefinition;
import org.deegree.process.jaxb.java.ComplexOutputDefinition;
import org.deegree.process.jaxb.java.LanguageStringType;
import org.deegree.process.jaxb.java.ProcessDefinition;
import org.deegree.process.jaxb.java.ProcessDefinition.InputParameters;
import org.deegree.process.jaxb.java.ProcessDefinition.OutputParameters;
import org.deegree.services.wps.GenericWPSProcess;
import org.deegree.services.wps.WPSProcess;
import org.deegree.services.wps.provider.ProcessProvider;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class StyleProcessProvider implements ProcessProvider {

	static final String IN_PARAM_ID = "Style";

	static final String OUT_PARAM_ID = "Legend";

	Map<CodeType, WPSProcess> idtoProcess = new HashMap<CodeType, WPSProcess>();

	private ResourceMetadata<ProcessProvider> metadata;

	public StyleProcessProvider(String processId, ResourceMetadata<ProcessProvider> metadata) {
		this.metadata = metadata;
		idtoProcess.put(new CodeType(processId),
				new GenericWPSProcess(getDescription(processId), new StyleProcesslet()));
	}

	private ProcessDefinition getDescription(String processId) {
		ProcessDefinition definition = new ProcessDefinition();

		org.deegree.process.jaxb.java.CodeType id = new org.deegree.process.jaxb.java.CodeType();
		id.setValue(processId);
		definition.setIdentifier(id);
		definition.setProcessVersion("0.0.1");
		definition.setStatusSupported(true);
		definition.setStoreSupported(true);

		LanguageStringType title = new LanguageStringType();
		title.setValue(processId + " process");
		definition.setTitle(title);

		ComplexInputDefinition complexInput = new ComplexInputDefinition();
		id = new org.deegree.process.jaxb.java.CodeType();
		id.setValue(IN_PARAM_ID);
		complexInput.setIdentifier(id);

		title = new LanguageStringType();
		title.setValue("style input");
		complexInput.setTitle(title);
		complexInput.setMinOccurs(ONE);
		complexInput.setMaxOccurs(ONE);

		ComplexFormatType inFormat = new ComplexFormatType();
		inFormat.setEncoding("UTF-8");
		inFormat.setMimeType("text/xml");
		inFormat.setSchema("http://www.opengis.net/sld");
		complexInput.setDefaultFormat(inFormat);

		JAXBElement<ComplexInputDefinition> inputEl = new JAXBElement<ComplexInputDefinition>(new QName("", ""),
				ComplexInputDefinition.class, complexInput);
		InputParameters inputs = new InputParameters();
		inputs.getProcessInput().add(inputEl);
		definition.setInputParameters(inputs);

		ComplexOutputDefinition complexOutput = new ComplexOutputDefinition();
		id = new org.deegree.process.jaxb.java.CodeType();
		id.setValue(OUT_PARAM_ID);
		complexOutput.setIdentifier(id);

		ComplexFormatType outFormat = new ComplexFormatType();
		outFormat.setMimeType("image/png");
		complexOutput.setDefaultFormat(outFormat);

		title = new LanguageStringType();
		title.setValue("legend graphic output");
		complexOutput.setTitle(title);

		JAXBElement<ComplexOutputDefinition> outputEl = new JAXBElement<ComplexOutputDefinition>(new QName("", ""),
				ComplexOutputDefinition.class, complexOutput);
		OutputParameters outputs = new OutputParameters();
		outputs.getProcessOutput().add(outputEl);
		definition.setOutputParameters(outputs);
		return definition;
	}

	@Override
	public Map<CodeType, ? extends WPSProcess> getProcesses() {
		return idtoProcess;
	}

	@Override
	public WPSProcess getProcess(CodeType id) {
		return idtoProcess.get(id);
	}

	@Override
	public void init() {
		// nothing to do
	}

	@Override
	public void destroy() {
		// nothing to do
	}

	@Override
	public ResourceMetadata<? extends Resource> getMetadata() {
		return metadata;
	}

}
