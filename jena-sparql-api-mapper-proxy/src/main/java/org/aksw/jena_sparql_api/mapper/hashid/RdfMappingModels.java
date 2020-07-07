package org.aksw.jena_sparql_api.mapper.hashid;

public class RdfMappingModels {
    private static RdfMappingModel INSTANCE = null;

    public static RdfMappingModel get() {
        if(INSTANCE == null) {
            synchronized(RdfMappingModels.class) {
                if(INSTANCE == null) {
                    INSTANCE = new RdfMappingModelImpl();
                }
            }
        }

        return INSTANCE;
    }



}
