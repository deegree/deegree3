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

import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.AjaxBehaviorListener;

import org.slf4j.Logger;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public class MultipleComponentListener implements AjaxBehaviorListener {

	private static final Logger LOG = getLogger(MultipleComponentListener.class);

	public static final String INPUT_ID_PARAM = "INPUT_ID";

	public static final String OCC_PARAM = "OCC";

	public static final String TYPE_PARAM = "TYPE_MC";

	public static final String INDEX_PARAM = "INDEX_OF_OCC";

	public enum MC_TYPE {

		add, remove

	}

	@SuppressWarnings("unchecked")
	@Override
	public void processAjaxBehavior(AjaxBehaviorEvent event) throws AbortProcessingException {
		FacesContext fc = FacesContext.getCurrentInstance();
		FormBean fb = (FormBean) fc.getApplication().getELResolver().getValue(fc.getELContext(), null, "formBean");
		if (fb != null) {
			UIComponent comp = event.getComponent();
			List<UIComponent> children = comp.getChildren();
			String id = null;
			MC_TYPE type = MC_TYPE.add;
			Map<String, Integer> occurences = null;
			int index = 0;
			for (UIComponent child : children) {
				if (child instanceof UIParameter) {
					UIParameter param = (UIParameter) child;
					if (INPUT_ID_PARAM.equals(param.getName())) {
						id = (String) param.getValue();
					}
					else if (OCC_PARAM.equals(param.getName())) {
						try {
							occurences = (Map<String, Integer>) param.getValue();
						}
						catch (Exception e) {
							// Do nothing
						}
					}
					else if (TYPE_PARAM.equals(param.getName())) {
						try {
							type = (MC_TYPE) param.getValue();
						}
						catch (Exception e) {
							// Do nothing
						}
					}
					else if (INDEX_PARAM.equals(param.getName())) {
						try {
							index = (Integer) param.getValue();
						}
						catch (Exception e) {
							// Do nothing
						}
					}
				}
			}
			LOG.debug("Updatie from: " + id + "; type: " + type + "; occ: " + occurences);
			if (occurences == null) {
				occurences = new HashMap<String, Integer>();
			}
			if (id != null) {
				if (MC_TYPE.remove.equals(type) && occurences.containsKey(id) && occurences.get(id) > 1) {
					occurences.put(id, occurences.get(id) - 1);
				}
				else if (MC_TYPE.add.equals(type)) {
					if (!occurences.containsKey(id)) {
						occurences.put(id, 1);
					}
					occurences.put(id, occurences.get(id) + 1);
				}
				fb.setOccurence(occurences);
				if (MC_TYPE.remove.equals(type) && occurences.containsKey(id) && occurences.get(id) > 1) {
					// remove the value!
					ExecuteBean eb = (ExecuteBean) fc.getApplication()
						.getELResolver()
						.getValue(fc.getELContext(), null, "executeBean");
					eb.getXmlInputs().remove(id + ":" + index);
					eb.getXmlRefInputs().remove(id + ":" + index);
					eb.getBboxInputs().remove(id + ":" + index);
					eb.getLiteralInputs().remove(id + ":" + index);
					eb.getBinaryInputs().remove(id + ":" + index);
					eb.getComplexInputFormats().remove(id + ":" + index);
				}
			}
		}
	}

}
