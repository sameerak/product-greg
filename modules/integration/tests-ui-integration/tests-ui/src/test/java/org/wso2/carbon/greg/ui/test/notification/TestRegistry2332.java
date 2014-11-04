

package org.wso2.carbon.greg.ui.test.notification;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.greg.integration.common.ui.page.LoginPage;
import org.wso2.greg.integration.common.ui.page.main.HomePage;
import org.wso2.greg.integration.common.ui.page.notififcations.ManageNotificationPage;
import org.wso2.greg.integration.common.ui.page.notififcations.NotificationHome;
import org.wso2.greg.integration.common.ui.page.resource.ResourceHome;
import org.wso2.greg.integration.common.utils.GREGIntegrationUIBaseTest;
import org.wso2.carbon.automation.engine.context.beans.User;

public class TestRegistry2332 extends GREGIntegrationUIBaseTest {

    private WebDriver driver;
    private User userInfo;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        userInfo = automationContext.getContextTenant().getContextUser();
        driver = BrowserManager.getWebDriver();
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
        driver.findElement(By.xpath("//a[@class=\"registry-breadcrumb\"]")).click();

        driver.findElement(By.id("viewNotification")).click();
        driver.findElement(By.xpath("//li[@class=\"notificationCell3\"]/input[@value=\"Hide\"]")).click();

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.findElement(By.xpath("//div[@id=\"menu-panel-button1\"]/span")).click();

        NotificationHome notificationHome = new NotificationHome(driver);
        ManageNotificationPage manageNotificationPage = new ManageNotificationPage(driver);

        driver.findElement(By.xpath("//td/a[contains(text(), \"Delete\"]")).click();
        driver.findElement(By.xpath("//div[@class=\"ui-dialog-buttonpane\"]/button[contains(text(), \"Yes\")]"))
                .click();

        driver.findElement(By.xpath("//div[@id=\"menu-panel-button1\"]/span")).click();

        ResourceHome resourceHome = new ResourceHome(driver);

        driver.findElement(By.id("propertiesIconMinimized")).click();
        driver.findElement(By.xpath("//div[@id=\"propViewPanel_0\"]/td/a[contains(text(), \"Delete\")]")).click();
        driver.findElement(By.xpath("//div[@class=\"ui-dialog-buttonpane\"]/button[contains(text(), \"Yes\")]"))
                .click();

        driver.close();
        driver.quit();
    }

}