package com.yatenesturno.serializers;

import org.json.JSONException;
import org.json.JSONObject;

public interface ObjectBuilder<E> {

    /**
     * Builds Object from Json
     *
     * @param json json to build object from
     * @return object built
     */
    E build(JSONObject json) throws JSONException;

}
