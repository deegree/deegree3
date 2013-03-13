.. _anchor-configuration-processproviders:

=================
Process providers
=================

Process providers plug geospatial processes into the WPS.

The remainder of this chapter describes some relevant terms and the process provider configuration files in detail. You can access this configuration level by clicking on the **processes** link in the administration console. The configuration files are located in the **processes/** subdirectory of the active deegree workspace directory.

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
* A Java class with the actual process code

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
* Bound to Java code from class ``org.deegree.wps.Process42``
* Title **Calculates the answer to life the universe and everything** (returned in WPS responses)
* No input parameters
* Single output parameter with identifier ``Answer`` and title **The universal answer**

In order to make this configuration work, you will also need a matching Java class that provides the process code:

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
| @processVersion  | 1           | String  | Process version (metadata)                                                   |
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
| Profile          | 0..n        | String  | TBD                                                                          |
+------------------+-------------+---------+------------------------------------------------------------------------------+
| WSDL             | 0..1        | String  | TBD                                                                          |
+------------------+-------------+---------+------------------------------------------------------------------------------+
| InputParameters  | 0..1        | Complex | Definition and metadata of the input parameters                              |
+------------------+-------------+---------+------------------------------------------------------------------------------+
| OutputParameters | 1           | Complex | Definition and metadata of the output parameters                             |
+------------------+-------------+---------+------------------------------------------------------------------------------+

The remainder of this section describes these options and their sub-options in detail.

^^^^^^^^^^^^^^^
General options
^^^^^^^^^^^^^^^

* ``processVersion``: The processVersion attribute has to be managed by the process developer and describes the version of the process implementation. This parameter is usually increased when changes to the implementation of a process apply. Reported by the WPS to clients.
* ``Identifier``: The Identifier element must contain an appropriate unambiguous identifier. Reported by the WPS to clients.
* ``Title``: Short and meaningful title. Reported by the WPS to clients.
* ``Abstract``: Short, human readable description. Reported by the WPS to clients.
* ``Metadata``: Additional metadata
* ``Profile``: 
* ``WSDL``: 

^^^^^^^^^^^^^^^^^^^^^^
The Java process class
^^^^^^^^^^^^^^^^^^^^^^

Option ``JavaClass`` specifies the fully qualified name of the Java class that implements the process logic. This class has to implement deegree's ``Processlet`` Java interface which is part of the deegree framework (qualified name: ``org.deegree.services.wps.Processlet``):

.. topic:: Java process provider: Processlet interface

   .. literalinclude:: java/Processlet.java
      :language: java

As you can see, the interface defines three methods that every process class must implement:

* ``init()``: Called once when the workspace initializes the Java process provider resource that references the class.
* ``destroy()``: Called once when the workspace destroys the Java process provider resource that references the class.
* ``process(...)``: Called every time an Execute request is sent to the WPS that targets this process. It usually reads the input parameters, performs the actual computation and writes the output parameters.

.. tip::
  The Java process provider instantiates the referenced process class only once. Multiple simultaneous executions of a process can occur (e.g. when parallel Execute-requests are sent to a WPS), and therefore, the process class must be implemented in a thread-safe manner. This behaviour is identical to the well-known Java Servlet interface.

^^^^^^^^^^^^^^^^^^^^^^^^^^^
Input and output parameters
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Besides the process logic, the most complex topic of process implementation is dealing with input and output parameters. The deegree WPS and the Java process provider support all parameter types that are defined by the WPS 1.0.0 specification. The specification permits three different types of input and output parameters:

* LiteralInput / LiteralOutput
* BoundingBoxInput / BoundingBoxOutput
* ComplexInput / ComplexOutput

LiteralInput is used for simple input parameters with literal values, that are given as a simple string e.g. "red", "42", "highway 101" (no nested XML fragments or similarly complex inputs). A BoundingBoxInput specifies a certain bounding box given in a specified or a default CRS. The content of a ComplexInput can be a complex XML structure as well as binary data (must be base64-encoded when given as an inline value).

""""""""""""""""""""""""""""""""""""
Defining input and output parameters
""""""""""""""""""""""""""""""""""""

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



