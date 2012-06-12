.. _anchor-lightly:

===============
Getting started
===============

In the previous chapter, you learned how to install and start deegree webservices. In this chapter, we will introduce the service console and learn how to perform basic tasks such as downloading and activating example configurations. In deegree terminology, a configuration for a deegree instance is called "deegree workspace". A workspace is a well-defined directory of configuration files that defines the different aspects of a deegree instance (active web services, data access, layers, etc). The following chapter describes the inner structure of a deegree workspace, but for the remainder of this chapter, it is sufficient to understand that it is the configuration of a deegree webservice instance.

-------------------------------
Using deegree's service console
-------------------------------

The service console is a web-based administration interface for configuring your deegree webservices installation. If deegree webservices are running on your machine, you can usually access the console from your browser via http://localhost:8080

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   deegree webservices console

.. tip::
  If you're not running the ZIP version, but deployed the WAR version into a web container, you most probably will have to use a different URL for accessing the console, e.g. http://localhost:8080/deegree-webservices-3.2-pre8. The port number and webapp name depend on your installation/deployment.

.. tip::
  You can access the service console from other machines on your network by exchanging *localhost* with the name or IP address of the machine that runs the deegree webservices.

Note that most links are grayed out, except for the following ones:

* **Modules**: See installed deegree modules and versions
* **send requests**: Access a simple user interface for sending raw OGC XML requests to the service (useful for testing). Until you activate some services, this is not be very useful, though.
* **see layers**: Shows the layers provided by the WMS in a simple web client. Until you activate the WMS, only an OpenStreetMap base layer is available.
* **deegree Wiki**: External link to the deegree wiki with information on deegree (currently a bit outdated, better cling to the handbook)

These links do not allow to change any settings of your deegree installation. In order to gain access to the administration tasks, you need to login first. 

^^^^^^^^^^
Logging in
^^^^^^^^^^

The default password is **deegree**. Enter it into the password field and click 'Log In'. The view will change now and give access to all configuration sections:

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   Logged in

The links on the left allow to configure different configuration aspects of your installation. Most of them will be introduced in the next chapter. In the remainder of this chapter, the relevant sections are:

* workspace: Download and activate example configurations
* proxy: Control proxy settings that deegree uses for accessing the internet

.. tip::
  If the machine running deegree webservices uses a proxy to access the internet and you have trouble downloading example configurations, you will probably have to configure the proxy settings. Ask your network administrator for details.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Activating example configurations
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Click on the *workspace* link:

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   Workspace section

The bottom of the workspace view lists the available example configurations.

.. _anchor-workspace-utah:

-------------------------------------
Example config 1: Webmapping for Utah
-------------------------------------

.. tip::
  Instead of using the built-in layer preview or the generic XML client, you may use any compliant OGC client for accessing the WMS and WFS. Successfully tested desktop clients include Quantum GIS (install WFS plugin for accessing WFS), uDig, OpenJUMP and deegree iGeoDesktop. The service address to enter in your client is: http://localhost:8080/services.

.. _anchor-workspace-inspire:

-----------------------------------
Example config 2: INSPIRE in action
-----------------------------------

.. _anchor-workspace-csw:

----------------------------------------
Example config 3: An ISO catalogue setup
----------------------------------------

.. _anchor-workspace-wps:

------------------------------------
Example config 4: Processing service
------------------------------------


