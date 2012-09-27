============================
Metadata store configuration
============================

----------------------------
Memory ISO Metadata store 
----------------------------
The memory ISO metadata store implementation is transactional and works file based.

The memory metadata store configuration is defined by schema file http://schemas.deegree.org/datasource/metadata/iso19139/3.2.0/memory.xsd

.. topic:: Memory ISO Metadatastore config (skeleton)

   .. literalinclude:: xml/metadatastore_iso_memory.xml
      :language: xml
      
The root element has to be ``ISOMemoryMetadataStore`` and the config attribute must be ``3.2.0``. The only mandatory element is:

* ``ISORecordDirectory``: A list of directories containing records loaded in the store during start of the store.  

To allow insert transactions one optional element must be declared:

* ``InsertDirectory``: Directory to store inserted records, can be one of the directories declared in the element ``ISORecordDirectory``.  

------------------------
SQL ISO Metadata store 
------------------------
The SQL ISO metadata store implementation currently supports the following backends:

* PostgreSQL with PostGIS
* Oracle Spatial

The SQL metadata store configuration is defined by schema file http://schemas.deegree.org/datasource/metadata/iso19115/3.2.0/iso19115.xsd

.. topic:: SQL ISO Metadatastore config (skeleton)

   .. literalinclude:: xml/metadatastore_iso_sql.xml
      :language: xml

The root element has to be ``ISOMetadataStore`` and the config attribute must be ``3.2.0``. The only mandatory element is:

* ``JDBCConnId``: Id of the JDBC connection to use (see ...)

The optional elements are:

* ``Inspectors``: List of inspectors inspecting a metadataset before inserting. Known inspectors are:

  * FileIdentifierInspector
  * InspireInspector
  * CoupledResourceInspector
  * SchemaValidator
  * NamespaceNormalizer
* ``AnyText``: Configuration of the values searchable by the queryable property ``AnyText``, possible values are:

  * All: all values
  * Core: the core queryable properties (default)
  * Custom: a custom set of properties defined as xpath expressions
* ``QueryableProperties``: Configuration of additional query properties.

-----------------------------
SQL EBRIM/EO Metadata store
-----------------------------

TBD