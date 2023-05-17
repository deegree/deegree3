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

package org.deegree.rendering.r3d.opengl.rendering.model.manager;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.media.opengl.GL;

import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.DirectGeometryBuffer;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;

/**
 * The <code>BuildingRenderer</code> organizes buildings in a scene by using a quadtree.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */
public class BuildingRenderer extends RenderableManager<WorldRenderableObject> {

	private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BuildingRenderer.class);

	private final DirectGeometryBuffer geometryBuffer;

	private LODSwitcher switchLevels;

	/**
	 * @param sceneDomain
	 * @param numberOfObjectsInLeaf
	 * @param geometryBuffer wrapper holding all geometries in a single direct
	 * {@link FloatBuffer}
	 * @param maxPixelError
	 * @param levels configured values for switching between different lods of the
	 * buildings.
	 */
	public BuildingRenderer(Envelope sceneDomain, int numberOfObjectsInLeaf, DirectGeometryBuffer geometryBuffer,
			double maxPixelError, LODSwitcher levels) {
		super(sceneDomain, numberOfObjectsInLeaf, maxPixelError);
		this.geometryBuffer = geometryBuffer;
		this.switchLevels = levels;
	}

	@Override
	public boolean add(WorldRenderableObject renderable) {
		renderable.setSwitchLevels(switchLevels);
		return super.add(renderable);
	}

	@Override
	public boolean addAll(Collection<? extends WorldRenderableObject> c) {
		boolean result = true;
		for (WorldRenderableObject p : c) {
			if (!result) {
				break;
			}
			result = add(p);
		}
		return result;
	}

	@Override
	public void render(RenderContext glRenderContext) {
		long begin = System.currentTimeMillis();
		ViewParams params = glRenderContext.getViewParams();
		GL context = glRenderContext.getContext();
		params.getViewFrustum().getEyePos();
		Set<WorldRenderableObject> buildings = getObjects(params);
		if (!buildings.isEmpty()) {
			// List<WorldRenderableObject> allBuildings = new
			// ArrayList<WorldRenderableObject>( buildings );
			// back to front
			// Collections.sort( allBuildings, new DistComparator( eye ) );
			// LOG.debug( "Sorting of " + allBillBoards.size() + " buildings took: "
			// + ( System.currentTimeMillis() - begin ) + " ms" );
			if (LOG.isDebugEnabled()) {
				LOG.debug("Number of buildings from viewparams: " + buildings.size());
				LOG.debug("Total number of buildings : " + size());
			}
			context.glPushAttrib(GL.GL_CURRENT_BIT | GL.GL_LIGHTING_BIT);
			Iterator<WorldRenderableObject> it = buildings.iterator();
			while (it.hasNext()) {
				WorldRenderableObject b = it.next();
				context.glPushMatrix();
				b.renderPrepared(glRenderContext, geometryBuffer);
				context.glPopMatrix();
			}
			context.glPopAttrib();
			if (LOG.isDebugEnabled()) {
				LOG.debug("Rendering of " + buildings.size() + " buildings took: "
						+ (System.currentTimeMillis() - begin) + " ms");
			}
		}
		else {
			LOG.debug("Not rendering any buildings.");
		}

	}

	/**
	 * @return the geometry buffer used for rendering.
	 */
	public DirectGeometryBuffer getGeometryBuffer() {
		return geometryBuffer;
	}

}
