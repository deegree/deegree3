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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.protocol.wps.client.WPSClient;
import org.deegree.protocol.wps.client.output.ComplexOutput;
import org.deegree.protocol.wps.client.output.type.OutputType;
import org.deegree.protocol.wps.client.process.Process;
import org.deegree.protocol.wps.client.process.ProcessExecution;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
@ManagedBean
@RequestScoped
public class ExecuteBean implements Serializable {

	private static final long serialVersionUID = -4044847936356583407L;

	private static final Logger LOG = getLogger(ExecuteBean.class);

	private Map<String, Object> params = new HashMap<String, Object>();

	private CodeType template;

	private String result;

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public Object print() {
		LOG.debug("try to print template " + template);
		if (template == null) {
			// TODO: msg

			return null;
		}

		String wpsUrl = Configuration.getWpsUrl();
		URL capUrl;
		try {
			capUrl = new URL(wpsUrl + "?service=WPS&version=1.0.0&request=GetCapabilities");
			WPSClient wpsClient = new WPSClient(capUrl);

			Process process = wpsClient.getProcess(getTemplate().getCode(), getTemplate().getCodeSpace());
			ProcessExecution exe = process.prepareExecution();

			for (String key : params.keySet()) {
				LOG.debug("parameter with key {} found.", key);
				Object value = params.get(key);
				if (value != null) {
					CodeType ct = CodeTypeConverter.getAsCodeType(key);
					String valueAsString;
					if (value instanceof Date) {
						DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
						valueAsString = df.format(value);
					}
					else {
						valueAsString = value.toString();
					}
					if (valueAsString.length() > 0)
						exe.addLiteralInput(ct.getCode(), ct.getCodeSpace(), valueAsString, null, null);
				}
			}

			OutputType[] outputTypes = process.getOutputTypes();
			exe.addOutput(outputTypes[0].getId().getCode(), outputTypes[0].getId().getCodeSpace(), null, true, null,
					null, null);

			ComplexOutput o = (ComplexOutput) exe.execute()
				.get(outputTypes[0].getId().getCode(), outputTypes[0].getId().getCodeSpace());
			String link = o.getWebAccessibleURI().toASCIIString();
			LOG.debug("Result can be found here: " + link);
			result = link;
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void setTemplate(CodeType template) {
		this.template = template;
	}

	public CodeType getTemplate() {
		return template;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getResult() {
		return result;
	}

}
