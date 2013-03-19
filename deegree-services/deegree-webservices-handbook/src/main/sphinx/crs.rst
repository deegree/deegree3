.. _anchor-configuration-crs:

============================
Coordinate reference systems
============================

Coordinate reference system identifiers are used in many places in deegree webservices:

* In incoming service requests (e.g. ``GetFeature``-requests to the WFS)
* In a lot of resource configuration files (e.g. in :ref:`anchor-configuration-featurestore`)

deegree has an internal CRS database that contains many commonly used coordinate reference systems. Some examples for valid CRS identifiers:

* ``EPSG:4258``
* ``http://www.opengis.net/gml/srs/epsg.xml#4258``
* ``urn:ogc:def:crs:epsg::4258``
* ``urn:opengis:def:crs:epsg::4258``

.. tip::
  As a rule of thumb, deegree's CRS database uses the ``EPSG:12345`` identifier variant to indicate XY axis order, while the URN variants (such as ``urn:ogc:def:crs:epsg::12345``) always use the official axis order defined by the EPSG. For example ``EPSG:4258`` and ``urn:ogc:def:crs:epsg::4258`` both refer to ETRS89, but ``EPSG:4258`` means ETRS89 in XY-order, while ``urn:ogc:def:crs:epsg::4258`` is YX (the official order defined by the EPSG for this CRS).

.. note::
  The CRS subsystem is not fully integrated with the deegree workspace yet. Rework and proper documentation are on the roadmap for one of the next releases. If you have trouble finding a specific CRS, please `contact the deegree mailing lists for support <http://www.deegree.org/Community>`_.






