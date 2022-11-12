package com.yatenesturno.object_views;

import com.yatenesturno.object_interfaces.Service;

public class ServiceObject {
    public Service service;
    public  Boolean isSelected;
    public  Boolean isAnotherSelected;
    public  Boolean isProvided;
    public  Boolean isCredits;
    public  Boolean isEmergency;

    public ServiceObject(Service service, Boolean isSelected, Boolean isProvided, Boolean isCredits, Boolean isAnotherSelected, Boolean isEmergency){
        this.service = service;
        this.isSelected = isSelected;
        this.isAnotherSelected = isAnotherSelected;
        this.isProvided = isProvided;
        this.isCredits = isProvided;
        this.isEmergency = isEmergency;
    }
}
