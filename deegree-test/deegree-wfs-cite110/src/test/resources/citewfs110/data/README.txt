The following WFS-1.1 test data are available for GMLSF levels 0 and 1.
Loading these features into the datastore is a precondition for compliance 
testing (see Note below).

sf-0/
  dataset-sf0-insert.xml    - test data for SF-0 (WFS Transaction)*
  dataset-sf0.pgdump        - PostgreSQL/PostGIS dump file
  dataset-sf0_postgis.sql   - PostgreSQL/PostGIS SQL script
  dataset-sf0_oracle.sql    - Oracle SQL script

sf-1/
  dataset-sf1-insert.xml    - additional test data for SF-1 (WFS Transaction)*


* NOTE: The Transaction request entities are provided as a convenience, but no 
assumptions are made about how the test data are loaded into the implementation 
under test.