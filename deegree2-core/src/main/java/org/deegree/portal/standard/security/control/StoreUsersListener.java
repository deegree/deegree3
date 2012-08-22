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
package org.deegree.portal.standard.security.control;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Vector;

import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCException;
import org.deegree.enterprise.control.RPCMember;
import org.deegree.enterprise.control.RPCMethodCall;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.i18n.Messages;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.SecurityTransaction;
import org.deegree.security.drm.model.User;

/**
 * This <code>Listener</code> reacts on 'storeUsers' events, extracts the contained user
 * definitions and updates the <code>SecurityManager</code> accordingly.
 *
 * Access constraints:
 * <ul>
 * <li>only users that have the 'SEC_ADMIN'-role are allowed</li>
 * </ul>
 *
 * @author <a href="mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class StoreUsersListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( StoreUsersListener.class );

    //ME: added for context chooser
    String usersDirectoryPath = null;

    String defaultContextName = null;

    String defaultContextPath = null;

    private final String STARTCONTEXT = "STARTCONTEXT";

    private final String CONTEXTNAME = "CONTEXT_NAME";

    @Override
    public void actionPerformed( FormEvent event ) {

        SecurityAccessManager manager = null;
        SecurityTransaction transaction = null;

        User[] users = null;

        try {
            RPCWebEvent ev = (RPCWebEvent) event;
            RPCMethodCall rpcCall = ev.getRPCMethodCall();
            RPCParameter[] params = rpcCall.getParameters();

            // ME: this part concerns extracting the usersdirectory member
            Vector<RPCParameter> vector = new Vector<RPCParameter>( params.length );
            for ( int i = 0; i < params.length; i++ ) {
                vector.add( params[i] );
            }

            boolean isUsersDirectoryFound = false;
            boolean isDefaultContextNameFound = false;
            boolean isDefaultContextPathFound = false;
            Iterator<RPCParameter> it = vector.iterator();

            while ( it.hasNext() ) {
                RPCStruct struct = (RPCStruct) it.next().getValue();
                RPCMember usersDirectoryRPC = struct.getMember( "usersDirectory" );
                RPCMember defCnNameRPC = struct.getMember( "defaultContextName" );
                RPCMember defCnPathRPC = struct.getMember( "defaultContextPath" );

                if ( usersDirectoryRPC != null ) {
                    if ( usersDirectoryRPC.getValue() instanceof String ) {
                        isUsersDirectoryFound = true;
                        usersDirectoryPath = (String) usersDirectoryRPC.getValue();
                    }
                }

                if ( defCnNameRPC != null ) {
                    if ( defCnNameRPC.getValue() instanceof String ) {
                        isDefaultContextNameFound = true;
                        defaultContextName = (String) defCnNameRPC.getValue();
                    }
                }

                if ( defCnPathRPC != null ) {
                    if ( defCnPathRPC.getValue() instanceof String ) {
                        isDefaultContextPathFound = true;
                        defaultContextPath = (String) defCnPathRPC.getValue();
                    }
                }
                if ( isUsersDirectoryFound || isDefaultContextNameFound || isDefaultContextPathFound ) {
                    it.remove();
                    break;
                }
            }

            params = new RPCParameter[vector.size()];
            for ( int i = 0; i < vector.size(); i++ ) {
                params[i] = vector.elementAt( i );
            }
            // ME: end

            // now extracting the members of each parameter
            users = new User[params.length];

            for ( int i = 0; i < params.length; i++ ) {
                if ( !( params[0].getValue() instanceof RPCStruct ) ) {
                    throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_STRUCT" ) );
                }
                RPCStruct struct = (RPCStruct) params[i].getValue();

                // extract user details
                RPCMember userIdRPC = struct.getMember( "userId" );
                RPCMember userNameRPC = struct.getMember( "userName" );
                RPCMember emailRPC = struct.getMember( "email" );
                RPCMember passwordRPC = struct.getMember( "password" );
                RPCMember firstNameRPC = struct.getMember( "firstName" );
                RPCMember lastNameRPC = struct.getMember( "lastName" );
                //ME: added for context chooser
                RPCMember contextNameRPC = struct.getMember( "contextName" );
                RPCMember contextPathRPC = struct.getMember( "contextPath" );

                int userId;
                String userName = null;
                String email = null;
                String password = null;
                String firstName = null;
                String lastName = null;
                //ME: added for context chooser
                String contextName = null;
                String contextPath = null;

                if ( userIdRPC == null ) {
                    throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_MEMBER", "user", "userId" ) );
                }
                if ( !( userIdRPC.getValue() instanceof String ) ) {
                    throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_MEMBER", "userId", "string" ) );
                }
                try {
                    userId = Integer.parseInt( ( (String) userIdRPC.getValue() ) );
                } catch ( NumberFormatException e ) {
                    throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_MEMBER", "userId", "integer" ) );
                }
                // extract userName
                if ( userNameRPC != null ) {
                    if ( !( userNameRPC.getValue() instanceof String ) ) {
                        throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_MEMBER", "userName", "string" ) );
                    }
                    userName = (String) userNameRPC.getValue();

                }
                // extract email
                if ( emailRPC != null ) {
                    if ( !( emailRPC.getValue() instanceof String ) ) {
                        throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_MEMBER", "email", "string" ) );
                    }
                    email = (String) emailRPC.getValue();

                }
                // extract password
                if ( passwordRPC != null ) {
                    if ( !( passwordRPC.getValue() instanceof String ) ) {
                        throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_MEMBER", "password", "string" ) );
                    }
                    password = (String) passwordRPC.getValue();

                }
                // extract firstName
                if ( firstNameRPC != null ) {
                    if ( !( firstNameRPC.getValue() instanceof String ) ) {
                        throw new RPCException(
                                                Messages.getMessage( "IGEO_STD_SEC_WRONG_MEMBER", "firstName", "string" ) );
                    }
                    firstName = (String) firstNameRPC.getValue();

                }
                // extract lastName
                if ( lastNameRPC != null ) {
                    if ( !( lastNameRPC.getValue() instanceof String ) ) {
                        throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_MEMBER", "lastName", "string" ) );
                    }
                    lastName = (String) lastNameRPC.getValue();

                }

                if ( userName == null ) {
                    throw new GeneralSecurityException( Messages.getMessage( "IGEO_STD_SEC_MISSING_MEMBER", "user",
                                                                             "name" ) );
                }
                if ( email == null ) {
                    throw new GeneralSecurityException( Messages.getMessage( "IGEO_STD_SEC_MISSING_MEMBER", "user",
                                                                             "email address" ) );
                }

                /*
                 * getting the value only in case this is not the default context or the param is
                 * missing
                 *
                 * if contextPathRPC==null, that means that no param is given, that means probably
                 * that the init parameter method in InitUserEditor didn't get anything from the
                 * conf_startcontext.xml
                 *
                 * otherwise if the contextPath is null we delete the users contexts, if it has a
                 * value we assign it this value
                 */

                LOG.logDebug( "userNameRPC: " + userName );
                LOG.logDebug( isUsersDirectoryFound ? "true" : "false" );
                LOG.logDebug( contextNameRPC != null ? "context name not null" : "context name null" );
                LOG.logDebug( contextPathRPC != null ? "contextPath not null" : "contextpath null" );

                if ( isUsersDirectoryFound && contextNameRPC != null ) {
                    contextName = (String) contextNameRPC.getValue();
                    if ( contextPathRPC != null ) {
                        contextPath = (String) contextPathRPC.getValue();
                    } else {
                        contextPath = null;
                    }
                    updateUserDirectory( userName, contextPath, contextName );
                }
                // ME: end

                users[i] = new User( userId, userName, password, firstName, lastName, email, null );
            }

            for ( int i = 0; i < users.length; i++ ) {
                LOG.logDebug( "id: " + users[i].getID() );
                LOG.logDebug( "firstName: " + users[i].getFirstName() );
                LOG.logDebug( "lastName: " + users[i].getLastName() );
                LOG.logDebug( "email: " + users[i].getEmailAddress() );
                LOG.logDebug( "password: " + users[i].getPassword() );
            }

            // get Transaction and perform access check
            manager = SecurityAccessManager.getInstance();
            transaction = SecurityHelper.acquireTransaction( this );
            SecurityHelper.checkForAdminRole( transaction );

            // remove deleted users
            User[] oldUsers = transaction.getAllUsers();
            for ( int i = 0; i < oldUsers.length; i++ ) {
                boolean deleted = true;
                for ( int j = 0; j < users.length; j++ ) {
                    if ( users[j].equals( oldUsers[i] ) ) {
                        deleted = false;
                    }
                }
                if ( oldUsers[i].getID() != User.ID_SEC_ADMIN && deleted ) {
                    transaction.deregisterUser( oldUsers[i] );
                }
            }

            // register all new users / update old users
            for ( int i = 0; i < users.length; i++ ) {
                if ( users[i].getID() == -1 ) {
                    transaction.registerUser( users[i].getName(), users[i].getPassword(), users[i].getLastName(),
                                              users[i].getFirstName(), users[i].getEmailAddress() );
                } else if ( users[i].getID() != User.ID_SEC_ADMIN ) {
                    transaction.updateUser( users[i] );
                }
            }
            manager.commitTransaction( transaction );
            transaction = null;

            getRequest().setAttribute( "MESSAGE", Messages.getMessage( "IGEO_STD_SEC_SUCCESS_INITUSEREDITOR" ) );
        } catch ( RPCException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", Messages.getMessage( "IGEO_STD_SEC_ERROR_CHANGE_REQ", e.getMessage() ) );
            setNextPage( "error.jsp" );
            LOG.logError( e.getMessage() );
        } catch ( GeneralSecurityException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE", Messages.getMessage( "IGEO_STD_SEC_ERROR_CHANGE", e.getMessage() ) );
            setNextPage( "error.jsp" );
            LOG.logError( e.getMessage(), e );
        } catch ( Exception e ) {
            LOG.logError( Messages.getMessage( "IGEO_STD_SEC_ERROR_UNKNOWN", StringTools.stackTraceToString( e ) ) );
        } finally {
            if ( manager != null && transaction != null ) {
                try {
                    manager.abortTransaction( transaction );
                } catch ( GeneralSecurityException ex ) {
                    LOG.logError( ex.getMessage(), ex );
                }
            }
        }
    }

    /**
     * ME: added for context chooser
     *
     * update the users directory, context.properties data, accroding to the value of contextPath
     * also creates a user folder in case it doesn't exist
     *
     * @param user
     * @param contextPath
     * @throws IOException
     * @throws ParseException
     *
     */
    private void updateUserDirectory( String user, String contextPath, String contextName )
                            throws IOException, ParseException {

        LOG.logDebug( "user name: " + user );
        File usersRootDirectory = new File( usersDirectoryPath );

        File userDirectory = null;
        try {
            userDirectory = new File( usersRootDirectory.getCanonicalPath() + "/" + user + "/" );
        } catch ( IOException e ) {
            LOG.logError( Messages.getMessage( "IGEO_STD_SEC_ERROR_CANONICAL_PATH",
                                               usersRootDirectory.getAbsolutePath() ) );
            throw new IOException( Messages.getMessage( "IGEO_STD_SEC_ERROR_CANONICAL_PATH",
                                                        usersRootDirectory.getAbsolutePath() ) );
        }

        LOG.logDebug( "context Path " + contextPath );
        if ( contextPath == null || contextPath.equals( "null" ) || contextPath.equals( "undefined" ) ) {
            LOG.logDebug( "trying to create a folder" );
            File userContextProperties = new File( userDirectory + "/" + "context.properties" );
            if ( userContextProperties.exists() ) {
                try {
                    if ( userContextProperties.delete() ) {
                        LOG.logDebug( "context deleted successfully" );
                    }

                    return;
                } catch ( SecurityException e ) {
                    LOG.logError( Messages.getMessage( "IGEO_STD_SEC_ERROR_ACCESSING_FILE", userContextProperties ) );
                    throw new SecurityException( Messages.getMessage( "IGEO_STD_SEC_ERROR_ACCESSING_FILE",
                                                                      userContextProperties ) );
                }
            } else {
                if ( !userDirectory.exists() ) {
                    userDirectory.mkdir();
                }
            }
            return;
        }

        if ( !userDirectory.exists() ) {
            userDirectory.mkdir();
        }
        // Here we know that the StartContext is not the default one
        File newContext = null;
        try {
            newContext = new File( userDirectory.getCanonicalPath() + "/" + "context.properties" );
        } catch ( IOException e ) {
            LOG.logError( Messages.getMessage( "IGEO_STD_SEC_ERROR_CANONICAL_PATH", userDirectory.getPath() ) );
        }
        if ( !userDirectory.exists() ) {
            userDirectory.mkdir();
        }

        // Here we will map the file path, in case the context doesn't exist
        // From the Drm-Admin directorty to the portal directory
        String outputPath = null;

        // String relativeUserDir= usersDirectoryPath + "/" + user;
        // LOG.logDebug("sourcePath: " + relativeUserDir);
        File absCntxtPath = new File( contextPath );
        String[] delimiters = { "\\", "/" };
        outputPath = RelativePath.mapRelativePath( userDirectory.getCanonicalPath(), absCntxtPath.getCanonicalPath(),
                                                   delimiters );

        try {
            BufferedWriter bufferedWriter = new BufferedWriter( new FileWriter( newContext.getCanonicalPath() ) );
            bufferedWriter.flush();
            StringBuffer buffer = new StringBuffer();
            buffer.append( STARTCONTEXT + '=' + outputPath + System.getProperty( "line.separator" ) );
            buffer.append( CONTEXTNAME + '=' + contextName + System.getProperty( "line.separator" ) );
            bufferedWriter.write( buffer.toString() );
            bufferedWriter.close();

        } catch ( IOException e ) {
            LOG.logError( Messages.getMessage( "IGEO_STD_SEC_ERROR_ACCESSING_FILE", newContext.getPath() ) );
            throw new IOException( Messages.getMessage( "IGEO_STD_SEC_ERROR_ACCESSING_FILE", newContext.getPath() ) );
        } catch ( Exception e ) {
            LOG.logError( Messages.getMessage( "IGEO_STD_SEC_ERROR_UNKNOWN", StringTools.stackTraceToString( e ) ) );
        }
    }
}
