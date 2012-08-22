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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
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
import org.deegree.security.drm.ManagementException;
import org.deegree.security.drm.SecurityAccess;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.SecurityHelper;
import org.deegree.security.drm.SecurityTransaction;
import org.deegree.security.drm.UnknownException;
import org.deegree.security.drm.model.Group;
import org.deegree.security.drm.model.User;

/**
 * This class provides the functionality to synchronize the <code>User</code> and
 * <code>Group</code> instances stored in a <code>SecurityManager</code> with an
 * ActiveDirectory-Server.
 * <p>
 * Synchronization involves four steps:
 * <ul>
 * <li>synchronization of groups
 * <li>synchronization of users
 * <li>updating of the special group "SEC_ALL" (contains all users)
 * <li>testing of subadmin-role validity (only one role per user max)
 * </ul>
 * Changes are committed after all steps succeeded. If an error occurs, changes in the
 * <code>SecurityManager</code> are undone.
 * <p>
 *
 *
 * @version $Revision$
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ActiveDirectoryImporter {

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
    ActiveDirectoryImporter( Properties config ) throws NamingException, GeneralSecurityException {

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
     * Synchronizes the AD's group objects with the SecurityManager's group objects.
     * <p>
     *
     * @return the mapping of the String to the Groups
     * @throws NamingException
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws UnauthorizedException
     */
    HashMap<String, Group> synchronizeGroups()
                            throws NamingException, IOException, UnauthorizedException, GeneralSecurityException {
        // keys are names (Strings), values are Group-objects
        HashMap<String, Group> groupMap = new HashMap<String, Group>( 20 );
        // keys are distinguishedNames (Strings), values are Group-objects
        HashMap<String, Group> groupMap2 = new HashMap<String, Group>( 20 );
        // keys are names (Strings), values are NamingEnumeration-objects
        HashMap<String, NamingEnumeration<?>> memberOfMap = new HashMap<String, NamingEnumeration<?>>( 20 );

        byte[] cookie = null;

        // specify the ids of the attributes to return
        String[] attrIDs = { "distinguishedName", getPropertySafe( "groupName" ), getPropertySafe( "groupTitle" ),
                            getPropertySafe( "groupMemberOf" ) };

        // set SearchControls
        SearchControls ctls = new SearchControls();
        ctls.setReturningAttributes( attrIDs );
        ctls.setSearchScope( SearchControls.SUBTREE_SCOPE );

        // specify the search filter to match
        // ask for objects that have the attribute "objectCategory" =
        // "CN=Group*"
        String filter = getPropertySafe( "groupFilter" );
        String context = getPropertySafe( "groupContext" );

        // create initial PagedResultsControl
        ctx.setRequestControls( new Control[] { new PagedResultsControl( pageSize, false ) } );

        // phase 1: make sure that all groups from AD are present in the
        // SecurityManager
        // (register them to the SecurityManager if necessary)
        do {
            // perform the search
            NamingEnumeration<?> answer = ctx.search( context, filter, ctls );

            // process results of the last batch
            while ( answer.hasMoreElements() ) {
                SearchResult result = (SearchResult) answer.nextElement();
                Attributes atts = result.getAttributes();
                String distinguishedName = (String) atts.get( "distinguishedName" ).get();
                String name = (String) atts.get( getPropertySafe( "groupName" ) ).get();
                String title = (String) atts.get( getPropertySafe( "groupTitle" ) ).get();

                // check if group is already registered
                Group group = null;
                try {
                    group = access.getGroupByName( name );
                } catch ( UnknownException e ) {
                    // no -> register group
                    logBuffer.append( "Registering group: " + name + "\n" );
                    group = trans.registerGroup( name, title );
                }
                groupMap.put( name, group );
                groupMap2.put( distinguishedName, group );
                if ( atts.get( getPropertySafe( "groupMemberOf" ) ) != null ) {
                    memberOfMap.put( name, atts.get( getPropertySafe( "groupMemberOf" ) ).getAll() );
                }
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

        // phase 2: make sure that all groups from the SecurityManager are known
        // to the AD
        // (deregister them from the SecurityManager if necessary)
        Group[] sMGroups = access.getAllGroups();
        for ( int i = 0; i < sMGroups.length; i++ ) {
            if ( groupMap.get( sMGroups[i].getName() ) == null && sMGroups[i].getID() != Group.ID_SEC_ADMIN
                 && !( sMGroups[i].getName().equals( "SEC_ALL" ) ) ) {
                logBuffer.append( "Deregistering group: " + sMGroups[i].getName() + "\n" );
                trans.deregisterGroup( sMGroups[i] );
            }
        }

        // phase 3: set the membership-relations between the groups
        Iterator<String> it = groupMap.keySet().iterator();
        while ( it.hasNext() ) {
            String name = it.next();
            Group group = groupMap.get( name );
            NamingEnumeration<?> memberOf = memberOfMap.get( name );
            ArrayList<Group> memberOfList = new ArrayList<Group>( 5 );

            if ( memberOf != null ) {
                while ( memberOf.hasMoreElements() ) {
                    String memberGroupName = (String) memberOf.nextElement();
                    Group memberGroup = groupMap2.get( memberGroupName );
                    if ( memberGroup != null ) {
                        memberOfList.add( memberGroup );
                    } else {
                        logBuffer.append( "Group " + name + " is member of unknown group " + memberGroupName
                                          + ". Membership ignored.\n" );
                    }
                }
            }
            Group[] newGroups = memberOfList.toArray( new Group[memberOfList.size()] );
            trans.setGroupsForGroup( group, newGroups );
        }
        return groupMap2;
    }

    /**
     * Synchronizes the AD's user objects with the SecurityManager's user objects.
     * <p>
     *
     * @param groups
     *
     * @throws NamingException
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws UnauthorizedException
     *
     */
    void synchronizeUsers( HashMap<String, Group> groups )
                            throws NamingException, IOException, UnauthorizedException, GeneralSecurityException {
        // keys are names (Strings), values are User-objects
        HashMap<String, User> userMap = new HashMap<String, User>( 20 );
        // keys are names (Strings), values are NamingEnumeration-objects
        HashMap<String, NamingEnumeration<?>> memberOfMap = new HashMap<String, NamingEnumeration<?>>( 20 );

        byte[] cookie = null;

        // specify the ids of the attributes to return
        String[] attrIDs = { getPropertySafe( "userName" ), getPropertySafe( "userTitle" ),
                            getPropertySafe( "userFirstName" ), getPropertySafe( "userLastName" ),
                            getPropertySafe( "userMail" ), getPropertySafe( "userMemberOf" ) };

        SearchControls ctls = new SearchControls();
        ctls.setReturningAttributes( attrIDs );
        ctls.setSearchScope( SearchControls.SUBTREE_SCOPE );

        // specify the search filter to match
        String filter = getPropertySafe( "userFilter" );
        String context = getPropertySafe( "userContext" );

        // create initial PagedResultsControl
        ctx.setRequestControls( new Control[] { new PagedResultsControl( pageSize, false ) } );

        // phase 1: make sure that all users from AD are present in the
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
                // Attribute titleAtt = atts.get( getPropertySafe( "userTitle" ) );
                Attribute firstNameAtt = atts.get( getPropertySafe( "userFirstName" ) );
                Attribute lastNameAtt = atts.get( getPropertySafe( "userLastName" ) );
                Attribute mailAtt = atts.get( getPropertySafe( "userMail" ) );
                Attribute memberOfAtt = atts.get( getPropertySafe( "userMemberOf" ) );

                String name = (String) nameAtt.get();
                // String title = titleAtt != null ? (String) titleAtt.get() : "";
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

                if ( memberOfAtt != null ) {
                    memberOfMap.put( name, memberOfAtt.getAll() );
                }
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
        // to the AD
        // (deregister them from the SecurityManager if necessary)
        User[] sMUsers = access.getAllUsers();
        for ( int i = 0; i < sMUsers.length; i++ ) {
            if ( userMap.get( sMUsers[i].getName() ) == null && sMUsers[i].getID() != User.ID_SEC_ADMIN ) {
                logBuffer.append( "Deregistering user: " + sMUsers[i].getName() + "\n" );
                trans.deregisterUser( sMUsers[i] );
            }
        }

        // phase 3: set the membership-relations between the groups and the
        // users
        Iterator<String> it = userMap.keySet().iterator();
        while ( it.hasNext() ) {
            String name = it.next();
            User user = userMap.get( name );
            NamingEnumeration<?> memberOf = memberOfMap.get( name );
            ArrayList<Group> memberOfList = new ArrayList<Group>( 5 );
            if ( memberOf != null ) {
                while ( memberOf.hasMoreElements() ) {
                    String memberGroupName = (String) memberOf.nextElement();
                    Group memberGroup = groups.get( memberGroupName );
                    if ( memberGroup != null ) {
                        memberOfList.add( memberGroup );
                    } else {
                        logBuffer.append( "User " + name + " is member of unknown group " + memberGroupName
                                          + ". Membership ignored.\n" );
                    }
                }
            }
            Group[] newGroups = memberOfList.toArray( new Group[memberOfList.size()] );
            trans.setGroupsForUser( user, newGroups );
        }

    }

    /**
     * Updates the special group "SEC_ALL" (contains all users).
     * <p>
     *
     * @throws GeneralSecurityException
     *
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
     * Checks subadmin-role validity (each user one role max).
     * <p>
     *
     * @throws ManagementException
     * @throws GeneralSecurityException
     */
    void checkSubadminRoleValidity()
                            throws ManagementException, GeneralSecurityException {
        SecurityHelper.checkSubadminRoleValidity( access );
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
     * NOTE: This is static, because it must be usable even when the construction of the ADExporter
     * failed.
     * <p>
     *
     * @param e
     */
    public static void sendError( Exception e ) {

        try {
            String mailMessage = "Beim Synchronisieren des ActiveDirectory mit der HUIS-"
                                 + "Sicherheitsdatenbank ist ein Fehler aufgetreten.\n"
                                 + "Die Synchronisierung wurde NICHT durchgeführt, der letzte "
                                 + "Stand wurde wiederhergestellt.\n";
            StringWriter sw = new StringWriter();
            PrintWriter writer = new PrintWriter( sw );
            e.printStackTrace( writer );
            mailMessage += "\n\nDie Java-Fehlermeldung lautet:\n" + sw.getBuffer();

            mailMessage += "\n\nMit freundlichem Gruss,\nIhr ADExporter";
            MailHelper.createAndSendMail(
                                          new EMailMessage( mailSender, mailRcpt, "Fehler im ADExporter", mailMessage ),
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
            String mailMessage = "Die Synchronisierung der HUIS-Sicherheitsdatenbank mit "
                                 + "dem ActiveDirectory wurde erfolgreich durchgeführt:\n\n";
            if ( logBuffer.length() == 0 ) {
                mailMessage += "Keine Änderungen.";
            } else {
                mailMessage += logBuffer.toString();
            }
            mailMessage += "\n\nMit freundlichem Gruss,\nIhr ADExporter";
            EMailMessage emm = new EMailMessage( mailSender, mailRcpt, "ActiveDirectory Sychronisierung durchgeführt",
                                                 mailMessage );
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
            System.out.println( "USAGE: ADExporter configfile" );
            System.exit( 0 );
        }

        long begin = System.currentTimeMillis();
        System.out.println( "Beginning synchronisation..." );

        ActiveDirectoryImporter exporter = null;
        try {
            Properties config = new Properties();
            config.load( new FileInputStream( args[0] ) );
            exporter = new ActiveDirectoryImporter( config );

            HashMap<String, Group> groups = exporter.synchronizeGroups();
            exporter.synchronizeUsers( groups );
            exporter.updateSecAll();
            exporter.checkSubadminRoleValidity();
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
