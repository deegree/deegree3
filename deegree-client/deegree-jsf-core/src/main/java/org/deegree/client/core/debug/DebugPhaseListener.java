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

package org.deegree.client.core.debug;

import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Map.Entry;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public class DebugPhaseListener implements PhaseListener {

	private static final long serialVersionUID = -2995584920005634458L;

	private static final Logger LOG = getLogger(DebugPhaseListener.class);

	public void afterPhase(PhaseEvent event) {
		LOG.debug("After phase: {}", event.getPhaseId());
	}

	public void beforePhase(PhaseEvent event) {
		LOG.debug("Before phase: {}", event.getPhaseId());
		Map<String, Object> sessionMap = event.getFacesContext().getExternalContext().getSessionMap();
		for (Entry<String, Object> beanEntry : sessionMap.entrySet()) {
			String beanName = beanEntry.getKey();
			Object bean = beanEntry.getValue();
			LOG.debug(" - {}: {}", beanName, bean);
		}
	}

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

}
