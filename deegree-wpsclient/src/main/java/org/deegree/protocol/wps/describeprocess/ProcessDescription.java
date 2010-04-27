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
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author last edited by: $Author: kiehle $
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

    public String getService() {
        return service;
    }

    public void setService( String service ) {
        this.service = service;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest( String request ) {
        this.request = request;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion( String version ) {
        this.version = version;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage( String language ) {
        this.language = language;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier( String identifier ) {
        this.identifier = identifier;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle( String title ) {
        this.title = title;
    }

    public String getAbstraCt() {
        return abstraCt;
    }

    public void setAbstraCt( String abstraCt ) {
        this.abstraCt = abstraCt;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata( String metadata ) {
        this.metadata = metadata;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile( String profile ) {
        this.profile = profile;
    }

    public String getProcessVersion() {
        return processVersion;
    }

    public void setProcessVersion( String processVersion ) {
        this.processVersion = processVersion;
    }

    public String getWSDL() {
        return WSDL;
    }

    public void setWSDL( String wSDL ) {
        WSDL = wSDL;
    }

    public List<DataInputDescribeProcess> getDataInputs() {

        return dataInputs;
    }

    public void setDataInputs( List<DataInputDescribeProcess> dataInputs ) {
        this.dataInputs = dataInputs;
    }

    public List<ProcessOutput> getProcessOutputs() {
        return processOutputs;
    }

    public void setProcessOutputs( List<ProcessOutput> processOutputs ) {
        this.processOutputs = processOutputs;
    }

    public boolean isStoreSupported() {
        return storeSupported;
    }

    public void setStoreSupported( boolean storeSupported ) {
        this.storeSupported = storeSupported;
    }

    public boolean isStatusSupported() {
        return statusSupported;
    }

    public void setStatusSupported( boolean statusSupported ) {
        this.statusSupported = statusSupported;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public void setSchemaLocation( String schemaLocation ) {
        this.schemaLocation = schemaLocation;
    }

}
