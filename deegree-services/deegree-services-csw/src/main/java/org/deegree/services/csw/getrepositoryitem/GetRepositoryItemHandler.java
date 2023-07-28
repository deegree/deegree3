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
package org.deegree.services.csw.getrepositoryitem;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.services.controller.utils.HttpResponseBuffer;

/**
 * Handles {@link GetRepositoryItem} requests
 *
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 */
public class GetRepositoryItemHandler {

	public void doGetRepositoryItem(GetRepositoryItem getRepItem, HttpResponseBuffer response) throws OWSException {
		// do nothing here - return exception
		throw new OWSException("No repository item available", OWSException.NO_APPLICABLE_CODE);
	}

}
