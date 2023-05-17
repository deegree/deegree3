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
package org.deegree.wpsclient.gui;

import static org.deegree.client.core.utils.MessageUtils.getFacesMessage;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.deegree.client.core.model.BBox;
import org.deegree.client.core.model.UploadedFile;
import org.deegree.commons.utils.StringPair;
import org.deegree.protocol.wps.client.output.BBoxOutput;
import org.deegree.protocol.wps.client.output.ComplexOutput;
import org.deegree.protocol.wps.client.output.ExecutionOutput;
import org.deegree.protocol.wps.client.output.LiteralOutput;
import org.deegree.protocol.wps.client.param.ComplexFormat;
import org.deegree.protocol.wps.client.process.Process;
import org.deegree.wpsclient.controller.ProcessExecuter;
import org.slf4j.Logger;

/**
 * <code>ExecuteBean</code> receives the parameters to execute the WPS and holds the
 * response.
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@ManagedBean
@RequestScoped
public class ExecuteBean implements Serializable {

	private static final long serialVersionUID = 5702665270758227972L;

	private static final Logger LOG = getLogger(ExecuteBean.class);

	public static final String PROCESS_ATTRIBUTE_KEY = "process";

	private Map<String, StringPair> literalInputs = new HashMap<String, StringPair>();

	private Map<String, UploadedFile> xmlInputs = new HashMap<String, UploadedFile>();

	private Map<String, String> xmlRefInputs = new HashMap<String, String>();

	private Map<String, UploadedFile> binaryInputs = new HashMap<String, UploadedFile>();

	private Map<String, BBox> bboxInputs = new HashMap<String, BBox>();

	private List<LiteralOutput> literalOutputs = new ArrayList<LiteralOutput>();

	private List<BBoxOutput> bboxOutputs = new ArrayList<BBoxOutput>();

	private List<ComplexOutput> xmlOutputs = new ArrayList<ComplexOutput>();

	private List<ComplexOutput> binaryOutputs = new ArrayList<ComplexOutput>();

	private List<String> outputs = new ArrayList<String>();

	private Map<String, ComplexFormat> complexInputFormats = new HashMap<String, ComplexFormat>();

	private Map<String, ComplexFormat> complexOutputFormats = new HashMap<String, ComplexFormat>();

	/**
	 * ajax listener to execute the process (the source component of this event must
	 * contain an attribute with key {@link PROCESS_ATTRIBUTE_KEY} from type
	 * {@link Process} containing the selected process.
	 * @param event
	 */

	public Object executeProcess() {
		FacesContext fc = FacesContext.getCurrentInstance();
		ClientBean cb = (ClientBean) fc.getELContext().getELResolver().getValue(fc.getELContext(), null, "clientBean");
		Process selectedProcess = cb.getSelectedProcess();

		if (selectedProcess != null) {
			if (outputs.size() == 0) {
				try {
					outputs.add(selectedProcess.getOutputTypes()[0].getId().toString());
				}
				catch (Exception e) {
					if (LOG.isDebugEnabled()) {
						e.printStackTrace();
					}
					FacesMessage msg = getFacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR.REQUEST_WPS",
							e.getMessage());
					fc.addMessage("ExecuteBean.execute.ERROR_REQUEST", msg);
				}
			}

			ProcessExecuter executer = new ProcessExecuter();

			ExecutionOutput[] executionOutput = executer.execute(selectedProcess, literalInputs, bboxInputs, xmlInputs,
					xmlRefInputs, binaryInputs, complexInputFormats, outputs, complexOutputFormats);

			if (executionOutput != null) {
				for (int i = 0; i < executionOutput.length; i++) {
					ExecutionOutput output = executionOutput[i];
					if (output instanceof LiteralOutput) {
						literalOutputs.add((LiteralOutput) output);
					}
					else if (output instanceof BBoxOutput) {
						bboxOutputs.add((BBoxOutput) output);
					}
					else if (output instanceof ComplexOutput) {
						ComplexFormat format = ((ComplexOutput) output).getFormat();
						if (format.getMimeType() != null && format.getMimeType().contains("xml")) {
							xmlOutputs.add((ComplexOutput) output);
						}
						else {
							binaryOutputs.add((ComplexOutput) output);
						}
					}
				}
				FacesMessage msg = getFacesMessage(FacesMessage.SEVERITY_INFO, "INFO.REQUEST_SUCCESS");
				fc.addMessage("ExecuteBean.execute.REQUEST", msg);
			}
		}
		return null;
	}

	/******************* GETTER / SETTER ******************/
	public List<String> getOutputs() {
		return outputs;
	}

	public void setOutputs(List<String> outputs) {
		this.outputs = outputs;
	}

	// INPUTS
	public Map<String, StringPair> getLiteralInputs() {
		return literalInputs;
	}

	public Map<String, UploadedFile> getXmlInputs() {
		return xmlInputs;
	}

	public Map<String, String> getXmlRefInputs() {
		return xmlRefInputs;
	}

	public Map<String, UploadedFile> getBinaryInputs() {
		return binaryInputs;
	}

	public Map<String, BBox> getBboxInputs() {
		return bboxInputs;
	}

	// OUTPUTS
	public List<LiteralOutput> getLiteralOutputs() {
		return literalOutputs;
	}

	public List<BBoxOutput> getBboxOutputs() {
		return bboxOutputs;
	}

	public List<ComplexOutput> getXmlOutputs() {
		return xmlOutputs;
	}

	public List<ComplexOutput> getBinaryOutputs() {
		return binaryOutputs;
	}

	public void setComplexInputFormats(Map<String, ComplexFormat> complexInputFormats) {
		this.complexInputFormats = complexInputFormats;
	}

	public Map<String, ComplexFormat> getComplexInputFormats() {
		return complexInputFormats;
	}

	public void setComplexOutputFormats(Map<String, ComplexFormat> complexOutputFormats) {
		this.complexOutputFormats = complexOutputFormats;
	}

	public Map<String, ComplexFormat> getComplexOutputFormats() {
		return complexOutputFormats;
	}

}
