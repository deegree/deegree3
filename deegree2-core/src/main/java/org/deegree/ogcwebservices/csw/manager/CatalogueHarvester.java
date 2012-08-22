//$HeadURL$
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
package org.deegree.ogcwebservices.csw.manager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.deegree.enterprise.WebUtils;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.FileUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.xml.XMLException;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.io.DBPoolException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.csw.configuration.CatalogueConfigurationDocument;
import org.deegree.ogcwebservices.csw.manager.HarvestRepository.Record;
import org.deegree.ogcwebservices.csw.manager.HarvestRepository.ResourceType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Harverster implementation for harvesting other catalogue services. Just dataset, series
 * (datasetcollection) und application metadatatypes will be harvested.
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class CatalogueHarvester extends AbstractHarvester {

    static final ILogger LOG = LoggerFactory.getLogger( CatalogueHarvester.class );

    private static CatalogueHarvester ch = null;

    private enum HarvestOperation {
        /**
         *
         */
        insert, /**
                 *
                 */
        update, /**
                 *
                 */
        delete, /**
                 *
                 */
        nothing
    }

    /**
     * @param version
     *            the version of the CSW
     */
    private CatalogueHarvester( String version ) {
        super( version );
    }

    /**
     * singelton
     * 
     * @param version
     *            the version of the CSW
     * 
     * @return instance of CatalogueHarvester
     */
    public static CatalogueHarvester getInstance( String version ) {
        if ( ch == null ) {
            ch = new CatalogueHarvester( version );
        }
        return ch;
    }

    @Override
    public void run() {
        LOG.logDebug( "starting harvest iteration for CatalogueHarvester." );
        try {
            HarvestRepository repository = HarvestRepository.getInstance();

            List<URI> sources = repository.getSources();
            for ( Iterator<URI> iter = sources.iterator(); iter.hasNext(); ) {
                URI source = iter.next();
                try {
                    // determine if source shall be harvested
                    if ( shallHarvest( source, ResourceType.catalogue ) ) {
                        // mark source as currently being harvested
                        inProgress.add( source );
                        HarvestProcessor processor = new HarvestProcessor( this, source );
                        processor.start();
                    }
                } catch ( Exception e ) {
                    e.printStackTrace();
                    LOG.logError( Messages.format( "CatalogueHarvester.exception1", source ), e );
                    informResponseHandlers( source, e );
                }
            }
        } catch ( Exception e ) {
            LOG.logError( Messages.getString( "CatalogueHarvester.exception2" ), e );
        }

    }

    /**
     * inner class for processing asynchronous harvesting of a catalogue
     * 
     * @version $Revision$
     * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
     * @author last edited by: $Author$
     * 
     * @version 1.0. $Revision$, $Date$
     * 
     * @since 2.0
     */
    protected class HarvestProcessor extends AbstractHarvestProcessor {

        private Map<String, Record> records = new HashMap<String, Record>( 10000 );

        private String sourceVersion = "2.0.0";

        /**
         * 
         * @param owner
         * @param source
         */
        HarvestProcessor( AbstractHarvester owner, URI source ) {
            super( owner, source );
            try {
                String capaRequest = source + "?REQUEST=GetCapabilities&service=CSW";
                CatalogueConfigurationDocument capa = new CatalogueConfigurationDocument();
                capa.load( new URL( capaRequest ) );
                List<String> versions = Arrays.asList( capa.getServiceIdentification().getServiceTypeVersions() );
                Collections.sort( versions );
                sourceVersion = versions.get( versions.size() - 1 );
            } catch ( IOException e ) {
                LOG.logError( Messages.format( "CatalogueHarvester.exception5", source ), e );
            } catch ( SAXException e ) {
                LOG.logError( Messages.format( "CatalogueHarvester.exception6", source ), e );
            } catch ( XMLParsingException e ) {
                LOG.logError( Messages.format( "CatalogueHarvester.exception7", source ), e );
            }
        }

        @Override
        public void run() {

            String[] typeNames = new String[] { "csw:dataset", "csw:datasetcollection", "csw:application",
                                               "csw:service" };
            records.clear();
            try {
                HarvestRepository repository = HarvestRepository.getInstance();
                XMLFragment metaData = null;
                Date harvestingTimestamp = repository.getNextHarvestingTimestamp( source );

                if ( "2.0.2".equals( sourceVersion ) ) {
                    runHarvest( "", metaData, repository );
                } else {
                    for ( int i = 0; i < typeNames.length; i++ ) {
                        runHarvest( typeNames[i], metaData, repository );
                    }
                }

                // delete all records from the target catalogue and the
                // from harvest cache
                deleteRecordsNoHostedAnymore( source );

                // update timestamps just if transaction has been performed
                // successfully
                writeLastHarvestingTimestamp( source, harvestingTimestamp );
                writeNextHarvestingTimestamp( source, harvestingTimestamp );
                // inform handlers assigend to the harvest request about successfully harvested
                // CSW. Even if harvesting a few records has failed s harvest process will
                // declared
                // as successfull if it cann be fineshed regulary
                informResponseHandlers( source );
                if ( repository.getHarvestInterval( source ) <= 0 ) {
                    repository.dropRequest( source );
                }
            } catch ( Exception e ) {
                LOG.logError( Messages.format( "CatalogueHarvester.exception4", source ), e );
                try {
                    e.printStackTrace();
                    owner.informResponseHandlers( source, e );
                } catch ( Exception ee ) {
                    ee.printStackTrace();
                }
            } finally {
                inProgress.remove( source );
            }

        }

        /**
         * 
         * @param typeName
         * @param metaData
         * @param repository
         * @throws XMLException
         * @throws IOException
         * @throws SAXException
         * @throws XMLParsingException
         */
        private void runHarvest( String typeName, XMLFragment metaData, HarvestRepository repository )
                                throws XMLException, IOException, SAXException, XMLParsingException {
            int index = 1;
            int hits = getNoOfMetadataRecord( source, typeName );
            LOG.logInfo( hits + " metadatasets to harvest ..." );
            for ( int j = 0; j < hits; j++ ) {
                try {
                    // read index'th metadata set from CSW
                    metaData = getNextMetadataRecord( source, index, typeName );
                    if ( metaData != null ) {
                        // read record from harvest database if dataset has been harvested
                        // before
                        // or create a new one
                        Record record = createOrGetRecord( source, metaData );
                        records.put( record.getFileIdentifier(), record );
                        String trans = null;
                        try {
                            // determine harvest operation to perfrom
                            // insert: dataset has not been harvested before
                            // update: dataset has been harvested before but has changed
                            // nothing: e.g. dataset is not a known metadata format
                            HarvestOperation ho = getHarvestOperation( record, metaData );
                            if ( ho == HarvestOperation.insert ) {
                                trans = createInsertRequest( metaData );
                            } else if ( ho == HarvestOperation.update ) {
                                trans = createUpdateRequest( getID( metaData ),
                                                             getIdentifierXPathForUpdate( metaData ), metaData );
                            }
                            // perform harvesting for current dataset; insert it or update
                            // extisting dataset in this CSW
                            if ( ho != HarvestOperation.nothing ) {
                                performTransaction( trans );
                                repository.storeRecord( record );
                            } else {
                                LOG.logInfo( "nothing to Harvest" );
                            }
                        } catch ( Throwable e ) {
                            LOG.logError( Messages.format( "CatalogueHarvester.exception3", index, getID( metaData ),
                                                           source ), e );
                            try {
                                // inform handlers assigend to the harvest request about
                                // failure
                                // harvesting one specifi dataset.
                                // notice:
                                // if harvisting one dataset fails, not the complete harvest
                                // process fails; the process gones on with next record
                                owner.informResponseHandlers( source, e );
                            } catch ( Exception ee ) {
                                ee.printStackTrace();
                            }
                            // remove fileIdentifier of current dataset from list of
                            // inserted
                            // or updated datasets. After process all available metadata
                            // records this list will be used to adjust the list of dataset
                            // assigend to a specific CSW in harvest-metadata db schema
                            records.remove( record.getFileIdentifier() );
                        }
                    } else {
                        LOG.logInfo( "harvesting will be stopped at index: " + index + " because metadata == null" );
                    }
                    LOG.logDebug( index + " metadata " + ( metaData == null ) );
                } catch ( Throwable e ) {
                    LOG.logError( Messages.format( "CatalogueHarvester.exception3", index, "not available", source ), e );
                    try {
                        e.printStackTrace();
                        // inform handlers assigend to the harvest request about failure
                        // harvesting one specific dataset.
                        // notice: if harvisting one dataset fails, not the complete harvest
                        // process fails; the process gones on with next record
                        owner.informResponseHandlers( source, e );
                    } catch ( Exception ee ) {
                        ee.printStackTrace();
                    }
                }
                index++;
                if ( index % 1000 == 0 ) {
                    System.gc();
                }

            }
        }

        /**
         * returns the XPath the metadata records dateStamp
         * 
         * @param metaData
         * @return the XPath the metadata records dateStamp
         */
        private String getDateStampXPath( XMLFragment metaData ) {
            String xpath = null;
            if ( metaData != null ) {
                String nspace = metaData.getRootElement().getNamespaceURI();
                nspace = StringTools.replace( nspace, "http://", "", true );
                xpath = Messages.getString( "dateStamp_" + nspace );
            }
            return xpath;
        }

        /**
         * returns the identifier of a metadata record to enable its update and deletion
         * 
         * @param metaData
         * @return the identifier of a metadata record to enable its update and deletion
         * @throws XMLParsingException
         */
        private String getID( XMLFragment metaData )
                                throws XMLParsingException {
            String xpath = getIdentifierXPath( metaData );
            String fileIdentifier = XMLTools.getRequiredNodeAsString( metaData.getRootElement(), xpath, nsc );
            return fileIdentifier;
        }

        @Override
        protected String createConstraint( String identifier, String xPath )
                                throws IOException {

            // read template from file
            URL url = Templates.getTemplate( "Constraints_" + version );
            String constraints = FileUtils.readTextFile( url ).toString();

            constraints = StringTools.replace( constraints, "$identifier$", identifier, false );
            return StringTools.replace( constraints, "$xPath$", xPath, false );
        }

        /**
         * validates if a record stored in the harvester cache if not provided by the harvested
         * catalogue any more; if so the record will be removed from the cache and the harvesting
         * catalogue.
         * 
         * @throws IOException
         * @throws SQLException
         * @throws DBPoolException
         * @throws SAXException
         * @throws OGCWebServiceException
         * 
         */
        private void deleteRecordsNoHostedAnymore( URI source )
                                throws DBPoolException, SQLException, IOException, OGCWebServiceException, SAXException {
            HarvestRepository repository = HarvestRepository.getInstance();
            List<String> cache = repository.getAllRecords( source );
            int id = repository.getSourceID( source );
            for ( int i = 0; i < cache.size(); i++ ) {
                String fid = cache.get( i );
                Record record = records.remove( fid );
                if ( record == null ) {
                    repository.dropRecord( repository.new Record( id, null, fid, source ) );
                    String trans = createDeleteRequest( fid );
                    performTransaction( trans );
                }
            }
        }

        /**
         * the method tries to read a record from the harvest repository. If the is not already
         * stored in the repository a new record will be created
         * 
         * @param metaData
         * @return record from harvest repository
         * @throws XMLParsingException
         * @throws IOException
         * @throws SQLException
         * @throws DBPoolException
         */
        private Record createOrGetRecord( URI source, XMLFragment metaData )
                                throws XMLParsingException, IOException, DBPoolException, SQLException {

            String xpath = getIdentifierXPath( metaData );
            String fileIdentifier = XMLTools.getRequiredNodeAsString( metaData.getRootElement(), xpath, nsc );

            HarvestRepository repository = HarvestRepository.getInstance();
            Record record = repository.getRecordByID( source, fileIdentifier );
            if ( record == null ) {
                xpath = getDateStampXPath( metaData );
                String s = XMLTools.getRequiredNodeAsString( metaData.getRootElement(), xpath, nsc );
                Date date = TimeTools.createCalendar( s ).getTime();
                record = repository.new Record( -1, date, fileIdentifier, source );
            }

            return record;
        }

        /**
         * determines what operation shall be performed on a metadata record read from a remote
         * catalogue
         * 
         * @param metaData
         * @return type of harvest operation to perform
         * @throws XMLParsingException
         */
        private HarvestOperation getHarvestOperation( Record record, XMLFragment metaData )
                                throws XMLParsingException {

            HarvestOperation ho = HarvestOperation.nothing;
            if ( record.getSourceId() < 0 ) {
                ho = HarvestOperation.insert;
            } else {
                String xpath = getDateStampXPath( metaData );
                String s = XMLTools.getRequiredNodeAsString( metaData.getRootElement(), xpath, nsc );
                Date date = TimeTools.createCalendar( s ).getTime();
                if ( !date.equals( record.getDatestamp() ) ) {
                    ho = HarvestOperation.update;
                }
            }
            return ho;
        }

        /**
         * read
         * 
         * @param source
         * @return Metadata record
         * @throws IOException
         * @throws HttpException
         * @throws SAXException
         * @throws XMLException
         * @throws XMLParsingException
         */
        private XMLFragment getNextMetadataRecord( URI source, int index, String type )
                                throws IOException, XMLException, SAXException, XMLParsingException {

            // read template from file
            URL url = Templates.getTemplate( "GetRecords_" + sourceVersion );
            String getRecords = FileUtils.readTextFile( url ).toString();
            getRecords = StringTools.replace( getRecords, "$index$", Integer.toString( index ), false );
            getRecords = StringTools.replace( getRecords, "$type$", type, false );

            StringRequestEntity re = new StringRequestEntity( getRecords, "text/xml", CharsetUtils.getSystemCharset() );
            PostMethod post = new PostMethod( source.toASCIIString() );
            post.setRequestEntity( re );
            HttpClient client = new HttpClient();
            int timeout = 30000;
            try {
                timeout = Integer.parseInt( Messages.getString( "harvest.source.timeout" ) );
            } catch ( Exception e ) {
                LOG.logInfo( "can not read timeout from messages.properties because: " + e.getMessage()
                             + "; use 30 sec as default" );
            }
            client.getHttpConnectionManager().getParams().setSoTimeout( timeout );
            client = WebUtils.enableProxyUsage( client, source.toURL() );
            client.executeMethod( post );
            InputStream is = post.getResponseBodyAsStream();
            XMLFragment xml = new XMLFragment();
            xml.load( is, source.toURL().toExternalForm() );

            Node node = XMLTools.getNode( xml.getRootElement(), Messages.getString( "SearchResult.child_"
                                                                                    + sourceVersion ), nsc );
            if ( node != null ) {
                xml.setRootElement( (Element) node );
            } else {
                xml = null;
            }

            return xml;
        }

        private int getNoOfMetadataRecord( URI source, String type )
                                throws IOException, XMLException, SAXException, XMLParsingException {

            // read template from file
            URL url = Templates.getTemplate( "GetNoOfRecords_" + sourceVersion );
            String getRecords = FileUtils.readTextFile( url ).toString();
            getRecords = StringTools.replace( getRecords, "$type$", type, false );
            StringRequestEntity re = new StringRequestEntity( getRecords, "text/xml", CharsetUtils.getSystemCharset() );
            PostMethod post = new PostMethod( source.toASCIIString() );
            post.setRequestEntity( re );
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setSoTimeout( 30000 );
            client = WebUtils.enableProxyUsage( client, source.toURL() );
            client.executeMethod( post );
            InputStream is = post.getResponseBodyAsStream();
            XMLFragment xml = new XMLFragment();
            xml.load( is, source.toURL().toExternalForm() );

            return XMLTools.getNodeAsInt( xml.getRootElement(), Messages.getString( "NumberOfRecordsMatched_"
                                                                                    + sourceVersion ), nsc, 0 );
        }

    }

}
