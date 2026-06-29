package tests;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class LoginTest {

    private static Playwright playwright;
    private static Browser browser;
    private static BrowserContext context;
    private static Page page;
    private String resultsPrefix;

    @BeforeAll
    static void setupForAll(){
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setArgs(List.of("--start-maximized")));
    }

    @AfterAll
    static void wrapUpForAll(){
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void setupForEach(TestInfo testInfo){
        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(null));
        context.tracing().start(
                new Tracing.StartOptions()
                        .setScreenshots(true)
                        .setSnapshots(true)
                        .setSources(true)
        );

        page = context.newPage();
        page.navigate("https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");
        resultsPrefix = testInfo.getDisplayName();
    }

    @AfterEach
    void wrapUpForEach(){
        context.tracing().stop(
                new Tracing.StopOptions()
                        .setPath(Paths.get(resultsPrefix.replace(' ','_') + ".zip")));
        page.close();
        context.close();
    }

    @Test
    @DisplayName("Verify user with valid credentials can log in successfully.")
    void testValidLogin(){
        String username = "Admin";
        String password = "admin123";

        takeScreenshot(page, resultsPrefix);
        page.getByPlaceholder("Username").fill(username);
        page.getByPlaceholder("Password").fill(password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login")).click();

        Locator dashboardHeading = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Dashboard"));
        assertThat(dashboardHeading).isVisible();

        takeScreenshot(page, resultsPrefix);
    }

    @Test
    @DisplayName("Verify that user with invalid credentials can't login.")
    void testInvalidLogin(){
        String username = "admin251";
        String password = "add8";

        takeScreenshot(page, resultsPrefix);
        page.getByPlaceholder("Username").fill(username);
        page.getByPlaceholder("Password").fill(password);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login")).click();

        assertThat(page.getByText("Invalid credentials")).isVisible();

        takeScreenshot(page, resultsPrefix);

    }

    private static void takeScreenshot(Page page, String prefix){
        try{
            Thread.sleep(3000);
            LocalDateTime dateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddhhmmssSSS");
            String screenshotName = prefix + "_" + formatter.format(dateTime) + ".png";
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(screenshotName)));
        }catch (InterruptedException e){
            System.out.println(e.getMessage());
        }

    }
}
