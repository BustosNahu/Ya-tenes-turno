package com.yatenesturno.activities.purchases.single_purchase;

import java.util.List;

public interface PacksManager {

    void fetchPacks(ListenerFetchPacks listener);

    void invalidate();

    interface ListenerFetchPacks {
        void onPacksFetched(List<CreditPack> creditPackList);
    }
}
