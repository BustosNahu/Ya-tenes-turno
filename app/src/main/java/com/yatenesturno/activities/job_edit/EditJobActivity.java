package com.yatenesturno.activities.job_edit;

import static com.yatenesturno.activities.get_premium.GetPremiumActivity.showPremiumInfoFromActivity;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.activities.get_premium.GetPremiumActivity;
import com.yatenesturno.activities.job_edit.days_off.DaysOffActivity;
import com.yatenesturno.activities.tutorial_screen.Screen;
import com.yatenesturno.activities.tutorial_screen.ScreenImpl;
import com.yatenesturno.activities.tutorial_screen.TutorialScreenImpl;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.functionality.DayScheduleManager;
import com.yatenesturno.functionality.JobSaver;
import com.yatenesturno.listeners.RepositorySaveListener;
import com.yatenesturno.object_interfaces.DaySchedule;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.user_auth.UserManagement;
import com.yatenesturno.utils.CustomAlertDialogBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EditJobActivity extends AppCompatActivity {

    /**
     * Arguments keys
     */
    private static final String DAY_SCHEDULE_FRAGMENT = "dayScheduleFragment";

    private static final String SWITCH_STATUS = "switch_status";

    private static final String MY_PREF = "switch_pref";

    boolean switch_status;

    /**
     * Instance variables
     */
    private Place place;
    private Job originalJob;
    private Job editedJob;

    private LinearLayout layout;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public void SetJobActivity(Job job, Place place) {
        this.originalJob = job;
        this.place = place;
    }

    private final ActivityResultLauncher<Intent> configLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null && result.getData().getExtras() != null) {
                        Bundle extras = result.getData().getExtras();
                        editedJob = extras.getParcelable("job");
                    }
                    setResult(RESULT_OK);
                }
            });
    /**
     * UI References
     */
    private LoadingOverlay loadingOverlay;
    private SwitchCompat switchUserCancellableApps;
    private EmergencySetupFragment emergencySetUpFragment;
    private DayScheduleConfigFragment dayScheduleConfigFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_job);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setTitle("");
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        loadingOverlay = new LoadingOverlay(findViewById(R.id.root));
        switchUserCancellableApps = findViewById(R.id.switchUserCancellableApps);

        if (savedInstanceState == null) {
            initUI();

        } else {
            recoverState(savedInstanceState);
            initViews();
        }

    }

    private void initUI() {

        originalJob = getIntent().getExtras().getParcelable("job");
        place = getIntent().getExtras().getParcelable("place");
        Log.d("editJobActivity", originalJob.getEmployee().getName());
        if (originalJob.getDaySchedules() == null) {
            fetchDaySchedules();
        } else {
            editedJob = originalJob.clone();
            Log.d("editJobActivity EDITED CLONED", editedJob.getDaySchedules().toString());
            initViews();
        }


    }

    private void fetchDaySchedules() {
        showLoadingOverlay();
        new Handler(Looper.myLooper()).postDelayed(() -> DayScheduleManager.getInstance().getDaySchedules(originalJob.getId(), dayScheduleList -> {
            hideLoadingOverlay();
            originalJob.setDaySchedules(dayScheduleList);
            Log.d("editJobActivity ORIGINAL", originalJob.getDaySchedules().toString());
            editedJob = originalJob.clone();
            Log.d("editJobActivity EDITED", editedJob.getDaySchedules().toString());

            loadingOverlay.hide();
            initViews();
        }), 300);
    }

    public void hideLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.hide();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.putFragment(outState, DAY_SCHEDULE_FRAGMENT, dayScheduleConfigFragment);

        outState.putAll(saveState());
    }

    private Bundle saveState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("editedJob", editedJob);
        bundle.putParcelable("originalJob", originalJob);
        bundle.putParcelable("place", place);

        return bundle;
    }

    private void recoverState(Bundle bundle) {
        editedJob = bundle.getParcelable("editedJob");
        originalJob = bundle.getParcelable("originalJob");
        place = bundle.getParcelable("place");

        FragmentManager fragmentManager = getSupportFragmentManager();

        dayScheduleConfigFragment = (DayScheduleConfigFragment) fragmentManager.getFragment(bundle, DAY_SCHEDULE_FRAGMENT);
    }

    private void initViews() {
        findViewById(R.id.holderContent).setVisibility(View.VISIBLE);
        setUpSwitchCancellableApps();
        setEditServicesBtnListener();
        setUpDayScheduleFragment();
        setUpEmergencyFragment();
        setUpDayOff();

    }

    private void notSetUpSwitchCancellable() {
        switchUserCancellableApps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (GetPremiumActivity.hasPremiumInPlaceOrShowScreen(EditJobActivity.this, place.getId(), UserManagement.getInstance().getUser().getId())) {
//                        onCheckedChanged(switchUserCancellableApps, true);
                    }
//                    onCheckedChanged(switchUserCancellableApps, true);

                } else {
                    if (GetPremiumActivity.hasPremiumInPlaceOrShowScreen(EditJobActivity.this, place.getId(), UserManagement.getInstance().getUser().getId())) {
//                        onCheckedChanged(switchUserCancellableApps, false);
                    }
//                    onCheckedChanged(switchUserCancellableApps, true);
                }
            }
        });

    }


    private void setUpDayOff() {
        View view = findViewById(R.id.btnOpenDayOffConfig);

        view.setOnClickListener(view1 -> {
            Intent intent = new Intent(EditJobActivity.this, DaysOffActivity.class);

            Bundle bundle = new Bundle();
            bundle.putParcelable("job", editedJob);
            bundle.putParcelable("place", place);
            intent.putExtras(bundle);

            configLauncher.launch(intent);
        });
    }

    private void setUpSwitchCancellableApps() {
        //editedJob.setUserCanCancelApps(isChecked));
        //switchUserCancellableApps.setOnCheckedChangeListener(null);

        //INICIA EL SWITCH SEGUN EL USUARIO ASI LO HAYA CONFIGURADO ANTERIORMENTE
        switchUserCancellableApps.setChecked(originalJob.canUserCancelApps());
        switchUserCancellableApps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (GetPremiumActivity.hasPremiumInPlaceOrShowScreen(EditJobActivity.this, place.getId(), UserManagement.getInstance().getUser().getId())) {
                    if (b == true) {
                        //if (GetPremiumActivity.hasPremiumInPlaceOrShowScreen(EditJobActivity.this, place.getId(), UserManagement.getInstance().getUser().getId())) {
                        editedJob.setUserCanCancelApps(b);
                        //}
                        //returnStateFalse();
                    } else if (b == false) {

                        //if (GetPremiumActivity.hasPremiumInPlaceOrShowScreen(EditJobActivity.this, place.getId(), UserManagement.getInstance().getUser().getId())) {
                        editedJob.setUserCanCancelApps(b);
                        // }
                        //returnStateTrue();
                    }
                }else if(b == true){
                    //no es premium
                    returnStateFalse();
                }else if(b == false){
                    //no es premium
                    returnStateTrue();
                }
            }
        });

    }

    private void returnStateTrue() {
        switchUserCancellableApps.setChecked(true);
    }

    private void returnStateFalse() {
        switchUserCancellableApps.setChecked(false);
    }

    private void setEditServicesBtnListener() {
        findViewById(R.id.btnServiceConfiguration).setOnClickListener(new ListenerBtnServiceConfiguration());
    }

    private void setUpEmergencyFragment() {
        if (shouldShowEmergency()) {
            findViewById(R.id.divider2).setVisibility(View.VISIBLE);

            if (hasJobChanged()) {
                findViewById(R.id.unsavedChanges).setVisibility(View.VISIBLE);
                findViewById(R.id.containerEmergency).setVisibility(View.GONE);
            } else {
                findViewById(R.id.containerEmergency).setVisibility(View.VISIBLE);
                findViewById(R.id.unsavedChanges).setVisibility(View.GONE);
                if (emergencySetUpFragment == null) {
                    emergencySetUpFragment = new EmergencySetupFragment(place, editedJob);
                }
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.containerEmergency, emergencySetUpFragment);
                transaction.commit();
            }

        } else {
            findViewById(R.id.divider2).setVisibility(View.GONE);
            findViewById(R.id.containerEmergency).setVisibility(View.GONE);
        }
    }

    private boolean shouldShowEmergency() {
        for (DaySchedule ds : editedJob.getDaySchedules()) {
            for (ServiceInstance si : ds.getServiceInstances()) {
                if (si.isEmergency()) return true;
            }
        }
        return false;
    }

    private void setUpDayScheduleFragment() {
        if (dayScheduleConfigFragment == null) {
            dayScheduleConfigFragment = new DayScheduleConfigFragment(editedJob);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containerDaySchedule, dayScheduleConfigFragment);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_job, menu);

        MenuItem item = menu.findItem(R.id.save_job);
        SpannableString spanString = new SpannableString(item.getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(getColor(R.color.black)), 0, spanString.length(), 0);
        item.setTitle(spanString);

        return true;
    }

    private void returnCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void returnOK() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (hasJobChanged()) {
            showSaveDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showSaveDialog() {
        new CustomAlertDialogBuilder(this)
                .setTitle(getString(R.string.save_before_exiting))
                .setPositiveButton(R.string.save, new ListenerSaveChanges())
                .setNeutralButton(R.string.discard, new ListenerDiscardChanges())
                .show();
    }

    private boolean hasJobChanged() {
        return !Objects.equals(editedJob, originalJob);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.RC_EDIT_SERVICES:

                case Constants.RC_EDIT_HOURS:
                    editedJob = (Job) data.getExtras().getSerializable("job");
                    dayScheduleConfigFragment.setJob(editedJob);
                    setUpEmergencyFragment();
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.save_job:
                onSaveJobClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onSaveJobClicked() {
        if (continueShowingWarning()) {
            showWarning();
        } else {
            commitJob();
        }
    }

    private boolean continueShowingWarning() {
        return getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE).getBoolean(Constants.SHARED_PREF_SCHEDULE_EDIT_WARNING, true);
    }

    private void commitJob() {
        showLoadingOverlay();
        new JobSaver().saveJob(editedJob, new RepositorySaveListener() {
            @Override
            public void onSuccess() {
                hideLoadingOverlay();
                returnOK();
            }

            @Override
            public void onFailure() {
                showConnectionErrorMessage();
                hideLoadingOverlay();
            }
        });
    }

    public void showConnectionErrorMessage() {
        Toast.makeText(getApplicationContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
    }

    private void showLoadingOverlay() {
        if (loadingOverlay != null) {
            loadingOverlay.show();
        }
    }

    private void showWarning() {
        View view = getLayoutInflater().inflate(R.layout.schedule_save_warning, null, false);
        final CheckBox checkBox = view.findViewById(R.id.checkBoxWarning);
        new CustomAlertDialogBuilder(this)
                .setTitle(getString(R.string.warning_save_changes))
                .setView(view)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    commitJob();
                    dialog.dismiss();
                    if (checkBox.isChecked()) {
                        saveDoNotShowAgain();
                    }
                })
                .setOnDismissListener(dialogInterface -> {
                    if (checkBox.isChecked()) {
                        saveDoNotShowAgain();
                    }
                })
                .show();
    }

    public void saveDoNotShowAgain() {
        getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE)
                .edit()
                .putBoolean(Constants.SHARED_PREF_SCHEDULE_EDIT_WARNING, false)
                .apply();
    }

    private class ListenerSaveChanges implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            onSaveJobClicked();
        }
    }

    private class ListenerDiscardChanges implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            returnCancel();
        }
    }

    private class ListenerBtnServiceConfiguration implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("job", editedJob);
            bundle.putSerializable("place", place);

            Intent intent = new Intent(getApplicationContext(), ServiceConfigActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, Constants.RC_EDIT_SERVICES);
        }
    }
}