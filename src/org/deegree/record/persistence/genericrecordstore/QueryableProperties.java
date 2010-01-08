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
package org.deegree.record.persistence.genericrecordstore;

import java.util.List;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.types.datetime.Date;

/**
 * Properties that are queryable by applicationprofiles such as ISO 
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class QueryableProperties {
    
    private List<String> subject;
    
    private List<String> title;
    
    private List<String> _abstract;
    
    private String anyText;
    
    private List<Format> format;
    
    private String identifier;
    
    private List<Date> modified;
    
    private String type;
    
    private BoundingBox boundingBox;
    
    private OMElement identificationInfo;
    
    private CRS crs;
    
    private OMElement language;
    
    private List<Keyword> keywords;
    
    private OMElement serviceType;
    
    private OMElement serviceTypeVersion;
    
    
    
    public QueryableProperties(){
        
    }
    
    /*public QueryableProperties(List<String> subject, List<OMElement> title, OMElement language, List<String> abridgement, String anyText, 
                               List<String> format, OMElement identifier, List<Date> modified, String type, 
                               BoundingBox boundingBox, CRS crs){
        this.subject = subject;
        this.title = title;
        this.language = language;
        this.abridgement = abridgement;
        this.anyText = anyText;
        this.format = format;
        this.identifier = identifier;
        this.modified = modified;
        this.type = type;
        this.boundingBox = boundingBox;
        this.crs = crs;
        
    }*/

    /**
     * @return the subject
     */
    public List<String> getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject( List<String> subject ) {
        this.subject = subject;
    }

    
    /**
     * @return the identificationInfo
     */
    public OMElement getIdentificationInfo() {
        return identificationInfo;
    }

    /**
     * @param identificationInfo the identificationInfo to set
     */
    public void setIdentificationInfo( OMElement identificationInfo ) {
        this.identificationInfo = identificationInfo;
    }

    /**
     * @return the abridgement
     */
    public List<String> get_abstract() {
        return _abstract;
    }

    /**
     * @param abridgement the abridgement to set
     */
    public void set_abstract( List<String> _abstract ) {
        this._abstract = _abstract;
    }

    /**
     * @return the anyText
     */
    public String getAnyText() {
        return anyText;
    }

    /**
     * @param anyText the anyText to set
     */
    public void setAnyText( String anyText ) {
        this.anyText = anyText;
    }

    /**
     * @return the format
     */
    public List<Format> getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat( List<Format> format ) {
        this.format = format;
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    public void setIdentifier( String identifier ) {
        this.identifier = identifier;
    }

    /**
     * @return the modified
     */
    public List<Date> getModified() {
        return modified;
    }

    /**
     * @param modified the modified to set
     */
    public void setModified( List<Date> modified ) {
        this.modified = modified;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType( String type ) {
        this.type = type;
    }

    

    /**
     * @return the crs
     */
    public CRS getCrs() {
        return crs;
    }

    /**
     * @param crs the crs to set
     */
    public void setCrs( CRS crs ) {
        this.crs = crs;
    }

    /**
     * @return the language
     */
    public OMElement getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage( OMElement language ) {
        this.language = language;
    }

    
    /**
     * @return the serviceType
     */
    public OMElement getServiceType() {
        return serviceType;
    }

    /**
     * @param serviceType the serviceType to set
     */
    public void setServiceType( OMElement serviceType ) {
        this.serviceType = serviceType;
    }

    /**
     * @return the serviceTypeVersion
     */
    public OMElement getServiceTypeVersion() {
        return serviceTypeVersion;
    }

    /**
     * @param serviceTypeVersion the serviceTypeVersion to set
     */
    public void setServiceTypeVersion( OMElement serviceTypeVersion ) {
        this.serviceTypeVersion = serviceTypeVersion;
    }

    /**
     * @return the title
     */
    public List<String> getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle( List<String> title ) {
        this.title = title;
    }

    /**
     * @return the boundingBox
     */
    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * @param boundingBox the boundingBox to set
     */
    public void setBoundingBox( BoundingBox boundingBox ) {
        this.boundingBox = boundingBox;
    }

    /**
     * @return the keywords
     */
    public List<Keyword> getKeywords() {
        return keywords;
    }

    /**
     * @param keywords the keywords to set
     */
    public void setKeywords( List<Keyword> keywords ) {
        this.keywords = keywords;
    }

   
    
    
    

}
