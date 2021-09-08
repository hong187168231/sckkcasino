package com.qianyi.casinocore.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.qianyi.casinocore.repository.SysDictRepository;

@Service
public class SysDictService {
	
    @Autowired
    SysDictRepository sysDictRepository;

    public List<SysDict> findAll() {
        return sysDictRepository.findAll();
    }
    
    public List<SysDict> findExample(SysDict sysDict) {
        return sysDictRepository.findAll(Example.of(sysDict));
    }
    
    public SysDict save(SysDict sysDict) {
    	return sysDictRepository.save(sysDict);
    }
    
    public void deleteById(SysDict sysDict) {
    	long id = (long)sysDict.getId();
    	sysDictRepository.deleteById(id);
    }
    
}
