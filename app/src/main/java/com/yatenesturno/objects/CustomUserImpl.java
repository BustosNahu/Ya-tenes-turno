package com.yatenesturno.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.yatenesturno.object_interfaces.CustomUser;

import java.util.Objects;

public class CustomUserImpl implements CustomUser {

    public static final Parcelable.Creator<CustomUserImpl> CREATOR = new Parcelable.Creator<CustomUserImpl>() {

        public CustomUserImpl createFromParcel(Parcel in) {
            return new CustomUserImpl(in);
        }

        public CustomUserImpl[] newArray(int size) {
            return new CustomUserImpl[size];
        }
    };

    private final String givenName;
    private final String familyName;
    private final String email;
    private final String id;
    private final String profilePicUrl;
    private int trialRemainingDays;

    public CustomUserImpl(String id, String givenName, String familyName, String email, String profilePic) {
        this.profilePicUrl = profilePic;
        this.givenName = givenName;
        this.familyName = familyName;
        this.email = email;
        this.id = id;
        trialRemainingDays = -1;
    }

    public CustomUserImpl(Parcel in) {
        givenName = in.readString();
        familyName = in.readString();
        email = in.readString();
        id = in.readString();
        profilePicUrl = in.readString();
        trialRemainingDays = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(givenName);
        dest.writeString(familyName);
        dest.writeString(email);
        dest.writeString(id);
        dest.writeString(profilePicUrl);
        dest.writeInt(trialRemainingDays);
    }

    @Override
    public int getTrialRemainingDays() {
        return trialRemainingDays;
    }

    @Override
    public void setTrialRemainingDays(int trial) {
        this.trialRemainingDays = trial;
    }

    @Override
    public String getGivenName() {
        return givenName;
    }

    @Override
    public String getFamilyName() {
        return familyName;
    }

    @Override
    public String getName() {
        return givenName + " " + familyName;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomUserImpl that = (CustomUserImpl) o;
        return Objects.equals(givenName, that.givenName) &&
                Objects.equals(familyName, that.familyName) &&
                Objects.equals(email, that.email) &&
                Objects.equals(id, that.id);
    }

}