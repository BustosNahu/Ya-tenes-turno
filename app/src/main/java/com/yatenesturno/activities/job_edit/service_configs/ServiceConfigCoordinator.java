package com.yatenesturno.activities.job_edit.service_configs;

import com.yatenesturno.activities.job_edit.service_configs.configurations.ServiceConfiguration;
import com.yatenesturno.activities.services.step3.objects.ServiceConfigurationKt;

import java.util.List;

public interface ServiceConfigCoordinator {

    List<ServiceConfiguration> getServiceConfigurationList();

}

