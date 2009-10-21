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
