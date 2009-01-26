//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.rendering.r3d;

import java.io.Serializable;
import java.util.ArrayList;

import org.deegree.rendering.r3d.geometry.SimpleAccessGeometry;
import org.deegree.rendering.r3d.opengl.rendering.prototype.PrototypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>GeometryQualityModel</code> defines a Quality level of a geometry model.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * @param <T>
 *            the geometry type
 * 
 */
public class QualityModel<T extends SimpleAccessGeometry> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5064310622827744008L;

    private transient static Logger LOG = LoggerFactory.getLogger( QualityModel.class );

    /**
     * The geometries of this quality model
     */
    protected ArrayList<T> geometryPatches = null;

    /**
     * The prototype
     */
    protected PrototypeReference prototype = null;

    /**
     * Creates a GeometryQualityModel with an empty list of geometry patches
     * 
     */
    public QualityModel() {
        this.geometryPatches = new ArrayList<T>();
    }

    /**
     * Creates a GeometryQualityModel with the given geometry patches
     * 
     * @param geometryPatches
     */
    public QualityModel( ArrayList<T> geometryPatches ) {
        this.geometryPatches = geometryPatches;
    }

    /**
     * Creates a GeometryQualityModel with the given geometry patches
     * 
     * @param prototype
     *            to be renedered
     */
    public QualityModel( PrototypeReference prototype ) {
        this.prototype = prototype;
    }

    /**
     * @param geometryPatch
     */
    public QualityModel( T geometryPatch ) {
        this.geometryPatches = new ArrayList<T>( 1 );
        if ( geometryPatch != null ) {
            this.geometryPatches.add( geometryPatch );
        }
    }

    /**
     * @param data
     *            to add to the geometries
     * @return true (as specified by Collection.add)
     */
    public final boolean addGeometryData( T data ) {
        if ( geometryPatches == null ) {
            geometryPatches = new ArrayList<T>();
            if ( prototype != null ) {
                LOG.debug( "Setting prototype reference to null" );
                prototype = null;
            }
        }
        return geometryPatches.add( data );
    }

    /**
     * @param index
     * @return the geometry data at given index or <code>null</code> if the index is out of bounds.
     */
    public final T getGeometryData( int index ) {
        if ( geometryPatches == null || index < 0 || index > geometryPatches.size() ) {
            return null;
        }
        return geometryPatches.get( index );
    }

    /**
     * @return the reference to the geometryPatches, alterations to the patches will be reflected, may be
     *         <code>null</code>
     */
    public final ArrayList<T> getGeometryPatches() {
        return geometryPatches;
    }

    /**
     * @return the prototype or <code>null</code> if no prototype was defined
     */
    public final PrototypeReference getPrototypeReference() {
        return prototype;
    }

    /**
     * @param prototype
     *            the prototype to set
     */
    public final void setPrototype( PrototypeReference prototype ) {
        if ( prototype != null ) {
            if ( geometryPatches != null ) {
                LOG.debug( "Setting geometry patches to null" );
                geometryPatches = null;
            }
        }
        this.prototype = prototype;
    }
}
