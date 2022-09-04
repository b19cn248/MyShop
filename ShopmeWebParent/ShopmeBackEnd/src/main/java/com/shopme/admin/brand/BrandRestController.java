package com.shopme.admin.brand;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopme.common.entity.Brand;
import com.shopme.common.entity.Category;

@RestController
public class BrandRestController {
	@Autowired
	private BrandService sv;
	
	@PostMapping("/brands/check_unique")
	public String checkUnique(@Param("id") Integer id, @Param("name") String name) {
		return sv.checkUnique(id, name);
	}
	
	@GetMapping("/brands/{id}/categories")
	public List<CategoryDTO> listCategoriesByBrand(@PathVariable(name="id") Integer BrandId) throws BrandNotFoundRestException{
		List<CategoryDTO> list = new ArrayList<>();
		try {
			Brand brand = sv.get(BrandId);
			Set<Category> categories = brand.getCategories();
			for (Category cat:categories) {
				CategoryDTO dto = new CategoryDTO(cat.getId(),cat.getName());
				list.add(dto);
			}
			return list;
		}catch(BrandNotFoundException e) {
			throw new BrandNotFoundRestException();
		}
		
	}
}
