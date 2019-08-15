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

    private final int allowedCycleDepth;

    private final List<QName> path = new ArrayList<>();

    private final List<XSElementDeclaration> parentEls = new ArrayList<>();

    private final List<XSComplexTypeDefinition> parentCTs = new ArrayList<>();

    CycleAnalyser( int allowedCycleDepth ) {
        this.allowedCycleDepth = allowedCycleDepth;
    }

    public boolean checkStopAtCycle( XSComplexTypeDefinition typeDef ) {
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

    public void start( PropertyType pt ) {
        path.add( pt.getName() );
    }

    public void stop( PropertyType pt ) {
        path.clear();
        parentEls.clear();
        parentCTs.clear();
    }

    public void add( XSComplexTypeDefinition typeDef ) {
        parentCTs.add( typeDef );
        path.add( getQName( typeDef ) );
    }

    public void add( XSElementDeclaration elDecl ) {
        parentEls.add( elDecl );
        path.add( getQName( elDecl ) );
    }

    public void remove( XSComplexTypeDefinition typeDef ) {
        if ( isLast( parentCTs, typeDef ) )
            parentCTs.remove( typeDef );
        QName qName = getQName( typeDef );
        if ( isLast( path, qName ) )
            path.remove( qName );
    }

    public void remove( XSElementDeclaration elDecl ) {
        if ( isLast( parentEls, elDecl ) )
            parentEls.remove( elDecl );
        QName qName = getQName( elDecl );
        if ( isLast( path, qName ) ) {
            path.remove( qName );
        }
    }

    public List<XSElementDeclaration> getParentEls() {
        return parentEls;
    }

    public void log() {
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
        if ( !xsType.getAnonymous() ) {
            return new QName( xsType.getNamespace(), xsType.getName() );
        }
        return null;
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
                        //logCycle( parentCTs );
                        LOG.info( "Found cycle at " + getQName( typeDef ) );
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void logCycle( List<XSComplexTypeDefinition> parentCTs ) {
        StringBuffer sb = new StringBuffer( "Path: " );
        for ( XSComplexTypeDefinition qName : parentCTs ) {
            sb.append( "{" );
            sb.append( qName.getNamespace() );
            sb.append( "}" );
            sb.append( qName.getName() );
            sb.append( " -> " );
        }
        LOG.info( "Found cycle '" + sb + "'." );
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