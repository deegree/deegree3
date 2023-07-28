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
package org.deegree.console.webservices.wps;

import static org.deegree.protocol.wps.WPSConstants.ExecutionState.SUCCEEDED;

import java.text.SimpleDateFormat;

/**
 * Encapsulates all information for displaying a
 * {@link org.deegree.services.wps.ProcessExecution}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class ProcessExecution {

	public String id;

	public String state;

	public String percentage = "100";

	public String startTime;

	public String finishTime = "-";

	public String duration = "-";

	public String getId() {
		return id;
	}

	public String getState() {
		return state;
	}

	public String getPercentage() {
		return percentage;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getFinishTime() {
		return finishTime;
	}

	public String getDuration() {
		return duration;
	}

	/**
	 * @param p
	 */
	ProcessExecution(org.deegree.services.wps.ProcessExecution p) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long duration = -1;
		if (p.getFinishTime() > 0) {
			duration = p.getFinishTime() - p.getStartTime();
		}
		else if (p.getStartTime() > 0) {
			duration = new java.util.Date().getTime() - p.getStartTime();
		}
		if (duration >= 0) {
			duration /= 1000;
			long seconds = duration % 60;
			long minutes = (duration % 3600) / 60;
			long hours = duration / 3600;
			this.duration = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		}
		this.id = p.getProcessId().toString();
		this.state = p.getExecutionState().toString();
		if (p.getExecutionState() != SUCCEEDED) {
			this.percentage = "" + p.getPercentCompleted();
		}
		this.startTime = df.format(p.getStartTime());
		this.startTime = df.format(p.getStartTime());
		if (p.getFinishTime() > 0) {
			this.finishTime = df.format(p.getFinishTime());
		}
	}

}
