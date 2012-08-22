//$HeadURL$
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

package org.deegree.ogcwebservices.wps.execute;

import java.util.ArrayList;
import java.util.List;

import org.deegree.datatypes.Code;

/**
 * ExecuteResponseType.java
 *
 * Created on 09.03.2006. 23:16:26h
 *
 *
 * WPS Execute operation response. By default, this XML document is delivered to
 * the client in response to an Execute request. If "status" is "false" in the
 * Execute operation request, this document is normally returned when process
 * execution has been completed. If "status" in the Execute request is "true",
 * this response shall be returned as soon as the Execute request has been
 * accepted for processing. In this case, the same XML document is also made
 * available as a web-accessible resource from the URL identified in the
 * statusLocation, and the WPS server shall repopulate it once the process has
 * completed. It may repopulate it on an ongoing basis while the process is
 * executing. However, the response to an Execute request will not include this
 * element in the special case where the output is a single complex value result
 * and the Execute request indicates that "store" is "false". Instead, the
 * server shall return the complex result (e.g., GIF image or GML) directly,
 * without encoding it in the ExecuteResponse. If processing fails in this
 * special case, the normal ExecuteResponse shall be sent, with the error
 * condition indicated. This option is provided to simplify the programming
 * required for simple clients and for service chaining.
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @version 1.0.
 * @since 2.0
 */
public class ExecuteResponse {

	/**
	 * Identifier of the Process requested to be executed. This Process
	 * identifier shall be as listed in the ProcessOfferings section of the WPS
	 * Capabilities document.
	 */
	protected Code identifier;

	/**
	 * Execution status of this process.
	 */
	protected Status status;

	/**
	 * Inputs that were provided as part of the execute request. This element
	 * can be omitted as an implementation decision by the WPS server. However,
	 * it is often advisable to have the response include this information, so
	 * the client can confirm that the request was received correctly, and to
	 * provide a source of metadata if the client wishes to store the result for
	 * future reference.
	 */
	protected ExecuteDataInputs dataInputs;

	/**
	 * Complete list of Output data types that were requested as part of the
	 * Execute request. This element can be omitted as an implementation
	 * decision by the WPS server. However, it is often advisable to have the
	 * response include this information, so the client can confirm that the
	 * request was received correctly, and to provide a source of metadata if
	 * the client wishes to store the result for future reference.
	 */
	protected OutputDefinitions outputDefinitions;

	/**
	 * List of values of the Process output parameters. Normally there would be
	 * at least one output when the process has completed successfully. If the
	 * process has not finished executing, the implementer can choose to include
	 * whatever final results are ready at the time the Execute response is
	 * provided. If the reference locations of outputs are known in advance,
	 * these URLs may be provided before they are populated.
	 */
	protected ProcessOutputs processOutputs;

	/**
	 * The URL referencing the location from which the ExecuteResponse can be
	 * retrieved. If "status" is "true" in the Execute request, the
	 * ExecuteResponse should also be found here as soon as the process returns
	 * the initial response to the client. It should persist at this location as
	 * long as the outputs are accessible from the server. The outputs may be
	 * stored for as long as the implementer of the server decides. If the
	 * process takes a long time, this URL can be repopulated on an ongoing
	 * basis in order to keep the client updated on progress. Before the process
	 * has succeeded, the ExecuteResponse contains information about the status
	 * of the process, including whether or not processing has started, and the
	 * percentage completed. It may also optionally contain the inputs and any
	 * ProcessStartedType interim results. When the process has succeeded, the
	 * ExecuteResponse found at this URL shall contain the output values or
	 * references to them.
	 */
	protected String statusLocation;

	/**
	 * Version of the WPS interface specification implemented by the server.
	 */
	protected String version;

	/**
	 * Convenience variable to simplify execute response handling.
	 */
	boolean directResponse = false;

	/**
	 *
	 * @param dataInputs
	 * @param identifier
	 * @param outputDefinitions
	 * @param processOutputs
	 * @param status
	 * @param statusLocation
	 * @param version
	 * @param directResponse
	 */
	public ExecuteResponse( ExecuteDataInputs dataInputs, Code identifier,
			OutputDefinitions outputDefinitions, ProcessOutputs processOutputs, Status status,
			String statusLocation, String version, boolean directResponse ) {
		this.dataInputs = dataInputs;
		this.identifier = identifier;
		this.outputDefinitions = outputDefinitions;
		this.processOutputs = processOutputs;
		this.status = status;
		this.statusLocation = statusLocation;
		this.version = version;
		this.directResponse = directResponse;
	}

	/**
	 * Just an empty constructor.
	 */
	public ExecuteResponse() {
	    //empty constructor.
	}

	/**
	 * @return Returns the identifier.
	 */
	public Code getIdentifier() {
		return identifier;
	}

	/**
	 * @return Returns the status.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @return Returns the dataInputs.
	 */
	public ExecuteDataInputs getDataInputs() {
		return dataInputs;
	}

	/**
	 * @return Returns the outputDefinitions.
	 */
	public OutputDefinitions getOutputDefinitions() {
		return outputDefinitions;
	}

	/**
	 * @return Returns the processOutputs.
	 */
	public ProcessOutputs getProcessOutputs() {
		return processOutputs;
	}

	/**
	 * @return Returns the statusLocation.
	 */
	public String getStatusLocation() {
		return statusLocation;
	}

	/**
	 * @return Returns the version.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @return Returns the directResponse.
	 */
	public boolean isDirectResponse() {
		return directResponse;
	}

	/**
	 *
	 * @param dataInputs
	 */
	public void setDataInputs( ExecuteDataInputs dataInputs ) {
		this.dataInputs = dataInputs;
	}

	/**
	 *
	 * @param directResponse
	 */
	public void setDirectResponse( boolean directResponse ) {
		this.directResponse = directResponse;
	}

	/**
	 * @param identifier to set
	 */
	public void setIdentifier( Code identifier ) {
		this.identifier = identifier;
	}

	/**
	 * @param outputDefinitions to set
	 */
	public void setOutputDefinitions( OutputDefinitions outputDefinitions ) {
		this.outputDefinitions = outputDefinitions;
	}

	/**
	 * @param processOutputs  to set
	 */
	public void setProcessOutputs( ProcessOutputs processOutputs ) {
		this.processOutputs = processOutputs;
	}

	/**
	 * @param status to set
	 */
	public void setStatus( Status status ) {
		this.status = status;
	}

	/**
	 * @param statusLocation to set
	 */
	public void setStatusLocation( String statusLocation ) {
		this.statusLocation = statusLocation;
	}

	/**
	 * @param version to set
	 */
	public void setVersion( String version ) {
		this.version = version;
	}

	/**
	 * <code>ProcessOutputs</code> process the outputs
	 *
	 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
	 *
	 * @author last edited by: $Author$
	 *
	 * @version $Revision$, $Date$
	 *
	 */
	public static class ProcessOutputs {

		private List<IOValue> outputs;

		/**
		 * @return Returns the output.
		 */
		public List<IOValue> getOutputs() {
			if ( outputs == null ) {
				outputs = new ArrayList<IOValue>();
			}
			return this.outputs;
		}

		/**
		 *
		 * @param outputs
		 */
		public void setOutputs( List<IOValue> outputs ) {
			this.outputs = outputs;
		}

	}

}
