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

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;

import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.protocol.wps.client.WPSClient;
import org.deegree.protocol.wps.client.input.type.InputType;
import org.deegree.protocol.wps.client.output.type.OutputType;
import org.deegree.protocol.wps.client.process.Process;

/**
 * <code>ClientBean</code> handles all selections/entries made in the GUI which leads to
 * changes in the GUI.
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@ManagedBean
@ViewScoped
public class ClientBean implements Serializable {

	private static final long serialVersionUID = -1434783003208250369L;

	private String url = "http://flexigeoweb.lat-lon.de/deegree-wps-demo/services";

	private List<String> urls = new ArrayList<String>();

	private WPSClient wpsClient;

	private List<Process> processes = new ArrayList<Process>();

	private CodeType process;

	private Process selectedProcess;

	private String information;

	final static String WPS_INFOKEY = "WPS";

	final static String PROCESS_INFOKEY = "PROCESS";

	final static String IN_INFOKEY = "INPUT";

	final static String OUT_INFOKEY = "OUTPUT";

	@PostConstruct
	public void init() {
		urls.add("http://deegree3-testing.deegree.org/wps-workspace/services");
		urls.add("http://localhost:8080/deegree-wps-demo/services");
		urls.add("http://flexigeoweb.lat-lon.de/deegree-wps-demo/services");
	}

	/**
	 * change the URL of the WPS and update the list of processes
	 * @param event
	 * @throws AbortProcessingException
	 */
	public void selectWPS(AjaxBehaviorEvent event) throws AbortProcessingException {
		FacesContext fc = FacesContext.getCurrentInstance();
		processes.clear();
		try {
			URL capUrl = new URL(url + "?service=WPS&version=1.0.0&request=GetCapabilities");
			wpsClient = new WPSClient(capUrl);
			FacesMessage msg = getFacesMessage(FacesMessage.SEVERITY_INFO, "INFO.SELECT_WPS", url);
			fc.addMessage("ClientBean.selectWPS.SELECT_WPS", msg);
			Process[] p = wpsClient.getProcesses();
			processes.addAll((List<Process>) Arrays.asList(p));
			if (!urls.contains(url))
				urls.add(url);
		}
		catch (MalformedURLException e) {
			FacesMessage msg = getFacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR.INVALID_URL", url);
			fc.addMessage("ClientBean.selectWPS.INVALID_URL", msg);
		}
		catch (Exception e) {
			FacesMessage msg = getFacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR.INVALID_WPS", url, e.getMessage());
			fc.addMessage("ClientBean.selectWPS.INVALID_WPS", msg);
		}
	}

	/**
	 * updates the gui, which depends on the selected process
	 * @param event
	 * @throws AbortProcessingException
	 */
	public void selectProcess(AjaxBehaviorEvent event) throws AbortProcessingException {
		FacesContext fc = FacesContext.getCurrentInstance();
		if (wpsClient == null) {
			FacesMessage msg = getFacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR.NO_URL");
			fc.addMessage("ClientBean.selectProcess.NO_WPS", msg);
			return;
		}
		if (process == null) {
			FacesMessage msg = getFacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR.NO_PROCESS");
			fc.addMessage("ClientBean.selectProcess.NO_Process", msg);
			return;
		}
		selectedProcess = wpsClient.getProcess(process.getCode(), process.getCodeSpace());
		if (selectedProcess == null) {
			FacesMessage msg = getFacesMessage(FacesMessage.SEVERITY_WARN, "WARN.NO_PROCESS_WITH_ID", url, process);
			fc.addMessage("ClientBean.selectProcess.NO_PROCESS_FOR_ID", msg);
			return;
		}
		information = selectedProcess.getAbstract() != null ? selectedProcess.getAbstract().getString() : "";
		FacesMessage msg = getFacesMessage(FacesMessage.SEVERITY_INFO, "INFO.SELECT_PROCESS", selectedProcess.getId());
		fc.addMessage("ClientBean.selectProcess.SELECT_PROCESS", msg);
	}

	/**
	 * action methode to update the information text dependent on the given parameter
	 * @return null, to stay on the same page
	 */
	public Object updateInfoText() {
		FacesContext fc = FacesContext.getCurrentInstance();
		Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
		if (params.containsKey("type")) {
			String type = params.get("type");
			if (WPS_INFOKEY.equals(type) && wpsClient != null) {
				ServiceIdentification si = wpsClient.getMetadata().getServiceIdentification();
				information = getAsLocaleString(si.getAbstracts());
			}
			else if (PROCESS_INFOKEY.equals(type) && selectedProcess != null) {
				information = selectedProcess.getAbstract() != null ? selectedProcess.getAbstract().getString() : "";
			}
			else if (IN_INFOKEY.equals(type) && params.containsKey("dataId") && selectedProcess != null) {
				try {
					String id = params.get("dataId");
					InputType[] inputTypes = selectedProcess.getInputTypes();
					for (int i = 0; i < inputTypes.length; i++) {
						if (equalsCodeType(inputTypes[i].getId(), id)) {
							information = inputTypes[i].getAbstract() != null ? inputTypes[i].getAbstract().getString()
									: "";
						}
					}
				}
				catch (Exception e) {
					information = null;
					FacesMessage msg = getFacesMessage(FacesMessage.SEVERITY_INFO, "INFO.UPDATE_INFO_FAILED");
					fc.addMessage("ClientBean.updateInfoText.FAILED", msg);
				}
			}
			else if (OUT_INFOKEY.equals(type) && params.containsKey("dataId") && selectedProcess != null) {
				try {
					String id = params.get("dataId");
					OutputType[] outputTypes = selectedProcess.getOutputTypes();
					for (int i = 0; i < outputTypes.length; i++) {
						if (equalsCodeType(outputTypes[i].getId(), id)) {
							information = outputTypes[i].getAbstract() != null
									? outputTypes[i].getAbstract().getString() : "";
						}
					}
				}
				catch (Exception e) {
					information = null;
					FacesMessage msg = getFacesMessage(FacesMessage.SEVERITY_INFO, "INFO.UPDATE_INFO_FAILED");
					fc.addMessage("ClientBean.updateInfoText.FAILED", msg);
				}
			}
		}
		return null;
	}

	private boolean equalsCodeType(CodeType codeType, String string) {
		if (codeType != null && codeType.getCode().equals(string)) {
			return true;
		}
		return false;
	}

	private String getAsLocaleString(List<LanguageString> languageStrings) {
		Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		for (LanguageString ls : languageStrings) {
			if (locale.getLanguage().equals(ls.getLanguage())) {
				return ls.getString();
			}
		}
		if (!languageStrings.isEmpty()) {
			return languageStrings.get(0).getString();
		}
		return "";
	}

	/******************* GETTER / SETTER ******************/

	/**
	 * @param url the URL of the WPS
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the URL of the WPS
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param process the process to execute
	 */
	public void setProcess(CodeType process) {
		this.process = process;

	}

	/**
	 * @return the process to execute
	 */
	public CodeType getProcess() {
		return process;
	}

	/**
	 * @return a list of available processes
	 */
	public List<Process> getProcesses() {
		return processes;
	}

	/**
	 * @param information information text to set
	 */
	public void setInformation(String information) {
		this.information = information;
	}

	/**
	 * @return the currently set information text
	 */
	public String getInformation() {
		return information;
	}

	/**
	 * @return true if a valid wps is selected
	 */
	public boolean isWpsEntered() {
		return wpsClient != null;
	}

	/**
	 * @return true if a process is selected
	 */
	public boolean isProcessSelected() {
		return selectedProcess != null;
	}

	/**
	 * @return the currently selected process
	 */
	public Process getSelectedProcess() {
		return selectedProcess;
	}

	public List<String> getUrls() {
		return urls;
	}

}
