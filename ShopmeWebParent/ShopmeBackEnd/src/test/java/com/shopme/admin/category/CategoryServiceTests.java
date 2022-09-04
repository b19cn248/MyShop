package com.shopme.admin.category;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.shopme.common.entity.Category;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
public class CategoryServiceTests {
	
	@MockBean
	private CategoryRepository repo;
	
	@InjectMocks
	private CategoryService sv;
	
	@Test
	public void test1() {
		Integer id = null;
		String name = "Computers";
		String alias = "abc";
		Category cat = new Category(id,name,alias);
		
		Mockito.when(repo.findByName(name)).thenReturn(cat);
		Mockito.when(repo.findByAlias(alias)).thenReturn(null);
		
		String result = sv.checkUnique(id, name, alias);
		
		assertThat(result).isEqualTo("DuplicateName");
	}
	
	@Test
	public void test2() {
		Integer id = null;
		String name = "abc";
		String alias = "Computers";
		Category cat = new Category(id,name,alias);
		
		Mockito.when(repo.findByName(name)).thenReturn(null);
		Mockito.when(repo.findByAlias(alias)).thenReturn(cat);
		
		String result = sv.checkUnique(id, name, alias);
		
		assertThat(result).isEqualTo("DuplicateAlias");
	}

	@Test
	public void test3() {
		Integer id = null;
		String name = "abc";
		String alias = "Coddmputers";
		
		Mockito.when(repo.findByName(name)).thenReturn(null);
		Mockito.when(repo.findByAlias(alias)).thenReturn(null);
		
		String result = sv.checkUnique(id, name, alias);
		
		assertThat(result).isEqualTo("OK");
	}
	
	@Test
	public void test4() {
		Integer id = 1;
		String name = "Computers";
		String alias = "Coddmputers";
		Category cat = new Category(2,name,alias);
		
		Mockito.when(repo.findByName(name)).thenReturn(cat);
		Mockito.when(repo.findByAlias(alias)).thenReturn(null);
		
		String result = sv.checkUnique(id, name, alias);
		
		assertThat(result).isEqualTo("DuplicateName");
	}
	
	@Test
	public void test5() {
		Integer id = 1;
		String name = "Compuddters";
		String alias = "Computers";
		Category cat = new Category(2,name,alias);
		
		Mockito.when(repo.findByName(name)).thenReturn(null);
		Mockito.when(repo.findByAlias(alias)).thenReturn(cat);
		
		String result = sv.checkUnique(id, name, alias);
		
		assertThat(result).isEqualTo("DuplicateAlias");
	}
	
	@Test
	public void test6() {
		Integer id = 1;
		String name = "Compuddters";
		String alias = "Compussters";
		
		Category cat = new Category(id,name,alias);
		
		Mockito.when(repo.findByName(name)).thenReturn(null);
		Mockito.when(repo.findByAlias(alias)).thenReturn(cat);
		
		String result = sv.checkUnique(id, name, alias);
		
		assertThat(result).isEqualTo("OK");
	}
}
