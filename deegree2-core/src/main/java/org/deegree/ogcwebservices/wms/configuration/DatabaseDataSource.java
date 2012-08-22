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
package org.deegree.ogcwebservices.wms.configuration;

import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.io.JDBCConnection;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wms.capabilities.ScaleHint;

/**
 *
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class DatabaseDataSource extends AbstractDataSource {

    private JDBCConnection jdbc;

    private String sqlTemplate;

    private String geomeryField;

    private CoordinateSystem nativeCRS;

    private final boolean customSQL;

    /**
     *
     * @param queryable
     * @param failOnException
     * @param name
     * @param scaleHint
     * @param validArea
     * @param reqTimeLimit
     * @param jdbc
     * @param sqlTemplate
     * @param geomeryField
     * @param nativeCRS
     */
    public DatabaseDataSource( boolean queryable, boolean failOnException, QualifiedName name, ScaleHint scaleHint,
                               Geometry validArea, int reqTimeLimit, JDBCConnection jdbc, String sqlTemplate,
                               String geomeryField, CoordinateSystem nativeCRS ) {
        this( queryable, failOnException, name, scaleHint, validArea, reqTimeLimit, jdbc, sqlTemplate, geomeryField,
              nativeCRS, false );
    }

    /**
     * @param queryable
     * @param failOnException
     * @param name
     * @param scaleHint
     * @param validArea
     * @param reqTimeLimit
     * @param jdbc
     * @param sqlTemplate
     * @param geometryField
     * @param nativeCRS
     * @param customSQL
     */
    public DatabaseDataSource( boolean queryable, boolean failOnException, QualifiedName name, ScaleHint scaleHint,
                               Geometry validArea, int reqTimeLimit, JDBCConnection jdbc, String sqlTemplate,
                               String geometryField, CoordinateSystem nativeCRS, boolean customSQL ) {
        this( queryable, failOnException, name, scaleHint, validArea, reqTimeLimit, jdbc, sqlTemplate, geometryField,
              nativeCRS, customSQL, null );
    }

    /**
     * @param queryable
     * @param failOnException
     * @param name
     * @param scaleHint
     * @param validArea
     * @param reqTimeLimit
     * @param jdbc
     * @param sqlTemplate
     * @param geomeryField
     * @param nativeCRS
     * @param customSQL
     * @param dimProps
     */
    public DatabaseDataSource( boolean queryable, boolean failOnException, QualifiedName name, ScaleHint scaleHint,
                               Geometry validArea, int reqTimeLimit, JDBCConnection jdbc, String sqlTemplate,
                               String geomeryField, CoordinateSystem nativeCRS, boolean customSQL,
                               Map<String, String> dimProps ) {
        super( queryable, failOnException, name, DATABASE, null, null, scaleHint, validArea, null, reqTimeLimit,
               dimProps );
        this.jdbc = jdbc;
        this.sqlTemplate = sqlTemplate;
        this.geomeryField = geomeryField;
        this.nativeCRS = nativeCRS;
        this.customSQL = customSQL;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.ogcwebservices.wms.configuration.AbstractDataSource#getOGCWebService()
     */
    @Override
    public OGCWebService getOGCWebService()
                            throws OGCWebServiceException {
        return null;
    }

    /**
     *
     * @return database connection description
     */
    public JDBCConnection getJDBCConnection() {
        return jdbc;
    }

    /**
     * @return the geomeryField
     */
    public String getGeometryFieldName() {
        return geomeryField;
    }

    /**
     * @return the sqlTemplate
     */
    public String getSqlTemplate() {
        return sqlTemplate;
    }

    /**
     * @return the nativeCRS
     */
    public CoordinateSystem getNativeCRS() {
        return nativeCRS;
    }

    /**
     * @return true, if sending custom SQL templates is allowed
     */
    public boolean isCustomSQLAllowed() {
        return customSQL;
    }

}
