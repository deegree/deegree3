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
    *   The class holds the elements which are needed to identify a Service
    *   according to OGC Web Services Common Specification  (Subclause 7.4.4) - ServiceIdentification
    *
    * 
    * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
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
    
    private String[] abstraCt;
    
    private String[] keywords;

    public String getServiceType(){
        return serviceType;
    }

    public void setServiceType(String serviceType){
        this.serviceType = serviceType;
    }

    public String getFees(){
        return fees;
    }

    public void setFees(String fees){
        this.fees = fees;
    }

    public String[] getAccessConstraints(){
        return accessConstraints;
    }

    public void setAccessConstraints(String[] accessConstraints){
        this.accessConstraints = accessConstraints;
    }

    public String[] getServiceTypeVersion(){
        return serviceTypeVersion;
    }

    public void setServiceTypeVersion(String[] serviceTypeVersion){
        this.serviceTypeVersion = serviceTypeVersion;
    }

    public String[] getProfile(){
        return profile;
    }

    public void setProfile(String[] profile){
        this.profile = profile;
    }

    public String[] getTitle(){
        return title;
    }

    public void setTitle(String[] title){
        this.title = title;
    }

    public String[] getAbstraCt(){
        return abstraCt;
    }

    public void setAbstraCt(String[] abstraCt){
        this.abstraCt = abstraCt;
    }

    public String[] getKeywords(){
        return keywords;
    }

    public void setKeywords(String[] keywords){
        this.keywords = keywords;
    }

}
