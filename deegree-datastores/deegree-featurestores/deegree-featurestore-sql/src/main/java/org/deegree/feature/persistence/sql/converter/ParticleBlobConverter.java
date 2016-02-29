package org.deegree.feature.persistence.sql.converter;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.apache.xerces.xs.XSElementDeclaration;
import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.GMLReferenceResolver;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.blob.BlobCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ParticleConverter} for particles that are mapped to BLOB columns.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.4
 */
public class ParticleBlobConverter implements ParticleConverter<TypedObjectNode> {

    private static final Logger LOG = LoggerFactory.getLogger( ParticleBlobConverter.class );

    private final SQLIdentifier column;

    private final XSElementDeclaration elementDecl;

    private final SQLFeatureStore fs;

    private final GMLReferenceResolver resolver;

    private final BlobCodec codec;

    private final ICRS crs;

    /**
     * Creates a new {@link ParticleBlobConverter} instance.
     * 
     * @param column
     *            name of the BLOB column, must not be <code>null</code>
     * @param elementDecl
     *            declaration of the element stored in the BLOB, must not be <code>null</code>
     * @param fs
     *            feature store instance, must not be <code>null</code>
     * @param resolver
     *            resolver used for resolving xlinks, must not be <code>null</code>
     * @param codec
     *            codec for encoding/decoding, must not be <code>null</code>
     * @param crs
     *            coordinate reference system, can be <code>null</code>
     */
    public ParticleBlobConverter( final SQLIdentifier column, final XSElementDeclaration elementDecl,
                                  final SQLFeatureStore fs, final GMLReferenceResolver resolver, final BlobCodec codec,
                                  final ICRS crs ) {
        this.column = column;
        this.elementDecl = elementDecl;
        this.fs = fs;
        this.resolver = resolver;
        this.codec = codec;
        this.crs = crs;
    }

    @Override
    public String getSelectSnippet( final String tableAlias ) {
        if ( tableAlias != null ) {
            return tableAlias + "." + column.getName();
        }
        return column.getName();
    }

    @Override
    public TypedObjectNode toParticle( final ResultSet rs, final int colIndex )
                            throws SQLException {
        final byte[] bytes = rs.getBytes( colIndex );
        if ( bytes == null ) {
            return null;
        }
        try {
            FileUtils.writeByteArrayToFile( new File( "/tmp/blob.xml" ), bytes );
        } catch ( IOException e1 ) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        final InputStream is = new ByteArrayInputStream( bytes );
        try {
            return codec.decode( is, fs.getNamespaceContext(), fs.getSchema(), crs, resolver, elementDecl );
        } catch ( final Exception e ) {
            throw new SQLException( e.getMessage(), e );
        } finally {
            closeQuietly( is );
        }
    }

    @Override
    public String getSetSnippet( final TypedObjectNode particle ) {
        return "?";
    }

    @Override
    public void setParticle( final PreparedStatement stmt, final TypedObjectNode particle, final int paramIndex )
                            throws SQLException {
        byte[] bytes = null;
        if ( particle != null ) {
            bytes = encodeBlob( particle );
        }
        stmt.setBytes( paramIndex, bytes );
    }

    private byte[] encodeBlob( final TypedObjectNode particle )
                            throws SQLException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            codec.encode( particle, fs.getNamespaceContext(), bos, crs );
        } catch ( Exception e ) {
            final String msg = "Error encoding particle to BLOB: " + e.getMessage();
            LOG.trace( msg );
            LOG.trace( "Stack trace:", e );
            throw new SQLException( msg, e );
        }
        return bos.toByteArray();
    }

}
