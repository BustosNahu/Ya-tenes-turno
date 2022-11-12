package com.yatenesturno.serializers;

import org.json.JSONObject;

public interface Serializer<Type> {

    JSONObject serialize(Type object);

    Type deserialize(JSONObject json);

}
