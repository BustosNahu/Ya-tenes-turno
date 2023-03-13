package com.yatenesturno.activities.place_credits;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.activities.main_screen.FragmentAdminClients;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoRead;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.serializers.BuilderListCustomUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import eu.davidea.flexibleadapter.FlexibleAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentPlaceClients#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentPlaceClients extends Fragment {


    /**
     * Argument keys
     */
    private static final String ARG_PARAM_PLACE = "place";

    /**
     * Instance variables
     */
    private Place place;
    private List<CustomUser> clientList;
    private PlaceClientCreditsActivity.OnClientClickListener listener;

    /**
     * UI References
     */
    private LoadingOverlay loadingOverlay;
    private RecyclerView recyclerViewClients;
    private CardView holderSearchViewClients;
    private SearchView searchView;
    private FlexibleAdapter<FragmentAdminClients.FlexibleClientItem> adapter;

    public FragmentPlaceClients() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param place Parameter 2.
     * @return A new instance of fragment FragmentPlaceClients.
     */
    public static FragmentPlaceClients newInstance(Place place) {
        FragmentPlaceClients fragment = new FragmentPlaceClients();

        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM_PLACE, place);

        fragment.setArguments(args);
        return fragment;
    }

    public void setListener(PlaceClientCreditsActivity.OnClientClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            recoverState(savedInstanceState);
        } else if (getArguments() != null) {
            recoverState(getArguments());
        }
    }

    private void recoverState(Bundle bundle) {
        place = bundle.getParcelable(ARG_PARAM_PLACE);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(ARG_PARAM_PLACE, place);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_place_clients, container, false);

        recyclerViewClients = view.findViewById(R.id.recyclerViewClients);
        loadingOverlay = new LoadingOverlay(view.findViewById(R.id.root));
        searchView = view.findViewById(R.id.searchViewClients);
        holderSearchViewClients = view.findViewById(R.id.holderSearchViewClients);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getClients();
    }

    private void getClients() {
        loadingOverlay.show();
        new Handler(Looper.myLooper()).postDelayed(() -> DatabaseDjangoRead.getInstance().GET(
                String.format(Constants.DJANGO_URL_CLIENTS_OF_PLACE, place.getId()),
                null,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        loadingOverlay.hide();
                        onClientsFetch(response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        loadingOverlay.hide();
                    }
                }
        ), 300);
    }

    public void showConnectionError() {
        Toast.makeText(getContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
    }

    private void onClientsFetch(JSONObject response) {
        BuilderListCustomUser builderListCustomUser = new BuilderListCustomUser();
        try {
            clientList = builderListCustomUser.build(response);
        } catch (JSONException e) {
            e.printStackTrace();
            clientList = new ArrayList<>();
        }
        populateClientsView();
    }

    private void populateClientsView() {
        if (hasClients()) {
            setUpHasClientsView();

            Collections.sort(clientList, (t0, t1) -> t0.getName().toLowerCase().compareTo(t1.getName().toLowerCase()));
            List<FragmentAdminClients.FlexibleClientItem> clientItemList = new ArrayList<>();
            for (CustomUser customUser : clientList) {
                clientItemList.add(new FragmentAdminClients.FlexibleClientItem(customUser, view -> notifyListener(customUser)));
            }
            recyclerViewClients.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
            adapter = new FlexibleAdapter<>(clientItemList);
            recyclerViewClients.setAdapter(adapter);
            setUpSearchView();
        } else {
            setUpNoClientsView();
        }
    }

    private void setUpSearchView() {
        if (clientList.size() > 5) {
            holderSearchViewClients.setVisibility(View.VISIBLE);
        } else {
            holderSearchViewClients.setVisibility(View.GONE);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return onQueryTextChange(query);
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (adapter.hasNewFilter(newText)) {
                    adapter.setFilter(newText);
                    adapter.filterItems(100);
                }

                return true;
            }
        });

    }

    private void notifyListener(CustomUser customUser) {
        if (listener != null) {
            listener.onClientClick(customUser);
        }
    }

    private boolean hasClients() {
        return clientList.size() > 0;
    }

    private void setUpNoClientsView() {
        getView().findViewById(R.id.labelNoClients).setVisibility(View.VISIBLE);
        recyclerViewClients.setVisibility(View.GONE);
    }

    private void setUpHasClientsView() {
        getView().findViewById(R.id.labelNoClients).setVisibility(View.GONE);
        recyclerViewClients.setVisibility(View.VISIBLE);
    }

    public class AdapterClients extends BaseAdapter {

        private final List<CustomUser> clientList;

        public AdapterClients(List<CustomUser> list) {
            this.clientList = list;
        }

        @Override
        public int getCount() {
            return clientList.size();
        }

        @Override
        public Object getItem(int position) {
            return clientList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.client_layout, parent, false);
            }
            ((TextView) convertView.findViewById(R.id.labelClient)).setText(clientList.get(position).getName());

            return convertView;
        }
    }
}