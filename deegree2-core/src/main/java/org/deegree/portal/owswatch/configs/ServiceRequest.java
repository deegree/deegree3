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

package org.deegree.portal.owswatch.configs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Holds the information about a certain request of a certain service, which http methods, this request cn use and what
 * are its htmlKeys
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ServiceRequest implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7463404275670442560L;

    private String name = null;

    private String getSnippetPath = null;

    private boolean canPOST = true;

    private boolean canGET = true;

    private String htmlText = null;

    private List<String> htmlKeys = null;

    // Reads an example xml file for a post request, will be used mainly for GetCapabilities. but its also possbible
    // to assign it to any request type
    private String postRequest = null;

    /**
     * A constructor to instantate a class describing the service name and its corresponding html code that accepts
     * specific paramters for this request
     *
     * @param name
     * @param getSnippetPath
     * @param postSnippetPath
     * @param canPOST
     * @param canGET
     * @param htmlKeys
     * @throws IOException
     */
    public ServiceRequest( String name, String getSnippetPath, String postSnippetPath, boolean canPOST, boolean canGET,
                           List<String> htmlKeys ) throws IOException {
        this.name = name;
        this.canPOST = canPOST;
        this.canGET = canGET;
        this.htmlText = readFileToString( getSnippetPath );
        this.postRequest = readFileToString( postSnippetPath );
        this.htmlKeys = htmlKeys;

    }

    /**
     * Reads the html snippet file and save it as a String variable to be used later by the DHTML
     *
     * @param filePath
     * @return String
     * @throws IOException
     */
    protected String readFileToString( String filePath )
                            throws IOException {

        if ( filePath == null || filePath.length() == 0 ) {
            // A Html Text is not necessary in all cases, ex. in case GetCapabilities there
            // are no extra fields
            return "";
        }

        File file = new File( filePath );
        BufferedReader reader = new BufferedReader( new FileReader( file.getCanonicalFile() ) );
        String line = reader.readLine();
        StringBuilder builder = new StringBuilder( 500 );
        while ( line != null ) {
            builder.append( line );
            line = reader.readLine();
        }
        return builder.toString();
    }

    /**
     * @return Request name, ex GetCapabilities
     */
    public String getName() {
        return name;
    }

    /**
     * @return path to html code describing the parameters for this service
     */
    public String getValue() {
        return getSnippetPath;
    }

    /**
     * @return can send GET Requests
     */
    public boolean isCanGET() {
        return canGET;
    }

    /**
     * @return can send POST requests
     */
    public boolean isCanPOST() {
        return canPOST;
    }

    /**
     * @return String
     */
    public String getHtmlText() {
        return htmlText;
    }

    /**
     * @return List of HTML keys
     */
    public List<String> getHtmlKeys() {
        return htmlKeys;
    }

    /**
     * Reads an example xml file for a post request, will be used mainly for GetCapabilities. but its also possbible to
     * assign it to any request type
     *
     * @return String
     */
    public String getPostRequest() {
        return postRequest;
    }
}
