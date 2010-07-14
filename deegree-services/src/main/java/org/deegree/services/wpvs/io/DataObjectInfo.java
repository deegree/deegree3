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

package org.deegree.services.wpvs.io;

import org.deegree.commons.index.PositionableModel;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;

/**
 * The <code>DataObjectInfo</code> class wraps the information needed to fill the backend into one class.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * @param
 *          <P>
 *
 */
public class DataObjectInfo<P extends PositionableModel> {

    private String uuid;

    private String type;

    private String name;

    private String externalRef;

    private Envelope envelope;

    private P data;

    private long time;

    private byte[] serializedData = null;

    private int insertedOrdinates = 0;

    private int insertedTextureOrdinates = 0;

    /**
     * @param uuid
     * @param type
     * @param name
     * @param externalRef
     * @param envelope
     * @param data
     */
    public DataObjectInfo( String uuid, String type, String name, String externalRef, Envelope envelope, P data ) {
        this( uuid, type, name, externalRef, envelope, data, System.currentTimeMillis() );
    }

    /**
     *
     * @param uuid
     * @param type
     * @param name
     * @param externalRef
     * @param envelope
     * @param data
     * @param time
     */
    public DataObjectInfo( String uuid, String type, String name, String externalRef, Envelope envelope, P data,
                           long time ) {
        this.uuid = uuid;
        this.type = type;
        this.name = name;
        this.externalRef = externalRef;
        this.envelope = envelope;
        this.data = data;
        this.time = time;
    }

    /**
     * @return the uuid
     */
    public final String getUuid() {
        return uuid;
    }

    /**
     * @return the type
     */
    public final String getType() {
        return type;
    }

    /**
     * @return the name
     */
    public final String getName() {
        return name;
    }

    /**
     * @return the externalRef
     */
    public final String getExternalRef() {
        return externalRef;
    }

    /**
     * @return the envelope
     */
    public final Envelope getEnvelope() {
        return envelope;
    }

    /**
     * @return the data
     */
    public final P getData() {
        return data;
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * @return the serializedData
     */
    public final byte[] getSerializedData() {
        return serializedData;
    }

    /**
     * @param serializedData
     *            the serializedData to set
     */
    public final void setSerializedData( byte[] serializedData ) {
        this.serializedData = serializedData;
    }

    /**
     * @param uuid
     *            the uuid to set
     */
    public final void setUuid( String uuid ) {
        this.uuid = uuid;
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
     * @param externalRef
     *            the externalRef to set
     */
    public final void setExternalRef( String externalRef ) {
        this.externalRef = externalRef;
    }

    /**
     * @param envelope
     *            the envelope to set
     */
    public final void setEnvelope( Envelope envelope ) {
        this.envelope = envelope;
    }

    /**
     * @param data
     *            the data to set
     */
    public final void setData( P data ) {
        this.data = data;
    }

    /**
     * @param time
     *            the time to set
     */
    public final void setTime( long time ) {
        this.time = time;
    }

    /**
     * @param insertedOrdinates
     *            the insertedOrdinates to set
     */
    public final void setInsertedOrdinates( int insertedOrdinates ) {
        this.insertedOrdinates = insertedOrdinates;
    }

    /**
     * @param insertedTextureOrdinates
     *            the insertedTextureOrdinates to set
     */
    public final void setInsertedTextureOrdinates( int insertedTextureOrdinates ) {
        this.insertedTextureOrdinates = insertedTextureOrdinates;
    }

    /**
     * @return the insertedOrdinates
     */
    public final int getInsertedOrdinates() {
        if ( insertedOrdinates == 0 ) {
            if ( data instanceof WorldRenderableObject ) {
                insertedOrdinates = ( (WorldRenderableObject) data ).getOrdinateCount();
            }
        }
        return insertedOrdinates;
    }

    /**
     * @return the insertedTextureOrdinates
     */
    public final int getInsertedTextureOrdinates() {
        if ( insertedTextureOrdinates == 0 ) {
            if ( data instanceof WorldRenderableObject ) {
                insertedTextureOrdinates = ( (WorldRenderableObject) data ).getTextureOrdinateCount();
            }
        }
        return insertedTextureOrdinates;
    }

}
