package com.yatenesturno.activities.purchases.single_purchase;

import com.yatenesturno.Constants;
import com.yatenesturno.Parser.Parser;
import com.yatenesturno.Parser.ParserGeneric;
import com.yatenesturno.database.DatabaseDjangoRead;
import com.yatenesturno.listeners.DatabaseCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class PacksManagerImpl implements PacksManager {

    private List<CreditPack> localStorageCreditPacks;

    public PacksManagerImpl() {
        this.localStorageCreditPacks = null;
    }

    @Override
    public void fetchPacks(ListenerFetchPacks listener) {
        if (localStorageCreditPacks == null) {
            fetchPacksInRemote(listener);
        } else {
            listener.onPacksFetched(localStorageCreditPacks);
        }
    }

    @Override
    public void invalidate() {
        this.localStorageCreditPacks = null;
    }

    private void fetchPacksInRemote(ListenerFetchPacks listener) {
        DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_CREDIT_PACKS,
                null,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Parser<CreditPackImpl> parser = new ParserGeneric<>(CreditPackImpl.class, "credit_packs");

                        List<CreditPack> creditPackList = new ArrayList<>(parser.parseByList(response));
                        listener.onPacksFetched(creditPackList);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                    }
                }
        );
    }

}
