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
package org.deegree.client.core.validator;

import java.util.ArrayList;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.deegree.client.core.model.UploadedFile;
import org.deegree.client.core.utils.MessageUtils;

/**
 *
 * <code>FileMimeTypeValidator</code> validates the mime type of an {@link UploadedFile}
 * to a list of supported mime types.
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
@FacesValidator(value = "fileMimeTypeValidator")
public class FileMimeTypeValidator implements Validator {

	private List<String> mimeTypes = new ArrayList<String>();

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		UploadedFile uploadedFile = (UploadedFile) value;
		if (uploadedFile != null) {
			MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
			String mimeType = mimetypesFileTypeMap.getContentType(uploadedFile.getFileName());

			if (!mimeTypes.contains(mimeType)) {
				uploadedFile.delete();
				FacesMessage message = MessageUtils.getFacesMessage(FacesMessage.SEVERITY_ERROR,
						"org.deegree.client.core.validator.FileMimeTypeValidator.invalidMimeType", mimeType, mimeTypes);
				throw new ValidatorException(message);
			}
		}
	}

	/**
	 * @param mimeTypes the supported mime types
	 */
	public void setMimeTypes(List<String> mimeTypes) {
		this.mimeTypes = mimeTypes;
	}

	/**
	 * @param mimeType a mime type to add to the list of supported mime types
	 */
	public void addMimeType(String mimeType) {
		mimeTypes.add(mimeType);
	}

}
