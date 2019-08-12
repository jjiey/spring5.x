package com.myspringtest.test;

import com.myspringtest.app.Appconfig;
import com.myspringtest.dao.IndexDao;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TestMain1 {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(Appconfig.class);
		IndexDao indexDao = annotationConfigApplicationContext.getBean(IndexDao.class);
		indexDao.query();
	}

}
