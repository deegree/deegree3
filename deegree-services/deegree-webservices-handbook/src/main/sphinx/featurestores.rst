.. _anchor-configuration-featurestore:

===========================
Feature store configuration
===========================

Feature stores are data sources that provide access to stored features. The two most common use cases for feature stores are:

* Accessing via WFS
* Provides data for vector layers in the WMS

The remainder of this chapter describes some relevant terms and the feature store configuration files in detail. You can access this configuration level by clicking on the ``feature stores`` link in the administration console. The configuration files have to be created or edited in the ``datasources/feature/`` directory of the deegree workspace.

-----------------------------------------------
Features, feature types and application schemas
-----------------------------------------------

Features are abstractions of real-world objects, such as rivers, buildings, streets or state boundaries. They are the geo objects of a particular application domain.

Feature types define classes of features. For example, a feature type ``River`` could define a class of river features that all have the same properties

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Simple vs. complex feature types
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Some feature type are much more complex than others. Traditionally, GIS software only copes with "simple" feature types:

* Every property is either simple (string, number, date, etc.) or a geometry
* Only a single property with one name is allowed

Basically, a simple feature type is everything that can be represented using a single database table or a single shape file. In contrast, complex feature types additionally allow the following:

* Multiple properties with the same name
* Properties that contain other features
* Properties that reference other features
* Properties that contain GML core datatypes which are not geometries (e.g. code types or units of measure)
* Properties that contain generic XML

All deegree feature stores support simple feature types, but only the SQL feature store and the memory feature store support complex feature types.

^^^^^^^^^^^^^^^^^^^
Application schemas
^^^^^^^^^^^^^^^^^^^

An application schema defines a hierarchy of (usually complex) feature types for a particular domain. When referring to an application schema, one usually means a GML application schema that defines a hierarchy of complex feature types. The following diagram shows a part of the INSPIRE Annex I application schema:

.. figure:: images/address_schema.png
   :figwidth: 60%
   :width: 50%
   :target: _images/address_schema.png

These kinds of application schemas can be served using the SQL feature store or the memory feature store.

-------------------
Shape feature store
-------------------

The shape feature store serves a feature type from an ESRI shape file. The configuration format for the deegree shape feature store is defined by schema file http://schemas.deegree.org/datasource/feature/shape/3.1.0/shape.xsd.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Minimal configuration example
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The only mandatory element is ``File``. A minimal valid configuration example looks like this:

.. topic:: Shape Feature Store config (minimal configuration example)

   .. literalinclude:: xml/shapefeaturestore_minimal.xml
      :language: xml

This configuration will set up a feature store based on the shape file ``/tmp/rivers.shp`` with the following settings:

