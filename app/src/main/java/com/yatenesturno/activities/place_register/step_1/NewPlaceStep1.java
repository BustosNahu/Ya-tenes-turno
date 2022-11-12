package com.yatenesturno.activities.place_register.step_1;

import static android.provider.MediaStore.Video.Thumbnails.getThumbnail;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.activities.ObjectConfigurator;
import com.yatenesturno.activities.place_register.NewPlaceActivity;
import com.yatenesturno.activities.place_register.NewPlaceLocationFragment;
import com.yatenesturno.activities.place_register.step_2.NewPlaceIntroStep2;
import com.yatenesturno.database.DatabaseDjangoRead;
import com.yatenesturno.database.ImageLoaderRead;
import com.yatenesturno.database.ImageLoaderReadImpl;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.JobType;
import com.yatenesturno.object_interfaces.Place;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.object_views.ViewJobTypeSelection;
import com.yatenesturno.serializers.BuilderListJobType;
import com.yatenesturno.utils.Utility;
import com.yatenesturno.utils.ValidationUtils;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;
import eu.davidea.flexibleadapter.FlexibleAdapter;


public class NewPlaceStep1 extends ObjectConfigurator {

    private static final String TAG = "NEWPLACENAMEANDPICFRAGMENT";
    final int RC_PICK_IMAGE = 1, RC_CREATING = 2;
    private List<ViewJobTypeSelection> jobTypeViewList;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    Bundle saveStateBundle = new Bundle();

    private final Place place;
    private Bitmap bitmap;
    private TextInputEditText etShopName, etSlogan,etPhone, etAddress, etShopCategory;
    private CardView   continueBtn;
    private CircleImageView ivPic ;
    private RecyclerView jobsRecyclerView;
    private ImageButton backArrow;
    private List<JobType> jobTypeList;
    boolean jobsRvDisplayed;
    private boolean isPopulated;
    private FlexibleAdapter<ViewJobTypeSelection> adapter;
    String userChoosenTask;
    Uri uri;
    private LatLng selectedLatLng;

    public NewPlaceStep1(Place place) {
        this.place = place;
        jobsRvDisplayed = false;
        isPopulated = false;

        getJobTypes();
    }

    public NewPlaceStep1() {
        //this.place = null;
        this(null);
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();

        if (jobTypeViewList.size() > 0) {
            populateRecyclerView();
            this.isPopulated = true;
        }
        fillIfEditingPlace();
        if (((NewPlaceActivity)requireActivity()).savedBundle){
            restoreState(((NewPlaceActivity)requireActivity()).returnBundleFromActToFragStep1());
        }


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putAll(saveState());

    }


    public ArrayList<JobType> getSelectedJobTypes() {
        ArrayList<JobType> selected_jobtypes = new ArrayList<>();
        for (ViewJobTypeSelection typeView : jobTypeViewList) {
            if (typeView.isSelected()) {
                selected_jobtypes.add(typeView.getJobType());
            }

        }
        return selected_jobtypes;
    }
    private Bundle saveState() {
        saveStateBundle.putString("etShopName", etShopName.getText().toString());
        saveStateBundle.putString("etSlogan", etSlogan.getText().toString());
        saveStateBundle.putString("etCategory", etShopCategory.getText().toString());
        saveStateBundle.putString("etPhone", etPhone.getText().toString());
        if (selectedLatLng != null){
            saveStateBundle.putString("address",  selectedLatLng.latitude + ":" + selectedLatLng.longitude);
        }
        saveStateBundle.putString("etAddress", etAddress.getText().toString());
        saveStateBundle.putParcelableArrayList("jobTypeList", (ArrayList<JobType>) jobTypeList);

        List<JobType> selectedJobTypes = getSelectedJobTypes();
        if (selectedJobTypes.size() > 0) {
            saveStateBundle.putParcelableArrayList("selectedJobTypeList", (ArrayList<JobType>) selectedJobTypes);
        }

        if (uri != null){
            Log.d("NewPLaceStep1Uri", uri.toString());
            saveStateBundle.putString("uri", uri.toString());
        }

        if (place != null) {
            saveStateBundle.putParcelable("place", place);
        }

        return saveStateBundle;
    }

