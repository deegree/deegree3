/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.services.wfs;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deegree.protocol.wfs.transaction.TransactionAction;

/**
 * Keeps track of the feature ids created or modified by a single type of
 * {@link TransactionAction}.
 *
 * @see TransactionHandler
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
class ActionResults {

	private final Map<String, List<String>> handleToFids = new LinkedHashMap<String, List<String>>();

	private final List<String> fidsWithoutHandle = new LinkedList<String>();

	private int count;

	void add(String fid, String handle) {
		if (handle == null) {
			fidsWithoutHandle.add(fid);
		}
		else {
			List<String> fids = handleToFids.get(handle);
			if (fids == null) {
				fids = new LinkedList<String>();
				handleToFids.put(handle, fids);
			}
			fids.add(fid);
		}
		count++;
	}

	int getTotal() {
		return count;
	}

	Set<String> getHandles() {
		return handleToFids.keySet();
	}

	List<String> getFids(String handle) {
		return handleToFids.get(handle);
	}

	List<String> getFidsWithoutHandle() {
		return fidsWithoutHandle;
	}

}
