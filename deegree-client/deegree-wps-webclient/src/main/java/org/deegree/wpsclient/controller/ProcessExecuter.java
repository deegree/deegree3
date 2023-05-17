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
package org.deegree.wpsclient.controller;

import static org.deegree.client.core.utils.MessageUtils.getFacesMessage;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.deegree.client.core.model.BBox;
import org.deegree.client.core.model.UploadedFile;
import org.deegree.commons.utils.StringPair;
import org.deegree.protocol.wps.client.input.type.BBoxInputType;
import org.deegree.protocol.wps.client.input.type.ComplexInputType;
import org.deegree.protocol.wps.client.input.type.InputType;
import org.deegree.protocol.wps.client.input.type.LiteralInputType;
import org.deegree.protocol.wps.client.output.ExecutionOutput;
import org.deegree.protocol.wps.client.param.ComplexFormat;
import org.deegree.protocol.wps.client.process.Process;
import org.deegree.protocol.wps.client.process.ProcessExecution;
import org.deegree.protocol.wps.client.process.execute.ExecutionOutputs;
import org.slf4j.Logger;

/**
 * <code>ProcessExecuter</code> is the executer of a process, with the given user entries.
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public class ProcessExecuter {

	private static final Logger LOG = getLogger(ProcessExecuter.class);

	/**
	 * Executes the given process. After this, the uploaded files (xmlInputs and
	 * binaryInputs) will be deleted.
	 * @param processToExecute the process to execute
	 * @param literalInputs all literalInputs; must not be null!
	 * @param bboxInputs all bboxInputs; must not be null!
	 * @param xmlInputs all xmlInputs; must not be null!
	 * @param xmlRefInputs
	 * @param binaryInputs all binaryInputs; must not be null!
	 * @param outputs the outputs
	 * @param complexOutputFormats
	 * @return the response of the WPS request
	 */
	public ExecutionOutput[] execute(Process processToExecute, Map<String, StringPair> literalInputs,
			Map<String, BBox> bboxInputs, Map<String, UploadedFile> xmlInputs, Map<String, String> xmlRefInputs,
			Map<String, UploadedFile> binaryInputs, Map<String, ComplexFormat> complexInputFormats,
			List<String> outputs, Map<String, ComplexFormat> complexOutputFormats) {

		FacesContext fc = FacesContext.getCurrentInstance();
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("execute selected process " + processToExecute.getId());
				LOG.debug("input parameters (LITERAL): " + literalInputs);
				LOG.debug("input parameters (XML): " + xmlInputs);
				LOG.debug("input parameters (XML-REFS): " + xmlRefInputs);
				LOG.debug("input parameters (BINARY): " + binaryInputs);
				LOG.debug("input parameters (BBOX): " + bboxInputs);
				LOG.debug("input formats: " + complexInputFormats);
				LOG.debug("outputs: " + outputs);
				LOG.debug("output formats: " + complexOutputFormats);
			}
			ProcessExecution execution = processToExecute.prepareExecution();
			InputType[] inputDescription = processToExecute.getInputTypes();
			for (int i = 0; i < inputDescription.length; i++) {
				InputType input = inputDescription[i];
				String id = input.getId().toString();
				if (input instanceof LiteralInputType) {
					for (String key : literalInputs.keySet()) {
						if (matches(id, key) && literalInputs.get(key) != null) {
							StringPair literal = literalInputs.get(key);
							execution.addLiteralInput(input.getId().getCode(), input.getId().getCodeSpace(),
									literal.getFirst(), null, literal.getSecond());
						}
					}

				}
				else if (input instanceof ComplexInputType) {
					String schema = null, encoding = null, mimeType = null;
					for (String key : complexInputFormats.keySet()) {
						if (matches(id, key) && complexInputFormats.get(key) != null) {
							ComplexFormat complexFormat = complexInputFormats.get(key);
							if (complexFormat != null) {
								schema = complexFormat.getSchema();
								mimeType = complexFormat.getMimeType();
								encoding = complexFormat.getEncoding();
							}
						}
					}
					UploadedFile xml = null;
					for (String key : xmlInputs.keySet()) {
						if (matches(id, key) && xmlInputs.get(key) != null)
							xml = xmlInputs.get(key);
					}
					String xmlRef = null;
					for (String key : xmlRefInputs.keySet()) {
						if (matches(id, key) && xmlRefInputs.get(key) != null)
							xmlRef = xmlRefInputs.get(key);
					}
					if (xml != null) {
						XMLStreamReader sr = XMLInputFactory.newInstance()
							.createXMLStreamReader(xml.getUrl().openStream());
						while (sr.getEventType() != XMLStreamReader.START_ELEMENT) {
							sr.next();
						}
						execution.addXMLInput(input.getId().getCode(), input.getId().getCodeSpace(), sr, mimeType,
								encoding, schema);
					}
					else if (xmlRef != null && xmlRef.trim().length() > 0) {
						execution.addXMLInput(input.getId().getCode(), input.getId().getCodeSpace(), new URI(xmlRef),
								true, mimeType, encoding, schema);
					}
					for (String key : binaryInputs.keySet()) {
						execution.addBinaryInput(input.getId().getCode(), input.getId().getCodeSpace(),
								binaryInputs.get(key).getUrl().openStream(), mimeType, encoding);
					}
				}
				else if (input instanceof BBoxInputType) {
					BBox bbox = bboxInputs.get(id);
					if (bbox != null) {
						execution.addBBoxInput(input.getId().getCode(), input.getId().getCodeSpace(),
								new double[] { bbox.getMinx(), bbox.getMinY() },
								new double[] { bbox.getMaxX(), bbox.getMaxY() }, bbox.getCrs());
					}
				}
			}
			for (String out : outputs) {
				String schema = null, encoding = null, mimeType = null;
				if (complexOutputFormats.containsKey(out)) {
					schema = complexOutputFormats.get(out).getSchema();
					encoding = complexOutputFormats.get(out).getEncoding();
					mimeType = complexOutputFormats.get(out).getMimeType();
				}
				LOG.debug("Append output " + out + " with format " + schema + ", " + encoding + ", " + mimeType);
				execution.addOutput(out, null, null, true, mimeType, encoding, schema);
			}
			ExecutionOutputs response = execution.execute();
			// // OR, not yet finished
			// execution.startAsync();

			delete(xmlInputs, binaryInputs);
			return response.getAll();
		}
		catch (Exception e) {
			if (LOG.isDebugEnabled()) {
				e.printStackTrace();
			}
			FacesMessage msg = getFacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR.REQUEST_WPS", e.getMessage());
			fc.addMessage("ExecuteBean.execute.ERROR_REQUEST", msg);
			return null;
		}
	}

	private boolean matches(String id, String key) {
		return key.matches("^" + id + ":[0-9]*");
	}

	private void delete(Map<String, UploadedFile> xmlInputs, Map<String, UploadedFile> binaryInputs) {
		for (String xml : xmlInputs.keySet()) {
			UploadedFile xmlToDelete = xmlInputs.get(xml);
			FileDeleter deleter = new FileDeleter(new File(xmlToDelete.getAbsolutePath()));
			deleter.start();
		}
		for (String xml : binaryInputs.keySet()) {
			UploadedFile binaryToDelete = binaryInputs.get(xml);
			FileDeleter deleter = new FileDeleter(new File(binaryToDelete.getAbsolutePath()));
			deleter.start();
		}
	}

	private class FileDeleter extends Thread {

		private File fileToDelete;

		public FileDeleter(File fileToDelete) {
			this.fileToDelete = fileToDelete;
		}

		@Override
		public void run() {
			if (fileToDelete != null && fileToDelete.exists()) {
				boolean delete = fileToDelete.delete();
				if (!delete) {
					LOG.debug("File " + fileToDelete + " could not be deletet!");
				}
			}
		}

	}

}
