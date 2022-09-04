package com.shopme.admin.brand;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import com.shopme.common.entity.Brand;
import com.shopme.common.entity.Category;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class BrandRepositoryTests {

	@Autowired
	private BrandRepository repo;
	
	@Test 
	public void testCreateBrand1() {
		Category laptops = new Category(6);
		Brand acer = new Brand("Acer");
		acer.getCategories().add(laptops);
		Brand savedBrand = repo.save(acer);
		
		assertThat(savedBrand).isNotNull();
		assertThat(savedBrand.getId()).isGreaterThan(0);
	}
	
	@Test 
	public void testCreateBrand2() {
		Category cellphones = new Category(4);
		Category tablets = new Category(7);
	
		Brand apple = new Brand("Apple");
		
		
		apple.getCategories().add(cellphones);
		apple.getCategories().add(tablets);
		
		Brand savedBrand = repo.save(apple);
		
		assertThat(savedBrand).isNotNull();
		assertThat(savedBrand.getId()).isGreaterThan(0);
	}
	
	@Test 
	public void testCreateBrand3() {
	
		Brand samsung = new Brand("Samsung");
		
		
		samsung.getCategories().add(new Category(29));
		samsung.getCategories().add(new Category(24));
		
		Brand savedBrand = repo.save(samsung);
		
		assertThat(savedBrand).isNotNull();
		assertThat(savedBrand.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testFindAll() {
		List<Brand> list = repo.findAll();
		list.forEach(System.out::println);
		assertThat(list).isNotEmpty();
	}
	
	@Test
	public void findByID() {
		Brand brand = repo.findById(2).get();
		assertThat(brand.getName()).isEqualTo("Apple");
	}
	
	@Test
	public void testUpdateName() {
		String name = "Samsung Electronics";
		Brand samsung = repo.findById(3).get();
		samsung.setName(name);
		Brand savedBrand = repo.save(samsung);
		assertThat(savedBrand.getName()).isEqualTo(name);
	}
	
	@Test
	public void testDelete() {
		Integer id = 4;
		repo.deleteById(id);
		
		Optional<Brand> result = repo.findById(id);
		assertThat(result.isEmpty());
	}
}
