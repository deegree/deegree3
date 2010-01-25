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
import org.deegree.crs.CRS;

/**
 * Properties that are queryable by applicationprofiles such as ISO
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class QueryableProperties {

    // ---<common queryable properties>
    // dc:subject
    private List<Keyword> keywords;

    private List<String> title;

    private List<String> _abstract;

    private String anyText;

    private List<Format> format;

    private String identifier;

    private Date modified;

    private String type;

    private BoundingBox boundingBox;

    private CRS crs;

    // ---</common queryable properties>

    // ---<additional common queryable properties>

    private Date revisionDate;

    private List<String> alternateTitle;

    private Date creationDate;

    private Date publicationDate;

    private String organisationName;

    private boolean hasSecurityConstraints;

    private String language;

    private String resourceIdentifier;

    private String parentIdentifier;

    private String resourceLanguage;

    private List<String> topicCategory;

    private String operatesOn;

    private String operatesOnIdentifier;

    private String operatesOnName;

    private int denominator;

    private float distanceValue;

    private String distanceUOM;
    
    private Date temporalExtentBegin;
    
    private Date temporalExtentEnd;

    // ---</additional common queryable properties>

    // ---<additional common queryable properties for SERVICE>

    private String serviceType;

    private String serviceTypeVersion;

    private String geographicDescriptionCode_service;

    private String operation;

    private String couplingType;

    // ---</additional common queryable properties for SERVICE>

    public QueryableProperties() {

    }

    /*
     * public QueryableProperties(List<String> subject, List<OMElement> title, OMElement language, List<String>
     * abridgement, String anyText, List<String> format, OMElement identifier, List<Date> modified, String type,
     * BoundingBox boundingBox, CRS crs){ this.subject = subject; this.title = title; this.language = language;
     * this.abridgement = abridgement; this.anyText = anyText; this.format = format; this.identifier = identifier;
     * this.modified = modified; this.type = type; this.boundingBox = boundingBox; this.crs = crs;
     * 
     * }
     */

    /**
     * @return the abridgement
     */
    public List<String> get_abstract() {
        return _abstract;
    }

    /**
     * @param abridgement
     *            the abridgement to set
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
     * @param anyText
     *            the anyText to set
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
     * @param format
     *            the format to set
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
     * @param identifier
     *            the identifier to set
     */
    public void setIdentifier( String identifier ) {
        this.identifier = identifier;
    }

    /**
     * @return the modified
     */
    public Date getModified() {
        return modified;
    }

    /**
     * @param modified
     *            the modified to set
     */
    public void setModified( Date modified ) {
        this.modified = modified;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
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
     * @param crs
     *            the crs to set
     */
    public void setCrs( CRS crs ) {
        this.crs = crs;
    }

    /**
     * @return the title
     */
    public List<String> getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
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
     * @param boundingBox
     *            the boundingBox to set
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
     * @param keywords
     *            the keywords to set
     */
    public void setKeywords( List<Keyword> keywords ) {
        this.keywords = keywords;
    }

    /**
     * @return the alternateTitle
     */
    public List<String> getAlternateTitle() {
        return alternateTitle;
    }

    /**
     * @param alternateTitle
     *            the alternateTitle to set
     */
    public void setAlternateTitle( List<String> alternateTitle ) {
        this.alternateTitle = alternateTitle;
    }

    /**
     * @return the resourceLanguage
     */
    public String getResourceLanguage() {
        return resourceLanguage;
    }

    /**
     * @param resourceLanguage
     *            the resourceLanguage to set
     */
    public void setResourceLanguage( String resourceLanguage ) {
        this.resourceLanguage = resourceLanguage;
    }

    /**
     * @return the revisionDate
     */
    public Date getRevisionDate() {
        return revisionDate;
    }

    /**
     * @param revisionDate
     *            the revisionDate to set
     */
    public void setRevisionDate( Date revisionDate ) {
        this.revisionDate = revisionDate;
    }

    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate
     *            the creationDate to set
     */
    public void setCreationDate( Date creationDate ) {
        this.creationDate = creationDate;
    }

    /**
     * @return the publicationDate
     */
    public Date getPublicationDate() {
        return publicationDate;
    }

    /**
     * @param publicationDate
     *            the publicationDate to set
     */
    public void setPublicationDate( Date publicationDate ) {
        this.publicationDate = publicationDate;
    }

    /**
     * @return the organisationName
     */
    public String getOrganisationName() {
        return organisationName;
    }

    /**
     * @param organisationName
     *            the organisationName to set
     */
    public void setOrganisationName( String organisationName ) {
        this.organisationName = organisationName;
    }

    /**
     * @return the hasSecurityConstraints
     */
    public boolean isHasSecurityConstraints() {
        return hasSecurityConstraints;
    }

    /**
     * @param hasSecurityConstraints
     *            the hasSecurityConstraints to set
     */
    public void setHasSecurityConstraints( boolean hasSecurityConstraints ) {
        this.hasSecurityConstraints = hasSecurityConstraints;
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language
     *            the language to set
     */
    public void setLanguage( String language ) {
        this.language = language;
    }

    /**
     * @return the resourceIdentifier
     */
    public String getResourceIdentifier() {
        return resourceIdentifier;
    }

    /**
     * @param resourceIdentifier
     *            the resourceIdentifier to set
     */
    public void setResourceIdentifier( String resourceIdentifier ) {
        this.resourceIdentifier = resourceIdentifier;
    }

    /**
     * @return the parentIdentifier
     */
    public String getParentIdentifier() {
        return parentIdentifier;
    }

    /**
     * @param parentIdentifier
     *            the parentIdentifier to set
     */
    public void setParentIdentifier( String parentIdentifier ) {
        this.parentIdentifier = parentIdentifier;
    }

    /**
     * @return the topicCategory
     */
    public List<String> getTopicCategory() {
        return topicCategory;
    }

    /**
     * @param topicCategory
     *            the topicCategory to set
     */
    public void setTopicCategory( List<String> topicCategory ) {
        this.topicCategory = topicCategory;
    }

    /**
     * @return the serviceType
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * @param serviceType
     *            the serviceType to set
     */
    public void setServiceType( String serviceType ) {
        this.serviceType = serviceType;
    }

    /**
     * @return the serviceTypeVersion
     */
    public String getServiceTypeVersion() {
        return serviceTypeVersion;
    }

    /**
     * @param serviceTypeVersion
     *            the serviceTypeVersion to set
     */
    public void setServiceTypeVersion( String serviceTypeVersion ) {
        this.serviceTypeVersion = serviceTypeVersion;
    }

    /**
     * @return the geographicDescriptionCode_service
     */
    public String getGeographicDescriptionCode_service() {
        return geographicDescriptionCode_service;
    }

    /**
     * @param geographicDescriptionCodeService
     *            the geographicDescriptionCode_service to set
     */
    public void setGeographicDescriptionCode_service( String geographicDescriptionCodeService ) {
        geographicDescriptionCode_service = geographicDescriptionCodeService;
    }

    /**
     * @return the operation
     */
    public String getOperation() {
        return operation;
    }

    /**
     * @param operation
     *            the operation to set
     */
    public void setOperation( String operation ) {
        this.operation = operation;
    }

    /**
     * @return the couplingType
     */
    public String getCouplingType() {
        return couplingType;
    }

    /**
     * @param couplingType
     *            the couplingType to set
     */
    public void setCouplingType( String couplingType ) {
        this.couplingType = couplingType;
    }

    /**
     * @return the operatesOn
     */
    public String getOperatesOn() {
        return operatesOn;
    }

    /**
     * @param operatesOn
     *            the operatesOn to set
     */
    public void setOperatesOn( String operatesOn ) {
        this.operatesOn = operatesOn;
    }

    /**
     * @return the operatesOnIdentifier
     */
    public String getOperatesOnIdentifier() {
        return operatesOnIdentifier;
    }

    /**
     * @param operatesOnIdentifier
     *            the operatesOnIdentifier to set
     */
    public void setOperatesOnIdentifier( String operatesOnIdentifier ) {
        this.operatesOnIdentifier = operatesOnIdentifier;
    }

    /**
     * @return the operatesOnName
     */
    public String getOperatesOnName() {
        return operatesOnName;
    }

    /**
     * @param operatesOnName
     *            the operatesOnName to set
     */
    public void setOperatesOnName( String operatesOnName ) {
        this.operatesOnName = operatesOnName;
    }

    /**
     * @return the denominator
     */
    public int getDenominator() {
        return denominator;
    }

    /**
     * @param denominator
     *            the denominator to set
     */
    public void setDenominator( int denominator ) {
        this.denominator = denominator;
    }

    /**
     * @return the distanceValue
     */
    public float getDistanceValue() {
        return distanceValue;
    }

    /**
     * @param distanceValue
     *            the distanceValue to set
     */
    public void setDistanceValue( float distanceValue ) {
        this.distanceValue = distanceValue;
    }

    /**
     * @return the distanceUOM
     */
    public String getDistanceUOM() {
        return distanceUOM;
    }

    /**
     * @param distanceUOM
     *            the distanceUOM to set
     */
    public void setDistanceUOM( String distanceUOM ) {
        this.distanceUOM = distanceUOM;
    }

    /**
     * @return the temporalExtentBegin
     */
    public Date getTemporalExtentBegin() {
        return temporalExtentBegin;
    }

    /**
     * @param temporalExtentBegin the temporalExtentBegin to set
     */
    public void setTemporalExtentBegin( Date temporalExtentBegin ) {
        this.temporalExtentBegin = temporalExtentBegin;
    }

    /**
     * @return the temporalExtentEnd
     */
    public Date getTemporalExtentEnd() {
        return temporalExtentEnd;
    }

    /**
     * @param temporalExtentEnd the temporalExtentEnd to set
     */
    public void setTemporalExtentEnd( Date temporalExtentEnd ) {
        this.temporalExtentEnd = temporalExtentEnd;
    }
    
    

}
