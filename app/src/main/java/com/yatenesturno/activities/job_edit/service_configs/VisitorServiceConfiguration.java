package com.yatenesturno.activities.job_edit.service_configs;

import com.yatenesturno.activities.job_edit.service_configs.configurations.BookingConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.ClassConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.ConcurrencyConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.ConcurrentServicesConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.CreditsConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.DaysAndDurationConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.EmergencyConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.FixedScheduleConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.IntervalConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.PriceConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.ReminderConfiguration;
import com.yatenesturno.activities.job_edit.service_configs.configurations.TimeSlotConfiguration;
import com.yatenesturno.activities.services.step3.objects.BasicServiceInfoConfigurator;
import com.yatenesturno.activities.services.step3.objects.ClientPermissions;
import com.yatenesturno.activities.services.step3.objects.SimultShiftsConfigurator;

/**
 * Visitor for service configuration visitor pattern
 */
public abstract class VisitorServiceConfiguration {

    public void visit(ClassConfiguration serviceConfiguration) {
    }

    public void visit(ConcurrencyConfiguration serviceConfiguration) {
    }

    public void visit(ConcurrentServicesConfiguration serviceConfiguration) {
    }

    public void visit(CreditsConfiguration serviceConfiguration) {
    }

    public void visit(DaysAndDurationConfiguration serviceConfiguration) {
    }

    public void visit(EmergencyConfiguration serviceConfiguration) {
    }

    public void visit(FixedScheduleConfiguration serviceConfiguration) {
    }

    public void visit(IntervalConfiguration serviceConfiguration) {
    }

    public void visit(PriceConfiguration serviceConfiguration) {
    }
    public void visit(BasicServiceInfoConfigurator serviceConfiguration) {
    }
    public void visit(ClientPermissions serviceConfiguration) {
    }
    public void visit(SimultShiftsConfigurator serviceConfiguration) {
    }
    public void visit(ReminderConfiguration serviceConfiguration) {
    }

    public void visit(TimeSlotConfiguration serviceConfiguration) {
    }

    public void visit(BookingConfiguration bookingConfiguration) {
    }
}
