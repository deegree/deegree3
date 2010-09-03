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

package org.deegree.services.wpvs.io.serializer;

import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import org.deegree.commons.utils.memory.AllocatedHeapMemory;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.BillBoard;
import org.deegree.services.wpvs.io.DataObjectInfo;

/**
 * The <code>BillBoardSerializer</code> class TODO add class documentation here.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 *
 */
public class BillBoardSerializer extends ObjectSerializer<BillBoard> {

    @Override
    public BillBoard read( ByteBuffer billBuffer ) {
        skipHeader( billBuffer );
        String id = readString( billBuffer );
        float[] location = new float[3];
        location[0] = billBuffer.getFloat();
        location[1] = billBuffer.getFloat();
        location[2] = billBuffer.getFloat();
        float width = billBuffer.getFloat();
        float height = billBuffer.getFloat();
        return new BillBoard( id, location, width, height );
    }

    @Override
    public void write( ByteBuffer buffer, DataObjectInfo<BillBoard> object ) {
        writeHeader( buffer, object );
        BillBoard bb = object.getData();
        writeString( buffer, bb.getTextureID() );
        float[] location = bb.getLocation();
        buffer.putFloat( location[0] );
        buffer.putFloat( location[1] );
        buffer.putFloat( location[2] );
        buffer.putFloat( bb.getWidth() );
        buffer.putFloat( bb.getHeight() );
    }

    @Override
    public int serializedObjectSize( DataObjectInfo<BillBoard> bb ) {
        return ( sizeOfString( bb.getData().getTextureID() ) + ( 5 * AllocatedHeapMemory.FLOAT_SIZE ) );
    }

    /**
     * Serializes an object using the standard serialization mechanism, {@link ObjectOutputStream}
     *
     * @param object
     *            to be serialized with the {@link ObjectOutputStream}
     * @return the byte array containing the serialized object.
     */
    @Override
    public byte[] serializeObject( DataObjectInfo<BillBoard> object ) {
        ByteBuffer bb = ByteBuffer.allocate( super.sizeOfSerializedObject( object ) );
        write( bb, object );
        return bb.array();
    }

    /**
     * Deserialize an object from the given byte array.
     *
     * @param buffer
     *            containing bytes to deserialize.
     * @return the deserialized object of type T.
     */
    @Override
    public BillBoard deserializeDataObject( byte[] buffer ) {
        ByteBuffer bb = ByteBuffer.wrap( buffer );
        return read( bb );
    }
}
