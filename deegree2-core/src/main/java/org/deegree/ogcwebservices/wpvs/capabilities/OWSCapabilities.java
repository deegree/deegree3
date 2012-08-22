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

package org.deegree.ogcwebservices.wpvs.capabilities;

import java.net.URL;

import org.deegree.ogcbase.BaseURL;

/**
 * This class represents an owsCapabilities object.
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class OWSCapabilities extends BaseURL {

	private String onlineResourceType;

	/**
     * Creates a new owsCapabilities object from format and onlineResource.
     *
     * Note: The parameter <code>format</code> is empty.
     *
     * @param format
     * @param onlineResource
     */
	public OWSCapabilities( String format, URL onlineResource ) {
        super(format, onlineResource);
    }

	/**
	 * Creates a new <code>OWSCapabilities</code> object
	 * from teh given Elements format, onlineResourceType and onlineResource.
     *
     * Note: The parameter <code>format</code> is empty.
	 *
	 * @param format
	 * @param onlineResourceType
	 * @param onlineResource
	 */
	public OWSCapabilities( String format, String onlineResourceType, URL onlineResource ) {
		super(format, onlineResource);
		this.onlineResourceType = onlineResourceType;
	}

	/**
	 * @return Returns the onlineResourceType.
	 */
	public String getOnlineResourceType() {
		return onlineResourceType;
	}

}
