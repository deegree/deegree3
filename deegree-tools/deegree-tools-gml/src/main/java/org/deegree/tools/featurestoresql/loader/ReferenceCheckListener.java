/*-
 * #%L
 * deegree-cli-utility
 * %%
 * Copyright (C) 2016 - 2023 lat/lon GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.deegree.tools.featurestoresql.loader;

import org.slf4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public abstract class ReferenceCheckListener {

	private static final Logger LOG = getLogger(ReferenceCheckListener.class);

	final Summary summary;

	ReferenceCheckListener(Summary summary) {
		this.summary = summary;
	}

	ExitStatus checkReferencesAndHandleResult(StepExecution stepExecution) {
		FeatureReferenceCheckResult featureReferenceCheckResult = checkReferences(stepExecution);
		if (featureReferenceCheckResult.isValid()) {
			return handleValidReferences(stepExecution);
		}
		else {
			summary.setUnresolvableReferences(featureReferenceCheckResult.getUnresolvableReferences());
			logResult(featureReferenceCheckResult);
			handleInvalidReferences();
			return new ExitStatus("FAILED", "Unresolvable References!");
		}
	}

	ExitStatus handleValidReferences(StepExecution stepExecution) {
		return stepExecution.getExitStatus();
	}

	void handleInvalidReferences() {
	}

	private FeatureReferenceCheckResult checkReferences(StepExecution stepExecution) {
		List<String> featureIds = (List<String>) stepExecution.getExecutionContext()
			.get(FeatureReferencesParser.FEATURE_IDS);
		List<String> referenceIds = (List<String>) stepExecution.getExecutionContext()
			.get(FeatureReferencesParser.REFERENCE_IDS);
		if (featureIds == null || referenceIds == null) {
			LOG.warn("The reference check is skipped during this operation");
			return new FeatureReferenceCheckResult(emptySet());
		}
		FeatureReferenceChecker featureReferenceChecker = new FeatureReferenceChecker();
		return featureReferenceChecker.checkReferences(featureIds, referenceIds);
	}

	private void logResult(FeatureReferenceCheckResult featureReferenceCheckResult) {
		Set<String> unresolvableReferences = featureReferenceCheckResult.getUnresolvableReferences();
		LOG.info("Unresolvable references detected:");
		for (String unresolvableReference : unresolvableReferences)
			LOG.info("   - {}", unresolvableReference);
	}

}
