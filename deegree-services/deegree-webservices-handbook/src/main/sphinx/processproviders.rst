.. _anchor-configuration-processproviders:

=================
Process providers
=================

Process providers plug geospatial processes into the :ref:`anchor-configuration-wps`.

The remainder of this chapter describes some relevant terms and the process provider configuration files in detail. You can access this configuration level by clicking on the **processes** link in the administration console. The corresponding configuration files are located in the **processes/** subdirectory of the active deegree workspace directory.

.. figure:: images/workspace-overview-process.png
   :figwidth: 80%
   :width: 80%
   :target: _images/workspace-overview-process.png

   Process providers plug geospatial processes into the WPS

---------------------
Java process provider
---------------------

A Java process provider injects processes written in the Java programming language. In order to set up a working Java process provider resource, two things are required:

* A Java process provider configuration file
* A Java class with the actual process code (a so-called ``Processlet``)

The first is an XML resource configuration file like any other deegree resource configuration. The second is special to this kind of resource. It provides the byte code with the process logic and has to be accessible by deegree's classloader. There are several options to make custom Java code available to deegree webservices (see :ref:`anchor-adding-jars` for details), but the most common options are:

* Putting class files into the ``classes/`` directory of the workspace
* Putting JAR files into the ``modules/`` directory of the workspace

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Minimal configuration example
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

A very minimal valid configuration example looks like this:

.. topic:: Java process provider: Minimal example (resource configuration)

   .. literalinclude:: xml/java_processprovider_minimal.xml
      :language: xml

This example defines a bogus process with the following properties:

* Identifier: ``Process42`` 
* Bound to Java code from class ``Processlet42``
* Title **Calculates the answer to life, the universe and everything** (returned in WPS responses)
* No input parameters
* Single output parameter with identifier ``Answer`` and title **The universal answer**

In order to make this configuration work, a matching Processlet is required:

.. topic:: Java process provider: Minimal example (Java code)

   .. literalinclude:: java/java_processprovider_minimal.java
      :language: java

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
More complex configuration example 
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

A more complex configuration example looks like this:

.. topic:: Java process provider: More complex example (resource configuration)

   .. literalinclude:: xml/java_processprovider_complex.xml
      :language: xml

^^^^^^^^^^^^^^^^^^^^^
Configuration options
^^^^^^^^^^^^^^^^^^^^^

The configuration format for the Java process provider is defined by schema file http://schemas.deegree.org/processes/java/3.0.0/java.xsd. The following table lists all available configuration options. When specifiying them, their order must be respected.

.. table:: Options for ``ProcessDefinition`` configuration files

+------------------+-------------+---------+------------------------------------------------------------------------------+
| Option           | Cardinality | Value   | Description                                                                  |
+==================+=============+=========+==============================================================================+
| @processVersion  | 1           | String  | Release version of this process (metadata)                                   |
+------------------+-------------+---------+------------------------------------------------------------------------------+
| @storeSupported  | 0..1        | Boolean | If set to true, asynchronous execution will become available                 |
+------------------+-------------+---------+------------------------------------------------------------------------------+
| @statusSupported | 0..1        | Boolean | If set to true, process code provides status information                     |
+------------------+-------------+---------+------------------------------------------------------------------------------+
| Identifier       | 1           | Complex | Identifier of the process                                                    |
+------------------+-------------+---------+------------------------------------------------------------------------------+
| JavaClass        | 1           | String  | Fully qualified name of the Java class that implements the process logic     |
+------------------+-------------+---------+------------------------------------------------------------------------------+
| Title            | 1           | Complex | Short and meaningful title (metadata)                                        |
+------------------+-------------+---------+------------------------------------------------------------------------------+
| Abstract         | 0..1        | Complex | Short, human readable description (metadata)                                 |
+------------------+-------------+---------+------------------------------------------------------------------------------+
| Metadata         | 0..n        | String  | Additional metadata                                                          |
+------------------+-------------+---------+------------------------------------------------------------------------------+
| Profile          | 0..n        | String  | Profile to which the WPS process complies (metadata)                         |
+------------------+-------------+---------+------------------------------------------------------------------------------+
| WSDL             | 0..1        | String  | URL of a WSDL document which describes this process (metadata)               |
+------------------+-------------+---------+------------------------------------------------------------------------------+
| InputParameters  | 0..1        | Complex | Definition and metadata of the input parameters                              |
+------------------+-------------+---------+------------------------------------------------------------------------------+
| OutputParameters | 1           | Complex | Definition and metadata of the output parameters                             |
+------------------+-------------+---------+------------------------------------------------------------------------------+

The following sections describe these options and their sub-options in detail.

^^^^^^^^^^^^^^^
General options
^^^^^^^^^^^^^^^

* ``processVersion``: The processVersion attribute has to be managed by the process developer and describes the version of the process implementation. This parameter is usually increased when changes to the implementation of a process apply. Reported by the WPS to clients.
* ``Identifier``: The Identifier element must contain an appropriate unambiguous identifier. Reported by the WPS to clients.
* ``Title``: Short and meaningful title. Reported by the WPS to clients.
* ``Abstract``: Short, human readable description. Reported by the WPS to clients.
* ``Metadata``: Additional metadata
* ``Profile``: Profile to which the WPS process complies. Reported by the WPS to clients.
* ``WSDL``: URL of a WSDL document which describes this process. Reported by the WPS to clients.

^^^^^^^^^^^^^^^^^^^^
The processlet class
^^^^^^^^^^^^^^^^^^^^

Option ``JavaClass`` specifies the fully qualified name of a Java class. This class has to implement deegree's ``Processlet`` Java interface (qualified name: ``org.deegree.services.wps.Processlet``):

.. topic:: Java process provider: Processlet interface

   .. literalinclude:: java/Processlet.java
      :language: java

As you can see, this interface defines three methods that every processlet must implement:

* ``init()``: Called once when the workspace initializes the Java process provider resource that references the class.
* ``destroy()``: Called once when the workspace destroys the Java process provider resource that references the class.
* ``process(...)``: Called every time an Execute request is sent to the WPS that targets this process. It usually reads the input parameters, performs the actual computation and writes the output parameters.

.. tip::
  The Java process provider instantiates the referenced process class only once. Multiple simultaneous executions of a process can occur (e.g. when parallel Execute-requests are sent to a WPS), and therefore, the process class must be implemented in a thread-safe manner. This behaviour is identical to the well-known Java Servlet interface (hence the name Processlet).

""""""""""""""""""""""
Processlet compilation
""""""""""""""""""""""

In order to succesfully compile a ``Processlet`` implementation, you will need to make the required dependencies available to the compile (such as deegree's ``Processlet`` interface). Generally, this means that the Java module ``deegree-services-wps`` (and it's dependencies) are on the build path of your Java compiler (or development environment). We suggest to use Apache Maven for this. Here's an example POM for your convenience:

.. topic:: Java process provider: Example for Maven POM for writing processlets

   .. literalinclude:: xml/java_processprovider_pom.xml
      :language: xml

.. tip::
  You can use this POM to compile the example Processlets above to create a JAR file that you can put into the ``modules`` directory of the deegree workspace. Just create an empty directory somewhere and save the Example POM as ``pom.xml``. Place the Processlet Java files into subdirectory ``src/main/java/`` (as files ``Processlet42.java`` / ``AdditionProcesslet.java``). On the command line, change to the project directory and use ``mvn package`` (Apache Maven 3.0 and a compatible Java JDK have to be installed). Subdirectory ``target`` should now contain a JAR file that you can copy into the ``modules`` directory of the deegree workspace. 

"""""""""""""""""""""""""""""""""""""""
Invoking processlets using WPS requests
"""""""""""""""""""""""""""""""""""""""

.. hint::
  In order to perform WPS request to access your processlet, you will need to have an active :ref:`anchor-configuration-wps` resource in your workspace (which handles the WPS protocol and forwards the request to the process provider and the processlet).

The general idea of the WPS specification is that a client connects to a WPS server and invokes processes offered by the server to perform a computation. However, in some cases, you may just want to send raw WPS requests to a server and check the response (e.g. for testing the behaviour of your processlet). The `WPS 1.0.0 specification <http://www.opengeospatial.org/standards/wps>`_ defines KVP, XML and SOAP-encoded requests. All encodings are supported by the deegree WPS, so you can choose the most appropriate one for your use-case. For sending KVP-requests, you can simply use your web browser (or command line tools like wget or curl). XML or SOAP requests can be send using deegree's generic client.

.. tip::
  :ref:`anchor-workspace-wps` contains XML example requests which demonstrate many of the features of the WPS protocol, such as input parameter passing (inline or by reference), return parameters (inline or by reference), response variants and asynchronous execution.
  
^^^^^^^^^^^^^^^^^^^^^^^^^^^
Input and output parameters
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Besides the process logic, the most crucial topic of Processlet implementation is the handling of input and output parameters. The deegree WPS and the Java process provider support all parameter types that are defined by the `WPS 1.0.0 specification <http://www.opengeospatial.org/standards/wps>`_ . There are three different types of input and output parameters:

* LiteralInput / LiteralOutput: Simple parameters with literal values, that are given as a simple string e.g. "red", "42", "highway 101"
* BoundingBoxInput / BoundingBoxOutput: A georeferenced bounding box given in a specified or a default CRS
* ComplexInput / ComplexOutput: Either an XML structure (e.g. GML encoded features) or binary data (e.g. coverage data as a GeoTIFF)

""""""""""""""""""""""""""""""""""""
Defining input and output parameters
""""""""""""""""""""""""""""""""""""

In order to create your own process, first find out which input and output parameters you want it to have. During implementation, each parameter has to be considered twice:

* In the resource configuration file (section ``InputParameters`` or ``OutputParameters``)
* In the ``process(..)`` method of your Processlet

The definition in the resource configuration is used to specify the metadata (identifier, title, abstract, datatype) of the parameter. The WPS will report it in response to ``DescribeProcess`` requests. When performing ``Execute`` requests, the deegree WPS will also perform a basic check of the validity of the input parameters (identifier, occurence, type) and issue an ``ExceptionReport`` if the constraints are not met.

"""""""""""""""""""""""""""""""""""""
Accessing input and output parameters
"""""""""""""""""""""""""""""""""""""

""""""""""""""""""""""""
Literal inputs / outputs
""""""""""""""""""""""""

""""""""""""""""""""""""""""
BoundingBox inputs / outputs
""""""""""""""""""""""""""""

""""""""""""""""""""""""
Complex inputs / outputs
""""""""""""""""""""""""

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Asynchronous execution and status information
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

* ``storeSupported``: When the storeSupported attribute is set to "true", asynchronous process execution will be available. Note that this doesn't add any requirements to the actual process code, this is taken care of by deegree automatically. See the advanced topics section for more information.
* ``statusSupported``: If statusSupported is set to true, the process class is declared to provide status information, i.e. execution percentage. See the advanced topics section for more information.



