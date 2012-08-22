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

package org.deegree.owscommon_1_1_0;

import java.util.List;

import org.deegree.framework.util.Pair;

/**
 * <code>ServiceIdentification</code> the service identification of owscommon 1.1.0
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */

public class ServiceIdentification extends DescriptionBase {

    private final List<String> serviceTypeVersions;

    private final List<String> accessConstraints;

    private final String fees;

    private final Pair<String, String> servicetype;

    private final List<String> profiles;

    /**
     * @param title
     * @param abstracts
     * @param keywords
     * @param servicetype
     *            a pair containing &lt; codetype, codespace &gt;
     * @param serviceTypeVersions
     * @param profiles
     * @param fees
     * @param accessConstraints
     */
    public ServiceIdentification( List<String> title, List<String> abstracts, List<Keywords> keywords,
                                  Pair<String, String> servicetype, List<String> serviceTypeVersions,
                                  List<String> profiles, String fees, List<String> accessConstraints ) {
        super( title, abstracts, keywords );
        this.servicetype = servicetype;
        this.serviceTypeVersions = serviceTypeVersions;
        this.profiles = profiles;
        this.fees = fees;
        this.accessConstraints = accessConstraints;
    }

    /**
     * @return the serviceTypeVersions.
     */
    public final List<String> getServiceTypeVersions() {
        return serviceTypeVersions;
    }

    /**
     * @return the accessConstraints.
     */
    public final List<String> getAccessConstraints() {
        return accessConstraints;
    }

    /**
     * @return the fees.
     */
    public final String getFees() {
        return fees;
    }

    /**
     * @return the codetype a pair containing &lt;codetype,codespace-attrib &gt;.
     */
    public final Pair<String, String> getServicetype() {
        return servicetype;
    }

    /**
     * @return the profiles.
     */
    public final List<String> getProfiles() {
        return profiles;
    }

}
