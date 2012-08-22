//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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
package org.deegree.ogcwebservices.wps.capabilities;

import java.util.ArrayList;
import java.util.List;

import org.deegree.ogcwebservices.wps.ProcessBrief;

/**
 * ProcessOfferings.java
 *
 * Created on 09.03.2006. 14:11:05h
 *
 * List of brief descriptions of the processes offered by this WPS server.
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @version 1.0.
 * @since 2.0
 */

public class ProcessOfferings {

	/**
	 *
	 * @param processBriefTypesList
	 */
	public ProcessOfferings( List<ProcessBrief> processBriefTypesList ) {
		this.processBriefTypesList = processBriefTypesList;
	}

	/**
	 * Unordered list of one or more brief descriptions of all the processes
	 * offered by this WPS server.
	 */
	protected List<ProcessBrief> processBriefTypesList;

	/**
	 * Gets the value of the process property.
	 * @return a list of brief process descriptions.
	 */
	public List<ProcessBrief> getProcessBriefTypesList() {
		if ( null == processBriefTypesList ) {
			processBriefTypesList = new ArrayList<ProcessBrief>();
		}
		return this.processBriefTypesList;
	}

}
