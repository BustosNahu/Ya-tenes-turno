package com.yatenesturno.object_views;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.LayoutRes;

import com.yatenesturno.R;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

public class ViewAddress extends AbstractFlexibleItem<ViewAddress.ViewHolderAddress> implements IFilterable<String> {

    String address;

    public ViewAddress(String a) {
        this.address = a;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        return ((ViewAddress) o).getAddress().equals(address);
    }

    @LayoutRes
    @Override
    public int getLayoutRes() {
        return R.layout.address_layout;
    }

    @Override
    public ViewHolderAddress createViewHolder(View view, FlexibleAdapter<IFlexible> adapter) {
        return new ViewHolderAddress(adapter, view);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter<IFlexible> adapter, ViewHolderAddress holder, int position, List<Object> payloads) {
        holder.labelAddress.setText(address);
    }

    @Override
    public boolean filter(String constraint) {
        return false;
    }

    public static class ViewHolderAddress extends FlexibleViewHolder {
        public TextView labelAddress;

        public ViewHolderAddress(FlexibleAdapter<IFlexible> adapter, View v) {
            super(v, adapter);
            this.labelAddress = v.findViewById(R.id.labelAddress);
        }
    }
}
