package com.yatenesturno.custom_views;

import com.yatenesturno.Constants;
import com.yatenesturno.database.DatabaseDjangoRead;
import com.yatenesturno.database.DatabaseDjangoWrite;
import com.yatenesturno.functionality.LabelHandler;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.Label;
import com.yatenesturno.objects.LabelImpl;
import com.yatenesturno.serializers.BuilderListJobLabel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class LabelHandlerImpl implements LabelHandler {

    private static LabelHandlerImpl instance;
    private final Map<String, List<Label>> mapJobLabels;

    private LabelHandlerImpl() {
        mapJobLabels = new HashMap<>();
    }

    public static LabelHandler getInstance() {
        if (instance == null) {
            instance = new LabelHandlerImpl();
        }
        return instance;
    }

    @Override
    public void getLabels(String placeId, String jobId, OnGetLabelsListener listener) {
        if (mapJobLabels.containsKey(jobId)) {
            listener.onSuccess(mapJobLabels.get(jobId));
        } else {
            fetchFromRemote(placeId, jobId, listener);
        }
    }

    private void fetchFromRemote(String placeId, final String jobId, final OnGetLabelsListener listener) {
        Map<String, String> body = new HashMap<>();

        DatabaseDjangoRead.getInstance().GET(
                String.format(Constants.DJANGO_URL_LABELS, placeId, jobId),
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        BuilderListJobLabel builderListJobLabel = new BuilderListJobLabel();
                        List<Label> out = builderListJobLabel.buildLabelList(response);

                        if (out == null) {
                            listener.onFailure();
                        } else {
                            mapJobLabels.put(jobId, out);
                            listener.onSuccess(out);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFailure();
                    }
                }
        );
    }

    @Override
    public void createLabel(String placeId, final String jobId, final String name, final String color, final OnCreateLabelListener listener) {
        Map<String, String> body = new HashMap<>();
        body.put("color", color);
        body.put("name", name);

        DatabaseDjangoWrite.getInstance().POST(
                String.format(Constants.DJANGO_URL_LABELS, placeId, jobId),
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Label label = addLabelToLocal(response, color, jobId, name);
                        listener.onSuccess(label);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFailure();
                    }
                }
        );
    }

    public Label addLabelToLocal(JSONObject response, String color, String jobId, String name) {
        try {
            Label label = new LabelImpl();
            label.setColor(color);
            label.setId(response.getString("label_id"));
            label.setJobId(jobId);
            label.setName(name);

            if (!mapJobLabels.containsKey(jobId)) {
                mapJobLabels.put(jobId, new ArrayList<Label>());
            }
            mapJobLabels.get(jobId).add(label);

            return label;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deleteLabel(String placeId, String jobId, String labelId, final LabelHandlerListener listener) {
        removeLabelFromLocal(jobId, labelId);
        DatabaseDjangoWrite.getInstance().DELETE(
                String.format(Constants.DJANGO_URL_LABELS_DELETE, placeId, jobId, labelId),
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        listener.onSuccess();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFailure();
                    }
                }
        );
    }

    private void removeLabelFromLocal(String jobId, String labelId) {
        if (mapJobLabels.containsKey(jobId)) {
            int indexToRemove = -1;
            for (int i = 0; i < mapJobLabels.get(jobId).size(); i++) {
                if (mapJobLabels.get(jobId).get(i).getId().equals(labelId)) {
                    indexToRemove = i;
                    break;
                }
            }

            if (indexToRemove != -1) {
                mapJobLabels.get(jobId).remove(indexToRemove);
            }
        }
    }

    @Override
    public void setLabel(String placeId, String jobId, String appId, String labelId, final LabelHandlerListener listener) {
        Map<String, String> body = new HashMap<>();
        body.put("label_id", labelId);

        DatabaseDjangoWrite.getInstance().POST(
                String.format(Constants.DJANGO_URL_SET_LABEL, placeId, jobId, appId),
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        listener.onSuccess();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFailure();
                    }
                }
        );
    }

    @Override
    public void setAttended(String placeId, String jobId, boolean attended, String appId, final LabelHandlerListener listener) {
        Map<String, String> body = new HashMap<>();

        body.put("attended", String.valueOf(attended));

        DatabaseDjangoWrite.getInstance().POST(
                String.format(Constants.DJANGO_URL_SET_ATTENDED, placeId, jobId, appId),
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        listener.onSuccess();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFailure();
                    }
                }
        );
    }

    @Override
    public void clearLabel(String placeId, String jobId, String appId, LabelHandlerListener listener) {
        DatabaseDjangoWrite.getInstance().DELETE(
                String.format(Constants.DJANGO_URL_SET_LABEL, placeId, jobId, appId),
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        listener.onSuccess();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFailure();
                    }
                }
        );
    }

    @Override
    public void invalidateLabels(String jobId) {
        mapJobLabels.remove(jobId);
    }
}
