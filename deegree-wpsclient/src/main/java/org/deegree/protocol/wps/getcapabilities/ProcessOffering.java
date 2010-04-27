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
import org.deegree.commons.tom.ows.Version;

/**
 * 
 * TODO add class documentation here
 * 
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author last edited by: $Author: kiehle $
 * 
 * @version $Revision: $, $Date: $
 */
public class ProcessOffering {

	private String identifier;

	private String _abstract;

	private String title;

	private String processVersion;
	
	private String[] metadata;
	
	private String profile;
	
	private Version versionType;
	
	private String wsdl;
	

	public void setIdentifier(String identifier){
		this.identifier = identifier;
	}

	public String getIdentifier(){
		return this.identifier;
	}

	public void setAbstract(String _abstract){
		this._abstract = _abstract;
	}

	public String getAbstract(){
		return this._abstract;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return this.title;
	}

	public String getProcessVersion(){
		return processVersion;
	}

	public void setProcessVersion(String processVersion){
		this.processVersion = processVersion;
	}

	public String[] getMetadata(){
		return metadata;
	}

	public void setMetadata(String[] metadata){
		this.metadata = metadata;
	}

	public String getProfile(){
		return profile;
	}

	public void setProfile(String profile){
		this.profile = profile;
	}

	public Version getVersionType(){
		return versionType;
	}

	public void setVersionType(Version versionType){
		this.versionType = versionType;
	}

	public String getWsdl(){
		return wsdl;
	}

	public void setWsdl(String wsdl){
		this.wsdl = wsdl;
	}
	
	@Override
	public String toString(){
	    StringBuilder sb = new StringBuilder();
	    sb.append("Identifier: " + identifier);
	    sb.append("Abstract: " + _abstract);
	    sb.append("Title: " + title);
        sb.append("ProcessVersion: " + processVersion);
        sb.append("Metadata: "); 
        for (int i=0;i<metadata.length;i++){
            sb.append( metadata[i] );
        }
        sb.append("Profile: " + profile);
        sb.append("VersionType: " + versionType);
        sb.append("WSDL: " + wsdl);
	    return sb.toString();
	}
	
	

}
