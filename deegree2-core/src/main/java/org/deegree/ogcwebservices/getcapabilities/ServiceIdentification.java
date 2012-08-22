// $HeadURL$
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

import org.deegree.datatypes.Code;
import org.deegree.model.metadata.iso19115.Keywords;

/**
 * Represents the <code>ServiceIdentification</code> section of the capabilities of an OGC
 * compliant web service according to the <code>OGC Common Implementation Specification 0.2</code>.
 * This section corresponds to and expands the SV_ServiceIdentification class in ISO 19119.
 * <p>
 * It consists of the following elements: <table border="1">
 * <tr>
 * <th>Name</th>
 * <th>Occurences</th>
 * <th>Function</th>
 * </tr>
 * <tr>
 * <td>ServiceType</td>
 * <td align="center">1</td>
 * <td>Useful to provide service type name useful for machine-to-machine communication</td>
 * </tr>
 * <tr>
 * <td>ServiceTypeVersion</td>
 * <td align="center">1-*</td>
 * <td>Useful to provide list of server-supported versions.</td>
 * </tr>
 * <tr>
 * <td>Title</td>
 * <td align="center">1</td>
 * <td>Useful to provide a server title for display to a human.</td>
 * </tr>
 * <tr>
 * <td>Abstract</td>
 * <td align="center">0|1</td>
 * <td>Usually useful to provide narrative description of server, useful for display to a human.</td>
 * </tr>
 * <tr>
 * <td>Keywords</td>
 * <td align="center">0-*</td>
 * <td>Often useful to provide keywords useful for server searching.</td>
 * </tr>
 * <tr>
 * <td>Fees</td>
 * <td align="center">0|1</td>
 * <td>Usually useful to specify fees, or NONE if no fees.</td>
 * </tr>
 * <tr>
 * <td>AccessConstraints</td>
 * <td align="center">0-*</td>
 * <td>Usually useful to specify access constraints, or NONE if no access constraints.</td>
 * </tr>
 * </table>
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$
 */
public class ServiceIdentification {

    private String name;

    private Code serviceType;

    private String[] serviceTypeVersions;

    private String title;

    private String serviceAbstract;

    private Keywords[] keywords;

    private String fees;

    private String[] accessConstraints;

    /**
     * Constructs a new ServiceIdentification object.
     *
     * @param name
     *
     * @param serviceType
     * @param serviceTypeVersions
     * @param title
     * @param serviceAbstract
     *            may be null
     * @param keywords
     *            may be an empty array or null
     * @param fees
     *            may be null
     * @param accessConstraints
     *            may be an empty array or null
     */
    public ServiceIdentification( String name, Code serviceType, String[] serviceTypeVersions, String title,
                                  String serviceAbstract, Keywords[] keywords, String fees, String[] accessConstraints ) {
        this.name = name;
        this.serviceType = serviceType;
        this.serviceTypeVersions = serviceTypeVersions;
        this.title = title;
        this.serviceAbstract = serviceAbstract;
        this.keywords = keywords;
        this.fees = fees;
        this.accessConstraints = accessConstraints;
    }

    /**
     * Constructs a new ServiceIdentification object.
     *
     * @param serviceType
     * @param serviceTypeVersions
     * @param title
     * @param serviceAbstract
     *            may be null
     * @param keywords
     *            may be an empty array or null
     * @param fees
     *            may be null
     * @param accessConstraints
     *            may be an empty array or null
     */
    public ServiceIdentification( Code serviceType, String[] serviceTypeVersions, String title, String serviceAbstract,
                                  Keywords[] keywords, String fees, String[] accessConstraints ) {
        this.name = title;
        this.serviceType = serviceType;
        this.serviceTypeVersions = serviceTypeVersions;
        this.title = title;
        this.serviceAbstract = serviceAbstract;
        this.keywords = keywords;
        this.fees = fees;
        this.accessConstraints = accessConstraints;
    }

    /**
     * Returns the java representation of the ServiceType-element. In the XML document, this element
     * has the type ows:CodeType.
     *
     * @return the java representation of the ServiceType-element. In the XML document, this element
     *         has the type ows:CodeType.
     *
     */
    public Code getServiceType() {
        return serviceType;
    }

    /**
     * Returns the java representation of the ServiceTypeVersion-elements. In the XML document,
     * these elements have the type ows:VersionType.
     *
     * @return the java representation of the ServiceTypeVersion-elements. In the XML document,
     *         these elements have the type ows:VersionType.
     */
    public String[] getServiceTypeVersions() {
        return serviceTypeVersions;
    }

    /**
     * Returns the java representation of the Title-element. In the XML document, this element has
     * the type string.
     *
     * @return the java representation of the Title-element. In the XML document, this element has
     *         the type string.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the java representation of the Abstract-element. In the XML document, this element
     * has the type string.
     *
     * @return the java representation of the Abstract-element. In the XML document, this element
     *         has the type string.
     */
    public String getAbstract() {
        return serviceAbstract;
    }

    /**
     * Returns the java representation of the Keywords-elements. In the XML document, these elements
     * have the type ows:Keyword.
     *
     * @return the java representation of the Keywords-elements. In the XML document, these elements
     *         have the type ows:Keyword.
     */
    public Keywords[] getKeywords() {
        return keywords;
    }

    /**
     * Returns the java representation of the AccessConstraints-elements. In the XML document, these
     * elements have the type string.
     *
     * @return the java representation of the AccessConstraints-elements. In the XML document, these
     *         elements have the type string.
     */
    public String getFees() {
        return fees;
    }

    /**
     * Returns the java representation of the AccessConstraints-elements. In the XML document, these
     * elements have the type string.
     *
     * @return the java representation of the AccessConstraints-elements. In the XML document, these
     *         elements have the type string.
     */
    public String[] getAccessConstraints() {
        return accessConstraints;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

}
