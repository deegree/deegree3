/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2022 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.gml.reference;

import static org.deegree.gml.GMLInputFactory.createGMLStreamReader;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.GMLReference;
import org.deegree.commons.tom.gml.GMLReferenceResolver;
import org.deegree.feature.types.AppSchema;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.reference.matcher.ReferencePatternMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps track of {@link GMLObject}s inside GML instance documents, their ids and local xlink references during the
 * parsing of GML documents.
 * <p>
 * Can be used for resolving local xlink-references at the end of the parsing process of a GML instance document or to
 * access all encountered objects on any level of the document.
 * </p>
 *
 * @see GMLReferenceResolver
 * @see GMLStreamReader
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
public class GmlDocumentIdContext implements GMLReferenceResolver {

    private static final Logger LOG = LoggerFactory.getLogger( GmlDocumentIdContext.class );

    private final GMLVersion version;

    private final Map<String, GMLObject> idToObject = new HashMap<String, GMLObject>();

    private final List<GMLReference<?>> refs = new ArrayList<GMLReference<?>>();

    private final List<GMLReference<?>> localRefs = new ArrayList<GMLReference<?>>();

    private AppSchema schema;

    private ReferencePatternMatcher referencePatternMatcher;

    private boolean skipUrnWarning = false;

    /**
     * Creates a new {@link GmlDocumentIdContext} instance for a GML document of the given version.
     *
     * @param version
     *            GML version, must not be <code>null</code>
     */
    public GmlDocumentIdContext( GMLVersion version ) {
        this.version = version;
    }

    /**
     * Sets the application schema (necessary for {@link #getObject(String, String)}.
     *
     * @param schema
     *            application schema to use for parsing external references
     */
    public void setApplicationSchema( AppSchema schema ) {
        this.schema = schema;
    }

    /**
     * Adds a new {@link GMLObject} that has been encountered during the parsing of the GML document.
     *
     * @param object
     *            GML object, must not be <code>null</code> and must not be of type {@link GMLReference}
     */
    public void addObject( GMLObject object ) {
        String id = object.getId();
        if ( id != null && id.length() > 0 ) {
            idToObject.put( object.getId(), object );
        }
    }

    /**
     * Adds a new {@link GMLReference} that has been encountered during the parsing of the GML document.
     *
     * @param ref
     *            GML reference, must not be <code>null</code>
     */
    public void addReference( GMLReference<?> ref ) {
        refs.add( ref );
        if ( ref.getURI().startsWith( "#" ) ) {
            localRefs.add( ref );
        }
    }

    /**
     * Adds a {@link ReferencePatternMatcher} that checks if a url should be skipped or not.
     *
     * @param referencePatternMatcher the matcher to add, may be <code>null</code> (all urls are resolved)
     */
    public void setReferencePatternMatcher( ReferencePatternMatcher referencePatternMatcher ) {
        this.referencePatternMatcher = referencePatternMatcher;

        // NOTE allow to suppress warnings regarding urn: (only if explicitly requested)
        this.skipUrnWarning = uriShouldBeSkipped( "urn:" );
    }

    /**
     * Returns the {@link GMLObject} with the specified id.
     *
     * @param id
     *            id of the object to be returned
     * @return the object, or <code>null</code> if it has not been added before
     */
    public GMLObject getObject( String id ) {
        return idToObject.get( id );
    }

    /**
     * Returns all {@link GMLObject} (but no {@link GMLReference} instances) that have been added.
     *
     * @return all gml objects that have been added before, may be empty, but never <code>null</code>
     */
    public Map<String, GMLObject> getObjects() {
        return idToObject;
    }

    /**
     * Return all {@link GMLReference} instances that have been added.
     *
     * @return all gml references that have been added before, may be empty, but never <code>null</code>
     */
    public List<GMLReference<?>> getReferences() {
        return refs;
    }

    @Override
    public GMLObject getObject( String uri, String baseURL ) {
        if ( uri.startsWith( "#" ) ) {
            return idToObject.get( uri.substring( 1 ) );
        } else if ( uri.startsWith( "urn:" ) ) {
            if ( !skipUrnWarning ) {
                LOG.warn( "Unable to resolve external object reference: {}. Resolving of urn references is not implemented yet.",
                          uri );
            }
        } else if ( !uriShouldBeSkipped( uri ) ) {
            return fetchExternalGmlObject( uri, baseURL );
        } else {
            LOG.info( "URL {} is configured to be skipped", uri );
        }
        return null;
    }

    private GMLObject fetchExternalGmlObject( String uri, String baseURL ) {
        GMLObject object = null;
        try {
            URL resolvedURL = null;
            if ( baseURL != null ) {
                resolvedURL = new URL( new URL( baseURL ), uri );
            } else {
                resolvedURL = new URL( uri );
            }
            GMLStreamReader gmlReader = createGMLStreamReader( version, resolvedURL );
            gmlReader.setApplicationSchema( schema );
            object = gmlReader.read();
            gmlReader.close();
            LOG.debug( "Read GML object: id='{}'", object.getId() );
        } catch ( Throwable e ) {
            String msg = "Unable to resolve external object reference to '" + uri + "': " + e.getMessage();
            throw new ReferenceResolvingException( msg );
        }
        return object;
    }

    /**
     * Resolves all local references that have been added before against the added objects.
     *
     * @throws ReferenceResolvingException
     *             if a local reference cannot be resolved
     */
    public void resolveLocalRefs()
                            throws ReferenceResolvingException {

        for ( GMLReference<?> ref : localRefs ) {
            String id = ref.getURI().substring( 1 );
            LOG.debug( "Resolving reference to object '{}'", id );
            if ( ref.getReferencedObject() == null ) {
                String msg = "Cannot resolve reference to object with id '" + id
                             + "'. There is no object with this id in the document.";
                throw new ReferenceResolvingException( msg );
            }
        }
    }

    private boolean uriShouldBeSkipped( String uri ) {
        if ( referencePatternMatcher != null )
            return referencePatternMatcher.isMatching( uri );
        return false;
    }

}