package com.yatenesturno.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.yatenesturno.object_interfaces.SubscriptionTemplate;

public class SubscriptionTemplateImpl implements SubscriptionTemplate {
    public static final Parcelable.Creator<SubscriptionTemplateImpl> CREATOR = new Parcelable.Creator<SubscriptionTemplateImpl>() {

        public SubscriptionTemplateImpl createFromParcel(Parcel in) {
            return new SubscriptionTemplateImpl(in);
        }

        public SubscriptionTemplateImpl[] newArray(int size) {
            return new SubscriptionTemplateImpl[size];
        }
    };
    private String id, url, description;
    private boolean isCombo;
    private int employeeCount;

    public SubscriptionTemplateImpl() {
    }

    public SubscriptionTemplateImpl(Parcel in) {
        id = in.readString();
        url = in.readString();
        description = in.readString();
        isCombo = in.readInt() == 1;
        employeeCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(url);
        dest.writeString(description);
        dest.writeInt(isCombo ? 1 : 0);
        dest.writeInt(employeeCount);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean isCombo() {
        return isCombo;
    }

    @Override
    public void setIsCombo(boolean combo) {
        isCombo = combo;
    }

    @Override
    public int getEmployeeCount() {
        return employeeCount;
    }

    @Override
    public void setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
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
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }


}
