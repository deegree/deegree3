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
package org.deegree.ogcwebservices.wfs.configuration;

import java.io.File;

import org.deegree.enterprise.DeegreeParams;
import org.deegree.io.datastore.LockManager;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.ogcwebservices.wfs.WFService;

/**
 * Represents the specific <code>deegreeParams</code> section of the configuration document for a {@link WFService}
 * instance. This class encapsulates the deegree WFS specific parameters and inherits the parameters from the
 * {@link DeegreeParams} class.
 * <p>
 * It adds the following elements to the common <code>deegreeParams<code>:
 * <table border="1">
 * <tr>
 * <th>Name</th>
 * <th>Mandatory</th>
 * <th>Function</th>
 * </tr>
 * <tr>
 * <td>DataDirectoryList/td>
 * <td align="center">+</td>
 * <td>List of directories to be scanned for featuretypes/datastores to be served by the WFS.
 * </td>
 * <td>LockManagerDirectory/td>
 * <td align="center">-</td>
 * <td>Directory where information about locked features is stored.</td>
 * </td>
 * </tr>
 * </table>
 *
 * @see org.deegree.enterprise.DeegreeParams
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WFSDeegreeParams extends DeegreeParams {

    private static final long serialVersionUID = 2425998383206611958L;

    private String[] dataDirectories;

    private File lockManagerDirectory;

    private boolean checkUTMZones;

    private static boolean switchAxes;

    private boolean printGeomGmlIds;

    /**
     * Creates a new <code>WFSDeegreeParams</code> instance.
     *
     * @param defaultOnlineResource
     * @param cacheSize
     * @param requestTimeLimit
     * @param characterSet
     * @param dataDirectories
     * @param lockManagerDirectory
     *            may be null
     */
    WFSDeegreeParams( OnlineResource defaultOnlineResource, int cacheSize, int requestTimeLimit, String characterSet,
                      String[] dataDirectories, File lockManagerDirectory ) {
        super( defaultOnlineResource, cacheSize, requestTimeLimit, characterSet );
        this.dataDirectories = dataDirectories;
        this.lockManagerDirectory = lockManagerDirectory;
    }

    WFSDeegreeParams( OnlineResource defaultOnlineResource, int cacheSize, int requestTimeLimit, String characterSet,
                      String[] dataDirectories, File lockManagerDirectory, boolean checkUTMZones ) {
        this( defaultOnlineResource, cacheSize, requestTimeLimit, characterSet, dataDirectories, lockManagerDirectory );
        this.checkUTMZones = checkUTMZones;
    }

    WFSDeegreeParams( OnlineResource defaultOnlineResource, int cacheSize, int requestTimeLimit, String characterSet,
                      String[] dataDirectories, File lockManagerDirectory, boolean checkUTMZones, boolean switchAxes ) {
        this( defaultOnlineResource, cacheSize, requestTimeLimit, characterSet, dataDirectories, lockManagerDirectory,
              checkUTMZones );
        WFSDeegreeParams.switchAxes = switchAxes;
    }

    WFSDeegreeParams( OnlineResource defaultOnlineResource, int cacheSize, int requestTimeLimit, String characterSet,
                      String[] dataDirectories, File lockManagerDirectory, boolean checkUTMZones, boolean switchAxes,
                      boolean printGeomGmlIds ) {
        this( defaultOnlineResource, cacheSize, requestTimeLimit, characterSet, dataDirectories, lockManagerDirectory,
              checkUTMZones, switchAxes );
        this.printGeomGmlIds = printGeomGmlIds;
    }

    /**
     * Returns the resolved (absolute) data directory paths.
     *
     * @return the resolved (absolute) data directory paths
     */
    public String[] getDataDirectories() {
        return this.dataDirectories;
    }

    /**
     * Returns the directory where the {@link LockManager} will store its files.
     *
     * @return the directory where the {@link LockManager} will store its files, may be null
     */
    public File getLockManagerDirectory() {
        return this.lockManagerDirectory;
    }

    /**
     * @return whether to check for correct UTM zones in bounding boxes
     */
    public boolean checkUTMZones() {
        return checkUTMZones;
    }

    /**
     * Please note that this is a global setting!
     *
     * @return whether to switch x/y to y/x for EPSG 4326
     */
    public static boolean getSwitchAxes() {
        return switchAxes;
    }

    /**
     * Please note that this is a global setting!
     *
     * @return whether to print out GML ids for geometries.
     */
    public boolean printGeometryGmlIds() {
        return printGeomGmlIds;
    }

}
