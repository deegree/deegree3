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
package org.deegree.console.datastore.coverage;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.deegree.console.AbstractCreateResourceBean;
import org.deegree.coverage.persistence.CoverageManager;

import java.io.Serializable;

/**
 * JSF backing bean for "Create new coverage" view.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.3
 */
@Named
@ViewScoped
public class CreateCoverageBean extends AbstractCreateResourceBean implements Serializable {

	public CreateCoverageBean() {
		super(CoverageManager.class);
	}

	@Override
	protected String getOutcome() {
		return "/console/datastore/coverage/index";
	}

}
