/*-
 * #%L
 * deegree-cli-utility
 * %%
 * Copyright (C) 2016 - 2021 lat/lon GmbH
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

import java.util.Set;

/**
 * Encapsulates the result of the {@link FeatureReferenceChecker}
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeatureReferenceCheckResult {

    private final Set<String> unresolvableReferences;

    /**
     * @param unresolvableReferences
     *            a list of unresolvable reference, may be empty but never <code>null</code>
     */
    public FeatureReferenceCheckResult( Set<String> unresolvableReferences ) {
        this.unresolvableReferences = unresolvableReferences;
    }

    /**
     * @return <code>true</code> if the {@link FeatureReferenceChecker} did not found any unresolvable references,
     *         <code>false</code> otherwise
     */
    public boolean isValid() {
        return unresolvableReferences.size() == 0;
    }

    /**
     * @return the detected unresolvable references, may be empty but never <code>null</code>
     */
    public Set<String> getUnresolvableReferences() {
        return unresolvableReferences;
    }

}