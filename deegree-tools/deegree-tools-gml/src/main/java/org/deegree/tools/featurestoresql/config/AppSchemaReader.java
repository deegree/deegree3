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
package org.deegree.tools.featurestoresql.config;

import org.deegree.feature.types.AppSchema;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.springframework.batch.item.ItemReader;

/**
 * Item reader to read application schema files.
 *
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