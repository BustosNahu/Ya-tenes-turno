package com.yatenesturno.functionality.place_credits;

import com.yatenesturno.Constants;
import com.yatenesturno.Parser.ParserGeneric;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoRead;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoWrite;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.objects.PlaceCreditsImpl;
import com.yatenesturno.serializers.SerializerPlaceCredits;
import com.yatenesturno.utils.CalendarUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

/**
 * Place Credits for clients organizer
 * follows Singleton pattern
 * <p>
 * stores PlaceCredits during runtime
 */
public class PlaceCreditsManager {

    public static void getCreditsForJobClient(String placeId, String clientId, Calendar from, Calendar until, List<String> serviceIdList, GetCreditsListener listener) {
        Map<String, String> queryParams = new HashMap<>();

        if (from != null) {
            queryParams.put("valid_from", CalendarUtils.formatDateYYYYMMDD(from));
        }

        if (until != null) {
            queryParams.put("valid_until", CalendarUtils.formatDateYYYYMMDD(until));
        }

        if (serviceIdList != null) {
            StringBuilder serviceIds = new StringBuilder();
            for (int i = 0; i < serviceIdList.size(); i++) {

                serviceIds.append(serviceIdList.get(i));
                if (i != serviceIdList.size() - 1) {
                    serviceIds.append(",");
                }

            }
            queryParams.put("services", serviceIds.toString());
        }

        DatabaseDjangoRead.getInstance().GET(
                String.format(Constants.DJANGO_URL_PLACE_CREDITS, placeId, clientId),
                queryParams,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        ParserGeneric<PlaceCreditsImpl> jobCreditsParser = new ParserGeneric<>(PlaceCreditsImpl.class, "data");
                        List<PlaceCreditsImpl> out = jobCreditsParser.parseByList(response, new SerializerPlaceCredits());
                        Collections.sort(out, (t0, t1) -> {
                            return t0.getValidUntil().compareTo(t1.getValidUntil());
                        });
                        listener.onFetch(out);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFailure();
                    }
                }
        );
    }

    public static void addCreditsToUser(
            String placeId,
            String clientId,
            Calendar validFrom,
            Calendar validUntil,
            List<ServiceInstance> serviceIdList,
            int credits,
            UpdateCreditsListener listener) {

        JSONObject body = new JSONObject();

        try {

            if (serviceIdList != null) {
                JSONArray serviceIds = new JSONArray();
                for (int i = 0; i < serviceIdList.size(); i++) {
                    serviceIds.put(serviceIdList.get(i).getService().getId());
                }

                body.put("services", serviceIds);
            }

            body.put("credits", credits);
            body.put("valid_from", CalendarUtils.formatDateYYYYMMDD(validFrom));
            body.put("valid_until", CalendarUtils.formatDateYYYYMMDD(validUntil));

            DatabaseDjangoWrite.getInstance().POSTJSON(
                    String.format(Constants.DJANGO_URL_PLACE_CREDITS, placeId, clientId),
                    body,
                    new DatabaseCallback() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            listener.onUpdate();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            listener.onFailure();
                        }
                    }
            );
        } catch (JSONException e) {
            listener.onFailure();
        }
    }

    public static void updateCreditsToUser(String placeId, String clientId, String creditsId, Calendar validFrom, Calendar validUntil, int currentCredits, UpdateCreditsListener listener) {
        JSONObject body = new JSONObject();

        try {
            body.put("current_credits", String.valueOf(currentCredits));

            if (validFrom != null) {
                body.put("valid_from", CalendarUtils.formatDate(validFrom));
            }

            if (validUntil != null) {
                body.put("valid_until", CalendarUtils.formatDate(validUntil));
            }

            DatabaseDjangoWrite.getInstance().PUT(
                    String.format(Constants.DJANGO_URL_PLACE_CREDITS_UPDATE, placeId, clientId, creditsId),
                    body,
                    new DatabaseCallback() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            listener.onUpdate();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            listener.onFailure();
                        }
                    }
            );
        } catch (JSONException e) {
            listener.onFailure();
        }
    }

    public static void removeCreditFromUser(String placeId, String clientId, String creditsId, UpdateCreditsListener listener) {
        DatabaseDjangoWrite.getInstance().DELETE(
                String.format(Constants.DJANGO_URL_PLACE_CREDITS_UPDATE, placeId, clientId, creditsId),
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        listener.onUpdate();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        listener.onFailure();
                    }
                }
        );
    }

    public interface GetCreditsListener {
        void onFetch(List<PlaceCreditsImpl> creditsList);

        void onFailure();
    }

    public interface UpdateCreditsListener {
        void onUpdate();

        void onFailure();
    }
}
