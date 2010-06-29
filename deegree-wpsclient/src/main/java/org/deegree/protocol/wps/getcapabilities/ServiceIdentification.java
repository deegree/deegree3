//$HeadURL$
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
package org.deegree.protocol.wps.getcapabilities;

/**
 * 
 /**
 * 
 * Represents the ServiceIdentification section of the GetCapabilties Document of the WPS specification 1.0
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ServiceIdentification {

    private String serviceType;

    private String fees;

    private String[] accessConstraints;

    private String[] serviceTypeVersion;

    private String[] profile;

    private String[] title;

    private String[] _abstract;

    private String[] keywords;

    /**
     * 
     * @return serviceType
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * 
     * @param serviceType
     */
    public void setServiceType( String serviceType ) {
        this.serviceType = serviceType;
    }

    /**
     * 
     * @return fees
     */
    public String getFees() {
        return fees;
    }

    /**
     * 
     * @param fees
     */
    public void setFees( String fees ) {
        this.fees = fees;
    }

    /**
     * 
     * @return accessConstraints
     */
    public String[] getAccessConstraints() {
        return accessConstraints;
    }

    /**
     * 
     * @param accessConstraints
     */
    public void setAccessConstraints( String[] accessConstraints ) {
        this.accessConstraints = accessConstraints;
    }

    /**
     * 
     * @return serviceTypeVersion
     */
    public String[] getServiceTypeVersion() {
        return serviceTypeVersion;
    }

    /**
     * 
     * @param serviceTypeVersion
     */
    public void setServiceTypeVersion( String[] serviceTypeVersion ) {
        this.serviceTypeVersion = serviceTypeVersion;
    }

    /**
     * 
     * @return profile
     */
    public String[] getProfile() {
        return profile;
    }

    /**
     * 
     * @param profile
     */
    public void setProfile( String[] profile ) {
        this.profile = profile;
    }

    /**
     * 
     * @return title
     */
    public String[] getTitle() {
        return title;
    }

    /**
     * 
     * @param title
     */
    public void setTitle( String[] title ) {
        this.title = title;
    }

    /**
     * 
     * @return abstraCt
     */
    public String[] getAbstraCt() {
        return _abstract;
    }

    /**
     * 
     * @param _abstract
     */
    public void setAbstraCt( String[] _abstract ) {
        this._abstract = _abstract;
    }

    /**
     * 
     * @return keywords
     */
    public String[] getKeywords() {
        return keywords;
    }

    /**
     * 
     * @param keywords
     */
    public void setKeywords( String[] keywords ) {
        this.keywords = keywords;
    }

}
