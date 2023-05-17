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
package org.deegree.client.core.component;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.html.HtmlInputTextarea;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;

import org.deegree.client.core.utils.MessageUtils;
import org.deegree.commons.xml.schema.SchemaValidationEvent;
import org.deegree.commons.xml.schema.SchemaValidator;
import org.slf4j.Logger;

/**
 * Component to handle XML inputs. The input value will be validated against a list of
 * schema URLs.
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@FacesComponent(value = "HtmlInputConfigurationXML")
public class HtmlInputConfigurationXML extends HtmlInputTextarea {

	private static final Logger LOG = getLogger(HtmlInputConfigurationXML.class);

	private enum AdditionalProperties {

		schemaURLS

	}

	public void setSchemaURLS(String schemaURLS) {
		getStateHelper().put(AdditionalProperties.schemaURLS, schemaURLS);
	}

	public String getSchemaURLS() {
		return (String) getStateHelper().eval(AdditionalProperties.schemaURLS, null);
	}

	@Override
	protected Object getConvertedValue(FacesContext context, Object newSubmittedValue) throws ConverterException {
		Object o = super.getConvertedValue(context, newSubmittedValue);
		String v;
		if (o instanceof String) {
			v = (String) o;
		}
		else {
			v = o.toString();
		}
		if (!v.startsWith("<?")) {
			// append xml prolog
			v = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + v;
		}
		return v;
	}

	@Override
	protected void validateValue(FacesContext context, Object newValue) {
		super.validateValue(context, newValue);
		LOG.debug("validate value " + newValue);
		try {
			String v = (String) newValue;
			InputStream xml = new ByteArrayInputStream(v.getBytes("UTF-8"));
			String s = getSchemaURLS();
			LOG.debug("Schemas: " + s);
			String[] schemas = null;
			if (s != null && s.length() > 0) {
				schemas = s.split(",");
				List<String> results = new ArrayList<String>();
				List<SchemaValidationEvent> evts = SchemaValidator.validate(xml, schemas);
				for (SchemaValidationEvent evt : evts) {
					results.add(evt.toString());
				}
				if (results.size() > 0) {
					FacesMessage message = MessageUtils.getFacesMessage(

							FacesMessage.SEVERITY_ERROR,
							"org.deegree.client.core.component.HtmlInputConfiguration.VALIDATION_FAILED", results);
					context.addMessage(getClientId(), message);
					setValid(false);
					return;
				}
			}
		}
		catch (UnsupportedEncodingException e) {
			LOG.error("UTF-8 is not supported!");
			return;
		}
		setValid(true);
	}

}