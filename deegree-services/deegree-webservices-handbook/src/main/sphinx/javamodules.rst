.. _anchor-configuration-javamodules:

==========================
Java modules and libraries
==========================

deegree webservices is a Java web application, and therefore the standard means of adding Java libraries to the classpath apply (e.g. you can add JARs to ``WEB-INF/lib`` of the deegree webapp directory). Alternatively, you may add your JARs to the ``modules/`` directory of your deegree workspace. This can be handy, as it allows to ship a self-contained workspace (no fiddling with other directories required) and also has the benefit the you can reload the deegree workspace only after adding your libraries (instead of restarting the deegree webapp or the whole web application container).

The remainder of this chapter describes to common use-cases.

.. _anchor-oraclejars:

^^^^^^^^^^^^^^^^^^^^^^^^^^
Adding Oracle JDBC drivers
^^^^^^^^^^^^^^^^^^^^^^^^^^

The following deegree modules support connecting to Oracle Spatial databases (10g, 11g):

* SimpleSQLFeatureStore
* SQLFeatureStore
* ISOMetadataStore

However, for copyright reasons, deegree webservices cannot ship the required Oracle JDBC driver. In order to enable Oracle connectivity, you need to add a compatible Oracle JDBC6-type driver (e.g. ``ojdbc6-11.2.0.2.jar``) to the classpath  as discussed in the introduction of this chapter. Reload your deegree workspace (or the webapp/web application container if you added it by some other means than putting it into the ``modules/`` directory).

Additionally, you need to add module deegree-sqldialect-oracle, which can be downloaded at:

^^^^^^^^^^^^^^^^^^^^^^^^^
Code for custom processes
^^^^^^^^^^^^^^^^^^^^^^^^^


