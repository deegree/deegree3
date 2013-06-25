package org.deegree.spring;

import java.net.URL;

import javax.xml.bind.JAXBElement;

import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.spring.jaxb.SingleBeanRef;

import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.AbstractResourceProvider;

public class GenericSpringResourceMetadata<T extends Resource> extends AbstractResourceMetadata<T> {

    private final String configJaxbPackage;

    private final URL configSchema;

    private final Class<T> clazz;

    public GenericSpringResourceMetadata( final Workspace workspace, final ResourceLocation<T> location,
                                          final AbstractResourceProvider<T> provider, final String configJaxbPackage,
                                          final URL configSchema, final Class<T> clazz ) {
        super( workspace, location, provider );

        this.configJaxbPackage = configJaxbPackage;
        this.configSchema = configSchema;
        this.clazz = clazz;
    }

    @Override
    public ResourceBuilder<T> prepare() {
        final SingleBeanRef config;

        try {
            final JAXBElement<?> element = (JAXBElement<?>) JAXBUtils.unmarshall( configJaxbPackage, configSchema,
                                                                                  location.getAsStream(), workspace );
            if ( element.getDeclaredType().equals( SingleBeanRef.class ) ) {
                config = (SingleBeanRef) element.getValue();
            } else {
                throw new ResourceInitException( "Wrong configuration object passed to GenericSpringResourceMetadata." );
            }
        } catch ( Exception e ) {
            throw new ResourceInitException( "Couldn't construct GenericSpringResourceBuilder.", e );
        }

        return new GenericSpringResourceBuilder<T>( workspace, config, clazz );
    }
}
