/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.services.wps.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.process.jaxb.java.LanguageStringType;
import org.deegree.process.jaxb.java.LiteralOutputDefinition;
import org.deegree.process.jaxb.java.ProcessDefinition;
import org.deegree.process.jaxb.java.ProcessDefinition.OutputParameters;
import org.deegree.services.wps.GenericWPSProcess;
import org.deegree.services.wps.WPSProcess;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;

/**
 * Example {@link ProcessProvider} implementation for process provider tutorial.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class ExampleProcessProvider implements ProcessProvider {

	private final Map<CodeType, WPSProcess> idToProcess = new HashMap<CodeType, WPSProcess>();

	private ResourceMetadata<ProcessProvider> metadata;

	/**
	 * @param processIdToReturnValue
	 */
	ExampleProcessProvider(Map<String, String> processIdToReturnValue, ResourceMetadata<ProcessProvider> metadata) {
		this.metadata = metadata;
		for (Entry<String, String> entry : processIdToReturnValue.entrySet()) {
			String processId = entry.getKey();
			String returnValue = entry.getValue();
			WPSProcess process = createProcess(processId, returnValue);
			idToProcess.put(new CodeType(processId), process);
		}
	}

	private WPSProcess createProcess(String processId, String returnValue) {

		// create Processlet instance dynamically
		ConstantProcesslet processlet = new ConstantProcesslet(returnValue);

		// create process definition dynamically
		ProcessDefinition definition = createProcessDefinition(processId);

		// build WPSProcess from processlet and process definition
		return new GenericWPSProcess(definition, processlet);
	}

	private ProcessDefinition createProcessDefinition(String processId) {

		ProcessDefinition definition = new ProcessDefinition();

		org.deegree.process.jaxb.java.CodeType id = new org.deegree.process.jaxb.java.CodeType();
		id.setValue(processId);
		definition.setIdentifier(id);
		definition.setProcessVersion("0.0.1");
		definition.setStatusSupported(false);
		definition.setStoreSupported(false);

		LanguageStringType title = new LanguageStringType();
		title.setValue(processId + " process");
		definition.setTitle(title);
		OutputParameters outputs = new OutputParameters();
		LiteralOutputDefinition literalOutput = new LiteralOutputDefinition();
		id = new org.deegree.process.jaxb.java.CodeType();
		id.setValue("LiteralOutput");
		literalOutput.setIdentifier(id);

		title = new LanguageStringType();
		title.setValue("Constant");
		literalOutput.setTitle(title);

		JAXBElement<LiteralOutputDefinition> outputEl = new JAXBElement<LiteralOutputDefinition>(new QName("", ""),
				LiteralOutputDefinition.class, literalOutput);
		outputs.getProcessOutput().add(outputEl);
		definition.setOutputParameters(outputs);
		return definition;
	}

	@Override
	public void init() {
		for (WPSProcess process : idToProcess.values()) {
			process.getProcesslet().init();
		}
	}

	@Override
	public void destroy() {
		for (WPSProcess process : idToProcess.values()) {
			process.getProcesslet().destroy();
		}
	}

	@Override
	public WPSProcess getProcess(CodeType id) {
		return idToProcess.get(id);
	}

	@Override
	public Map<CodeType, WPSProcess> getProcesses() {
		return idToProcess;
	}

	@Override
	public ResourceMetadata<? extends Resource> getMetadata() {
		return metadata;
	}

}
