package com.focusit.jsflight.recorder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = { "/jsflight/recorder/download" })
public class RecorderDownloadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = req.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) {
			/* report an error */
		}

		String result = java.net.URLDecoder.decode(jb.toString(), "UTF-8");

		System.out.println(result);
		String name = "record_"+System.currentTimeMillis()+".json";
		File file = new File(name);
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
		try {
			out.write(result);
		} finally {
			out.close();
		}
		resp.getWriter().println("Saved to "+file.getAbsolutePath());
		resp.getWriter().flush();
	}

}
