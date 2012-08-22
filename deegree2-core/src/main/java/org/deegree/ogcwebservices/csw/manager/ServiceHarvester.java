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
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.ogcwebservices.csw.manager.HarvestRepository.ResourceType;
import org.xml.sax.SAXException;

/**
 * <p>
 * Concrete implementation of
 * 
 * @see org.deegree.ogcwebservices.csw.manager.AbstractHarvester for harvesting service metadata
 *      from OGC web services. To enable this capabilities documents of the OWS will be accessed and
 *      transformed into a valid format that will be understood by the underlying catalogue. To
 *      enable a lot of flexibility a XSLT read from resource bundle (harvestservice.xsl) script
 *      will be used to perform the required transformation.
 *      </p>
 *      <p>
 *      A valid harvest SOURCE for a service must be a complete GetCapabilities request; the
 *      RESOURCETYPE must be 'service'. Example:
 *      </p>
 *      <p>
 *      ...?request=Harvest&version=2.0.0&source=[http://MyServer:8080/deegree?
 *      service=WFS&version=1.1.0&request=GetCapabilities]&resourceType=service&
 *      resourceFormat=text/xml&responseHandler=mailto:info@lat-lon.de&harvestInterval=P2W
 *      </p>
 *      <p>
 *      value in brackets [..] must be URL encoded and send without brackets!
 *      </p>
 *      <p>
 *      This is not absolutly compliant to OGc CSW 2.0.0 specification but Harvest definition as
 *      available from the spec is to limited because it just targets single metadata documents.
 *      </p>
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class ServiceHarvester extends AbstractHarvester {

    static final ILogger LOG = LoggerFactory.getLogger( ServiceHarvester.class );

    static final URL xslt = ServiceHarvester.class.getResource( "harvestservice.xsl" );

    private static ServiceHarvester sh = null;

    /**
     * @param version
     *            the version of the CSW
     */
    public ServiceHarvester( String version ) {
        super( version );
    }

    /**
     * singelton
     * 
     * @param version
     *            the version of the CSW
     * 
     * @return the new instance
     */
    public static ServiceHarvester getInstance( String version ) {
        if ( sh == null ) {
            sh = new ServiceHarvester( version );
        }
        return sh;
    }

    @Override
    public void run() {
        LOG.logDebug( "starting harvest iteration for ServiceHarvester." );
        try {
            HarvestRepository repository = HarvestRepository.getInstance();

            List<URI> sources = repository.getSources();
            for ( Iterator<URI> iter = sources.iterator(); iter.hasNext(); ) {
                URI source = iter.next();
                try {
                    // determine if source shall be harvested
                    if ( shallHarvest( source, ResourceType.service ) ) {
                        inProgress.add( source );
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
     * inner class for processing asynchronous harvesting of a service
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
                XMLFragment capabilities = accessSourceCapabilities( source );
                Date harvestingTimestamp = repository.getNextHarvestingTimestamp( source );
                XMLFragment metaData = transformCapabilities( capabilities );
                String trans = null;
                if ( repository.getLastHarvestingTimestamp( source ) == null ) {
                    trans = createInsertRequest( metaData );
                } else {
                    trans = createUpdateRequest( getID( metaData ), getIdentifierXPathForUpdate( metaData ), metaData );
                }
                performTransaction( trans );
                // update timestamps just if transaction has been performed
                // successfully
                writeLastHarvestingTimestamp( source, harvestingTimestamp );
                writeNextHarvestingTimestamp( source, harvestingTimestamp );
                informResponseHandlers( source );
            } catch ( Exception e ) {
                LOG.logError( "could not perform harvest operation for source: " + source, e );
                try {
                    owner.informResponseHandlers( source, e );
                } catch ( Exception ee ) {
                    ee.printStackTrace();
                }
            }
        }

        private String getID( XMLFragment metaData )
                                throws XMLParsingException {
            String xpath = getIdentifierXPath( metaData );
            String fileIdentifier = XMLTools.getRequiredNodeAsString( metaData.getRootElement(), xpath, nsc );
            return fileIdentifier;
        }

        @Override
        protected String createConstraint( String fileIdentifier, String xPath ) {
            throw new UnsupportedOperationException();
        }

        /**
         * transforms a OWS capabilities document into the desired target format
         * 
         * @param xml
         * @return the transformed document
         * @throws IOException
         * @throws SAXException
         * @throws TransformerException
         */
        private XMLFragment transformCapabilities( XMLFragment xml )
                                throws IOException, SAXException, TransformerException {

            XSLTDocument xsltDoc = new XSLTDocument();
            xsltDoc.load( xslt );

            return xsltDoc.transform( xml );
        }

        /**
         * returns the capabilities of
         * 
         * @param source
         * @return the capabilities
         * @throws IOException
         * @throws SAXException
         */
        private XMLFragment accessSourceCapabilities( URI source )
                                throws IOException, SAXException {

            URL url = source.toURL();
            XMLFragment xml = new XMLFragment();
            xml.load( url );
            return xml;
        }

    }

}
