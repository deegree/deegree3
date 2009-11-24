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
package org.deegree.record.persistence;

import java.sql.SQLException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.configuration.JDBCConnections;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.record.persistence.sqltransform.postgres.TransformatorPostGres;

/**
 * Base interface of the {@link Record} persistence layer, provides access to stored {@link Record} instances and their
 * schemas.
 * <p>
 * NOTE: One {@link RecordStore} instance corresponds to one metadata format (e.g. DublinCore, MD_Metadata (ISO TC211),
 * SV_Service (ISO TC211)).
 * </p>
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public interface RecordStore {

    /**
     * Called by the container to indicate that this {@link RecordStore} instance is being placed into service.
     * 
     * @throws FeatureStoreException
     *             if the initialization fails
     */
    public void init()
                            throws RecordStoreException;

    /**
     * Called by the container to indicate that this {@link RecordStoreException} instance is being taken out of
     * service.
     */
    public void destroy();

    /**
     * Exports the XML schema for the associated metadata format.
     * 
     * @param xmlWriter
     *            writer to export to, must not be <code>null</code>
     */
    public void describeRecord();

    /**
     * 
     * Exports the XML for the requested records.
     * 
     * @param writer
     *            writer to export to, must not be <code>null</code>
     * @param typeName
     *            typeName for the requested record
     * @param connection
     *            JDBC connection attributes
     * @param filterTransformator
     *            PostGresFilterTransformator that transforms the filterexpression into an SQL compatible fragment
     * @throws SQLException
     * @throws XMLStreamException
     */
    public void getRecords( XMLStreamWriter writer, QName typeName, JDBCConnections connection,
                            TransformatorPostGres filterTransformator )
                            throws SQLException, XMLStreamException;

    /**
     * Returns the TypeNames that are known in the Backend
     * 
     * @return QName
     */
    public QName getTypeName();

}
