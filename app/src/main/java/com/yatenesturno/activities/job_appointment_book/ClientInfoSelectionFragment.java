package com.yatenesturno.activities.job_appointment_book;
import static android.content.Intent.getIntent;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;
import com.yatenesturno.R;
import com.yatenesturno.activities.get_premium.GetPremiumActivity;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.user_auth.UserManagement;
import com.yatenesturno.utils.ValidationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;


public class ClientInfoSelectionFragment extends Fragment {

    public static final String NAME = "name";
    public static final String PHONE = "phone";
    public static final String EMAIL = "email";
    public static final String CLIENT_LIST = "clientList";

    public static final String PLACE_ID = "placeId";
    private ArrayList<CustomUser> clientList;

    private String placeId;
    private OnConfirmedListener listener;
    private TextInputLayout tilName, tilEmail, tilPhone;
    private TextInputEditText tietName, tietEmail, tietPhone;
    private Button btnConfirm;
    private CustomUser selectedClient;
    private RelativeLayout containerClientInfo;

    public ClientInfoSelectionFragment(ArrayList<CustomUser> clientList) {
        Collections.sort(clientList, (t0, t1) -> t0.getName().toLowerCase().compareTo(t1.getName().toLowerCase()));
        this.clientList = clientList;
    }

    public ClientInfoSelectionFragment() {

    }

    public void setListener(OnConfirmedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putAll(saveState());
    }


    private Bundle saveState() {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(CLIENT_LIST, clientList);
        bundle.putString(NAME, tietName.getText().toString());
        bundle.putString(PHONE, tietPhone.getText().toString());
        bundle.putString(EMAIL, tietEmail.getText().toString());
        bundle.putString(PLACE_ID, placeId);

        return bundle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_client_info_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();
        initViews();


        if (savedInstanceState != null) {
            recoverState(savedInstanceState);
        }
    }


    private void recoverState(Bundle bundle) {
        tietName.setText(bundle.getString(NAME));
        tietPhone.setText(bundle.getString(PHONE));
        tietEmail.setText(bundle.getString(EMAIL));
        clientList = bundle.getParcelableArrayList(CLIENT_LIST);
        placeId = bundle.getString(PLACE_ID);
    }

    private void init(){
        placeId = getActivity().getIntent().getStringExtra("placeId");
    }

    private void initViews() {
        tilName = getView().findViewById(R.id.text_input_layout_name);
        tilEmail = getView().findViewById(R.id.text_input_layout_email);
        tilPhone = getView().findViewById(R.id.text_input_layout_phone);


        tietName = getView().findViewById(R.id.editTextName);
        tietEmail = getView().findViewById(R.id.editTextEmail);
        tietPhone = getView().findViewById(R.id.editTextPhone);

        btnConfirm = getView().findViewById(R.id.btnConfirm);

        containerClientInfo = getView().findViewById(R.id.containerClientInfo);

        populateSpinnerClients();
        setBtnConfirmListener();
    }

    private void populateSpinnerClients() {

        SearchableSpinner searchableSpinner = getView().findViewById(R.id.searchableSpinner);
        searchableSpinner.setAdapter(new ClientSearchableSpinnerAdapter());
        searchableSpinner.setPositiveButton("Cerrar");
        searchableSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    showInputClientInfo();
                    searchableSpinner.setTitle(getString(R.string.new_client));
                    selectedClient = null;
                } else {
                    hideInputClientInfo();
                    selectedClient = clientList.get(position - 1);
                    searchableSpinner.setTitle(selectedClient.getName());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showInputClientInfo() {
        containerClientInfo.setVisibility(View.VISIBLE);
    }

    private void hideInputClientInfo() {
        containerClientInfo.setVisibility(View.GONE);
    }


    /**
     * This method listen when the user click on the button
     */
    private void setBtnConfirmListener() {
        btnConfirm.setOnClickListener(v ->{
            onConfirmClicked();}
        );
    }


    /**
     * Method to know when the btnConfirm is clicked,
     * and it calls another method to validate dates,
     * but first it validates if the user is premium or not, is answer es not,
     * it shows the user getPremium scree
     */
    public void onConfirmClicked() {
            if (selectedClient == null) {
                if (validateClientInfo()) {
                    if (GetPremiumActivity.hasPremiumInPlaceOrShowScreen(requireActivity(), placeId, UserManagement.getInstance().getUser().getId())) {
                        listener.onConfirm(
                                tietName.getText().toString(),
                                tietEmail.getText().toString(),
                                "+549" + tietPhone.getText().toString()
                        );
                    }
                }
            } else {
                listener.onConfirm(
                        selectedClient.getName(),
                        selectedClient.getEmail(),
                        null
                );
            }
        //}
    }


    /**
     * Metod to validate if user has all fields completed
     *
     * @return
     */
    private boolean validateClientInfo() {
        boolean isValid = true;

        if (TextUtils.isEmpty(tietName.getText())) {
            tilName.setError("Ingrese el nombre del cliente");
            isValid = false;
        } else {
            tilName.setError(null);
        }

        if (TextUtils.isEmpty(tietEmail.getText())) {
            tilEmail.setError("Ingrese el email del cliente");
            isValid = false;
        } else if (!ValidationUtils.validateEmail(tietEmail.getText().toString())) {
            tilEmail.setError("Ingrese un email válido");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        if (TextUtils.isEmpty(tietPhone.getText())) {
            tilPhone.setError("Ingrese el teléfono del cliente");
            isValid = false;

        } else if (!ValidationUtils.validatePhoneNumber(tietPhone.getText().toString())) {
            tilPhone.setError(getString(R.string.ingrese_telefono_valido));
            isValid = false;
        } else {
            tilPhone.setError(null);
        }




        return isValid;
    }

    public interface OnConfirmedListener {
        void onConfirm(String name, String email, String phonenumber);
    }

    private class ClientSearchableSpinnerAdapter extends ArrayAdapter<String> {

        public ClientSearchableSpinnerAdapter() {
            super(ClientInfoSelectionFragment.this.getContext(), R.layout.item_client);
        }

        @Override
        public View getDropDownView(int i, View view, ViewGroup viewGroup) {
            return null;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_client, parent, false);
            }

            TextView labelName = convertView.findViewById(R.id.itemClientLabel);

            if (position > 0) {
                CustomUser user = clientList.get(position - 1);
                labelName.setText(user.getName());
                labelName.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            } else {
                labelName.setText(getString(R.string.new_client));
                labelName.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            }

            return convertView;
        }

        @Override
        public int getCount() {
            return clientList.size() + 1;
        }

        @Override
        public String getItem(int position) {
            if (position == 0) {
                return getString(R.string.new_client);
            }
            return clientList.get(position - 1).getName();
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}