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
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.deegree.enterprise.WebUtils;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.mail.EMailMessage;
import org.deegree.framework.mail.MailHelper;
import org.deegree.framework.mail.MailMessage;
import org.deegree.framework.mail.SendMailException;
import org.deegree.framework.mail.UnknownMimeTypeException;
import org.deegree.framework.trigger.Trigger;
import org.deegree.framework.trigger.TriggerException;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.i18n.Messages;
import org.deegree.io.DBConnectionPool;
import org.xml.sax.SAXException;

/**
 * Trigger implementation for synchronizing several CSW instances for incomming Transaction requests
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class CSWSychronizationTrigger implements Trigger {

    private static final ILogger LOG = LoggerFactory.getLogger( CSWSychronizationTrigger.class );

    private String name;

    private URL[] cswAddr;

    private String driver;

    private String url;

    private String user;

    private String password;

    private String smtpServer;

    private String sender;

    private String receiver;

    private int maxRepeat = 0;

    /**
     *
     * @param driver
     * @param url
     * @param user
     * @param password
     * @param smtpServer
     * @param sender
     * @param receiver
     * @param maxRepeat
     * @param address
     *            addresses of all CSW instances to be synchronized
     */
    public CSWSychronizationTrigger( String driver, String url, String user, String password, String smtpServer,
                                     String sender, String receiver, Integer maxRepeat, URL address ) {
        this.cswAddr = new URL[] { address };
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
        this.smtpServer = smtpServer;
        this.sender = sender;
        this.receiver = receiver;
        this.maxRepeat = maxRepeat;
    }

    /**
     * @param caller
     * @param values
     * @return the objects
     */
    public Object[] doTrigger( Object caller, Object... values ) {

        // try to execute failed request stored in the db
        performFormerRequests();

        if ( !( values[0] instanceof TransactionResult ) ) {
            return values;
        }

        TransactionResult result = (TransactionResult) values[0];
        Transaction transaction = (Transaction) result.getRequest();

        TransactionDocument tDoc = null;
        try {
            tDoc = XMLFactory.export( transaction );
        } catch ( Exception e ) {
            // should not happen because request has been parsed and
            // performed before caling this method
            LOG.logError( e.getMessage(), e );
            throw new TriggerException( e );
        }

        List<URL> errorAddr = new ArrayList<URL>();
        String req = tDoc.getAsString();
        for ( int i = 0; i < cswAddr.length; i++ ) {
            try {
                String excep = performRequest( req, cswAddr[i] );
                if ( "Exception".equals( excep ) ) {
                    errorAddr.add( cswAddr[i] );
                }
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
                errorAddr.add( cswAddr[i] );
            }
        }

        try {
            if ( errorAddr.size() > 0 ) {
                handleErrors( errorAddr, tDoc.getAsString() );
            }
        } catch ( Exception e ) {
            // exception will not be forwarded because it does not affect
            // performance of request by the triggering CSW
            LOG.logError( e.getMessage(), e );
        }

        return values;
    }

    /**
     * sends a request to the passed url
     *
     * @param req
     * @param url
     * @return the local name of the response XML
     * @throws IOException
     * @throws HttpException
     * @throws SAXException
     */
    private String performRequest( String req, URL url )
                            throws IOException, HttpException, SAXException {
        StringRequestEntity re = new StringRequestEntity( req, "text/xml", CharsetUtils.getSystemCharset() );
        PostMethod post = new PostMethod( url.toExternalForm() );
        post.setRequestEntity( re );
        HttpClient client = new HttpClient();
        client = WebUtils.enableProxyUsage( client, url );
        client.executeMethod( post );
        InputStream is = post.getResponseBodyAsStream();
        XMLFragment xml = new XMLFragment();
        xml.load( is, url.toExternalForm() );
        String excep = xml.getRootElement().getLocalName();
        return excep;
    }

    /**
     *
     * @param errorAddr
     * @param request
     */
    private void handleErrors( List<URL> errorAddr, String request ) {
        storeCurrentRequest( errorAddr, request );
        informAdmin( Messages.getMessage( "CSW_ERROR_SYNCHRONIZE_CSW", errorAddr, request ) );
    }

    private void performFormerRequests() {
        try {
            DBConnectionPool pool = DBConnectionPool.getInstance();
            Connection con = pool.acquireConnection( driver, url, user, password );
            Statement stmt = con.createStatement();
            List<Fail> failed = new ArrayList<Fail>( 100 );
            ResultSet rs = stmt.executeQuery( "SELECT * FROM FAILEDREQUESTS" );
            // first read all request that failed before from the database
            // to avoid performing transactions on the same table at the
            // same time
            while ( rs.next() ) {
                int id = rs.getInt( "ID" );
                String req = rs.getString( "REQUEST" );
                String cswAddress = rs.getString( "CSWADDRESS" );
                int repeat = rs.getInt( "REPEAT" );
                failed.add( new Fail( id, req, new URL( cswAddress ), repeat ) );
            }
            rs.close();

            for ( int i = 0; i < failed.size(); i++ ) {
                try {
                    String excep = performRequest( failed.get( i ).request, failed.get( i ).cswAddress );
                    if ( !"Exception".equals( excep ) ) {
                        // if request has been performed successfully delete entry
                        // from the database
                        stmt.execute( "DELETE FROM FAILEDREQUESTS WHERE ID = " + failed.get( i ).id );
                    } else {
                        // otherwise increase counter to indicate how often performing
                        // this request has failed
                        updateFailedrequests( stmt, failed.get( i ) );
                    }
                } catch ( Exception e ) {
                    // just to ensure that if a sql exception occurs other requests
                    // has the chance to be removed from the DB
                    LOG.logError( e.getMessage(), e );
                    informAdmin( Messages.getMessage( "CSW_ERROR_UPDATING_FAILEDREQUESTS", failed.get( i ).id ) );
                    updateFailedrequests( stmt, failed.get( i ) );
                }
            }
            stmt.close();
            pool.releaseConnection( con, driver, url, user, password );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new TriggerException( e );
        }
    }

    private void updateFailedrequests( Statement stmt, Fail failed )
                            throws SQLException {
        // increase counter to indicate how often performing
        // this request has failed
        failed.repeat++;
        if ( failed.repeat > maxRepeat ) {
            informAdmin( Messages.getMessage( "CSW_ERROR_EXCEEDING_MAX_REPEAT", failed.cswAddress, failed.request,
                                              maxRepeat ) );
            Boolean result = stmt.execute( "DELETE FROM FAILEDREQUESTS WHERE ID = " + failed.id );
            LOG.logDebug( "Result of deleting from failed requests when maxRepeat is reached: " + result );
        } else {
            Boolean result = stmt.execute( "UPDATE FAILEDREQUESTS SET REPEAT = " + failed.repeat + " WHERE ID = "
                                           + failed.id );
            LOG.logDebug( "Result of updating repeat of failed requests: " + result );
        }
    }

    private void storeCurrentRequest( List<URL> errorAddr, String request ) {

        try {
            DBConnectionPool pool = DBConnectionPool.getInstance();
            Connection con = pool.acquireConnection( driver, url, user, password );
            for ( int i = 0; i < errorAddr.size(); i++ ) {
                PreparedStatement stmt = con.prepareStatement( "INSERT INTO FAILEDREQUESTS (REQUEST,CSWADDRESS,REPEAT) VALUES (?,?,?)" );
                try {
                    stmt.setString( 1, request );
                    stmt.setString( 2, errorAddr.get( i ).toExternalForm() );
                    stmt.setInt( 3, 1 );
                    Boolean result = stmt.execute();
                    LOG.logDebug( "Result of inserting failed requests: " + result );
                } catch ( Exception e ) {
                    // just to ensure that if a sql exception occurs other requests
                    // has the chance to be inserted into the DB
                    LOG.logError( e.getMessage(), e );
                    informAdmin( Messages.getMessage( "CSW_ERROR_INSERTING_INTO_FAILEDREQUESTS", errorAddr.get( i ),
                                                      request ) );
                }
                stmt.close();
            }
            pool.releaseConnection( con, driver, url, user, password );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new TriggerException( e );
        }

    }

    private void informAdmin( String message ) {

        String subject = Messages.getMessage( "CSW_SYNCHRONIZE_MAIL_SUBJECT" );

        MailMessage email;
        try {
            email = new EMailMessage( sender, receiver, subject, message, "text/html" );
        } catch ( UnknownMimeTypeException e ) {
            LOG.logError( e.getMessage(), e );
            throw new TriggerException( "Unknown mime type set." + e );
        }

        try {
            MailHelper.createAndSendMail( email, smtpServer );
        } catch ( SendMailException e ) {
            LOG.logError( e.getMessage(), e );
        }

    }

    /**
     * @see org.deegree.framework.trigger.Trigger#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @see org.deegree.framework.trigger.Trigger#setName(java.lang.String)
     */
    public void setName( String name ) {
        this.name = name;
    }

    private class Fail {
        /**
         *
         */
        public int id = 0;

        /**
         *
         */
        public String request;

        /**
         *
         */
        public URL cswAddress;

        /**
         *
         */
        public int repeat;

        /**
         * @param id
         * @param request
         * @param cswAddress
         * @param repeat
         */
        public Fail( int id, String request, URL cswAddress, int repeat ) {
            this.id = id;
            this.request = request;
            this.cswAddress = cswAddress;
            this.repeat = repeat;
        }
    }

}
