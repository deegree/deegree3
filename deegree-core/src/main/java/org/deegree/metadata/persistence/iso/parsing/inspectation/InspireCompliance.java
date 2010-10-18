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

import static org.slf4j.LoggerFactory.getLogger;

import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig.RequireInspireCompliance;
import org.slf4j.Logger;

/**
 * Inspects the INSPIRE compliance of the metadataset.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class InspireCompliance {

    private static final Logger LOG = getLogger( InspireCompliance.class );

    private final RequireInspireCompliance ric;

    private final Connection conn;

    private InspireCompliance( RequireInspireCompliance ric, Connection conn ) {
        this.ric = ric;
        this.conn = conn;
    }

    public static InspireCompliance newInstance( RequireInspireCompliance ric, Connection conn ) {
        return new InspireCompliance( ric, conn );
    }

    public boolean checkInspireCompliance() {
        if ( ric == null ) {
            return false;
        } else {
            return true;
        }
    }

    // /**
    // * Determines if the required constraint of the equality of the attribute
    // *
    // <Code>gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier</Code>
    // * and <Code>gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/@id</Code> is given.
    // *
    // * @param rsList
    // * the list of RS_Identifier, not <Code>null</Code>.
    // * @param id
    // * the id attribute if exists, can be <Code>null</Code>.
    // * @return a list of RS_Identifier, not <Code>null</Code> but empty, at least.
    // * @throws MetadataStoreException
    // */
    // public List<String> determineInspireCompliance( InspireType type )
    // throws MetadataStoreException {
    //
    // if ( checkInspireCompliance() ) {
    // boolean generateAutomatic = ric.isGenerateAutomatic();
    // if ( generateAutomatic == false ) {
    // if ( checkRSListAgainstID( rsList, id ) ) {
    // LOG.info( "The resourceIdentifier has been accepted." );
    // return rsList;
    // }
    // LOG.debug(
    // "There was no match between resourceIdentifier and the id-attribute! Without any automatic guarantee this metadata has to be rejected! "
    // );
    // throw new MetadataStoreException( "There was no match between resourceIdentifier and the id-attribute!" );
    // }
    // if ( checkRSListAgainstID( rsList, id ) ) {
    // LOG.info( "The resourceIdentifier has been accepted without any automatic creation. " );
    // return rsList;
    // }
    // /**
    // * if both, id and resourceIdentifier exists but different: update id with resourceIdentifier
    // * <p>
    // * if id exists: update resourceIdentifier with id
    // * <p>
    // * if resourceIdentifier exists: update id with resourceIdentifier
    // * <p>
    // * if nothing exists: generate it for id and resourceIdentifier
    // */
    // if ( rsList.size() == 0 && id == null ) {
    // LOG.info( "Neither an id nor a resourceIdentifier exists...so this creates a new one. " );
    // rsList.add( IdUtils.newInstance( conn ).generateUUID() );
    // return rsList;
    // } else if ( rsList.size() == 0 && id != null ) {
    // LOG.info( "An id exists but not a resourceIdentifier...so adapting resourceIdentifier with id. " );
    // rsList.add( id );
    // return rsList;
    // }
    // }
    // LOG.info( "No modification happened, so the resourceIdentifierList will be passed through. " );
    // return rsList;
    // }
    //
    // private boolean checkRSListAgainstID( List<String> rsList, String id ) {
    // if ( rsList.size() == 0 ) {
    // return false;
    // } else {
    // if ( checkUUIDCompliance( rsList.get( 0 ) ) ) {
    // if ( checkUUIDCompliance( id ) ) {
    // return rsList.get( 0 ).equals( id );
    // }
    // }
    //
    // }
    // return false;
    // }

    private boolean checkUUIDCompliance( String uuid ) {

        char firstChar = uuid.charAt( 0 );
        Pattern p = Pattern.compile( "[0-9]" );
        Matcher m = p.matcher( "" + firstChar );
        if ( m.matches() ) {
            return false;
        }
        return true;
    }

    public RequireInspireCompliance getRic() {
        return ric;
    }

}
