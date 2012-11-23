.. _anchor-configuration-serverconnections:

==================
Server connections
==================

Server connections are used to configure parameters necessary to connect other resources to a server. A server can be anything from a database server to another OGC webservice.

There are currently two types of server connections, JDBC connections (to connect to a database) and remote OWS connections (to connect to other OGC webservices).

.. _anchor-configuration-jdbc:

----------------
JDBC connections
----------------

TBD

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

^^^^^^^^^^^^^^^^^^^^^
Remote WFS connection
^^^^^^^^^^^^^^^^^^^^^

TBD

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

