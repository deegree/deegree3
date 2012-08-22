What's it?
----------

philosopher/

Complete WFS example configuration with a fairly complex (hierarchical + recursive) application
schema. Uses PostGIS as backend. The necessary DDL (table generation) scripts are in
"/scripts/sql/wfs/postgis". Don't forget to edit db connection information in
philosopher/featuretypes/Philosopher.xsd. Example requests (including a transaction to fill insert
some features) can be found in "philosopher/requests".


featuretypes/

Examples of (annotated) GML application schemas aka feature type definitions.


ogc/

Example requests from the Web Feature Service Specification 1.1.0. Corrected, where needed.
