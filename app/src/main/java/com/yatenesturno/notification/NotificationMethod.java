package com.yatenesturno.notification;

import android.content.Context;
import android.os.Bundle;

public interface NotificationMethod {

    /**
     * @param context
     * @param data
     */
    void sendMessage(Context context, Bundle data);

    /**
     * @return
     */
    String getType();
}
