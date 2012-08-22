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
package org.deegree.portal.context;

/**
 * Implements the access to the IO settings of a Web Map Context.
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public class IOSettings {

    private DirectoryAccess downloadDirectory = null;

    private DirectoryAccess sLDDirectory = null;

    private DirectoryAccess printDirectory = null;

    private DirectoryAccess tempDirectory = null;

    /**
     * @param downloadDirectory
     *            directory to store temoprary files for downloading
     * @param sLDDirectory
     *            directory for storing temporary SLD files
     * @param printDirectory
     *            directory for storing temporary for print-views
     */
    IOSettings( DirectoryAccess downloadDirectory, DirectoryAccess sLDDirectory, DirectoryAccess printDirectory,
                DirectoryAccess tempDirectory ) {
        this.downloadDirectory = downloadDirectory;
        this.sLDDirectory = sLDDirectory;
        this.printDirectory = printDirectory;
        this.tempDirectory = tempDirectory;
    }

    /**
     * @return Returns the downloadDirectory.
     */
    public DirectoryAccess getDownloadDirectory() {
        return downloadDirectory;
    }

    /**
     * @param downloadDirectory
     *            The downloadDirectory to set.
     */
    public void setDownloadDirectory( DirectoryAccess downloadDirectory ) {
        this.downloadDirectory = downloadDirectory;
    }

    /**
     * @return Returns the printDirectory.
     */
    public DirectoryAccess getPrintDirectory() {
        return printDirectory;
    }

    /**
     * @param printDirectory
     *            The printDirectory to set.
     */
    public void setPrintDirectory( DirectoryAccess printDirectory ) {
        this.printDirectory = printDirectory;
    }

    /**
     * @return Returns the sLDDirectory.
     */
    public DirectoryAccess getSLDDirectory() {
        return sLDDirectory;
    }

    /**
     * @param directory
     *            The sLDDirectory to set.
     */
    public void setSLDDirectory( DirectoryAccess directory ) {
        sLDDirectory = directory;
    }

    /**
     * @return Returns the tempDirectory.
     */
    public DirectoryAccess getTempDirectory() {
        return tempDirectory;
    }

    /**
     * @param tempDirectory
     *            The tempDirectory to set.
     */
    public void setTempDirectory( DirectoryAccess tempDirectory ) {
        this.tempDirectory = tempDirectory;
    }

}
