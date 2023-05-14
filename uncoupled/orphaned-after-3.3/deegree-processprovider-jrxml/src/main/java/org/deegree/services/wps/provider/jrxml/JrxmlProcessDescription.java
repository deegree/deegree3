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
package org.deegree.services.wps.provider.jrxml;

import java.net.URL;
import java.util.Map;

import org.deegree.services.wps.provider.jrxml.jaxb.process.ResourceBundle;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class JrxmlProcessDescription {

    private final String id;

    private final URL url;

    private final URL template;

    private final Map<String, URL> subreports;

    private final ResourceBundle resourceBundle;

    private final String description;

    private final Map<String, ParameterDescription> parameterDescriptions;

    public JrxmlProcessDescription( String id, URL url, String description,
                                    Map<String, ParameterDescription> parameterDescriptions, URL template,
                                    Map<String, URL> subreports, ResourceBundle resourceBundle ) {
        this.id = id;
        this.url = url;
        this.description = description;
        this.template = template;
        this.subreports = subreports;
        this.parameterDescriptions = parameterDescriptions;
        this.resourceBundle = resourceBundle;
    }

    String getId() {
        return id;
    }

    URL getUrl() {
        return url;
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public Map<String, URL> getSubreports() {
        return subreports;
    }

    public URL getTemplate() {
        return template;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, ParameterDescription> getParameterDescriptions() {
        return parameterDescriptions;
    }

}
