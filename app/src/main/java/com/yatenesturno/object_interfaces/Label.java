package com.yatenesturno.object_interfaces;

import java.io.Serializable;

public interface Label extends Serializable {
    String getId();

    void setId(String id);

    String getName();

    void setName(String name);

    String getColor();

    void setColor(String color);

    String getJobId();

    void setJobId(String jobId);

    boolean equals(Object o);

    boolean isNotAttendedLabel();
}
