import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class ServletTest {
	private Servlet servlet;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@Before
	public void setUp() {
		servlet = new Servlet();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Test
	public void checkNullLongLatParameters() throws ServletException, IOException {
		String value = null;
		request.addParameter("latitude", value);
		request.addParameter("longitude", value);

		servlet.doPost(request, response);

		assertEquals(500, response.getStatus());

	}

	@Test
	public void checkNullMacParameter() throws ServletException, IOException {
		String value = null;
		request.addParameter("MAC", value);
		request.addParameter("ENTRY", "10,20,d0");

		servlet.doPost(request, response);

		assertEquals(500, response.getStatus());

	}

	@Test
	public void checkNullEntryParameter() throws ServletException, IOException {
		String value = null;
		request.addParameter("MAC", "d3:eb");
		request.addParameter("ENTRY", value);

		servlet.doPost(request, response);

		assertEquals(500, response.getStatus());

	}

	@SuppressWarnings("deprecation")
	@Test
	public void checkPostcodeValidator() {
		String value = new String("BN 19RH");
		ArrayList<String> postcode = new ArrayList<String>(Arrays.asList(value));

		// Should return false, accepted is BN1 9RH or BN19RH
		DatabaseAccess db = new DatabaseAccess();
		assertEquals(0, db.checkPostcode(postcode).size());

	}

	@SuppressWarnings("deprecation")
	@Test
	public void checkOneEntry() {

		double averagePrice = 190000;
		double leastExpensive = 190000;
		double mostExpensive = 190000;
		DatabaseAccess d = new DatabaseAccess();
		assertEquals(1, d.performWeightCalculation(averagePrice, leastExpensive, mostExpensive));

		// Helped to discover a bug
	}

	@SuppressWarnings("deprecation")
	@Test
	public void divideByZero() {
		double values = 0;
		DatabaseAccess d = new DatabaseAccess();
		assertEquals(1, d.performWeightCalculation(values, values, values));
	}

	@Test
	public void checkNullPostParameter() throws ServletException, IOException {
		String value = null;
		request.addParameter("MAC", value);
		request.addParameter("ENTRY", value);

		servlet.doPost(request, response);

		assertEquals(500, response.getStatus());

	}

}