* The feature store offers the feature type ``app:rivers`` (``app`` bound to ``http://www.deegree.org/app``)
* SRS information is taken from file ``/tmp/rivers.prj`` (if it does not exist, ``EPSG:4326`` is assumed)
* The geometry is added as property ``app:GEOMETRY``
* All data columns from file ``/tmp/rivers.dbf`` are used as properties in the feature type
* Encoding of text columns in ``/tmp/rivers.dbf`` is guessed based on actual contents
* An alphanumeric index is created for the dbf to speed up filtering based on non-geometric constraints

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
More complex configuration example 
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

A more complex example that uses all available configuration options:

.. topic:: Shape Feature Store config (more complex configuration example)

   .. literalinclude:: xml/shapefeaturestore_complex.xml
      :language: xml

This configuration will set up a feature store based on the shape file ``/tmp/rivers.shp`` with the following settings:

* SRS of stored geometries is ``EPSG:4326`` (no auto-detection)
* The feature store offers the shape file contents as feature type ``app:River`` (``app`` bound to ``http://www.deegree.org/app``)
* Encoding of text columns in ``/tmp/rivers.dbf`` is ``ISO-8859-1`` (no auto-detection)
* No alphanumeric index is created for the dbf (filtering based on non-geometric constraints has to be performed in-memory)
* The mapping between the shape file columns and the feature type properties is customized.
* Property ``objectid`` corresponds to column ``OBJECTID`` of the shape file
* Property ``geometry`` corresponds to the geometry of the shape file

^^^^^^^^^^^^^^^^^^^^^
Configuration options
^^^^^^^^^^^^^^^^^^^^^

The following table lists all available configuration options. When specifiying them, their order must be respected.

.. table:: Options for ``ShapeFeatureStore`` configuration files

+-----------------------------+-------------+---------+------------------------------------------------------------------------------+
| Option                      | Cardinality | Value   | Description                                                                  |
+=============================+=============+=========+==============================================================================+
| StorageCRS                  | 0..1        | String  | CRS of stored geometries                                                     |
+-----------------------------+-------------+---------+------------------------------------------------------------------------------+
| FeatureTypeName             | 0..n        | String  | Local name of the feature type (defaults to base name of shape file)         |
+-----------------------------+-------------+---------+------------------------------------------------------------------------------+
| FeatureTypeNamespace        | 0..1        | String  | Namespace of the feature type (defaults to "http://www.deegree.org/app")     |
+-----------------------------+-------------+---------+------------------------------------------------------------------------------+
| FeatureTypePrefix           | 0..1        | String  | Prefix of the feature type (defaults to "app")                               |
+-----------------------------+-------------+---------+------------------------------------------------------------------------------+
| File                        | 1..1        | String  | Path to shape file (can be relative)                                         |
+-----------------------------+-------------+---------+------------------------------------------------------------------------------+
| Encoding                    | 0..1        | Integer | Encoding of text fields in dbf file                                          |
+-----------------------------+-------------+---------+------------------------------------------------------------------------------+
| GenerateAlphanumericIndexes | 0..1        | Boolean | Set to true, if an index for alphanumeric fields should be generated         |
+-----------------------------+-------------+---------+------------------------------------------------------------------------------+
| Mapping                     | 0..1        | Complex | Customized mapping between dbf column names and property names               |
+-----------------------------+-------------+---------+------------------------------------------------------------------------------+

--------------------
Memory feature store
--------------------

The memory feature store serves feature types that are defined by a GML application schema and are stored in memory. The configuration format for the deegree memory feature store is defined by schema file http://schemas.deegree.org/datasource/feature/memory/3.0.0/memory.xsd.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Minimal configuration example
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The only mandatory element is ``GMLSchema``. A minimal valid configuration example looks like this:

.. topic:: Memory Feature Store config (minimal configuration example)

   .. literalinclude:: xml/memoryfeaturestore_minimal.xml
      :language: xml

This configuration will set up a memory feature store with the following settings:

* The GML 3.2 application schema from file ``../../appschemas/inspire/annex1/addresses.xsd`` is used as application schema (i.e. scanned for feature type definitions)
* No GML datasets are loaded on startup, so the feature store will be empty unless an insertion is performed (e.g. via WFS-T)

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
More complex configuration example 
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

A more complex example that uses all available configuration options:

.. topic:: Memory Feature Store config (more complex configuration example)

   .. literalinclude:: xml/memoryfeaturestore_complex.xml
      :language: xml

This configuration will set up a memory feature store with the following settings:

* Directory ``../../appschemas/inspire/annex1/`` is scanned for ``*.xsd`` files. All found files are loaded as a GML 3.2 application schema (i.e. analyzed for feature type definitions).
* Dataset file ``../../data/gml/address.gml`` is loaded on startup. This must be a GML 3.2 file that contains a feature collection with features that validates against the application schema.
* Dataset file ``../../data/gml/parcels.gml`` is loaded on startup. This must be a GML 3.2 file that contains a feature collection with features that validates against the application schema.
* The geometries of loaded features are converted to ``urn:ogc:def:crs:EPSG::4258``.

^^^^^^^^^^^^^^^^^^^^^
Configuration options
^^^^^^^^^^^^^^^^^^^^^

The following table lists all available configuration options (the complex ones contain nested options themselves). When specifiying them, their order must be respected.

.. table:: Options for ``Memory Feature Store`` configuration files

+-----------------------------+-------------+---------+------------------------------------------------------------------------------+
| Option                      | Cardinality | Value   | Description                                                                  |
+=============================+=============+=========+==============================================================================+
| StorageCRS                  | 0..1        | String  | CRS of stored geometries                                                     |
+-----------------------------+-------------+---------+------------------------------------------------------------------------------+
| GMLSchema                   | 1..n        | String  | Path/URL to GML application schema files/dirs to read feature types from     |
+-----------------------------+-------------+---------+------------------------------------------------------------------------------+
| GMLFeatureCollection        | 0..n        | Complex | Path/URL to GML feature collections documents to read features from          |
+-----------------------------+-------------+---------+------------------------------------------------------------------------------+

------------------------
Simple SQL feature store
------------------------

The simple SQL feature store serves simple feature types that are stored in a spatially-enabled database. The configuration format is defined by schema file http://schemas.deegree.org/datasource/feature/simplesql/3.0.1/simplesql.xsd.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Minimal configuration example
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

There are three mandatory elements: ``JDBCConnId``, ``SQLStatement`` and ``BBoxStatement``. A minimal configuration example looks like this:

.. topic:: Simple SQL Feature Store config (minimal configuration example)

   .. literalinclude:: xml/simplesqlfeaturestore_minimal.xml
      :language: xml

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
More complex configuration example 
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. topic:: Simple SQL Feature Store config (more complex configuration example)

   .. literalinclude:: xml/simplesqlfeaturestore_complex.xml
      :language: xml

^^^^^^^^^^^^^^^^^^^^^
Configuration options
^^^^^^^^^^^^^^^^^^^^^

The following table lists all available configuration options (the complex ones contain nested options themselves). When specifiying them, their order must be respected.

.. table:: Options for ``Simple SQL feature store`` configuration files

+-----------------------------+-------------+---------+------------------------------------------------------------------------------+
| Option                      | Cardinality | Value   | Description                                                                  |
+=============================+=============+=========+==============================================================================+
| StorageCRS                  | 0..1        | String  | CRS of stored geometries                                                     |
+-----------------------------+-------------+---------+------------------------------------------------------------------------------+
| FeatureTypeName             | 0..n        | String  | Local name of the feature type (defaults to table name)                      |
+-----------------------------+-------------+---------+------------------------------------------------------------------------------+
| FeatureTypeNamespace        | 0..1        | String  | Namespace of the feature type (defaults to "http://www.deegree.org/app")     |
+-----------------------------+-------------+---------+------------------------------------------------------------------------------+
| FeatureTypePrefix           | 0..1        | String  | Prefix of the feature type (defaults to "app")                               |
+-----------------------------+-------------+---------+------------------------------------------------------------------------------+
| JDBCConnId                  | 1..1        | String  | Identifier of the database connection                                        |
+-----------------------------+-------------+---------+------------------------------------------------------------------------------+
| SQLStatement                | 1..1        | String  | SELECT statement that defines the feature type                               |
+-----------------------------+-------------+---------+------------------------------------------------------------------------------+
| BBoxStatement               | 1..1        | String  | SELECT statement for the bounding box of the feature type                    |
+-----------------------------+-------------+---------+------------------------------------------------------------------------------+
| LODStatement                | 0..n        | Complex | Statements for specific WMS scale ranges                                     |
+-----------------------------+-------------+---------+------------------------------------------------------------------------------+

-------------------------
SQL feature store: Basics
-------------------------

The SQL feature store implementation currently supports the following backends:

* PostgreSQL (8.3, 8.4, 9.0, 9.1) with PostGIS extension (1.4, 1.5, 2.0)
* Oracle Spatial (10g, 11g)

The SQL feature store configuration format is defined by schema file http://schemas.deegree.org/datasource/feature/sql/3.2.0/sql.xsd. Due to the potential complexity, it is highly recommended to perform editing of SQL feature store configs in a schema-aware XML editor. The basic structure of an SQL feature store config always looks like this:

.. topic:: SQL feature store config (skeleton)

   .. literalinclude:: xml/sqlfeaturestore_basic.xml
      :language: xml

The root element has to be ``SQLFeatureStore`` and the config attribute must be ``3.2.0``. The only mandatory element is:

* ``JDBCConnId``: Id of the JDBC connection to use (see ...)

This example is valid, but will not have much of an effect, as it does not define any feature types. In order to add feature types, you first have to choose between two configuration approaches supported by the SQL feature store implementation. Both approaches map data stored in tables to feature, but they work differently. The following table shows a comparison.

.. raw:: latex

   \begin{table}
   \begin{center}

.. table::

+------------------------------+----------------------------+---------------------------------+
|                              | Table-driven mode          | Schema-driven mode              |
+==============================+============================+=================================+
| GML application schema       | Derived from tables        | Must be provided                |
+------------------------------+----------------------------+---------------------------------+
| Feature type definitions     | Derived from tables        | Derived from GML app schema     |
+------------------------------+----------------------------+---------------------------------+
| GML version                  | Any (GML 2, 3.0, 3.1, 3.2) | Fixed to version of app schema  |
+------------------------------+----------------------------+---------------------------------+
| Mapping principle            | Property to table column   | XPath-based or BLOB-based       |
+------------------------------+----------------------------+---------------------------------+
| Supported mapping complexity | Low                        | Very high                       |
+------------------------------+----------------------------+---------------------------------+

.. raw:: latex

   \end{center}
   \caption{SQLFeatureStore: Table-driven vs. schema-driven-mode}
   \end{table}

.. hint::
  If you want to map an existing GML application schema (e.g. INSPIRE Data Themes, GeoSciML, CityGML, XPlanung, AAA) always use schema-driven mode. Otherwise, try if table-driven meets your mapping requirements. If your table structures turn out to be too complex to be usable with table-driven mode, you will need to create a matching GML application schema manually and use schema-driven mode.

------------------------------------
SQL feature store: Table-driven mode
------------------------------------

Table-driven mode configs use one or more ``FeatureType`` elements to define the tables that are accessed as feature types:

.. topic:: SQL feature store (table-driven mode): Mapping a single table

   .. literalinclude:: xml/sqlfeaturestore_tabledriven1.xml
      :language: xml

The above example assumes that the database contains a table named ``country``, which is located within the default database schema (for PostgreSQL ``public``). Alternatively you can fully qualify the table name such as ``public.country``. The feature store will determine the columns of the table automatically and derive a feature type definition:

* Feature type name: ``app:country`` (app=http://www.deegree.org/app)
* feature id (``gml:id``) based on primary key column of table ``country``
* every primitive column (number, string, date) is used as a primitive property
* every geometry column is used as a geometry property

A single config file may map more than one table. The following example defines two feature types, based on tables ``country`` and ``cities``.

.. topic:: SQL feature store (table-driven mode): Mapping two tables

   .. literalinclude:: xml/sqlfeaturestore_tabledriven2.xml
      :language: xml

There are several optional attributes and elements that give you more control over the derived feature type definition. The ``name`` attribute allows to set the feature type name explicity. In the following example, it will be ``app:Land`` (Land is German for country).

.. topic:: SQL feature store (table-driven mode): Customizing the feature type name

   .. literalinclude:: xml/sqlfeaturestore_tabledriven3.xml
      :language: xml

You may use standard XML namespace binding to control the namespace and prefix of the feature type:

.. topic:: SQL feature store (table-driven mode): Customizing the feature type namespace and prefix

   .. literalinclude:: xml/sqlfeaturestore_tabledriven4.xml
      :language: xml

-------------------------------------
SQL feature store: Schema-driven mode
-------------------------------------

In schema-driven mode, the SQL feature store always retrieves feature type definitions and property declarations from a GML application schema (e.g. INSPIRE Addresses, GeoSciML, CityGML, XPlanung, AAA) specified in the configuration. A basic configuration for schema-driven mode defines the JDBC connection id, the CRS of the stored geometries and one or more GML schema files that make up the application schema:

.. topic:: SQL FeatureStore (Schema-driven mode): Skeleton config

   .. literalinclude:: xml/sqlfeaturestore_schemadriven1.xml
      :language: xml

* ``StorageCRS``:
* ``GMLSchemaFile``:

The remainder of the configuration defines how feature types from the GML schema are mapped to database tables and columns. Schema-driven mode knows two variants for mapping feature types:

* Relational mapping:
* BLOB mapping:

^^^^^^^^^^^^^^^^^^
Relational mapping
^^^^^^^^^^^^^^^^^^

In schema-driven, relational mapping mode, the mapping of a feature type is defined using ``FeatureTypeMapping`` elements:

.. topic:: SQL FeatureStore (Schema-driven mode): Relational skeleton config

   .. literalinclude:: xml/sqlfeaturestore_schemadriven2.xml
      :language: xml

The ``FeatureTypeMapping`` element has the following attributes:

* ``name``: Qualified name of the feature type to map. Use standard XML namespace mechanisms (``xmlns``) for binding namespace prefixes.
* ``table``: Name of the base table that stores the feature type. Properties may be mapped to related tables, but the base table must at least contain the columns that constitute the unique feature id (gml:id).

.. hint::
   In schema-driven mode, every mapped feature type must be defined in the referenced GML schema file. It is however not necessary to map all feature types defined in the schema. Unmapped feature types will be known to the feature store (e.g. a WFS will list them in a GetCapabilities response), but not queryable.

""""""""""
Feature id
""""""""""

The first child of every ``FeatureTypeMapping`` element must be a ``FIDMapping`` element:

.. topic:: SQL feature store (schema-driven mode): FeatureTypeMapping elements

   .. literalinclude:: xml/sqlfeaturestore_featuretypemapping1.xml
      :language: xml

.. hint::
   After providing a correct FIDMapping, a feature type is already queryable, e.g. you can perform a ``GetFeature`` requests against a WFS that uses this feature store. When creating a configuration manually for an existing database, it is a good idea to do this as a first step. This way you test if everything works so far (although no properties will be returned).

""""""""""
Properties
""""""""""

In order to add mappings for properties of the feature type, the following mapping elements are available:

* ``Primitive``: Maps a primitive value (a text node or an attribute node) of the feature.
* ``Geometry``: Maps a geometry value of the feature.
* ``Feature``: Maps a referenced or inlined feature of the feature.
* ``Complex``: Maps a complex element that is neither a geometry nor a feature. A container for nested mapping elements.

Mapping the actual content of a feature works by associating XML nodes with columns in the database. In the beginning of the feature type mapping, the current node is the root element of the feature ``ad:Address`` and the current table is ``ad_address``.

----------------------------------------
SQL feature store: Feature id generation
----------------------------------------

When new features are inserted into a SQL feature store (for example via a WFS transaction), the values of the feature ids (in the gml:id attribute) sometimes have to be re-generated. This depends on the used id generation mode. There are three id generation modes available, which stem from the WFS 1.1.0 specification:

* ``UseExisting``: The feature store will store the original gml:id values that have been provided in the input. This may lead to errors if the provided ids are already in use or if the format of the id does not match the configuration.
* ``GenerateNew``: The feature store will discard the original gml:id values and use the configured generator to produce new and unique identifiers. References in the input (xlink:href) that point to a feature with an id that is re-generated are fixed.
* ``ReplaceDuplicate``: TBD. Not implemented yet.

There are several aspects of the id generation that can be configured in the ``FIDMapping`` element. Here's an example snippet:

.. topic:: SQL feature store: FIDMapping (Feature id generation)

   .. literalinclude:: xml/sqlfeaturestore_fidmapping1.xml
      :language: xml

The above snippet defines the feature id mapping and id generation behaviour for a feature type called ``ad:Address``

* Column ``attr_gml_id`` stores the value of the gml:id (minus the prefix ``AD_ADDRESS_``). If ``attr_gml_id`` contains the value ``42``, the corresponding feature instance will have the value ``AD_ADDRESS_42``.
* On insert (mode=UseExisting), the provided gml:id values must have the format ``AD_ADDRESS_$``. The prefix ``AD_ADDRESS_`` is removed and the remaining part of the identifier is stored in column ``attr_gml_id``.
* On insert (mode=GenerateNew), the database sequence ``SEQ_FID`` is queried for new values to be stored in column ``attr_gml_id``.



^^^^^^^^^^^^
BLOB mapping
^^^^^^^^^^^^

An alternative approach to schema-driven relational mapping is schema-driven BLOB mapping.

