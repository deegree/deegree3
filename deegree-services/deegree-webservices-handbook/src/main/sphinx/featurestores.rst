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

This kinds of application schemas can be served using the SQL feature store or the memory feature store.

-------------------
Shape feature store
-------------------

TBD

--------------------
Memory feature store
--------------------

TBD

------------------------
Simple SQL feature store
------------------------

TBD

-------------------------
SQL feature store: Basics
-------------------------

The SQL feature store implementation currently supports the following backends:

* PostgreSQL (8.3, 8.4, 9.0, 9.1) with PostGIS (1.4, 1.5)
* Oracle Spatial (10g, 11g)

The SQL feature store configuration format is defined by schema file http://schemas.deegree.org/datasource/feature/sql/3.1.0/sql.xsd. Due to the potential complexity, it is highly recommended to perform editing of SQL feature store configs in a schema-aware XML editor. The basic structure of an SQL feature store config always looks like this:

.. topic:: SQL FeatureStore config (skeleton)

   .. literalinclude:: xml/sqlfeaturestore_basic.xml
      :language: xml

The root element has to be ``SQLFeatureStore`` and the config attribute must be ``3.1.0``. The only mandatory element is:

* ``JDBCConnId``: Id of the JDBC connection to use (see ...)

This example is valid, but will not do much, as it does not define any feature types. In order to add feature types, you first have to choose between two configuration approaches supported by the SQL feature store implementation. Both approaches map data stored in tables to feature, but they work quite differently. The following table shows a comparison.

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
   \caption{SQLFeatureStore: Table-driven vs. Schema-driven-mode}
   \end{table}

.. hint::
  If you want to map an existing GML application schema (e.g. INSPIRE Addresses, GeoSciML, CityGML, XPlanung, AAA) always use schema-driven mode. Otherwise, try if table-driven meets your mapping requirements. If your table structures turn out to be too complex to be usable with table-driven mode, you will need to create a matching GML application schema manually and use schema-driven mode.

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

One config file may map more than one table. The following example defines two feature types, based on tables ``country`` and ``cities``.

.. topic:: SQL feature store (table-driven mode): Mapping two stables

   .. literalinclude:: xml/sqlfeaturestore_tabledriven2.xml
      :language: xml

There are several optional attributes and elements that will give you more control over the feature type definition. The ``name`` attribute allows to set the feature type name explicity. In the following example, it will be ``app:Land`` (Land is German for country).

.. topic:: SQL feature store (table-driven mode): Customizing the feature type name

   .. literalinclude:: xml/sqlfeaturestore_tabledriven3.xml
      :language: xml

You may use standard XML namespace binding to control the namespace and prefix of the feature type:

.. topic:: SQL FeatureStore (Table-driven mode): Customizing the feature type namespace and prefix

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

Inside each ``FeatureTypeMapping`` element, a ``FIDMapping`` element is required:

.. topic:: SQL feature store (schema-driven mode): FeatureTypeMapping elements

   .. literalinclude:: xml/sqlfeaturestore_featuretypemapping1.xml
      :language: xml

.. hint::
   After providing a correct FIDMapping, a feature type is already queryable, e.g. you can perform a ``GetFeature`` requests against a WFS (if you add it to the workspace first). When creating a configuration manually for an existing database, it is a recommended to do this as a first step to see that everything works so far (although no properties will be returned).

""""""""""
Properties
""""""""""

In order to add mappings for properties of the feature type, the following mapping elements are available:

* ``Primitive``: Maps a primitive value (a text node or an attribute node) of the feature.
* ``Geometry``: Maps a geometry value of the feature.
* ``Feature``: Maps a referenced or inlined feature of the feature.
* ``Complex``: Maps a complex element that is neither a geometry nor a feature. A container for nested mapping elements.

Mapping the actual content of a feature works by associating XML nodes with columns in the database. In the beginning of the feature type mapping, the current node is the root element of the feature ``ad:Address`` and the current table is ``ad_address``.


^^^^^^^^^^^^
BLOB mapping
^^^^^^^^^^^^

An alternative approach to schema-driven relational mapping is schema-driven BLOB mapping.

