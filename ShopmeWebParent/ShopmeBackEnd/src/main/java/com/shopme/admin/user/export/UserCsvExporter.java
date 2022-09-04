package com.shopme.admin.user.export;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.shopme.admin.AbstractExport;
import com.shopme.common.entity.User;

public class UserCsvExporter extends AbstractExport{
	
	public void export(List<User> listUsers, HttpServletResponse response) throws IOException {
		super.setResponseHeader(response, "text/csv",".csv","users_");
		ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
		String[] csvHeader  = {"User ID","E-mail","First Name","Last Name","Roles","Enabled"};
		csvWriter.writeHeader(csvHeader);
		String[] fieldMapping = {"id","email","firstName","lastName","roles","enabled"};
		for (User user:listUsers) {
			csvWriter.write(user, fieldMapping);
		}
		csvWriter.close();
	}
}
