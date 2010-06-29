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
package org.deegree.protocol.wps.getcapabilities;

import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.tom.ows.Version;

/**
 * 
 * Represents the ProcessBrief section of the GetCapabilties Document of the WPS specification 1.0
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author last edited by: $Author: walenciak $
 * 
 * @version $Revision: $, $Date: $
 */
public class ProcessBrief {

    private String identifier;

    private String _abstract;

    private String title;

    private String processVersion;

    private String[] metadata;

    private String[] profiles;

    private String versionType;

    private String wsdl;

    /**
     * 
     * @param identifier
     */
    public void setIdentifier( String identifier ) {
        this.identifier = identifier;
    }

    /**
     * 
     * @return identifier
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * 
     * @param _abstract
     */
    public void setAbstract( String _abstract ) {
        this._abstract = _abstract;
    }

    /**
     * 
     * @return _abstract
     */
    public String getAbstract() {
        return this._abstract;
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
     * @return title
     */
    public String getTitle() {
        return this.title;
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
     * @return metadata
     */
    public String[] getMetadata() {
        return metadata;
    }

    /**
     * 
     * @param metadata
     */
    public void setMetadata( String[] metadata ) {
        this.metadata = metadata;
    }

    /**
     * 
     * @return profiles
     */
    public String[] getProfiles() {
        return profiles;
    }

    /**
     * 
     * @param profiles
     */
    public void setProfiles( String[] profiles ) {
        this.profiles = profiles;
    }

    /**
     * 
     * @return versionType
     */
    public String getVersionType() {
        return versionType;
    }

    /**
     * 
     * @param versionType
     */
    public void setVersionType( String versionType ) {
        this.versionType = versionType;
    }

    /**
     * 
     * @return wsdl
     */
    public String getWsdl() {
        return wsdl;
    }

    /**
     * 
     * @param wsdl
     */
    public void setWsdl( String wsdl ) {
        this.wsdl = wsdl;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "Identifier: " + identifier );
        sb.append( "Abstract: " + _abstract );
        sb.append( "Title: " + title );
        sb.append( "ProcessVersion: " + processVersion );
        sb.append( "Metadata: " );
        for ( int i = 0; i < metadata.length; i++ ) {
            sb.append( metadata[i] );
        }
        sb.append( "Profile: " + profiles );
        sb.append( "VersionType: " + versionType );
        sb.append( "WSDL: " + wsdl );
        return sb.toString();
    }

}
