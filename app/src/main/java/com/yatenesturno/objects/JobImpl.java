package com.yatenesturno.objects;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.yatenesturno.object_interfaces.CustomUser;
import com.yatenesturno.object_interfaces.DaySchedule;
import com.yatenesturno.object_interfaces.Job;
import com.yatenesturno.object_interfaces.ServiceInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class JobImpl implements Job {
    public static final Parcelable.Creator<JobImpl> CREATOR = new Parcelable.Creator<JobImpl>() {

        public JobImpl createFromParcel(Parcel in) {
            return new JobImpl(in);
        }

        public JobImpl[] newArray(int size) {
            return new JobImpl[size];
        }
    };

    private final CustomUser serviceProvider;
    private final String id;
    private List<DaySchedule> dayScheduleList;
    private boolean editMode, chatMode;
    private boolean userCancellableApps;
    private int maxAppsPerDay;
    private int maxAppsSimultaneusly;

    public JobImpl(String id,
                   CustomUser serviceProvider,
                   List<DaySchedule> daySchedules,
                   boolean editMode,
                   boolean chatMode) {
        this.dayScheduleList = daySchedules;
        this.serviceProvider = serviceProvider;
        this.id = id;
        this.editMode = editMode;
        this.chatMode = chatMode;
        maxAppsPerDay = -1;
        maxAppsSimultaneusly = -1;
    }

    public JobImpl(Parcel in) {
        serviceProvider = in.readParcelable(getClass().getClassLoader());
        id = in.readString();
        dayScheduleList = (List<DaySchedule>) in.readValue(DaySchedule.class.getClassLoader());
        editMode = in.readInt() == 1;
        chatMode = in.readInt() == 1;
        userCancellableApps = in.readInt() == 1;
        maxAppsPerDay = in.readInt();
        maxAppsSimultaneusly = in.readInt();
    }

    @Override
    public int getMaxAppsPerDay() {
        return maxAppsPerDay;
    }

    @Override
    public void setMaxAppsPerDay(int maxAppsPerDay) {
        this.maxAppsPerDay = maxAppsPerDay;
    }

    @Override
    public int getMaxAppsSimultaneusly() {
        return maxAppsSimultaneusly;
    }

    @Override
    public void setMaxAppsSimultaneusly(int maxAppsSimultaneusly) {
        this.maxAppsSimultaneusly = maxAppsSimultaneusly;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(serviceProvider, flags);
        dest.writeString(id);
        dest.writeValue(dayScheduleList);
        dest.writeInt(editMode ? 1 : 0);
        dest.writeInt(chatMode ? 1 : 0);
        dest.writeInt(userCancellableApps ? 1 : 0);
        dest.writeInt(maxAppsPerDay);
        dest.writeInt(maxAppsSimultaneusly);
    }

    @Override
    public CustomUser getEmployee() {
        return serviceProvider;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<DaySchedule> getDaySchedules() {
        return dayScheduleList;
    }

    @Override
    public void setDaySchedules(List<DaySchedule> daySchedules) {
        this.dayScheduleList = daySchedules;
    }

    @Override
    public boolean canEdit() {
        return editMode;
    }

    @Override
    public void setCanEdit(boolean editMode) {
        this.editMode = editMode;
    }

    @Override
    public boolean canChat() {
        return chatMode;
    }

    @Override
    public void setCanChat(boolean chatMode) {
        this.chatMode = chatMode;
    }

    @Override
    public ServiceInstance getServiceInstanceForServiceId(String serviceId) {
        // NULL HERE
//        Log.d("serviceProblem ID:", serviceId);

        if (dayScheduleList != null){
            Log.d("serviceProblem dayScheduleList:",dayScheduleList.toString());
            for (DaySchedule ds : dayScheduleList) {
                for (ServiceInstance si : ds.getServiceInstances()) {
                    if (si.getService().getId().equals(serviceId)) {
                        return si;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public DaySchedule getDaySchedule(int dayOfWeek) {
        if (dayScheduleList != null) {
            for (DaySchedule ds : dayScheduleList) {
                if (ds.getDayOfWeek() == dayOfWeek) {
                    return ds;
                }
            }
        }

        return null;
    }

    @Override
    public void addDaySchedule(DaySchedule daySchedule) {
        this.dayScheduleList.add(daySchedule);
    }

    @Override
    public Job clone() {
        Job out = new JobImpl(id, serviceProvider, null, editMode, chatMode);
        out.setDaySchedules(new ArrayList<DaySchedule>());
        for (DaySchedule ds : dayScheduleList) {
            out.addDaySchedule(ds.clone());
        }
        out.setCanChat(chatMode);
        out.setCanEdit(editMode);
        out.setUserCanCancelApps(userCancellableApps);
        return out;
    }

    @Override
    public boolean canUserCancelApps() {
        return this.userCancellableApps;
    }

    @Override
    public void setUserCanCancelApps(boolean canCancel) {
        this.userCancellableApps = canCancel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobImpl that = (JobImpl) o;

        if (!Objects.equals(id, that.id)) {
            return false;
        }

        if (!Objects.equals(userCancellableApps, that.userCancellableApps)) {
            return false;
        }

        if (getDaySchedules() == null && that.getDaySchedules() != null ||
                getDaySchedules() != null && that.getDaySchedules() == null) {
            return false;
        }
        if (getDaySchedules() != null && getDaySchedules().size() != that.getDaySchedules().size())
            return false;

        boolean foundEqualDaySchedule;

        for (DaySchedule ds1 : dayScheduleList) {
            foundEqualDaySchedule = false;

            for (DaySchedule ds2 : that.getDaySchedules()) {
                if (ds1.equals(ds2)) {
                    foundEqualDaySchedule = true;
                    break;
                }
            }

            if (!foundEqualDaySchedule) {
                return false;
            }
        }

        return true;
    }
}