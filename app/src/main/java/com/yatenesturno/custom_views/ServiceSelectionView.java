package com.yatenesturno.custom_views;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.R;
import com.yatenesturno.object_interfaces.ServiceInstance;
import com.yatenesturno.object_views.ViewSelectableServiceInstance;
import com.yatenesturno.utils.CustomAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;

/**
 * Service Selection View
 * Displays a dialog with a searchable recycler view so the user can select services
 */
public class ServiceSelectionView extends LinearLayoutCompat {

    /**
     * UI References
     */
    private final CardView rootView;
    private final AppCompatTextView tvSelectedServices;
    private RecyclerView recyclerViewServices;
    private Dialog dialog;

    /**
     * Instance variables
     */
    private List<ServiceInstance> serviceInstanceList;
    private String title;
    private ArrayList<ViewSelectableServiceInstance> servicesViewsList;
    private OnSelectedServicesListener listener;
    private CardView searchViewWrapper;
    private FlexibleAdapter<ViewSelectableServiceInstance> adapterServices;
    private View dialogView;
    private List<ServiceInstance> selectedServices;

    public ServiceSelectionView(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);

        rootView = inflateView(context).findViewById(R.id.root);
        tvSelectedServices = rootView.findViewById(R.id.tvSelectedServices);
        title = context.getString(R.string.select_services);
        serviceInstanceList = new ArrayList<>();

        initViews();
    }

    public ServiceSelectionView(@NonNull Context context) {
        this(context, null);
    }

    private View inflateView(@NonNull Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return inflater.inflate(R.layout.service_selection_layout, this, true);
    }

    private void initViews() {
        rootView.setOnClickListener(view -> displayDialog());
    }

    /**
     * Build dialog with services listed
     */
    private void buildDialog() {

        dialogView = LayoutInflater.from(rootView.getContext()).inflate(R.layout.dialog_select_services, null, false);

        recyclerViewServices = dialogView.findViewById(R.id.recyclerViewServices);
        searchViewWrapper = dialogView.findViewById(R.id.wrapperSearchView);

        initRecyclerView();
        initSearchView();

        dialogView.findViewById(R.id.btnConfirm).setOnClickListener(view -> onConfirm());

        dialog = new CustomAlertDialogBuilder(rootView.getContext())
                .setView(dialogView)
                .setTitle(title)
                .setOnDismissListener(dialogInterface -> onConfirm())
                .create();
    }

    private void onConfirm() {
        LoadingButton loadingButton = dialogView.findViewById(R.id.btnConfirm);
        loadingButton.hideLoading();
        dialog.dismiss();
        updateSelectedServicesView(getSelectedServices());
        notifyListener();
    }

    private void notifyListener() {
        if (listener != null) {
            listener.onSelected();
        }
    }

    /**
     * Disable clicking or choosing services
     */
    public void disable() {
        rootView.setForeground(AppCompatResources.getDrawable(getContext(), R.drawable.rounded_background_grey_all));
        rootView.setEnabled(false);
    }

    /**
     * Enable selection
     */
    public void enable() {
        TypedArray a = ((Activity) getContext()).getTheme().obtainStyledAttributes(R.style.AppTheme, new int[]{android.R.attr.selectableItemBackground});
        int attributeResourceId = a.getResourceId(0, 0);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), attributeResourceId, null);
        rootView.setForeground(drawable);
        rootView.setEnabled(true);
    }

    /**
     * Display dialog
     */
    private void displayDialog() {
        if (dialog != null) {
            dialog.show();
        }
    }

    private void initSearchView() {
        int SHOW_SEARCH_VIEW_THRESHOLD = 3;

        if (serviceInstanceList.size() > SHOW_SEARCH_VIEW_THRESHOLD) {
            searchViewWrapper.setVisibility(VISIBLE);
            SearchView searchView = searchViewWrapper.findViewById(R.id.searchViewService);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return onQueryTextChange(query);
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterServices(newText);
                    return false;
                }
            });
        }
    }

    private void filterServices(String newText) {
        adapterServices.setFilter(newText);
        adapterServices.filterItems();

        if (TextUtils.isEmpty(newText)) {
            recyclerViewServices.postDelayed(() -> recyclerViewServices.smoothScrollToPosition(0), 200);
        }
    }

    /**
     * Update view to display selected services
     * If no service was selected, displays a message accordingly
     */
    private void updateSelectedServicesView(List<ServiceInstance> selectedServices) {

        if (selectedServices.size() == 0) {
            tvSelectedServices.setText(rootView.getContext().getString(R.string.select_services));
            tvSelectedServices.setTextColor(rootView.getContext().getColor(R.color.darker_grey));
        } else {
            String servicesString = getServicesString(selectedServices);
            tvSelectedServices.setText(servicesString);
            tvSelectedServices.setTextColor(rootView.getContext().getColor(R.color.black));
        }
    }

    /**
     * Returns a string with the selected services
     *
     * @param servicesList selected services
     * @return string with services name concatenated
     */
    private String getServicesString(List<ServiceInstance> servicesList) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < servicesList.size(); i++) {
            stringBuilder.append(servicesList.get(i).getService().getName());

            if (i < servicesList.size() - 1) {
                stringBuilder.append(", ");
            }
        }

        return stringBuilder.toString();
    }

    public String getServicesString() {
        return this.getServicesString(getSelectedServices());
    }

    /**
     * Populates service instance recycler view with services provided with credits
     */
    private void initRecyclerView() {
        recyclerViewServices.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        servicesViewsList = new ArrayList<>();
        for (ServiceInstance si : serviceInstanceList) {
            ViewSelectableServiceInstance view = new ViewSelectableServiceInstance(si, null);

            if (selectedServices != null) {
                view.setSelected(isPreviouslySelected(si));
            }

            servicesViewsList.add(view);
        }


        this.adapterServices = new FlexibleAdapter<>(servicesViewsList);
        recyclerViewServices.setAdapter(adapterServices);
    }

    private boolean isPreviouslySelected(ServiceInstance serviceInstance) {
        for (ServiceInstance selectedService : selectedServices) {
            if (selectedService.getId().equals(serviceInstance.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loops through service selection recycler view to find selected services
     *
     * @return selected service instances
     */
    public List<ServiceInstance> getSelectedServices() {
        List<ServiceInstance> selected = new ArrayList<>();

        if (servicesViewsList != null) {
            for (ViewSelectableServiceInstance view : servicesViewsList) {
                if (view.isSelected()) {
                    selected.add(view.getServiceInstance());
                }
            }
        }

        return selected;
    }

    /**
     * Sets services that should be initialized as selected
     *
     * @param selectedServices
     */
    public void setSelectedServices(List<ServiceInstance> selectedServices) {
        this.selectedServices = selectedServices;

        buildDialog();
        updateSelectedServicesView(selectedServices);
    }

    public void setServices(@NonNull List<ServiceInstance> serviceInstanceList) {
        this.serviceInstanceList = serviceInstanceList;
        buildDialog();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setListener(OnSelectedServicesListener listener) {
        this.listener = listener;
    }

    public void invalidate() {
        selectedServices = new ArrayList<>();
        serviceInstanceList = new ArrayList<>();
        servicesViewsList = new ArrayList<>();
        updateSelectedServicesView(getSelectedServices());
    }

    public interface OnSelectedServicesListener {
        void onSelected();
    }
}
