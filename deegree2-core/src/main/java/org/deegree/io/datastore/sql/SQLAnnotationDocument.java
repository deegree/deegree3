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

package org.deegree.io.datastore.sql;

import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.io.IODocument;
import org.deegree.io.JDBCConnection;
import org.deegree.io.datastore.AnnotationDocument;
import org.deegree.io.datastore.Datastore;
import org.w3c.dom.Element;

/**
 * Handles the annotation parsing for SQL based datastores.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SQLAnnotationDocument extends AnnotationDocument {

    private static final long serialVersionUID = -6663755656885555966L;

    private Class<? extends Datastore> datastoreClass;

    /**
     * Creates a new instance of {@link SQLAnnotationDocument} for the given datastore class.
     *
     * @param datastoreClass
     */
    public SQLAnnotationDocument (Class<? extends Datastore> datastoreClass) {
        this.datastoreClass = datastoreClass;
    }

    @Override
    public SQLDatastoreConfiguration parseDatastoreConfiguration() throws XMLParsingException {
        Element appinfoElement = (Element) XMLTools.getRequiredNode( getRootElement(), "xs:annotation/xs:appinfo",
                                                                     nsContext );
        IODocument ioDoc = new IODocument( (Element) XMLTools.getRequiredNode( appinfoElement, "dgjdbc:JDBCConnection",
                                                                               nsContext ) );
        ioDoc.setSystemId( this.getSystemId() );
        JDBCConnection jdbcConnection = ioDoc.parseJDBCConnection();
        return new SQLDatastoreConfiguration( jdbcConnection, datastoreClass );
    }
}
