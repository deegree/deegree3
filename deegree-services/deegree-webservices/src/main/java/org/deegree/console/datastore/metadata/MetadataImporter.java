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
package org.deegree.console.datastore.metadata;

import static java.util.Collections.singletonList;
import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.MetadataRecordFactory;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.persistence.transaction.InsertOperation;
import org.deegree.protocol.csw.MetadataStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
@ManagedBean
@RequestScoped
public class MetadataImporter implements Serializable {

	private static Logger LOG = LoggerFactory.getLogger(MetadataImporter.class);

	private static final long serialVersionUID = -1896633353209120888L;

	private final MetadataStore ms;

	private MetadataStoreTransaction ta;

	/**
	 * single
	 */
	private static final String METADATA_S = "metadataRecord";

	/**
	 * plural
	 */
	private static final String METADATA_P = "metadataRecords";

	private String url = "";

	private int countInsertFailed;

	private File file;

	private List<String> failedFiles;

	public MetadataImporter(MetadataStore ms) {
		this.ms = ms;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void importData() throws Throwable {

		int countInserted = 0;
		countInsertFailed = 0;
		failedFiles = new ArrayList<String>();
		try {

			File folder = new File(url);
			if (!folder.exists()) {
				LOG.info("folder doesn't exists!");
				FacesMessage fm = new FacesMessage(SEVERITY_ERROR, "Folder doesn't exists!", null);
				FacesContext.getCurrentInstance().addMessage(null, fm);
				return;
			}
			File[] fileArray = folder.listFiles();

			InsertOperation insert = null;
			for (File file : fileArray) {
				this.file = file;
				ta = ms.acquireTransaction();
				MetadataRecord record = MetadataRecordFactory.create(file);
				insert = new InsertOperation(singletonList(record), record.getAsOMElement().getQName(), "insert");
				try {
					ta.performInsert(insert);
					ta.commit();
					countInserted++;
				}
				catch (MetadataInspectorException e) {
					e.printStackTrace();
					// skip
					fail(file);
				}
				catch (MetadataStoreException e2) {
					e2.printStackTrace();
					// skip
					fail(file);
				}
			}

		}
		catch (Throwable t) {
			t.printStackTrace();
			fail(file);
			FacesMessage fm = new FacesMessage(SEVERITY_ERROR,
					"Metadata import failed: " + t.getMessage() + " check file: " + file.getName(), null);
			FacesContext.getCurrentInstance().addMessage(null, fm);
			return;
		}
		String metadataRecordOut;
		if (countInserted == 1) {
			metadataRecordOut = METADATA_S;
		}
		else {
			metadataRecordOut = METADATA_P;
		}
		String msgImportOK = "Imported " + countInserted + " " + metadataRecordOut + ".";
		if (countInsertFailed == 1) {
			metadataRecordOut = METADATA_S;
		}
		else {
			metadataRecordOut = METADATA_P;
		}
		String msgImportFail = "Failed to import " + countInsertFailed + " " + metadataRecordOut + ". "
				+ "Ignored files are: " + failedFiles;
		FacesMessage fm1 = new FacesMessage(SEVERITY_INFO, msgImportOK, null);
		FacesMessage fm2 = new FacesMessage(SEVERITY_INFO, msgImportFail, null);
		FacesContext.getCurrentInstance().addMessage(null, fm1);
		FacesContext.getCurrentInstance().addMessage(null, fm2);
	}

	private void fail(File file) throws MetadataStoreException {
		countInsertFailed++;
		if (failedFiles != null) {
			failedFiles.add(file.getName());
		}

		if (ta != null) {
			try {
				ta.rollback();
			}
			catch (MetadataStoreException e) {
				throw new MetadataStoreException(e.getMessage());
			}
		}
	}

}
