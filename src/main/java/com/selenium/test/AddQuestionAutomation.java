package com.selenium.test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AddQuestionAutomation {

    private static final String BASE_URL = "https://storyflow-admin-panel-staging.up.railway.app/login";
    private static final String EMAIL = "admin@storyflow.com";
    private static final String PASSWORD = "Admin@storyflow1";
    private static final String CHROME_DRIVER_PATH = "C:\\Users\\us\\Downloads\\chromedriver.exe";

    // Locators
    private static final By EMAIL_INPUT = By.id("email");
    private static final By PASSWORD_INPUT = By.name("password");
    private static final By SIGN_IN_BUTTON = By.xpath("//button[span[text()='Sign In']]");
    private static final By QUESTIONS_LINK = By.cssSelector("a[href='/books/questions-stories']");
    private static final By ADD_QUESTION_BUTTON = By.xpath("//button[.//span[text()='Add Question']]");
    private static final By QUESTION_INPUT = By.cssSelector("input[placeholder='Enter question']");
    private static final By ADD_BUTTON = By.xpath("//button[.//span[text()='Add']]");
    private static final By LOADING_SPINNER = By.cssSelector(".ant-spin-dot");

    // Category dropdown locators
    private static final By CATEGORY_DROPDOWN = By.cssSelector("div.ant-select");
    private static final By CATEGORY_DROPDOWN_OPTIONS_LIST = By.cssSelector("div[role='listbox'] div[role='option']");

    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);
        WebDriver driver = initializeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        try {
            driver.get(BASE_URL);
            login(driver, wait);
            closeModalIfPresent(driver);
            waitForLoadingToDisappear(wait);

            Thread.sleep(1000); // buffer after modal close

            UIHelper.captureScreenshot(driver, "before_books_menu_click");

            clickBooksMenu(driver, wait);
            clickQuestionsSubmenu(driver, wait);

            waitForLoadingToDisappear(wait);

            UIHelper.waitAndClick(driver, wait, ADD_QUESTION_BUTTON, "Failed to click Add Question button");

            waitForLoadingToDisappear(wait);

            addQuestion(driver, wait, "What is your favorite memory from the wedding?");

            System.out.println("✅ Question added successfully!");

        } catch (TimeoutException | NoSuchElementException e) {
            UIHelper.captureScreenshot(driver, "unexpected_error");
            System.out.println("Error during execution: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();  // Restore interrupt status
        } finally {
            driver.quit();
        }
    }

    private static WebDriver initializeDriver() {
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        return driver;
    }

    private static void login(WebDriver driver, WebDriverWait wait) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT)).sendKeys(EMAIL);
        driver.findElement(PASSWORD_INPUT).sendKeys(PASSWORD);
        driver.findElement(SIGN_IN_BUTTON).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".ant-menu")));
        System.out.println("✅ Logged in and dashboard loaded");
    }

    private static void closeModalIfPresent(WebDriver driver) {
        try {
            WebElement closeButton = driver.findElement(By.cssSelector(".ant-modal-close-x"));
            if (closeButton.isDisplayed()) {
                closeButton.click();
                System.out.println("⚠️ Modal closed.");
            }
        } catch (NoSuchElementException ignored) {
        }
    }

    private static void waitForLoadingToDisappear(WebDriverWait wait) {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(LOADING_SPINNER));
            System.out.println("✅ Loading spinner disappeared");
        } catch (TimeoutException e) {
            System.out.println("⚠️ Spinner still visible after timeout");
        }
    }

    private static void clickBooksMenu(WebDriver driver, WebDriverWait wait) {
        try {
            System.out.println("🔎 Looking for Books submenu header...");
            By booksMenuTitle = By.xpath("//div[@role='menuitem' and .//div[normalize-space()='Books']]");
            WebElement booksMenu = wait.until(ExpectedConditions.elementToBeClickable(booksMenuTitle));
            booksMenu.click();
            System.out.println("✅ Clicked on 'Books' submenu header");

            By questionsSubMenu = By.xpath("//a[contains(@href, '/books/questions-stories')]");
            wait.until(ExpectedConditions.elementToBeClickable(questionsSubMenu)).click();

            System.out.println("✅ Clicked on 'Questions / Stories' menu item");
        } catch (TimeoutException | NoSuchElementException e) {
            UIHelper.captureScreenshot(driver, "books_menu_click_failed");
            System.out.println("❌ Failed to click on Books menu: " + e.getMessage());
            throw e;
        }
    }

    private static void clickQuestionsSubmenu(WebDriver driver, WebDriverWait wait) {
        WebElement questionsMenuItem = wait.until(ExpectedConditions.elementToBeClickable(QUESTIONS_LINK));
        questionsMenuItem.click();
        System.out.println("✅ Clicked Questions / Stories submenu item");
    }

    private static void addQuestion(WebDriver driver, WebDriverWait wait, String question) {
        try {
            // Select the Birthday category from dropdown options
            selectCategoryFromDropdown(driver, wait, "Birthday");

            // Then type the question
            WebElement inputQuestion = wait.until(ExpectedConditions.elementToBeClickable(QUESTION_INPUT));
            inputQuestion.clear();
            inputQuestion.sendKeys(question);
            System.out.println("✅ Typed question via sendKeys");

            Thread.sleep(500);

            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(ADD_BUTTON));
            wait.until(d -> addBtn.isEnabled());

            Thread.sleep(500);

            addBtn.click();
            System.out.println("✅ Clicked Add button");

            wait.until(ExpectedConditions.or(
                ExpectedConditions.invisibilityOf(inputQuestion),
                ExpectedConditions.attributeToBe(QUESTION_INPUT, "value", "")
            ));
            System.out.println("✅ Submission verified: Input cleared or disappeared");

        } catch (TimeoutException | NoSuchElementException e) {
            UIHelper.captureScreenshot(driver, "failed_add_question");
            System.out.println("❌ Failed to add question: " + e.getMessage());
            throw e;
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

   private static void selectCategoryFromDropdown(WebDriver driver, WebDriverWait wait, String categoryName) {
    try {
        // Try clicking the dropdown selector
        By dropdownSelector = By.cssSelector("div.ant-select-selector");
        By dropdownArrow = By.cssSelector("span.ant-select-arrow");

        try {
            WebElement selector = wait.until(ExpectedConditions.elementToBeClickable(dropdownSelector));
            selector.click();
        } catch (Exception e) {
            WebElement arrow = wait.until(ExpectedConditions.elementToBeClickable(dropdownArrow));
            arrow.click();
        }

        System.out.println("✅ Clicked category dropdown to open");

        // Dynamically get the dropdown list ID from input
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input.ant-select-selection-search-input")));
        String dropdownListId = input.getAttribute("aria-owns");

        // Wait for dropdown to appear
        By dropdownOptionsContainer = By.id(dropdownListId);
        wait.until(ExpectedConditions.visibilityOfElementLocated(dropdownOptionsContainer));

        // Select category by visible text
        By optionLocator = By.xpath("//div[@id='" + dropdownListId + "']//div[contains(@class,'ant-select-item-option-content') and normalize-space(text())='" + categoryName + "']");
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(optionLocator));
        option.click();

        System.out.println("✅ Selected category: " + categoryName);

    } catch (Exception e) {
        UIHelper.captureScreenshot(driver, "failed_select_category");
        System.out.println("❌ Failed to select category from dropdown: " + e.getMessage());
        throw e;
    }
}
}

