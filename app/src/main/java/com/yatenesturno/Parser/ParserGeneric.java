package com.yatenesturno.Parser;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yatenesturno.serializers.Serializer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ParserGeneric<Type> implements Parser<Type> {

    private final String mainKey;
    private final Class<Type> typeParameterClass;

    public ParserGeneric(Class<Type> typeParameterClass, String mainKey) {
        this.typeParameterClass = typeParameterClass;
        this.mainKey = mainKey;
    }

    @Override
    public List<Type> parseByList(JSONObject response) {
        return parseByList(response, null);
    }

    public List<Type> parseByList(JSONObject response, Serializer<Type> serializer) {
        List<Type> out = new ArrayList<>();

        try {
            JSONObject typeElementListJson = response.getJSONObject(mainKey);
            for (Iterator<String> it = typeElementListJson.keys(); it.hasNext(); ) {
                String singleElementKey = it.next();
                JSONObject singlePackObject = typeElementListJson.getJSONObject(singleElementKey);

                Type singleElement;
                if (serializer != null) {
                    singleElement = serializer.deserialize(singlePackObject);
                } else {
                    singleElement = parseSingle(singlePackObject);
                }
                out.add(singleElement);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return out;
    }

    @Override
    public Type parseSingle(JSONObject response) {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        return gson.fromJson(response.toString(), typeParameterClass);
    }

}
