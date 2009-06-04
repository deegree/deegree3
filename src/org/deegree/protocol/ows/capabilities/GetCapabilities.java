//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/controller/ows/ServiceProviderXMLAdapter_1_1_0.java $
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de

 ---------------------------------------------------------------------------*/
package org.deegree.protocol.ows.capabilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deegree.commons.types.ows.Version;

/**
 * Generic representation of an OWS/OGC GetCapabilities request. Used for <code>GetCapabilities</code> requests to all
 * kinds of OGC web services.
 * <p>
 * Compliance has been checked with the following specifications:
 * <ul>
 * <li>OWS Common 1.0.0</li>
 * <li>OWS Common 1.1.0</li>
 * <li>WFS 1.0.0</li>
 * <li>WPS 1.0.0 (= OWS Commons 1.1.0 + multilanguage support)</li>
 * </ul>
 * </p>
 * <p>
 * Supports multilingual services according to OWS Common change request OGC 08-016r2. This is already used by the WPS
 * Specification 1.0.0.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GetCapabilities {

    private Version version;

    private List<Version> acceptVersions = new ArrayList<Version>();

    private Set<String> sections = new HashSet<String>();

    private Set<String> acceptFormats = new HashSet<String>();

    private String updateSequence;

    private List<String> languages = new ArrayList<String>();

    /**
     * Constructs a new <code>GetCapabilities</code> request.
     * 
     * @param acceptVersions
     *            acceptable protocol versions in order of client preference, may be empty or null
     * @param sections
     *            queried section names, may be empty or null
     * @param acceptFormats
     *            acceptable response formats, may be empty or null
     * @param updateSequence
     *            TODO (what does it do exactly), may be null
     * @param languages
     *            RFC 4646 language codes for human readable text (e.g. "en-CA,fr-CA"), may be emtpy or null
     */
    public GetCapabilities( Collection<Version> acceptVersions, Collection<String> sections,
                            Collection<String> acceptFormats, String updateSequence, Collection<String> languages ) {
        if ( acceptVersions != null ) {
            this.acceptVersions.addAll( acceptVersions );
        }
        if ( sections != null ) {
            this.sections.addAll( sections );
        }
        if ( acceptFormats != null ) {
            this.acceptFormats.addAll( acceptFormats );
        }
        this.updateSequence = updateSequence;
        if ( languages != null ) {
            this.languages.addAll( languages );
        }
    }

    /**
     * Constructs a new <code>GetCapabilities</code> request that specifies the requested version in a pre-OWS fashion
     * (see section D.11 of OGC 06-121r3).
     * 
     * @param version
     *            old-style version information, may be null
     */
    public GetCapabilities( Version version ) {
        this.version = version;
    }

    /**
     * Returns the old-style version information (used by pre-OWS GetCapabilities requests, see section D.11 of OGC
     * 06-121r3).
     * 
     * @return old-style version information, may be null (if this is an OWS-style request or an pre-OWS request without
     *         version specification)
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Returns the acceptable {@link Version}s in order of client preference (most preferred version comes first).
     * 
     * @return the acceptable <code>Version</code>s, in order of preference, may be empty, but not null
     */
    public List<Version> getAcceptVersions() {
        return acceptVersions;
    }

    /**
     * Returns the sections requested by the client.
     * 
     * @return the requested sections, may be empty, but not null
     */
    public Set<String> getSections() {
        return sections;
    }

    public Set<String> getAcceptFormats() {
        return acceptFormats;
    }

    public String getUpdateSequence() {
        return updateSequence;
    }

    /**
     * Returns the languages for human readable text requested by the client.
     * 
     * @return list of RFC 4646 language codes, may be empty, but not null
     */
    public List<String> getLanguages() {
        return languages;
    }
}