// -------------------------
// 🔧 Helper Class
// -------------------------
class UIHelper {

    public static void waitAndClick(WebDriver driver, WebDriverWait wait, By locator, String errorMessage) {
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
            System.out.println("✅ Clicked element: " + locator);
        } catch (TimeoutException | NoSuchElementException e) {
            captureScreenshot(driver, "failed_click");
            System.out.println("❌ " + errorMessage + ": " + e.getMessage());
            throw e;
        }
    }

    public static void waitAndType(WebDriver driver, WebDriverWait wait, By locator, String text) {
        try {
            WebElement input = wait.until(ExpectedConditions.elementToBeClickable(locator));
            input.clear();
            input.sendKeys(text);
            System.out.println("✅ Entered text into: " + locator);
        } catch (TimeoutException | NoSuchElementException e) {
            captureScreenshot(driver, "failed_type");
            System.out.println("❌ Failed to type into input: " + e.getMessage());
            throw e;
        }
    }

     public static void captureScreenshot(WebDriver driver, String baseName) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File destDir = new File("screenshots");
            if (!destDir.exists()) destDir.mkdirs();
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File dest = new File(destDir, baseName + "_" + timestamp + ".png");
            FileUtils.copyFile(src, dest);
            System.out.println("📸 Screenshot saved: " + dest.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("⚠️ Screenshot capture failed: " + e.getMessage());
        }
    }
}
