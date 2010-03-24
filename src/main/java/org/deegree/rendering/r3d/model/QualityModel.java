//$HeadURL$
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

package org.deegree.rendering.r3d.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.utils.memory.AllocatedHeapMemory;
import org.deegree.commons.utils.memory.MemoryAware;
import org.deegree.rendering.r3d.opengl.rendering.model.prototype.PrototypeReference;
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
 *            the qualitymodel type
 *
 */
public class QualityModel<T extends QualityModelPart> implements Serializable, MemoryAware {

    /**
     *
     */
    private static final long serialVersionUID = 9016130832456126790L;

    private transient static Logger LOG = LoggerFactory.getLogger( QualityModel.class );

    /**
     * The geometries of this quality model
     */
    protected transient ArrayList<T> qualityModelParts = null;

    /**
     * The prototype
     */
    protected transient PrototypeReference prototype = null;

    /**
     * Creates a GeometryQualityModel with an empty list of geometry patches
     *
     */
    public QualityModel() {
        this.qualityModelParts = new ArrayList<T>();
    }

    /**
     * Creates a GeometryQualityModel with the given geometry patches
     *
     * @param qualityModelParts
     */
    public QualityModel( ArrayList<T> qualityModelParts ) {
        this.qualityModelParts = qualityModelParts;
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
     * @param qualityModelPart
     */
    public QualityModel( T qualityModelPart ) {
        this.qualityModelParts = new ArrayList<T>( 1 );
        if ( qualityModelPart != null ) {
            this.qualityModelParts.add( qualityModelPart );
        }
    }

    /**
     * @param part
     *            to add to the geometries
     * @return true (as specified by Collection.add)
     */
    public final boolean addQualityModelPart( T part ) {
        if ( qualityModelParts == null ) {
            qualityModelParts = new ArrayList<T>();
            if ( prototype != null ) {
                LOG.debug( "Setting prototype reference to null" );
                prototype = null;
            }
        }
        return qualityModelParts.add( part );
    }

    /**
     * @param parts
     *            to add to the quality model
     * @return true (as specified by Collection.add)
     */
    public final boolean addQualityModelParts( List<T> parts ) {
        if ( qualityModelParts == null ) {
            qualityModelParts = new ArrayList<T>();
            if ( prototype != null ) {
                LOG.debug( "Setting prototype reference to null" );
                prototype = null;
            }
        }
        return qualityModelParts.addAll( parts );
    }

    /**
     * @param index
     * @return the geometry data at given index or <code>null</code> if the index is out of bounds.
     */
    public final T getQualityModelPart( int index ) {
        if ( qualityModelParts == null || index < 0 || index > qualityModelParts.size() ) {
            return null;
        }
        return qualityModelParts.get( index );
    }

    /**
     * @return the reference to the qualityModelParts, alterations to the patches will be reflected, may be
     *         <code>null</code>
     */
    public final ArrayList<T> getQualityModelParts() {
        return qualityModelParts;
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
            if ( qualityModelParts != null ) {
                LOG.debug( "Setting quality model parts to null" );
                qualityModelParts = null;
            }
        }
        this.prototype = prototype;
    }

    /**
     * Method called while serializing this object
     *
     * @param out
     *            to write to.
     * @throws IOException
     */
    private void writeObject( java.io.ObjectOutputStream out )
                            throws IOException {
        LOG.trace( "Serializing to object stream." );
        out.writeObject( qualityModelParts );
        out.writeObject( prototype );
    }

    /**
     * Method called while de-serializing (instancing) this object.
     *
     * @param in
     *            to create the methods from.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    private void readObject( java.io.ObjectInputStream in )
                            throws IOException, ClassNotFoundException {
        LOG.trace( "Deserializing from object stream." );
        qualityModelParts = (ArrayList<T>) in.readObject();
        prototype = (PrototypeReference) in.readObject();
    }

    /**
     * @return approximate size in bytes
     */
    public long sizeOf() {
        long localSize = AllocatedHeapMemory.INSTANCE_SIZE;
        localSize += AllocatedHeapMemory.sizeOfList( qualityModelParts, true );
        if ( qualityModelParts != null ) {
            for ( T part : qualityModelParts ) {
                localSize += part.sizeOf();
            }
        } else {
            if ( prototype != null ) {
                localSize += AllocatedHeapMemory.REF_SIZE + prototype.getApproximateSizeInBytes();
            }
        }
        return localSize;
    }

}
