package com.shopme.admin.category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shopme.common.entity.Category;
import com.shopme.common.exception.CategoryNotFoundException;

@Service
@Transactional
public class CategoryService {
	public static final int PAGE_SIZE = 5;

	@Autowired
	private CategoryRepository repo;

	public List<Category> listAll(String sortDir, String keyword) {
		List<Category> list = new ArrayList<>();
		if (keyword != null)
			list = repo.findAllCategory(keyword, 1);
		else
			list = (List<Category>) repo.findAll();
		return list;
	}

	public List<Category> listByPage(List<Category> list, String sortDir, int pageNum, String keyword) {
		int n = pageNum;
//		List<Category> list = new ArrayList<>();
//		list = listExam(sortDir,keyword);
//		if (keyword == null) list = listCategoriesUsedInformAndTable(sortDir);
//		else list = listAllByKeyWord(sortDir, keyword);
		int l = list.size();
		List<Category> listPage = new ArrayList<>();
		if (n * PAGE_SIZE < l) {
			for (int i = (n - 1) * PAGE_SIZE; i < n * PAGE_SIZE; i++)
				listPage.add(list.get(i));
		} else {
			for (int i = (n - 1) * PAGE_SIZE; i < l; i++)
				listPage.add(list.get(i));
		}
		return listPage;
	}

//	public Category save(Category category) {
//		Category parent = category.getParent();
//		if (parent != null) {
//			String allParentIds = parent.getAllParentIDs() == null ? "-" : parent.getAllParentIDs();
//			allParentIds += String.valueOf(parent.getId()) + "-";
//			category.setAllParentIDs(allParentIds);
//		}
//		return repo.save(category);
//	}
	
	public Category save(Category category) {
		Category parent = category.getParent();
		if (parent != null) {
			String allParentIds = parent.getAllParentIDs() == null ? "-" : parent.getAllParentIDs();
			allParentIds += String.valueOf(parent.getId()) + "-";
			category.setAllParentIDs(allParentIds);
		}
		
		return repo.save(category);
	}

	public List<Category> listCategoriesUsedInformAndTable(String sortDir) {
		List<Category> list = new ArrayList<>();
		List<Category> catInDB = (List<Category>) repo.findAll();
//		if (keyword == null) catInDB = (List<Category>) repo.findAll();
//		else catInDB = repo.findAllCategory(keyword,1);
		Collections.sort(catInDB, new Comparator<Category>() {
			@Override
			public int compare(Category c1, Category c2) {
				if (sortDir == null || sortDir.equals("asc") || !sortDir.equals("desc"))
					return c1.getName().toLowerCase().compareTo(c2.getName().toLowerCase());
				else
					return c2.getName().toLowerCase().compareTo(c1.getName().toLowerCase());
			}
		});
		for (Category category : catInDB) {
			if (category.getParent() == null) {
				listChildren(list, category, 0, sortDir);
			}
		}
		return list;
	}

	public List<Category> listExam(String sortDir, String keyword) {
		if (keyword != null)
			return listAllByKeyWord(sortDir, keyword);
		else
			return listCategoriesUsedInformAndTable(sortDir);
	}

	public Category get(Integer id) throws CategoryNotFoundException {
		try {
			return repo.findById(id).get();
		} catch (NoSuchElementException e) {
			throw new CategoryNotFoundException("Could not find any category with ID " + id);
		}
	}

	private void listChildren(List<Category> list, Category cat, int n, String sortDir) {
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
			for (Category sub : sortSubCategories(cat.getChildren(), sortDir)) {
				listChildren(list, sub, n, sortDir);
			}
		}
	}

	private SortedSet<Category> sortSubCategories(Set<Category> children, String sortDir) {
		SortedSet<Category> sortedChildren = new TreeSet<>(new Comparator<Category>() {

			@Override
			public int compare(Category o1, Category o2) {
				if (sortDir == null || sortDir.equals("asc") || !sortDir.equals("desc"))
					return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
				else
					return o2.getName().toLowerCase().compareTo(o1.getName().toLowerCase());
			}
		});
		sortedChildren.addAll(children);
		return sortedChildren;
	}

	public String checkUnique(Integer id, String name, String alias) {
		boolean isCreatingNew = (id == null || id == 0);

		Category catByName = repo.findByName(name);
		if (isCreatingNew) {
			if (catByName != null) {
				return "DuplicateName";
			} else {
				Category categoryByAlias = repo.findByAlias(alias);
				if (categoryByAlias != null) {
					return "DuplicateAlias";
				}
			}
		} else {
			if (catByName != null && catByName.getId() != id) {
				return "DuplicateName";
			}
			Category categoryByAlias = repo.findByAlias(alias);
			if (categoryByAlias != null && categoryByAlias.getId() != id) {
				return "DuplicateAlias";
			}
		}
		return "OK";
	}

	public void updateCategoryEnabledStatus(Integer id, boolean enabled) {
		repo.updateEnabledStatus(id, enabled);
	}

	public void delete(Integer id) throws CategoryNotFoundException {
		Long countById = repo.countById(id);
		if (countById == null || countById == 0) {
			throw new CategoryNotFoundException("Could not find any category with ID " + id);
		}

		repo.deleteById(id);
	}

	public List<Category> listAllByKeyWord(String sortDir, String keyword) {
		List<Category> list = repo.findAllCategory(keyword, 1);
		Collections.sort(list, new Comparator<Category>() {
			@Override
			public int compare(Category c1, Category c2) {
				if (sortDir == null || sortDir.equals("asc") || !sortDir.equals("desc"))
					return c1.getName().toLowerCase().compareTo(c2.getName().toLowerCase());
				else
					return c2.getName().toLowerCase().compareTo(c1.getName().toLowerCase());
			}
		});
		return list;
	}
}
