package org.deegree.feature.persistence.transaction;

import static org.deegree.protocol.wfs.transaction.action.UpdateAction.REMOVE;
import static org.deegree.protocol.wfs.transaction.action.UpdateAction.REPLACE;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.property.GenericProperty;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.Surface;
import org.deegree.protocol.wfs.transaction.action.ParsedPropertyReplacement;
import org.deegree.protocol.wfs.transaction.action.UpdateAction;

public class FeatureUpdater {

    /**
     * Updates the given {@link Feature} instance.
     *
     * TODO Use a copy of the original feature to avoid modifications on rollback. Difficult part: Consider updating of
     * references.
     *
     * @param feature
     *            feature to be updated, must not be <code>null</code>
     * @param replacementProps
     *            properties to be replaced, must not be <code>null</code>
     */
    public void update( final Feature feature, final List<ParsedPropertyReplacement> replacementProps )
                            throws FeatureStoreException {

        for ( ParsedPropertyReplacement replacement : replacementProps ) {
            Property prop = replacement.getNewValue();
            UpdateAction updateAction = replacement.getUpdateAction();
            GenericProperty newProp = new GenericProperty( prop.getType(), null );
            if ( prop.getValue() != null ) {
                newProp.setValue( prop.getValue() );
            } else if ( !prop.getChildren().isEmpty() && prop.getChildren().get( 0 ) != null ) {
                newProp.setChildren( prop.getChildren() );
            } else if ( updateAction == null ) {
                updateAction = REMOVE;
            }

            if ( updateAction == null ) {
                updateAction = REPLACE;
            }

            int idx = replacement.getIndex();
            switch ( updateAction ) {
            case INSERT_AFTER:
                List<Property> ps = feature.getProperties();
                ListIterator<Property> iter = ps.listIterator();
                while ( iter.hasNext() ) {
                    if ( iter.next().getType().getName().equals( prop.getType().getName() ) ) {
                        --idx;
                    }
                    if ( idx < 0 ) {
                        iter.add( newProp );
                        break;
                    }
                }
                break;
            case INSERT_BEFORE:
                ps = feature.getProperties();
                iter = ps.listIterator();
                while ( iter.hasNext() ) {
                    if ( iter.next().getType().getName().equals( prop.getType().getName() ) ) {
                        --idx;
                    }
                    if ( idx == 0 ) {
                        iter.add( newProp );
                        break;
                    }
                }
                break;
            case REMOVE:
                ps = feature.getProperties();
                iter = ps.listIterator();
                while ( iter.hasNext() ) {
                    if ( iter.next().getType().getName().equals( prop.getType().getName() ) ) {
                        iter.remove();
                    }
                }
                break;
            case REPLACE:
                ps = feature.getProperties();
                iter = ps.listIterator();
                while ( iter.hasNext() ) {
                    if ( iter.next().getType().getName().equals( prop.getType().getName() ) ) {
                        --idx;
                    }
                    if ( idx < 0 ) {
                        iter.set( newProp );
                        break;
                    }
                }
                break;
            }
            validateProperties( feature, feature.getProperties() );
        }
    }

    private void validateProperties( Feature feature, List<Property> props ) {
        Map<PropertyType, Integer> ptToCount = new HashMap<PropertyType, Integer>();
        for ( Property prop : props ) {

            if ( prop.getValue() instanceof Geometry ) {
                Geometry geom = (Geometry) prop.getValue();
                if ( geom != null ) {
                    Property current = feature.getProperties( prop.getType().getName() ).get( 0 );
                    Geometry currentGeom = current != null ? ( (Geometry) current.getValue() ) : null;
                    // check compatibility (CRS) for geometry replacements (CITE
                    // wfs:wfs-1.1.0-Transaction-tc7.2)
                    if ( currentGeom != null && currentGeom.getCoordinateDimension() != geom.getCoordinateDimension() ) {
                        String msg = "Cannot replace given geometry property '" + prop.getType().getName()
                                     + "' with given value (wrong dimension).";
                        throw new InvalidParameterValueException( msg );
                    }
                    // check compatibility (geometry type) for geometry replacements (CITE
                    // wfs:wfs-1.1.0-Transaction-tc10.1)
                    QName qname = new QName( "http://cite.opengeospatial.org/gmlsf", "surfaceProperty" );
                    if ( !( geom instanceof Surface ) && prop.getType().getName().equals( qname ) ) {
                        String msg = "Cannot replace given geometry property '" + prop.getType().getName()
                                     + "' with given value (wrong type).";
                        throw new InvalidParameterValueException( msg );
                    }
                }
            }

            Integer count = ptToCount.get( prop.getType() );
            if ( count == null ) {
                count = 1;
            } else {
                count++;
            }
            ptToCount.put( prop.getType(), count );
        }
        for ( PropertyType pt : feature.getType().getPropertyDeclarations() ) {
            int count = ptToCount.get( pt ) == null ? 0 : ptToCount.get( pt );
            if ( count < pt.getMinOccurs() ) {
                String msg = "Update would result in invalid feature: property '" + pt.getName()
                             + "' must be present at least " + pt.getMinOccurs() + " time(s).";
                throw new InvalidParameterValueException( msg );
            } else if ( pt.getMaxOccurs() != -1 && count > pt.getMaxOccurs() ) {
                String msg = "Update would result in invalid feature: property '" + pt.getName()
                             + "' must be present no more than " + pt.getMaxOccurs() + " time(s).";
                throw new InvalidParameterValueException( msg );
            }
        }
    }

}
