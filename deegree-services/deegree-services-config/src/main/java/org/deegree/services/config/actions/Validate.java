package org.deegree.services.config.actions;

import static org.apache.commons.io.IOUtils.write;
import static org.deegree.services.config.actions.Utils.getWorkspaceAndPath;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.utils.Pair;
import org.deegree.workspace.ErrorHandler;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceManager;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * Action to validate files of a workspace.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class Validate {

	private static final Logger LOG = getLogger(Validate.class);

	/**
	 * @param path identifying the resource to validate, never <code>null</code>
	 * @param resp the reposne to write the validation result in, never <code>null</code>
	 * @throws IOException
	 */
	public static void validate(String path, HttpServletResponse resp) throws IOException {
		Pair<DeegreeWorkspace, String> p = getWorkspaceAndPath(path);

		try {
			DeegreeWorkspace workspace = p.getFirst();
			workspace.destroyAll();
			workspace.initAll();
		}
		catch (ResourceInitException e) {
			setStatusCodeAndContentType(500, resp);
			write("Error while validating: " + e.getLocalizedMessage() + "\n", resp.getOutputStream());
			return;
		}

		try {
			DeegreeWorkspace workspace = p.first;
			String file = p.second;
			if (file == null) {
				validate(workspace, resp);
			}
			else {
				validate(workspace, file, resp);
			}
		}
		catch (IOException e) {
			resp.setStatus(500);
			resp.setContentType("text/plain");
			write("Error while validating: " + e.getLocalizedMessage() + "\n", resp.getOutputStream());
		}
	}

	private static void validate(DeegreeWorkspace ws, String file, HttpServletResponse resp) throws IOException {
		File wsLocation = ws.getLocation();
		File requestedPath = new File(wsLocation, file);
		if (!requestedPath.exists()) {
			setStatusCodeAndContentType(404, resp);
			write("No such file in workspace: " + ws.getName() + " -> " + file + "\n", resp.getOutputStream());
			return;
		}

		PathMatcher pathMatcher = createPatternMatcher(requestedPath);
		Map<String, List<String>> resourcesToErrors = validateWithMatcher(ws, pathMatcher);
		String wsName = ws.getName();
		setStatusCodeAndContentType(200, resp);
		if (resourcesToErrors.isEmpty()) {
			write("Resource " + file + " in workspace " + wsName + " is valid.", resp.getOutputStream());
		}
		else {
			write("Resource " + file + " in workspace " + wsName
					+ " is not valid. The files with errors are shown below.\n", resp.getOutputStream());
			writeErrors(resourcesToErrors, resp);
		}
	}

	private static void validate(DeegreeWorkspace ws, HttpServletResponse resp) throws IOException {
		Map<String, List<String>> resourcesToErrors = validateWithMatcher(ws, null);
		String wsName = ws.getName();
		setStatusCodeAndContentType(200, resp);
		if (resourcesToErrors.isEmpty()) {
			write("Workspace " + wsName + " is valid.", resp.getOutputStream());
		}
		else {
			write("Workspace " + wsName + " is not valid. The files with errors are shown below.\n",
					resp.getOutputStream());
			writeErrors(resourcesToErrors, resp);
		}
	}

	private static Map<String, List<String>> validateWithMatcher(DeegreeWorkspace ws, PathMatcher pathMatcher) {
		Workspace newWorkspace = ws.getNewWorkspace();
		ErrorHandler errorHandler = newWorkspace.getErrorHandler();
		return collectErrors(ws, newWorkspace, pathMatcher, errorHandler);
	}

	private static void writeErrors(Map<String, List<String>> resourcesToErrors, HttpServletResponse resp)
			throws IOException {
		for (Map.Entry<String, java.util.List<String>> resourceToErrors : resourcesToErrors.entrySet()) {
			write("\n", resp.getOutputStream());
			write(resourceToErrors.getKey() + ":\n", resp.getOutputStream());
			for (String error : resourceToErrors.getValue()) {
				write("   - " + error + "\n", resp.getOutputStream());
			}
		}
	}

	private static Map<String, java.util.List<String>> collectErrors(DeegreeWorkspace ws, Workspace newWorkspace,
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

	private static Map<String, java.util.List<String>> collectErrors(DeegreeWorkspace ws,
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

	private static boolean isResourceRequestedAndHasErrors(PathMatcher pathMatcher, File resourceLocation,
			java.util.List<String> errors) {
		return !errors.isEmpty() && resourceLocation != null
				&& (pathMatcher == null || pathMatcher.matches(resourceLocation.toPath()));
	}

	private static PathMatcher createPatternMatcher(File requestedPath) {
		String pattern = requestedPath.toString();
		FileSystem fileSystem = FileSystems.getDefault();
		if (requestedPath.isDirectory())
			pattern = pattern + fileSystem.getSeparator() + "*";
		pattern = pattern.replace("\\", "\\\\");
		return fileSystem.getPathMatcher("glob:" + pattern);
	}

	private static String retrieveIdentifierWithPath(DeegreeWorkspace ws, ResourceMetadata<? extends Resource> rm,
			File resourceLocation) {
		File wsLocation = ws.getLocation();
		URI identifierWithPath = wsLocation.toURI().relativize(resourceLocation.toURI());
		return identifierWithPath.toString();
	}

	private static void setStatusCodeAndContentType(int statusCode, HttpServletResponse resp) {
		resp.setStatus(statusCode);
		resp.setContentType("text/plain");
	}

}