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
package org.deegree.ogcwebservices.getcapabilities;

import java.util.Map;

import org.deegree.ogcwebservices.AbstractOGCWebServiceRequest;

/**
 * Each OGC Web Service must be able to describe its capabilities. This abstract base class defines
 * the structure intended to convey general information about the service itself, and summary
 * information about the available data.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$
 */
public abstract class GetCapabilities extends AbstractOGCWebServiceRequest {

    private static final long serialVersionUID = -7462466499142347260L;

    private String updateSequence;

    private String[] sections;

    private String[] acceptVersions;

    private String[] acceptFormats;

    /**
     * @param id
     * @param version
     * @param updateSequence
     * @param acceptVersions
     * @param sections
     * @param acceptFormats
     * @param vendorSpecificParameter
     */
    protected GetCapabilities( String id, String version, String updateSequence, String[] acceptVersions,
                               String[] sections, String[] acceptFormats, Map<String, String> vendorSpecificParameter ) {
        super( version, id, vendorSpecificParameter );
        this.updateSequence = updateSequence;
        this.sections = sections;
        this.acceptVersions = acceptVersions;
        this.acceptFormats = acceptFormats;
    }

    /**
     * @return Returns the capabilitiesSection.
     *
     */
    public String[] getSections() {
        return sections;
    }

    /**
     * @return acceptVersions
     */
    public String[] getAcceptVersions() {
        return acceptVersions;
    }

    /**
     * @return acceptFormats
     */
    public String[] getAcceptFormats() {
        return acceptFormats;
    }

    /**
     * @return Returns the updateSequence.
     */
    public String getUpdateSequence() {
        return updateSequence;
    }

}
