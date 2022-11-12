package com.yatenesturno.activities.purchases.single_purchase;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.yatenesturno.R;
import com.yatenesturno.custom_views.LoadingOverlay;

import java.util.List;


public class CreditsPacksFragment extends BottomSheetDialogFragment {

    private OnPurchasePackListener onPurchaseListener;
    private List<CreditPack> creditPackList;
    private RecyclerView recyclerViewCreditPacks;
    private boolean viewCreated = false;
    private LoadingOverlay loadingOverlay;
    private String employeeName;

    public CreditsPacksFragment() {
        new Handler(Looper.myLooper()).postDelayed(this::fetchPacks, 200);
    }

    public void setOnPurchaseListener(OnPurchasePackListener onPurchaseListener) {
        this.onPurchaseListener = onPurchaseListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_credits_packs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewCreated = true;

        initUI();
    }

    private void initUI() {
        recyclerViewCreditPacks = getView().findViewById(R.id.recyclerviewCreditPack);
        loadingOverlay = new LoadingOverlay(getView().findViewById(R.id.root));
        loadingOverlay.show();

        LinearLayoutManager lp = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerViewCreditPacks.setLayoutManager(lp);

        if (creditPackList != null) {
            loadingOverlay.hide();
            fillRecyclerViewCreditPacks();
        }
    }

    @Override
    public void show(@NonNull FragmentManager transaction, String employeeName) {
        this.employeeName = employeeName;

        super.show(transaction, null);
    }

    private void fetchPacks() {
        PacksManager packsManager = new PacksManagerImpl();
        packsManager.fetchPacks(this::onPacksFetched);
    }

    private void onPacksFetched(List<CreditPack> creditPackList) {
        this.creditPackList = creditPackList;

        if (viewCreated) {
            loadingOverlay.hide();
            fillRecyclerViewCreditPacks();
        }
    }

    private void fillRecyclerViewCreditPacks() {
        AdapterCreditPacks adapter = new AdapterCreditPacks(creditPackList);
        recyclerViewCreditPacks.setAdapter(adapter);
        ((TextView) getView().findViewById(R.id.titlePacks)).setText(String.format("Pack de créditos para %s", employeeName));
    }

    public interface OnPurchasePackListener {
        void onPurchase(CreditPack creditPack);
    }

    private class AdapterCreditPacks extends RecyclerView.Adapter<AdapterCreditPacks.ViewHolderCreditPack> {

        private final List<CreditPack> creditPackList;

        public AdapterCreditPacks(List<CreditPack> creditPackList) {
            this.creditPackList = creditPackList;
        }

        @NonNull
        @Override
        public ViewHolderCreditPack onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            ViewHolderCreditPack viewHolder;

            viewHolder = new ViewHolderCreditPack(layoutInflater.inflate(R.layout.credit_pack_list_item, parent, false));

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderCreditPack holder, int position) {
            CreditPack creditPack = creditPackList.get(position);

            if (creditPack.getEmailCredits() > 0) {
                holder.labelEmailCredits.setVisibility(View.VISIBLE);
                holder.labelEmailCredits.setText(creditPack.getEmailCredits() + "");
            } else {
                holder.labelEmailCredits.setVisibility(View.GONE);
            }

            if (creditPack.getWhatsappCredits() > 0) {
                holder.labelWhatsappCredits.setVisibility(View.VISIBLE);
                holder.labelWhatsappCredits.setText(creditPack.getWhatsappCredits() + "");
            } else {
                holder.labelWhatsappCredits.setVisibility(View.GONE);
            }

            holder.labelPrice.setText(String.format("$%s", creditPack.getPrice()));
            holder.btnConfirmPurchase.setOnClickListener(view -> onPurchaseListener.onPurchase(creditPack));
        }


        @Override
        public int getItemCount() {
            return creditPackList.size();
        }

        private class ViewHolderCreditPack extends RecyclerView.ViewHolder {
            TextView labelPrice, labelWhatsappCredits, labelEmailCredits;
            Button btnConfirmPurchase;

            public ViewHolderCreditPack(@NonNull View itemView) {
                super(itemView);

                this.btnConfirmPurchase = itemView.findViewById(R.id.btnConfirmPurchase);
                this.labelPrice = itemView.findViewById(R.id.labelPrice);
                this.labelWhatsappCredits = itemView.findViewById(R.id.labelWhatsappCredits);
                this.labelEmailCredits = itemView.findViewById(R.id.labelEmailCredits);
            }
        }
    }
}