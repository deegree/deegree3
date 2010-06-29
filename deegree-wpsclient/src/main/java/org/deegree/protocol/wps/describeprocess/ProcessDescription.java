//$HeadURL: https://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.protocol.wps.describeprocess;

import java.util.List;

/**
 * 
 * This class holds the elements of the DescribeProcess Document
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author last edited by: $Author: walenciak $
 * 
 * @version $Revision: $, $Date: $
 */
public class ProcessDescription {

    private String service;

    private String request;

    private String version;

    private String language;

    private String schemaLocation;

    private String identifier;

    private String title;

    private String abstraCt;

    private String metadata;

    private String profile;

    private String processVersion;

    private String WSDL;

    private List<DataInputDescribeProcess> dataInputs;

    private List<ProcessOutput> processOutputs;

    private boolean storeSupported;

    private boolean statusSupported;

    /**
     * 
     * @return service
     */
    public String getService() {
        return service;
    }

    /**
     * 
     * @param service
     */
    public void setService( String service ) {
        this.service = service;
    }

    /**
     * 
     * @return request
     */
    public String getRequest() {
        return request;
    }

    /**
     * 
     * @param request
     */
    public void setRequest( String request ) {
        this.request = request;
    }

    /**
     * 
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * 
     * @param version
     */
    public void setVersion( String version ) {
        this.version = version;
    }

    /**
     * 
     * @return language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * 
     * @param language
     */
    public void setLanguage( String language ) {
        this.language = language;
    }

    /**
     * 
     * @return identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * 
     * @param identifier
     */
    public void setIdentifier( String identifier ) {
        this.identifier = identifier;
    }

    /**
     * 
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * 
     * @param title
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     * 
     * @return abstraCt
     */
    public String getAbstraCt() {
        return abstraCt;
    }

    /**
     * 
     * @param abstraCt
     */
    public void setAbstraCt( String abstraCt ) {
        this.abstraCt = abstraCt;
    }

    /**
     * 
     * @return metadata
     */
    public String getMetadata() {
        return metadata;
    }

    /**
     * 
     * @param metadata
     */
    public void setMetadata( String metadata ) {
        this.metadata = metadata;
    }

    /**
     * 
     * @return profile
     */
    public String getProfile() {
        return profile;
    }

    /**
     * 
     * @param profile
     */
    public void setProfile( String profile ) {
        this.profile = profile;
    }

    /**
     * 
     * @return processVersion
     */
    public String getProcessVersion() {
        return processVersion;
    }

    /**
     * 
     * @param processVersion
     */
    public void setProcessVersion( String processVersion ) {
        this.processVersion = processVersion;
    }

    /**
     * 
     * @return WSDL
     */
    public String getWSDL() {
        return WSDL;
    }

    /**
     * 
     * @param WSDL
     */
    public void setWSDL( String wSDL ) {
        WSDL = wSDL;
    }

    /**
     * 
     * @return List<DataInputDescribeProcess>
     */
    public List<DataInputDescribeProcess> getDataInputs() {

        return dataInputs;
    }

    /**
     * 
     * @param List
     *            <DataInputDescribeProcess>
     */
    public void setDataInputs( List<DataInputDescribeProcess> dataInputs ) {
        this.dataInputs = dataInputs;
    }

    /**
     * 
     * @return List<ProcessOutput>
     */
    public List<ProcessOutput> getProcessOutputs() {
        return processOutputs;
    }

    /**
     * 
     * @param List
     *            <ProcessOutput>
     */
    public void setProcessOutputs( List<ProcessOutput> processOutputs ) {
        this.processOutputs = processOutputs;
    }

    /**
     * 
     * @return is store supported?
     */
    public boolean isStoreSupported() {
        return storeSupported;
    }

    /**
     * 
     * @param is
     *            store supported?
     */
    public void setStoreSupported( boolean storeSupported ) {
        this.storeSupported = storeSupported;
    }

    /**
     * 
     * @return is status supported?
     */
    public boolean isStatusSupported() {
        return statusSupported;
    }

    /**
     * 
     * @param is
     *            status supported?
     */
    public void setStatusSupported( boolean statusSupported ) {
        this.statusSupported = statusSupported;
    }

    /**
     * 
     * @return schema location
     */
    public String getSchemaLocation() {
        return schemaLocation;
    }

    /**
     * 
     * @param schema
     *            location
     */
    public void setSchemaLocation( String schemaLocation ) {
        this.schemaLocation = schemaLocation;
    }

}
