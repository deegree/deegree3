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
package org.deegree.client.wpsprinter;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.context.FacesContext;
import javax.faces.convert.DateTimeConverter;
import javax.faces.convert.DoubleConverter;
import javax.faces.convert.FloatConverter;
import javax.faces.convert.IntegerConverter;
import javax.faces.convert.LongConverter;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.DoubleRangeValidator;
import javax.faces.validator.LongRangeValidator;

import org.deegree.client.core.utils.MessageUtils;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.ArrayUtils;
import org.deegree.protocol.wps.client.WPSClient;
import org.deegree.protocol.wps.client.input.type.ComplexInputType;
import org.deegree.protocol.wps.client.input.type.InputType;
import org.deegree.protocol.wps.client.input.type.InputType.Type;
import org.deegree.protocol.wps.client.input.type.LiteralInputType;
import org.deegree.protocol.wps.client.output.ComplexOutput;
import org.deegree.protocol.wps.client.output.type.ComplexOutputType;
import org.deegree.protocol.wps.client.process.Process;
import org.deegree.protocol.wps.client.process.ProcessExecution;
import org.slf4j.Logger;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
@ManagedBean
@ViewScoped
public class WpsPrinterBean implements Serializable {

	private static final String TEMPLATE_PROCESS_ID = "TEMPLATE";

	private static final long serialVersionUID = -5623716280017806019L;

	private static final Logger LOG = getLogger(WpsPrinterBean.class);

	private CodeType template;

	private List<SelectItem> templates;

	private HtmlPanelGrid metaInfoGrp;

	private String description;

	private String overview;

	public List<SelectItem> getTemplates() {
		if (templates == null) {
			initTemplates();
		}
		return templates;
	}

	private void initTemplates() {
		if (templates == null) {
			templates = new ArrayList<SelectItem>();
			WPSClient wpsClient = getWPSClient();
			if (wpsClient != null) {
				List<String> configuredTemplates = Configuration.getTemplates();
				if (configuredTemplates != null && !configuredTemplates.isEmpty()) {
					if (configuredTemplates.size() == 1) {
						Process process = wpsClient.getProcess(configuredTemplates.get(0));
						if (process != null) {
							template = process.getId();
							updateTemplateMetadata();
						}
					}
					else {
						for (String configuredTemplate : configuredTemplates) {
							Process p = wpsClient.getProcess(configuredTemplate);
							if (p != null) {
								addProcess(templates, p);
							}
						}
					}
				}
				else {
					if (wpsClient != null) {
						Process[] processes = wpsClient.getProcesses();
						if (processes.length > 1) {
							if (processes.length == 1) {
								CodeType id = processes[0].getId();
								if (TEMPLATE_PROCESS_ID.equals(id.getCode())) {
									setNotTemplateMsg();
								}
								else {
									template = id;
									updateTemplateMetadata();
								}
							}
							else {
								for (int i = 0; i < processes.length; i++) {
									Process p = processes[i];
									if (!TEMPLATE_PROCESS_ID.equals(p.getId().getCode())) {
										addProcess(templates, p);
									}
								}
							}
						}
						else {
							setNotTemplateMsg();
						}
					}
				}
			}
		}
	}

	private void setNotTemplateMsg() {
		FacesMessage fm = MessageUtils.getFacesMessage(FacesMessage.SEVERITY_INFO, "WPSPrinterBean.info.noTemplates");
		FacesContext.getCurrentInstance().addMessage(null, fm);
	}

	private void addProcess(List<SelectItem> templates, Process p) {
		if (p != null) {
			final String label = p.getId().getCode() + (p.getTitle() != null ? ("-" + p.getTitle().getString()) : "");
			LOG.debug("add prozess with id {}", p.getId());
			templates.add(new SelectItem(p.getId(), label));
		}
	}

