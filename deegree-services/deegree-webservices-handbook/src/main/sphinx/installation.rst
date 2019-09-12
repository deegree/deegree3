.. _anchor-installation:

============
Installation
============

-------------------
System requirements
-------------------

deegree webservices work on any platform with a compatible Java SE 8 installation, including:

* Microsoft Windows
* Linux
* Mac OS X
* Solaris

Supported Java SE 8 versions are `Oracle JDK 8 <http://www.oracle.com/technetwork/java/javase/downloads/index.html>`_ [#f1]_, `OpenJDK 8 <http://openjdk.java.net>`_ [#f2]_. Newer Java SE versions may work, but are not officially supported by the deegree development team.

-----------
Downloading
-----------

deegree webservices downloads are available on the `deegree home page <http://www.deegree.org>`_. You have the choice between:

* *Docker* : Docker Image with deegree webservices on OpenJDK and Apache Tomcat [#f3]_
* *WAR*: Generic Java Web Archive for deployment in an existing Java Servlet container [#f4]_
* *ZIP*: Distribution bundle with Apache Tomcat [#f5]_

.. tip::
  If you are confused by the two options and unsure which version to pick, use the ZIP. Both variants contain exactly the same deegree software, they only differ in packaging.

---------------------
Starting and stopping
---------------------

In order to run the ZIP version, extract it into a directory of your choice. Afterwards, fire up the included start script for your operating system:

* Microsoft Windows: ``start-deegree-windows`` 
* Linux/Solaris: ``start-deegree-linux.sh`` (when starting via a Desktop Environment such as Gnome, choose "Run in terminal")
* Mac OS X: ``start-deegree-osx.cmd``

You should now see a terminal window on your screen with a lot of log messages: 

.. figure:: images/terminal.png
   :figwidth: 60%
   :width: 50%
   :target: _images/terminal.png

   deegree webservices starting up

.. tip::
  If you don't see this terminal window, make sure that the ``java`` command is on the system path. You can verify this by entering ``java -version`` at the command prompt. Also ensure that ``JAVA_HOME`` system environment variable points to the correct installation directory of a compatible JDK.

You may minimize this window, but don't close it as long as you want to be able to use the deegree webservices. In order to check if the services are actually running, open http://localhost:8080 in your browser. You should see the following page:

.. figure:: images/console_start.png
   :figwidth: 60%
   :width: 50%
   :target: _images/console_start.png

   deegree webservices administration console

To shut deegree webservices down, switch back to the terminal window and press ``CTRL+C`` or simply close it. 

.. tip::
  If you want to run deegree webservices on system startup automatically, consider installing `Apache Tomcat 8 <http://tomcat.apache.org>`_ as a system service. Afterwards, download the WAR version of deegree webservices and deploy it into your Tomcat installation (e.g. by copying the WAR file into the ``webapps`` folder). Consult the `Tomcat documentation <https://tomcat.apache.org/tomcat-8.5-doc/index.html>`_ for more information and options.

-----------------
Securing deegree
-----------------
Most weaknesses in deegree come from incorrect or inappropriate configuration. It is nearly always possible to make
deegree more secure than the default out of the box configuration. The following documents best practices and recommendations on
securing a production deegree server, whether it be hosted on a Windows or Unix based operating system.

__________________
Software Versions
__________________

The first step is to make sure you are running the latest stable releases of software:

* Operating System including the latest updates and security patches
* Java Runtime Environment (JRE) or JDK
* Apache Tomcat, Jetty or your preferred Java Servlet container
* Third-party libraries such as GDAL, JDBC driver, and
* deegree webservices itself.

.. tip::
    If you are running Apache Tomcat we recommend that you read and apply all recommendations as documented in `Apache Tomcat Security Considerations <https://tomcat.apache.org/tomcat-8.5-doc/security-howto.html>`_.

______________
Encryption
______________

When operating deegree in a production environment enable HTTPS with SSL or TLS. Either enable HTTPS on your Java Servlet
Container or operate it behind a web server such as Apache httpd oder NGINX.

.. tip::
    If you are running Apache Tomcat read the `SSL HowTo <http://tomcat.apache.org/tomcat-8.5-doc/ssl-howto.html>`_.

______________________________________
Securing deegree console and REST API
______________________________________
It is as a huge security problem to operate the deegree web app without setting a password for the deegree console.
How to set the password for the deegree console is described in :ref:`anchor-configuration-basics`.
The same applies to the deegree REST API. Since both transfer the credentials as clear text (with a little bit of
obscurity) it is highly recommended to enable encryption on the protocol level as described above!
For further information how to protect the deegree REST API read more in :ref:`anchor-configuration-restapi`.
You should also consider to limit the access to both resources. Apply a filter by IP or hostname to only allow a subset
of machines to connect and access the deegree console and REST API.

.. warning::
    The deegree console provides access to the server file system. Therefore you must not operate the Java Servlet container as root user! Furthermore you should consider to enable the Java Security Manager and define restrictive file permissions. [#f6]_

.. rubric:: Footnotes

.. [#f1] Oracle JDK 7 and earlier versions are not supported anymore, be aware that those versions are out of maintenance and reached End-of-life.
.. [#f2] OpenJDK binaries are provided by `Azul Systems <https://www.azul.com/downloads/zulu/>`_ or `AdoptOpenJDK <https://adoptopenjdk.net>`_.
.. [#f3] Requires an installation of Docker Community or Enterprise Edition, download Docker from `www.docker.com <https://www.docker.com/>`_.
.. [#f4] A Java Servlet 2.5 compliant container is required. We recommend using the latest `Apache Tomcat 8 <http://tomcat.apache.org/>`_ release.
.. [#f5] As of deegree 3.4.0 the ZIP distribution bundle is deprecated and the download links are removed from the website. Download the ZIP from the `Nexus repository <http://repo.deegree.org/content/groups/public/org/deegree/deegree-webservices-tomcat-bundle/>`_ instead.
.. [#f6] How to run securely Java applications we recommend to follow the `Java Security Guidelines <https://docs.oracle.com/javase/8/docs/technotes/guides/security/index.html>`_ and for `Apache Tomcat the Security Manager HowTo <http://tomcat.apache.org/tomcat-8.5-doc/security-manager-howto.html>`_.