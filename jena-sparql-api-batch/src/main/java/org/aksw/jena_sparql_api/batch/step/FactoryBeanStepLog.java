package org.aksw.jena_sparql_api.batch.step;

import org.aksw.spring.bean.util.BeanDefinitionProxyUtils;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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
        Tasklet tmp = new TaskletLog(text);
        Tasklet tasklet = BeanDefinitionProxyUtils.createScopedProxy(ctx, tmp, "step", null);

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
