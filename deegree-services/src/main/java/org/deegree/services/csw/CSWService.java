//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.services.csw;

import static org.deegree.services.i18n.Messages.get;

import java.util.HashSet;
import java.util.Set;

import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.MetadataStoreManager;
import org.deegree.services.jaxb.csw.ServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specifies the Service-Layer. <br>
 * Initial class to register all configured {@link MetadataStore}s that are specified in the Service-Configuration.
 * 
 * 
 * @author <a href="mailto:thomas@deegree.org">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class CSWService {

    private final Set<MetadataStore> recordStore = new HashSet<MetadataStore>();

    private static final Logger LOG = LoggerFactory.getLogger( CSWService.class );

    /**
     * Creates a {@link CSWService} instance to get a binding to the configuration.
     * 
     * @param sc
     *            the serviceConfiguration that is specified in the configuration.xml document
     * @throws MetadataStoreException
     */
    public CSWService( ServiceConfiguration sc, String baseURL ) throws MetadataStoreException {

        LOG.info( "Initializing/looking up configured record stores." );

        for ( MetadataStore rs : MetadataStoreManager.getAll().values() ) {
            addToStore( rs );
        }
    }

    /**
     * Registers a new {@link MetadataStore} to the CSW.
     * 
     * @param rs
     *            store to be registered
     */
    public void addToStore( MetadataStore rs ) {
        synchronized ( this ) {
            if ( recordStore.contains( rs ) ) {
                String msg = get( "CSW_RECORDSTORE_ALREADY_REGISTERED", rs );
                LOG.error( msg );
                throw new IllegalArgumentException( msg );
            }

            recordStore.add( rs );
        }
    }

    /**
     * Unregisters the specified {@link MetadataStore} from the CSW.
     * 
     * @param rs
     *            store to be registered
     */
    public void removeStore( MetadataStore rs ) {
        synchronized ( this ) {
            // TODO
        }
    }

    public MetadataStore getStore() {
        if ( recordStore.isEmpty() ) {
            return null;
        }
        return recordStore.iterator().next();
    }

    /**
     * 
     * @return Set of type <Code>RecordStore<Code>
     */
    public Set<MetadataStore> getMetadataStore() {
        return recordStore;
    }
}