	private WPSClient getWPSClient() {
		String wpsUrl = Configuration.getWpsUrl();
		try {
			URL capUrl = new URL(wpsUrl + "?service=WPS&version=1.0.0&request=GetCapabilities");
			return new WPSClient(capUrl);
		}
		catch (Exception e) {
			FacesMessage fm = MessageUtils.getFacesMessage(FacesMessage.SEVERITY_ERROR,
					"WPSPrinterBean.error.wpsclient", e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null, fm);
		}
		return null;
	}

	public void setTemplate(CodeType template) {
		this.template = template;
	}

	public CodeType getTemplate() {
		return template;
	}

	public String getDescription() {
		return description;
	}

	public String getOverview() {
		return overview;
	}

	public void setMetaInfoGrp(HtmlPanelGrid metaInfoGrp) {
		this.metaInfoGrp = metaInfoGrp;
	}

	public HtmlPanelGrid getMetaInfoGrp() {
		return metaInfoGrp;
	}

	public void updateTemplateMetadata(AjaxBehaviorEvent event) throws AbortProcessingException {
		updateTemplateMetadata();
	}

	private void updateTemplateMetadata() {
		description = null;
		overview = null;
		if (template != null) {
			WPSClient wpsClient = getWPSClient();
			if (wpsClient != null) {
				Process templateProcess = wpsClient.getProcess(template.getCode());
				description = templateProcess.getAbstract() != null ? templateProcess.getAbstract().getString() : null;

				Process process = wpsClient.getProcess(TEMPLATE_PROCESS_ID);
				if (process != null) {
					ProcessExecution prepareExecution = process.prepareExecution();

					try {
						LiteralInputType it = (LiteralInputType) process.getInputTypes()[0];
						if (ArrayUtils.contains(it.getAllowedValues(), template.getCode())) {

							prepareExecution.addLiteralInput(it.getId().getCode(), it.getId().getCodeSpace(),
									template.getCode(), null, null);
							ComplexOutputType ot = (ComplexOutputType) process.getOutputTypes()[0];
							prepareExecution.addOutput(ot.getId().getCode(), ot.getId().getCodeSpace(), null, true,
									ot.getDefaultFormat().getMimeType(), null, null);
							ComplexOutput eo = (ComplexOutput) prepareExecution.execute()
								.get(ot.getId().getCode(), ot.getId().getCodeSpace());
							overview = eo.getWebAccessibleURI().toASCIIString();
						}
					}
					catch (Exception e) {
						FacesMessage fm = MessageUtils.getFacesMessage(FacesMessage.SEVERITY_ERROR,
								"WPSPrinterBean.error.updateTemplate", e.getMessage());
						FacesContext.getCurrentInstance().addMessage(null, fm);
					}
				}
			}
		}
	}

	public void renderMetaInfo(ComponentSystemEvent event) throws AbortProcessingException {
		LOG.debug("append template GUI elements for template " + template);

		if (metaInfoGrp == null) {
			metaInfoGrp = new HtmlPanelGrid();
		}
		metaInfoGrp.getChildren().clear();
		if (template != null) {
			WPSClient wpsClient = getWPSClient();
			if (wpsClient != null) {
				FacesContext fc = FacesContext.getCurrentInstance();
				Process process = wpsClient.getProcess(template.getCode(), template.getCodeSpace());
				try {
					InputType[] inputTypes = process.getInputTypes();
					CodeTypeConverter converter = new CodeTypeConverter();
					for (InputType inputType : inputTypes) {
						if (inputType.getType() == Type.LITERAL) {
							addLiteralInput(fc, metaInfoGrp, (LiteralInputType) inputType, converter);
						}
						else if (inputType.getType() == Type.COMPLEX) {
							ComplexInputType comp = (ComplexInputType) inputType;
							String id = null;
							if ("http://www.deegree.org/processprovider/map"
								.equals(comp.getDefaultFormat().getSchema())) {
								id = comp.getId().getCode() + "_mapInput";
								// TODO!
								HtmlOutputText width = new HtmlOutputText();
								String style = "display:none;";
								width.setStyle(style);
								width.setId("map_width");
								width.setValue(250);
								HtmlOutputText height = new HtmlOutputText();
								height.setStyle(style);
								height.setId("map_height");
								height.setValue(500);
								metaInfoGrp.getChildren().add(width);
								metaInfoGrp.getChildren().add(height);
							}
							else if ("http://www.deegree.org/processprovider/table"
								.equals(comp.getDefaultFormat().getSchema())) {
								id = comp.getId().getCode() + "_dataInput";
							}
							if (id != null) {
								metaInfoGrp.getChildren().add(new HtmlPanelGroup());
								HtmlInputHidden input = new HtmlInputHidden();
								input.setId(id);
								ValueExpression ve = fc.getApplication()
									.getExpressionFactory()
									.createValueExpression(
											fc.getELContext(), "#{executeBean.params['"
													+ CodeTypeConverter.getAsString(inputType.getId()) + "']}",
											Object.class);
								input.setValueExpression("value", ve);
								metaInfoGrp.getChildren().add(input);
							}
						}
					}
				}
				catch (Exception e) {
					FacesMessage fm = MessageUtils.getFacesMessage(FacesMessage.SEVERITY_ERROR,
							"WPSPrinterBean.error.updateParams", e.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, fm);
				}

			}
		}
	}

