package com.myspringtest.dao;

import org.springframework.stereotype.Repository;

@Repository
public class IndexDao implements Dao {

	public void query(){
		System.out.println("index");
	}

}
