package com.shopme.admin.product;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.shopme.admin.brand.BrandService;
import com.shopme.admin.category.CategoryService;
import com.shopme.admin.security.ShopmeUserDetails;
import com.shopme.common.entity.Brand;
import com.shopme.common.entity.Category;
import com.shopme.common.entity.Product;
import com.shopme.common.entity.ProductImage;
import com.shopme.common.exception.ProductNotFoundException;

@Controller
@Transactional
public class ProductController {

	@Autowired private ProductService productSv;
	@Autowired private BrandService brandSv;
	@Autowired private CategoryService catSv;
	
	
	
	@GetMapping("/products")
	public String listAll(Model model) {
		return listByPage(1, model, "name", "asc", null, 0);
	}
	
	@GetMapping("/products/page/{pageNum}")
	public String listByPage(@PathVariable(name="pageNum") Integer pageNum, Model model, 
			 @Param("sortField") String sortField, @Param("sortDir") String sortDir, 
			 @Param("keyword") String keyword, @Param("categoryId") Integer categoryId) {
		
		
		Page<Product> page = productSv.listByPage(pageNum, sortField, sortDir, keyword,categoryId);
		List<Product> list = page.getContent();
		
		long startCount  = (pageNum-1)*productSv.PRODUCTS_PER_PAGE + 1;
		long endCount = startCount+productSv.PRODUCTS_PER_PAGE - 1;
		if (endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}
		
		List<Category> listCat = catSv.listCategoriesUsedInformAndTable(null); 
		
		
		String reverseSortDir = sortDir.equals("asc") ? "desc" : "asc";
		
		if (categoryId !=null) model.addAttribute("categoryId",categoryId);
		
		model.addAttribute("startCount",startCount);
		model.addAttribute("endCount",endCount);
		model.addAttribute("totalItems",page.getTotalElements());
		model.addAttribute("totalPages",page.getTotalPages());
		model.addAttribute("currentPage",pageNum);
		model.addAttribute("sortDir",sortDir);
		model.addAttribute("sortField","name");
		model.addAttribute("reverseSortDir",reverseSortDir);
		model.addAttribute("keyword",keyword);
		model.addAttribute("listProducts",list);
		model.addAttribute("listCategories",listCat);
		model.addAttribute("moduleURL", "/products");
		
		return "products/products";
	}
	
	@GetMapping("/products/new")
	public String newProduct(Model model) {
		List<Brand> listBrands = brandSv.listAllBrands();
		Product product = new Product();
		product.setEnabled(true);
		product.setInStock(true);
		
		model.addAttribute("product",product);
		model.addAttribute("listBrands",listBrands);
		model.addAttribute("pageTitle","Create New Product");
		model.addAttribute("numberOfExistingExtraImages",0);
		
		return "products/product_form";
	}
	
	@PostMapping("/products/save")
	public String saveProduct(Product product,RedirectAttributes ra,
			@RequestParam(value =  "fileImage", required = false) MultipartFile mainImageMultipart,
			@RequestParam(value = "extraImage", required = false) MultipartFile[] extraImageMultiparts,
			@RequestParam(name="detailIDs", required = false) String[] detailIDs,
			@RequestParam(name="detailNames", required = false) String[] detailNames,
			@RequestParam(name="detailValues", required = false) String[] detailValues,
			@RequestParam(name="imageIDs", required = false) String[] imageIDs,
			@RequestParam(name="imageNames", required = false) String[] imageNames,
			@AuthenticationPrincipal ShopmeUserDetails loggedUser) throws IOException {
		
			if (loggedUser.hasRole("Salesperson")) {
				productSv.saveProductPrice(product);
				ra.addFlashAttribute("message","The Product has been saved successfully!!!");
				return "redirect:/products";
			}
		
			ProductSaveHelper.setMainImageName(mainImageMultipart, product);
			ProductSaveHelper.setExistingExtraImageNames(imageIDs,imageNames,product);
			ProductSaveHelper.setNewExtraImageNames( extraImageMultiparts,product);
			ProductSaveHelper.setProductDetails(detailIDs,detailNames,detailValues,product);
		
			Product savedProduct = productSv.save(product);
			
			ProductSaveHelper.saveUploadedImages(mainImageMultipart,extraImageMultiparts, savedProduct);
			
			ProductSaveHelper.deleteExtraImagesWereRemovedOnForm(product);
		
			ra.addFlashAttribute("message","The Product has been saved successfully!!!");
			return "redirect:/products";
	}
	

	@GetMapping("/products/{id}/enabled/{status}")
	public String updateCategoryEnabledStatus(@PathVariable("id") Integer id, @PathVariable("status") boolean enabled, 
			RedirectAttributes ra) {
		productSv.updateEnabledStatus(id, enabled);
		String status = enabled ? "enabled" : "disabled";
		String message = "The Product ID " + id + " has been " + status;
		ra.addFlashAttribute("message",message);
		return "redirect:/products";
	}
	
	@GetMapping("/products/delete/{id}")
	public String deleteProduct(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
		try {
			productSv.deleteProduct(id);
			String productExtraImageSir = "../product-images/"+id+ "/extras";
			String productImageSir = "../product-images/"+id;
			
			FileUploadUtil.removeDir(productExtraImageSir);
			FileUploadUtil.removeDir(productImageSir);
			
			ra.addFlashAttribute("message","The Product ID " + id + " has been delete successfully!!!");
		}catch(ProductNotFoundException e) {
			ra.addFlashAttribute("message",e.getMessage());
		}
		return "redirect:/products";
	}
	
	@GetMapping("/products/edit/{id}")
	public String editProduct(@PathVariable("id") Integer id, Model model,
			RedirectAttributes ra) {
		try {
			Product product = productSv.get(id);
			List<Brand> listBrands = brandSv.listAllBrands();
			Integer numberOfExistingExtraImages = product.getImages().size();
			
			
			model.addAttribute("listBrands",listBrands);
			model.addAttribute("product",product);
			model.addAttribute("pageTitle","Edit product (ID: " + id + ")");
			model.addAttribute("numberOfExistingExtraImages",numberOfExistingExtraImages);
			
			
			
			
			return "products/product_form";
		}catch(ProductNotFoundException e) {
			ra.addFlashAttribute("message",e.getMessage());
			return "redirect:/products";
		}
		
	}
	
	@GetMapping("/products/detail/{id}")
	public String viewProductDetails(@PathVariable("id") Integer id, Model model,
			RedirectAttributes ra) {
		try {
			Product product = productSv.get(id);
			Integer numberOfExistingExtraImages = product.getImages().size();
			
			model.addAttribute("product",product);
			model.addAttribute("numberOfExistingExtraImages",numberOfExistingExtraImages);
			
			return "products/product_detail_modal";
		}catch(ProductNotFoundException e) {
			ra.addFlashAttribute("message",e.getMessage());
			return "redirect:/products";
		}
		
	}
}
