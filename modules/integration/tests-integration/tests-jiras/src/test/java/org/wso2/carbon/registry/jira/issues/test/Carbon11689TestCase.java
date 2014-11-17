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
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.greg.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.greg.integration.common.utils.GREGIntegrationBaseTest;
import org.wso2.greg.integration.common.utils.RegistryProviderUtil;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Date;

public class Carbon11689TestCase extends GREGIntegrationBaseTest {

    private Registry governance;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String pathPrefix = "/_system/governance";
    private RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
    private ServiceManager serviceManager;
    private String serviceId;
    private String servicePath;
    private WSRegistryServiceClient wsRegistryServiceClient;
    Date lastModified;

    @BeforeClass
    public void init() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String sessionCookie = getSessionCookie();


        wsRegistryServiceClient = registryProviderUtil.getWSRegistry(automationContext);

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, sessionCookie);
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        governance = registryProviderUtil.getGovernanceRegistry(wsRegistryServiceClient, automationContext);

    }

    @Test(groups = {"wso2.greg"}, description = "add a service")
    public void testAddService() throws RegistryException {
        serviceId = addService("test_name1", "test_namespace1", "1.0.0");

    }

    @Test(groups = {"wso2.greg"}, description = "modify service", dependsOnMethods = {"testAddService"})
    public void testModifyService() throws RegistryException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry)governance,
                GovernanceUtils.findGovernanceArtifactConfigurations(governance));
        Service service = serviceManager.getService(serviceId);
        service.setAttribute("overview_description", "modified");
        serviceManager.updateService(service);
        lastModified = wsRegistryServiceClient.get(pathPrefix + servicePath).getLastModified();
    }

    @Test(groups = {"wso2.greg"}, description = "modify service", dependsOnMethods = {"testModifyService"})
    public void testVerifyUpdatedTime() throws MalformedURLException, RegistryException {
        serviceManager.getAllServices();
        Date lastModifiedDate = wsRegistryServiceClient.get(pathPrefix + servicePath).getLastModified();
        Assert.assertEquals(lastModified, lastModifiedDate);

    }

    public String addService(String serviceName, String namespace, String version) throws RegistryException {
        serviceManager = new ServiceManager(governance);
        Service service;
        service = serviceManager.newService(new QName(namespace, serviceName));
        service.addAttribute("overview_version", version);
        serviceManager.addService(service);
        servicePath = service.getPath();
        return service.getId();
    }

    @AfterClass(alwaysRun = true)
    public void clean() throws Exception {

        Service[] services = serviceManager.getAllServices();

        for(Service service: services){
            if(service.getQName().getLocalPart().contains("test_name1")) {
                serviceManager.removeService(service.getId());
            }
        }

        governance = null;
        registryProviderUtil = null;
        resourceAdminServiceClient = null;
        serviceManager = null;
        wsRegistryServiceClient = null;
    }

    public void delete(String destPath) throws ResourceAdminServiceExceptionException, RemoteException,
            RegistryException {
        if(wsRegistryServiceClient.resourceExists(destPath)){
            resourceAdminServiceClient.deleteResource(destPath);
        }
    }


}
