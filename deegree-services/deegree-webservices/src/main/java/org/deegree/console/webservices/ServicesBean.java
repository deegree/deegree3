/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.console.webservices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.deegree.console.AbstractResourceManagerBean;
import org.deegree.console.Config;
import org.deegree.services.OwsManager;
import org.deegree.workspace.ResourceMetadata;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:name@company.com">Your Name</a>
 */
@ManagedBean
@ViewScoped
public class ServicesBean extends AbstractResourceManagerBean<OwsManager> implements Serializable {

	private static final long serialVersionUID = -8669333203479413121L;

	private final Config mainConfig;

	public ServicesBean() {
		super(OwsManager.class);
		mainConfig = new MainConfig();
	}

	public Config getMainConfig() {
		return mainConfig;
	}

	@Override
	public List<Config> getConfigs() {
		List<Config> configs = new ArrayList<Config>();
		for (ResourceMetadata<?> state : resourceManager.getResourceMetadata()) {
			configs.add(new ServiceConfig(state, resourceManager));
		}
		Collections.sort(configs);
		return configs;
	}

}
