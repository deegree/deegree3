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
package org.deegree.tools.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import org.deegree.framework.mail.EMailMessage;
import org.deegree.framework.mail.MailHelper;
import org.deegree.framework.mail.SendMailException;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.SecurityAccess;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.SecurityTransaction;
import org.deegree.security.drm.UnknownException;
import org.deegree.security.drm.model.Group;
import org.deegree.security.drm.model.User;

/**
 * This class provides the functionality to synchronize the <code>User</code> instances stored in a
 * <code>SecurityManager</code> with a generic LDAP-Server (such as OpenLDAP).
 * <p>
 * Changes are committed after all steps succeeded. If an error occurs, changes in the <code>SecurityManager</code> are
 * undone.
 * <p>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class LDAPImporter {

    private SecurityAccessManager manager;

    private SecurityAccess access;

    private SecurityTransaction trans;

    private User admin;

    private Hashtable<String, String> env;

    private LdapContext ctx;

    private Properties config;

    // mail configuration
    private static String mailSender;

    private static String mailRcpt;

    private static String mailHost;

    private static boolean mailLog;

    // number of results to fetch in one batch
    private int pageSize = 500;

    private StringBuffer logBuffer = new StringBuffer( 1000 );

    /**
     * Constructs a new <code>ADExporter</code> -instance.
     * <p>
     *
     * @param config
     * @throws NamingException
     * @throws GeneralSecurityException
     */
    LDAPImporter( Properties config ) throws NamingException, GeneralSecurityException {

        this.config = config;

        // retrieve mail configuration first
        mailSender = getPropertySafe( "mailSender" );
        mailRcpt = getPropertySafe( "mailRcpt" );
        mailHost = getPropertySafe( "mailHost" );
        mailLog = getPropertySafe( "mailLog" ).equals( "true" ) || getPropertySafe( "mailLog" ).equals( "yes" ) ? true
                                                                                                               : false;

        // get a SecurityManager (with an SQLRegistry)
        Properties registryProperties = new Properties();
        registryProperties.put( "driver", getPropertySafe( "sqlDriver" ) );
        registryProperties.put( "url", getPropertySafe( "sqlLogon" ) );
        registryProperties.put( "user", getPropertySafe( "sqlUser" ) );
        registryProperties.put( "password", getPropertySafe( "sqlPass" ) );

        // default timeout: 20 min
        long timeout = 1200000;
        try {
            timeout = Long.parseLong( getPropertySafe( "timeout" ) );
        } catch ( NumberFormatException e ) {
            logBuffer.append( "Specified property value for timeout invalid. " + "Defaulting to 1200 (secs)." );
        }

        if ( !SecurityAccessManager.isInitialized() ) {
            SecurityAccessManager.initialize( "org.deegree.security.drm.SQLRegistry", registryProperties, timeout );
        }

        manager = SecurityAccessManager.getInstance();
        admin = manager.getUserByName( getPropertySafe( "u3rAdminName" ) );
        admin.authenticate( getPropertySafe( "u3rAdminPassword" ) );

        // prepare LDAP connection
        String jndiURL = "ldap://" + getPropertySafe( "ldapHost" ) + ":389/";
        String initialContextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
        String authenticationMode = "simple";
        String contextReferral = "ignore";
        env = new Hashtable<String, String>();
        env.put( Context.INITIAL_CONTEXT_FACTORY, initialContextFactory );
        env.put( Context.PROVIDER_URL, jndiURL );
        env.put( Context.SECURITY_AUTHENTICATION, authenticationMode );
        env.put( Context.SECURITY_PRINCIPAL, getPropertySafe( "ldapUser" ) );
        env.put( Context.SECURITY_CREDENTIALS, getPropertySafe( "ldapPass" ) );
        env.put( Context.REFERRAL, contextReferral );

        access = manager.acquireAccess( admin );
        trans = manager.acquireTransaction( admin );
        ctx = new InitialLdapContext( env, null );

    }

    /**
     * Returns a configuration property. If it is not defined, an exception is thrown.
     * <p>
     *
     * @param name
     * @return a configuration property. If it is not defined, an exception is thrown.
     */
    private String getPropertySafe( String name ) {

        String value = config.getProperty( name );
        if ( value == null ) {
            throw new RuntimeException( "Configuration does not define needed property '" + name + "'." );
        }
        return value;
    }

    /**
     * Synchronizes the LDAP's user objects with the SecurityManager's user objects.
     * <p>
     *
     * @throws NamingException
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws UnauthorizedException
     *
     */
    void synchronizeUsers()
                            throws NamingException, IOException, UnauthorizedException, GeneralSecurityException {

        // keys are names (Strings), values are User-objects
        HashMap<String, User> userMap = new HashMap<String, User>( 20 );

        byte[] cookie = null;

        // specify the ids of the attributes to return
        String[] attrIDs = { getPropertySafe( "userName" ), getPropertySafe( "userTitle" ),
                            getPropertySafe( "userFirstName" ), getPropertySafe( "userLastName" ),
                            getPropertySafe( "userMail" ) };

        SearchControls ctls = new SearchControls();
        ctls.setReturningAttributes( attrIDs );
        ctls.setSearchScope( SearchControls.SUBTREE_SCOPE );

        // specify the search filter to match
        String filter = getPropertySafe( "userFilter" );
        String context = getPropertySafe( "userContext" );

        // create initial PagedResultsControl
        ctx.setRequestControls( new Control[] { new PagedResultsControl( pageSize, false ) } );

        // phase 1: make sure that all users from LDAP are present in the
        // SecurityManager
        // (register them to the SecurityManager if necessary)
        do {
            // perform the search
            NamingEnumeration<?> answer = ctx.search( context, filter, ctls );

            // process results of the last batch
            while ( answer.hasMoreElements() ) {
                SearchResult result = (SearchResult) answer.nextElement();

                Attributes atts = result.getAttributes();
                Attribute nameAtt = atts.get( getPropertySafe( "userName" ) );
                Attribute firstNameAtt = atts.get( getPropertySafe( "userFirstName" ) );
                Attribute lastNameAtt = atts.get( getPropertySafe( "userLastName" ) );
                Attribute mailAtt = atts.get( getPropertySafe( "userMail" ) );

                String name = (String) nameAtt.get();
                String firstName = firstNameAtt != null ? (String) firstNameAtt.get() : "" + "";
                String lastName = lastNameAtt != null ? (String) lastNameAtt.get() : "" + "";
                String mail = mailAtt != null ? (String) mailAtt.get() : "";

                // check if user is already registered
                User user = null;
                try {
                    user = access.getUserByName( name );
                } catch ( UnknownException e ) {
                    // no -> register user
                    logBuffer.append( "Registering user: " + name + "\n" );
                    user = trans.registerUser( name, null, lastName, firstName, mail );
                }
                userMap.put( name, user );
            }

            // examine the paged results control response
            Control[] controls = ctx.getResponseControls();
            if ( controls != null ) {
                for ( int i = 0; i < controls.length; i++ ) {
                    if ( controls[i] instanceof PagedResultsResponseControl ) {
                        PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
                        // total = prrc.getResultSize();
                        cookie = prrc.getCookie();
                    }
                }
            }

            if ( cookie != null ) {
                // re-activate paged results
                ctx.setRequestControls( new Control[] { new PagedResultsControl( pageSize, cookie, Control.CRITICAL ) } );
            }
        } while ( cookie != null );

        // phase 2: make sure that all users from the SecurityManager are known
        // to the LDAP (deregister them from the SecurityManager if necessary)
        User[] sMUsers = access.getAllUsers();
        for ( int i = 0; i < sMUsers.length; i++ ) {
            if ( userMap.get( sMUsers[i].getName() ) == null && sMUsers[i].getID() != User.ID_SEC_ADMIN ) {
                logBuffer.append( "Deregistering user: " + sMUsers[i].getName() + "\n" );
                trans.deregisterUser( sMUsers[i] );
            }
        }
    }

    /**
     * Updates the special group "SEC_ALL" (contains all users).
     * <p>
     *
     * @throws GeneralSecurityException
     */
    void updateSecAll()
                            throws GeneralSecurityException {
        Group secAll = null;

        // phase1: make sure that group "SEC_ALL" exists
        // (register it if necessary)
        try {
            secAll = access.getGroupByName( "SEC_ALL" );
        } catch ( UnknownException e ) {
            secAll = trans.registerGroup( "SEC_ALL", "SEC_ALL" );
        }

        // phase2: set all users to be members of this group
        User[] allUsers = access.getAllUsers();
        trans.setUsersInGroup( secAll, allUsers );
    }

    /**
     * Aborts the synchronization process and undoes all changes.
     */
    public void abortChanges() {
        if ( manager != null && trans != null ) {
            try {
                manager.abortTransaction( trans );
            } catch ( GeneralSecurityException e ) {
                e.printStackTrace();
            }
        }
        if ( ctx != null ) {
            try {
                ctx.close();
            } catch ( NamingException e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Ends the synchronization process and commits all changes.
     */
    public void commitChanges() {
        if ( manager != null && trans != null ) {
            try {
                manager.commitTransaction( trans );
            } catch ( GeneralSecurityException e ) {
                e.printStackTrace();
            }
        }
        if ( ctx != null ) {
            try {
                ctx.close();
            } catch ( NamingException e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends an eMail to inform the admin that something went wrong.
     * <p>
     * NOTE: This is static, because it must be usable even when the construction of the ADExporter failed.
     * <p>
     *
     * @param e
     */
    public static void sendError( Exception e ) {

        try {
            String mailMessage = "Beim Synchronisieren der LDAP-Datenbank mit der deegree-"
                                 + "Sicherheitsdatenbank ist ein Fehler aufgetreten.\n"
                                 + "Die Synchronisierung wurde NICHT durchgeführt, der letzte "
                                 + "Stand wurde wiederhergestellt.\n";
            StringWriter sw = new StringWriter();
            PrintWriter writer = new PrintWriter( sw );
            e.printStackTrace( writer );
            mailMessage += "\n\nDie Java-Fehlermeldung lautet:\n" + sw.getBuffer();

            mailMessage += "\n\nMit freundlichem Gruss,\nIhr LDAPImporter";
            MailHelper.createAndSendMail(
                                          new EMailMessage( mailSender, mailRcpt, "Fehler im LDAPImporter", mailMessage ),
                                          mailHost );
        } catch ( SendMailException ex ) {
            ex.printStackTrace();
        }

    }

    /**
     * Sends an eMail with a log of the transaction.
     * <p>
     */
    public void sendLog() {

        try {
            String mailMessage = "Die Synchronisierung der deegree Sicherheitsdatenbank mit "
                                 + "der LDAP-Datenbank wurde erfolgreich durchgeführt:\n\n";
            if ( logBuffer.length() == 0 ) {
                mailMessage += "Keine Änderungen.";
            } else {
                mailMessage += logBuffer.toString();
            }
            mailMessage += "\n\nMit freundlichem Gruss,\nIhr LDAPImporter";
            EMailMessage emm = new EMailMessage( mailSender, mailRcpt, "LDAP Sychronisierung durchgeführt", mailMessage );
            MailHelper.createAndSendMail( emm, mailHost );
        } catch ( SendMailException ex ) {
            ex.printStackTrace();
        }

    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main( String[] args )
                            throws Exception {

        if ( args.length != 1 ) {
            System.out.println( "USAGE: LDAPImporter configfile" );
            System.exit( 0 );
        }

        long begin = System.currentTimeMillis();
        System.out.println( "Beginning synchronisation..." );

        LDAPImporter exporter = null;
        try {
            Properties config = new Properties();
            config.load( new FileInputStream( args[0] ) );
            exporter = new LDAPImporter( config );

            exporter.synchronizeUsers();
            exporter.updateSecAll();
            exporter.commitChanges();
        } catch ( Exception e ) {
            if ( exporter != null ) {
                exporter.abortChanges();
            }
            sendError( e );
            System.err.println( "Synchronisation has been aborted. Error message: " );
            e.printStackTrace();
            System.exit( 0 );
        }

        if ( mailLog ) {
            exporter.sendLog();
        }

        System.out.println( "Synchronisation took " + ( System.currentTimeMillis() - begin ) + " milliseconds." );
        System.exit( 0 );
    }
}
