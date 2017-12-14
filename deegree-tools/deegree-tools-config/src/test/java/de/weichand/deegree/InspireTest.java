/*-
 * #%L
 * deegree-cli-utility
 * %%
 * Copyright (C) 2016 - 2017 weichand.de, lat/lon GmbH
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
package de.weichand.deegree;

import org.junit.Test;

/**
 *
 * @author weich_ju
 */
public class InspireTest {

    final String SCHEMA_URL_CP = "http://inspire.ec.europa.eu/schemas/cp/4.0/CadastralParcels.xsd";

    @Test
    public void schemaOnly()
                    throws Exception {
        String[] args = { SCHEMA_URL_CP };
        Exec.main( args );
    }

    @Test
    public void schemaToDeegreeConfig()
                    throws Exception {
        String[] args = { SCHEMA_URL_CP, "--format=deegree" };
        Exec.main( args );
    }

    @Test
    public void schemaToDeegreeConfigWithSrid()
                    throws Exception {
        String[] args = { SCHEMA_URL_CP, "--format=deegree", "--srid=31468" };
        Exec.main( args );
    }

    @Test
    public void schemaToDeegreeConfigAndSqlDdl()
                    throws Exception {
        String[] args = { SCHEMA_URL_CP, "--format=all", "--srid=31468" };
        Exec.main( args );
    }

    @Test
    public void schemaToDeegreeConfigAndSqlDdlAndIdTypeInteger()
                    throws Exception {
        String[] args = { SCHEMA_URL_CP, "--format=all", "--srid=4326", "--idtype=int" };
        Exec.main( args );
    }

    @Test
    public void schemaToDeegreeConfigAndSqlDdlAndIdTypeUuid()
                    throws Exception {
        String[] args = { SCHEMA_URL_CP, "--format=all", "--srid=4326", "--idtype=uuid" };
        Exec.main( args );
    }

    @Test
    public void schemaToSqlDdl()
                    throws Exception {
        String[] args = { SCHEMA_URL_CP, "--format=ddl" };
        Exec.main( args );
    }

    @Test
    public void schemaToDeegreeConfigAndSqlDdlWithBlobMapping()
                    throws Exception {
        String[] args = { SCHEMA_URL_CP, "--format=all", "--mapping=blob" };
        Exec.main( args );
    }

    @Test
    public void schemaToDeegreeConfigAndSqlDdlWithBlobMappingAndAllOptions()
                    throws Exception {
        String[] args = { SCHEMA_URL_CP, "--format=all", "--srid=4326", "--mapping=blob" };
        Exec.main( args );
    }

    @Test
    public void schemaToDeegreeConfigAndSqlDdlWithForOracle()
                    throws Exception {
        String[] args = { SCHEMA_URL_CP, "--format=all", "--srid=4326", "--dialect=oracle" };
        Exec.main( args );
    }

    @Test
    public void schemaToDeegreeConfigAndSqlDdlWithBlobMappingForOracle()
                    throws Exception {
        String[] args = { SCHEMA_URL_CP, "--format=all", "--srid=4326", "--mapping=blob", "--dialect=oracle" };
        Exec.main( args );
    }

    @Test
    public void schemaToDeegreeConfigAndListOfPropertiesWithPrimitiveHref()
                    throws Exception {
        String[] args = { SCHEMA_URL_CP, "--format=deegree",
                         "--listOfPropertiesWithPrimitiveHref=src/test/resources/listOfPropertiesWithPrimitiveHref" };
        Exec.main( args );
    }

}