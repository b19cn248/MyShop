package com.shopme;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.shopme.category.CategoryService;
import com.shopme.common.entity.Category;

@Controller
public class MainController {

	@Autowired private CategoryService sv;
	
	@GetMapping("")
	public String viewHomePage(Model model) {
		List<Category> list = sv.listNoChildrenCategories();
		model.addAttribute("listCategories",list);
		return "index";
	}
	
}
