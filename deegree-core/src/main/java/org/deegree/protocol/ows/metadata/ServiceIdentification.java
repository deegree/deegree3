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
package org.deegree.protocol.ows.metadata;

import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.Version;

/**
 * The <code>ServiceIdentification</code> bean encapsulates the corresponding GetCapabilities response metadata element.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class ServiceIdentification {

    private Description description;

    private CodeType serviceType;

    private List<Version> serviceTypeVersion;

    private List<String> profiles;

    private String fees;

    private List<String> accessConstraints;

    /**
     * @param description
     */
    public void setDescription( Description description ) {
        this.description = description;
    }

    /**
     * @return description, may be <code>null</code>.
     */
    public Description getDescription() {
        return description;
    }

    /**
     * @param serviceType
     */
    public void setServiceType( CodeType serviceType ) {
        this.serviceType = serviceType;
    }

    /**
     * @return serviceType, may be <code>null</code>.
     */
    public CodeType getServiceType() {
        return serviceType;
    }

    /**
     * @return serviceTypeVersion, may be empty but never <code>null</code>.
     */
    public List<Version> getServiceTypeVersion() {
        if ( serviceTypeVersion == null ) {
            serviceTypeVersion = new ArrayList<Version>();
        }
        return serviceTypeVersion;
    }

    /**
     * @return profiles, may be empty but never <code>null</code>.
     */
    public List<String> getProfiles() {
        if ( profiles == null ) {
            profiles = new ArrayList<String>();
        }
        return profiles;
    }

    /**
     * @param fees
     */
    public void setFees( String fees ) {
        this.fees = fees;
    }

    /**
     * @return accessConstraints, may be <code>null</code>.
     */
    public List<String> getAccessConstraints() {
        if ( accessConstraints == null ) {
            accessConstraints = new ArrayList<String>();
        }
        return accessConstraints;
    }

}
