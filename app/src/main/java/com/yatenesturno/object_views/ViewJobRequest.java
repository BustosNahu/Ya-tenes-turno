package com.yatenesturno.object_views;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.LayoutRes;

import com.yatenesturno.R;
import com.yatenesturno.activities.job_request.JobRequestActivity;
import com.yatenesturno.object_interfaces.JobRequest;
import com.yatenesturno.utils.CustomAlertDialogBuilder;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

public class ViewJobRequest extends AbstractFlexibleItem<ViewJobRequest.ViewHolderJobRequest> implements IFilterable<Boolean> {

    private final JobRequestActivity jobRequestActivity;
    private final JobRequest jobRequest;
    private boolean accepted, denied;
    private boolean canChat, canEdit;

    public ViewJobRequest(JobRequestActivity activity, JobRequest jobRequest) {
        this.jobRequest = jobRequest;
        this.jobRequestActivity = activity;
        this.accepted = false;
        this.denied = false;
    }

    public JobRequest getJobRequest() {
        return jobRequest;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public boolean isDenied() {
        return denied;
    }

    @Override
    public boolean equals(Object o) {
        return ((ViewJobRequest) o).getJobRequest().equals(jobRequest);
    }

    @LayoutRes
    @Override
    public int getLayoutRes() {
        return R.layout.job_request_layout;
    }

    @Override
    public ViewHolderJobRequest createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
        return new ViewHolderJobRequest(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter<IFlexible> adapter, final ViewHolderJobRequest holder, final int position, List<Object> payloads) {
        holder.textViewToPlace.setText(jobRequest.getPlace().getBusinessName());
        holder.textViewFromWho.setText(jobRequest.getCustomUser().getName());
        holder.btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accepted = true;
                showSelectAccessModeDialog(holder.btnCancel.getContext());
            }
        });

        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                denied = true;
                removeJobRequest();
            }
        });
    }

    @Override
    public boolean filter(Boolean constraint) {
        return !isAccepted() && !isDenied();
    }

    private void showSelectAccessModeDialog(Context context) {
        final View accessModeView = LayoutInflater.from(context).inflate(R.layout.access_mode_view, null);
        new CustomAlertDialogBuilder(context)
                .setMessage(R.string.select_access_options)
                .setView(accessModeView)
                .setPositiveButton(R.string.accept, (dialog, which) -> {
                    canEdit = ((Switch) accessModeView.findViewById(R.id.switchEdit)).isChecked();
                    canChat = ((Switch) accessModeView.findViewById(R.id.switchChat)).isChecked();
                    acceptJobRequest();
                })
                .show();
    }

    public void acceptJobRequest() {
        jobRequestActivity.acceptJobRequest(this, canEdit, canChat);
    }

    public void removeJobRequest() {
        jobRequestActivity.removeJobRequest(this);
    }

    public static class ViewHolderJobRequest extends FlexibleViewHolder {
        public TextView textViewToPlace;
        public TextView textViewFromWho;
        public Button btnConfirm, btnCancel;

        public ViewHolderJobRequest(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.textViewToPlace = view.findViewById(R.id.textViewToPlace);
            this.textViewFromWho = view.findViewById(R.id.textViewFromWho);
            this.btnCancel = view.findViewById(R.id.btnCancel);
            this.btnConfirm = view.findViewById(R.id.btnConfirm);
        }
    }
}
