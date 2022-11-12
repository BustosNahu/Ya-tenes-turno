package com.yatenesturno.activities.job_edit.service_configs;

public interface ObservableServiceConfiguration {


    void attach(ObserverServiceConfiguration observer);

    void detach(ObserverServiceConfiguration observer);

    void notifyObservers();



}
