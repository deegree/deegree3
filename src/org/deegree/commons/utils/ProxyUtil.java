//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.commons.utils;

import java.util.Properties;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public final class ProxyUtil {

    private static final String PROXY_HOST = "proxyHost";

    private static final String HTTP_PROXY_HOST = "http.proxyHost";

    private static final String FTP_PROXY_HOST = "ftp.proxyHost";

    private static final String PROXY_PORT = "proxyPort";

    private static final String HTTP_PROXY_PORT = "http.proxyPort";

    private static final String FTP_PROXY_PORT = "ftp.proxyPort";

    private static final String PROXY_USER = "proxyUser";

    private static final String HTTP_PROXY_USER = "http.proxyUser";

    private static final String FTP_PROXY_USER = "ftp.proxyUser";

    private static final String PROXY_PASSWORD = "proxyPassword";

    private static final String HTTP_PROXY_PASSWORD = "http.proxyPassword";

    private static final String FTP_PROXY_PASSWORD = "ftp.proxyPassword";

    private static final String NON_PROXY_HOSTS = "nonProxyHosts";

    private static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

    private static final String FTP_NON_PROXY_HOSTS = "ftp.nonProxyHosts";

    private static final String PROXY_SET = "proxySet";
    
    public synchronized static void setupProxyParameters( String proxyHost, String httpProxyHost, String ftpProxyHost,
                                             int proxyPort, int httpProxyPort, int ftpProxyPort, String proxyUser,
                                             String httpProxyUser, String ftpProxyUser, String proxyPassword,
                                             String httpProxyPassword, String ftpProxyPassword, String nonProxyHosts,
                                             String httpNonProxyHosts, String ftpNonProxyHosts, boolean overrideSystemSettings ) {

        Properties props = System.getProperties();

        if (overrideSystemSettings) {
            
        } else {
            
        }
    }
}
