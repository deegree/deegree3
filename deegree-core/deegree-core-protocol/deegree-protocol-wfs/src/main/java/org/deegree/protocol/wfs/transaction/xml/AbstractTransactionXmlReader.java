/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.protocol.wfs.transaction.xml;

import static org.deegree.protocol.wfs.transaction.ReleaseAction.ALL;
import static org.deegree.protocol.wfs.transaction.ReleaseAction.SOME;

import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.protocol.wfs.AbstractWFSRequestXMLAdapter;
import org.deegree.protocol.wfs.transaction.ReleaseAction;

/**
 * Abstract base class for readers for XML encoded <code>Transaction</code> requests and
 * contained actions.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
abstract class AbstractTransactionXmlReader extends AbstractWFSRequestXMLAdapter implements TransactionXmlReader {

	protected ReleaseAction parseReleaseAction(String releaseActionString) {
		ReleaseAction releaseAction = null;
		if (releaseActionString != null) {
			if ("SOME".equals(releaseActionString)) {
				releaseAction = SOME;
			}
			else if ("ALL".equals(releaseActionString)) {
				releaseAction = ALL;
			}
			else {
				String msg = "Invalid value (=" + releaseActionString
						+ ") for release action parameter. Valid values are 'ALL' or 'SOME'.";
				throw new InvalidParameterValueException(msg, "releaseAction");
			}
		}
		return releaseAction;
	}

}
