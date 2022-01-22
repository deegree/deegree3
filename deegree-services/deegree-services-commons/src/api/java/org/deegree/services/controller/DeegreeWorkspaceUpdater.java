package org.deegree.services.controller;

import static org.deegree.commons.xml.stax.XMLStreamUtils.closeQuietly;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.xml.stax.XMLInputFactoryUtils;
import org.deegree.workspace.PreparedResources;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceManager;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.ResourceProvider;
import org.deegree.workspace.ResourceStates;
import org.deegree.workspace.ResourceStates.ResourceState;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.graph.ResourceNode;
import org.deegree.workspace.standard.DefaultResourceIdentifier;
import org.deegree.workspace.standard.DefaultResourceLocation;
import org.slf4j.Logger;

public class DeegreeWorkspaceUpdater {

    private static final Logger LOG = getLogger( DeegreeWorkspaceUpdater.class );

    public final static DeegreeWorkspaceUpdater INSTANCE = new DeegreeWorkspaceUpdater();

    private final XMLInputFactory xmlInputFactory;

    private List<File> filesRemoved = new ArrayList<File>();

    private List<File> filesModified = new ArrayList<File>();

    private List<File> filesUnmodified = new ArrayList<File>();

    private List<File> filesAdded = new ArrayList<File>();

    private Map<File, Long> fileStatusMap = new HashMap<File, Long>();

    private File lastWorkspaceLocation = null;

    private Workspace workspace;

    private DeegreeWorkspaceUpdater() {
        this.xmlInputFactory = XMLInputFactoryUtils.newSafeInstance();
    }

    public void init( DeegreeWorkspace workspace ) {
        // remember workspace and file status
        updateFileStatusMap( workspace.getLocation() );
        lastWorkspaceLocation = workspace.getLocation();
        this.workspace = workspace.getNewWorkspace();
    }

    public boolean isWorkspaceChange( DeegreeWorkspace newWorkspace ) {
        final File newLocation = newWorkspace.getLocation();
        if ( newLocation.equals( lastWorkspaceLocation ) )
            return false;
        lastWorkspaceLocation = newLocation;
        this.workspace = newWorkspace.getNewWorkspace();
        return true;
    }

    public void notifyWorkspaceChange( DeegreeWorkspace workspace ) {
        // remember file status
        LOG.info( "Workspace change" );
        updateFileStatusMap( workspace.getLocation() );
    }

    public void updateWorkspace( DeegreeWorkspace workspace ) {
        LOG.info( "Updating workspace" );
        synchronized ( this ) {
            analyzeChanges( workspace.getLocation() );
            applyChanges( workspace.getNewWorkspace() );
            // remember new file status
            updateFileStatusMap( workspace.getLocation() );
        }
    }

    private void analyzeChanges( File wsDir ) {
        filesRemoved.clear();
        filesAdded.clear();
        filesModified.clear();
        filesUnmodified.clear();

        final List<File> allFiles = collectFiles( wsDir, null );

        for ( File file : allFiles ) {
            if ( fileStatusMap.containsKey( file ) ) { // existing
                final long lastTimeStamp = fileStatusMap.get( file );
                final long lastModified = file.lastModified();
                if ( lastTimeStamp != lastModified )
                    filesModified.add( file );
                else
                    filesUnmodified.add( file );
            } else {
                filesAdded.add( file );
            }
        }
        // check removed
        for ( File file : fileStatusMap.keySet() ) {
            if ( !allFiles.contains( file ) ) {
                filesRemoved.add( file );
            }
        }

        logChange( "new", filesAdded, true );
        logChange( "removed", filesRemoved, true );
        logChange( "modified", filesModified, true );
        logChange( "unmodified", filesUnmodified, false );
    }

    private void updateFileStatusMap( File wsDir ) {
        fileStatusMap.clear();
        final List<File> allFiles = new ArrayList<File>();
        collectFiles( wsDir, allFiles );
        for ( File file : allFiles ) {
            fileStatusMap.put( file, file.lastModified() );
        }
    }

    private void logChange( String message, List<File> files, boolean verbose ) {
        LOG.debug( message + ": " + files.size() );
        if ( verbose ) {
            for ( File file : files ) {
                LOG.debug( message + ": " + file.getName() );
            }
        }
    }

