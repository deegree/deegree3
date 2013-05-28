/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.console.proxy;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.deegree.commons.utils.ProxyUtils;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@ManagedBean
@RequestScoped
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
