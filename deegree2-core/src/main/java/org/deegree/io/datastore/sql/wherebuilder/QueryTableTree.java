//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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

 e-mail: info@deegree.org
----------------------------------------------------------------------------*/
package org.deegree.io.datastore.sql.wherebuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.io.datastore.schema.MappedFeaturePropertyType;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGMLId;
import org.deegree.io.datastore.schema.MappedGMLSchema;
import org.deegree.io.datastore.schema.MappedGeometryPropertyType;
import org.deegree.io.datastore.schema.MappedPropertyType;
import org.deegree.io.datastore.schema.MappedSimplePropertyType;
import org.deegree.io.datastore.schema.TableRelation;
import org.deegree.io.datastore.schema.content.ConstantContent;
import org.deegree.io.datastore.schema.content.SimpleContent;
import org.deegree.io.datastore.sql.TableAliasGenerator;
import org.deegree.ogcbase.AnyStep;
import org.deegree.ogcbase.AttributeStep;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathStep;

/**
 * Represents selected {@link MappedFeatureType}s and {@link PropertyPath} instances (properties used in an OGC filter
 * and as sort criteria) and their mapping to a certain relational schema.
 * <p>
 * The requested {@link MappedFeatureType}s are the root nodes of the tree. If there is more than root node (feature
 * type), the query requests a join between feature types.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class QueryTableTree {

    private static final ILogger LOG = LoggerFactory.getLogger( QueryTableTree.class );

    private TableAliasGenerator aliasGenerator;

    private FeatureTypeNode[] roots;

    // maps the aliases (specified in the query) to the corresponding FeatureTypeNode
    private Map<String, FeatureTypeNode> ftAliasToRootFtNode = new HashMap<String, FeatureTypeNode>();

    // uses 2 lists instead of Map, because PropertyPath.equals() is overwritten,
    // and identity (==) is needed here (different occurences of "equal" PropertyName
    // in filter must be treated as different PropertyPaths)
    private List<PropertyPath> propertyPaths = new ArrayList<PropertyPath>();

    private List<PropertyNode> propertyNodes = new ArrayList<PropertyNode>();

    /**
     * Creates a new instance of <code>QueryTableTree</code>.
     *
     * @param rootFts
     *            selected feature types, more than one type means that the types are joined
     * @param aliases
     *            aliases for the feature types, may be null (must have same length as rootFts otherwise)
     * @param aliasGenerator
     *            aliasGenerator to be used to generate table aliases, may be null
     */
    public QueryTableTree( MappedFeatureType[] rootFts, String[] aliases, TableAliasGenerator aliasGenerator ) {

        if ( aliasGenerator != null ) {
            this.aliasGenerator = aliasGenerator;
        } else {
            this.aliasGenerator = new TableAliasGenerator();
        }
        this.roots = new FeatureTypeNode[rootFts.length];
        for ( int i = 0; i < rootFts.length; i++ ) {
            FeatureTypeNode rootFtNode = new FeatureTypeNode( rootFts[i], aliases != null ? aliases[i] : null,
                                                              aliasGenerator.generateUniqueAlias() );
            this.roots[i] = rootFtNode;
            if ( aliases != null ) {
                this.ftAliasToRootFtNode.put( aliases[i], rootFtNode );
                LOG.logDebug( "Alias '" + aliases[i] + "' maps to '" + rootFtNode + "'." );
            }
        }
    }

    /**
     * Returns the root feature type nodes of the tree.
     *
     * @return the root feature type nodes of the tree, contains at least one entry
     */
    public FeatureTypeNode[] getRootNodes() {
        return this.roots;
    }

    /**
     * Returns the alias for the root table.
     *
     * TODO support more than one root node
     *
     * @return the alias for the root table
     */
    public String getRootAlias() {
        return this.roots[0].getTableAlias();
    }

    /**
     * Returns the property node for the given property path.
     *
     * @param path
     *            property to be looked up
     * @return the property node for the given property path
     */
    public PropertyNode getPropertyNode( PropertyPath path ) {

        PropertyNode node = null;
        for ( int i = 0; i < this.propertyPaths.size(); i++ ) {
            if ( this.propertyPaths.get( i ) == path ) {
                node = this.propertyNodes.get( i );
                break;
            }
        }
        return node;
    }

    /**
     * Tries to insert the given {@link PropertyPath} as a filter criterion into the tree.
     * <p>
     * The {@link PropertyPath} is validated during insertion.
     *
     * @param property
     *            property to be inserted, has to have at least one step
     * @throws PropertyPathResolvingException
     *             if the path violates the feature type's schema
     */
    public void addFilterProperty( PropertyPath property )
                            throws PropertyPathResolvingException {
        MappedPropertyType pt = validate( property, false );
        if ( pt instanceof MappedSimplePropertyType ) {
            SimpleContent content = ( (MappedSimplePropertyType) pt ).getContent();
            if ( content instanceof ConstantContent ) {
                // add SimplePropertyNode to root node (because table path is irrelevant)
                String[] tableAliases = generateTableAliases( pt );
                PropertyNode propertyNode = new SimplePropertyNode( (MappedSimplePropertyType) pt, roots[0],
                                                                    tableAliases );
                this.propertyPaths.add( property );
                this.propertyNodes.add( propertyNode );
                // root.addPropertyNode( propertyNode );
            } else {
                insertValidatedPath( property );
            }
        } else {
            insertValidatedPath( property );
        }
    }

    /**
     * Tries to insert the given {@link PropertyPath} as a sort criterion into the tree.
     * <p>
     * The {@link PropertyPath} is validated during insertion. It is also checked that the path is unique, i.e. every
     * property type on the path must have maxOccurs set to 1.
     *
     * @param property
     *            property to be inserted, has to have at least one step
     * @throws PropertyPathResolvingException
     *             if the path violates the feature type's schema
     */
    public void addSortProperty( PropertyPath property )
                            throws PropertyPathResolvingException {
        MappedPropertyType pt = validate( property, false );
        if ( pt instanceof MappedSimplePropertyType ) {
            SimpleContent content = ( (MappedSimplePropertyType) pt ).getContent();
            if ( content.isSortable() ) {
                insertValidatedPath( property );
            } else {
                String msg = "Skipping property '" + property + "' as sort criterion.";
                LOG.logDebug( msg );
                // add SimplePropertyNode to root node (because table path is irrelevant)
                String[] tableAliases = generateTableAliases( pt );
                PropertyNode propertyNode = new SimplePropertyNode( (MappedSimplePropertyType) pt, roots[0],
                                                                    tableAliases );
                this.propertyPaths.add( property );
                this.propertyNodes.add( propertyNode );
                // root.addPropertyNode( propertyNode );
            }
        } else {
            String msg = Messages.getMessage( "DATASTORE_PROPERTY_PATH_SORT1", property );
            throw new PropertyPathResolvingException( msg );
        }
    }

    /**
     * Validates the (normalized) {@link PropertyPath} against the {@link MappedGMLSchema} and returns the
     * {@link MappedPropertyType} that the path refers to.
     *
     * @param propertyPath
     *            PropertyPath to be validated, has to have at least one step
     * @param forceUniquePath
     *            if set to true, an exeption is thrown if the path is not unique, i.e. at least one property on the
     *            path has maxOccurs set to a value > 1
     * @return the property type that the path ends with
     * @throws PropertyPathResolvingException
     *             if the path violates the feature type's schema
     */
    private MappedPropertyType validate( PropertyPath propertyPath, boolean forceUniquePath )
                            throws PropertyPathResolvingException {

        MappedPropertyType endingPt = null;
        MappedFeatureType currentFt = null;
        int firstPropertyPos = 1;

        // check if path starts with (valid) alias
        QualifiedName firstStep = propertyPath.getStep( 0 ).getPropertyName();
        LOG.logDebug( "Validating propertyPath.getStep( 0 ).getPropertyName(): " + firstStep );
        if ( firstStep.getLocalName().startsWith( "$" ) ) {
            LOG.logDebug( "The first step is an alias" );
            // path starts with alias
            String ftAlias = firstStep.getLocalName().substring( 1 );
            FeatureTypeNode rootNode = this.ftAliasToRootFtNode.get( ftAlias );
            if ( rootNode == null ) {
                String msg = Messages.getMessage( "DATASTORE_PROPERTY_PATH_RESOLVE6", propertyPath,
                                                  firstStep.getLocalName() );
                throw new PropertyPathResolvingException( msg );
            }
            currentFt = rootNode.getFeatureType();
        } else {
            // path does not start with an alias
            if ( this.roots.length > 1 ) {
                LOG.logDebug( "Multiple (join) feature type request. First step of '" + propertyPath
                              + "' must be the name (or an alias) of a requested feature type then..." );
                QualifiedName ftName = propertyPath.getStep( 0 ).getPropertyName();
                for ( FeatureTypeNode rootNode : this.roots ) {
                    if ( rootNode.getFeatureType().getName().equals( ftName ) ) {
                        currentFt = rootNode.getFeatureType();
                        break;
                    }
                }
                if ( currentFt == null ) {
                    String msg = Messages.getMessage( "DATASTORE_PROPERTY_PATH_RESOLVE5", propertyPath,
                                                      propertyPath.getStep( 0 ) );
                    throw new PropertyPathResolvingException( msg );
                }
            } else {
                currentFt = this.roots[0].getFeatureType();
                LOG.logDebug( "Single feature type request. Trying to validate '" + propertyPath
                              + "' against schema of feature type '" + currentFt.getName() + "'..." );

                QualifiedName elementName = propertyPath.getStep( 0 ).getPropertyName();

                // must be the name of the feature type or the name of a property of the feature
                // type
                if ( elementName.equals( currentFt.getName() ) ) {
                    LOG.logDebug( "First step matches the name of the feature type." );
                } else {
                    LOG.logDebug( "First step does not match the name of the feature type. "
                                  + "Must be the name of a property then." );
                    firstPropertyPos = 0;
                }
            }
        }

        for ( int step = firstPropertyPos; step < propertyPath.getSteps(); step += 2 ) {
            LOG.logDebug( "Looking up property: " + propertyPath.getStep( step ).getPropertyName() );
            endingPt = getPropertyType( currentFt, propertyPath.getStep( step ) );

            if ( endingPt == null ) {
                String msg = Messages.getMessage( "DATASTORE_PROPERTY_PATH_RESOLVE4", propertyPath, step,
                                                  propertyPath.getStep( step ), currentFt.getName(),
                                                  propertyPath.getStep( step ) );
                throw new PropertyPathResolvingException( msg );
            }

            if ( forceUniquePath ) {
                if ( endingPt.getMaxOccurs() != 1 ) {
                    String msg = Messages.getMessage( "DATASTORE_PROPERTY_PATH_SORT2", propertyPath, step,
                                                      endingPt.getName() );
                    throw new PropertyPathResolvingException( msg );
                }
            }

            if ( endingPt instanceof MappedSimplePropertyType ) {
                if ( step < propertyPath.getSteps() - 1 ) {
                    String msg = Messages.getMessage( "DATASTORE_PROPERTY_PATH_RESOLVE1", propertyPath, step,
                                                      endingPt.getName(), "simple" );
                    throw new PropertyPathResolvingException( msg );
                }
            } else if ( endingPt instanceof MappedGeometryPropertyType ) {
                if ( step < propertyPath.getSteps() - 1 ) {
                    String msg = Messages.getMessage( "DATASTORE_PROPERTY_PATH_RESOLVE1", propertyPath, step,
                                                      endingPt.getName(), "geometry" );
                    throw new PropertyPathResolvingException( msg );
                }
            } else if ( endingPt instanceof MappedFeaturePropertyType ) {
                if ( step == propertyPath.getSteps() - 1 ) {
                    String msg = Messages.getMessage( "DATASTORE_PROPERTY_PATH_RESOLVE2", propertyPath, step,
                                                      endingPt.getName() );
                    throw new PropertyPathResolvingException( msg );
                }
                MappedFeaturePropertyType pt = (MappedFeaturePropertyType) endingPt;
                MappedFeatureType[] allowedTypes = pt.getFeatureTypeReference().getFeatureType().getConcreteSubstitutions();
                QualifiedName givenTypeName = propertyPath.getStep( step + 1 ).getPropertyName();

                // check if an alias is used
                if ( givenTypeName.getLocalName().startsWith( "$" ) ) {
                    String ftAlias = givenTypeName.getLocalName().substring( 1 );
                    FeatureTypeNode ftNode = this.ftAliasToRootFtNode.get( ftAlias );
                    if ( ftNode == null ) {
                        String msg = Messages.getMessage( "DATASTORE_PROPERTY_PATH_RESOLVE6", propertyPath, step + 1,
                                                          propertyPath.getStep( step + 1 ) );
                        throw new PropertyPathResolvingException( msg );
                    }
                    givenTypeName = ftNode.getFeatureType().getName();
                }

                MappedFeatureType givenType = null;

                for ( int i = 0; i < allowedTypes.length; i++ ) {
                    if ( allowedTypes[i].getName().equals( givenTypeName ) ) {
                        givenType = allowedTypes[i];
                        break;
                    }
                }
                if ( givenType == null ) {
                    StringBuffer validTypeList = new StringBuffer();
                    for ( int i = 0; i < allowedTypes.length; i++ ) {
                        validTypeList.append( '\'' );
                        validTypeList.append( allowedTypes[i].getName() );
                        validTypeList.append( '\'' );
                        if ( i != allowedTypes.length - 1 ) {
                            validTypeList.append( ", " );
                        }
                    }
                    String msg = Messages.getMessage( "DATASTORE_PROPERTY_PATH_RESOLVE3", propertyPath, step + 1,
                                                      propertyPath.getStep( step + 1 ), validTypeList );
                    throw new PropertyPathResolvingException( msg.toString() );
                }
                currentFt = pt.getFeatureTypeReference().getFeatureType();
            } else {
                assert ( false );
            }
        }
        return endingPt;
    }

    /**
     * Inserts the (already validated!!!) {@link PropertyPath} into the query tree.
     *
     * @see #validate(PropertyPath, boolean)
     *
     * @param propertyPath
     *            validated PropertyPath to be inserted into the tree
     */
    private void insertValidatedPath( PropertyPath propertyPath ) {

        LOG.logDebug( "Inserting '" + propertyPath + "' into the query table tree." );

        FeatureTypeNode ftNode = null;
        int firstPropertyPos = 1;

        // check if path starts with an alias
        QualifiedName firstStep = propertyPath.getStep( 0 ).getPropertyName();
        if ( firstStep.getLocalName().startsWith( "$" ) ) {
            String ftAlias = firstStep.getLocalName().substring( 1 );
            ftNode = this.ftAliasToRootFtNode.get( ftAlias );
        } else {
            if ( this.roots.length > 1 ) {
                QualifiedName ftName = propertyPath.getStep( 0 ).getPropertyName();
                for ( FeatureTypeNode rootNode : this.roots ) {
                    if ( rootNode.getFeatureType().getName().equals( ftName ) ) {
                        ftNode = rootNode;
                        break;
                    }
                }
            } else {
                PropertyPathStep step = propertyPath.getStep( 0 );
                QualifiedName elementName = step.getPropertyName();

                // must be the name of the feature type or the name of a property of the feature type
                if ( elementName.equals( this.roots[0].getFeatureType().getName() ) ) {
                    LOG.logDebug( "First step matches the name of the feature type." );
                } else {
                    LOG.logDebug( "First step does not match the name of the feature type. "
                                  + "Must be the name of a property then." );
                    firstPropertyPos = 0;
                }
                ftNode = this.roots[0];
            }
        }

        PropertyNode propNode = null;
        for ( int i = firstPropertyPos; i < propertyPath.getSteps(); i += 2 ) {

            // check for property with step name in the feature type
            MappedFeatureType currentFt = ftNode.getFeatureType();
            MappedPropertyType pt = getPropertyType( currentFt, propertyPath.getStep( i ) );

            // check if feature type node already has such a property node, add it otherwise
            propNode = ftNode.getPropertyNode( pt );
            if ( propNode == null || propNode.getProperty().getMaxOccurs() != 1 ) {
                propNode = createPropertyNode( ftNode, pt );
                ftNode.addPropertyNode( propNode );
            }

            if ( i + 1 < propertyPath.getSteps() ) {
                // more steps? propNode must be a FeaturePropertyNode then
                assert propNode instanceof FeaturePropertyNode;
                QualifiedName featureStep = propertyPath.getStep( i + 1 ).getPropertyName();
                ftNode = findOrAddSubFtNode( (FeaturePropertyNode) propNode, featureStep );
            }
        }

        this.propertyPaths.add( propertyPath );
        this.propertyNodes.add( propNode );

        // // "equal" path is already registered, map this one to existing instance
        // if ( getPropertyNode( propertyPath ) == null ) {
        // this.propertyPaths.add( propertyPath );
        // this.propertyNodes.add( propNode );
        // }
    }

    private PropertyNode createPropertyNode( FeatureTypeNode ftNode, MappedPropertyType pt ) {

        PropertyNode propNode = null;
        if ( pt instanceof MappedSimplePropertyType ) {
            String[] tableAliases = generateTableAliases( pt );
            propNode = new SimplePropertyNode( (MappedSimplePropertyType) pt, ftNode, tableAliases );
        } else if ( pt instanceof MappedGeometryPropertyType ) {
            String[] tableAliases = generateTableAliases( pt );
            propNode = new GeometryPropertyNode( (MappedGeometryPropertyType) pt, ftNode, tableAliases );
        } else if ( pt instanceof MappedFeaturePropertyType ) {
            String[] tableAliases = this.aliasGenerator.generateUniqueAliases( pt.getTableRelations().length - 1 );
            propNode = new FeaturePropertyNode( ( (MappedFeaturePropertyType) pt ), ftNode, tableAliases );
        } else {
            assert ( false );
        }
        return propNode;
    }

    /**
     * Returns a {@link FeatureTypeNode} that select a child of the given {@link FeaturePropertyNode}.
     * <p>
     * If the step specifies a feature type alias (instead of the feature type name), the corresponding root feature
     * type node is returned.
     *
     * @param propNode
     * @param featureStep
     */
    private FeatureTypeNode findOrAddSubFtNode( FeaturePropertyNode propNode, QualifiedName featureStep ) {

        FeatureTypeNode childNode = null;

        // check if step specifies an alias -> use corresponding root feature node then
        if ( featureStep.getLocalName().startsWith( "$" ) ) {
            String alias = featureStep.getLocalName().substring( 1 );
            FeatureTypeNode[] childNodes = propNode.getFeatureTypeNodes();
            for ( FeatureTypeNode node : childNodes ) {
                if ( alias.equals( node.getFtAlias() ) ) {
                    childNode = node;
                    break;
                }
            }
            if ( childNode == null ) {
                childNode = this.ftAliasToRootFtNode.get( alias );
                propNode.addFeatureTypeNode( childNode );
            }
        } else {
            FeatureTypeNode[] subFtNodes = propNode.getFeatureTypeNodes();
            for ( FeatureTypeNode node : subFtNodes ) {
                if ( node.getFeatureType().getName().equals( featureStep ) ) {
                    childNode = node;
                    break;
                }
            }
            if ( childNode == null ) {
                MappedFeatureType subFt = this.roots[0].getFeatureType().getGMLSchema().getFeatureType( featureStep );
                String tableAlias = this.aliasGenerator.generateUniqueAlias();
                childNode = new FeatureTypeNode( subFt, null, tableAlias );
                propNode.addFeatureTypeNode( childNode );
            }
        }

        return childNode;
    }

    // private FeaturePropertyNode createFeaturePropertyNode( FeatureTypeNode ftNode, MappedFeaturePropertyType pt) {
    //
    // // MappedFeatureType[] allowedTypes = pt.getFeatureTypeReference().getFeatureType().getConcreteSubstitutions();
    // // QualifiedName givenTypeName = propertyPath.getStep( step + 1 ).getPropertyName();
    // // MappedFeatureType givenType = null;
    //
    //
    // }

    // private void addPathFragment( FeatureTypeNode ftNode, PropertyPath propertyPath, int startStep ) {
    //
    // for ( int step = startStep; step < propertyPath.getSteps(); step += 2 ) {
    // LOG.logDebug( "Looking up property: " + propertyPath.getStep( step ).getPropertyName() );
    // MappedPropertyType pt = getPropertyType( ftNode.getFeatureType(), propertyPath.getStep( step ) );
    // if ( pt instanceof MappedSimplePropertyType ) {
    // addSimplePropertyNode( ftNode, (MappedSimplePropertyType) pt, propertyPath, step );
    // break;
    // } else if ( pt instanceof MappedGeometryPropertyType ) {
    // addGeometryPropertyNode( ftNode, (MappedGeometryPropertyType) pt, propertyPath, step );
    // break;
    // } else if ( pt instanceof MappedFeaturePropertyType ) {
    // MappedFeaturePropertyType featurePT = (MappedFeaturePropertyType) pt;
    // ftNode = addFeaturePropertyNode( ftNode, featurePT, propertyPath, step );
    // } else {
    // assert ( false );
    // }
    // }
    // }

    /**
     * Returns the {@link MappedPropertyType} for the given {@link MappedFeatureType} that matches the given
     * {@link PropertyPathStep}.
     *
     * @param ft
     * @param step
     * @return matching property type or null, if none exists
     */
    private MappedPropertyType getPropertyType( MappedFeatureType ft, PropertyPathStep step ) {

        MappedPropertyType pt = null;
        QualifiedName name = step.getPropertyName();

        if ( step instanceof AttributeStep ) {
            // TODO remove handling of gml:id here (after adaptation of feature model)
            if ( CommonNamespaces.GMLNS.equals( name.getNamespace() ) && "id".equals( name.getLocalName() ) ) {
                MappedGMLId gmlId = ft.getGMLId();
                pt = new MappedSimplePropertyType( name, Types.VARCHAR, 1, 1, false, null, gmlId.getIdFields()[0] );
            }
        } else {
            // normal property (not gml:id)
            pt = (MappedPropertyType) ft.getProperty( name );

            // quirk starts here
            if ( pt == null && name.getNamespace() == null ) {
                pt = (MappedPropertyType) ft.getProperty( name, true );
            }
        }

        if ( pt == null && step instanceof AnyStep ) {
            AnyStep as = (AnyStep) step;
            if ( as.getIndex() != 0 ) {
                pt = (MappedPropertyType) ft.getProperty( ft.getPropertyName( as.getIndex() - 1 ) );
            } else {
                pt = (MappedPropertyType) ft.getProperty( ft.getPropertyName( 0 ) );
            }
        }

        return pt;
    }

    // private void addSimplePropertyNode( FeatureTypeNode featureTypeNode, MappedSimplePropertyType propertyType,
    // PropertyPath propertyPath, int step ) {
    //
    // assert ( step == propertyPath.getSteps() - 1 );
    // String[] tableAliases = generateTableAliases( propertyType );
    // PropertyNode propertyNode = new SimplePropertyNode( propertyType, featureTypeNode, tableAliases );
    // this.propertyPaths.add( propertyPath );
    // this.propertyNodes.add( propertyNode );
    // featureTypeNode.addPropertyNode( propertyNode );
    // }
    //
    // private void addGeometryPropertyNode( FeatureTypeNode featureTypeNode, MappedGeometryPropertyType propertyType,
    // PropertyPath propertyPath, int step ) {
    //
    // assert ( step == propertyPath.getSteps() - 1 );
    // String[] tableAliases = generateTableAliases( propertyType );
    // PropertyNode propertyNode = new GeometryPropertyNode( propertyType, featureTypeNode, tableAliases );
    // this.propertyPaths.add( propertyPath );
    // this.propertyNodes.add( propertyNode );
    // featureTypeNode.addPropertyNode( propertyNode );
    // }
    //
    // private FeatureTypeNode addFeaturePropertyNode( FeatureTypeNode ftNode, MappedFeaturePropertyType pt,
    // PropertyPath propertyPath, int step ) {
    //
    // assert ( step < propertyPath.getSteps() - 1 );
    // MappedFeatureType[] allowedTypes = pt.getFeatureTypeReference().getFeatureType().getConcreteSubstitutions();
    // QualifiedName givenTypeName = propertyPath.getStep( step + 1 ).getPropertyName();
    // MappedFeatureType givenType = null;
    //
    // for ( int i = 0; i < allowedTypes.length; i++ ) {
    // if ( allowedTypes[i].getName().equals( givenTypeName ) ) {
    // givenType = allowedTypes[i];
    // break;
    // }
    // }
    // assert ( givenType != null );
    //
    // // TODO make proper
    // String[] tableAliases = this.aliasGenerator.generateUniqueAliases( pt.getTableRelations().length - 1 );
    // String tableAlias = this.aliasGenerator.generateUniqueAlias();
    // FeatureTypeNode childFeatureTypeNode = new FeatureTypeNode( givenType, null, tableAlias );
    //
    // FeatureType ft = pt.getFeatureTypeReference().getFeatureType();
    // LOG.logDebug( "featureType: " + ft.getName() );
    //
    // PropertyNode propertyNode = new FeaturePropertyNode( pt, ftNode, tableAliases, childFeatureTypeNode );
    // // this.propertyPaths.add (propertyPath);
    // // this.propertyNodes.add (propertyNode);
    // ftNode.addPropertyNode( propertyNode );
    // return childFeatureTypeNode;
    // }

    private String[] generateTableAliases( MappedPropertyType pt ) {
        String[] aliases = null;
        TableRelation[] relations = pt.getTableRelations();
        if ( relations != null ) {
            aliases = new String[relations.length];
            for ( int i = 0; i < aliases.length; i++ ) {
                aliases[i] = this.aliasGenerator.generateUniqueAlias();
            }
        }
        return aliases;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for ( FeatureTypeNode root : this.roots ) {
            sb.append( root.toString( "" ) );
            sb.append( '\n' );
        }
        return sb.toString();
    }
}
