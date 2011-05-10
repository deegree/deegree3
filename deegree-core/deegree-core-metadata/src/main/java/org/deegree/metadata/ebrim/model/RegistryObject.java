//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.metadata.ebrim.model;

import org.apache.axiom.om.OMElement;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class RegistryObject {

    private final String objectType;

    private final String id;

    private final String home;

    private final String lid;

    private final String status;

    private final String name;

    private final String desc;

    private final String versionInfo;

    private final String extId;

    private final OMElement element;

    public RegistryObject( String id, String home, String lid, String status, String name, String desc,
                           String versionInfo, String extId, String objectType, OMElement element ) {
        this.id = id;
        this.home = home;
        this.lid = lid;
        this.status = status;
        this.name = name;
        this.desc = desc;
        this.extId = extId;
        this.versionInfo = versionInfo;
        this.objectType = objectType;
        this.element = element;
    }

    public RegistryObject( RegistryObject ro ) {
        this.id = ro.getId();
        this.home = ro.getHome();
        this.lid = ro.getLid();
        this.status = ro.getStatus();
        this.name = ro.getName();
        this.desc = ro.getDesc();
        this.extId = ro.getExtId();
        this.versionInfo = ro.getVersionInfo();
        this.objectType = ro.getObjectType();
        this.element = ro.getElement();
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @return the extId
     */
    public String getExtId() {
        return extId;
    }

    /**
     * @return the home
     */
    public String getHome() {
        return home;
    }

    /**
     * @return the lid
     */
    public String getLid() {
        return lid;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the versionInfo
     */
    public String getVersionInfo() {
        return versionInfo;
    }

    /**
     * @return the objectType
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * @return the element
     */
    public OMElement getElement() {
        return element;
    }

}
