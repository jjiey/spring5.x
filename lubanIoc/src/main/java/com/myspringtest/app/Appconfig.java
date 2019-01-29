package com.myspringtest.app;

import com.myspringtest.imports.MyImportSelector;
import org.springframework.context.annotation.*;

@Configuration
@ComponentScan({"com.myspringtest"})
@Import(MyImportSelector.class)
public class Appconfig {

//	@Bean
//	public IndexDao1 indexDao1(){
//		return new IndexDao1();
//	}
//
//	@Bean
//	public IndexDao indexDao(){
//		indexDao1();
//		indexDao1();
//		return new IndexDao();
//	}

}
