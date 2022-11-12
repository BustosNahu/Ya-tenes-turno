package com.yatenesturno.activities.purchases.single_purchase;

import android.os.Parcel;
import android.os.Parcelable;

public class CreditPackImpl implements CreditPack {
    public static final Parcelable.Creator<CreditPackImpl> CREATOR = new Parcelable.Creator<CreditPackImpl>() {

        public CreditPackImpl createFromParcel(Parcel in) {
            return new CreditPackImpl(in);
        }

        public CreditPackImpl[] newArray(int size) {
            return new CreditPackImpl[size];
        }
    };
    private int whatsappCredits, emailCredits;
    private float price;
    private String mercadoPagoUrl;

    public CreditPackImpl(Parcel in) {
        whatsappCredits = in.readInt();
        emailCredits = in.readInt();
        price = in.readFloat();
        mercadoPagoUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(whatsappCredits);
        parcel.writeInt(emailCredits);
        parcel.writeFloat(price);
        parcel.writeString(mercadoPagoUrl);
    }

    @Override
    public String getMercadoPagoUrl() {
        return mercadoPagoUrl;
    }

    @Override
    public void setMercadoPagoUrl(String mercadoPagoUrl) {
        this.mercadoPagoUrl = mercadoPagoUrl;
    }

    @Override
    public int getWhatsappCredits() {
        return whatsappCredits;
    }

    @Override
    public void setWhatsappCredits(int whatsappCredits) {
        this.whatsappCredits = whatsappCredits;
    }

    @Override
    public int getEmailCredits() {
        return emailCredits;
    }

    @Override
    public void setEmailCredits(int emailCredits) {
        this.emailCredits = emailCredits;
    }

    @Override
    public float getPrice() {
        return price;
    }

    @Override
    public void setPrice(float price) {
        this.price = price;
    }


}
