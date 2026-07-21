package ru.leonaugust.link_aggregator.boot.config;// microservice/src/main/java/com/example/boot/UseCaseBeanRegistrar.java

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import ru.leonaugust.link_aggregator.application.UseCase;

@Component
public class UseCaseBeanRegistrar implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // Сканируем пакет application на наличие @UseCase
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(UseCase.class));

        // Находим все candidate компоненты
        var candidates = scanner.findCandidateComponents("ru.leonaugust.link_aggregator");

        for (var candidate : candidates) {
            try {
                Class<?> clazz = Class.forName(candidate.getBeanClassName());
                GenericBeanDefinition beanDef = new GenericBeanDefinition();
                beanDef.setBeanClass(clazz);
                // Автоматическое определение имени бина — по имени класса
                String beanName = clazz.getSimpleName();
                // Можно также указать, что бин должен быть синглтоном (по умолчанию)
                beanDef.setAutowireMode(GenericBeanDefinition.AUTOWIRE_CONSTRUCTOR);
                registry.registerBeanDefinition(beanName, beanDef);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // noop
    }
}