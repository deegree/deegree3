------------- INFO -------------
This file is part of deegree.
For copyright/license information, please visit http://www.deegree.org/license.
--------------------------------

This folder is needed for download files. Please do not delete it.
The contents of this folder are created with the download button in toolbar module.

You may rename this folder. But then you also need to rename the references to this folder 
in each and every web map context file of your iGeoPortal instance.

    <Extension xmlns:deegree="http://www.deegree.org/context">
        <deegree:IOSettings>
            ...
            <deegree:DownloadDirectory>
                <!-- relative path to the folder referenced in the DownloadServlet of igeoportals web.xml -->
                <deegree:Name>../../../../downloads</deegree:Name>
                <deegree:Access>
                    <OnlineResource xlink:type="simple" xlink:href="http://localhost:8080/igeoportal-std/" />
                </deegree:Access>
            </deegree:DownloadDirectory>
            ...
        </deegree:IOSettings>
    </Extension>

You will also need to adjust the path referenced in the DownloadServlet of igeoportals web.xml.
