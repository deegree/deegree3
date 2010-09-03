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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.deegree.commons.utils.nio.BufferSerializer;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.model.geometry.SimpleGeometryStyle;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.DirectGeometryBuffer;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableGeometry;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableQualityModel;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableQualityModelPart;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.RenderableTexturedGeometry;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.rendering.r3d.opengl.rendering.model.prototype.PrototypeReference;
import org.deegree.services.wpvs.io.DataObjectInfo;

/**
 * The <code>WROSerializer</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class WROSerializer extends ObjectSerializer<WorldRenderableObject> {
    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( WROSerializer.class );

    private static final int QL_PROTO = 1;

    private static final int QL_GEOMETRIES = 2;

    private static final int RENDERABLE_TEXTURED_GEOM = 1;

    private static final int RENDERABLE_PLAIN_GEOM = 2;

    private static final int NULL_VALUE = Integer.MAX_VALUE;

    private DirectGeometryBuffer geometryBuffer = null;

    /**
     * The <code>VertexType</code> simple enum for vertex definitions
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author$
     * @version $Revision$, $Date$
     * 
     */
    private enum VertexType {
        /**
         *
         */
        Normals,
        /**
         *
         */
        TextureCoordinates,
        /**
         *
         */
        VertexCoordinates;
    }

    /**
     * @return the geometryBuffer
     */
    public final DirectGeometryBuffer getGeometryBuffer() {
        return geometryBuffer;
    }

    /**
     * @param geometryBuffer
     *            the geometryBuffer to set
     */
    public final void setGeometryBuffer( DirectGeometryBuffer geometryBuffer ) {
        this.geometryBuffer = geometryBuffer;
    }

    @Override
    public WorldRenderableObject read( ByteBuffer buffer ) {
        int objectSize = super.readObjectSize( buffer );
        skipHeader( buffer );
        byte[] ob = new byte[objectSize];
        ByteBuffer wrapper = ByteBuffer.wrap( ob );
        int ol = buffer.limit();
        int pos = buffer.position();
        buffer.limit( pos + objectSize );
        wrapper.put( buffer );
        buffer.limit( ol );
        // for ( int i = 0; i < ob.length; ++i ) {
        // ob[i] = buffer.get();
        // }
        WorldRenderableObject result = deserializeDataObject( ob );
        return result;
    }

    @Override
    public int serializedObjectSize( DataObjectInfo<WorldRenderableObject> object ) {
        int result = 0;
        if ( object.getSerializedData() != null ) {
            result = object.getSerializedData().length;
        } else {
            byte[] data = serializeObject( object );
            if ( data != null ) {
                result = data.length;
                object.setSerializedData( data );
            }

        }
        // the wro and the envelope
        return result;
    }

    @Override
    public void write( ByteBuffer buffer, DataObjectInfo<WorldRenderableObject> object ) {
        writeHeader( buffer, object );
        byte[] data = null;
        if ( object.getSerializedData() == null ) {
            data = serializeObject( object );
            if ( data != null ) {
                object.setSerializedData( data );
            }
        } else {
            data = object.getSerializedData();
        }
        if ( data != null ) {
            buffer.put( data );
        }
    }

    private void writeEnvelope( DataOutputStream out, Envelope env )
                            throws IOException {
        double[] min = env.getMin().getAsArray();
        double[] max = env.getMax().getAsArray();
        out.writeDouble( min[0] );
        out.writeDouble( min[1] );
        out.writeDouble( min[2] );
        out.writeDouble( max[0] );
        out.writeDouble( max[1] );
        out.writeDouble( max[2] );
    }

    /**
     * @param buffer
     * @return
     * @throws IOException
     */
    private Envelope readEnvelope( DataInputStream in )
                            throws IOException {
        double[] min = new double[3];
        double[] max = new double[3];
        min[0] = in.readDouble();
        min[1] = in.readDouble();
        min[2] = in.readDouble();

        max[0] = in.readDouble();
        max[1] = in.readDouble();
        max[2] = in.readDouble();
        return geomFac.createEnvelope( min, max, null );
    }

    /**
     * Serializes an object using the standard serialization mechanism, {@link ObjectOutputStream}
     * 
     * @param doi
     *            to be serialized with a {@link DataOutputStream}
     * @return the byte array containing the serialized object.
     */
    @Override
    public byte[] serializeObject( DataObjectInfo<WorldRenderableObject> doi ) {
        WorldRenderableObject dm = doi.getData();
        byte[] result = null;
        if ( dm != null ) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try {
                DataOutputStream output = new DataOutputStream( baos );
                write( dm, output );
                output.close();
                baos.close();
            } catch ( IOException e ) {
                LOG.error( "Error while serializing object because: " + e.getLocalizedMessage(), e );
            }
            result = baos.toByteArray();
            doi.setSerializedData( result );
        }
        return result;
    }

    /**
     * Deserialize an object from the given byte array.
     * 
     * @param buffer
     *            containing bytes to deserialize.
     * @return the deserialized object of type T.
     */
    @Override
    @SuppressWarnings("unchecked")
    public WorldRenderableObject deserializeDataObject( byte[] buffer ) {
        WorldRenderableObject result = null;
        if ( buffer != null ) {
            try {
                DataInputStream in = new DataInputStream( new ByteArrayInputStream( buffer ) );
                result = readWRO( in );
            } catch ( ClassCastException e ) {
                LOG.error( "Error while deserializing object because: " + e.getLocalizedMessage(), e );
            } catch ( IOException e ) {
                LOG.error( "Error while deserializing object because: " + e.getLocalizedMessage(), e );
            } catch ( Throwable e ) {
                LOG.error( "Error while deserializing object because: " + e.getLocalizedMessage(), e );
            }
        }
        return result;

    }

    /**
     * @param in
     * @return
     * @throws IOException
     */
    private WorldRenderableObject readWRO( DataInputStream in )
                            throws IOException {
        Envelope env = readEnvelope( in );
        WorldRenderableObject result = null;
        int numberOfQL = in.readInt();
        if ( numberOfQL != -1 ) {
            RenderableQualityModel[] qm = new RenderableQualityModel[numberOfQL];
            for ( int i = 0; i < numberOfQL; ++i ) {
                qm[i] = readQualityModel( in );
            }
            result = new WorldRenderableObject( null, null, env, qm );
        }
        return result;
    }

    /**
     * Method called while serializing this object
     * 
     * @param out
     *            to write to.
     * @throws IOException
     */
    private void write( WorldRenderableObject wro, DataOutputStream out )
                            throws IOException {
        Envelope env = wro.getBbox();
        writeEnvelope( out, env );
        int numberOfQL = wro.getNumberOfQualityLevels();
        if ( numberOfQL > 0 ) {
            out.writeInt( numberOfQL );
            for ( int i = 0; i < numberOfQL; ++i ) {
                RenderableQualityModel rqm = wro.getQualityLevel( i );
                if ( rqm != null ) {
                    // was this qualitymodel not null?
                    out.writeInt( i );
                    write( rqm, out );
                } else {
                    out.writeInt( -1 );
                }
            }
        } else {
            out.write( -1 );
        }
    }

    /**
     * @param rqm
     * @param out
     * @throws IOException
     */
    private void write( RenderableQualityModel rqm, DataOutputStream out )
                            throws IOException {
        if ( rqm.getPrototypeReference() != null ) {
            out.writeInt( QL_PROTO );
            write( rqm.getPrototypeReference(), out );
        } else {
            out.writeInt( QL_GEOMETRIES );
            ArrayList<RenderableQualityModelPart> qmParts = rqm.getQualityModelParts();
            if ( qmParts != null && !qmParts.isEmpty() ) {
                int size = qmParts.size();
                out.writeInt( size );
                for ( RenderableQualityModelPart rqmp : rqm.getQualityModelParts() ) {
                    write( rqmp, out );
                }
            } else {
                out.write( -1 );
            }

        }
    }

    /**
     * @param in
     * @return
     * @throws IOException
     */
    private RenderableQualityModel readQualityModel( DataInputStream in )
                            throws IOException {
        int rqNull = in.readInt();
        RenderableQualityModel result = null;
        if ( rqNull != -1 ) {
            int qlType = in.readInt();
            if ( qlType == QL_PROTO ) {
                PrototypeReference pr = readPrototypeReference( in );
                result = new RenderableQualityModel( pr );
            } else {
                int size = in.readInt();
                if ( size != -1 ) {
                    ArrayList<RenderableQualityModelPart> qmps = new ArrayList<RenderableQualityModelPart>( size );
                    for ( int i = 0; i < size; ++i ) {
                        RenderableQualityModelPart p = readRenderableQMPart( in );
                        if ( p != null ) {
                            qmps.add( p );
                        }

                    }
                    result = new RenderableQualityModel( qmps );
                }
            }
        }
        return result;

    }

    /**
     * @param prototypeReference
     * @param out
     * @throws IOException
     */
    private void write( PrototypeReference prototypeReference, DataOutputStream out )
                            throws IOException {
        out.writeUTF( prototypeReference.getPrototypeID() );
        out.writeFloat( prototypeReference.getWidth() );
        out.writeFloat( prototypeReference.getHeight() );
        out.writeFloat( prototypeReference.getDepth() );
        out.writeFloat( prototypeReference.getAngle() );
        float[] location = prototypeReference.getLocation();
        out.writeFloat( location[0] );
        out.writeFloat( location[1] );
        out.writeFloat( location[2] );
    }

    /**
     * @param in
     * @return
     * @throws IOException
     */
    private PrototypeReference readPrototypeReference( DataInputStream in )
                            throws IOException {
        String id = in.readUTF();
        float width = in.readFloat();
        float height = in.readFloat();
        float depth = in.readFloat();
        float angle = in.readFloat();
        float[] location = new float[3];
        location[0] = in.readFloat();
        location[1] = in.readFloat();
        location[2] = in.readFloat();
        return new PrototypeReference( id, angle, location, width, height, depth );
    }

    /**
     * @param rqmp
     * @param out
     * @throws IOException
     */
    private void write( RenderableQualityModelPart rqmp, DataOutputStream out )
                            throws IOException {
        if ( rqmp != null ) {
            if ( rqmp instanceof RenderableTexturedGeometry ) {
                out.writeInt( RENDERABLE_TEXTURED_GEOM );
                writeTexturedGeometry( ( (RenderableTexturedGeometry) rqmp ), out );
            } else {
                out.writeInt( RENDERABLE_PLAIN_GEOM );
                writePlainGeometry( ( (RenderableGeometry) rqmp ), out );
            }
        } else {
            out.writeInt( NULL_VALUE );
        }
    }

    /**
     * @param in
     * @return
     * @throws IOException
     */
    private RenderableQualityModelPart readRenderableQMPart( DataInputStream in )
                            throws IOException {
        int type = in.readInt();
        RenderableQualityModelPart result = null;
        if ( type != NULL_VALUE ) {
            // read the style
            SimpleGeometryStyle style = readStyle( in );
            int glType = in.readInt();

            // read the buffers.
            FloatBuffer coordBuffer = null;
            int[] coordPositions = new int[2];
            if ( geometryBuffer != null ) {
                coordPositions = geometryBuffer.readCoordinates( in );
            } else {
                coordBuffer = readFloatBuffer( in );
            }
            FloatBuffer normalBuffer = null;
            int[] normalPositions = new int[2];
            if ( geometryBuffer != null ) {
                normalPositions = geometryBuffer.readNormals( in );
            } else {
                normalBuffer = readFloatBuffer( in );
            }
            if ( type == RENDERABLE_TEXTURED_GEOM ) {
                String textureID = in.readUTF();
                if ( geometryBuffer != null ) {
                    int[] texPos = geometryBuffer.readTextureOrdinates( in );
                    result = new RenderableTexturedGeometry( coordPositions[0], ( coordPositions[1] / 3 ), glType,
                                                             normalPositions[0], style, textureID, texPos[0] );
                } else {
                    FloatBuffer textureBuffer = readFloatBuffer( in );
                    result = new RenderableTexturedGeometry( coordBuffer, glType, normalBuffer, style, textureID,
                                                             textureBuffer );
                }
            } else {

                if ( geometryBuffer != null ) {
                    result = new RenderableGeometry( coordPositions[0], ( coordPositions[1] / 3 ), glType,
                                                     normalPositions[0], style );
                } else {
                    result = new RenderableGeometry( coordBuffer, glType, normalBuffer, style );
                }
            }
        }
        return result;
    }

    /**
     * @param style
     * @param out
     * @throws IOException
     */
    private void writeStyle( SimpleGeometryStyle style, DataOutputStream out )
                            throws IOException {
        if ( style != null ) {
            out.writeInt( style.getAmbientColor() );
            out.writeInt( style.getDiffuseColor() );
            out.writeInt( style.getSpecularColor() );
            out.writeInt( style.getEmmisiveColor() );
            out.writeFloat( style.getShininess() );
        } else {
            out.writeInt( NULL_VALUE );
        }
    }

    /**
     * @param in
     * @return
     * @throws IOException
     */
    private SimpleGeometryStyle readStyle( DataInputStream in )
                            throws IOException {
        int ambientColor = in.readInt();
        if ( ambientColor == NULL_VALUE ) {
            return null;
        }
        int diffuseColor = in.readInt();
        int specularColor = in.readInt();
        int emmisiveColor = in.readInt();
        float shini = in.readFloat();

        return new SimpleGeometryStyle( specularColor, ambientColor, diffuseColor, emmisiveColor, shini );
    }

    /**
     * @param renderableGeometry
     * @param out
     * @throws IOException
     */
    private void writePlainGeometry( RenderableGeometry renderableGeometry, DataOutputStream out )
                            throws IOException {
        writeStyle( renderableGeometry.getStyle(), out );

        out.writeInt( renderableGeometry.getOpenGLType() );
        // coords
        FloatBuffer fb = getBuffer( renderableGeometry.getCoordBuffer(), renderableGeometry.getCoordPosition(),
                                    renderableGeometry.getOrdinateCount(), VertexType.VertexCoordinates );
        writeFloatBuffer( fb, out, VertexType.VertexCoordinates );
        // normals
        fb = getBuffer( renderableGeometry.getNormalBuffer(), renderableGeometry.getNormalPosition(),
                        renderableGeometry.getOrdinateCount(), VertexType.Normals );
        writeFloatBuffer( fb, out, VertexType.Normals );
    }

    /**
     * @param renderableTexturedGeometry
     * @param out
     * @throws IOException
     */
    private void writeTexturedGeometry( RenderableTexturedGeometry renderableTexturedGeometry, DataOutputStream out )
                            throws IOException {
        writePlainGeometry( renderableTexturedGeometry, out );
        out.writeUTF( renderableTexturedGeometry.getTexture() );
        FloatBuffer fb = getBuffer( renderableTexturedGeometry.getTextureCoordinates(),
                                    renderableTexturedGeometry.getTexturePosition(),
                                    renderableTexturedGeometry.getTextureOrdinateCount(), VertexType.TextureCoordinates );
        writeFloatBuffer( fb, out, VertexType.TextureCoordinates );
    }

    private FloatBuffer getBuffer( FloatBuffer buffer, int position, int limit, VertexType type ) {
        if ( buffer == null ) {
            if ( geometryBuffer == null ) {
                LOG.warn( "The given buffer is null and the geometry buffer was not set; the geometry has no " + type );
            } else {
                if ( position == -1 ) {
                    LOG.warn( "The given geometry does not have a direct buffer position and no buffer; the geometry has no "
                              + type );
                } else {
                    buffer = geometryBuffer.getCoords( position, limit );
                }
            }
        }
        return buffer;
    }

    private void writeFloatBuffer( FloatBuffer buffer, DataOutputStream out, VertexType type )
                            throws IOException {
        if ( buffer == null ) {
            LOG.warn( "The given geometry does not have a buffer to write; the geometry has no: " + type );
        }
        BufferSerializer.writeBufferToStream( buffer, out );

    }

    /**
     * Read the floatbuffers from the dataInputStream
     * 
     * @param in
     * @return
     * @throws IOException
     */
    private FloatBuffer readFloatBuffer( DataInputStream in )
                            throws IOException {
        FloatBuffer result = null;
        int numberOfValues = in.readInt();
        if ( numberOfValues != -1 ) {
            result = FloatBuffer.allocate( numberOfValues );
            for ( int i = 0; i < numberOfValues; ++i ) {
                try {
                    result.put( in.readFloat() );
                } catch ( IOException e ) {
                    result.clear();
                    throw e;
                }
            }
            result.rewind();
        }
        return result;
    }

}
