//$HeadURL: svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/wpvs/model/data3d/geometry/WorldGeometryObject.java $
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

package org.deegree.rendering.r3d.model.geometry;

import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.model.WorldObject;

/**
 * The <code>WorldRenderableObject</code> is kind of a marker class for DataObjects based on geometry quality models.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author: rbezema $
 *
 * @version $Revision: 15598 $, $Date: 2009-01-12 15:03:49 +0100 (Mo, 12 Jan 2009) $
 */
public class WorldGeometryObject extends WorldObject<SimpleAccessGeometry, GeometryQualityModel> {

    /**
     *
     */
    private static final long serialVersionUID = 1352860202021497685L;

    /**
     * Creates a new WorldGeometryObject with given number of data quality levels (LOD)
     *
     * @param id
     * @param time
     * @param bbox
     *
     * @param levels
     */
    public WorldGeometryObject( String id, String time, Envelope bbox, int levels ) {
        this( id, time, bbox, new GeometryQualityModel[levels] );
    }

    /**
     * @param id
     *            of this object
     * @param time
     *            this object was created in the dbase
     * @param bbox
     *            of this object (may not be null)
     * @param qualityLevels
     *            this data object may render.
     */
    public WorldGeometryObject( String id, String time, Envelope bbox, GeometryQualityModel[] qualityLevels ) {
        super( id, time, bbox, qualityLevels );
    }
}