	private void addLiteralInput(FacesContext fc, HtmlPanelGrid parent, LiteralInputType inputType,
			CodeTypeConverter converter) {
		String id = inputType.getId().getCode();
		String dataType = inputType.getDataType() != null ? inputType.getDataType().getValue() : null;

		HtmlOutputLabel label = new HtmlOutputLabel();
		label.setId(id + "_label");
		label.setValue(inputType.getTitle() != null ? inputType.getTitle().getString() : id);
		String inputId = id + "_input";
		label.setFor(inputId);
		UIInput input;
		if ("boolean".equals(dataType)) {
			input = new HtmlSelectBooleanCheckbox();
		}
		else {
			input = new HtmlInputText();
			if ("double".equals(dataType)) {
				input.setConverter(new DoubleConverter());
				input.addValidator(new DoubleRangeValidator(Double.MAX_VALUE, Double.MIN_VALUE));
			}
			else if ("decimal".equals(dataType)) {
				input.setConverter(new DoubleConverter());
				input.addValidator(new DoubleRangeValidator(Double.MAX_VALUE, Double.MIN_VALUE));
			}
			else if ("float".equals(dataType)) {
				input.setConverter(new FloatConverter());
				input.addValidator(new DoubleRangeValidator(Float.MAX_VALUE, Float.MIN_VALUE));
			}
			else if ("long".equals(dataType)) {
				input.setConverter(new LongConverter());
				input.addValidator(new LongRangeValidator(Long.MAX_VALUE, Long.MIN_VALUE));
			}
			else if ("integer".equals(dataType)) {
				input.setConverter(new IntegerConverter());
				input.addValidator(new LongRangeValidator(Integer.MAX_VALUE, Integer.MIN_VALUE));
			}
			else if ("date".equals(dataType)) {
				DateTimeConverter dtc = new DateTimeConverter();
				String pattern = Configuration.getDatePattern();
				dtc.setPattern(pattern);
				input.setConverter(dtc);
			}
			else if ("dateTime".equals(dataType)) {
				DateTimeConverter dtc = new DateTimeConverter();
				String pattern = Configuration.getDateTimePattern();
				dtc.setPattern(pattern);
				input.setConverter(dtc);
			}
			else if ("time".equals(dataType)) {
				DateTimeConverter dtc = new DateTimeConverter();
				String pattern = Configuration.gettimePattern();
				dtc.setPattern(pattern);
				input.setConverter(dtc);
			}
		}
		input.setId(inputId);
		ValueExpression ve = fc.getApplication()
			.getExpressionFactory()
			.createValueExpression(fc.getELContext(),
					"#{executeBean.params['" + CodeTypeConverter.getAsString(inputType.getId()) + "']}", Object.class);
		input.setValueExpression("value", ve);

		parent.getChildren().add(label);
		parent.getChildren().add(input);
	}

}
