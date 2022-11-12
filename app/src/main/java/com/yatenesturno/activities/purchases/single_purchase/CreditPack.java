package com.yatenesturno.activities.purchases.single_purchase;

import android.os.Parcelable;

public interface CreditPack extends Parcelable {

    String getMercadoPagoUrl();

    void setMercadoPagoUrl(String mercadoPagoUrl);

    int getWhatsappCredits();

    void setWhatsappCredits(int whatsappCredits);

    int getEmailCredits();

    void setEmailCredits(int emailCredits);

    float getPrice();

    void setPrice(float price);
}
