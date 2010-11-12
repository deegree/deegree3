//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.services.csw;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.NamespaceContext;

/**
 * Abstract base class for the requests of all operations.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public abstract class AbstractCSWRequest {

    private Version version;

    private NamespaceContext namespaces;

    private QName[] typeNames;

    private String outputFormat;

    /**
     * 
     * 
     * @param version
     * @param namespaces
     * @param typeNames
     * @param outputFormat
     */
    public AbstractCSWRequest( Version version, NamespaceContext namespaces, QName[] typeNames, String outputFormat ) {
        this.version = version;
        this.namespaces = namespaces;
        this.typeNames = typeNames;
        this.outputFormat = outputFormat;
    }

    /**
     * @return the version
     */
    public Version getVersion() {
        return version;
    }

    /**
     * @return the namespaces
     */
    public NamespaceContext getNamespaces() {
        return namespaces;
    }

    /**
     * @return the typeNames
     */
    public QName[] getTypeNames() {
        return typeNames;
    }

    /**
     * @return the outputFormat
     */
    public String getOutputFormat() {
        return outputFormat;
    }

}
