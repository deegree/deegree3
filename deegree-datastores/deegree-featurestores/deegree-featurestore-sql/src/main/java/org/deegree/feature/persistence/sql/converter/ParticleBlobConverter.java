package org.deegree.feature.persistence.sql.converter;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSTypeDefinition;
import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.gml.GMLReferenceResolver;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.blob.BlobCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParticleBlobConverter implements ParticleConverter<TypedObjectNode> {

    private static final Logger LOG = LoggerFactory.getLogger( ParticleBlobConverter.class );

    private final SQLIdentifier column;

    private final XSElementDeclaration elementDecl;

    private final SQLFeatureStore fs;

    private final GMLReferenceResolver resolver;

    private final BlobCodec codec;

    private final ICRS crs;

    public ParticleBlobConverter( final SQLIdentifier column, final XSElementDeclaration elementDecl, final SQLFeatureStore fs,
                                  final GMLReferenceResolver resolver, final BlobCodec codec, final ICRS crs ) {
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
            LOG.error( msg );
            LOG.trace( "Stack trace:", e );
            throw new SQLException( msg, e );
        }
        return bos.toByteArray();
    }

}
