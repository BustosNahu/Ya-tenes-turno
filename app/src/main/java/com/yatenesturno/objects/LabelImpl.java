package com.yatenesturno.objects;

import com.yatenesturno.object_interfaces.Label;

import java.util.Objects;

public class LabelImpl implements Label {
    private String id, name, color, jobId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LabelImpl)) return false;
        LabelImpl label = (LabelImpl) o;
        return Objects.equals(id, label.id);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public boolean isNotAttendedLabel() {
        return id == null;
    }
}
