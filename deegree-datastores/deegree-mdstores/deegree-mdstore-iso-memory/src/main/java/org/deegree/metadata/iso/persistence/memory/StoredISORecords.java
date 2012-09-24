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

import java.io.File;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.config.ResourceInitException;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.MetadataRecordFactory;
import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.deegree.metadata.persistence.MetadataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates the {@link ISORecord}s stored by a {@link MetadataStore} instance.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: 30992 $, $Date: 2011-05-31 16:09:20 +0200 (Di, 31. Mai 2011) $
 */
public class StoredISORecords {

    private static final Logger LOG = LoggerFactory.getLogger( StoredISORecords.class );

    private final Map<String, ISORecord> fileIdentifierToRecord = new HashMap<String, ISORecord>();

    StoredISORecords() {
    }

    StoredISORecords( List<URL> recordDirectories ) throws ResourceInitException {
        addRecords( recordDirectories );
    }

    private void addRecords( List<URL> recordDirectories )
                            throws ResourceInitException {
        for ( URL url : recordDirectories ) {
            addRecords( url );
        }
    }

    private void addRecords( URL url )
                            throws ResourceInitException {
        try {
            File recordDirectory = new File( url.toURI() );
            File[] records = recordDirectory.listFiles( new FilenameFilter() {
                @Override
                public boolean accept( File dir, String name ) {
                    return name.endsWith( ".xml" );
                }
            } );
            for ( File record : records ) {
                addRecord( record );
            }
        } catch ( URISyntaxException e ) {
            throw new ResourceInitException( "Could not read records from " + url + ": " + e.getMessage(), e );
        }

    }

    private void addRecord( File recordFile ) {
        try {
            MetadataRecord record = MetadataRecordFactory.create( recordFile );
            if ( !( record instanceof ISORecord ) ) {
                LOG.warn( "Ignore record {}: is not a ISO19139 record.", recordFile.getName() );
                return;
            }
            fileIdentifierToRecord.put( record.getIdentifier(), (ISORecord) record );
        } catch ( Exception e ) {
            LOG.warn( "Ignore record {}: could not be parsed: {}.", recordFile.getName(), e.getMessage() );
        }
    }

    public void addRecord( ISORecord record ) {
        try {
            fileIdentifierToRecord.put( record.getIdentifier(), (ISORecord) record );
        } catch ( Exception e ) {
            LOG.warn( "Ignore record, could not be parsed: {}.", e.getMessage() );
        }
    }

    /**
     * Requests all records with the passed ids.
     * 
     * @param idList
     *            may be empty but never null never <code>null</code>
     * @param recordTypeNames 
     * @return the records with the passed ids.
     */
    public MetadataResultSet<ISORecord> getRecordById( List<String> idList, QName[] recordTypeNames ) {
        if ( idList == null ) {
            throw new IllegalArgumentException( "List with ids must not be null!" );
        }
        LOG.info( "Parameter recordTypeNames is currently not supported!" );
        List<ISORecord> foundRecords = new ArrayList<ISORecord>();
        for ( String id : idList ) {
            if ( fileIdentifierToRecord.containsKey( id ) ) {
                foundRecords.add( fileIdentifierToRecord.get( id ) );
            }
        }
        return new ListMetadataResultSet( foundRecords );
    }

}
