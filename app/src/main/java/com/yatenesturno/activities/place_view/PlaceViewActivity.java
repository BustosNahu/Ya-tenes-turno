package com.yatenesturno.activities.place_view;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.custom_views.LoadingOverlay;
import com.yatenesturno.database.djangoImpl.DatabaseDjangoWrite;
import com.yatenesturno.database.interfaces.ImageLoaderRead;
import com.yatenesturno.database.ImageLoaderReadImpl;
import com.yatenesturno.functionality.ManagerPlace;
import com.yatenesturno.listeners.DatabaseCallback;
import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.Place;
import com.yatenesturno.user_auth.UserManagement;
import com.yatenesturno.utils.CustomAlertDialogBuilder;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

public class PlaceViewActivity extends AppCompatActivity {

    private final int RC_EDIT_PLACE = 1;
    private Place place;
    private List<Job> jobList;
    private LoadingOverlay loadingOverlay;
    private RecyclerView recyclerViewJobs;
    private String placeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_place);

        Intent intent = getIntent();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setBackgroundDrawable(getDrawable(R.color.white));
        getSupportActionBar().setElevation(0);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setTitle("");

        loadingOverlay = new LoadingOverlay(findViewById(R.id.root));
        recyclerViewJobs = findViewById(R.id.recyclerViewJobs);

        placeId = intent.getExtras().getString("placeId");
        initUI();
    }

    private void initUI() {
        ManagerPlace.getInstance().getOwnedPlaces(new ManagerPlace.OnPlaceListFetchListener() {
            @Override
            public void onFetch(List<Place> placeList) {
                loadingOverlay.hide();
                place = findPlace(placeList, placeId);
                updateUI();
            }

            @Override
            public void onFailure() {
                Toast.makeText(getApplicationContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
                loadingOverlay.hide();
            }
        });
    }

    public Place findPlace(List<Place> list, String placeId) {
        for (Place p : list) {
            if (p.getId().equals(placeId)) {
                return p;
            }
        }
        return null;
    }

    private void updateUI() {
        TextView textviewBusinessName = findViewById(R.id.labelBusinessName);
        CircleImageView circleImageView = findViewById(R.id.imageViewPlace);

        textviewBusinessName.setText(place.getBusinessName());
        new ImageLoaderReadImpl().getImage(this, place, new ImageLoaderRead.OnGetImageListener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                circleImageView.setImageBitmap(bitmap);
            }

            @Override
            public void onFailure() {

            }
        });


        jobList = place.getJobList();
        if (jobList.size() > 0) {
            displayHasJobs();
        } else {
            displayNoJobs();
        }
        checkForOwnJob();
    }

    private void displayNoJobs() {
        recyclerViewJobs.setVisibility(View.GONE);
        findViewById(R.id.noJobsInPlaceView).setVisibility(View.VISIBLE);
    }

    private void displayHasJobs() {
        recyclerViewJobs.setVisibility(View.VISIBLE);
        findViewById(R.id.noJobsInPlaceView).setVisibility(View.GONE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewJobs.setLayoutManager(layoutManager);
        AdapterRecyclerViewJobs adapterRecyclerViewJobs = new AdapterRecyclerViewJobs();
        recyclerViewJobs.setAdapter(adapterRecyclerViewJobs);
    }

    private void checkForOwnJob() {
        ExtendedFloatingActionButton btnNewOwnJob = findViewById(R.id.btnNewOwnJob);
        if (!doesAJobInPlace()) {
            btnNewOwnJob.setVisibility(View.VISIBLE);
            btnNewOwnJob.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createOwnJob();
                }
            });
        } else {
            btnNewOwnJob.setVisibility(View.GONE);
        }
    }

    private void createOwnJob() {
        loadingOverlay.show();
        Map<String, String> body = new HashMap<>();
        body.put("place_id", place.getId());

        DatabaseDjangoWrite.getInstance().POST(
                Constants.DJANGO_URL_NEW_OWNER_JOB,
                body,
                new DatabaseCallback() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        ManagerPlace.getInstance().invalidate();
                        setResult(RESULT_OK);
                        initUI();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        loadingOverlay.hide();
                        setResult(RESULT_CANCELED);
                        Toast.makeText(getApplicationContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

    private boolean doesAJobInPlace() {
        String userId = UserManagement.getInstance().getUser().getId();
        for (Job j : jobList) {
            if (j.getEmployee().getId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_delete, menu);
        menu.getItem(0).setVisible(true);
        return true;
    }

    private void showDialogRemovePlace() {
        String title = "¿Desea eliminar este lugar?";

        new CustomAlertDialogBuilder(this)
                .setTitle(title)
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onRemovePlaceConfirmed();
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void onRemovePlaceConfirmed() {
        loadingOverlay.show();
        ManagerPlace.getInstance().dropPlace(
                place,
                new ManagerPlace.OnDropListener() {
                    @Override
                    public void onDrop() {
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onFailure() {
                        setResult(RESULT_CANCELED);
                        Toast.makeText(getApplicationContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
                        loadingOverlay.hide();
                    }
                });
    }

    private void showRemoveJobDialog(final int position) {
        final Job job = jobList.get(position);

        String title = "¿Desea remover el trabajo de " + job.getEmployee().getName() + "?";

        new CustomAlertDialogBuilder(this)
                .setTitle(title)
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeJob(job);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void removeJob(final Job job) {
        loadingOverlay.show();
        ManagerPlace.getInstance().dropJob(
                place,
                job,
                new ManagerPlace.OnDropListener() {
                    @Override
                    public void onDrop() {
                        setResult(RESULT_OK);
                        initUI();
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
                        loadingOverlay.hide();
                        setResult(RESULT_CANCELED);
                    }
                }
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.delete:
                showDialogRemovePlace();
                return true;
            case R.id.edit:
                showEditPlaceActivity();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showEditPlaceActivity() {
        Intent intent = new Intent(this, EditPlaceActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("placeId", place.getId());
        intent.putExtras(bundle);
        startActivityForResult(intent, RC_EDIT_PLACE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_EDIT_PLACE && resultCode == RESULT_OK) {
            ManagerPlace.getInstance().invalidate();
            initUI();
            setResult(RESULT_OK);
        }
    }

    private void showSelectAccessModeDialog(final Job job) {
        final View accessModeView = getLayoutInflater().inflate(R.layout.access_mode_view, null);
        final Switch canEditSwitch = accessModeView.findViewById(R.id.switchEdit);
        final Switch canChatSwitch = accessModeView.findViewById(R.id.switchChat);

        canChatSwitch.setChecked(job.canChat());
        canEditSwitch.setChecked(job.canEdit());

        new CustomAlertDialogBuilder(this)
                .setMessage("Selecciona las opciones de acceso que deseas permitir al empleado")
                .setView(accessModeView)
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean canEdit = canEditSwitch.isChecked();
                        boolean canChat = canChatSwitch.isChecked();
                        if (job.canChat() != canChat || job.canEdit() != canEdit) {
                            savePermissions(job, canEdit, canChat);
                        }
                    }
                })
                .show();
    }

    private void savePermissions(Job job, boolean canEdit, boolean canChat) {
        loadingOverlay.show();
        ManagerPlace.getInstance().changeJobPermissions(
                job,
                canEdit,
                canChat,
                new ManagerPlace.OnChangePermissionListener() {
                    @Override
                    public void onChange() {
                        setResult(RESULT_OK);
                        initUI();
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_de_conexion), Toast.LENGTH_SHORT).show();
                        loadingOverlay.hide();
                        setResult(RESULT_CANCELED);
                    }
                }
        );
    }

    private class AdapterRecyclerViewJobs extends RecyclerView.Adapter<AdapterRecyclerViewJobs.ViewHolderJob> {

        private final Map<String, Bitmap> userBitmapMap;

        public AdapterRecyclerViewJobs() {
            userBitmapMap = new HashMap<>();
        }

        @NonNull
        @Override
        public ViewHolderJob onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.job_item_layout, parent, false);
            return new ViewHolderJob(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderJob holder, int position) {
            CustomUser user = jobList.get(position).getEmployee();
            holder.labelEmployee.setText(user.getName());

            boolean isOwner = user.getId().equals(UserManagement.getInstance().getUser().getId());
            if (!userBitmapMap.containsKey(user.getId())) {
                getProfileImage(user, holder.ivPic);
            } else {
                Bitmap image = userBitmapMap.get(user.getId());
                if (image != null) {
                    holder.ivPic.setImageBitmap(image);
                }
            }
            holder.removeJob.setOnClickListener(v -> showRemoveJobDialog(position));
            if (isOwner) {
                holder.editJob.setVisibility(View.INVISIBLE);
            } else {
                holder.editJob.setVisibility(View.VISIBLE);
                holder.editJob.setOnClickListener(v -> showSelectAccessModeDialog(jobList.get(position)));
            }
        }

        private void getProfileImage(final CustomUser user, final ImageView iv) {
            new ImageLoaderReadImpl().getImage(getApplicationContext(), user, new ImageLoaderRead.OnGetImageListener() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    userBitmapMap.put(user.getId(), bitmap);
                    iv.setImageBitmap(bitmap);
                }

                @Override
                public void onFailure() {

                }
            });
        }

        @Override
        public int getItemCount() {
            return jobList.size();
        }

        public class ViewHolderJob extends RecyclerView.ViewHolder {

            public CircleImageView ivPic;
            public TextView labelEmployee;
            public ImageButton removeJob, editJob;

            public ViewHolderJob(@NonNull View view) {
                super(view);

                labelEmployee = view.findViewById(R.id.labelEmployee);
                removeJob = view.findViewById(R.id.removeJob);
                ivPic = view.findViewById(R.id.ivPic);
                editJob = view.findViewById(R.id.editJob);
            }
        }
    }
}