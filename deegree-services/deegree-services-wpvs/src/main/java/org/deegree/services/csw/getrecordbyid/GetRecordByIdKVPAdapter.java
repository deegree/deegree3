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
package org.deegree.services.csw.getrecordbyid;

import static org.deegree.protocol.csw.CSWConstants.VERSION_202;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.deegree.protocol.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates the method for parsing a {@link GetRecordById} KVP request via Http-GET.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetRecordByIdKVPAdapter {
    private static final Logger LOG = LoggerFactory.getLogger( GetRecordByIdKVPAdapter.class );

    /**
     * Parses the {@link GetRecordById} kvp request and decides which version has to parse because of the requested
     * version
     * 
     * @param normalizedKVPParams
     *            that are requested containing all mandatory and optional parts regarding CSW spec
     * @return {@link GetRecordById}
     * @throws MetadataStoreException
     */
    public static GetRecordById parse( Map<String, String> normalizedKVPParams ) {
        Version version = Version.parseVersion( KVPUtils.getRequired( normalizedKVPParams, "VERSION" ) );
        GetRecordById result = null;

        if ( VERSION_202.equals( version ) ) {
            result = parse202( VERSION_202, normalizedKVPParams );

        } else {
            String msg = Messages.get( "UNSUPPORTED_VERSION", version, Version.getVersionsString( VERSION_202 ) );
            throw new InvalidParameterValueException( msg );
        }

        return result;
    }

    /**
     * Parses the {@link GetRecordById} request on the basis of CSW version 2.0.2
     * 
     * @param version202
     *            at is requested, 2.0.2
     * @param normalizedKVPParams
     *            that are requested containing all mandatory and optional parts regarding CSW spec
     * @return {@link GetRecordById}
     * @throws MetadataStoreException
     */
    private static GetRecordById parse202( Version version202, Map<String, String> normalizedKVPParams ) {

        // outputFormat (optional)
        String outputFormat = KVPUtils.getDefault( normalizedKVPParams, "outputFormat", "application/xml" );

        String elementSetNameString = KVPUtils.getDefault( normalizedKVPParams, "ELEMENTSETNAME",
                                                           ReturnableElement.summary.name() );

        ReturnableElement elementSetName = ReturnableElement.determineReturnableElement( elementSetNameString );

        // outputSchema String
        String outputSchemaString = KVPUtils.getDefault( normalizedKVPParams, "OUTPUTSCHEMA",
                                                         "http://www.opengis.net/cat/csw/2.0.2" );
        URI outputSchema = URI.create( outputSchemaString );

        // elementName List<String>
        List<String> id = KVPUtils.splitAll( normalizedKVPParams, "ID" );
        if ( id.size() == 0 ) {
            String msg = "No ID provided, please check the mandatory element 'id'. ";
            LOG.info( msg );
            throw new MissingParameterException( msg );
        }

        return new GetRecordById( version202, outputFormat, elementSetName, outputSchema, id );
    }

}
