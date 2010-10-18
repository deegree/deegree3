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
package org.deegree.metadata.persistence.iso.parsing.inspectation;

import static org.deegree.commons.utils.JDBCUtils.close;
import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig.CoupledResourceInspector;
import org.slf4j.Logger;

/**
 * Inspects the coupling of data-metadata and service-metadata.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CoupledDataInspector implements RecordInspector {

    private static final Logger LOG = getLogger( CoupledDataInspector.class );

    private final Connection conn;

    private final CoupledResourceInspector ci;

    private CoupledDataInspector( CoupledResourceInspector ci, Connection conn ) {
        this.conn = conn;
        this.ci = ci;
    }

    public static CoupledDataInspector newInstance( CoupledResourceInspector ci, Connection conn ) {
        return new CoupledDataInspector( ci, conn );
    }

    /**
     * 
     * @param operatesOnList
     * @param operatesOnIdentifierList
     * @return
     * @throws MetadataStoreException
     */
    public boolean determineTightlyCoupled( List<String> operatesOnList, List<String> operatesOnIdentifierList )
                            throws MetadataStoreException {
        consistencyCheck( operatesOnList );
        boolean isTightlyCoupled = false;
        // TODO please more efficiency and intelligence
        for ( String operatesOnString : operatesOnList ) {

            for ( String operatesOnIdentifierString : operatesOnIdentifierList ) {

                if ( operatesOnString.equals( operatesOnIdentifierString ) ) {
                    isTightlyCoupled = true;
                    break;
                }
                isTightlyCoupled = false;

            }
            // OperatesOnList [a,b,c] - OperatesOnIdList [b,c,d] -> a not in OperatesOnIdList ->
            // inconsistency
            if ( isTightlyCoupled == false ) {

                String msg = "Missmatch between OperatesOn '" + operatesOnString
                             + "' and its tightly coupled resource OperatesOnIdentifier. ";
                LOG.info( msg );
                throw new MetadataStoreException( msg );

                // there is no possibility to set the operationName -> not able to set the coupledResource

            }

        }
        // OperatesOnList [] - OperatesOnIdList [a,b,c] -> inconsistency
        if ( isTightlyCoupled == false && operatesOnIdentifierList.size() != 0 ) {

            String msg = "Missmatch between OperatesOn and its tightly coupled resource OperatesOnIdentifier. ";
            LOG.info( msg );
            throw new MetadataStoreException( msg );
        }

        return isTightlyCoupled;
    }

    private void consistencyCheck( List<String> operatesOnList )
                            throws MetadataStoreException {
        if ( ci.isThrowConsistencyError() ) {
            for ( String operatesOnString : operatesOnList ) {
                if ( !getCoupledDataMetadatasets( operatesOnString ) ) {
                    String msg = "No resourceIdentifier "
                                 + operatesOnString
                                 + " found in the data metadata. So there is no coupling possible and an exception has to be thrown in conformity with configuration. ";
                    LOG.info( msg );
                    throw new MetadataStoreException( msg );
                }
            }
        }

    }

    /**
     * If there is a data metadata record available for the service metadata record.
     * 
     * @param resourceIdentifierList
     * @return
     * @throws MetadataStoreException
     */
    private boolean getCoupledDataMetadatasets( String resourceIdentifier )
                            throws MetadataStoreException {

        boolean gotOneDataset = false;
        ResultSet rs = null;
        PreparedStatement stm = null;
        String s = "SELECT resourceidentifier FROM isoqp_resourceidentifier WHERE resourceidentifier = ?;";

        try {
            stm = conn.prepareStatement( s );
            stm.setObject( 1, resourceIdentifier );
            rs = stm.executeQuery();
            while ( rs.next() ) {
                gotOneDataset = true;
            }
        } catch ( SQLException e ) {
            LOG.debug( "Error while proving the ID for the coupled resources: {}", e.getMessage() );
            throw new MetadataStoreException( "Error while proving the ID for the coupled resources: {}"
                                              + e.getMessage() );
        } finally {
            close( rs );
            close( stm );
        }

        return gotOneDataset;
    }

    @Override
    public OMElement inspect( OMElement record )
                            throws MetadataStoreException {
        // TODO Auto-generated method stub
        return null;
    }

}