    private List<File> collectFiles( File file, List<File> collector ) {
        if ( collector == null )
            collector = new ArrayList<File>();
        if ( file.isFile() ) {
            if ( !isFileToIgnore( file ) )
                collector.add( file );
            return collector;
        } else if ( file.isDirectory() ) {
            for ( File child : file.listFiles() ) {
                collectFiles( child, collector );
            }
        }
        return collector;
    }

    private boolean isFileToIgnore( File file ) {
        // these files should not be managed
        final String path = file.getAbsolutePath();
        if ( path.contains( "appschemas" ) )
            return true;
        if ( "bbox_cache.properties".equals( file.getName() ) )
            return true;
        if ( "main.xml".equals( file.getName() ) )
            return true;
        return false;
    }

    private void applyChanges( Workspace workspace ) {

        final StringBuffer debugLog = new StringBuffer();

        try {
            // 1. destroy deleted / modified resources
            final Set<File> notDestroyed = destroyResources( workspace, filesRemoved, filesModified );

            // 2. prepare added / modified resources
            PreparedResources preparedResources = new PreparedResources( workspace );
            final Set<File> notPrepared = prepareResources( preparedResources, filesAdded );
            notPrepared.addAll( prepareResources( preparedResources, filesModified ) );

            // 3. initialize prepared resources and reinitialize dependent resources
            final Set<File> notInitialized = initResources( workspace, preparedResources );

            if ( notPrepared.size() + notDestroyed.size() + notInitialized.size() > 0 ) {
                debugLog.append( "WARNING:" + "\n" );
                for ( File file : notPrepared ) {
                    debugLog.append( "NOT prepared/initialized: " + file + "\n" );
                }
                for ( File file : notDestroyed ) {
                    debugLog.append( "NOT destroyed: " + file + "\n" );
                }
            }

        } catch ( Exception e ) {
            LOG.error( e.getMessage(), e );
        } finally {
            LOG.debug( debugLog.toString() );
        }
    }

    private Set<File> destroyResources( final Workspace workspace, final Collection<File> filesRemoved, final Collection<File> filesModified ) {
        Set<File> errorFiles = new HashSet<>();
        LinkedHashMap<File, ResourceIdentifier<Resource>> fileToResourceId = getResourcesInDependencyOrder();
        List<File> managedFilesInOrder = new ArrayList<File>( fileToResourceId.keySet() );
        Collections.reverse( managedFilesInOrder );
        for ( File file : managedFilesInOrder ) {
            if ( filesRemoved.contains( file ) || filesModified.contains( file ) ) {
                LOG.info( "Destroying managed resource " + file );
                try {
                    workspace.destroyAndShutdownDependents( fileToResourceId.get( file ) );
                } catch ( Exception e ) {
                    LOG.error( e.getMessage(), e );
                    errorFiles.add( file );
                }
            }
        }
        return errorFiles;
    }

    private Set<File> prepareResources( PreparedResources preparedResources, Collection<File> newFiles ) {
        Set<File> errorFiles = new HashSet<>();
        for ( File file : new HashSet<File>( newFiles ) ) {
            try {
                prepareResource( file, preparedResources );
            } catch ( Exception e ) {
                LOG.error( e.getMessage(), e );
                errorFiles.add( file );
            }
        }
        return errorFiles;
    }

    private Set<File> initResources( Workspace workspace, PreparedResources preparedResources ) {
        Set<File> errorFiles = new HashSet<>();
        Set<ResourceIdentifier<Resource>> dependents = new LinkedHashSet<>();
        for ( ResourceMetadata<?> md : workspace.getDependencyGraph().toSortedList() ) {
            ResourceIdentifier<?> resourceId = md.getIdentifier();
            ResourceStates states = workspace.getStates();
            ResourceState state = states.getState( md.getIdentifier() );
            if ( state == ResourceState.Prepared ) {
                try {
                    workspace.init( resourceId, preparedResources );
                    collectDependents( dependents, workspace.getDependencyGraph().getNode( resourceId ) );
                    dependents.remove( resourceId );
                } catch ( Exception e ) {
                    LOG.error( e.getMessage(), e );
                    File file = getResourceFile( md );
                    if ( file != null ) {
                        errorFiles.add( file );
                    }
                }
            }
        }
        LOG.debug( "Reinitializing dependent resources: " + dependents );
        for ( ResourceIdentifier<Resource> id : dependents ) {
            ResourceMetadata<Resource> md = workspace.getDependencyGraph().getNode( id ).getMetadata();
            try {
                workspace.destroyAndShutdownDependents( md.getIdentifier() );
                workspace.add( md.getLocation() );
                workspace.init( md.getIdentifier(), null );
            } catch ( Exception e ) {
                LOG.error( e.getMessage(), e );
                File file = getResourceFile( md );
                if ( file != null ) {
                    errorFiles.add( file );
                }
            }
        }
        return errorFiles;
    }

