package com.nikhilnishad.main;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Application {

    private WebDriver driver;
    private Set<String> visitedLinks;

    public static void main(String[] args) {
        Application obj=new Application();
        obj.setUp();
        obj.readVisitedList();
        //obj.login();
        List<String> propertyLinks=obj.propertyList();
        obj.sendContactDetails(propertyLinks);
        obj.updateVisitedList();
    }

    public void setUp(){
        System.out.println("Web driver Getting Ready");
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        //options.addArguments("--headless");
        //String userAgent="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36";
        //options.addArguments("user-agent="+userAgent);
        driver = new ChromeDriver(options);
        driver.navigate()
                .to("https://www.rightmove.co.uk/property-to-rent/find.html?locationIdentifier=REGION%5E93616&minBedrooms=2&maxPrice=1200&radius=3.0&propertyTypes=&maxDaysSinceAdded=1&includeLetAgreed=false&mustHave=&dontShow=&furnishTypes=furnished&keywords=");
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(150));
        System.out.println("Web driver ready");
    }

    public void readVisitedList(){
        try{
            FileInputStream fileInputStream
                    = new FileInputStream("visitedList.data");
            ObjectInputStream objectInputStream
                    = new ObjectInputStream(fileInputStream);
            visitedLinks = (Set<String>) objectInputStream.readObject();
            objectInputStream.close();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(120));
        } catch (FileNotFoundException e) {
            try {
                FileOutputStream fileOutputStream
                        = new FileOutputStream("visitedList.data");
                ObjectOutputStream objectOutputStream
                        = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(new HashSet<String>());
                objectOutputStream.flush();
                objectOutputStream.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void login(String username, String password){
        System.out.println("inside login");
        try{
            FileInputStream fileInputStream
                    = new FileInputStream("cookie.data");
            ObjectInputStream objectInputStream
                    = new ObjectInputStream(fileInputStream);
            Set<Cookie> cookies = (Set<Cookie>) objectInputStream.readObject();
            objectInputStream.close();
            driver.manage().deleteAllCookies();
            for (Cookie c:cookies) driver.manage().addCookie(c);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(120));
        } catch (FileNotFoundException e) {
            try{
                Thread.sleep(1000);
                WebElement usernameTxt = driver.findElement(By.id("email-input"));
                usernameTxt.sendKeys(username);
                WebElement passwordTxt = driver.findElement(By.id("password-input"));
                passwordTxt.sendKeys(password);
                try{
                    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(120));
                    passwordTxt.sendKeys(Keys.RETURN);

                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
                Thread.sleep(1000);
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(120));
                System.out.println("Current URL is:" + driver.getCurrentUrl());
                FileOutputStream fileOutputStream
                        = new FileOutputStream("cookie.data");
                ObjectOutputStream objectOutputStream
                        = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(driver.manage().getCookies());
                objectOutputStream.flush();
                objectOutputStream.close();
            } catch (IOException | InterruptedException ex){
                ex.printStackTrace();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        //driver.navigate().to("https://www.rightmove.co.uk/property-to-rent/find.html?searchType=RENT&locationIdentifier=REGION%5E93616&insId=1&radius=3.0&minPrice=&maxPrice=1200&minBedrooms=2&maxBedrooms=&displayPropertyType=&maxDaysSinceAdded=1&sortByPriceDescending=&_includeLetAgreed=on&primaryDisplayPropertyType=&secondaryDisplayPropertyType=&oldDisplayPropertyType=&oldPrimaryDisplayPropertyType=&letType=&letFurnishType=&houseFlatShare=#prop53122155");
    }


    public List<String> propertyList(){
        List<WebElement> mailList = driver.findElements(By.className("mail-icon"));
        List<String> propertiesLinkToVisit=new ArrayList<>();
        mailList.forEach( property->{
            if(visitedLinks==null || !visitedLinks.contains(property.getAttribute("href"))){
                propertiesLinkToVisit.add(property.getAttribute("href"));
            }

        });
        return propertiesLinkToVisit;
    }

    public void sendContactDetails(List<String> propertyListToVisit){
        propertyListToVisit.forEach(link->{
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            driver.get(link);
            driver.findElement(By.id("toViewProperty")).click();
            driver.findElement(By.id("firstName")).sendKeys("Nikhil");
            driver.findElement(By.id("lastName")).sendKeys("Nishad");
            driver.findElement(By.id("phone.number")).sendKeys("07721701633");
            driver.findElement(By.id("email")).sendKeys("nikhilnishadatuk@gmail.com");
            driver.findElement(By.id("address.countryCode")).sendKeys("United Kingdom");
            driver.findElement(By.id("postcode")).sendKeys("G40PU");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            driver.findElement(By.id("postcode")).sendKeys(Keys.TAB,Keys.TAB,Keys.TAB,Keys.TAB,Keys.TAB,
                    Keys.TAB,Keys.TAB,Keys.TAB,Keys.TAB,Keys.TAB,
                    Keys.TAB,Keys.TAB,Keys.TAB,
                    Keys.ENTER);
            driver.findElement(By.id("comments"))
                    .sendKeys("Viewing request for this beautiful place. " +
                            "I am a software engineer working with JP Morgan and I neither smoke nor have pets. " +
                            "I am only one to move in.");
            //driver.findElement(By.className("dsrm_button")).click();
            //visitedLinks.add(link);
        });
    }

    public void updateVisitedList(){
        try {
            FileOutputStream fileOutputStream
                    = new FileOutputStream("visitedList.data");
            ObjectOutputStream objectOutputStream
                    = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(visitedLinks);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
