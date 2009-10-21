//$HeadURL: svn+ssh://sthomas@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.protocol.csw;

import org.deegree.commons.types.ows.Version;

public class CSWConstants {
	
	/** Namespace for elements from the CSW 2.0.2 specification 
	 * <p>
	 * Namespace="http://www.opengis.net/cat/csw/2.0.2" 
	 * */
    public static final String CSW_202_NS = "http://www.opengis.net/cat/csw/2.0.2";
    
    /** Location of the schema */
    public static final String CSW_202_SCHEMA = "http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd";

    /** Common namespace prefix for elements from the CSW specification */
    public static final String CSW_PREFIX = "csw";	
	
	/** CSW protocol version 2.0.2 */
    public static final Version VERSION_202 = Version.parseVersion( "2.0.2" );

	
	public enum CSWRequestType{
		
		/** Retrieve the capabilities of the service. */
        GetCapabilities,
        /** Discover elements of the service */
		DescribeRecord, 
		/** Resource discovery combines the two operations - search and present */
		GetRecords, 
		/** Retrieve the default representation of the service */
		GetRecordById
	}
	
	 public enum Sections {

	        /***/
	        ServiceIdentification,
	        /***/
	        ServiceProvider,
	        /***/
	        OperationsMetadata,
	        /***/
	        Filter_Capabilities
	    }

}
