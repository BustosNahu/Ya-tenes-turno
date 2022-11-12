package com.yatenesturno.serializers;

import com.yatenesturno.object_interfaces.Place;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BuilderListPlace {

    public List<Place> build(JSONObject json) throws JSONException {

        List<Place> out = new ArrayList<>();
        JSONObject jsonPlaces = json.getJSONObject("places");

        BuilderObjectPlace builderPlace = new BuilderObjectPlace();
        Place place;
        JSONObject jsonSinglePlace;
        for (Iterator<String> it = jsonPlaces.keys(); it.hasNext(); ) {
            String id = it.next();
            jsonSinglePlace = jsonPlaces.getJSONObject(id);
            place = builderPlace.build(jsonSinglePlace);

            out.add(place);
        }

        return out;
    }

}
