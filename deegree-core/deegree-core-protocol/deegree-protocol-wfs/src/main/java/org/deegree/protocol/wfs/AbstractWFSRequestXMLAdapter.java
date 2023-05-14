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
package org.deegree.protocol.wfs;

import static org.deegree.commons.xml.CommonNamespaces.FES_20_NS;
import static org.deegree.commons.xml.CommonNamespaces.FES_PREFIX;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;

/**
 * Provides basic functionality for parsing WFS XML requests.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public abstract class AbstractWFSRequestXMLAdapter extends XMLAdapter {

	/** Namespace context with predefined bindings "wfs" and "wfs200" */
	protected static final NamespaceBindings nsContext;

	/** Namespace binding for WFS 1.0.0 and WFS 1.1.0 constructs */
	protected final static String WFS_PREFIX = "wfs";

	/** Namespace binding for WFS 2.0.0 constructs */
	protected final static String WFS_200_PREFIX = "wfs200";

	/** Namespace binding for WFS Temporal Extension 1.0.0 constructs */
	protected static final String WFS_TE_10_NS = "http://www.opengis.net/wfs-te/1.0";

	protected static final String WFS_TE_10_PREFIX = "wfs-te";

	/** Namespace binding for FES Temporal Extension 1.0.0 constructs */
	protected static final String FES_TE_10_NS = "http://www.opengis.net/fes-te/1.0";

	protected static final String FES_TE_10_PREFIX = "fes-te";

	static {
		nsContext = new NamespaceBindings(XMLAdapter.nsContext);
		nsContext.addNamespace(WFS_PREFIX, WFSConstants.WFS_NS);
		nsContext.addNamespace(WFS_200_PREFIX, WFSConstants.WFS_200_NS);
		nsContext.addNamespace(FES_PREFIX, FES_20_NS);
		nsContext.addNamespace(WFS_TE_10_PREFIX, WFS_TE_10_NS);
		nsContext.addNamespace(FES_TE_10_PREFIX, FES_TE_10_NS);
	}

	/**
	 * Returns the protocol version for the given WFS request element based on the value
	 * of the <code>version</code> attribute (for WFS 1.1.0, this attribute is optional
	 * and thus it is assumed that missing implies 1.1.0).
	 * @return protocol version based on <code>version</code> attribute (if missing and
	 * namespace is WFS 1.0.0/1.1.0, version is assumed to be 1.1.0)
	 */
	protected Version determineVersion110Safe() {
		if (WFS_NS.equals(rootElement.getQName().getNamespaceURI())) {
			String s = getNodeAsString(rootElement, new XPath("@version", nsContext), "1.1.0");
			if (s.isEmpty()) {
				s = "1.1.0";
			}
			return Version.parseVersion(s);
		}
		return Version.parseVersion(getRequiredNodeAsString(rootElement, new XPath("@version", nsContext)));
	}

}
