package com.yatenesturno.functionality;

import com.yatenesturno.Constants;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoRead;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoWrite;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.serializers.BuilderListJobPlace;
import com.yatenesturno.user_auth.UserManagement;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class ManagerPlace {

    private static ManagerPlace instance;
    private final ArrayList<OnPlaceListFetchListener> registeredListeners;
    private List<Place> placeList;
    private boolean fetchingPlaces;

    public ManagerPlace() {
        registeredListeners = new ArrayList<>();
    }

    public static ManagerPlace getInstance() {
        if (instance == null) {
            instance = new ManagerPlace();
        }
        return instance;
    }

    public void getOwnedPlaces(final OnPlaceListFetchListener listener) {
        if (placeList != null) {
            List<Place> ownedPlaces = findOwnedPlaces();
            listener.onFetch(ownedPlaces);
        } else {
            getPlaces(new OnPlaceListFetchListener() {
                @Override
                public void onFetch(List<Place> placeList) {
                    listener.onFetch(findOwnedPlaces());
                }

                @Override
                public void onFailure() {
                    listener.onFailure();
                }
            });
        }
    }

    public void getPlaces(final OnPlaceListFetchListener listener) {
        if (placeList != null) {
            listener.onFetch(placeList);
        } else {
            registeredListeners.add(listener);
            if (fetchingPlaces) {
                return;
            }
            fetchingPlaces = true;
            fetchPlaces(new FetchPlacesFromRemoteListener() {
                @Override
                public void onFetch() {
                    fetchingPlaces = false;
                    for (OnPlaceListFetchListener registeredListener : registeredListeners) {
                        registeredListener.onFetch(placeList);
                    }
                    registeredListeners.clear();
                }

                @Override
                public void onFailure() {
                    fetchingPlaces = false;
                    for (OnPlaceListFetchListener registeredListener : registeredListeners) {
                        registeredListener.onFailure();
                    }
                    registeredListeners.clear();
                }
            });
        }
    }

    private List<Place> findOwnedPlaces() {
        List<Place> ownedPlaces = new ArrayList<>();
        for (Place p : placeList) {
            if (p.getOwner().getId().equals(UserManagement.getInstance().getUser().getId())) {
                ownedPlaces.add(p);
            }
        }
        return ownedPlaces;
    }

    private void fetchPlaces(final FetchPlacesFromRemoteListener listener) {
        DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_GET_OWNED_PLACES,
                null,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            BuilderListJobPlace builder = new BuilderListJobPlace();
                            placeList = builder.build(response);
                            listener.onFetch();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFailure();
                    }
                }
        );
    }

    public void dropPlace(final Place place, final OnDropListener listener) {
        Map<String, String> body = new HashMap<>();
        body.put("place_id", place.getId());
        DatabaseDjangoWrite.getInstance().POST(
                Constants.DJANGO_URL_DROP_PLACE,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        placeList.remove(place);
                        listener.onDrop();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFailure();
                    }
                }
        );

    }

    public void invalidate() {
        instance = null;
        placeList = null;
    }

    public void replaceList(List<Place>places , ManagerPlace newIntance){
        placeList = places;
        instance = newIntance;
    }

    public void dropJob(final Place place, final Job job, final OnDropListener onDropListener) {
        Map<String, String> body = new HashMap<>();
        body.put("job_id", job.getId());
        DatabaseDjangoWrite.getInstance().POST(
                Constants.DJANGO_URL_DROP_JOB,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        place.getJobList().remove(job);
                        onDropListener.onDrop();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        onDropListener.onFailure();
                    }
                }
        );
    }

    public void changeJobPermissions(final Job job, final boolean canEdit, final boolean canChat, final OnChangePermissionListener listener) {
        Map<String, String> body = new HashMap<>();
        body.put("job_id", job.getId());

        String canEditString = canEdit ? "True" : "False";
        String canChatString = canChat ? "True" : "False";

        body.put("can_edit", canEditString);
        body.put("can_chat", canChatString);
        DatabaseDjangoWrite.getInstance().POST(
                Constants.DJANGO_URL_CHANGE_JOB_PERMISSIONS,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        job.setCanEdit(canEdit);
                        job.setCanChat(canChat);
                        listener.onChange();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFailure();
                    }
                }
        );
    }

    public Place getPlaceById(String placeId) {
        if (placeList != null) {
            for (Place p : placeList) {
                if (p.getId().equals(placeId)) {
                    return p;
                }
            }
        }
        return null;
    }

    public interface OnDropListener {
        void onDrop();

        void onFailure();
    }

    private interface FetchPlacesFromRemoteListener {
        void onFetch();

        void onFailure();
    }

    public interface OnPlaceListFetchListener {
        void onFetch(List<Place> placeList);

        void onFailure();
    }

    public interface OnChangePermissionListener {
        void onChange();

        void onFailure();
    }
}
