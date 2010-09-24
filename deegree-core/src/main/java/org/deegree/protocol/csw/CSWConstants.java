//$HeadURL: svn+ssh://sthomas@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.protocol.csw;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.ows.Version;
import org.deegree.metadata.persistence.MetadataStoreException;

/**
 * 
 * Container for, in the specification defined, static specified elements
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public final class CSWConstants {

    /**
     * Namespace for elements from the CSW 2.0.2 specification <br>
     * Namespace="http://www.opengis.net/cat/csw/2.0.2"
     * */
    public static final String CSW_202_NS = "http://www.opengis.net/cat/csw/2.0.2";

    /**
     * ISO application profile <br>
     * "http://www.isotc211.org/2005/gmd"
     */
    public static final String ISO_19115_NS = "http://www.isotc211.org/2005/gmd";

    /**
     * DC application profile <br>
     * "http://purl.org/dc/elements/1.1/"
     */
    public static final String DC_NS = "http://purl.org/dc/elements/1.1/";

    /**
     * DCT application profile <br>
     * "http://purl.org/dc/terms/"
     */
    public static final String DCT_NS = "http://purl.org/dc/terms/";

    /**
     * LOCAL_PART = "dct"
     */
    public static final String DCT_PREFIX = "dct";

    /**
     * APISO application profile <br>
     * "http://www.opengis.net/cat/csw/apiso/1.0"
     */
    public static final String APISO_NS = "http://www.opengis.net/cat/csw/apiso/1.0";

    /**
     * Namespace for elements from the ISO AP 1.0 specification <br>
     * Namespace="http://www.isotc211.org/2005/gmd"
     * */
    public static final String GMD_NS = "http://www.isotc211.org/2005/gmd";

    /**
     * Location of the schema <br>
     * "http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd"
     */
    public static final String CSW_202_DISCOVERY_SCHEMA = "http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd";

    /**
     * Location of the schema <br>
     * "http://schemas.opengis.net/csw/2.0.2/CSW-publication.xsd"
     */
    public static final String CSW_202_PUBLICATION_SCHEMA = "http://schemas.opengis.net/csw/2.0.2/CSW-publication.xsd";

    /**
     * Location of the schema <br>
     * "http://schemas.opengis.net/csw/2.0.2/record.xsd"
     */
    public static final String CSW_202_RECORD = "http://schemas.opengis.net/csw/2.0.2/record.xsd";

    /** Common namespace prefix for elements from the CSW specification */
    public static final String CSW_PREFIX = "csw";

    /**
     * Common namespace prefix for elements from the ISO AP specification for the types "Dataset", "DatasetCollection"
     * and "Application"
     */
    public static final String GMD_PREFIX = "gmd";

    /** Common namespace prefix for elements from the ISO AP specification */
    public static final String APISO_PREFIX = "apiso";

    /**
     * Common local part of a qualified name for elements from the CSW specification <br>
     * LOCAL_PART = "Record"
     */
    public static final String DC_LOCAL_PART = "Record";

    /**
     * LOCAL_PART = "dc"
     */
    public static final String DC_PREFIX = "dc";

    /**
     * Common local part of a qualified name for elements from the ISO AP specification <br>
     * LOCAL_PART = "MD_Metadata"
     */
    public static final String GMD_LOCAL_PART = "MD_Metadata";

    /** CSW protocol version 2.0.2 */
    public static final Version VERSION_202 = Version.parseVersion( "2.0.2" );

    /**
     * 
     * Operations that is the webservice capable of <br>
     * <li>GetCapabilities</li> <li>DescribeRecord</li> <li>GetRecords</li> <li>GetRecordById</li><li>Transaction</li> <br>
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author: thomas $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum CSWRequestType {

        /** Retrieve the capabilities of the service. */
        GetCapabilities,
        /** Discover elements of the service */
        DescribeRecord,
        /** Resource discovery combines the two operations - search and present */
        GetRecords,
        /** Retrieve the default representation of the service */
        GetRecordById,
        /** Creates, modifys and deletes catalogue records */
        Transaction
    }

    /**
     * 
     * Sections are informations about the service represented in the GetCapabilities operation <br>
     * <li>ServiceIdentification</li> <li>ServiceProvider</li> <li>OperationsMetadata</li> <li>Filter_Capabilities</li> <br>
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author: thomas $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum Sections {

        /** Metadata about the CSW implementation */
        ServiceIdentification,
        /** Metadata about the organisation that provides the CSW implementation */
        ServiceProvider,
        /** Metadata about the operations provided by this CSW implementation */
        OperationsMetadata,
        /** Metadata about the filter capabilities that are implemented at this server */
        Filter_Capabilities
    }

    /**
     * Specifies the mode of the response that is requested. The modes are: <br>
     * <li>hits (default)</li> <li>results</li> <li>validate</li> <br>
     * 
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author: thomas $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum ResultType {

        /** returns an empty SearchResults element that include the size of the result set */
        hits,
        /** returns one or more records from the result set up to the maximum number of records specified in the request */
        results,
        /** validates the request message */
        validate;

        ResultType() {

        }

        public static ResultType determineResultType( String typeString ) {
            ResultType resultType = null;
            typeString = typeString.toLowerCase();
            if ( typeString.equalsIgnoreCase( ResultType.hits.name() ) ) {
                resultType = ResultType.hits;
            } else if ( typeString.equalsIgnoreCase( ResultType.results.name() ) ) {
                resultType = ResultType.results;
            } else if ( typeString.equalsIgnoreCase( ResultType.validate.name() ) ) {
                resultType = ResultType.validate;
            }

            return resultType;
        }

    }

    /**
     * 
     * Specifies the elements that should be returned in the response <br>
     * <li>brief</li> <li>summary</li> <li>full</li> <br>
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author: thomas $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum ReturnableElement {

        /**
         * Brief representation of a record. This is the shortest view of a record by a specific profile.
         */
        brief,

        /**
         * Summary representation of a record. This view responses all the elements that should be queryable by a
         * record-profile.
         */
        summary,
        /**
         * Full representation of a record. In that response there are all the elements represented that a record holds.
         * Thus, there are elements presented that are not queryable regarding to the CSW specification.
         */
        full;

        private ReturnableElement() {
            // TODO Auto-generated constructor stub
        }

        public static ReturnableElement determineReturnableElement( String returnableElement ) {
            ReturnableElement elementSetName = null;
            returnableElement = returnableElement.toLowerCase();
            if ( returnableElement.equalsIgnoreCase( ReturnableElement.brief.name() ) ) {
                elementSetName = ReturnableElement.brief;
            } else if ( returnableElement.equalsIgnoreCase( ReturnableElement.summary.name() ) ) {
                elementSetName = ReturnableElement.summary;
            } else if ( returnableElement.equalsIgnoreCase( ReturnableElement.full.name() ) ) {
                elementSetName = ReturnableElement.full;
            } else {
                elementSetName = ReturnableElement.summary;
            }
            return elementSetName;
        }

    }

    /**
     * 
     * Specifies in which filter mode the query has to be processed. Either there is a OGC XML filter encoding after the
     * filterspecification document <a href="http://www.opengeospatial.org/standards/filter">OGC 04-095</a> or there is
     * a common query language string (CqlText) which can be seen as an explicit typed statement like an SQL statement.
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author: thomas $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum ConstraintLanguage {

        /** Common Queryable Language Text filtering */
        CQLTEXT,

        /** Filterexpression specified in OGC Spec document 04-095 */
        FILTER

    }

    /**
     * 
     * Defined in the CSW-publication.xsd. Specifies the data manipulation operations <br>
     * <li>insert</li> <li>delete</li> <li>update</li> <br>
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author: thomas $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum TransactionType {

        /**
         * With the INSERT action of the transaction operation there can be inserted one or more records into the
         * backend.
         */
        INSERT,

        /**
         * With the DELETE action of the transaction operation there can be deleted specific records defined by a filter
         * expression.
         */
        DELETE,

        /**
         * With the UPDATE action of the transaction operation there can be updated one complete record or just
         * properties of specific records defined by a filter expression.
         */
        UPDATE

    }

    public enum OutputSchema {
        DC,

        ISO_19115;

        private OutputSchema() {

        }

        public static URI determineOutputSchema( OutputSchema outputSchema )
                                throws MetadataStoreException {
            URI schema = null;
            try {
                switch ( outputSchema ) {
                case DC:

                    schema = new URI( CSWConstants.CSW_202_NS );
                    break;
                case ISO_19115:
                    schema = new URI( CSWConstants.GMD_NS );
                    break;

                }
            } catch ( URISyntaxException e ) {
                throw new MetadataStoreException( e.getMessage() );
            }

            return schema;

        }

        public static OutputSchema determineByTypeName( QName typeName ) {
            String uri = typeName.getNamespaceURI();

            if ( uri.equals( CSW_202_NS ) ) {
                return DC;
            } else if ( uri.equals( GMD_NS ) ) {
                return ISO_19115;
            }

            return null;
        }

    }

}
