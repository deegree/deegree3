package org.deegree.feature.persistence.sql.mapper;

import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSTypeDefinition;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CycleAnalyser {

    private static final Logger LOG = LoggerFactory.getLogger( CycleAnalyser.class );

    private final List<QName> path = new ArrayList<>();

    private final List<XSElementDeclaration> parentEls = new ArrayList<>();

    private final List<XSComplexTypeDefinition> parentCTs = new ArrayList<>();

    private final int allowedCycleDepth;

    private final QName featureTypeName;

    /**
     * @param allowedCycleDepth
     *                         the allowed depth of cycles
     * @param featureTypeName name of the feature type, never <code>null</code>
     */
    CycleAnalyser( int allowedCycleDepth, QName featureTypeName ) {
        this.allowedCycleDepth = allowedCycleDepth;
        this.featureTypeName = featureTypeName;
    }

    /**
     * Checks if a cycle occurs and the allowed cycle depth is reached.
     *
     * @param typeDef
     *                         the type definition to check, never <code>null</code>
     * @return <code>true</code> if a cycle was found and the allowed cycle depth reached, <code>false</code> otherwise
     */
    public boolean checkStopAtCycle( XSComplexTypeDefinition typeDef ) {
        if ( typeDef.getAnonymous() ) {
            LOG.info( "Anonymous type definition found, will be ignored for cycle detection." );
            return false;
        }
        log();
        boolean isCycle = isCycle( typeDef );
        if ( isCycle ) {
            if ( stop( getQName( typeDef ) ) ) {
                LOG.info( "Allowed cycle depth of " + allowedCycleDepth
                          + " reached. Mapping will stop at this cycle." );
                return true;
            }
        }
        add( typeDef );
        return false;
    }

    /**
     * Start with a new path with a property.
     *
     * @param pt
     *                         never <code>null</code>
     */
    public void start( PropertyType pt ) {
        path.add( pt.getName() );
    }

    /**
     * Stops the current path.
     */
    public void stop() {
        path.clear();
        parentEls.clear();
        parentCTs.clear();
    }

    /**
     * Adds a new step.
     *
     * @param typeDef
     *                         step to add, never <code>null</code>
     */
    public void add( XSComplexTypeDefinition typeDef ) {
        if ( typeDef.getAnonymous() )
            return;
        parentCTs.add( typeDef );
        path.add( getQName( typeDef ) );
    }

    /**
     * Adds a new step.
     *
     * @param elDecl
     *                         step to add, never <code>null</code>
     */
    public void add( XSElementDeclaration elDecl ) {
        parentEls.add( elDecl );
        path.add( getQName( elDecl ) );
    }

    /**
     * Removes the last step if matching.
     *
     * @param typeDef
     *                         step to remove, never <code>null</code>
     */
    public void remove( XSComplexTypeDefinition typeDef ) {
        if ( typeDef.getAnonymous() )
            return;
        if ( isLast( parentCTs, typeDef ) )
            parentCTs.remove( parentCTs.size() - 1 );
        QName qName = getQName( typeDef );
        if ( isLast( path, qName ) )
            path.remove( path.size() - 1 );
    }

    /**
     * Removes the last step if matching.
     *
     * @param elDecl
     *                         step to remove, never <code>null</code>
     */
    public void remove( XSElementDeclaration elDecl ) {
        if ( isLast( parentEls, elDecl ) )
            parentEls.remove( parentEls.size() - 1 );
        QName qName = getQName( elDecl );
        if ( isLast( path, qName ) )
            path.remove( path.size() - 1 );
    }

    /**
     * @return the current element declarations of the path, may be empty but never <code>null</code>
     */
    public List<XSElementDeclaration> getElementDeclarations() {
        return parentEls;
    }

    /**
     * @return the name of the feature type, never <code>null</code>
     */
    public QName getFeatureTypeName() {
        return featureTypeName;
    }

    /**
     * @return the current path. May be empty but never <code>null</code>
     */
    public List<QName> getPath() {
        return path;
    }

    private void log() {
        StringBuffer sb = new StringBuffer();
        Map<QName, Integer> nameToCycleDepth = new HashMap<>();
        for ( QName step : path ) {
            sb.append( "\n      -> " );
            if ( nameToCycleDepth.containsKey( step ) )
                nameToCycleDepth.put( step, ( nameToCycleDepth.get( step ) + 1 ) );
            else
                nameToCycleDepth.put( step, 0 );
            sb.append( step );
            sb.append( " (cycle depth: " ).append( nameToCycleDepth.get( step ) ).append( ")" );
        }
        LOG.info( "Current path:" + sb.toString() );
    }

    private QName getQName( XSTypeDefinition xsType ) {
        if ( xsType.getAnonymous() )
            return null;
        return new QName( xsType.getNamespace(), xsType.getName() );
    }

    private QName getQName( XSElementDeclaration elDecl ) {
        return new QName( elDecl.getNamespace(), elDecl.getName() );
    }

    private boolean isCycle( XSComplexTypeDefinition typeDef ) {
        if ( typeDef.getName() != null ) {
            for ( XSComplexTypeDefinition ct : parentCTs ) {
                if ( ct.getName() != null ) {
                    if ( typeDef.getName().equals( ct.getName() ) && typeDef.getNamespace().equals(
                                            ct.getNamespace() ) ) {
                        LOG.info( "Found cycle at " + getQName( typeDef ) );
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean stop( QName qname ) {
        return currentCycleDepth( qname ) > this.allowedCycleDepth;
    }

    private long currentCycleDepth( QName qname ) {
        return this.path.stream().filter( e -> qname.equals( e ) ).count();
    }

    private <T> boolean isLast( List<T> list, T entry ) {
        return list.lastIndexOf( entry ) == list.size() - 1;
    }

}