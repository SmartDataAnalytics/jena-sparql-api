package org.aksw.jena_sparql_api.batch.processor;

import java.io.InputStream;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.aksw.commons.util.StreamUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class MainJavaScriptTest {
    
    public static void main(String[] args) throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("myFn.js");
        
        
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");
        
        //engine.getContext().
        //Context context = new Context()
        //engine.setContext(context);
        
        InputStream in = resource.getInputStream();
        String str = StreamUtils.toStringSafe(in);
//        
//        Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
        engine.eval(str);
        Invocable inv = (Invocable) engine;

        try {
            Object val = inv.invokeFunction("myFn", "test");
            System.out.println(val);
        } catch (ScriptException e) {
            System.out.println(e.getMessage() + " " + e.getLineNumber() + " " + e.getColumnNumber() + " " + e.getFileName());
        }


    }
}
