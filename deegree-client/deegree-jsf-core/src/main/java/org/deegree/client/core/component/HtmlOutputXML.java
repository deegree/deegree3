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

import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIOutput;

import org.deegree.client.core.utils.MessageUtils;

/**
 * Renders an XML document indented and colored, if possible. It's also possible to create
 * a link for downloading the value. The created file will be deleted after the given
 * time, default are 60 minutes.
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@ResourceDependency(library = "deegree", name = "css/outputXML.css", target = "head")
@FacesComponent(value = "HtmlOutputXML")
public class HtmlOutputXML extends UIOutput {

	public HtmlOutputXML() {
		setRendererType("org.deegree.OutputXML");
	}

	private static enum AdditionalPropertyKeys {

		downloadable, downloadLabel, downloadFile, minutesUntilDelete, styleClass

	}

	/**
	 * @param downloadLabel The text rendered as label for the download link. If null, the
	 * default value will be returned.
	 */
	public void setDownloadLabel(String downloadLabel) {
		getStateHelper().put(AdditionalPropertyKeys.downloadLabel, downloadLabel);
	}

	/**
	 * @return The text rendered as label for the download link.
	 */
	public String getDownloadLabel() {
		String label = MessageUtils.getResourceText(null,
				"org.deegree.client.core.component.HtmlOutputXML.DOWNLOADLABEL");
		return (String) getStateHelper().eval(AdditionalPropertyKeys.downloadLabel, label);
	}

	/**
	 * @param downloadable true if a link for a download should be rendered. Default is
	 * false.
	 */
	public void setDownloadable(boolean downloadable) {
		getStateHelper().put(AdditionalPropertyKeys.downloadable, downloadable);
	}

	/**
	 * @return true, if a link for downloading the value as file should be rendered.
	 * Default is false.
	 */
	public boolean isDownloadable() {
		return (Boolean) getStateHelper().eval(AdditionalPropertyKeys.downloadable, false);
	}

	/**
	 * @param downloadFile The name of the directory in the web app directory, where the
	 * files should be stored. Can be null.
	 */
	public void setDownloadFile(String downloadFile) {
		getStateHelper().put(AdditionalPropertyKeys.downloadFile, downloadFile);
	}

	/**
	 * @return The name of the directory in the web app directory, where the files should
	 * be stored. Can be null.
	 */
	public String getDownloadFile() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.downloadFile, null);
	}

	/**
	 * @param minutesUntilDelete Time in minutes after the created file should be deleted.
	 * A value <= 0 means, the file should not be deleted. Default value is 60.
	 */
	public void setMinutesUntilDelete(int minutesUntilDelete) {
		getStateHelper().put(AdditionalPropertyKeys.minutesUntilDelete, minutesUntilDelete);
	}

	/**
	 * @return The time in minutes after the created file should be deleted. A value <= 0
	 * means, the file should not be deleted. Default value is 60.
	 */
	public int getMinutesUntilDelete() {
		return (Integer) getStateHelper().eval(AdditionalPropertyKeys.minutesUntilDelete, 60);
	}

	/**
	 * @return A comma seperated list of available styleClasses. Default value is
	 * "outputXML".
	 */
	public String getStyleClass() {
		return (String) getStateHelper().eval(AdditionalPropertyKeys.styleClass, "outputXML");

	}

	/**
	 * @param styleClass A comma sepereated list of available style classes, passed
	 * through the class attribute of the component.
	 */
	public void setStyleClass(String styleClass) {
		getStateHelper().put(AdditionalPropertyKeys.styleClass, styleClass);
	}

	@Override
	public String getValue() {
		String v = null;
		Object value = super.getValue();
		if (value != null) {
			if (value instanceof String) {
				v = (String) value;
			}
			else {
				v = value.toString();
			}
		}
		return v;
	}

}
