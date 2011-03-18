//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.console;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.deegree.commons.utils.ProxyUtils;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@ManagedBean
@ApplicationScoped
public class ProxyBean {

    public String getNonftpProxyHosts() {
        return ProxyUtils.getFtpNonProxyHosts( true );
    }

    public String getFtpProxyPassword() {
        return ProxyUtils.getFtpProxyPassword( true );
    }

    public String getFtpProxyUser() {
        return ProxyUtils.getFtpProxyUser( true );
    }

    public String getFtpProxyPort() {
        return ProxyUtils.getFtpProxyPort( true );
    }

    public String getFtpProxyHost() {
        return ProxyUtils.getFtpProxyHost( true );
    }

    public String getNonhttpProxyHosts() {
        return ProxyUtils.getHttpNonProxyHosts( true );
    }

    public String getHttpProxyPassword() {
        return ProxyUtils.getHttpProxyPassword( true );
    }

    public String getHttpProxyUser() {
        return ProxyUtils.getHttpProxyUser( true );
    }

    public String getHttpProxyPort() {
        return ProxyUtils.getHttpProxyPort( true );
    }

    public String getHttpProxyHost() {
        return ProxyUtils.getHttpProxyHost( true );
    }

    public String getNonProxyHosts() {
        return ProxyUtils.getNonProxyHosts();
    }

    public String getProxyPassword() {
        return ProxyUtils.getProxyPassword();
    }

    public String getProxyUser() {
        return ProxyUtils.getProxyUser();
    }

    public String getProxyPort() {
        return ProxyUtils.getProxyPort();
    }

    public String getProxyHost() {
        return ProxyUtils.getProxyHost();
    }

}
