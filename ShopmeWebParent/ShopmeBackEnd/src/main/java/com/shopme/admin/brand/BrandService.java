package com.shopme.admin.brand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shopme.common.entity.Brand;

@Service
public class BrandService {
	public static final int PAGE_SIZE = 10;

	@Autowired
	private BrandRepository repo;
	
	public List<Brand> listAll(){
		return repo.findAll();
	}
	
	public List<Brand> listAllBrands(){
		return repo.findAllBrands();
	}
	
	public Brand save(Brand brand) {
		return repo.save(brand);
	}
	
	public Brand get(Integer id) throws BrandNotFoundException {
		try {
			return repo.findById(id).get();
		}catch(NoSuchElementException e) {
			throw new BrandNotFoundException("Could not find any brand with ID: "+id);
		}
	}
	
	public void delete(Integer id) throws BrandNotFoundException {
		Long countbyId = repo.countById(id);
		if (countbyId==null || countbyId==0) {
			throw new BrandNotFoundException("Could not find any brand with ID: "+id);
		}
		repo.deleteById(id);
	}
	
	public String checkUnique(Integer id, String name) {
		boolean isCreatingNew = (id==null || id==0);
		Brand brandByName = repo.findByName(name);
		
		if (isCreatingNew) {
			if (brandByName != null) return "Duplicated";
		}else {
			if (brandByName !=null & brandByName.getId() !=id) {
				return "Duplicated";
			}
		}
		
		return "OK";
	}
	
	public List<Brand> listByPage(List<Brand> list,int pageNum,String sortDir){
		
		Collections.sort(list, new Comparator<Brand>() {
			  @Override
			  public int compare(Brand b1, Brand b2) {
				if (sortDir == null || sortDir.equals("asc") || !sortDir.equals("desc") ) 
			    return b1.getName().toLowerCase().compareTo(b2.getName().toLowerCase());
				else return b2.getName().toLowerCase().compareTo(b1.getName().toLowerCase());
			  }
			});
		
		int n = pageNum;
		int l = list.size();
		List<Brand> listPage = new ArrayList<>();
		if (n*PAGE_SIZE<l) {
			for (int i=(n-1)*PAGE_SIZE;i<n*PAGE_SIZE;i++) listPage.add(list.get(i));
		}
		else {
			for (int i=(n-1)*PAGE_SIZE;i<l;i++) listPage.add(list.get(i));
		}
		return listPage;
	}
	
	public List<Brand> listByKeyWord(String keyword){
		if (keyword==null) return listAll();
		else return repo.findAllBrand(keyword, 1);
	}
}
