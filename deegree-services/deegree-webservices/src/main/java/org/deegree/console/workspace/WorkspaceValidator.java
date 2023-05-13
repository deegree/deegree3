package org.deegree.console.workspace;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.workspace.ErrorHandler;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceManager;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.File;
import java.net.URI;
import java.nio.file.PathMatcher;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;

public class WorkspaceValidator {

	private static Logger LOG = LoggerFactory.getLogger(WorkspaceValidator.class);

	public void validateWorkspace(DeegreeWorkspace workspace) {
		Map<String, List<String>> resourcesToErrors = validateWithMatcher(workspace, null);
		String wsName = workspace.getName();
		if (resourcesToErrors.isEmpty()) {
			FacesMessage fm = new FacesMessage(SEVERITY_INFO, "Workspace " + wsName + " is valid.", null);
			FacesContext.getCurrentInstance().addMessage(null, fm);
		}
		else {
			FacesMessage fm = new FacesMessage(SEVERITY_ERROR,
					"Workspace " + wsName + " is not valid. The files with errors are" + writeErrors(resourcesToErrors),
					null);
			FacesContext.getCurrentInstance().addMessage(null, fm);
		}
	}

	private Map<String, List<String>> validateWithMatcher(DeegreeWorkspace ws, PathMatcher pathMatcher) {
		Workspace newWorkspace = ws.getNewWorkspace();
		ErrorHandler errorHandler = newWorkspace.getErrorHandler();
		return collectErrors(ws, newWorkspace, pathMatcher, errorHandler);
	}

	private Map<String, java.util.List<String>> collectErrors(DeegreeWorkspace ws, Workspace newWorkspace,
			PathMatcher pathMatcher, ErrorHandler errorHandler) {
		Map<String, java.util.List<String>> resourceToErrors = new TreeMap<>();
		if (errorHandler.hasErrors()) {
			java.util.List<ResourceManager<? extends Resource>> resourceManagers = newWorkspace.getResourceManagers();
			for (ResourceManager<? extends Resource> resourceManager : resourceManagers) {
				Collection<? extends ResourceMetadata<? extends Resource>> resourceMetadata = resourceManager
					.getResourceMetadata();
				for (ResourceMetadata<? extends Resource> rm : resourceMetadata) {
					collectErrors(ws, rm, pathMatcher, errorHandler, resourceToErrors);
				}
			}
		}
		return resourceToErrors;
	}

	private Map<String, java.util.List<String>> collectErrors(DeegreeWorkspace ws,
			ResourceMetadata<? extends Resource> rm, PathMatcher pathMatcher, ErrorHandler errorHandler,
			Map<String, java.util.List<String>> resourceToErrors) {
		File resourceLocation = rm.getLocation().getAsFile();
		if (resourceLocation != null) {
			ResourceIdentifier<? extends Resource> identifier = rm.getIdentifier();
			java.util.List<String> errors = errorHandler.getErrors(identifier);
			if (isResourceRequestedAndHasErrors(pathMatcher, resourceLocation, errors)) {
				String id = retrieveIdentifierWithPath(ws, rm, resourceLocation);
				resourceToErrors.put(id, errors);
			}
		}
		else {
			LOG.warn("Validation of resources without file location is not implemented yet.");
		}
		return resourceToErrors;
	}

	private String retrieveIdentifierWithPath(DeegreeWorkspace ws, ResourceMetadata<? extends Resource> rm,
			File resourceLocation) {
		File wsLocation = ws.getLocation();
		URI identifierWithPath = wsLocation.toURI().relativize(resourceLocation.toURI());
		return identifierWithPath.toString();
	}

	private boolean isResourceRequestedAndHasErrors(PathMatcher pathMatcher, File resourceLocation,
			java.util.List<String> errors) {
		return !errors.isEmpty() && resourceLocation != null
				&& (pathMatcher == null || pathMatcher.matches(resourceLocation.toPath()));
	}

	private String writeErrors(Map<String, List<String>> resourcesToErrors) {
		StringBuffer buffer = new StringBuffer();
		for (Map.Entry<String, java.util.List<String>> resourceToErrors : resourcesToErrors.entrySet()) {
			buffer.append("<br/>");
			buffer.append(resourceToErrors.getKey() + ":\n");
			for (String error : resourceToErrors.getValue()) {
				buffer.append("   - " + error + "<br/>");
			}
		}
		return buffer.toString();
	}

}
