package com.shopme.product;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.shopme.category.CategoryService;
import com.shopme.common.entity.Category;
import com.shopme.common.entity.Product;
import com.shopme.common.exception.CategoryNotFoundException;
import com.shopme.common.exception.ProductNotFoundException;

@Controller
public class ProductController {

	@Autowired private CategoryService catSv;
	
	@Autowired private ProductService productSv;
	
	@GetMapping("/c/{category_alias}")
	public String viewCategoryFirstPage(@PathVariable("category_alias") String alias,
			Model model) {
		return viewCategoryByPage(alias, 1, model);
	}
	
	@GetMapping("/c/{category_alias}/page/{pageNum}")
	public String viewCategoryByPage(@PathVariable("category_alias") String alias,
			@PathVariable("pageNum") int pageNum,
			Model model)  {
		
		try {
			
			Category category = catSv.getCategory(alias);
			if (category == null) {
				return "error/404";
			}
			
			List<Category> listCategoryParents = catSv.getCategoryParents(category);
			Page<Product> pageProducts = productSv.listByCategory(pageNum, category.getId());
			List<Product> listProducts = pageProducts.getContent();
			
			
			long startCount  = (pageNum-1)*productSv.PRODUCTS_PER_PAGE + 1;
			long endCount = startCount+productSv.PRODUCTS_PER_PAGE - 1;
			if (endCount >  pageProducts.getTotalElements()) {
				endCount =  pageProducts.getTotalElements();
			}
			
			
					
			model.addAttribute("startCount",startCount);
			model.addAttribute("endCount",endCount);
			model.addAttribute("totalItems", pageProducts.getTotalElements());
			model.addAttribute("totalPages", pageProducts.getTotalPages());
			model.addAttribute("currentPage",pageNum);
			model.addAttribute("pageTitle",category.getName());
			model.addAttribute("moduleURL","/c/"+category.getAlias());
			model.addAttribute("category",category);
			
			
			model.addAttribute("listCategoryParents",listCategoryParents);
			model.addAttribute("listProducts",listProducts);
			
			
			return "product/product_by_category";
		}catch(CategoryNotFoundException e) {
			return "error/404";
		}
		
	}
	
	@GetMapping("/p/{product_alias}")
	public String viewProductDetail(@PathVariable("product_alias") String alias, Model model) {
		try {
			Product product = productSv.getProduct(alias);
			List<Category> listCategoryParents = catSv.getCategoryParents(product.getCategory());
			
			model.addAttribute("listCategoryParents",listCategoryParents);
			model.addAttribute("product",product);
			model.addAttribute("pageTitle",product.getShortName());
			
			return "product/product_detail";
		}catch(ProductNotFoundException e) {
			e.printStackTrace();
			return "error/404";
		}
	}
	
	@GetMapping("/search")
	public String searchFirstPage(@Param("keyword") String keyword, Model model) {
		return searchByPage(keyword, 1, model);
	}
	
//	@GetMapping("/search/{pageNum}")
//	public String search(@Param("keyword") String keyword, 
//			@PathVariable("pageNum") int pageNum,
//			Model model) {
//		
//		Page<Product> pageProducts = productSv.search(keyword, pageNum);
//		List<Product> listResult = pageProducts.getContent();
//		
//		long startCount  = (pageNum-1)*productSv.SEARCH_RESULTS_PER_PAGE + 1;
//		long endCount = startCount+productSv.SEARCH_RESULTS_PER_PAGE - 1;
//		if (endCount >  pageProducts.getTotalElements()) {
//			endCount =  pageProducts.getTotalElements();
//		}
//		
//		
//				
//		model.addAttribute("startCount",startCount);
//		model.addAttribute("endCount",endCount);
//		model.addAttribute("totalItems", pageProducts.getTotalElements());
//		model.addAttribute("totalPages", pageProducts.getTotalPages());
//		model.addAttribute("currentPage",pageNum);
//		model.addAttribute("pageTitle", keyword + " - Search Result");
//		model.addAttribute("moduleURL","/search");
//		
//		model.addAttribute("keyword",keyword);
//		model.addAttribute("listResult",listResult);
//		return "product/search_result";
//	}
	
	
	@GetMapping("/search/page/{pageNum}")
	public String searchByPage(String keyword,
			@PathVariable("pageNum") int pageNum,
			Model model) {
		Page<Product> pageProducts = productSv.search(keyword, pageNum);
		List<Product> listResult = pageProducts.getContent();
		
		long startCount = (pageNum - 1) * ProductService.SEARCH_RESULTS_PER_PAGE + 1;
		long endCount = startCount + ProductService.SEARCH_RESULTS_PER_PAGE - 1;
		if (endCount > pageProducts.getTotalElements()) {
			endCount = pageProducts.getTotalElements();
		}
		
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages", pageProducts.getTotalPages());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalItems", pageProducts.getTotalElements());
		model.addAttribute("pageTitle", keyword + " - Search Result");
		model.addAttribute("moduleURL","/search");
		
		model.addAttribute("keyword", keyword);
		model.addAttribute("searchKeyword", keyword);
		model.addAttribute("listResult", listResult);
		
		return "product/search_result";
	}		
}
