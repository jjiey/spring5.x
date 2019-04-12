package com.myspringtest.app;

import com.myspringtest.dao.IndexDao3;
import com.myspringtest.dao.IndexDao4;
import com.myspringtest.imports.MyImportBeanDefinitionRegistrar;
import com.myspringtest.imports.MyImportSelector;
import org.springframework.context.annotation.*;

@Configuration
@ComponentScan({"com.myspringtest"})
//@Import(MyImportSelector.class)
@Import(MyImportBeanDefinitionRegistrar.class)
//@EnableAspectJAutoProxy
public class Appconfig {

	/*
	不加@Configuration，会打印两遍：IndexDao3 init
	加上@Configuration，会打印一遍：IndexDao3 init
	加上，至少证明了indexDao3()不再返回new IndexDao3()这个对象了，这个方法肯定被改变了
	改变方法的行为，就是去代理Appconfig，根本不去调new IndexDao3()方法
	spring就是为了解决这个问题才去给它加的代理

	如果加了static，会打印两遍，和底层生成bean的机制有关系
	 */

	/**
	 * 我跟了一下源码，不加@Scope的时候跟视频中讲的一样（理解），但是加了之后为什么先调用的是indexDao4（不理解 todo）？
	 * 源码位置：ConfigurationClassEnhancer中if (isCurrentlyInvokedFactoryMethod(beanMethod))
	 * 另外发现AbstractApplicationContext#finishBeanFactoryInitialization中，DefaultListableBeanFactory#preInstantiateSingletons才会去调代理中的方法
	 */
	@Bean
	//@Scope("prototype")
	public IndexDao3 indexDao3(){
		return new IndexDao3();
	}

	@Bean
	public IndexDao4 indexDao4(){
		indexDao3();
		return new IndexDao4();
	}

}
