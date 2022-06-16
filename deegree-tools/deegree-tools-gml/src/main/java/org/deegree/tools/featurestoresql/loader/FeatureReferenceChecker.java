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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Checks if all references can be resolved.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeatureReferenceChecker {

    /**
     * Checks if for all references a featureId exists.
     * 
     * @param featureIds
     *            list of featureIds, may be empty but never <code>null</code>
     * @param references
     *            list of references, may be empty but never <code>null</code>
     * @return the result of the check, never <code>null</code>
     */
    public FeatureReferenceCheckResult checkReferences( List<String> featureIds, List<String> references ) {
        Set<String> unresolvableReferences = collectUnresolvableReferences( ensureNotNull( featureIds ),
                                                                             ensureNotNull( references ) );
        return new FeatureReferenceCheckResult( unresolvableReferences );
    }

    private Set<String> collectUnresolvableReferences( List<String> featureIds, List<String> references ) {
        Set<String> unresolvableReferences = new HashSet<>( );
        if ( featureIds != null && references != null ) {
            for ( String reference : references ) {
                String referenceId = parseReference( reference );
                if ( !featureIds.contains( referenceId ) )
                    unresolvableReferences.add( referenceId );
            }
        }
        return unresolvableReferences;
    }

    private String parseReference( String reference ) {
        if ( reference.startsWith( "#" ) )
            return reference.substring( 1 );
        return reference;
    }

    private List<String> ensureNotNull( List<String> list ) {
        return list == null ? Collections.emptyList() : list;
    }

}
