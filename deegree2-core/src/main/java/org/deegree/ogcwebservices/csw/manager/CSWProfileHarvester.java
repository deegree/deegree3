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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.FileUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcwebservices.csw.manager.HarvestRepository.ResourceType;
import org.xml.sax.SAXException;

/**
 * Harverster implementation for harvesting single metadata documents.
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
public class CSWProfileHarvester extends AbstractHarvester {

    static final ILogger LOG = LoggerFactory.getLogger( CSWProfileHarvester.class );

    private static CSWProfileHarvester ch = null;

    /**
     * @param version
     *            the version of the CSW
     */
    private CSWProfileHarvester( String version ) {
        super( version );
    }

    /**
     * singelton
     * 
     * @param version
     *            the version of the CSW
     * 
     * @return the instance
     */
    public static CSWProfileHarvester getInstance( String version ) {
        if ( ch == null ) {
            ch = new CSWProfileHarvester( version );
        }
        return ch;
    }

    @Override
    public void run() {
        LOG.logDebug( "starting harvest iteration for CSWProfileHarvester." );
        try {
            HarvestRepository repository = HarvestRepository.getInstance();

            List<URI> sources = repository.getSources();
            for ( Iterator<URI> iter = sources.iterator(); iter.hasNext(); ) {
                URI source = iter.next();
                try {
                    // determine if source shall be harvested
                    if ( shallHarvest( source, ResourceType.csw_profile ) ) {
                        HarvestProcessor processor = new HarvestProcessor( this, source );
                        processor.start();
                    }
                } catch ( Exception e ) {
                    LOG.logError( "Exception harvesting service: " + source, e );
                    informResponseHandlers( source, e );
                }
            }
        } catch ( Exception e ) {
            LOG.logError( "generell Exception harvesting services", e );
        }

    }

    /**
     * inner class for processing asynchronous harvesting of a csw:profile metadata document
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

        HarvestProcessor( AbstractHarvester owner, URI source ) {
            super( owner, source );
        }

        @Override
        public void run() {
            try {
                HarvestRepository repository = HarvestRepository.getInstance();
                XMLFragment metaData = accessMetadata( source );
                Date harvestingTimestamp = repository.getNextHarvestingTimestamp( source );
                String trans = null;
                // ensure that a resource just will be harvested if something has changed
                if ( shallHarvest( source, ResourceType.csw_profile ) ) {
                    if ( repository.getLastHarvestingTimestamp( source ) == null ) {
                        trans = createInsertRequest( metaData );
                    } else {
                        trans = createUpdateRequest( getID( metaData ), getIdentifierXPathForUpdate( metaData ),
                                                     metaData );
                    }
                    performTransaction( trans );

                    long ts = repository.getHarvestInterval( source );
                    if ( ts <= 0 ) {
                        // if the harvest interval is less or equal to 0 the source
                        // shall just be harvested for one time and it will be
                        // removed from harvest cache db
                        informResponseHandlers( source );
                        repository.dropRequest( source );
                    } else {
                        // update timestamps just if transaction has been performed
                        // successfully
                        writeLastHarvestingTimestamp( source, harvestingTimestamp );
                        writeNextHarvestingTimestamp( source, harvestingTimestamp );
                        informResponseHandlers( source );
                    }
                }

            } catch ( Exception e ) {
                e.printStackTrace();
                LOG.logError( "could not perform harvest operation for source: " + source, e );
                try {
                    owner.informResponseHandlers( source, e );
                } catch ( Exception ee ) {
                    ee.printStackTrace();
                }
            }
        }

        /**
         * returns the identifier of a metadata record to enable its update
         * 
         * @param metaData
         * @return the identifier of a metadata record to enable its update
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
         * 
         * @param source
         * @return the document
         * @throws SAXException
         * @throws IOException
         * @throws MalformedURLException
         */
        private XMLFragment accessMetadata( URI source )
                                throws MalformedURLException, IOException, SAXException {

            XMLFragment xml = new XMLFragment();
            xml.load( source.toURL() );
            return xml;
        }

    }

}
