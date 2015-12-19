package org.aksw.jena_sparql_api.batch.step;

import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

public class FactoryBeanStepLog
    extends FactoryBeanStepBase
    implements ApplicationContextAware
{
    protected String text;

    public String getText() {
        return text;
    }

    public void setText(String message) {
        this.text = message;
    }

    @Override
    protected Step configureStep(StepBuilder stepBuilder) {
        if(text.startsWith("##")) {
            text = "#{ " + text.substring(2) + " }";
        }

        RootBeanDefinition bd = new RootBeanDefinition(TaskletLog.class);
        bd.setScope("step");
        bd.getPropertyValues()
            .add("text", text)
            ;

        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)((ConfigurableApplicationContext)ctx).getBeanFactory();
        String name = "mytest";
        beanFactory.registerBeanDefinition(name, bd);

        String proxyName = name + "-proxy";
        RootBeanDefinition proxyBd = new RootBeanDefinition(ScopedProxyFactoryBean.class);
        proxyBd.getPropertyValues()
            .add("targetBeanName", name);
        beanFactory.registerBeanDefinition(proxyName, proxyBd);



        Tasklet tasklet = (Tasklet)beanFactory.getBean(proxyName);

        //Tasklet tasklet = new TaskletLog(text);
        Step result = stepBuilder
                .tasklet(tasklet)
                .build();

        return result;
    }

    ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }



}
