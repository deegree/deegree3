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

import java.nio.ByteBuffer;

import org.deegree.rendering.r3d.opengl.rendering.model.geometry.DirectGeometryBuffer;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableQualityModel;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.rendering.r3d.opengl.rendering.model.prototype.RenderablePrototype;
import org.deegree.services.wpvs.io.DataObjectInfo;

/**
 * The <code>PrototypeSerializer</code> class TODO add class documentation here.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 *
 */
public class PrototypeSerializer extends ObjectSerializer<RenderablePrototype> {

    private WROSerializer wroSerializer;

    /**
     * Create a new prototype serializer
     */
    public PrototypeSerializer() {
        wroSerializer = new WROSerializer();
    }

    /**
     * @return the geometryBuffer
     */
    public final DirectGeometryBuffer getGeometryBuffer() {
        return wroSerializer.getGeometryBuffer();
    }

    /**
     * @param geometryBuffer
     *            the geometryBuffer to set
     */
    public final void setGeometryBuffer( DirectGeometryBuffer geometryBuffer ) {
        wroSerializer.setGeometryBuffer( geometryBuffer );
    }

    @Override
    public RenderablePrototype read( ByteBuffer buffer ) {
        WorldRenderableObject wro = wroSerializer.read( buffer );
        return createRenderablePrototype( wro );
    }

    /**
     * @param wro
     * @return
     */
    private RenderablePrototype createRenderablePrototype( WorldRenderableObject wro ) {
        RenderableQualityModel[] models = wro.getQualityLevels();
        if ( models == null || models.length != 1 ) {
            return null;
        }
        return new RenderablePrototype( wro.getId(), wro.getTime(), wro.getBbox(), models[0] );
    }

    @Override
    public int serializedObjectSize( DataObjectInfo<RenderablePrototype> object ) {
        DataObjectInfo<WorldRenderableObject> doi = createDataObject( object );
        int result = wroSerializer.serializedObjectSize( doi );
        object.setSerializedData( doi.getSerializedData() );
        return result;
    }

    /**
     * Helper method needed for the WRO serializer.
     *
     * @param object
     *            to generate the result from.
     * @return the object info needed for the wro serializer
     */
    private DataObjectInfo<WorldRenderableObject> createDataObject( DataObjectInfo<RenderablePrototype> object ) {
        DataObjectInfo<WorldRenderableObject> result = new DataObjectInfo<WorldRenderableObject>(
                                                                                                  object.getUuid(),
                                                                                                  object.getType(),
                                                                                                  object.getName(),
                                                                                                  object.getExternalRef(),
                                                                                                  object.getEnvelope(),
                                                                                                  object.getData(),
                                                                                                  object.getTime() );
        result.setSerializedData( object.getSerializedData() );
        return result;
    }

    @Override
    public void write( ByteBuffer buffer, DataObjectInfo<RenderablePrototype> object ) {
        DataObjectInfo<WorldRenderableObject> doi = createDataObject( object );
        wroSerializer.write( buffer, doi );
        object.setSerializedData( doi.getSerializedData() );
    }

    @Override
    public byte[] serializeObject( DataObjectInfo<RenderablePrototype> object ) {
        DataObjectInfo<WorldRenderableObject> doi = createDataObject( object );
        wroSerializer.serializeObject( doi );
        object.setSerializedData( doi.getSerializedData() );
        return object.getSerializedData();
    }

    @Override
    public RenderablePrototype deserializeDataObject( byte[] buffer ) {
        WorldRenderableObject wro = wroSerializer.deserializeDataObject( buffer );
        return createRenderablePrototype( wro );
    }

}
