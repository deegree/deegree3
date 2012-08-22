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
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.deegree.enterprise.WebUtils;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.mail.EMailMessage;
import org.deegree.framework.mail.MailHelper;
import org.deegree.framework.mail.MailMessage;
import org.deegree.framework.util.FileUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.io.DBPoolException;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.csw.CSWFactory;
import org.deegree.ogcwebservices.csw.manager.HarvestRepository.ResourceType;
import org.xml.sax.SAXException;

/**
 * Abstract super class of all CS-W harvesters. For each kind of source a specialized harvester shall be implemented. A
 * concrete implementation of AbstractHarvester will be called within a timer loop.
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
public abstract class AbstractHarvester extends TimerTask {

    private static final ILogger LOG = LoggerFactory.getLogger( AbstractHarvester.class );

    private boolean stopped = true;

    private Timer timer = null;

    protected List<URI> inProgress = new Vector<URI>();

    protected String version;

    protected static NamespaceContext nsc = CommonNamespaces.getNamespaceContext();
    static {
        try {
            nsc.addNamespace( "smXML", new URI( "http://metadata.dgiwg.org/smXML" ) );
            nsc.addNamespace( "iso19119", new URI( "http://schemas.opengis.net/iso19119" ) );
            nsc.addNamespace( "iso19115", new URI( "http://schemas.opengis.net/iso19115full" ) );
        } catch ( URISyntaxException e ) {
            e.printStackTrace();
        }
    }

    /**
     * @param version
     *            the version of the CSW
     */
    protected AbstractHarvester( String version ) {
        this.version = version;
    }

    /**
     * adds a request to the harvesting process
     * 
     * @param request
     * @throws IOException
     * @throws SQLException
     * @throws DBPoolException
     */
    public void addRequest( Harvest request )
                            throws IOException, DBPoolException, SQLException {
        HarvestRepository.getInstance().storeRequest( request );
    }

    /**
     * returns true if the harvesting process is running
     * 
     * @return <code>true</code> if the harvesting process is running
     */
    public boolean isRunning() {
        return !stopped;
    }

    /**
     * removes a request from the harvesting request.
     * <p>
     * <b> !! At the moment the OGC CSW does not know a mechanism/request to stop a cyclic harvesting job, so this
     * method can not be called with a standard OGC OWS request !!</b>
     * </p>
     * 
     * @param request
     * @throws IOException
     * @throws SQLException
     * @throws DBPoolException
     */
    public void removeRequest( Harvest request )
                            throws IOException, DBPoolException, SQLException {
        HarvestRepository.getInstance().dropRequest( request.getSource() );
    }

    /**
     * starts the harvesting process
     * 
     */
    public void startHarvesting() {
        timer = new Timer();
        timer.schedule( this, 0, 10000 );
        stopped = false;
        LOG.logInfo( "harvesting has been started" );
    }

    /**
     * stops the harvesting process
     * 
     */
    public void stopHarvesting() {
        timer.purge();
        timer.cancel();
        stopped = true;
        LOG.logInfo( "harvesting has been stopped" );
    }

    /**
     * informs all response handlers assigend to a source about successful harvesting of the source
     * 
     * @param source
     * @throws URISyntaxException
     * @throws SQLException
     * @throws DBPoolException
     * @throws MalformedURLException
     */
    protected void informResponseHandlers( URI source )
                            throws IOException, DBPoolException, SQLException, URISyntaxException {

        List<HarvestRepository.ResponseHandler> list = HarvestRepository.getInstance().getResponseHandlers( source );

        for ( Iterator<HarvestRepository.ResponseHandler> iter = list.iterator(); iter.hasNext(); ) {
            HarvestRepository.ResponseHandler handler = iter.next();
            String message = StringTools.concat( 100, "source: ", source, " has been harvested successfully!" );
            sendMessage( handler, message );
        }

    }

    /**
     * returns true if the passed source shall be harvested. this is true if a source has not been harvested before or
     * the next harvesting timestamp has been reached and the source is of type
     * 
     * @see HarvestRepository.ResourceType service
     * 
     * @param source
     * @return <code>true</code> if the passed source shall be harvested
     * @throws DBPoolException
     * @throws SQLException
     */
    protected boolean shallHarvest( URI source, ResourceType targetType )
                            throws IOException, DBPoolException, SQLException {

        if ( inProgress.contains( source ) ) {
            return false;
        }

        HarvestRepository repository = HarvestRepository.getInstance();

        ResourceType st = repository.getSourceType( source );

        if ( !st.equals( targetType ) ) {
            return false;
        }

        Date lastHarvesting = repository.getLastHarvestingTimestamp( source );
        Date nextHarvesting = repository.getNextHarvestingTimestamp( source );

        long tmp = System.currentTimeMillis() - nextHarvesting.getTime();
        return lastHarvesting == null || tmp >= 0 || repository.shallForceHarvesting( source );
    }

    /**
     * informs all response handlers assigend to a source about an exception that occurs when harvesting a source
     * 
     * @param source
     * @param e
     * @throws URISyntaxException
     * @throws SQLException
     * @throws DBPoolException
     */
    protected void informResponseHandlers( URI source, Throwable e )
                            throws IOException, DBPoolException, SQLException, URISyntaxException {

        List<HarvestRepository.ResponseHandler> list = HarvestRepository.getInstance().getResponseHandlers( source );

        for ( Iterator<HarvestRepository.ResponseHandler> iter = list.iterator(); iter.hasNext(); ) {
            HarvestRepository.ResponseHandler handler = iter.next();
            String message = StringTools.concat( 500, "exception occures harvesting source: ", source, "; exception: ",
                                                 e.getMessage() );
            sendMessage( handler, message );

        }

    }

    /**
     * 
     * @param handler
     * @param message
     */
    private void sendMessage( HarvestRepository.ResponseHandler handler, String message ) {
        if ( handler.isMailAddress() ) {
            String s = handler.getUri().toASCIIString();
            int p = s.indexOf( ":" );
            s = s.substring( p + 1, s.length() );
            LOG.logDebug( "sending message", message );
            LOG.logDebug( StringTools.concat( 200, "informing response handler ", s, "via mail" ) );
            MailMessage mm = new EMailMessage( "info@lat-lon.de", s, "CS-W harvesting", message );
            try {
                MailHelper.createAndSendMail( mm, System.getProperty( "mailHost" ) );
            } catch ( Exception e ) {
                LOG.logInfo( "could not send mail to admin:", e.getMessage() );
                LOG.logError( e.getMessage(), e );
            }
        } else {
            LOG.logDebug( StringTools.concat( 200, "informing response handler ", handler.getUri(), "via HTTP GET" ) );
            HttpClient client = new HttpClient();
            LOG.logDebug( message );

            try {
                client = WebUtils.enableProxyUsage( client, handler.getUri().toURL() );
                GetMethod get = new GetMethod( handler.getUri().toURL().toExternalForm() + "?message=" + message );
                client.executeMethod( get );
            } catch ( Exception e ) {
                LOG.logInfo( "could not post message: '" + message + "' to: " + handler.getUri() + "; reason: "
                             + e.getMessage() );
            }

        }
    }

    /**
     * abstract super class for all harvest processores
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
    protected abstract class AbstractHarvestProcessor extends Thread {

        protected URI source = null;

        protected AbstractHarvester owner = null;

        protected AbstractHarvestProcessor( AbstractHarvester owner, URI source ) {
            this.owner = owner;
            this.source = source;
        }

        /**
         * performs a transaction for inserting or updating a service meta data record in the catalogue a harvester
         * instance belongs too
         * 
         * @param trans
         * @throws SAXException
         * @throws IOException
         * @throws OGCWebServiceException
         */
        protected void performTransaction( String trans )
                                throws SAXException, IOException, OGCWebServiceException {

            StringReader sr = new StringReader( trans );
            XMLFragment xml = new XMLFragment();
            xml.load( sr, XMLFragment.DEFAULT_URL );
            Transaction transaction = Transaction.create( "id", xml.getRootElement() );
            CSWFactory.getService().doService( transaction );

        }

        /**
         * creates a CSW Transaction including an Update operation for the passed meta data.
         * 
         * @param identifier
         * @param xpath
         * @param metaData
         * @return update request
         * @throws IOException
         */
        protected String createUpdateRequest( String identifier, String xpath, XMLFragment metaData )
                                throws IOException {
            // read template from file
            URL url = Templates.getTemplate( "Update_" + version );
            String update = FileUtils.readTextFile( url ).toString();

            // remove XML doctype section to enable inserting the metadata set as string
            // into the template
            // TODO transform metaData into xml valid against the correct ISO AP version
            String s = metaData.getAsString();
            int p = s.lastIndexOf( "?>" );
            if ( p > -1 ) {
                s = s.substring( p + 2, s.length() );
            }

            // set metadata set to update by replacing the string '$metadata$'
            // in the template
            update = StringTools.replace( update, "$metadata$", s, false );
            s = createConstraint( identifier, xpath ).toString();

            // set filter/constraint to identify the metadata set to be updated
            // by replacing the string '$constraints$' in the template
            return StringTools.replace( update, "$constraints$", s, false );
        }

        /**
         * creates a transaction request including a delete operation to remove the metadata record with the passed
         * fileIdentifier from the catalogue
         * 
         * @param identifier
         * @return delete request
         * @throws IOException
         */
        protected String createDeleteRequest( String identifier )
                                throws IOException {
            // read template from file
            URL url = Templates.getTemplate( "Delete_" + version );
            String delete = FileUtils.readTextFile( url ).toString();

            String xpath = Messages.getString( "IdentifierDelete_" + version );
            String s = createConstraint( identifier, xpath ).toString();

            // set filter/constraint to identify the metadata set to be deleted
            // by replacing the string '$constraints$' in the template
            return StringTools.replace( delete, "$constraints$", s, false );
        }

        /**
         * a constraint for delete und update operation depends on concrete metadata format. An implementing class must
         * consider this.
         * 
         * @param fileIdentifier
         *            value to be compared
         * @param xpath
         *            comparable property
         * @return the constraint
         */
        protected abstract String createConstraint( String fileIdentifier, String xpath )
                                throws IOException;

        /**
         * creates a CSW Transaction including an Update operation for the passed meta data
         * 
         * @param metaData
         * @return the request
         * @throws IOException
         */
        protected String createInsertRequest( XMLFragment metaData )
                                throws IOException {
            // read template from file
            URL url = Templates.getTemplate( "Insert_" + version );
            String insert = FileUtils.readTextFile( url ).toString();

            // TODO transform metaData into xml valid against the correct ISO AP version
            String s = metaData.getAsString();
            int p = s.lastIndexOf( "?>" );
            if ( p > -1 ) {
                s = s.substring( p + 2, s.length() );
            }

            // set metadata set to insert by replacing the string '$metadata$'
            // in the template
            return StringTools.replace( insert, "$metadata$", s, false );

        }

        /**
         * actualizes the source in the repository with timestamp of last harvesting
         * 
         * @param source
         * @param date
         * @throws SQLException
         * @throws DBPoolException
         */
        protected void writeLastHarvestingTimestamp( URI source, Date date )
                                throws IOException, DBPoolException, SQLException {
            HarvestRepository repository = HarvestRepository.getInstance();
            repository.setLastHarvestingTimestamp( source, date );
        }

        /**
         * actualizes the source in the repository with timestamp when next harvesting shall be performed
         * 
         * @param source
         * @param date
         * @throws SQLException
         * @throws DBPoolException
         */
        protected void writeNextHarvestingTimestamp( URI source, Date date )
                                throws IOException, DBPoolException, SQLException {
            HarvestRepository repository = HarvestRepository.getInstance();
            long ts = repository.getHarvestInterval( source );
            date = new Date( ts + date.getTime() );
            repository.setNextHarvestingTimestamp( source, date );
        }

    }

    /**
     * returns the XPath the metadata records identifier
     * 
     * @param metaData
     * @return the XPath the metadata records identifier
     */
    protected String getIdentifierXPath( XMLFragment metaData ) {
        // default is iso 19115
        String xpath = "iso19115:fileIdentifier/smXML:CharacterString";
        if ( metaData != null ) {
            String nspace = metaData.getRootElement().getNamespaceURI();
            nspace = StringTools.replace( nspace, "http://", "", true );
            xpath = Messages.getString( "Identifier_" + nspace );
        }
        return xpath;
    }

    /**
     * returns the XPath the metadata records identifier
     * 
     * @param metaData
     * @return the XPath the metadata records identifier
     */
    protected String getIdentifierXPathForUpdate( XMLFragment metaData ) {
        String xpath = "iso19115:fileIdentifier/smXML:CharacterString";
        if ( metaData != null ) {
            String nspace = metaData.getRootElement().getNamespaceURI();
            nspace = StringTools.replace( nspace, "http://", "", true );
            xpath = Messages.getString( "IdentifierUpdate_" + nspace );
        }
        return xpath;
    }

}
