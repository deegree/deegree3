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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.security.GeneralSecurityException;
import org.deegree.security.drm.SecurityAccess;
import org.deegree.security.drm.model.User;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This <code>Listener</code> reacts on 'initUserEditor' events, queries the <code>SecurityManager</code> and passes the
 * group data on to be displayed by the JSP.
 * <p>
 * The internal "SEC_ADMIN" user is sorted out from the USERS parameter.
 * </p>
 * <p>
 * Access constraints:
 * <ul>
 * <li>only users that have the 'SEC_ADMIN'-role are allowed</li>
 * </ul>
 * </p>
 *
 * @author <a href="mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class InitUserEditorListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( InitUserEditorListener.class );

    private NamespaceContext cn = CommonNamespaces.getNamespaceContext();

    private File usersDirectory = null;

    private final String STARTCONTEXT = "STARTCONTEXT";

    private final String CONTEXTNAME = "CONTEXT_NAME";

    private String confPath = null;

    @Override
    public void actionPerformed( FormEvent event ) {

        try {
            // perform access check
            SecurityAccess access = SecurityHelper.acquireAccess( this );
            SecurityHelper.checkForAdminRole( access );

            getRequest().setAttribute( "ACCESS", access );
            User[] users = access.getAllUsers();
            User[] noAdminUsers = new User[users.length - 1];
            int j = 0;
            for ( int i = 0; i < users.length; i++ ) {
                if ( users[i].getID() != User.ID_SEC_ADMIN ) {
                    noAdminUsers[j++] = users[i];
                }
            }

            // read the file name which contains a list of our startContexts
            String configStartContexts = getInitParameter( "configFile" );
            // now we know there is a file path in that string

            // Start sending data to the jsp
            if ( configStartContexts != null && configStartContexts.length() != 0 ) {
                confPath = configStartContexts.substring( 0, configStartContexts.lastIndexOf( "/" ) + 1 );
                String confFilePath = getHomePath() + "/" + configStartContexts;
                /*
                 * String confFilePath = null; if ( getHomePath().endsWith( "/" ) ) { confFilePath = getHomePath() +
                 * configStartContexts; } else { confFilePath = getHomePath() + '/' + configStartContexts; }
                 */

                File absFilePath = new File( confFilePath );
                if ( absFilePath.exists() ) {
                    XMLFragment fragment = new XMLFragment( absFilePath.toURI().toURL() );
                    Map<String, StartContext> contextsList = parseContextsXml( fragment.getRootElement() );
                    Map<String, StartContext> usersList = parseUsersXml( fragment.getRootElement(), contextsList );

                    // sending the data to the jsp page
                    if ( contextsList == null ) {
                        LOG.logError( Messages.getMessage( "IGEO_STD_SEC_ERROR_CNXT_LIST_NULL" ) );
                    }
                    if ( usersList == null ) {
                        LOG.logError( Messages.getMessage( "IGEO_STD_SEC_ERROR_USERS_LIST_NULL" ) );
                    }

                    String usersDirectory = getHomePath() + confPath
                                            + getUsersDirectory( fragment.getRootElement() ).toString();
                    getRequest().setAttribute( "STARTCONTEXTSLIST", contextsList );
                    getRequest().setAttribute( "USERSCONTEXTLIST", usersList );
                    getRequest().setAttribute( "USERSDIRECTORY", usersDirectory );
                } else {
                    LOG.logDebug( "The configuration file could not be found: " + absFilePath.getCanonicalFile() );
                }
            } else {
                LOG.logInfo( Messages.getMessage( "IGEO_STD_SEC_MISSING_CNTXT_FILE_PATH", "configFile" ) );
            }

            getRequest().setAttribute( "USERS", noAdminUsers );
        } catch ( GeneralSecurityException e ) {
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE",
                                       Messages.getMessage( "IGEO_STD_SEC_FAIL_INIT_USER_EDITOR", e.getMessage() ) );
            setNextPage( "error.jsp" );
            LOG.logError( e.getMessage(), e );
        } catch ( Exception e ) {
            LOG.logError( Messages.getMessage( "IGEO_STD_SEC_ERROR_UNKNOWN", StringTools.stackTraceToString( e ) ) );
        }
    }

    /**
     * method used for context chooser
     *
     * @param rootElem
     * @return a [2] map Array that contains a list of the read contexts as a first element and a list of the user
     *         contexts as a second element.the array length should always be 2
     * @throws XMLParsingException
     * @throws ClientConfigurationException
     * @throws IOException
     * @throws DOMException
     */
    private Map<String, StartContext> parseContextsXml( Element rootElem )
                            throws XMLParsingException, ClientConfigurationException, DOMException, IOException {

        Hashtable<String, StartContext> contextsListHTable = new Hashtable<String, StartContext>();

        String xPath = "dgsec:availableWMC/dgsec:WMC";

        List<Node> list = XMLTools.getRequiredNodes( rootElem, xPath, cn );

        if ( list.isEmpty() ) {
            LOG.logError( Messages.getMessage( "IGEO_STD_SEC_MISSING_XML_NO_CONTEXTS" ) );
            throw new ClientConfigurationException( Messages.getMessage( "IGEO_STD_SEC_MISSING_XML_NO_CONTEXTS" ) );
        }
        contextsListHTable = new Hashtable<String, StartContext>( list.size() );

        Iterator<Node> it = list.iterator();
        // iterating through the context nodes
        while ( it.hasNext() ) {
            Node node = it.next();
            boolean isDefault = false;
            if ( node.hasAttributes() ) {
                Node defaultSelection = node.getAttributes().getNamedItem( "isDefault" );
                // if there exists the attribute "isDefault"
                if ( defaultSelection != null && defaultSelection.getNodeValue().compareTo( "1" ) == 0 ) {
                    isDefault = true;
                }
            }

            // extracting the name and path of the context
            Node nameNode = XMLTools.getNode( node, "dgsec:Name", cn );
            Node pathNode = XMLTools.getNode( node, "dgsec:URL", cn );
            if ( isDefault ) {
                updateDefaultContext( nameNode.getTextContent(), pathNode.getTextContent(), rootElem );
            }
            if ( nameNode == null ) {
                LOG.logError( Messages.getMessage( "IGEO_STD_SEC_MISSING_XML_ELEMENT", "name" ) );
                throw new ClientConfigurationException(
                                                        Messages.getMessage( "IGEO_STD_SEC_MISSING_XML_ELEMENT", "name" ) );
            }
            if ( pathNode == null ) {
                LOG.logError( Messages.getMessage( "IGEO_STD_SEC_MISSING_XML_ELEMENT", "path" ) );
                throw new ClientConfigurationException(
                                                        Messages.getMessage( "IGEO_STD_SEC_MISSING_XML_ELEMENT", "path" ) );
            }
            File contextPath = new File( getHomePath() + confPath + pathNode.getTextContent() );
            StartContext context = new StartContext( nameNode.getTextContent(), contextPath.getCanonicalPath(),
                                                     isDefault );
            contextsListHTable.put( context.getContextName(), context );
        }

        return contextsListHTable;
    }

    /**
     * Used for context chooser
     *
     * @param rootElem
     * @param contextsHT
     *            its the hashtable containing all the context read from the start_config.xml They will be useful to
     *            determine the context name
     * @return list of users contexts
     * @throws XMLParsingException
     * @throws ClientConfigurationException
     */
    private Map<String, StartContext> parseUsersXml( Element rootElem, Map<String, StartContext> contextsHT )
                            throws XMLParsingException, ClientConfigurationException {

        if ( usersDirectory == null ) {
            usersDirectory = getUsersDirectory( rootElem );
        }
        // Will be used to find context names, for these contexts with no names
        Collection<StartContext> contextsList = contextsHT.values();

        Map<String, StartContext> usersListHTable = new HashMap<String, StartContext>( 20 );

        // FileReader reader = null;
        String[] tempParts = null;

        // checking for the default context.properties
        File absUsersDirectory = new File( getHomePath() + confPath + usersDirectory );

        // the list of folders under the folder "users"
        String[] usersList = absUsersDirectory.list();

        try {
            // iterating through the users folders
            for ( int i = 0; i < usersList.length; i++ ) {
                File userFolder = new File( absUsersDirectory.getAbsoluteFile() + "/" + usersList[i] );
                if ( userFolder.isDirectory() ) {
                    // read the context properties of the user
                    File contextProps = new File( userFolder.getAbsoluteFile() + "/context.properties" );
                    if ( contextProps.exists() ) {
                        // reader = new FileReader( contextProps );
                        String line = getFirstLineMatch( contextProps.getCanonicalPath(), STARTCONTEXT );
                        if ( line != null && line.length() != 0 ) {
                            tempParts = line.split( "=" );
                            if ( tempParts.length < 2 ) {
                                throw new ClientConfigurationException( "the context.properties file in user '"
                                                                        + userFolder.getName() + "' is bad formatted" );
                            }
                            File contextPath = new File( userFolder.getCanonicalPath() + "/" + tempParts[1] );
                            line = getFirstLineMatch( contextProps.getCanonicalPath(), CONTEXTNAME );

                            String[] tempParts2 = null;
                            String contextName = null;
                            if ( line != null && line.length() > 0 ) {
                                tempParts2 = line.split( "=" );
                            } else {
                                contextName = findContextName( usersList[i], tempParts[1], contextsList );
                            }

                            if ( tempParts2 != null && tempParts2.length == 2 ) {
                                contextName = tempParts2[1];
                            }
                            usersListHTable.put( userFolder.getName(),
                                                 new StartContext( contextName, contextPath.getCanonicalPath(),
                                                                   new Boolean( false ) ) );
                            // reader.close();
                        }
                    }
                }
            }

        } catch ( Exception e ) {
            LOG.logError( Messages.getMessage( "IGEO_STD_SEC_ERROR_UNKNOWN", StringTools.stackTraceToString( e ) ) );
        }
        return usersListHTable;

    }

    /**
     * method used for context chooser
     *
     * @param rootElem
     * @return usserDirectory
     * @throws XMLParsingException
     * @throws ClientConfigurationException
     */
    private File getUsersDirectory( Element rootElem )
                            throws XMLParsingException, ClientConfigurationException {

        if ( usersDirectory != null ) {
            return usersDirectory;
        }

        Node userDirectoryNode = XMLTools.getNode( rootElem, "dgsec:UserDirectory", cn );
        if ( userDirectoryNode == null ) {
            LOG.logError( Messages.getMessage( "IGEO_STD_SEC_MISSING_XML_ELEMENT", "userdirectory" ) );
            throw new ClientConfigurationException( Messages.getMessage( "IGEO_STD_SEC_MISSING_XML_ELEMENT",
                                                                         "userdirectory" ) );
        }

        return new File( userDirectoryNode.getTextContent() );
    }

    /**
     * @param contextPath
     *            The path to search for in the contexts
     * @param contexts
     *            The list of contexts to look into
     * @return The found context name if nothing is found, the file name.xml will be used
     */
    private String findContextName( String user, String contextPath, Collection<StartContext> contexts ) {

        Iterator<StartContext> it = contexts.iterator();
        String[] delimiters = { "\\", "/" };

        File absUserDirectory = new File( getHomePath() + confPath + usersDirectory + "/" + user );

        while ( it.hasNext() ) {
            StartContext context = it.next();
            File absContextPath = new File( getHomePath() + confPath + context.getPath() );

            try {
                String mappedPath = RelativePath.mapRelativePath( absUserDirectory.getCanonicalPath(),
                                                                  absContextPath.getCanonicalPath(), delimiters );

                // We return the context name in which its path matches the path written in
                // context.properties
                if ( mappedPath.compareTo( contextPath ) == 0 ) {
                    return context.getContextName();
                }
            } catch ( Exception e ) {
                LOG.logError( Messages.getMessage( "IGEO_STD_SEC_ERROR_PATH_MAPPING",
                                                   absUserDirectory.getAbsolutePath(), absContextPath.getAbsolutePath() ) );
            }
        }

        int index = contextPath.lastIndexOf( "/" );
        String contextName = null;
        if ( index != -1 ) {
            contextName = contextPath.substring( index + 1 );
        }
        return contextName;
    }

    /**
     * @param target
     *            The string we are looking for
     * @return The whole line where our string exists
     */
    private String getFirstLineMatch( String filePath, String target ) {

        try {
            String line = null;
            BufferedReader buffer = new BufferedReader( new FileReader( filePath ) );
            while ( ( line = buffer.readLine() ) != null ) {
                if ( line.indexOf( target ) > -1 ) {
                    return line;
                }
            }
        } catch ( Exception e ) {
            LOG.logError( Messages.getMessage( "IGEO_STD_SEC_ERROR_READING_FILE", filePath ) );
        }
        return null;
    }

    /**
     * @param contextName
     * @param contextPath
     */
    private void updateDefaultContext( String contextName, String contextPath, Element rootElem ) {

        File usersFolder = null;
        File contextFile = null;
        try {
            usersFolder = new File( getHomePath() + confPath + getUsersDirectory( rootElem ) );
            File defaultContextFile = new File( getHomePath() + confPath + getUsersDirectory( rootElem )
                                                + "/context.properties" );
            contextFile = new File( getHomePath() + confPath + contextPath );

            String[] delimiters = { "\\", "/" };
            String mappedPath = RelativePath.mapRelativePath( usersFolder.getCanonicalPath(),
                                                              contextFile.getCanonicalPath(), delimiters );
            boolean pathChanged = false;
            if ( !defaultContextFile.exists() ) {
                defaultContextFile.createNewFile();
                pathChanged = true;
            }
            String line = getFirstLineMatch( defaultContextFile.getCanonicalPath(), "STARTCONTEXT" );
            if ( line != null && line.length() > 0 ) {
                String[] parts = line.split( "=" );
                if ( parts.length == 2 ) {
                    if ( parts[1].compareTo( mappedPath ) != 0 ) {
                        pathChanged = true;
                    }
                }
            }

            if ( pathChanged ) {
                BufferedWriter bufferedWriter = new BufferedWriter(
                                                                    new FileWriter(
                                                                                    defaultContextFile.getCanonicalPath() ) );
                bufferedWriter.flush();
                StringBuffer buffer = new StringBuffer();
                buffer.append( STARTCONTEXT + "=" + mappedPath + System.getProperty( "line.separator" ) );
                buffer.append( CONTEXTNAME + "=" + contextName + System.getProperty( "line.separator" ) );
                bufferedWriter.write( buffer.toString() );
                bufferedWriter.close();
                getRequest().setAttribute(
                                           "DEFAULTCHANGED",
                                           contextFile.getCanonicalPath().substring(
                                                                                     contextFile.getCanonicalPath().indexOf(
                                                                                                                             "WEB-INF" ) ) );
            }

        } catch ( ParseException e ) {
            LOG.logError( Messages.getMessage( "IGEO_STD_SEC_ERROR_PATH_MAPPING", usersFolder, contextFile ) );
        } catch ( IOException e ) {
            LOG.logError( Messages.getMessage( "IGEO_STD_SEC_ERROR_IO", StringTools.stackTraceToString( e ) ) );
        } catch ( ClientConfigurationException e ) {
            LOG.logError( Messages.getMessage( "IGEO_STD_SEC_ERROR_CLIENT_CONFIG", e.getMessage() ) );
        } catch ( Exception e ) {
            LOG.logError( Messages.getMessage( "IGEO_STD_SEC_ERROR_UNKNOWN", StringTools.stackTraceToString( e ) ) );
        }
    }
}
