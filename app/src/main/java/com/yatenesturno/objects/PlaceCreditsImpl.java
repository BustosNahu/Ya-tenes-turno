package com.yatenesturno.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.yatenesturno.object_interfaces.PlaceCredits;
import com.yatenesturno.utils.CalendarUtils;

import java.util.Calendar;

public class PlaceCreditsImpl implements PlaceCredits {

    public static final Parcelable.Creator<PlaceCreditsImpl> CREATOR = new Parcelable.Creator<PlaceCreditsImpl>() {

        public PlaceCreditsImpl createFromParcel(Parcel in) {
            return new PlaceCreditsImpl(in);
        }

        public PlaceCreditsImpl[] newArray(int size) {
            return new PlaceCreditsImpl[size];
        }
    };
    private String id;
    private Calendar validFrom, validUntil;
    private int currentCredits, credits;

    public PlaceCreditsImpl() {
    }

    public PlaceCreditsImpl(Parcel in) {
        id = in.readString();
        validFrom = CalendarUtils.parseDate(in.readString());
        validUntil = CalendarUtils.parseDate(in.readString());
        currentCredits = in.readInt();
        credits = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(CalendarUtils.formatCalendar(validFrom));
        parcel.writeString(CalendarUtils.formatCalendar(validUntil));
        parcel.writeInt(currentCredits);
        parcel.writeInt(credits);
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
    public Calendar getValidFrom() {
        return validFrom;
    }

    @Override
    public void setValidFrom(Calendar validFrom) {
        this.validFrom = validFrom;
    }

    @Override
    public Calendar getValidUntil() {
        return validUntil;
    }

    @Override
    public void setValidUntil(Calendar validUntil) {
        this.validUntil = validUntil;
    }

    @Override
    public int getCurrentCredits() {
        return currentCredits;
    }

    @Override
    public void setCurrentCredits(int currentCredits) {
        this.currentCredits = currentCredits;
    }

    @Override
    public int getCredits() {
        return credits;
    }

    @Override
    public void setCredits(int credits) {
        this.credits = credits;
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
