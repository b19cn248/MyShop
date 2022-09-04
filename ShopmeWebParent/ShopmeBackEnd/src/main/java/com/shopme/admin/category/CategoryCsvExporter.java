package com.shopme.admin.category;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import com.shopme.admin.AbstractExport;
import com.shopme.common.entity.Category;

public class CategoryCsvExporter extends AbstractExport{
	
	public void export(List<Category> listCategories, HttpServletResponse response) throws IOException {
		super.setResponseHeader(response, "text/csv",".csv","categories_");
		ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
		String[] csvHeader  = {"Category ID","Category Name"};
		csvWriter.writeHeader(csvHeader);
		String[] fieldMapping = {"id","name"};
		for (Category cat:listCategories) {
			cat.setName(cat.getName().replace("--", "   "));
			csvWriter.write(cat, fieldMapping);
		}
		csvWriter.close();
	}
}
