// $HeadURL:
// /cvsroot/deegree/src/org/deegree/ogcwebservices/getcapabilities/Contents.java,v
// 1.1 2004/06/23 11:55:40 mschneider Exp $
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
package org.deegree.model.filterencoding.capabilities;

/**
 * FilterCapabilitiesBean used to represent <code>Filter<code> expressions according to the
 * 1.0.0 as well as the 1.1.1 <code>Filter Encoding Implementation Specification</code>.
 *
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 *
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class FilterCapabilities {

    /**
     *
     */
    public static final String VERSION_100 = "1.0.0";

    /**
     *
     */
    public static final String VERSION_110 = "1.1.0";

    private ScalarCapabilities scalarCapabilities;

    private SpatialCapabilities spatialCapabilities;

    private IdCapabilities idCapabilities;

    private String version;

    /**
     * Constructs a new <code>FilterCapabilities</code> -instance with the given parameters. Used
     * for filter expressions according to the 1.0.0 specification that don't have an
     * <code>Id_Capabilities</code> section.
     *
     * @param scalarCapabilities
     * @param spatialCapabilities
     */
    public FilterCapabilities( ScalarCapabilities scalarCapabilities, SpatialCapabilities spatialCapabilities ) {
        this.scalarCapabilities = scalarCapabilities;
        this.spatialCapabilities = spatialCapabilities;
        this.version = VERSION_100;
    }

    /**
     * Constructs a new <code>FilterCapabilities</code> -instance with the given parameters. Used
     * for filter expressions according to the 1.1.0 specification that have an
     * <code>Id_Capabilities</code> section.
     *
     * @param scalarCapabilities
     * @param spatialCapabilities
     * @param idCapabilities
     */
    public FilterCapabilities( ScalarCapabilities scalarCapabilities, SpatialCapabilities spatialCapabilities,
                               IdCapabilities idCapabilities ) {
        this.scalarCapabilities = scalarCapabilities;
        this.spatialCapabilities = spatialCapabilities;
        this.idCapabilities = idCapabilities;
        this.version = VERSION_110;
    }

    /**
     * @return scalarCapabilities
     *
     */
    public ScalarCapabilities getScalarCapabilities() {
        return scalarCapabilities;
    }

    /**
     * @return spatialCapabilities
     *
     */
    public SpatialCapabilities getSpatialCapabilities() {
        return spatialCapabilities;
    }

    /**
     * @param capabilities
     *
     */
    public void setScalarCapabilities( ScalarCapabilities capabilities ) {
        scalarCapabilities = capabilities;
    }

    /**
     * @param capabilities
     *
     */
    public void setSpatialCapabilities( SpatialCapabilities capabilities ) {
        spatialCapabilities = capabilities;
    }

    /**
     * @return Returns the idCapabilities.
     */
    public IdCapabilities getIdCapabilities() {
        return idCapabilities;
    }

    /**
     * @param idCapabilities
     *            The idCapabilities to set.
     */
    public void setIdCapabilities( IdCapabilities idCapabilities ) {
        this.idCapabilities = idCapabilities;
    }

    /**
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }
}