    private void restoreState(Bundle savedInstanceState) {
        etShopName.setText(savedInstanceState.getString("etShopName"));
        etSlogan.setText(savedInstanceState.getString("etSlogan"));
        etPhone.setText(savedInstanceState.getString("etPhone"));
        etAddress.setText(savedInstanceState.getString("etAddress"));
        etShopCategory.setText(savedInstanceState.getString("etCategory"));
        jobTypeList = savedInstanceState.getParcelableArrayList("jobTypeList");

        if (savedInstanceState.containsKey("uri")) {
             uri = Uri.parse(savedInstanceState.getString("uri"));
             ivPic.setImageURI(uri);
        }
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getParentFragmentManager().setFragmentResultListener("dataFromMap", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                selectedLatLng = result.getParcelable("selectedLatLng");
                if (selectedLatLng == null){
                    showLocationError();
                }else {
                    getAddress(selectedLatLng.latitude, selectedLatLng.longitude);

                }
                Log.d("selectedLatLag", selectedLatLng.toString());
            }
        });
        if (savedInstanceState != null){
            restoreState(savedInstanceState);
        }
        return inflater.inflate(R.layout.fragment_new_place_a, container, false);
    }
    private void getAddress(double latitude, double longitude){
        try {
            Geocoder geo = new Geocoder(requireContext().getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
            if (addresses.isEmpty()) {
                etAddress.setText("Error");
            }
            else {
                if (addresses.size() > 0) {
                    etAddress.setText(addresses.get(0).getAddressLine(0).toString());
                    Log.d("addressget", addresses.get(0).getAddressLine(0));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }
    }



    public void initViews() {
       // requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        etShopName = getView().findViewById(R.id.shop_name_et);
        etSlogan = getView().findViewById(R.id.slogan_et_new_place);
        etPhone = getView().findViewById(R.id.phone_et_new_place);
        etAddress = getView().findViewById(R.id.address_et_new_place);
        etShopCategory = getView().findViewById(R.id.category_et_new_place);
        ivPic = getView().findViewById(R.id.image_card_view);
        continueBtn = getView().findViewById(R.id.btn_continue_step1);
        backArrow = getView().findViewById(R.id.back_btn_new_place_a);
        jobsRecyclerView = getView().findViewById(R.id.recyclerViewJobTypes);

        continueBtn.setOnClickListener(v -> openStep2());


        etShopCategory.setOnClickListener( v ->  displayJobsRv());

        etAddress.setOnClickListener( v ->openMapFragment());


        backArrow.setOnClickListener( v -> navigateBack());
        ivPic.setOnClickListener(v -> checkPermission());

    }

    private void displayJobsRv() {
        if (jobsRvDisplayed){
            jobsRecyclerView.setVisibility(View.GONE);
            jobsRvDisplayed = false;
        }else{
            jobsRecyclerView.setVisibility(View.VISIBLE);
            jobsRvDisplayed = true;

        }
    }


    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }
    private void openMapFragment() {
        ((NewPlaceActivity)requireActivity()).saveBundleFromStep1(saveState());
        Fragment fragment = new NewPlaceLocationFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.new_place_fragment_main, fragment)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((NewPlaceActivity)requireActivity()).saveBundleFromStep1(saveState());
    }

    private void openStep2() {
        if (validateData()){
            ((NewPlaceActivity)requireActivity()).saveBundleFromStep1(saveState());
            Log.d("dblSAved", saveState().toString());
            Fragment fragment = new NewPlaceIntroStep2();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.new_place_fragment_main, fragment)
                    .addToBackStack("")
                    .commit();
        }

    }


    private void navigateBack(){
        Fragment fragment = new NewPlaceIntroStep1();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.new_place_fragment_main, fragment)
                .addToBackStack("a")
                .commit();
    }



    public void fillIfEditingPlace() {
        if (isEditing()) {
            etShopName.setText(place.getBusinessName());
            etSlogan.setText(place.getSlogan());
            etPhone.setText(place.getPhoneNumber());
            etAddress.setText(place.getAddress());
            etShopCategory.setText(place.getCategory());

            ImageLoaderRead imageLoaderRead = new ImageLoaderReadImpl();
            imageLoaderRead.getImage(getContext(), place, new ImageLoaderRead.OnGetImageListener() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    updateBitmap(bitmap);

                }

                @Override
                public void onFailure() {

                }
            });
        }
    }

    void imageChooser() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

        startActivityForResult(getIntent, RC_PICK_IMAGE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }


    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        bitmap = thumbnail;
        ivPic.setImageBitmap(thumbnail);
        ivPic.setScaleX(1);
        ivPic.setScaleY(1);
        ivPic.setVisibility(View.VISIBLE);
    }


    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm=null;
        if (data != null) {
            try {
                uri = data.getData();
                bm = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), data.getData());
                saveStateBundle.putString("uri", uri.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bitmap = bm;
        ivPic.setImageBitmap(bm);
        ivPic.setScaleX(1);
        ivPic.setScaleY(1);
        ivPic.setVisibility(View.VISIBLE);

    }




    public boolean isEditing() {
        return place != null;
    }

    public void updateBitmap(Bitmap bitmap) {
        ivPic.setImageBitmap(bitmap);
        this.bitmap = bitmap;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bitmap != null) {
            ivPic.setImageBitmap(bitmap);
        }else{
            if (uri != null){
                ivPic.setImageURI(uri);
            }
        }

    }


    private void getJobTypes() {
        jobTypeViewList = new ArrayList<>();
        DatabaseDjangoRead.getInstance().GET(
                Constants.DJANGO_URL_JOBTYPES,
                null,
                new CallbackGetJobTypes()
        );
    }

    private class CallbackGetJobTypes extends DatabaseCallback {

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            try {
                onJobTypesFetch(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            showConnectionError();
        }
    }

    public void showConnectionError() {
        Toast.makeText(getActivity(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
    }
    public void showLocationError() {
        Toast.makeText(getActivity(), "Error obteniendo ubicacion.", Toast.LENGTH_SHORT).show();
    }

    private void setSelectedToViews(List<JobType> selectedJobTypes) {
        for (JobType jobType : selectedJobTypes) {
            for (ViewJobTypeSelection jobTypeView : jobTypeViewList) {
                if (jobTypeView.getJobType().equals(jobType)) {
                    jobTypeView.setSelected(true);
                }
            }
        }
    }

    public void onJobTypesFetch(JSONObject response) throws JSONException {
        jobTypeList = new BuilderListJobType().build(response);

        setUpRecyclerView();

        if (place != null) {
            setSelectedToViews(place.getJobTypes());
            setUnchangeable(place.getJobTypes());
        }
    }

    private void setUnchangeable(List<JobType> jobTypes) {
        for (JobType jobType : jobTypes) {
            for (ViewJobTypeSelection jobTypeView : jobTypeViewList) {
                if (jobTypeView.getJobType().equals(jobType)) {
                    jobTypeView.disableCheck();
                }
            }
        }
    }

    public void setUpRecyclerView() {
        jobTypeViewList.clear();
        for (JobType jobType : jobTypeList) {
            jobTypeViewList.add(new ViewJobTypeSelection(jobType));
        }

        if (!isPopulated && jobsRecyclerView != null && getActivity() != null) {
            populateRecyclerView();
        }
    }

    private void populateRecyclerView() {
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        jobsRecyclerView.setLayoutManager(layoutManager);

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter = new FlexibleAdapter<>(jobTypeViewList);
                jobsRecyclerView.setAdapter(adapter);
            }
        });
        ListenerQueryTextJobType listener = new ListenerQueryTextJobType();
    }



    private class ListenerQueryTextJobType implements SearchView.OnQueryTextListener {

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
    }

    


    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            imageChooser();
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RC_CREATING);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    Snackbar.make(requireView(), "No se ha podido obtener el permiso para obtener una imagen", BaseTransientBottomBar.LENGTH_LONG).show();
                }
                break;
        }

    }

    public Bundle getData() {
//        String name = etShopName.getText().toString();
//        String slogan = etSlogan.getText().toString();
//        String info = etShopCategory.getText().toString();
//
//        Bundle bundle = new Bundle();
//        bundle.putString("businessname", name);
//        bundle.putString("slogan", slogan);
//        bundle.putString("info", info);
//
////        if (uri != null) {
////            bundle.putString("uri", uri.toString());
////        }

        return saveStateBundle;
    }

    public boolean validateData() {

        TextInputLayout layoutName = requireView().findViewById(R.id.textInputName);
        TextInputLayout phone = requireView().findViewById(R.id.phone_textInputLayout);
        TextInputLayout address = requireView().findViewById(R.id.location_textInputLayout);
        //make with all lyouts
        if (TextUtils.isEmpty(etShopName.getText())) {
            layoutName.setError("Este campo es obligatorio");
            return false;
        } else {
            layoutName.setError(null);
        }
        if (uri == null) {
            Snackbar.make(requireView(), "Seleccione una imagen", BaseTransientBottomBar.LENGTH_SHORT).show();
            return false;
        }else if (TextUtils.isEmpty(etAddress.getText())){
            address.setError("Ingresa una direccion");
            return false;
        }else if (getSelectedJobTypes().size() == 0){
            Snackbar.make(requireView(), "Seleccione al menos un tipo de trabajo", BaseTransientBottomBar.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(etPhone.getText())) {
            phone.setError(getString(R.string.obligatory_field));
            return false;
        } else if (!ValidationUtils.validatePhoneNumber(etPhone.getText().toString())) {
            phone.setError(getString(R.string.ingrese_telefono_valido));
            return false;
        } else {
            phone.setError(null);
        }

        return true;
    }

}