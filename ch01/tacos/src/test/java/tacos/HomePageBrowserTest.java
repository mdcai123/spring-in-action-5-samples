package tacos;

import java.io.IOException;
import java.net.MalformedURLException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
public class HomePageBrowserTest {
  
  @LocalServerPort
  private int port;
  private static WebClient  browser;  
  
  @BeforeClass
  public static void setup() {
    browser = new WebClient();   
   }
  
  @AfterClass
  public static void teardown() {
    browser.close();
  }
  
  @Test
  public void testHomePage() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
    String homePage = "http://localhost:" + port;
    HtmlPage page = browser.getPage(homePage);
    
    String titleText = page.getTitleText();
    Assert.assertEquals("Taco Cloud", titleText);
    
    String h1Text = page.getElementsByTagName("h1").get(0).asText();
    Assert.assertEquals("Welcome to...", h1Text);
    
    String imgSrc = page.getElementsByTagName("img").get(0).getAttribute("src");
    Assert.assertEquals(homePage + "/images/TacoCloud.png", imgSrc);
  }
}
