package org.deegree.metadata.persistence.genericmetadatastore;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.publication.DeleteTransaction;
import org.deegree.metadata.publication.InsertTransaction;
import org.deegree.metadata.publication.UpdateTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MetadataStoreException} implementation for the {@link ISOMetadataStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ISOMetadataStoreTransaction implements MetadataStoreTransaction {

    private static final Logger LOG = LoggerFactory.getLogger( ISOMetadataStoreTransaction.class );

    private final Connection conn;

    ISOMetadataStoreTransaction( Connection conn ) throws SQLException {
        this.conn = conn;
        conn.setAutoCommit( false );
    }

    @Override
    public void commit()
                            throws MetadataStoreException {
        LOG.debug( "Committing transaction." );
        try {
            conn.commit();
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            LOG.debug( e.getMessage(), e.getNextException() );
            throw new MetadataStoreException( "Unable to commit SQL transaction: " + e.getMessage() );
        }
    }

    @Override
    public int performDelete( DeleteTransaction delete )
                            throws MetadataStoreException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<String> performInsert( InsertTransaction insert )
                            throws MetadataStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int performUpdate( UpdateTransaction update )
                            throws MetadataStoreException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void rollback()
                            throws MetadataStoreException {
        LOG.debug( "Performing rollback of transaction." );
        try {
            conn.rollback();
        } catch ( SQLException e ) {
            LOG.debug( e.getMessage(), e );
            throw new MetadataStoreException( "Unable to rollback SQL transaction: " + e.getMessage() );
        }
    }
}