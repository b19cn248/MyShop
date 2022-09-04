package com.shopme.admin.category;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import com.shopme.common.entity.Category;

@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class CategoryRepositoryTests {

	@Autowired
	private CategoryRepository repo;
	
	@Test
	public void testCreatRootCategory() {
		Category cat = new Category("Electronics");
		Category saveCat = repo.save(cat);
		assertThat(saveCat.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testCreateSubCategory() {
		Category parent = new Category(7);
		Category sub = new Category("iPhone X",parent);
		repo.save(sub);
		assertThat(sub.getId()).isGreaterThan(0);
	}
	
	@Test
	public void testGetCategory() {
		Category cat = repo.findById(2).get();
		System.out.println(cat.getName());
		
		Set<Category> children = cat.getChildren();
		for (Category sub:children) {
			System.out.println(sub.getName());
		}
		assertThat(children.size()).isGreaterThan(0);
	}
	
	@Test
	public void testPrintHierarchialCategories() {
		Iterable<Category> categories = repo.findAll();
		for (Category category : categories) {
			if (category.getParent() == null) {
				System.out.println(category.getName());
				Set<Category> children = category.getChildren();
				for (Category subCategory:children) {
					System.out.println("--"+subCategory.getName());
					printChildren(subCategory, 1);
				}
			}
		}
	}
	
	@Test
	public void testDemo() {
		Iterable<Category> categories = repo.findAll();
		for (Category category : categories) {
			if (category.getParent()==null) {
				printDemo(category,0);
			}
		}
	}
	
	private void printDemo(Category cat,int n) {
		for (int i=1;i<n;i++) System.out.print("--");
		System.out.println(cat.getName());
		n++;
		if (cat.getChildren().size()==0) return ;
		else {
			for (Category sub:cat.getChildren()) {
				printDemo(sub,n);
			}
		}
	}
	
	private void printChildren(Category parent, int subLevel) {
		int newSubLevel = subLevel+1;
		
		Set<Category> children = parent.getChildren();
		
		for (Category subCategory:children) {
			for (int i=0;i<newSubLevel;i++) {
				System.out.print("--");
			}
			System.out.println(subCategory.getName());
			printChildren(subCategory, newSubLevel);
		}
	}
	
	@Test
	public void listCategoriesUsedInform() {
		List<Category> list = new ArrayList<>();
		Iterable<Category> catInDB = repo.findAll();
		for (Category category : catInDB) {
			if (category.getParent()==null) {
				listChildren(list, category, 0);
			}
		}
		Collections.sort(list, new Comparator<Category>() {
			  @Override
			  public int compare(Category c1, Category c2) {
			    return c1.getName().compareTo(c2.getName());
			  }
			});
		for (Category cat:list) {
			System.out.println(cat.getId()+" "+cat.getName()+" "+cat.getImagePath());
		}
	}
	
	private void listChildren(List<Category> list, Category cat,int n) {
		String s = "";
		for (int i = 1; i <= n; i++) {
			s += "--";
		}
		s += cat.getName();
		Category cate = Category.copyFull(repo.findById(cat.getId()).get()); 
		cate.setName(s);
		list.add(cate);
		n++;
		if (cat.getChildren().size() == 0)
			return;
		else {
			for (Category sub : cat.getChildren()) {
				listChildren(list, sub, n);
			}
		}
	}
	
	@Test
	public void testListRootCategories() {
		List<Category> listInDB = (List<Category>) repo.findAll();
		Collections.sort(listInDB, new Comparator<Category>() {
			  @Override
			  public int compare(Category c1, Category c2) {
			    return c1.getName().compareTo(c2.getName());
			  }
			});
		listInDB.forEach(cat -> System.out.println(cat.getName()));
	}
	
	@Test
	public void testFindByName() {
		String name = "Computers1";
		Category category  = repo.findByName(name);
		
		assertThat(category).isNotNull();
		assertThat(category.getName()).isEqualTo(name);
	}
	
	@Test
	public void testFindByAlias() {
		String alias = "Electronics";
		Category category  = repo.findByAlias(alias);
		
		assertThat(category).isNotNull();
		assertThat(category.getAlias()).isEqualTo(alias);
	}
	
	@Test
	public void testttt() {
		List<Category> list = repo.findAllCategory("com",1);
		list.forEach(cat -> System.out.println(cat.getId()+" "+cat.getName()));
	}
	
	@Test
	public void updateParent() {
		List<Category> list = (List<Category>) repo.findAll();
		for (Category cat:list) {
			Category parent = cat.getParent();
			if (parent != null) {
				String allParentIds = parent.getAllParentIDs() == null ? "-" : parent.getAllParentIDs();
				allParentIds += String.valueOf(parent.getId()) + "-";
				cat.setAllParentIDs(allParentIds);
			}
			
			repo.save(cat);
		}
	}
	
}
