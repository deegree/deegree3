/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2026 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
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
package org.deegree.protocol.wms.ops;

/**
 * Implementations of this class provides slds from a passed query sld param.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public interface SldProvider {

	/**
	 * Called once to initialize the SldProvider
	 */
	void init();

	/**
	 * @param sld the value of the sld query param, never <code>null</code>
	 * @return the content associated to the sld query param, may be <code>null</code> if
	 * not retrievable or parseable
	 */
	String retrieveSldBody(String sld);

}
