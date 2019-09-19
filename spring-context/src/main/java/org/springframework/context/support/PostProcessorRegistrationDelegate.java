/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.lang.Nullable;

/**
 * Delegate for AbstractApplicationContext's post-processor handling.
 *
 * @author Juergen Hoeller
 * @since 4.0
 */
final class PostProcessorRegistrationDelegate {

	/**
	 * 我想吐槽: 下边有两大陀代码, 看似逻辑差不多, 但是写了很多重复代码; 两个分类的依据是一样的, 但是分类的方式却有一点点区别。。。
	 * 进入正题:
	 * 这个方法主要处理了所有的BeanFactoryPostProcessor和它的子类BeanDefinitionRegistryPostProcessor, 主要执行的方法是:
	 * (1)所有的BeanDefinitionRegistryPostProcessor扩展的方法postProcessBeanDefinitionRegistry
	 * (2)所有的BeanDefinitionRegistryPostProcessor父类的方法postProcessBeanFactory
	 * (3)所有的BeanFactoryPostProcessor的方法postProcessBeanFactory
	 */
	public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

		// Invoke BeanDefinitionRegistryPostProcessors first, if any.
		Set<String> processedBeans = new HashSet<>();

		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
			// 存放用户自定义的实现了BeanFactoryPostProcessor接口的对象
			List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
			// 存放用户自定义的实现了BeanDefinitionRegistryPostProcessor接口的对象, BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor
			List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();
			// 循环用户自定义的beanFactoryPostProcessors放到上面两个不同的list里
			for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryProcessor = (BeanDefinitionRegistryPostProcessor) postProcessor;
					// 在这里执行了自定义的BeanDefinitionRegistryPostProcessor扩展的第三个方法postProcessBeanDefinitionRegistry
					registryProcessor.postProcessBeanDefinitionRegistry(registry);
					registryProcessors.add(registryProcessor);
				} else {
					regularPostProcessors.add(postProcessor);
				}
			}

			// Do not initialize FactoryBeans here: We need to leave all regular beans
			// uninitialized to let the bean factory post-processors apply to them!
			// Separate between BeanDefinitionRegistryPostProcessors that implement
			// PriorityOrdered, Ordered, and the rest.
			// 存放spring内部自己实现了BeanDefinitionRegistryPostProcessor接口的对象
			List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

			/**
			 * ==========start
			 * 接下来下边三步主要执行所有的BeanDefinitionRegistryPostProcessor的扩展方法postProcessBeanDefinitionRegistry:
			 * First: 调用实现PriorityOrdered的BeanDefinitionRegistryPostProcessor
			 * Next: 调用实现Ordered的BeanDefinitionRegistryPostProcessor
			 * Finally: 调用所有其他的BeanDefinitionRegistryPostProcessor
			 * ==========start
			 */

			// First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
			/**
			 * 根据bean的类型(BeanDefinition当中描述当前类的class类型)获取bean的名字
			 * 这里其实只会拿到ConfigurationClassPostProcessor一个类, 因为在前边先放了spring自定义的6个类, 1个beanFactoryPostProcessor, 5个beanPostProcessor
			 * 此时beanFactory正在实例化, 实例化时通过回调别的类(ConfigurationClassPostProcessor)来做要做的事情, 因为这部分代码其实不适合写在这个方法当中, 通过委托实现了这两个接口的多个外部类来做不同的事情, 实现解耦, 也易于扩展
			 */
			String[] postProcessorNames =
					beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			/**
			 * 这个地方可以得到一个BeanFactoryPostProcessor, 其实就是上面的ConfigurationClassPostProcessor类
			 * 为什么要在最开始注册这6个类呢?
			 * 因为spring的工厂需要去解析去扫描等等功能，而这些功能都是需要在spring工厂初始化完成之前执行
			 * 要么在工厂最开始的时候、要么在工厂初始化之中，反正不能再之后。因为如果再之后就没有意义, 因为那个时候已经需要使用工厂了
			 * 所以这里spring在一开始就注册了一个BeanFactoryPostProcessor, 用来插手spring factory的实例化过程, 这个类就是ConfigurationClassPostProcessor
			 * 下面我们对这个牛逼哄哄的类(他能插手spring工厂的实例化过程还不牛逼吗?)的作用参考源码重点解释
			 */
			for (String ppName : postProcessorNames) {
				// 因为ConfigurationClassPostProcessor implements PriorityOrdered，所以会进
				if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			// 排序不重要, 况且currentRegistryProcessors这里也只有一个数据
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			// 合并list(为什么要合并? 因为还有自己的, 合并起来统一处理)
			registryProcessors.addAll(currentRegistryProcessors);
			// 很重要。在这里执行了spring自定义的BeanDefinitionRegistryPostProcessor扩展的第三个方法postProcessBeanDefinitionRegistry
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			// 执行完成了所有BeanDefinitionRegistryPostProcessor, 这个list只是一个临时变量, 故而要清除
			currentRegistryProcessors.clear();

			// Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
			postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
			for (String ppName : postProcessorNames) {
				if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
					currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
					processedBeans.add(ppName);
				}
			}
			sortPostProcessors(currentRegistryProcessors, beanFactory);
			registryProcessors.addAll(currentRegistryProcessors);
			invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
			currentRegistryProcessors.clear();

			// Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
			boolean reiterate = true;
			while (reiterate) {
				reiterate = false;
				postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
				for (String ppName : postProcessorNames) {
					if (!processedBeans.contains(ppName)) {
						currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
						processedBeans.add(ppName);
						reiterate = true;
					}
				}
				sortPostProcessors(currentRegistryProcessors, beanFactory);
				registryProcessors.addAll(currentRegistryProcessors);
				invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
				currentRegistryProcessors.clear();
			}

			/**
			 * ==========end
			 * 所有的BeanDefinitionRegistryPostProcessor的扩展方法postProcessBeanDefinitionRegistry执行完毕
			 * ==========end
			 */

			// Now, invoke the postProcessBeanFactory callback of all processors handled so far.
			// 这里开始执行的是BeanDefinitionRegistryPostProcessor中的postProcessBeanFactory(java基础: 子类也需要执行父类的方法)(在这里会有cglib代理, 里边会解释full和lite的作用)
			invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
			// 这里开始执行的是自定义实现BeanFactoryPostProcessor接口的子类中的postProcessBeanFactory
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
		} else {
			// Invoke factory processors registered with the context instance.
			invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
		}

		/**
		 * ==========start
		 * 主要执行其他的实现了BeanFactoryPostProcessor接口的类(除了用户自定义实现BeanFactoryPostProcessor接口的子类)的方法postProcessBeanFactory:
		 * 先按照实现接口的不同分类: PriorityOrdered、Ordered和其他
		 * First: 调用实现PriorityOrdered的BeanFactoryPostProcessor
		 * Next: 调用实现Ordered的BeanFactoryPostProcessor
		 * Finally: 调用其他的BeanFactoryPostProcessor
		 * ==========start
		 */

		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let the bean factory post-processors apply to them!
		// 从工厂里获取所有BeanFactoryPostProcessor类型的bean的名字
		String[] postProcessorNames =
				beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// Separate between BeanFactoryPostProcessors that implement PriorityOrdered, Ordered, and the rest.
		// 将实现PriorityOrdered、Ordered和其他接口的BeanFactoryPostProcessors分开
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
		// 吐槽: 这里真搞笑, 不直接new List<BeanFactoryPostProcessor>, 在后边又多写代码来处理, 搞不懂。。。
		List<String> orderedPostProcessorNames = new ArrayList<>();
		// 吐槽: 这里真搞笑, 不直接new List<BeanFactoryPostProcessor>, 在后边又多写代码来处理, 搞不懂。。。
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
		for (String ppName : postProcessorNames) {
			// 如果processedBeans里包含, 跳过, 因为再往下的代码执行起来也没有意义了
			if (processedBeans.contains(ppName)) {
				// skip - already processed in first phase above
			} else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			} else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			} else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// Next, invoke the BeanFactoryPostProcessors that implement Ordered.
		// 吐槽: 这里真搞笑, 不直接在上边处理了, 在这里又多写代码来处理, 搞不懂。。。
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// Finally, invoke all other BeanFactoryPostProcessors.
		// 吐槽: 这里真搞笑, 不直接在上边处理了, 在这里又多写代码来处理, 搞不懂。。。
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

		/**
		 * ==========end
		 * 其他的实现了BeanFactoryPostProcessor接口的类的方法postProcessBeanFactory执行完毕
		 * ==========end
		 */

		// Clear cached merged bean definitions since the post-processors might have
		// modified the original metadata, e.g. replacing placeholders in values...
		// 最后清除一下缓存
		beanFactory.clearMetadataCache();
	}

	/**
	 * 这里的添加指的是beanFactory.addBeanPostProcessor()直接添加到工厂的beanPostProcessors属性
	 * 之前已经添加了三个后置处理器:
	 * prepareBeanFactory()时注册了两个: ApplicationContextAwareProcessor ApplicationListenerDetector
	 * ConfigurationClassPostProcessor执行父类postProcessBeanFactory()时注册了一个: ConfigurationClassPostProcessor.ImportAwareBeanPostProcessor
	 * 这个方法会再注册四个后置处理器:
	 * PostProcessorRegistrationDelegate.BeanPostProcessorChecker
	 * CommonAnnotationBeanPostProcessor
	 * AutowiredAnnotationBeanPostProcessor
	 * RequiredAnnotationBeanPostProcessor
	 * 这个方法结束后一共有七个后置处理器, 但是这七个里没有一个是处理AOP的
	 * 如果开启了AOP, 在这个方法里还会注入AOP的后置处理器: AnnotationAwareAspectJAutoProxyCreator
	 */
	public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

		// 从beanDefinitionMap中得到所有的BeanPostProcessor
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		/**
		 * 这里会注册后置处理器: PostProcessorRegistrationDelegate.BeanPostProcessorChecker
		 */
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// Separate between BeanPostProcessors that implement PriorityOrdered, Ordered, and the rest.
		// 翻译: 把实现了(PriorityOrdered，Ordered，其它)接口的BeanPostProcessors区分开
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>(); // PriorityOrdered
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>(); // MergedBeanDefinitionPostProcessor
		List<String> orderedPostProcessorNames = new ArrayList<>(); // Ordered
		List<String> nonOrderedPostProcessorNames = new ArrayList<>(); // rest
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			} else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			} else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}
		//priorityOrderedPostProcessors.remove(1); // 自己测试去掉RequiredAnnotationBeanPostProcessor的情况, 不会解析@Required注解
		// First, register the BeanPostProcessors that implement PriorityOrdered.
		sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		/**
		 * 这里会注册后置处理器:
		 * CommonAnnotationBeanPostProcessor
		 * AutowiredAnnotationBeanPostProcessor
		 * RequiredAnnotationBeanPostProcessor
		 */
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, register the BeanPostProcessors that implement Ordered.
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Now, register all regular BeanPostProcessors.
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		/**
		 * 如果开启了AOP, 在这里还会注入AOP的后置处理器: AnnotationAwareAspectJAutoProxyCreator
		 */
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// Finally, re-register all internal BeanPostProcessors.
		//internalPostProcessors.remove(1);  // 自己测试去掉RequiredAnnotationBeanPostProcessor的情况, 不会解析@Required注解
		sortPostProcessors(internalPostProcessors, beanFactory);
		/**
		 * 重新注册CommonAnnotationBeanPostProcessor AutowiredAnnotationBeanPostProcessor RequiredAnnotationBeanPostProcessor三个
		 * 是为了改变顺序, 如果有AOP的后置处理器, 把它的顺序放到这三个前面
		 */
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
		/**
		 * 重新注册ApplicationListenerDetector, 为了将它的顺序移动到最末尾(用于获取代理等)
		 */
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
	}

	private static void sortPostProcessors(List<?> postProcessors, ConfigurableListableBeanFactory beanFactory) {
		Comparator<Object> comparatorToUse = null;
		if (beanFactory instanceof DefaultListableBeanFactory) {
			comparatorToUse = ((DefaultListableBeanFactory) beanFactory).getDependencyComparator();
		}
		if (comparatorToUse == null) {
			comparatorToUse = OrderComparator.INSTANCE;
		}
		postProcessors.sort(comparatorToUse);
	}

	/**
	 * Invoke the given BeanDefinitionRegistryPostProcessor beans.
	 * 注意对比这两个类中的方法: BeanDefinitionRegistryPostProcessor和BeanFactoryPostProcessor
	 */
	private static void invokeBeanDefinitionRegistryPostProcessors(
			Collection<? extends BeanDefinitionRegistryPostProcessor> postProcessors, BeanDefinitionRegistry registry) {
		for (BeanDefinitionRegistryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanDefinitionRegistry(registry);
		}
	}

	/**
	 * Invoke the given BeanFactoryPostProcessor beans.
	 */
	private static void invokeBeanFactoryPostProcessors(
			Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}

	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		for (BeanPostProcessor postProcessor : postProcessors) {
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}


	/**
	 * BeanPostProcessor that logs an info message when a bean is created during
	 * BeanPostProcessor instantiation, i.e. when a bean is not eligible for
	 * getting processed by all BeanPostProcessors.
	 * 当Spring的配置中的后置处理器还没有被注册就已经开始了bean的初始化, 便会打印出BeanPostProcessorChecker中设定的信息
	 */
	private static final class BeanPostProcessorChecker implements BeanPostProcessor {

		private static final Log logger = LogFactory.getLog(BeanPostProcessorChecker.class);

		private final ConfigurableListableBeanFactory beanFactory;

		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			// 怎么检查? 比较后置处理器的数量 和 当前执行过的后置处理器的数量, 不一致就报错
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (!(bean instanceof BeanPostProcessor) && !isInfrastructureBean(beanName) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' of type [" + bean.getClass().getName() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}

		private boolean isInfrastructureBean(@Nullable String beanName) {
			if (beanName != null && this.beanFactory.containsBeanDefinition(beanName)) {
				BeanDefinition bd = this.beanFactory.getBeanDefinition(beanName);
				return (bd.getRole() == RootBeanDefinition.ROLE_INFRASTRUCTURE);
			}
			return false;
		}
	}

}
