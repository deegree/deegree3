package org.deegree.tools.featurestoresql.config;

import org.deegree.feature.types.AppSchema;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.springframework.batch.item.ItemReader;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class AppSchemaReader implements ItemReader<AppSchema> {

    private String schemaUrl;

    public AppSchemaReader( String schemaUrl ) {
        this.schemaUrl = schemaUrl;
    }

    @Override
    public AppSchema read()
                            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if ( schemaUrl != null ) {
            String[] schemaUrls = { schemaUrl };
            GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaUrls );
            AppSchema appSchema = xsdDecoder.extractAppSchema();
            schemaUrl = null;
            return appSchema;
        }
        return null;
    }

}