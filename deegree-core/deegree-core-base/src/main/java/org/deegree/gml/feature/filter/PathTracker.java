package org.deegree.gml.feature.filter;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class PathTracker {

    private final List<QName> currentPath = new ArrayList<>();

    public void startFeatureStep( QName featureName ) {
        if ( !currentPath.isEmpty() )
            this.currentPath.add( featureName );
    }

    public void startStep( QName propertyName ) {
        this.currentPath.add( propertyName );
    }

    public void stopStep( QName propertyName ) {
        if ( currentPath.size() > 0 && propertyName.equals( currentPath.get( currentPath.size() - 1 ) ) ) {
            this.currentPath.remove( currentPath.size() - 1 );
        }
    }

    public void reset() {
        this.currentPath.clear();
    }

    public List<QName> getCurrentPath() {
        return currentPath;
    }

    public QName firstStep() {
        if ( currentPath.isEmpty() )
            return null;
        return currentPath.get( 0 );
    }
}