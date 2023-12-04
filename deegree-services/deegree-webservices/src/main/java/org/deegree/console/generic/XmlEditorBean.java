/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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
package org.deegree.console.generic;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static javax.faces.context.FacesContext.getCurrentInstance;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.xerces.xni.parser.XMLParseException;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.xml.schema.SchemaValidationEvent;
import org.deegree.commons.xml.schema.SchemaValidator;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceManager;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.ResourceProvider;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.WorkspaceUtils;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.deegree.workspace.standard.DefaultResourceIdentifier;
import org.deegree.workspace.standard.DefaultResourceLocation;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.slf4j.Logger;

@ManagedBean
@ViewScoped
public class XmlEditorBean implements Serializable {

	private static final long serialVersionUID = -2345424266499294734L;

	private static final Logger LOG = getLogger(XmlEditorBean.class);

	private String id;

	private String fileName;

	private String schemaUrl;

	private String nextView;

	private String content;

	private String schemaAsText;

	private String resourceProviderClass;

	private String emptyTemplate;

	public String getEmptyTemplate() {
		return emptyTemplate;
	}

	public void setEmptyTemplate(String emptyTemplate) {
		this.emptyTemplate = emptyTemplate;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSchemaUrl() {
		if (schemaUrl == null) {
			try {
				Workspace workspace = OGCFrontController.getServiceWorkspace().getNewWorkspace();
				Class<?> cls = workspace.getModuleClassLoader().loadClass(resourceProviderClass);
				ResourceMetadata<?> md = workspace.getResourceMetadata((Class) cls, id);
				if (md.getProvider() instanceof AbstractResourceProvider) {
					setSchemaUrl(((AbstractResourceProvider) md.getProvider()).getSchema().toExternalForm());
				}
			}
			catch (Exception e) {
				LOG.trace(e.getLocalizedMessage());
			}
		}
		return schemaUrl;
	}

	public void setSchemaUrl(String schemaUrl) {
		this.schemaUrl = schemaUrl;
		if (schemaUrl != null && !schemaUrl.isEmpty()) {
			try {
				schemaAsText = IOUtils.toString(new URL(schemaUrl).openStream(), "UTF-8");
			}
			catch (IOException e) {
				LOG.error("Error while setting schema file: ", e.getLocalizedMessage());
			}
		}
	}

	public String getNextView() {
		return nextView;
	}

	public void setNextView(String nextView) {
		this.nextView = nextView;
	}

	private File getFile() {
		File workspaceRoot = new File(DeegreeWorkspace.getWorkspaceRoot());

		String separator = "";
		StringBuilder sb = new StringBuilder();
		for (String filePart : fileName.split("[/\\\\]+")) {
			if ("..".equals(filePart)) {
				continue;
			}

			sb.append(separator);
			sb.append(filePart);

			separator = File.separator;
		}

		return new File(workspaceRoot, sb.toString());
	}

	public String getContent() throws IOException, ClassNotFoundException {
		LOG.trace("Editing file: " + getFileName() + " with ID + " + getId());
		LOG.trace("Using schema: " + getSchemaUrl());
		LOG.trace("Editor content: " + content);
		LOG.trace("Related ResourceProviderClass: " + resourceProviderClass);
		LOG.trace("Next view: " + getNextView());
		if (content == null) {
			LOG.trace("No content set for " + this.toString());
			if (resourceProviderClass == null) {
				File file = getFile();
				if (fileName != null && file.exists()) {
					LOG.trace("Loading content from file: " + file.getAbsolutePath());
					content = FileUtils.readFileToString(file);
					LOG.trace("Setting content to: " + content);
					return content;
				}
				else if (emptyTemplate != null) {
					// load template content if the requested file did not exists
					LOG.trace("Loading template from " + emptyTemplate);
					StringWriter sw = new StringWriter();
					IOUtils.copy((new URL(emptyTemplate)).openStream(), sw);
					content = sw.toString();
					LOG.trace("Setting content to:" + content);
					return content;
				}
			}
			else {
				Workspace workspace = OGCFrontController.getServiceWorkspace().getNewWorkspace();
				Class<?> cls = workspace.getModuleClassLoader().loadClass(resourceProviderClass);
				ResourceMetadata<?> md = workspace.getResourceMetadata((Class) cls, id);
				if (md != null) {
					content = IOUtils.toString(md.getLocation().getAsStream());
					LOG.trace("Loading content from resource: " + md.getLocation().getAsFile().getAbsolutePath());
				}
				else if (emptyTemplate != null) {
					LOG.trace("Loading template for resource provider " + this.getResourceProviderClass() + " from "
							+ emptyTemplate);
					StringWriter sw = new StringWriter();
					IOUtils.copy((new URL(emptyTemplate)).openStream(), sw);
					content = sw.toString();
				}
			}
		}
		LOG.trace("Setting content to: " + content);
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSchemaAsText() {
		return schemaAsText;
	}

	public void setSchemaAsText(String schemaAsText) {
		this.schemaAsText = schemaAsText;
	}

	public String cancel() {
		return nextView;
	}

	public String save() {
		if (checkValidity()) {
			activate();
			FacesMessage fm = new FacesMessage(SEVERITY_INFO, "Saved configuration.", null);
			getCurrentInstance().addMessage(null, fm);
			return nextView;
		}
		return null;
	}

	public String getResourceProviderClass() {
		return resourceProviderClass;
	}

	public void setResourceProviderClass(String providerClass) {
		this.resourceProviderClass = providerClass;
	}

	private ResourceManager<?> lookupResourceManager(Workspace workspace, Class<?> cls) {
		ResourceManager<?> mgr = null;
		outer: for (ResourceManager<?> r : workspace.getResourceManagers()) {
			for (ResourceProvider<?> p : r.getProviders()) {
				if (p != null && cls.isAssignableFrom(p.getClass())) {
					mgr = r;
					break outer;
				}
			}
		}

		return mgr;
	}

	private void activate() {
		try {
			if (resourceProviderClass == null) {
				FileUtils.write(getFile(), content);
				return;
			}

			Workspace workspace = OGCFrontController.getServiceWorkspace().getNewWorkspace();
			Class<?> cls = workspace.getModuleClassLoader().loadClass(resourceProviderClass);
			ResourceMetadata<?> md = workspace.getResourceMetadata((Class) cls, id);

			if (md != null) {
				workspace.destroy(md.getIdentifier());

				md.getLocation().setContent(IOUtils.toInputStream(content));
				workspace.getLocationHandler().persist(md.getLocation());
				workspace.getLocationHandler().activate(md.getLocation());
				WorkspaceUtils.reinitializeChain(workspace, md.getIdentifier());
			}
			else {
				// newly create entry
				if (!(workspace instanceof DefaultWorkspace)) {
					throw new Exception("Could not persist configuration.");
				}

				ResourceManager<?> mgr = lookupResourceManager(workspace, cls);

				File wsDir = ((DefaultWorkspace) workspace).getLocation();

				File resourceDir = new File(wsDir, mgr.getMetadata().getWorkspacePath());
				File resourceFile = new File(resourceDir, id + ".xml");

				if (!resourceDir.exists() && !resourceDir.mkdirs()) {
					throw new IOException("Could not create resource directory '" + resourceDir + "'");
				}

				FileUtils.writeStringToFile(resourceFile, content);

				DefaultResourceIdentifier<?> ident = new DefaultResourceIdentifier(cls, id);
				ResourceLocation<?> loc = new DefaultResourceLocation(resourceFile, ident);
				workspace.add(loc);

				workspace.getLocationHandler().activate(loc);
				WorkspaceUtils.reinitializeChain(workspace, ident);
			}
		}
		catch (Exception t) {
			LOG.warn("Could not activate Resource {}", t.getMessage());
			LOG.trace("Exception", t);
			FacesMessage fm = new FacesMessage(SEVERITY_ERROR, "Unable to activate resource: " + t.getMessage(), null);
			FacesContext.getCurrentInstance().addMessage(null, fm);
			return;
		}
	}

	public boolean checkValidity() {
		try {
			InputStream xml = new ByteArrayInputStream(content.getBytes("UTF-8"));
			// Workaround to allow relative imports (as these are not working with fully
			// qualified URLs)
			if (schemaUrl != null && schemaUrl.startsWith("jar:file") && schemaUrl.indexOf('!') > 0) {
				schemaUrl = schemaUrl.substring(schemaUrl.indexOf('!') + 1);
			}

			String[] schemas = new String[] { schemaUrl };
			List<SchemaValidationEvent> events = SchemaValidator.validate(xml, schemas);
			if (!events.isEmpty()) {
				for (SchemaValidationEvent event : events) {
					String msg = toString(event);
					FacesMessage fm = new FacesMessage(SEVERITY_WARN, msg, null);
					getCurrentInstance().addMessage(null, fm);
				}
				return false;
			}
		}
		catch (UnsupportedEncodingException e) {
			LOG.error("UTF-8 is not supported!");
			return true;
		}
		return true;
	}

	private String toString(SchemaValidationEvent event) {
		XMLParseException e = event.getException();
		return "<a href=\"javascript:jumpTo(" + e.getLineNumber() + "," + e.getColumnNumber() + ")\">Error near line "
				+ e.getLineNumber() + ", column " + e.getColumnNumber() + "</a>: " + e.getLocalizedMessage();
	}

	public String validate() {
		if (checkValidity()) {
			FacesMessage fm = new FacesMessage(SEVERITY_INFO, "Document is valid.", null);
			getCurrentInstance().addMessage(null, fm);
		}
		return null;
	}

	public String getTitle() throws ClassNotFoundException {
		if (fileName != null) {
			return fileName;
		}

		Workspace workspace = OGCFrontController.getServiceWorkspace().getNewWorkspace();
		Class<?> cls = workspace.getModuleClassLoader().loadClass(resourceProviderClass);
		ResourceMetadata<?> md = workspace.getResourceMetadata((Class) cls, id);

		if (md == null) {
			// lookup path if file will be created from template
			ResourceManager<?> mgr = lookupResourceManager(workspace, cls);
			if (mgr != null) {
				return mgr.getMetadata().getWorkspacePath() + "/" + id;
			}
		}
		else {
			for (ResourceManager<? extends Resource> resourceManager : workspace.getResourceManagers()) {
				if (resourceManager.getProviders().contains(md.getProvider())) {
					return resourceManager.getMetadata().getWorkspacePath() + "/" + id;
				}
			}
		}

		return null;
	}

}
