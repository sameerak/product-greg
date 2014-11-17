/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.registry.jira.issues.test;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.integration.common.utils.FileManager;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.SearchAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

public class Carbon9147TestCase extends GREGIntegrationBaseTest {

    private Registry governance;
    private SearchAdminServiceClient searchAdminServiceClient;


    @BeforeClass
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient registry = registryProviderUtil.getWSRegistry(automationContext);
        governance = registryProviderUtil.getGovernanceRegistry(registry, automationContext);
        searchAdminServiceClient =
                new SearchAdminServiceClient(backendURL, getSessionCookie());

        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("echo.wsdl")) {
                wsdlManager.removeWsdl(wsdl.getId());
            }
        }

        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance,
                GovernanceUtils.findGovernanceArtifactConfigurations(governance));
        addWSDL();
    }


    @Test(groups = {"wso2.greg"}, description = "Search by Resource Name 'echo.wsdl'")
    public void searchByResourceName()
            throws SearchAdminServiceRegistryExceptionException, RemoteException,
                   RegistryException {


        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName("echo.wsdl");
        ArrayOfString[] paramList = paramBean.getParameterList();
        searchQuery.setParameterValues(paramList);
        paramList = null;
        AdvancedSearchResultsBean result = searchAdminServiceClient.getAdvancedSearchResults(searchQuery);

        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertEquals(result.getResourceDataList().length, 1, "WSDL is added more than once");

    }


    @AfterClass
    public void RemoveWSDL() throws RegistryException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl[] wsdls = wsdlManager.getAllWsdls();
        for (Wsdl wsdl : wsdls) {
            if (wsdl.getQName().getLocalPart().equals("echo.wsdl")) {
                wsdlManager.removeWsdl(wsdl.getId());
            }
        }

        ServiceManager serviceManager = new ServiceManager(governance);
        Service[] services = serviceManager.getAllServices();
        for (Service s : services) {
            if (s.getQName().getLocalPart().equals("echoyuSer1")) {
                serviceManager.removeService(s.getId());
            }
        }

        governance = null;
        searchAdminServiceClient = null;

    }


    public void addWSDL()
            throws IOException, ResourceAdminServiceExceptionException, RegistryException {
        Wsdl wsdl;
        WsdlManager wsdlManager = new WsdlManager(governance);

        String wsdlFilePath = getTestArtifactLocation() + "artifacts" + File.separator +
                              "GREG" + File.separator + "wsdl" + File.separator + "echo.wsdl";
        wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFilePath).getBytes(), "echo.wsdl");
        wsdlManager.addWsdl(wsdl);
    }

}
