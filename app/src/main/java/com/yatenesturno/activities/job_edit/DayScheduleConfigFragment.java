package com.yatenesturno.activities.job_edit;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.Constants;
import com.yatenesturno.R;
import com.yatenesturno.object_interfaces.DaySchedule;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.ServiceInstance;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DayScheduleConfigFragment extends Fragment {

    private static final Map<Integer, String> mapNumberDay = new HashMap<Integer, String>() {{
        put(1, "Domingo");
        put(2, "Lunes");
        put(3, "Martes");
        put(4, "Miércoles");
        put(5, "Jueves");
        put(6, "Viernes");
        put(7, "Sábado");
    }};

    private Job job;
    private RecyclerView recyclerViewDays;
    private AdapterRecyclerViewDays adapterRecyclerViewDays;

    public DayScheduleConfigFragment(Job job) {
        this.job = job;
    }

    public DayScheduleConfigFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_configure, container, false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putAll(saveState());
    }

    private Bundle saveState() {
        Bundle bundle = new Bundle();

        bundle.putParcelable("job", job);

        return bundle;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.recyclerViewDays = view.findViewById(R.id.recyclerViewDays);

        if (savedInstanceState != null) {
            recoverState(savedInstanceState);
        }

        initUI();
    }

    private void recoverState(Bundle bundle) {
        job = bundle.getParcelable("job");
    }

    @Override
    public void onResume() {
        super.onResume();
        adapterRecyclerViewDays.notifyDataSetChanged();
    }

    private void initUI() {
        LinearLayoutManager layoutManagerDays = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewDays.setLayoutManager(layoutManagerDays);
        adapterRecyclerViewDays = new AdapterRecyclerViewDays();
        recyclerViewDays.setAdapter(adapterRecyclerViewDays);
    }

    public void setJob(Job job) {
        this.job = job;
        initUI();
    }

    private class AdapterRecyclerViewDays extends RecyclerView.Adapter<AdapterRecyclerViewDays.ViewHolderScheduleDays> {
        private final int[] days;

        public AdapterRecyclerViewDays() {
            days = new int[]{1, 2, 3, 4, 5, 6, 7};
        }

        @Override
        public ViewHolderScheduleDays onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.day_job_edit_layout, parent, false);

            return new ViewHolderScheduleDays(v);
        }

        @Override
        public void onBindViewHolder(ViewHolderScheduleDays holder, final int position) {
            holder.labelDay.setText(mapNumberDay.get(days[position]));
            if (hasScheduleThisDay(position)) {
                DaySchedule daySchedule = job.getDaySchedule(days[position]);
                holder.labelClosed.setVisibility(View.GONE);
                holder.btnEditHours.setVisibility(View.VISIBLE);
                holder.servicesContainer.setVisibility(View.VISIBLE);
                holder.labelLabourHours.setVisibility(View.VISIBLE);

                String startStr = String.format("%02d:%02d", daySchedule.getDayStart().get(Calendar.HOUR_OF_DAY), daySchedule.getDayStart().get(Calendar.MINUTE));
                String endStr = String.format("%02d:%02d", daySchedule.getDayEnd().get(Calendar.HOUR_OF_DAY), daySchedule.getDayEnd().get(Calendar.MINUTE));

                holder.labelLabourHours.setText(String.format("(%s - %s)", startStr, endStr));

                holder.servicesContainer.removeAllViews();
                for (ServiceInstance p : daySchedule.getServiceInstances()) {
                    if (!p.isEmergency()) {
                        View serviceView = createViewForService(p);
                        holder.servicesContainer.addView(serviceView);
                    }
                }

                holder.btnEditHours.setOnClickListener(new ListenerBtnHourEdit(days[position]));
                holder.servicesWrapper.setCardBackgroundColor(getContext().getColor(R.color.white));
            } else {
                holder.labelClosed.setVisibility(View.VISIBLE);
                holder.servicesContainer.setVisibility(View.GONE);
                holder.labelLabourHours.setVisibility(View.GONE);
                holder.btnEditHours.setVisibility(View.INVISIBLE);
                holder.servicesWrapper.setCardBackgroundColor(getContext().getColor(R.color.grey));
            }
        }

        private View createViewForService(ServiceInstance p) {
            View serviceView = LayoutInflater.from(getContext()).inflate(R.layout.day_schedule_service_view, null);

            ((TextView) serviceView.findViewById(R.id.labelService)).setText(p.getService().getName());

            return serviceView;
        }

        private boolean hasScheduleThisDay(int position) {
            DaySchedule ds = job.getDaySchedule(days[position]);
            if (ds == null) return false;

            for (ServiceInstance si : ds.getServiceInstances()) {
                if (!si.isEmergency()) return true;
            }

            return false;
        }

        @Override
        public int getItemCount() {
            return days.length;
        }

        public class ViewHolderScheduleDays extends RecyclerView.ViewHolder {
            public TextView labelDay, labelClosed, labelLabourHours;
            public ImageButton btnEditHours;
            public LinearLayout servicesContainer;
            public CardView servicesWrapper;

            public ViewHolderScheduleDays(View v) {
                super(v);
                btnEditHours = v.findViewById(R.id.btnEditHours);
                labelDay = v.findViewById(R.id.labelDay);
                labelClosed = v.findViewById(R.id.labelClosed);
                servicesContainer = v.findViewById(R.id.recyclerViewServices);
                labelLabourHours = v.findViewById(R.id.labelStart);
                servicesWrapper = v.findViewById(R.id.servicesWrapper);
            }
        }
    }

    private class ListenerBtnHourEdit implements View.OnClickListener {
        private final int dayNumber;

        public ListenerBtnHourEdit(int dayNumber) {
            this.dayNumber = dayNumber;
        }

        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("job", job);
            bundle.putInt("day_number", dayNumber);

            Intent intent = new Intent(getContext(), EditHoursActivity.class);
            intent.putExtras(bundle);

            getActivity().startActivityForResult(intent, Constants.RC_EDIT_HOURS);
        }
    }
}