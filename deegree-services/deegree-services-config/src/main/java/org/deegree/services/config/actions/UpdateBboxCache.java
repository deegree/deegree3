package org.deegree.services.config.actions;

import static org.apache.commons.io.IOUtils.write;
import static org.deegree.services.config.actions.Utils.getWorkspaceAndPath;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.types.FeatureType;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;

/**
 * Action to update the bboxes of feature stores.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class UpdateBboxCache {

	private static final Logger LOG = getLogger(UpdateBboxCache.class);

	public static final String FEATURESTOREID = "FEATURESTOREID";

	/**
	 * Updates the bounding boxes of all feature types in all feature stores.
	 * @param path identifying the resource to validate, never <code>null</code>
	 * @param queryString
	 * @param response the response to write the validation result in, never
	 * <code>null</code>
	 * @throws IOException if the OutputStream of the response could not be requested
	 */
	public static void updateBboxCache(String path, String queryString, HttpServletResponse response)
			throws IOException {

		DeegreeWorkspace ws = getWorkspaceAndPath(path).first;

		try {
			ws.initAll();
		}
		catch (ResourceInitException e) {
			response.setStatus(500);
			response.setContentType("text/plain");
			write("Error while validating: " + e.getLocalizedMessage() + "\n", response.getOutputStream());
			return;
		}
		try {
			List<String> featureStoreIds = parseFeatureStoreIds(queryString);
			updateBboxCache(ws.getNewWorkspace(), featureStoreIds, response);
		}
		catch (Exception e) {
			response.setStatus(400);
			write("Error while processing request: " + e.getLocalizedMessage() + "\n", response.getOutputStream());
		}
	}

	private static void updateBboxCache(Workspace workspace, List<String> featureStoreIds, HttpServletResponse response)
			throws IOException {
		List<String> featureStoreIdsToUpdate = findFeatureStoreIdsToUpdate(featureStoreIds, workspace);
		UpdateLog updateLog = new UpdateLog();
		for (String featureStoreId : featureStoreIdsToUpdate) {
			updateCacheOfFeatureStore(workspace, featureStoreId, updateLog);
		}
		response.setContentType("text/plain");
		updateLog.logResult(response.getOutputStream());
	}

	private static void updateCacheOfFeatureStore(Workspace workspace, String featureStoreId, UpdateLog updateLog) {
		FeatureStore featureStore = workspace.getResource(FeatureStoreProvider.class, featureStoreId);
		if (featureStore == null)
			throw new IllegalArgumentException("FeatureStore with ID " + featureStoreId + " does not exist");
		FeatureType[] featureTypes = featureStore.getSchema().getFeatureTypes();
		for (FeatureType featureType : featureTypes) {
			QName featureTypeName = featureType.getName();
			try {
				featureStore.calcEnvelope(featureTypeName);
				updateLog.addSucceed(featureStoreId, featureTypeName);
			}
			catch (FeatureStoreException e) {
				updateLog.addFailed(featureStoreId, featureTypeName);
				LOG.debug("Update of FeatureType " + featureTypeName + ", from FeatureStore with ID " + featureStoreId
						+ " failed", e);
			}
		}
	}

	private static List<String> findFeatureStoreIdsToUpdate(List<String> featureStoreIds, Workspace workspace) {
		if (!featureStoreIds.isEmpty())
			return featureStoreIds;
		List<String> allFeatureStoreIds = new ArrayList<>();
		for (ResourceIdentifier<FeatureStore> resourceIdentifier : workspace
			.getResourcesOfType(FeatureStoreProvider.class)) {
			String featureStoreId = resourceIdentifier.getId();
			allFeatureStoreIds.add(featureStoreId);
		}
		return allFeatureStoreIds;
	}

	private static List<String> parseFeatureStoreIds(String queryString) throws UnsupportedEncodingException {
		if (queryString == null)
			return Collections.emptyList();
		Map<String, String> normalizedKVPMap = KVPUtils.getNormalizedKVPMap(queryString, null);
		String featureStoreId = normalizedKVPMap.get(FEATURESTOREID);
		if (featureStoreId == null)
			return Collections.emptyList();
		return Arrays.asList(KVPUtils.splitList(featureStoreId));
	}

	static class UpdateLog {

		Map<String, FailedAndSucceed> resultsPerFeatureStore = new HashMap<>();

		void addFailed(String featureStore, QName featureType) {
			if (!resultsPerFeatureStore.containsKey(featureStore))
				resultsPerFeatureStore.put(featureStore, new FailedAndSucceed());
			resultsPerFeatureStore.get(featureStore).addFailed(featureType);
		}

		void addSucceed(String featureStore, QName featureType) {
			if (!resultsPerFeatureStore.containsKey(featureStore))
				resultsPerFeatureStore.put(featureStore, new FailedAndSucceed());
			resultsPerFeatureStore.get(featureStore).addSucceed(featureType);
		}

		void logResult(ServletOutputStream outputStream) throws IOException {
			StringBuilder sb = new StringBuilder();
			sb.append("Update of bbox cache finished: \n\n");
			for (Map.Entry<String, FailedAndSucceed> resultPerFeatureStore : resultsPerFeatureStore.entrySet()) {
				sb.append("FeatureStoreId: ").append(resultPerFeatureStore.getKey()).append("\n");
				List<QName> succeed = resultPerFeatureStore.getValue().succeed;
				sb.append("  *  ").append(succeed.size()).append(" feature types successful succeed: \n");
				for (QName featureType : succeed)
					sb.append("    -  ").append(featureType).append("\n");
				List<QName> failed = resultPerFeatureStore.getValue().failed;
				sb.append("  *  ").append(failed.size()).append(" feature types failed: \n");
				for (QName featureType : failed)
					sb.append("    -  ").append(featureType).append("\n");
				sb.append("\n");
			}
			IOUtils.write(sb.toString(), outputStream);
		}

		static class FailedAndSucceed {

			List<QName> failed = new ArrayList<>();

			List<QName> succeed = new ArrayList<>();

			void addFailed(QName featureType) {
				failed.add(featureType);
			}

			void addSucceed(QName featureType) {
				succeed.add(featureType);
			}

		}

	}

}