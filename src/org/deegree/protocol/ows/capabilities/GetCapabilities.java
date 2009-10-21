//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/controller/ows/ServiceProviderXMLAdapter_1_1_0.java $
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
package org.deegree.protocol.ows.capabilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deegree.commons.types.ows.Version;
import org.deegree.commons.utils.ArrayUtils;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;

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

    private String version;

    private List<String> acceptVersions = new ArrayList<String>();

    private Set<String> sections = new HashSet<String>();

    private Set<String> acceptFormats = new HashSet<String>();

    private String updateSequence;

    private List<String> languages = new ArrayList<String>();

    /**
     * Constructs a new <code>GetCapabilities</code> request.
     * 
     * @param acceptVersions
     *            acceptable protocol versions in order of client preference, may be empty or <code>null</code>
     * @param sections
     *            queried section names, may be empty or <code>null</code>
     * @param acceptFormats
     *            acceptable response formats, may be empty or <code>null</code>
     * @param updateSequence
     *            TODO (what does it do exactly), may be <code>null</code>
     * @param languages
     *            RFC 4646 language codes for human readable text (e.g. "en-CA,fr-CA"), may be emtpy or
     *            <code>null</code>
     */
    public GetCapabilities( Collection<String> acceptVersions, Collection<String> sections,
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
     *            old-style version information, may be <code>null</code>
     * @param sections
     *            queried section names, may be empty or <code>null</code>
     * @param acceptFormats
     *            acceptable response formats, may be empty or <code>null</code>
     * @param updateSequence
     *            TODO (what does it do exactly), may be <code>null</code>
     * @param languages
     *            RFC 4646 language codes for human readable text (e.g. "en-CA,fr-CA"), may be emtpy or
     *            <code>null</code>
     */
    public GetCapabilities( String version, Collection<String> sections, Collection<String> acceptFormats,
                            String updateSequence, Collection<String> languages ) {
        this.version = version;
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
     *            old-style version information, may be <code>null</code>
     */
    public GetCapabilities( String version ) {
        this.version = version;
    }

    /**
     * Constructs a new <code>GetCapabilities</code> request that specifies the requested version in a pre-OWS fashion
     * (see section D.11 of OGC 06-121r3).
     * 
     * @param version
     *            old-style version information, may be <code>null</code>
     */    
    public GetCapabilities( Version version ) {
        if (version != null) {
            this.version = version.toString();
        }
    }

    /**
     * Returns the old-style version information (used by pre-OWS GetCapabilities requests, see section D.11 of OGC
     * 06-121r3).
     * 
     * @return old-style version information, may be <code>null</code> (if this is an OWS-style request or an pre-OWS
     *         request without version specification)
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the old-style version information (used by pre-OWS GetCapabilities requests, see section D.11 of OGC
     * 06-121r3).
     * 
     * @return old-style version information, may be <code>null</code> (if this is an OWS-style request or an pre-OWS
     *         request without version specification)
     * @throws InvalidParameterValueException
     *             if any of the versions is not syntactically correct
     */
    public Version getVersionAsVersion()
                            throws InvalidParameterValueException {
        return Version.parseVersion( version );
    }

    /**
     * Returns the acceptable versions in order of client preference (most preferred version first).
     * 
     * @return the acceptable versions, in order of preference, may be empty, but not <code>null</code>
     */
    public List<String> getAcceptVersions() {
        return acceptVersions;
    }

    /**
     * Returns the acceptable versions in order of client preference (most preferred version first).
     * 
     * @see Version
     * 
     * @return the acceptable versions, in order of preference, may be empty, but not <code>null</code>
     * @throws InvalidParameterValueException
     *             if any of the versions is not syntactically correct
     */
    public List<Version> getAcceptVersionsAsVersions()
                            throws InvalidParameterValueException {
        List<Version> versions = new ArrayList<Version>( acceptVersions.size() );
        for ( String version : acceptVersions ) {
            versions.add( Version.parseVersion( version ) );
        }
        return versions;
    }

    /**
     * Returns the sections requested by the client.
     * 
     * @return the requested sections, may be empty, but not <code>null</code>
     */
    public Set<String> getSections() {
        return sections;
    }

    /**
     * Returns the formats accepted by the client.
     * 
     * @return the accepted formats, may be empty, but not <code>null</code>
     */
    public Set<String> getAcceptFormats() {
        return acceptFormats;
    }

    /**
     * Returns the update sequence value.
     * 
     * @return the update sequence value or <code>null</code> if unspecified
     */
    public String getUpdateSequence() {
        return updateSequence;
    }

    /**
     * Returns the languages for human readable text requested by the client.
     * 
     * @return list of RFC 4646 language codes, may be empty, but not <code>null</code>
     */
    public List<String> getLanguages() {
        return languages;
    }

    @Override
    public String toString() {
        String s = "{version=" + getVersion() + ",";
        s += "acceptVersions={" + ArrayUtils.join( ",", acceptVersions ) + "},";
        s += "sections={" + ArrayUtils.join( ",", sections ) + "},";
        s += "acceptFormats={" + ArrayUtils.join( ",", acceptFormats ) + "},";
        s += "updateSequence={" + updateSequence + "},";
        s += "languages={" + ArrayUtils.join( ",", languages ) + "}";
        s += "}";
        return s;
    }
}
