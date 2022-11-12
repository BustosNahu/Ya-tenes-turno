package com.yatenesturno.object_interfaces;

import android.os.Parcelable;

import java.util.Calendar;

public interface PlaceCredits extends Parcelable {


    String getId();

    void setId(String id);

    Calendar getValidFrom();

    void setValidFrom(Calendar validFrom);

    Calendar getValidUntil();

    void setValidUntil(Calendar validUntil);

    int getCurrentCredits();

    void setCurrentCredits(int currentCredits);

    int getCredits();

    void setCredits(int credits);
}
