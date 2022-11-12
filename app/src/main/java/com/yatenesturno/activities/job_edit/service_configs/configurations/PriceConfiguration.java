package com.yatenesturno.activities.job_edit.service_configs.configurations;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.yatenesturno.R;
import com.yatenesturno.activities.job_edit.service_configs.ConfigurationId;
import com.yatenesturno.activities.job_edit.service_configs.ValidationResult;
import com.yatenesturno.activities.job_edit.service_configs.VisitorServiceConfiguration;
import com.yatenesturno.object_interfaces.ServiceInstance;

/**
 * Configure service price
 */
public final class PriceConfiguration extends ServiceConfiguration {

    /**
     * UI References
     */
    private TextInputEditText inputPrice;
    private TextInputLayout tilPrice;

    public PriceConfiguration(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public ConfigurationId getConfigurationId() {
        return ConfigurationId.PRICE;
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.configuration_price;
    }

    public String getPrice() {
        if (inputPrice.getText() == null) {
            return "";
        }
        return inputPrice.getText().toString();
    }

    @Override
    protected void onViewInflated(View view) {
        inputPrice = view.findViewById(R.id.inputPrice);
        tilPrice = view.findViewById(R.id.tilPrice);

        hideKeyBoard();
    }

    @Override
    public void acceptVisitor(VisitorServiceConfiguration visitor) {
        visitor.visit(this);
    }

    @Override
    public void saveState(Bundle bundle) {
        bundle.putString("price", inputPrice.getText().toString());
    }

    @Override
    public void recoverState(Bundle bundle) {
        inputPrice.setText(bundle.getString(getConfigurationId().toString()));
    }

    @Override
    public void initFromServiceInstance(ServiceInstance serviceInstance) {
        inputPrice.setText(String.valueOf(serviceInstance.getPrice()));
    }

    @Override
    public ValidationResult validate() {
        hideKeyBoard();

        if (TextUtils.isEmpty(inputPrice.getText())) {
            return new ValidationResult(false, R.string.req_select_price);
        }

        return new ValidationResult(true);
    }

    @Override
    public void updateUI(Boolean active) {

    }

    private void hideKeyBoard() {
        tilPrice.clearFocus();
        inputPrice.clearFocus();
        InputMethodManager imm = (InputMethodManager) inputPrice.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(inputPrice.getWindowToken(), 0);
    }

    @Override
    public void reset() {
        hideKeyBoard();

        tilPrice.setError(null);
        inputPrice.setText("");
    }
}
