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
package org.deegree.ogcwebservices.wcs.getcapabilities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.deegree.ogcwebservices.wcs.CoverageOfferingBrief;

/**
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class ContentMetadata implements Serializable {

    private String version = null;

    private String updateSequence = null;

    /**
     *
     */
    private CoverageOfferingBrief[] coverageOfferingBrief;

    private Map<String, CoverageOfferingBrief> map = new HashMap<String, CoverageOfferingBrief>( 100 );

    /**
     * @param version
     * @param updateSequence
     * @param coverageOfferingBrief
     */
    public ContentMetadata( String version, String updateSequence, CoverageOfferingBrief[] coverageOfferingBrief ) {

        this.version = version;
        this.updateSequence = updateSequence;
        setCoverageOfferingBrief( coverageOfferingBrief );
    }

    /**
     * @return Returns the coverageOfferingBrief.
     */
    public CoverageOfferingBrief[] getCoverageOfferingBrief() {
        return coverageOfferingBrief;
    }

    /**
     * returns the <tt>CoverageOfferingBrief<tt> for the coverage matching
     * the passed name. if no coverage with this name is available <tt>null</tt>
     * will be returned.
     *
     * @param coverageName
     * @return the <tt>CoverageOfferingBrief<tt> for the coverage matching
     * the passed name. if no coverage with this name is available <tt>null</tt>
     * will be returned.
     */
    public CoverageOfferingBrief getCoverageOfferingBrief( String coverageName ) {
        return map.get( coverageName );
    }

    /**
     * @param coverageOfferingBrief
     *            The coverageOfferingBrief to set.
     */
    public void setCoverageOfferingBrief( CoverageOfferingBrief[] coverageOfferingBrief ) {
        map.clear();
        this.coverageOfferingBrief = new CoverageOfferingBrief[coverageOfferingBrief.length];

        for ( int i = 0; i < coverageOfferingBrief.length; i++ ) {
            this.coverageOfferingBrief[i] = coverageOfferingBrief[i];
            map.put( coverageOfferingBrief[i].getName(), coverageOfferingBrief[i] );
        }
    }

    /**
     * @return Returns the updateSequence.
     */
    public String getUpdateSequence() {
        return updateSequence;
    }

    /**
     * @param updateSequence
     *            The updateSequence to set.
     */
    public void setUpdateSequence( String updateSequence ) {
        this.updateSequence = updateSequence;
    }

    /**
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version
     *            The version to set.
     */
    public void setVersion( String version ) {
        this.version = version;
    }

}
