package com.yatenesturno.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.object_interfaces.SubscriptionTemplate;
import com.yatenesturno.object_interfaces.SubscriptionToken;

public class SubscriptionTokenImpl implements SubscriptionToken {

    public static final Parcelable.Creator<SubscriptionTokenImpl> CREATOR = new Parcelable.Creator<SubscriptionTokenImpl>() {

        public SubscriptionTokenImpl createFromParcel(Parcel in) {
            return new SubscriptionTokenImpl(in);
        }

        public SubscriptionTokenImpl[] newArray(int size) {
            return new SubscriptionTokenImpl[size];
        }
    };

    private String id, token;
    private CustomUser user;
    private SubscriptionTemplate template;
    private Place place;
    private boolean valid, warning;

    public SubscriptionTokenImpl() {
    }

    public SubscriptionTokenImpl(Parcel in) {
        user = (CustomUser) in.readValue(CustomUser.class.getClassLoader());
        template = (SubscriptionTemplate) in.readValue(SubscriptionTemplate.class.getClassLoader());
        place = (Place) in.readValue(Place.class.getClassLoader());
        id = in.readString();
        token = in.readString();
        valid = in.readInt() == 1;
        warning = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(user);
        dest.writeValue(template);
        dest.writeValue(place);
        dest.writeString(id);
        dest.writeString(token);
        dest.writeInt(valid ? 1 : 0);
        dest.writeInt(warning ? 1 : 0);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public boolean hasWarning() {
        return warning;
    }

    @Override
    public void setWarning(boolean warning) {
        this.warning = warning;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public Place getPlace() {
        return place;
    }

    @Override
    public void setPlace(Place place) {
        this.place = place;
    }

    @Override
    public CustomUser getUser() {
        return user;
    }

    @Override
    public void setUser(CustomUser user) {
        this.user = user;
    }

    @Override
    public SubscriptionTemplate getTemplate() {
        return template;
    }

    @Override
    public void setTemplate(SubscriptionTemplate template) {
        this.template = template;
    }

    @Override
    public int describeContents() {
        return 0;
    }


}
