.. _anchor-configuration-serverconnections:

==================
Server connections
==================

Server connections are workspace resources that provide connections to remote services. These connections can then be used by other workspace resources. Some common example use cases:

* JDBC connection: Used by SQL feature stores to access the database that stores the feature data
* JDBC connection: Used by SQL ISO metadata stores to access the database that stores the metadata records
* WMS connection: Used by remote WMS layers to access remote WMS
* WMS connection: Used by remote WMS tile stores to access remote WMS
* WMTS connection: Used by remote WMTS tile stores to access remote WMTS

There are currently two categories of server connection resources, JDBC connections (to connect to SQL databases) and remote OWS connections (to connect to other OGC webservices).

.. figure:: images/workspace-overview-connection.png
   :figwidth: 80%
   :width: 80%
   :target: _images/workspace-overview-connection.png

   Server connection resources define how to obtain a connection to a remote server

.. _anchor-configuration-jdbc:

----------------
JDBC connections
----------------

JDBC connections define connections to SQL databases. Here's an example that connects to a PostgreSQL database on localhost, port 5432. The database to connect to is called 'inspire'
, the database user is 'postgres' and password is 'postgres'.

.. code-block:: xml

  <JDBCConnection configVersion="3.0.0" xmlns="http://www.deegree.org/jdbc" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                  xsi:schemaLocation="http://www.deegree.org/jdbc http://schemas.deegree.org/jdbc/3.0.0/jdbc.xsd">
    <Url>jdbc:postgresql://localhost:5432/inspire</Url>
    <User>postgres</User>
    <Password>postgres</Password>
  </JDBCConnection>

The JDBC connection config file format is defined by schema file http://schemas.deegree.org/jdbc/3.0.0/jdbc.xsd. The root element is ``JDBCConnection`` and the config attribute must be ``3.0.0``. The following table lists all available configuration options. When specifiying them, their order must be respected.

.. table:: Options for ``JDBCConnection``

+----------+-------------+--------+----------------------------------------+
| Option   | Cardinality | Value  | Description                            |
+==========+=============+========+========================================+
| Url      | 1..1        | String | JDBC URL (without username / password) |
+----------+-------------+--------+----------------------------------------+
| User     | 1..n        | String | DB username                            |
+----------+-------------+--------+----------------------------------------+
| Password | 1..1        | String | DB password                            |
+----------+-------------+--------+----------------------------------------+

.. hint::
   By default, deegree webservices includes JDBC drivers for connecting to PostgreSQL and Derby databases. If you want to make a connection to other SQL databases (e.g. Oracle), you will need to add a compatible JDBC driver manually. This is described in :ref:`anchor-oraclejars`.

----------------------
Remote OWS connections
----------------------

Remote OWS connections are typically configured with a capabilities document reference and optionally some HTTP request parameters (such as timeouts etc.). Contrary to earlier experiments these resources only define the actual connection to the service, not what is requested. This resource is all about *how* to request, not *what* to request. Other resources (such as a remote WMS tile store) which make use of such a server connection typically define *what* to request.

^^^^^^^^^^^^^^^^^^^^^
Remote WMS connection
^^^^^^^^^^^^^^^^^^^^^

The remote WMS connection can be used to connect to OGC WMS services. Versions 1.1.1 and 1.3.0 (with limitations) are supported.

Let's have a look at an example:

.. code-block:: xml

  <RemoteWMS xmlns="http://www.deegree.org/remoteows/wms" configVersion="3.1.0">
    <CapabilitiesDocumentLocation
      location="http://deegree3-demo.deegree.org/utah-workspace/services?request=GetCapabilities&amp;service=WMS&amp;version=1.1.1" />
    <ConnectionTimeout>10</ConnectionTimeout>
    <RequestTimeout>30</RequestTimeout>
    <HTTPBasicAuthentication>
      <Username>hans</Username>
      <Password>moleman</Password>
    </HTTPBasicAuthentication>
  </RemoteWMS>

* The capabilities document location is the only mandatory option. You can also use a relative path to a local copy of the capabilities document to improve startup time.
* The connection timeout defines (in seconds) how long to wait for a connection before throwing an error. Default is 5 seconds.
* The request timeout defines (in seconds) how long to wait for data before throwing an error. Default is 60 seconds.
* The http basic authentication options can be used to provide authentication credentials to use a HTTP basic protected service. Default is not to authenticate.

The WMS version will be detected from the capabilities document version. When using 1.3.0, there are some limitations (eg. GetFeatureInfo is not supported), and it is tested to a lesser extent compared with the 1.1.1 version.

^^^^^^^^^^^^^^^^^^^^^^
Remote WMTS connection
^^^^^^^^^^^^^^^^^^^^^^

The remote WMTS connection can be used to connect to a OGC WMTS service. Version 1.0.0 is supported. The configuration format is almost identical to the remote WMS configuration.

Let's have a look at an example:

.. code-block:: xml

  <RemoteWMTS xmlns="http://www.deegree.org/remoteows/wmts" configVersion="3.2.0">
    <CapabilitiesDocumentLocation
      location="http://deegree3-testing.deegree.org/utah-workspace/services?request=GetCapabilities&amp;service=WMTS&amp;version=1.0.0" />
    <ConnectionTimeout>10</ConnectionTimeout>
    <RequestTimeout>30</RequestTimeout>
    <HTTPBasicAuthentication>
      <Username>hans</Username>
      <Password>moleman</Password>
    </HTTPBasicAuthentication>
  </RemoteWMTS>

* The capabilities document location is the only mandatory option. You can also use a relative path to a local copy of the capabilities document to improve startup time.
* The connection timeout defines (in seconds) how long to wait for a connection before throwing an error. Default is 5 seconds.
* The request timeout defines (in seconds) how long to wait for data before throwing an error. Default is 60 seconds.
* The http basic authentication options can be used to provide authentication credentials to use a HTTP basic protected service. Default is not to authenticate.

Only GetTile operations are supported for remote WMTS resources.

