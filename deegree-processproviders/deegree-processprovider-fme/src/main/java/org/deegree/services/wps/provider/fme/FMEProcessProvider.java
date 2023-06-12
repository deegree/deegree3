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

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.services.wps.WPSProcess;
import org.deegree.services.wps.provider.ProcessProvider;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;

import java.util.Map;

/**
 * Example {@link ProcessProvider} implementation for process provider tutorial.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class FMEProcessProvider implements ProcessProvider {

	private final Map<CodeType, FMEProcess> idToProcess;

	private final FMEProcessMetadata metadata;

	public FMEProcessProvider(Map<CodeType, FMEProcess> processes, FMEProcessMetadata metadata) {
		idToProcess = processes;
		this.metadata = metadata;
	}

	@Override
	public ResourceMetadata<? extends Resource> getMetadata() {
		return metadata;
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
	public Map<CodeType, FMEProcess> getProcesses() {
		return idToProcess;
	}

}
