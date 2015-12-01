package com.focusit.jsflight.recorder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * Servlet provides a js to track events in a browser
 * 
 * @author Denis V. Kirpichenkov
 *
 */
@WebServlet(urlPatterns = { "/jsflight/recorder" })
public class RecorderServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		try(InputStream is = classloader.getResourceAsStream("recorder.js"))
		{
			resp.setContentType("application/javascript");
			try(OutputStream out = resp.getOutputStream()){
				IOUtils.copy(is, out);
				out.flush();
			}
		}
	}
}
