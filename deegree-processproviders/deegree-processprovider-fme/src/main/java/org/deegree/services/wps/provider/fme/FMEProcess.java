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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.process.jaxb.java.LanguageStringType;
import org.deegree.process.jaxb.java.ProcessDefinition;
import org.deegree.process.jaxb.java.ProcessDefinition.InputParameters;
import org.deegree.services.wps.DefaultExceptionCustomizer;
import org.deegree.services.wps.ExceptionCustomizer;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.WPSProcess;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class FMEProcess implements WPSProcess {

	private final ProcessDefinition definition;

	private final String fmeWorkspace;

	private final String repo;

	private final FMEProcesslet process;

	public FMEProcess(InputParameters inputs, FMEInvocationStrategy invocationStrategy, String fmeWorkspace,
			String title, String repo, String wsDescr, String restUrl, String uri, String tokenUrl, String token,
			Map<String, String> tokenmap, boolean idQualified) throws MalformedURLException, IOException {
		this.fmeWorkspace = fmeWorkspace;
		this.repo = repo;
		definition = createDefinition(inputs, invocationStrategy, fmeWorkspace, title, repo, wsDescr, idQualified);
		process = new FMEProcesslet(restUrl, tokenUrl, tokenmap, repo, fmeWorkspace, invocationStrategy);
	}

	private ProcessDefinition createDefinition(InputParameters inputs, FMEInvocationStrategy invocationStrategy,
			String fmeWorkspace, String fmeTitle, String repo, String wsDescr, boolean idQualified) {
		ProcessDefinition definition = new ProcessDefinition();
		org.deegree.process.jaxb.java.CodeType id = new org.deegree.process.jaxb.java.CodeType();
		if (idQualified) {
			id.setCodeSpace(repo);
		}
		id.setValue(fmeWorkspace);
		definition.setIdentifier(id);
		definition.setProcessVersion("0.0.1");
		definition.setStatusSupported(false);
		definition.setStoreSupported(false);
		LanguageStringType title = new LanguageStringType();
		title.setValue(fmeTitle);
		definition.setTitle(title);
		LanguageStringType abstract_ = new LanguageStringType();
		abstract_.setValue(wsDescr);
		definition.setAbstract(abstract_);
		definition.setInputParameters(inputs);
		definition.setOutputParameters(invocationStrategy.getOutputParameters());
		definition.setStoreSupported(true);
		return definition;
	}

	public ProcessDefinition getDescription() {
		return definition;
	}

	public ExceptionCustomizer getExceptionCustomizer() {
		return new DefaultExceptionCustomizer(new CodeType(fmeWorkspace, repo));
	}

	public Processlet getProcesslet() {
		return process;
	}

}
