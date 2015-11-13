import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.SerializationUtils;

import messageUtils.Message;

/**
 * Servlet implementation class Servlet
 */
@WebServlet("/Servlet")
public class Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public Servlet() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		OutputStream outputStream = response.getOutputStream();
		String longitude = request.getParameter("longitude");
		String latitude = request.getParameter("latitude");
		DatabaseAccess db = new DatabaseAccess();
		db.runQueries(longitude, latitude);
		System.out.println("Running!");
		Message fullMessage = db.getMessage();
		System.out.println(fullMessage.getHouse().size());
		System.out.println(fullMessage.getHouses().size());
		outputStream.write(fromJavaToByteArray(fullMessage));
		outputStream.close();
		outputStream.flush();

	}

	public static byte[] fromJavaToByteArray(Serializable object) {
		return SerializationUtils.serialize(object);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		hasConnected();
		PrintWriter output = response.getWriter();
		String fileName = request.getParameter("MAC");
		Boolean foundFile = findFile(fileName + ".txt", new File(getServletContext().getRealPath("/")));
		saveFile(fileName, request.getParameter("ENTRY"), new Date().toString(), foundFile);
		output.write("Message received!");
		output.close();
		output.flush();
	}

	protected void saveFile(String fileName, String entry, String date, Boolean foundFile)
			throws ServletException, IOException {

		File outputFile = new File(getServletContext().getRealPath("/") + fileName + ".txt");
		FileWriter fw = new FileWriter(outputFile, true);
		System.out.println(entry);
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("\n");
			sb.append(entry);
			System.out.println(entry);
			sb.append(", ");
			sb.append(date);
			if (!foundFile) {
				fw.write("long, lat, date");
			}
			fw.write(sb.toString());
		} finally {
			fw.close();
		}
	}

	protected Boolean findFile(String name, File file) {
		File[] list = file.listFiles();
		if (list != null) {

			for (File f : list) {
				System.out.println(f.getAbsolutePath());
				if (f.isDirectory()) {
					findFile(name, f);
				} else if (name.equalsIgnoreCase(f.getName())) {
					System.out.println("Found the file!");
					return true;
				}
			}

		} else {
			return false;
		}
		return false;
	}

	protected void hasConnected() throws IOException {

		File outputFile = new File(getServletContext().getRealPath("/") + "connections.txt");
		FileWriter fw = new FileWriter(outputFile, true);

		try {
			StringBuilder sb = new StringBuilder();
			sb.append("Someone has connected at: ");
			sb.append(new Date().toString());
			fw.write(sb.toString());
		} finally {
			fw.close();
		}
	}
}
