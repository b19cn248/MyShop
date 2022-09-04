package com.shopme.admin;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;


public class AbstractExport {

	public void setResponseHeader(HttpServletResponse response, String contentType, String extension, String prefix) throws IOException {
		DateFormat f = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String timestamp = f.format(new Date());
		String fileName = prefix+timestamp+extension;
		response.setContentType(contentType);
		String headerKey = "Content-Disposition";
		String headerValue = "attachment; fileName="+fileName;
		response.setHeader(headerKey, headerValue);
	}
}
