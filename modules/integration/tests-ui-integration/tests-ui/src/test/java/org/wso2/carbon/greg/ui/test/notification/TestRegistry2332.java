/*
*Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.greg.ui.test.notification;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.notififcations.ManageNotificationPage;
import org.wso2.greg.integration.common.ui.page.notififcations.NotificationHome;
import org.wso2.greg.integration.common.ui.page.resource.ResourceHome;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;
import org.wso2.carbon.automation.engine.context.beans.User;

public class TestRegistry2332 extends GREGIntegrationUIBaseTest {

    private WebDriver driver;
    private User userInfo;
    private WebDriverWait driverWait;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        userInfo = automationContext.getContextTenant().getContextUser();
        driver = BrowserManager.getWebDriver();
        driverWait = new WebDriverWait(driver, 5);
        driver.get(getLoginURL());
    }

    @Test(groups = "wso2.greg", description = "verify adding notification is successful")
    public void testLogin() throws Exception {
        LoginPage test = new LoginPage(driver);
        test.loginAs(userInfo.getUserName(), userInfo.getPassword());
    }

    @Test(groups = "wso2.greg", description = "adding a subscription to root element",
            dependsOnMethods = { "testLogin" })
    public void testAddSubscription() throws Exception {
        NotificationHome notificationHome = new NotificationHome(driver);
        ManageNotificationPage manageNotificationPage = new ManageNotificationPage(driver);

        driver.findElement(By.xpath("//div[@id=\"subscriptionOptionTable\"]/a")).click();

        new Select(this.driver.findElement(By.id("eventList"))).selectByVisibleText("Update");
        new Select(this.driver.findElement(By.id("notificationMethodList"))).selectByVisibleText("Management Console");
        driver.findElement(By.id("subscriptionInput")).sendKeys(new CharSequence[] { "admin" });

        driver.findElement(By.id("subscribeButton")).click();

        driver.findElement(By.xpath("//div[@id=\"menu-panel-button1\"]/span")).click();
    }

    @Test(groups = "wso2.greg", description = "adding a subscription to root element",
            dependsOnMethods = { "testAddSubscription" })
    public void testGenerateNotification() throws Exception {
        driverWait.until(ExpectedConditions.visibilityOfElementLocated(By
                .xpath("//ul[@class=\"main\"]/li[contains(text(), \"Resources\")]")));
        ResourceHome resourceHome = new ResourceHome(driver);

        driver.findElement(By.id("propertiesIconMinimized")).click();
        driver.findElement(By.xpath("//div[@id=\"propertiesExpanded\"]/div/a")).click();

        driver.findElement(By.id("propName")).sendKeys(new CharSequence[] { "propName" });
        driver.findElement(By.id("propValue")).sendKeys(new CharSequence[] { "propValue" });

        driver.findElement(By.xpath("//div[@id=\"propertiesAddDiv\"]/form/table/tbody/tr/" +
                "td[@class=\"buttonRow\"]/input[@value=\"Add\"]")).click();
    }

    @Test(groups = "wso2.greg", description = "adding a subscription to root element",
            dependsOnMethods = { "testGenerateNotification" })
    public void testHideNotification() throws Exception {
        //refresh page to load management console notifications
        driver.findElement(By.xpath("//a[@class=\"registry-breadcrumb\"]")).click();

        driver.findElement(By.id("viewNotification")).click();
        driver.findElement(By.xpath("//li[@class=\"notificationCell3\"]/input[@value=\"Hide\"]")).click();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.findElement(By.xpath("//div[@id=\"menu-panel-button1\"]/span")).click();
        driverWait.until(ExpectedConditions.visibilityOfElementLocated(By
                .xpath("//ul[@class=\"main\"]/li[contains(text(), \"Resources\")]")));

        NotificationHome notificationHome = new NotificationHome(driver);
        ManageNotificationPage manageNotificationPage = new ManageNotificationPage(driver);

        //Delete added subscription
        driver.findElement(By.xpath("//td/a[contains(text(), \"Delete\")]")).click();
        driver.findElement(By.xpath("//div[@class=\"ui-dialog-buttonpane\"]/button[contains(text(), \"Yes\")]"))
                .click();

        driver.findElement(By.xpath("//div[@id=\"menu-panel-button1\"]/span")).click();
        driverWait.until(ExpectedConditions.visibilityOfElementLocated(By
                .xpath("//ul[@class=\"main\"]/li[contains(text(), \"Resources\")]")));

        ResourceHome resourceHome = new ResourceHome(driver);

        //Delete added property
        driver.findElement(By.id("propertiesIconMinimized")).click();
        driver.findElement(By.xpath("//tr[@id=\"propViewPanel_0\"]/td/a[contains(text(), \"Delete\")]")).click();
        driver.findElement(By.xpath("//div[@class=\"ui-dialog-buttonpane\"]/button[contains(text(), \"Yes\")]"))
                .click();

        driver.close();
        driver.quit();
    }

}