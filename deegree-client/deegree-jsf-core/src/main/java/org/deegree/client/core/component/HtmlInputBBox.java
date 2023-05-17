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

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.FacesComponent;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;

import org.deegree.client.core.model.BBox;
import org.deegree.client.core.utils.MessageUtils;

/**
 * <code>HtmlInputBBox</code>
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */

@FacesComponent(value = "HtmlInputBBox")
public class HtmlInputBBox extends UISelectOne {

	/**
	 * <p>
	 * The standard component type for this component.
	 * </p>
	 */
	public static final String COMPONENT_TYPE = "HtmlInputBBox";

	private static enum AdditionalPropertyKeys {

		showCRS, styleClass, crsLabel, crsSize, minxLabel, minyLabel, maxxLabel, maxyLabel, disabled, onchange,
		labelColumnClass, inputColumnClass

	}

	public HtmlInputBBox() {
		setRendererType("org.deegree.InputBBox");
	}

	public String getOnchange() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.onchange, null);
	}

	public void setOnchange(String onchange) {
		getStateHelper().put(AdditionalPropertyKeys.onchange, onchange);
	}

	public String getStyleClass() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.styleClass, null);
	}

	public void setStyleClass(String styleClass) {
		getStateHelper().put(AdditionalPropertyKeys.styleClass, styleClass);
	}

	public String getLabelColumnClass() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.labelColumnClass, null);
	}

	public void setLabelColumnClass(String labelColumnClass) {
		getStateHelper().put(AdditionalPropertyKeys.labelColumnClass, labelColumnClass);
	}

	public String getInputColumnClass() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.inputColumnClass, null);
	}

	public void setInputColumnClass(String inputColumnClass) {
		getStateHelper().put(AdditionalPropertyKeys.inputColumnClass, inputColumnClass);
	}

	public String getCrsLabel() {
		String crsLabel = (String) getStateHelper().eval(AdditionalPropertyKeys.crsLabel, null);
		if (crsLabel == null) {
			crsLabel = MessageUtils.getResourceText(null,
					"org.deegree.client.core.renderer.InputBBoxRenderer.CRSLABEL");
		}
		return crsLabel;
	}

	public void setCrsLabel(String crsLabel) {
		getStateHelper().put(AdditionalPropertyKeys.crsLabel, crsLabel);
	}

	public String getMinxLabel() {
		String minxLabel = (String) getStateHelper().eval(AdditionalPropertyKeys.minxLabel, null);
		if (minxLabel == null) {
			minxLabel = MessageUtils.getResourceText(null,
					"org.deegree.client.core.renderer.InputBBoxRenderer.MINXLABEL");
		}
		return minxLabel;
	}

	public void setMinxLabel(String minxLabel) {
		getStateHelper().put(AdditionalPropertyKeys.minxLabel, minxLabel);
	}

	public String getMinyLabel() {
		String minyLabel = (String) getStateHelper().eval(AdditionalPropertyKeys.minyLabel, null);
		if (minyLabel == null) {
			minyLabel = MessageUtils.getResourceText(null,
					"org.deegree.client.core.renderer.InputBBoxRenderer.MINYLABEL");
		}
		return minyLabel;
	}

	public void setMinyLabel(String minyLabel) {
		getStateHelper().put(AdditionalPropertyKeys.minyLabel, minyLabel);
	}

	public String getMaxxLabel() {
		String maxxLabel = (String) getStateHelper().eval(AdditionalPropertyKeys.maxxLabel, null);
		if (maxxLabel == null) {
			maxxLabel = MessageUtils.getResourceText(null,
					"org.deegree.client.core.renderer.InputBBoxRenderer.MAXXLABEL");
		}
		return maxxLabel;
	}

	public void setMaxxLabel(String maxxLabel) {
		getStateHelper().put(AdditionalPropertyKeys.maxxLabel, maxxLabel);
	}

	public String getMaxyLabel() {
		String maxyLabel = (String) getStateHelper().eval(AdditionalPropertyKeys.maxyLabel, null);
		if (maxyLabel == null) {
			maxyLabel = MessageUtils.getResourceText(null,
					"org.deegree.client.core.renderer.InputBBoxRenderer.MAXYLABEL");
		}
		return maxyLabel;
	}

	public void setMaxyLabel(String maxyLabel) {
		getStateHelper().put(AdditionalPropertyKeys.maxyLabel, maxyLabel);
	}

	public void setCrsSize(int crsSize) {
		getStateHelper().put(AdditionalPropertyKeys.crsSize, crsSize);
	}

	public int getCrsSize() {
		return (Integer) getStateHelper().eval(AdditionalPropertyKeys.crsSize, -1);
	}

	public boolean isShowCRS() {
		return (Boolean) getStateHelper().eval(AdditionalPropertyKeys.showCRS, true);
	}

	public void setShowCRS(boolean crsLabel) {
		getStateHelper().put(AdditionalPropertyKeys.showCRS, crsLabel);
	}

	public void setDisabled(boolean disabled) {
		getStateHelper().put(AdditionalPropertyKeys.disabled, disabled);
	}

	public boolean isDisabled() {
		return (Boolean) getStateHelper().eval(AdditionalPropertyKeys.disabled, false);
	}

	@Override
	public BBox getValue() {
		Object value = super.getValue();
		if (value == null) {
			return null;
		}
		if (!(value instanceof BBox)) {
			throw new FacesException("value of HtmlInputBBox must be a org.deegree.client.core.model.BBox");
		}
		return (BBox) super.getValue();
	}

	@Override
	protected void validateValue(FacesContext context, Object value) {

		if (!isValid()) {
			return;
		}

		if (isRequired() && isBBoxEmpty(value)) {
			FacesMessage message = MessageUtils.getFacesMessage(FacesMessage.SEVERITY_ERROR,
					"org.deegree.client.core.component.HtmlInputBBox.REQUIRED", getClientId());
			context.addMessage(getClientId(context), message);
			setValid(false);
			return;
		}

		BBox bbox = (BBox) value;
		if (isShowCRS() && bbox.getCrs() == null) {
			FacesMessage message = MessageUtils.getFacesMessage(FacesMessage.SEVERITY_ERROR,
					"org.deegree.client.core.component.HtmlInputBBox.INVALID_CRS", getClientId());
			context.addMessage(getClientId(context), message);
			setValid(false);
		}
		if (Double.isNaN(bbox.getMinx())) {
			FacesMessage message = MessageUtils.getFacesMessage(FacesMessage.SEVERITY_ERROR,
					"org.deegree.client.core.component.HtmlInputBBox.INVALID_MINX", getClientId(), bbox.getMinx());
			context.addMessage(getClientId(context), message);
			setValid(false);
		}

		if (Double.isNaN(bbox.getMinY())) {
			FacesMessage message = MessageUtils.getFacesMessage(FacesMessage.SEVERITY_ERROR,
					"org.deegree.client.core.component.HtmlInputBBox.INVALID_MINY", getClientId(), bbox.getMinY());
			context.addMessage(getClientId(context), message);
			setValid(false);
		}
		if (Double.isNaN(bbox.getMaxX())) {
			FacesMessage message = MessageUtils.getFacesMessage(FacesMessage.SEVERITY_ERROR,
					"org.deegree.client.core.component.HtmlInputBBox.INVALID_MAXX", getClientId(), bbox.getMaxX());
			context.addMessage(getClientId(context), message);
			setValid(false);
		}
		if (Double.isNaN(bbox.getMaxY())) {
			FacesMessage message = MessageUtils.getFacesMessage(FacesMessage.SEVERITY_ERROR,
					"org.deegree.client.core.component.HtmlInputBBox.INVALID_MAXY", getClientId(), bbox.getMaxY());
			context.addMessage(getClientId(context), message);
			setValid(false);
		}

		if (bbox.getMaxX() < bbox.getMinx() || bbox.getMaxY() < bbox.getMaxY()) {
			FacesMessage message = MessageUtils.getFacesMessage(FacesMessage.SEVERITY_ERROR,
					"org.deegree.client.core.component.HtmlInputBBox.INVALID_BBOX", getClientId(), bbox.getMinx(),
					bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());
			context.addMessage(getClientId(context), message);
			setValid(false);
		}
	}

	private boolean isBBoxEmpty(Object value) {
		if (value == null) {
			return true;
		}
		if (!(value instanceof BBox)) {
			return true;
		}
		BBox bbox = (BBox) value;
		if ((isShowCRS() && bbox.getCrs() != null) && !(bbox.getCrs().length() > 0) && Double.isNaN(bbox.getMinx())
				&& Double.isNaN(bbox.getMinY()) && Double.isNaN(bbox.getMaxX()) && Double.isNaN(bbox.getMaxY())) {
			return true;
		}
		return false;
	}

}
