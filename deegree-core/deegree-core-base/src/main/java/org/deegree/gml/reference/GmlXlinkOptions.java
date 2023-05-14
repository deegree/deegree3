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
package org.deegree.gml.reference;

import static java.lang.Integer.parseInt;

import java.util.List;

import org.deegree.commons.tom.ResolveMode;
import org.deegree.commons.tom.ResolveParams;
import org.deegree.commons.tom.gml.GMLObject;
import org.jaxen.expr.NameStep;

/**
 * Options for controlling the generation of xlinks to {@link GMLObject}s.
 *
 * @see GmlXlinkStrategy
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class GmlXlinkOptions {

	private List<NameStep> resolvePath;

	private final int resolveDepth;

	private final int currentLevel;

	private final ResolveMode mode;

	private final long remoteTimeoutInMilliseconds;

	public GmlXlinkOptions() {
		resolvePath = null;
		resolveDepth = 0;
		currentLevel = 0;
		mode = ResolveMode.NONE;
		remoteTimeoutInMilliseconds = 0;
	}

	public GmlXlinkOptions(ResolveParams params) {
		if (params.getDepth() != null) {
			if ("*".equals(params.getDepth())) {
				resolveDepth = -1;
			}
			else {
				resolveDepth = parseInt(params.getDepth());
			}
		}
		else {
			resolveDepth = 0;
		}
		this.currentLevel = 0;
		this.mode = params.getMode();
		if (params.getTimeout() != null) {
			remoteTimeoutInMilliseconds = params.getTimeout().longValue() * 1000;
		}
		else {
			remoteTimeoutInMilliseconds = 60 * 1000;
		}
	}

	/**
	 * @param remainingResolvePath
	 * @param depth
	 * @param currentLevel
	 * @param mode
	 * @param remoteTimeoutInMilliseconds
	 */
	public GmlXlinkOptions(List<NameStep> remainingResolvePath, int depth, int currentLevel, ResolveMode mode,
			long remoteTimeoutInMilliseconds) {
		this.resolvePath = remainingResolvePath;
		this.resolveDepth = depth;
		this.currentLevel = currentLevel;
		this.mode = mode;
		this.remoteTimeoutInMilliseconds = remoteTimeoutInMilliseconds;
	}

	public int getDepth() {
		return resolveDepth;
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public ResolveMode getMode() {
		return mode;
	}

	public long getRemoteTimeoutInMilliseconds() {
		return remoteTimeoutInMilliseconds;
	}

}
