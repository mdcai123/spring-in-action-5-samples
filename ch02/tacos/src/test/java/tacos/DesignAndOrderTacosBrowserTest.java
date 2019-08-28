package tacos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class DesignAndOrderTacosBrowserTest {
  
  private static WebClient browser;
  
  @LocalServerPort
  private int port;
  
  @Autowired
  TestRestTemplate rest;
  
  @BeforeClass
  public static void setup() {
    browser = new WebClient();
  }
  
  @AfterClass
  public static void closeBrowser() {
    browser.close();
  }
  
  @Test
  public void testDesignATacoPage_HappyPath() throws Exception {
	HtmlPage page = browser.getPage(homePageUrl());
    page = clickDesignATaco(page);
    assertDesignPageElements(page);
    HtmlPage orderPage = buildAndSubmitATaco(page, "Basic Taco", "FLTO", "GRBF", "CHED", "TMTO", "SLSA");
    HtmlPage anotherOrderPage = clickBuildAnotherTaco(orderPage);
    HtmlPage aPage = buildAndSubmitATaco(anotherOrderPage, "Another Taco", "COTO", "CARN", "JACK", "LETC", "SRCR");
    HtmlPage homePage = fillInAndSubmitOrderForm(aPage);
    assertEquals(homePageUrl(),url(homePage));
  }
  
  @Test
  public void testDesignATacoPage_EmptyOrderInfo() throws Exception {
	HtmlPage page = browser.getPage(homePageUrl());
	page = clickDesignATaco(page);
    assertDesignPageElements(page);
    HtmlPage orderPage = buildAndSubmitATaco(page, "Basic Taco", "FLTO", "GRBF", "CHED", "TMTO", "SLSA");
    submitEmptyOrderForm(orderPage);
    HtmlPage homePage = fillInAndSubmitOrderForm(orderPage);
    assertEquals(homePageUrl(),url(homePage));
  }

  @Test
  public void testDesignATacoPage_InvalidOrderInfo() throws Exception {
	HtmlPage page = browser.getPage(homePageUrl());
    HtmlPage designPage = clickDesignATaco(page);
    assertDesignPageElements(designPage);
    HtmlPage orderPage = buildAndSubmitATaco(designPage, "Basic Taco", "FLTO", "GRBF", "CHED", "TMTO", "SLSA");
    submitInvalidOrderForm(orderPage);
    HtmlPage homePage = fillInAndSubmitOrderForm(orderPage);
    assertEquals(homePageUrl(),url(homePage));
  }

  //
  // Browser test action methods
  //
  private HtmlPage buildAndSubmitATaco(HtmlPage page, String name, String... ingredients) throws IOException {
    for (String ingredient : ingredients) {
    	HtmlCheckBoxInput checkBox = page.querySelector("input[value='" + ingredient + "']");
    	checkBox.setChecked(true);
    }
    HtmlInput input = page.querySelector("input#name");
    input.setValueAttribute(name);
    final HtmlForm form = (HtmlForm)page.querySelector("form");
    final HtmlButton button = form.getFirstByXPath(".//button");
    HtmlPage newPage = button.click();
    return newPage;
  }

  private HtmlPage assertDesignPageElements(HtmlPage page) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
    assertEquals(designPageUrl(), url(page));
    List<?> ingredientGroups = page.getByXPath("//div[@class='ingredient-group']");
    assertEquals(5, ingredientGroups.size());
    
    DomNode wrapGroup = page.querySelector("div.ingredient-group#wraps");
    List<DomNode> wraps = wrapGroup.getByXPath("div");
    assertEquals(2, wraps.size());
    assertIngredient(wraps, 0, "FLTO", "Flour Tortilla");
    assertIngredient(wraps, 1, "COTO", "Corn Tortilla");
    
    DomNode proteinGroup = page.querySelector("div.ingredient-group#proteins");
    List<DomNode> proteins = proteinGroup.getByXPath("div");
    assertEquals(2, proteins.size());
    assertIngredient(proteins, 0, "GRBF", "Ground Beef");
    assertIngredient(proteins, 1, "CARN", "Carnitas");

    DomNode cheeseGroup = page.querySelector("div.ingredient-group#cheeses");
    List<DomNode> cheeses = cheeseGroup.getByXPath("div");
    assertEquals(2, cheeses.size());
    assertIngredient(cheeses, 0, "CHED", "Cheddar");
    assertIngredient(cheeses, 1, "JACK", "Monterrey Jack");

    DomNode veggieGroup = page.querySelector("div.ingredient-group#veggies");
    List<DomNode> veggies = veggieGroup.getByXPath("div");
    assertEquals(2, veggies.size());
    assertIngredient(veggies, 0, "TMTO", "Diced Tomatoes");
    assertIngredient(veggies, 1, "LETC", "Lettuce");

    DomNode sauceGroup = page.querySelector("div.ingredient-group#sauces");
    List<DomNode> sauces = sauceGroup.getByXPath("div");
    assertEquals(2, sauces.size());
    assertIngredient(sauces, 0, "SLSA", "Salsa");
    assertIngredient(sauces, 1, "SRCR", "Sour Cream");
    return page;
  }
  

  private HtmlPage fillInAndSubmitOrderForm(HtmlPage page) throws IOException {
    fillField(page, "input#name", "Ima Hungry");
    fillField(page, "input#street", "1234 Culinary Blvd.");
    fillField(page, "input#city", "Foodsville");
    fillField(page, "input#state", "CO");
    fillField(page, "input#zip", "81019");
    fillField(page, "input#ccNumber", "4111111111111111");
    fillField(page, "input#ccExpiration", "10/19");
    fillField(page, "input#ccCVV", "123");
    final HtmlForm form = (HtmlForm)page.querySelector("form");
    final HtmlSubmitInput submit = form.querySelector("input[type=submit]");
    return submit.click();
  }

  private void submitEmptyOrderForm(HtmlPage page) throws IOException {
    final HtmlForm form = (HtmlForm)page.querySelector("form");
    final HtmlSubmitInput submit = form.querySelector("input[type=submit]");
    HtmlPage orderDetailsPage = submit.click();
    
    assertEquals(orderDetailsPageUrl(), url(orderDetailsPage));

    List<String> validationErrors = getValidationErrorTexts(orderDetailsPage);
    assertEquals(9, validationErrors.size());
    assertTrue(validationErrors.get(0).contains("Please correct the problems below and resubmit."));
    assertTrue(validationErrors.get(1).contains("Name is required"));
    assertTrue(validationErrors.get(2).contains("Street is required"));
    assertTrue(validationErrors.get(3).contains("City is required"));
    assertTrue(validationErrors.get(4).contains("State is required"));
    assertTrue(validationErrors.get(5).contains("Zip code is required"));
    assertTrue(validationErrors.get(6).contains("Not a valid credit card number"));
    assertTrue(validationErrors.get(7).contains("Must be formatted MM/YY"));
    assertTrue(validationErrors.get(8).contains("Invalid CVV"));    
  }

  private List<String> getValidationErrorTexts(HtmlPage page) {
    List<HtmlSpan> validationErrorElements = page.getByXPath("//span[@class='validationError']");
    List<String> validationErrors = validationErrorElements.stream()
        .map(el -> el.getTextContent())
        .collect(Collectors.toList());
    return validationErrors;
  }

  private void submitInvalidOrderForm(HtmlPage page) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
    fillField(page, "input#name", "I");
    fillField(page, "input#street", "1");
    fillField(page, "input#city", "F");
    fillField(page, "input#state", "C");
    fillField(page, "input#zip", "8");
    fillField(page, "input#ccNumber", "1234432112344322");
    fillField(page, "input#ccExpiration", "14/91");
    fillField(page, "input#ccCVV", "1234");
    final HtmlForm form = (HtmlForm)page.querySelector("form");
    final HtmlSubmitInput submit = form.querySelector("input[type=submit]");
    HtmlPage orderDetailsPage = submit.click();
    
    assertEquals(orderDetailsPageUrl(), url(orderDetailsPage));

    List<String> validationErrors = getValidationErrorTexts(orderDetailsPage);
    assertEquals(4, validationErrors.size());
    assertTrue(validationErrors.get(0).contains("Please correct the problems below and resubmit."));
    assertTrue(validationErrors.get(1).contains("Not a valid credit card number"));
    assertTrue(validationErrors.get(2).contains("Must be formatted MM/YY"));
    assertTrue(validationErrors.get(3).contains("Invalid CVV"));    
  }

  private void fillField(HtmlPage page, String fieldName, String value) {
    HtmlInput field = page.querySelector(fieldName);
    field.setValueAttribute(value);
  }
  
  private void assertIngredient(List<DomNode> proteins,int ingredientIdx, String id, String name) {
    DomNode ingredient = proteins.get(ingredientIdx); 
    assertEquals(id, ((HtmlCheckBoxInput)ingredient.querySelector("input")).getValueAttribute());
    assertEquals(name, ((HtmlSpan)ingredient.querySelector("span")).getTextContent());
  }

  private HtmlPage clickDesignATaco(HtmlPage page) throws IOException {	
    assertEquals(homePageUrl(), url(page));
    HtmlAnchor a = page.querySelector("a[id='design']");
    return a.click();
  }

  private HtmlPage clickBuildAnotherTaco(HtmlPage page) throws IOException {
    assertEquals(currentOrderDetailsPageUrl(), url(page));
    HtmlAnchor a = page.querySelector("a[id='another']");
    return a.click();
  }
  
  private String url(HtmlPage page) {
	  return page.getUrl().toString();
  }
 
  //
  // URL helper methods
  //
  private String designPageUrl() {
    return homePageUrl() + "design";
  }

  private String homePageUrl() {
    return "http://localhost:" + port + "/";
  }

  private String orderDetailsPageUrl() {
    return homePageUrl() + "orders";
  }

  private String currentOrderDetailsPageUrl() {
    return homePageUrl() + "orders/current";
  }

}
