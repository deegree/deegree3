//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-metadata/src/main/java/org/deegree/metadata/iso/persistence/ISOMetadataStoreProvider.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.metadata.iso.persistence.memory;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.protocol.csw.MetadataStoreException;

/**
 * {@link MetadataStore} implementation for accessing ISO 19115 records kept in memory.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: 30992 $, $Date: 2011-05-31 16:09:20 +0200 (Di, 31. Mai 2011) $
 */
public class ISOMemoryMetadataStore implements MetadataStore<ISORecord> {

    private StoredISORecords storedIsoRecords;

    private MetadataStoreTransaction activeTransaction = null;

    private URL transactionalDirectory;

    /**
     * 
     * @param recordDirectories
     *            never <code>null</code> but may be empty when no directories exists
     * @param transactionalDirectory
     *            directory to store inserted records, can be <code>null</code> if transactions are not allowed
     * @throws ResourceInitException
     */
    public ISOMemoryMetadataStore( List<URL> recordDirectories, URL transactionalDirectory )
                            throws ResourceInitException {
        this.transactionalDirectory = transactionalDirectory;
        storedIsoRecords = new StoredISORecords( recordDirectories );
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public void init( DeegreeWorkspace deegreeWorkspace )
                            throws ResourceInitException {
        // nothing to do
    }

    @Override
    public MetadataResultSet<ISORecord> getRecords( MetadataQuery query )
                            throws MetadataStoreException {
        try {
            return storedIsoRecords.getRecords( query );
        } catch ( FilterEvaluationException e ) {
            throw new MetadataStoreException( e );
        }
    }

    @Override
    public int getRecordCount( MetadataQuery query )
                            throws MetadataStoreException {
        try {
            List<ISORecord> records = storedIsoRecords.getRecords( query.getFilter() );
            return records.size();
        } catch ( FilterEvaluationException e ) {
            throw new MetadataStoreException( e );
        }
    }

    @Override
    public MetadataResultSet<ISORecord> getRecordById( List<String> idList, QName[] recordTypeNames )
                            throws MetadataStoreException {
        return storedIsoRecords.getRecordById( idList, recordTypeNames );
    }

    @Override
    public MetadataStoreTransaction acquireTransaction()
                            throws MetadataStoreException {
        // only one transaction per time is accepted!
        while ( activeTransaction != null ) {
            // wait until active transaction is released!
        }
        activeTransaction = new ISOMemoryMetadataStoreTransaction( this, storedIsoRecords, transactionalDirectory );
        return activeTransaction;
    }

    /**
     * @returns <code>null</code>, cause no JDBC connection is required
     */
    @Override
    public String getConnId() {
        return null;
    }

    @Override
    public String getType() {
        return "iso";
    }

    /**
     * Release the active transaction if existing.
     */
    public void releaseTransaction() {
        activeTransaction = null;
    }

}
