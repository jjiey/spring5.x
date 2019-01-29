package com.myspringtest.dao;

import org.springframework.stereotype.Component;

@Component
public class IndexDao implements Dao {

	public void query(){
		System.out.println("index");
	}

}
