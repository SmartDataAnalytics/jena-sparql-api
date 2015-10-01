package org.aksw.jena_sparql_api.batch.cli.main;

import java.math.BigDecimal;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LazilyParsedNumber;

public class JobParametersJsonUtils {
    public static void processArg(JobParametersBuilder builder, String key, JsonPrimitive p, boolean isIdentifying) {
        if(p.isBoolean()) {
            boolean tmp = p.getAsBoolean();
            Long v = tmp == true ? 1l : 0l;
            builder.addLong(key, v, isIdentifying);
        } else if(p.isString()) {
            String tmp = p.getAsString();
            builder.addString(key, tmp, isIdentifying);
        } else if(p.isNumber()) {
            Number n = p.getAsNumber();
            if(n instanceof LazilyParsedNumber) {
                LazilyParsedNumber lpn = (LazilyParsedNumber)n;
                String tmp = lpn.toString();

                boolean endsWithDotZero = tmp.endsWith(".0");
                if (tmp != null && tmp.contains(".") && !endsWithDotZero) {
                    n = Double.parseDouble(tmp);
                } else {
                    if(endsWithDotZero) {
                        tmp = tmp.substring(0, tmp.length() - 2);
                    }

                    n = Long.parseLong(tmp);
                }
            }

            if(n instanceof Integer || n instanceof Long || n instanceof BigDecimal) {
                Long tmp = n.longValue();
                builder.addLong(key, tmp, isIdentifying);
            } else if(n instanceof Float || n instanceof Double) {
                Double tmp = n.doubleValue();
                builder.addDouble(key, tmp, isIdentifying);
            } else {
                throw new RuntimeException("Should not happen");
            }
        }
    }

    public static void processArg(JobParametersBuilder builder, String key, JsonElement json, Set<String> identifyingKeys) {
        JsonObject obj = json.getAsJsonObject();
        JsonElement value = obj.get("value");
        JsonElement tmpIdent = obj.get("ident");


        boolean isIdent = tmpIdent != null ? tmpIdent.getAsBoolean() : false;

        JsonPrimitive p = value.getAsJsonPrimitive();
        processArg(builder, key, p, isIdent);
    }

    public static JobParameters toJobParameters(JsonElement json, Set<String> identifyingKeys) {
        JobParametersBuilder builder = new JobParametersBuilder();

        toJobParameters(builder, json, identifyingKeys);
        JobParameters result = builder.toJobParameters();

        return result;
    }


    /**
     * {
     *   param: 'foo',
     *   param: { $date: '2015-10-29' },
     *   param: [ 'foo' ], // we could reuse array syntax for ident; but let's better avoid that.
     *   param: { value: 'foo', ident: true }
     * }
     *
     * @param builder
     * @param json
     * @param identifyingKeys A set of keys which are identifying by default
     */
    public static void toJobParameters(JobParametersBuilder builder, JsonElement json, Set<String> identifyingKeys) {
        if(json == null || json.isJsonNull()) {
            // nothing to do
        } else if(json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            for(Entry<String, JsonElement> entry : obj.entrySet()) {
                String key = entry.getKey();
                JsonElement val = entry.getValue();

                processArg(builder, key, val, identifyingKeys);
            }
        } else {
            throw new RuntimeException("Invalid job parameters config: " + json);
        }
    }

}
