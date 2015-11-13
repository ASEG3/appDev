import org.junit.Before;
import org.junit.Test;

import com.mockrunner.mock.web.WebMockObjectFactory;
import com.mockrunner.servlet.ServletTestModule;

import junit.framework.Assert;

public class ServletMockTest {

	private ServletTestModule tester;
	private WebMockObjectFactory factory;

	// JUnit tests using a MockRunner

	public ServletMockTest() {

	}

	@Before
	public void setup() {
		factory = new WebMockObjectFactory();
		tester = new ServletTestModule(factory);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void doNullPostMac() {

		int expectedCode = 500;
		tester.addRequestParameter(null, "123,140");

		// change to a valid mac address
		tester.createServlet(Servlet.class);
		tester.doPost();

		Assert.assertEquals(expectedCode, factory.getMockResponse().getStatusCode());

	}

	@SuppressWarnings("deprecation")
	@Test
	public void doNullPostEntry() {

		int expectedCode = 500;
		String mac = "valid mac";
		String r = null;
		tester.addRequestParameter(mac, r);

		// change to a valid mac address
		tester.createServlet(Servlet.class);
		tester.doPost();

		Assert.assertEquals(expectedCode, factory.getMockResponse().getStatusCode());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void doNullPostFull() {

		int expectedCode = 500;
		String r = null;
		tester.addRequestParameter(r, r);

		// change to a valid mac address
		tester.createServlet(Servlet.class);
		tester.doPost();

		Assert.assertEquals(expectedCode, factory.getMockResponse().getStatusCode());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void doInvalidLongLatValues() {
		// Middle of the atlantic ocean!
		tester.addRequestParameter("longitude", "0");
		tester.addRequestParameter("latitude", "0");
		tester.doGet();
		Assert.assertEquals(500, factory.getMockResponse().getStatusCode());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void doNullPostcodeValues() {
		String r = null;
		tester.addRequestParameter("longitude", r);
		tester.addRequestParameter("latitude", r);
		tester.doGet();
		Assert.assertEquals(500, factory.getMockResponse().getStatusCode());

	}

}
