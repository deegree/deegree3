/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.metadata.persistence.inspectors;

import static org.deegree.metadata.DCRecord.SCHEMA_URL;
import static org.deegree.metadata.iso.ISORecord.SCHEMA_URL_GMD;
import static org.deegree.metadata.iso.ISORecord.SCHEMA_URL_SRV;

import java.io.InputStream;
import java.sql.Connection;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.io.StreamBufferStore;
import org.deegree.commons.xml.schema.SchemaValidationEvent;
import org.deegree.commons.xml.schema.SchemaValidator;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.i18n.Messages;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.sqldialect.SQLDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RecordInspector} that performs schema-validation.
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public class MetadataSchemaValidationInspector<T extends MetadataRecord> implements RecordInspector<T> {

	private static Logger LOG = LoggerFactory.getLogger(MetadataSchemaValidationInspector.class);

	/**
	 * Before any transaction operation is possible there should be an evaluation of the
	 * record. The response of the full ISO record has to be valid. With this method this
	 * is guaranteed.
	 * @param elem that has to be evaluated before there is any transaction operation
	 * possible.
	 * @return a list of error-strings, or empty list if there is no validation needed.
	 * @throws MetadataStoreException
	 */
	private List<SchemaValidationEvent> validate(OMElement elem) throws MetadataInspectorException {
		InputStream is = null;
		try {
			StreamBufferStore os = new StreamBufferStore();
			elem.serialize(os);
			is = os.getInputStream();
		}
		catch (Throwable e) {
			LOG.debug("error: " + e.getMessage(), e);
			throw new MetadataInspectorException(e.getMessage());
		}

		if (new QName("http://www.isotc211.org/2005/gmd", "MD_Metadata").equals(elem.getQName())) {
			return SchemaValidator.validate(is, SCHEMA_URL_GMD, SCHEMA_URL_SRV);
		}
		// DublinCore
		return SchemaValidator.validate(is, SCHEMA_URL);
	}

	@Override
	public T inspect(T record, Connection conn, SQLDialect dialect) throws MetadataInspectorException {
		List<SchemaValidationEvent> errors = validate(record.getAsOMElement());
		if (errors.isEmpty()) {
			return record;
		}
		else {
			StringBuilder sb = new StringBuilder();
			for (SchemaValidationEvent error : errors) {
				sb.append(error);
				sb.append("\n");
			}
			String msg = Messages.getMessage("ERROR_VALIDATE" + sb);
			LOG.debug(msg);
			throw new MetadataInspectorException(msg);
		}
	}

}