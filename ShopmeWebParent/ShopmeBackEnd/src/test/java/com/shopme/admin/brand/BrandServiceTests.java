package com.shopme.admin.brand;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.shopme.common.entity.Brand;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
public class BrandServiceTests {

	@MockBean private BrandRepository repo;
	
	@InjectMocks private BrandService sv;
	
	@Test
	public void testCheck() {
		Integer id = null;
		String name = "Acer";
		Brand brand = new Brand(name);
		
		Mockito.when(repo.findByName(name)).thenReturn(brand);
		
		String result = sv.checkUnique(id, name);
		assertThat(result).isEqualTo("Duplicated");
	}
	
	@Test
	public void testCheck1() {
		Integer id = null;
		String name = "Kadfa";
		
		Mockito.when(repo.findByName(name)).thenReturn(null);
		
		String result = sv.checkUnique(id, name);
		assertThat(result).isEqualTo("OKfdsa");
	}
	
	@Test
	public void testCheck2() {
		Integer id = 1;
		String name = "Canon";
		Brand brand = new Brand(id,name);
		
		Mockito.when(repo.findByName(name)).thenReturn(brand);
		
		String result = sv.checkUnique(2, "Canon");
		assertThat(result).isEqualTo("Duplicated");
	}
	
}
