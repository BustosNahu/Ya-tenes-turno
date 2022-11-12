package com.yatenesturno.Parser;

import org.json.JSONObject;

import java.util.List;

/**
 * Newer version to solve Serializing-Deserializing
 * Mean to replace /serializers approach
 *
 * @param <Type>
 */

public interface Parser<Type> {

    List<Type> parseByList(JSONObject response);

    Type parseSingle(JSONObject response);
}
