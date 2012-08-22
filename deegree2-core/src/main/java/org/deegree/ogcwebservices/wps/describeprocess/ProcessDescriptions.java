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

package org.deegree.ogcwebservices.wps.describeprocess;

import java.util.ArrayList;
import java.util.List;

/**
 * ProcessDescriptions.java
 *
 * Created on 09.03.2006. 22:37:58h
 *
 * WPS DescribeProcess operation response.
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @version 1.0.
 * @since 2.0
 */

public class ProcessDescriptions {
	/**
	 * Ordered list of one or more full Process descriptions, listed in the
	 * order in which they were requested in the DescribeProcess operation
	 * request.
	 */
	protected List<ProcessDescription> processDescription;

	/**
	 *
	 * @param processDescriptionList
	 */
	public ProcessDescriptions( List<ProcessDescription> processDescriptionList ) {
		processDescription = processDescriptionList;
	}

	/**
	 * @return Returns the processDescription.
	 */
	public List<ProcessDescription> getProcessDescription() {
		if ( processDescription == null ) {
			processDescription = new ArrayList<ProcessDescription>();
		}
		return this.processDescription;
	}

}
