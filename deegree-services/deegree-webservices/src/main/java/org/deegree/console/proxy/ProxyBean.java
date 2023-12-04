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

import java.io.IOException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.deegree.commons.proxy.ProxySettings;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
@ManagedBean
@RequestScoped
public class ProxyBean {

	public ProxyConfig getProxyConfig() throws IOException {
		return new ProxyConfig();
	}

	public String getNonftpProxyHosts() {
		return ProxySettings.getFtpNonProxyHosts(true);
	}

	public String getFtpProxyPassword() {
		return ProxySettings.getFtpProxyPassword(true);
	}

	public String getFtpProxyUser() {
		return ProxySettings.getFtpProxyUser(true);
	}

	public String getFtpProxyPort() {
		return ProxySettings.getFtpProxyPort(true);
	}

	public String getFtpProxyHost() {
		return ProxySettings.getFtpProxyHost(true);
	}

	public String getNonhttpProxyHosts() {
		return ProxySettings.getHttpNonProxyHosts(true);
	}

	public String getHttpProxyPassword() {
		return ProxySettings.getHttpProxyPassword(true);
	}

	public String getHttpProxyUser() {
		return ProxySettings.getHttpProxyUser(true);
	}

	public String getHttpProxyPort() {
		return ProxySettings.getHttpProxyPort(true);
	}

	public String getHttpProxyHost() {
		return ProxySettings.getHttpProxyHost(true);
	}

	public String getNonProxyHosts() {
		return ProxySettings.getNonProxyHosts();
	}

	public String getProxyPassword() {
		return ProxySettings.getProxyPassword();
	}

	public String getProxyUser() {
		return ProxySettings.getProxyUser();
	}

	public String getProxyPort() {
		return ProxySettings.getProxyPort();
	}

	public String getProxyHost() {
		return ProxySettings.getProxyHost();
	}

}
