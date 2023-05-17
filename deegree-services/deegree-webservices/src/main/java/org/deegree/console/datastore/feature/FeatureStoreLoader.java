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
package org.deegree.console.datastore.feature;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.deegree.protocol.wfs.transaction.action.IDGenMode.GENERATE_NEW;
import static org.deegree.protocol.wfs.transaction.action.IDGenMode.USE_EXISTING;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.protocol.wfs.transaction.action.IDGenMode;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
@ManagedBean
@RequestScoped
public class FeatureStoreLoader implements Serializable {

	private static final long serialVersionUID = 5091506903775758089L;

	private final FeatureStore fs;

	private GMLVersion gmlVersion = GML_32;

	private IDGenMode idGenMode = GENERATE_NEW;

	private String url = "";

	FeatureStoreLoader(FeatureStore fs) {
		this.fs = fs;
		if (fs.getSchema().getGMLSchema() != null) {
			gmlVersion = fs.getSchema().getGMLSchema().getVersion();
		}
	}

	public String getGmlVersion() {
		return gmlVersion.name();
	}

	public void setGmlVersion(String gmlVersion) {
		this.gmlVersion = GMLVersion.valueOf(gmlVersion);
	}

	public String[] getAvailableGmlVersions() {
		String[] gmlVersions = new String[GMLVersion.values().length];
		int i = 0;
		for (GMLVersion version : GMLVersion.values()) {
			gmlVersions[i++] = version.name();
		}
		return gmlVersions;
	}

	public IDGenMode getIdGenMode() {
		return idGenMode;
	}

	public void setIdGenMode(IDGenMode idGenMode) {
		this.idGenMode = idGenMode;
	}

	public IDGenMode[] getAvailableIdGenModes() {
		return new IDGenMode[] { GENERATE_NEW, USE_EXISTING };
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void importData() throws Throwable {
		List<String> fids = null;
		FeatureStoreTransaction ta = null;
		try {
			GMLStreamReader gmlStream = GMLInputFactory.createGMLStreamReader(gmlVersion, new URL(url));
			gmlStream.setApplicationSchema(fs.getSchema());
			FeatureCollection fc = gmlStream.readFeatureCollection();
			gmlStream.getIdContext().resolveLocalRefs();
			gmlStream.close();
			ta = fs.acquireTransaction();
			fids = ta.performInsert(fc, idGenMode);
			ta.commit();
		}
		catch (Throwable t) {
			if (ta != null) {
				try {
					ta.rollback();
				}
				catch (FeatureStoreException e) {
					e.printStackTrace();
				}
			}
			FacesMessage fm = new FacesMessage(SEVERITY_ERROR, "GML import failed: " + t.getMessage(), null);
			FacesContext.getCurrentInstance().addMessage(null, fm);
			return;
		}

		FacesMessage fm = new FacesMessage(SEVERITY_INFO, "Imported " + fids.size() + " features", null);
		FacesContext.getCurrentInstance().addMessage(null, fm);
	}

}
