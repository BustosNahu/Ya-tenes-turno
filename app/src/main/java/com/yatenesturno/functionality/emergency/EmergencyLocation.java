package com.yatenesturno.functionality.emergency;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;

public class EmergencyLocation implements Serializable, Parcelable {

    public static final Creator<EmergencyLocation> CREATOR = new Creator<EmergencyLocation>() {
        @Override
        public EmergencyLocation createFromParcel(Parcel in) {
            return new EmergencyLocation(in);
        }

        @Override
        public EmergencyLocation[] newArray(int size) {
            return new EmergencyLocation[size];
        }
    };
    private float lat, lon;
    private String id;
    private String name;
    private boolean active;

    public EmergencyLocation() {
    }

    protected EmergencyLocation(Parcel in) {
        name = in.readString();
        lat = in.readFloat();
        lon = in.readFloat();
        active = in.readInt() == 1;
        id = in.readString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmergencyLocation)) return false;
        EmergencyLocation that = (EmergencyLocation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lon, id, name, active);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeFloat(lat);
        parcel.writeFloat(lon);
        parcel.writeInt(active ? 1 : 0);
        parcel.writeString(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public JSONObject toJson() {
        JSONObject out = new JSONObject();

        try {
            out.put("name", name);
            out.put("lat", lat);
            out.put("lon", lon);
            out.put("active", active);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return out;
    }

    @Override
    public int describeContents() {
        return 0;
    }


}
