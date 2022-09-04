package com.shopme.admin.category;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.shopme.admin.FileUploadUtil;
import com.shopme.admin.user.UserService;
import com.shopme.common.entity.Category;
import com.shopme.common.exception.CategoryNotFoundException;

@Controller
public class CategoryController {
	
	@Autowired
	private CategoryService sv;
	
	@GetMapping("/categories")
	public String listAll(@Param("sortDir") String sortDir, Model model, @Param("keyword") String keyword) {
//		List<Category> list = sv.listAll(sortDir);
//		model.addAttribute("listCategories",list);
//		if (sortDir == null || sortDir.isEmpty()) sortDir = "asc";
//		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";
//		model.addAttribute("reverseSortDir",reverseSortDir);
		//listByPage(1, model, sortDir,null);
		listByPage(1, model, "asc",null);
		return "categories/categories";
	}
	
	@GetMapping("categories/page/{pageNum}")
	public String listByPage(@PathVariable(name = "pageNum") int pageNum,  Model model, 
			@Param("sortDir") String sortDir, @Param("keyword") String keyword) {
		int totalElement = sv.listExam(sortDir, keyword).size();
//		if (keyword == null) totalElement = sv.listCategoriesUsedInformAndTable(sortDir).size();
//		else totalElement = sv.listAllByKeyWord(sortDir,keyword).size();
		int pageSize = sv.PAGE_SIZE;
		int totalPage = 0;
		if (totalElement%pageSize==0) totalPage = totalElement/pageSize;
		else totalPage = totalElement/pageSize+1;
		
		long startCount  = (pageNum-1)*sv.PAGE_SIZE+1;
		long endCount = startCount+pageSize-1;
		if (endCount > totalElement) {
			endCount = totalElement;
		}
		
		List<Category> list = sv.listByPage(sv.listExam(sortDir, keyword),sortDir, pageNum,keyword);
		
		if (sortDir == null || sortDir.isEmpty()) sortDir = "asc";
		String sortPage = sortDir.equals("asc") ? "asc" : "desc";
		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";
		
		
	
		
		model.addAttribute("sortPage",sortPage);
		model.addAttribute("reverseSortDir",reverseSortDir);
		model.addAttribute("listCategories",list);
		model.addAttribute("startCount",startCount);
		model.addAttribute("endCount",endCount);
		model.addAttribute("totalItems",totalElement);
		model.addAttribute("totalPages",totalPage);
		model.addAttribute("currentPage",pageNum);
		model.addAttribute("keyword",keyword);
		model.addAttribute("sortField","name");
		model.addAttribute("moduleURL", "/categories");
		return "categories/categories";
	}

	
	@GetMapping("/categories/new")
	public String newCategory(Model model) {
		List<Category> list = sv.listCategoriesUsedInformAndTable("asc");
		model.addAttribute("category",new Category());
		model.addAttribute("pageTitle","Create new Category");
		model.addAttribute("listCategories",list);
		return "categories/category_form";
	}
	
	@PostMapping("/categories/save")
	public String saveCategory(Category category,@RequestParam("fileImage") MultipartFile multipartFile,
			RedirectAttributes redi) throws IOException {
		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			category.setImage(fileName);

			Category savedCategory = sv.save(category);
			String uploadDir = "../category-images/" + savedCategory.getId();
			
			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
			
		}else {
			sv.save(category);
		}
		
		redi.addFlashAttribute("message","The Category have been saved successfully!!!");
		
		return "redirect:/categories";
		
	}
	
	@GetMapping("/categories/edit/{id}")
	public String editCategory(@PathVariable(name="id") Integer id, Model model, RedirectAttributes redi) {
		try {
		
			List<Category> list = sv.listCategoriesUsedInformAndTable("asc");
			Category category = sv.get(id);
			
			
			model.addAttribute("category",category);
			model.addAttribute("listCategories",list);
			model.addAttribute("pageTitle","Edit Category (ID = "+id+")");
			
			return "categories/category_form";
		}catch(CategoryNotFoundException e) {
			redi.addFlashAttribute("message",e.getMessage());
			return "redirect:/categories";
		}
	}
	
	@GetMapping("/categories/{id}/enabled/{status}")
	public String updateCategoryEnabledStatus(@PathVariable("id") Integer id, @PathVariable("status") boolean enabled,
			RedirectAttributes redi) {
		sv.updateCategoryEnabledStatus(id, enabled);
		String status = enabled ? "enabled" : "disabled";
		String message = "The Category ID " + id + " has ben " + status;
		redi.addFlashAttribute("message",message);
		return "redirect:/categories";
	}
	
//	@GetMapping("/categories/delete/{id}")
//	public String deleteCategory(@PathVariable(name="id") Integer id, Model model, RedirectAttributes redi) {
//		try {
//			sv.delete(id);
//			String categoryDir = "../category-images" + id;
//			FileUploadUtil.removeDir(categoryDir);
//			redi.addFlashAttribute("message","The category ID "+id+" has been deleted successfully");
//		}catch(CategoryNotFoundException e) {
//			redi.addFlashAttribute("message",e.getMessage());
//		}
//		return "redirect:/categories";
//	}
	
	@GetMapping("/categories/delete/{id}")
	public String deleteCategory(@PathVariable(name = "id") Integer id, 
			Model model,
			RedirectAttributes redirectAttributes) {
		try {
			sv.delete(id);
			String categoryDir = "../category-images/" + id;
			FileUploadUtil.removeDir(categoryDir);
			
			redirectAttributes.addFlashAttribute("message", 
					"The category ID " + id + " has been deleted successfully");
		} catch (CategoryNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
		}
		
		return "redirect:/categories";
	}
	
	@GetMapping("/categories/export/csv")
	public void exportToCSV(HttpServletResponse response) throws IOException{
		List<Category> list = sv.listCategoriesUsedInformAndTable(null);
		CategoryCsvExporter exporter = new CategoryCsvExporter();
		exporter.export(list, response);
	}
	
	
}
