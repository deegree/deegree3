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

import static org.deegree.commons.utils.memory.AllocatedHeapMemory.sizeOfObjectArray;
import static org.deegree.commons.utils.memory.AllocatedHeapMemory.sizeOfString;

import java.io.IOException;
import java.io.Serializable;

import org.deegree.commons.index.PositionableModel;
import org.deegree.commons.utils.math.Vectors3d;
import org.deegree.commons.utils.memory.AllocatedHeapMemory;
import org.deegree.commons.utils.memory.MemoryAware;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.primitive.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>WorldRenderableObject</code> top level class, all data objects can be stored in a dbase.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * @param <G>
 *            the geometry type of the quality model
 * @param <QM>
 *            the quality model type
 * 
 */
public class WorldObject<G extends QualityModelPart, QM extends QualityModel<G>> implements Serializable, MemoryAware,
                                                                                 PositionableModel {

    /**
     *
     */
    private static final long serialVersionUID = 628773986403744985L;

    private final static Logger LOG = LoggerFactory.getLogger( WorldObject.class );

    private transient String type;

    private transient String name;

    private transient String externalReference;

    private transient String id;

    private transient String time;

    private transient Envelope bbox;

    private transient float[] modelBBox;

    /**
     * the position of this world object.
     */
    protected transient float[] position;

    private transient float error;

    private transient float groundLevel;

    private transient float height;

    /**
     * The quality levels of the this object
     */
    protected transient QM[] qualityLevels;

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
    public WorldObject( String id, String time, Envelope bbox, QM[] qualityLevels ) {
        this.id = id;
        this.time = time;
        if ( bbox == null ) {
            throw new NullPointerException( "Bbox may not be null" );
        }
        this.bbox = bbox;
        Point p = bbox.getCentroid();
        double[] min = bbox.getMin().getAsArray();
        double[] max = bbox.getMax().getAsArray();
        position = new float[] { (float) p.get0(), (float) p.get1(),
                                (float) ( ( p.getCoordinateDimension() == 3 ) ? p.get2() : 0 ) };
        if ( bbox.getMin().getCoordinateDimension() == 2 ) {
            min = new double[] { min[0], min[1], 0 };
        }
        if ( bbox.getMax().getCoordinateDimension() == 2 ) {
            max = new double[] { max[0], max[1], 0 };
        }
        error = (float) Vectors3d.length( Vectors3d.sub( max, min ) );
        this.height = (float) ( max[2] - min[2] );
        this.groundLevel = (float) min[2];
        this.qualityLevels = qualityLevels;
    }

    /**
     * @param index
     *            to get the level for
     * @return the quality model at the given index.
     */
    public QM getQualityLevel( int index ) {
        if ( index < 0 || index > qualityLevels.length ) {
            return null;
        }
        return qualityLevels[index];
    }

    /**
     * @return the quality models array
     */
    public QM[] getQualityLevels() {
        return qualityLevels;
    }

    /**
     * @return the number of quality levels this worldobject can hold.
     */
    public int getNumberOfQualityLevels() {
        return ( qualityLevels == null ) ? 0 : qualityLevels.length;
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
     * @param name
     *            of this object
     * @param type
     *            of this object
     * @param externalReference
     *            of this object
     */
    public WorldObject( String id, String time, Envelope bbox, QM[] qualityLevels, String name, String type,
                        String externalReference ) {
        this( id, time, bbox, qualityLevels );
        this.name = name;
        this.type = type;
        this.externalReference = externalReference;
    }

    /**
     * Set the model at the given quality level. If the index is out of bounds nothing will happen, if the model is
     * <code>null</code> the array at given location will be null (deleted).
     * 
     * @param index
     *            to place the model at
     * @param model
     *            to place
     */
    public void setQualityLevel( int index, QM model ) {
        if ( qualityLevels != null ) {
            if ( index >= 0 || index < qualityLevels.length ) {
                qualityLevels[index] = model;
            }
        }
    }

    /**
     * @param newLevels
     *            an instantiated array of QualityModels (which may be null), if the given array is <code>null</code>
     *            nothing will happen.
     */
    protected void resetQualityLevels( QM[] newLevels ) {
        if ( newLevels != null ) {
            qualityLevels = newLevels;
        }
    }

    /**
     * @return the id
     */
    public final String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public final void setId( String id ) {
        this.id = id;
    }

    /**
     * @return the time
     */
    public final String getTime() {
        return time;
    }

    /**
     * @param time
     *            the time to set
     */
    public final void setTime( String time ) {
        this.time = time;
    }

    /**
     * @return the bbox
     */
    public final Envelope getBbox() {
        return bbox;
    }

    /**
     * @param bbox
     *            the bbox to set
     */
    public final void setBbox( Envelope bbox ) {
        if ( bbox == null ) {
            throw new NullPointerException( "Bbox may not be null" );
        }
        this.bbox = bbox;
        Point p = bbox.getCentroid();
        double[] min = bbox.getMin().getAsArray();
        double[] max = bbox.getMax().getAsArray();
        position = new float[] { (float) p.get0(), (float) p.get1(),
                                (float) ( ( p.getCoordinateDimension() == 3 ) ? p.get2() : 0 ) };
        if ( bbox.getMin().getCoordinateDimension() == 2 ) {
            min = new double[] { min[0], min[1], 0 };
        }
        if ( bbox.getMax().getCoordinateDimension() == 2 ) {
            max = new double[] { max[0], max[1], 0 };
        }
        this.error = (float) Vectors3d.length( Vectors3d.sub( max, min ) );
        this.height = (float) bbox.getSpan1();
        this.groundLevel = (float) min[2];
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
        out.writeObject( qualityLevels );
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
        qualityLevels = (QM[]) in.readObject();
    }

    /**
     * @return the approximate size in bytes of this world object. This value is not perfectly correct (references in
     *         the rendering.r3 package are called) but it will give an estimate of the amount of memory a worldobject
     *         uses.
     */
    public long sizeOf() {
        long localSize = AllocatedHeapMemory.INSTANCE_SIZE;// class address is 8 bytes
        localSize += sizeOfObjectArray( qualityLevels, true );
        if ( qualityLevels != null && qualityLevels.length > 0 ) {
            for ( QualityModel<?> qm : qualityLevels ) {
                localSize += qm.sizeOf();
            }
        }
        localSize += sizeOfString( id, true, true );
        localSize += sizeOfString( time, true, true );
        // localSize += sizeOfEnvelope( bbox, true );
        return localSize;
    }

    @Override
    public float[] getPosition() {
        return position;
    }

    @Override
    public float getErrorScalar() {
        return error;
    }

    @Override
    public float getGroundLevel() {
        return groundLevel;
    }

    @Override
    public float getObjectHeight() {
        return height;
    }

    /**
     * @return the type of this object, e.g. building or tv.
     */
    public String getType() {
        return type;
    }

    /**
     * @return the name of this object,
     */
    public String getName() {
        return name;
    }

    /**
     * @return the externalReference
     */
    public final String getExternalReference() {
        return externalReference;
    }

    /**
     * @param type
     *            the type to set
     */
    public final void setType( String type ) {
        this.type = type;
    }

    /**
     * @param name
     *            the name to set
     */
    public final void setName( String name ) {
        this.name = name;
    }

    /**
     * @param externalReference
     *            the externalReference to set
     */
    public final void setExternalReference( String externalReference ) {
        this.externalReference = externalReference;
    }

    @Override
    public float[] getModelBBox() {
        if ( modelBBox == null ) {
            modelBBox = new float[6];
            double[] min = bbox.getMin().getAsArray();
            double[] max = bbox.getMax().getAsArray();
            modelBBox[0] = (float) min[0];
            modelBBox[1] = (float) min[1];
            modelBBox[2] = (float) min[2];

            modelBBox[3] = (float) max[0];
            modelBBox[4] = (float) max[1];
            modelBBox[5] = (float) max[2];
        }
        return modelBBox;
    }
}
