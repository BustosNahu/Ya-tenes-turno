package com.yatenesturno.functionality;

import com.yatenesturno.object_interfaces.Label;

import java.util.List;

public interface LabelHandler {

    void getLabels(String placeId, String jobId, OnGetLabelsListener listener);

    void createLabel(String placeId, String jobId, String name, String color, OnCreateLabelListener listener);

    void deleteLabel(String placeId, String jobId, String labelId, LabelHandlerListener listener);

    void setLabel(String placeId, String jobId, String appId, String labelId, LabelHandlerListener listener);

    void setAttended(String placeId, String jobId, boolean attended, String appId, LabelHandlerListener listener);

    void clearLabel(String placeId, String jobId, String appId, LabelHandlerListener listener);

    void invalidateLabels(String jobId);

    interface LabelHandlerListener {
        void onSuccess();

        void onFailure();
    }

    interface OnCreateLabelListener {
        void onSuccess(Label newLabelId);

        void onFailure();
    }

    interface OnGetLabelsListener {
        void onSuccess(List<Label> result);

        void onFailure();
    }
}
