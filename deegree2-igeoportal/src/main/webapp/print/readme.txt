------------- INFO -------------
This file is part of deegree.
For copyright/license information, please visit http://www.deegree.org/license.
--------------------------------

This folder is needed for print files. Please do not delete it.

You may rename this folder. But then you also need to rename the references to this folder 
in each and every web map context file of your iGeoPortal instance.

	<Extension xmlns:deegree="http://www.deegree.org/context">
        <deegree:IOSettings>
            <deegree:PrintDirectory>
                <deegree:Name>../../../print</deegree:Name>
                <deegree:Access>
                    <OnlineResource xlink:type="simple"
                        xlink:href="http://127.0.0.1:8080/igeoportal-std/print" />
                </deegree:Access>
            </deegree:PrintDirectory>
            ...
        </deegree:IOSettings>
    </Extension>
