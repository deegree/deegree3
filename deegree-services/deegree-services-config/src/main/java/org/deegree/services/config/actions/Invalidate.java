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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.services.config.actions;

import static org.deegree.services.config.actions.Utils.getWorkspaceAndPath;

import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.utils.ArrayUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.tile.persistence.cache.CachingTileStore;

/**
 * <code>Invalidate</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */

public class Invalidate {

	public static void invalidate(String path, String qstring, HttpServletResponse resp) throws IOException {
		Pair<DeegreeWorkspace, String> p = getWorkspaceAndPath(path);

		resp.setContentType("text/plain");

		String id = path.split("/")[0];
		String tmsid = path.split("/")[1];
		Envelope bbox = null;
		if (qstring != null && qstring.toLowerCase().startsWith("bbox=")) {
			String s = qstring.substring(5);
			s = URLDecoder.decode(s, "UTF-8");
			double[] ds = ArrayUtils.splitAsDoubles(s, ",");
			if (ds.length != 4) {
				resp.setStatus(404);
				IOUtils.write("The value of the bbox parameter was invalid.\n", resp.getOutputStream());
				return;
			}
			bbox = new GeometryFactory().createEnvelope(ds[0], ds[1], ds[2], ds[3], null);
		}

		TileStore ts = p.first.getNewWorkspace().getResource(TileStoreProvider.class, id);
		if (ts == null) {
			resp.setStatus(404);
			IOUtils.write("No such tile store.\n", resp.getOutputStream());
			return;
		}

		if (!(ts instanceof CachingTileStore)) {
			resp.setStatus(403);
			IOUtils.write("The tile store is no caching tile store.\n", resp.getOutputStream());
			return;
		}

		if (bbox != null) {
			bbox.setCoordinateSystem(
					ts.getTileDataSet(tmsid).getTileMatrixSet().getSpatialMetadata().getCoordinateSystems().get(0));
		}

		long num = ((CachingTileStore) ts).invalidateCache(tmsid, bbox);
		IOUtils.write("Removed " + num + " elements from the cache.\n", resp.getOutputStream());
	}

}
