# deegree-cli-utility

[![Build Status](https://travis-ci.org/JuergenWeichand/deegree-cli-utility.svg?branch=master)](https://travis-ci.org/JuergenWeichand/deegree-cli-utility)

Command line utility based on [deegree3](https://github.com/deegree/deegree3) to generate `DDL` and `deegree SQLFeatureStore` from GML application schemas. 

You can download the latest release [here](https://github.com/lat-lon/deegree-cli-utility/releases) or build it yourself.

    git clone https://github.com/lat-lon/deegree-cli-utility.git
    cd deegree-cli-utility/
    git checkout deegree-3.4
    mvn clean package -Pjar-with-dependencies

## Usage

```
Usage: java -jar deegree-cli-utility.jar [options] schema_url

options:
 --format={deegree|ddl|all}, default=deegree
 --srid=<epsg_code>, default=4258
 --idtype={int|uuid}, default=int
 --mapping={relational|blob}, default=relational
 --dialect={postgis|oracle}, default=postgis
 --listOfPropertiesWithPrimitiveHref=<path/to/file>
```

The SQL DDL and XML output is written into files in the current directory. The filename of each file is derived from the 
schema file name in the given `schema_url`.

### Example: Generate SQL DDL for INSPIRE Cadastral Parcels 4.0 with UUIDGenerator

    java -jar deegree-cli-utility.jar --srid=25832 --format=ddl --idtype=uuid http://inspire.ec.europa.eu/schemas/cp/4.0/CadastralParcels.xsd

The generated file is './CadastralParcels.sql'.    

### Example: Generate deegree SQLFeatureStore for INSPIRE Cadastral Parcels 4.0 with UUIDGenerator

    java -jar deegree-cli-utility.jar --srid=25832 --format=deegree --idtype=uuid http://inspire.ec.europa.eu/schemas/cp/4.0/CadastralParcels.xsd
    
The generated file is './CadastralParcels.xml'.    

### Example: Generate SQL DDL for INSPIRE Cadastral Parcels 4.0 with AutoIDGenerator

    java -jar deegree-cli-utility.jar --srid=25832 --format=ddl --idtype=int http://inspire.ec.europa.eu/schemas/cp/4.0/CadastralParcels.xsd

The generated file is './CadastralParcels.sql'.

### Example: Generate deegree SQLFeatureStore for INSPIRE Cadastral Parcels 4.0 with AutoIDGenerator

    java -jar deegree-cli-utility.jar --srid=25832 --format=deegree --idtype=int http://inspire.ec.europa.eu/schemas/cp/4.0/CadastralParcels.xsd

The generated file is './CadastralParcels.xml'.

### Example: Generate deegree SQLFeatureStore and SQL DDL for INSPIRE Cadastral Parcels 4.0 with AutoIDGenerator

    java -jar deegree-cli-utility.jar --srid=25832 --format=all --idtype=int http://inspire.ec.europa.eu/schemas/cp/4.0/CadastralParcels.xsd

The generated files are './CadastralParcels.sql' and './CadastralParcels.xml'.

### Example: Generate deegree SQLFeatureStore and SQL DDL for INSPIRE Cadastral Parcels 4.0 with Blob-Mapping

    java -jar deegree-cli-utility.jar --format=all --mapping=blob http://inspire.ec.europa.eu/schemas/cp/4.0/CadastralParcels.xsd
    
The generated files are './CadastralParcels.sql' and './CadastralParcels.xml' with Blob-Mapping for PostGIS.    

### Example: Generate deegree SQLFeatureStore and SQL DDL for INSPIRE Cadastral Parcels 4.0 for Oracle DBMS with Oracle Locator

    java -jar deegree-cli-utility.jar --format=all --dialect=oracle http://inspire.ec.europa.eu/schemas/cp/4.0/CadastralParcels.xsd

The generated files are './CadastralParcels.sql' and './CadastralParcels.xml' with relational mapping for Oracle Locator.

### Example: Generate deegree SQLFeatureStore for INSPIRE Cadastral Parcels 4.0 with list of properties with primitive href

    java -jar deegree-cli-utility.jar --format=deegree --listOfPropertiesWithPrimitiveHref=<path/to/file> http://inspire.ec.europa.eu/schemas/cp/4.0/CadastralParcels.xsd

The generated file is './CadastralParcels.xml'. All properties listed in the referenced file are written with primitive instead of feature mappings.

## Usage of option listOfPropertiesWithPrimitiveHref

The option listOfPropertiesWithPrimitiveHref references a file listing properties which are written with primitive instead of feature mappings.

For example, in some INSPIRE themes codelists values are stored in xlink:href attributes. Corresponding to the GML appplication schema the type is a gml:ReferenceType. Usually deegree would handle this as feature mapping but it is recommended to use a primitive mapping here.

Primitive mapping enables direct filtering on those properties with deegree. For example, filtering on INSPIRE codelist hrefs is possible then.

Syntax of content of file:

    {NamespaceURI}localPart

If multiple properties shall use primitive mappings, they must be listed in new lines.

Example:

    {http://inspire.ec.europa.eu/schemas/gn/4.0}nativeness
    {http://inspire.ec.europa.eu/schemas/ps/4.0}designation

## Behind http proxy

Set the `http.proxyHost`, `http.proxyPort` and `http.nonProxyHosts` config properties.

    java -Dhttp.proxyHost=your-proxy.net -Dhttp.proxyPort=80 -jar deegree-cli-utility.jar --format=ddl --idtype=uuid http://inspire.ec.europa.eu/schemas/cp/4.0/CadastralParcels.xsd

# License

[![License](https://img.shields.io/badge/License-LGPL%20v2.1-blue.svg)](https://www.gnu.org/licenses/lgpl-2.1)
