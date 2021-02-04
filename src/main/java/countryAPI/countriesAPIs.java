package countryAPI;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class countriesAPIs {
	private static RequestSpecification requestSpec;
	private static ResponseSpecification responseSpec;

	@BeforeClass
	public static void createSpecifications() {
		// Creating an object of RequestSpecBuilder And Set Base URI
		requestSpec = new RequestSpecBuilder().setBaseUri("https://restcountries.eu").log(LogDetail.ALL).build();

		// Creating an object of ResponseSpecBuilder And Assert on response status code
		// And the response content type
		responseSpec = new ResponseSpecBuilder().expectStatusCode(200).expectContentType(ContentType.JSON)
				.log(LogDetail.BODY).build();
	}

	@Test
	public void extractCapitalNameFromResponseBodyAndSendItToAnotherAPI() {

		// Check Capital Name Exists In Response Body
		given().spec(requestSpec).when().get("/rest/v2/all?fields=name;capital;currencies;latlng").then()
				.spec(responseSpec).and().assertThat().body("capital[0]", equalTo("Kabul"));

		// Extract Capital Name
		String capitalName = given().spec(requestSpec).when().get("/rest/v2/all?fields=name;capital;currencies;latlng")
				.then().extract().path("capital[0]");

		// Send Capital To Another API And Assert it's working
		given().spec(requestSpec).when()
				.get("/rest/v2/capital/" + capitalName + "?fields=name;capital;currencies;latlng;regionalBlocs").then()
				.spec(responseSpec).and().assertThat().body("capital[0]", equalTo("Kabul"));

		// Validate Currency Code
		Assert.assertEquals(
				given().spec(requestSpec).when().get("/rest/v2/all?fields=name;capital;currencies;latlng").then()
						.extract().path("currencies[0].'code'"),
				given().spec(requestSpec).when()
						.get("/rest/v2/capital/" + capitalName + "?fields=name;capital;currencies;latlng;regionalBlocs")
						.then().extract().path("currencies[0].'code'"));
	}

	@Test
	public void checkTheResponceWhenSendInvalidData() {

		// assert That Response message is Not Found
		given().spec(requestSpec).when()
				.get("/rest/v2/capital/%3cextractedCapitalName%3e?fields=name;capital;currencies;latlng;regionalBlocs")
				.then().assertThat().body("message", equalTo("Not Found"));

		// assert That Response Code is 404
		given().spec(requestSpec).when()
				.get("/rest/v2/capital/%3cextractedCapitalName%3e?fields=name;capital;currencies;latlng;regionalBlocs")
				.then().statusCode(404);

	}

}