    @SuppressWarnings("unchecked")
    private void collectDependents( Set<ResourceIdentifier<Resource>> dependents, ResourceNode<? extends Resource> node ) {
        if ( node == null ) {
            return;
        }
        for ( ResourceNode<? extends Resource> n : node.getDependents() ) {
            if ( n.getMetadata() != null ) {
                dependents.add( (ResourceIdentifier<Resource>) n.getMetadata().getIdentifier() );
                collectDependents( dependents, n );
            }
        }
    }

    private void prepareResource( File file, PreparedResources preparedResources ) {
        Path relativePath = Paths.get( lastWorkspaceLocation.toURI() ).relativize( Paths.get( file.toURI() ) );
        String fileName = relativePath.getFileName().toString();
        if ( !fileName.endsWith( ".xml" ) && !fileName.endsWith( ".ignore" ) ) {
            return;
        }
        String id = fileName.substring( 0, fileName.lastIndexOf( '.' ) );
        String configNamespace = getRootElement( file ).getNamespaceURI();
        Class<ResourceProvider<Resource>> providerClass = findResourceProviderClass( relativePath, configNamespace );
        if ( providerClass != null ) {
            ResourceIdentifier<Resource> identifier = new DefaultResourceIdentifier<>( providerClass, id );
            DefaultResourceLocation<Resource> location = new DefaultResourceLocation<>( file, identifier );
            workspace.add( location );
            if (fileName.endsWith( "xml" )) {
                ResourceBuilder<Resource> builder = workspace.prepare( identifier );
                preparedResources.addBuilder( identifier, builder );
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Class<ResourceProvider<Resource>> findResourceProviderClass( Path relativePath, String configNamespace ) {
        List<ResourceManager<? extends Resource>> managers = workspace.getResourceManagers();
        for ( ResourceManager<? extends Resource> manager : managers ) {
            String workspacePath = manager.getMetadata().getWorkspacePath();
            if ( relativePath.startsWith( workspacePath ) ) {
                List<?> providers = manager.getProviders();
                for ( Object p : providers ) {
                    ResourceProvider<Resource> rp = (ResourceProvider<Resource>) p;
                    if ( rp.getNamespace() != null && rp.getNamespace().equals( configNamespace ) ) {
                        return (Class<ResourceProvider<Resource>>) manager.getMetadata().getProviderClass();
                    }
                }
            }
        }
        return null;
    }

    private QName getRootElement( File file ) {
        XMLStreamReader xmlStream = null;
        try {
            xmlStream = xmlInputFactory.createXMLStreamReader( new FileInputStream( file ) );
            while ( !xmlStream.isStartElement() ) {
                xmlStream.next();
            }
            return xmlStream.getName();
        } catch ( FileNotFoundException | XMLStreamException e ) {
            return null;
        } finally {
            closeQuietly( xmlStream );
        }
    }

    @SuppressWarnings("unchecked")
    private LinkedHashMap<File, ResourceIdentifier<Resource>> getResourcesInDependencyOrder() {
        LinkedHashMap<File, ResourceIdentifier<Resource>> fileToResourceId = new LinkedHashMap<>();
        for ( ResourceMetadata<? extends Resource> md : workspace.getDependencyGraph().toSortedList() ) {
            File file = getResourceFile( md );
            if ( file != null ) {
                fileToResourceId.put( file, (ResourceIdentifier<Resource>) md.getIdentifier() );
            }
        }
        return fileToResourceId;
    }

    @SuppressWarnings("unchecked")
    private File getResourceFile( ResourceMetadata<? extends Resource> md ) {
        if ( md.getLocation() instanceof DefaultResourceLocation<?> ) {
            DefaultResourceLocation<?> defaultResourceLocation = (DefaultResourceLocation<Resource>) md.getLocation();
            if ( defaultResourceLocation.getAsFile() != null ) {
                return defaultResourceLocation.getAsFile();
            }
        }
        return null;
    }
}
