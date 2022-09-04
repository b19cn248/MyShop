package com.shopme.admin.brand;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import com.shopme.admin.category.CategoryService;
import com.shopme.common.entity.Brand;
import com.shopme.common.entity.Category;

@Controller
public class BrandController {

	@Autowired
	private BrandService BrandSv;
	
	@Autowired
	private CategoryService CatSv;
	
	@GetMapping("/brands")
	public String listAll(Model model) {
		return listByPage(1, model,"asc","name",null);
	}
	
	@GetMapping("/brands/page/{pageNum}")
	public String listByPage(@PathVariable(name="pageNum") Integer pageNum, Model model, 
			@Param("sortDir") String sortDir, @Param("sortField") String sortField, @Param("keyword") String keyword) {
		List<Brand> list = BrandSv.listByKeyWord(keyword);
		
		int totalElement = list.size();
		int pageSize = BrandSv.PAGE_SIZE;
		int totalPage = 0;
		if (totalElement%pageSize==0) totalPage = totalElement/pageSize;
		else totalPage = totalElement/pageSize+1;
		List<Brand> brands = BrandSv.listByPage(list,pageNum,sortDir);
		
		long startCount  = (pageNum-1)*BrandSv.PAGE_SIZE+1;
		long endCount = startCount+pageSize-1;
		if (endCount > totalElement) {
			endCount = totalElement;
		}
		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";
		
		
		model.addAttribute("startCount",startCount);
		model.addAttribute("endCount",endCount);
		model.addAttribute("totalItems",totalElement);
		model.addAttribute("totalPages",totalPage);
		model.addAttribute("currentPage",pageNum);
		model.addAttribute("moduleURL", "/brands");
		model.addAttribute("sortDir",sortDir);
		model.addAttribute("sortField","name");
		model.addAttribute("reverseSortDir",reverseSortDir);
		model.addAttribute("keyword",keyword);
		
		model.addAttribute("brands",brands);
		return "brands/brands";	
	}
	
	@GetMapping("/brands/new")
	public String newBrand(Model model) {
		List<Category> listCategories = CatSv.listCategoriesUsedInformAndTable(null);
		
		model.addAttribute("listCategories", listCategories);
		model.addAttribute("brand", new Brand());
		model.addAttribute("pageTitle", "Create New Brand");
		
		return "brands/brand_form";		
	}
	
	@PostMapping("/brands/save")
	public String saveBrand(Brand brand, @RequestParam("fileImage") MultipartFile multipartFile,
			RedirectAttributes redi) throws IOException {
		if (!multipartFile.isEmpty()) {
			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			brand.setLogo(fileName);
			
			Brand saveBrand = BrandSv.save(brand);
			String uploadDir = "../brand-logos/"+saveBrand.getId();
			
			FileUploadUtil.cleanDir(uploadDir);
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
		}else {
			BrandSv.save(brand);
		}
		redi.addFlashAttribute("message","The brand has been saved successfully");
		return "redirect:/brands";
	}
	
	@GetMapping("brands/edit/{id}")
	public String editBrand(@PathVariable(name="id") Integer id, Model model, RedirectAttributes redi) {
		try {
			Brand brand = BrandSv.get(id);
			List<Category> listCategories = CatSv.listCategoriesUsedInformAndTable(null);
			
			model.addAttribute("listCategories",listCategories);
			model.addAttribute("brand",brand);
			model.addAttribute("pageTitle","Edit Brand (ID: "+id+")");
			
			return "brands/brand_form";
		}catch(BrandNotFoundException e) {
			redi.addAttribute("message",e.getMessage());
			return "redirect:/brands";
		}
	}
	

	@GetMapping("brands/delete/{id}")
	public String deleteBrand(@PathVariable(name="id") Integer id, Model model, RedirectAttributes redi) {
		try {
			BrandSv.delete(id);
			String brandDir = "../brand-logos/"+id;
			
			FileUploadUtil.removeDir(brandDir);
			
			redi.addFlashAttribute("message","The Brand ID "+id+" has been deleted successfully!!!");
			
		}catch(BrandNotFoundException e) {
			redi.addAttribute("message",e.getMessage());
			return "redirect:/brands";
		}
		return "redirect:/brands";
	}
	
	
}
