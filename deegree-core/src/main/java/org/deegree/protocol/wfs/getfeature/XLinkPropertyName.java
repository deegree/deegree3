//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.protocol.wfs.getfeature;

import org.deegree.filter.expression.PropertyName;

/**
 * Specifies a feature property for which the resolving behaviour for xlink-references should be altered selectively.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class XLinkPropertyName {

    private final PropertyName propertyName;

    // positive Integer or "*" (unlimited)
    private final String traverseXlinkDepth;

    // using Integer instead of int here, so it can be null (unspecified)
    private final Integer traverseXlinkExpiry;

    /**
     * Creates a new {@link XLinkPropertyName} instance.
     *
     * @param propertyName
     *            name of the targeted property, never null
     * @param traverseXlinkDepth
     *            the depth to which nested property XLink linking element locator attribute (href) XLinks are traversed
     *            and resolved if possible, the range of valid values for this parameter consists of positive integers
     *            and "*" (unlimited), but must never be null
     * @param traverseXlinkExpiry
     *            indicates how long the WFS should wait to receive a response to a nested GetGmlObject request (in
     *            minutes), this attribute is only relevant if a value is specified for the traverseXlinkDepth
     *            attribute, may be null
     */
    public XLinkPropertyName( PropertyName propertyName, String traverseXlinkDepth, Integer traverseXlinkExpiry ) {
        this.propertyName = propertyName;
        this.traverseXlinkDepth = traverseXlinkDepth;
        this.traverseXlinkExpiry = traverseXlinkExpiry;
    }

    /**
     * Returns the targeted property name.
     *
     * @return the targeted property name, never null
     */
    public PropertyName getPropertyName() {
        return propertyName;
    }

    /**
     * Returns the depth to which nested property XLink linking element locator attribute (href) XLinks are traversed
     * and resolved if possible. The range of valid values for this parameter consists of positive integers and "*"
     * (unlimited).
     *
     * @return the depth (positive integer) or "*" (unlimited), never null
     */
    public String getTraverseXlinkDepth() {
        return traverseXlinkDepth;
    }

    /**
     * Return the number of minutes that the WFS should wait to receive a response to a nested GetGmlObject request.
     * This is only relevant if a value is specified for the traverseXlinkDepth parameter.
     *
     * @return the number of minutes to wait for nested GetGmlObject responses (positive integer) or null (unspecified)
     */
    public Integer getTraverseXlinkExpiry() {
        return traverseXlinkExpiry;
    }
}
