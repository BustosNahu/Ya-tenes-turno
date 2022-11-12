package com.yatenesturno.activities.job_edit.service_configs;

/**
 * Service Configuration validation result class
 */
public class ValidationResult {

    /**
     * Instance variables
     */
    private final int messageResourceId;
    private final boolean result;

    public ValidationResult(boolean result, int messageResourceId) {
        this.result = result;
        this.messageResourceId = messageResourceId;
    }

    public ValidationResult(boolean result) {
        this.result = result;
        this.messageResourceId = -1;
    }

    public int getMessageResourceId() {
        return messageResourceId;
    }

    public boolean getResult() {
        return result;
    }

}
