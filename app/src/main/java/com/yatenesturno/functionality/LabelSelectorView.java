package com.yatenesturno.functionality;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yatenesturno.R;
import com.yatenesturno.custom_views.LabelHandlerImpl;
import com.yatenesturno.object_interfaces.Appointment;
import com.yatenesturno.object_interfaces.Label;
import com.yatenesturno.object_views.ViewLabel;
import com.yatenesturno.objects.LabelImpl;
import com.yatenesturno.utils.CustomAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import dev.sasikanth.colorsheet.ColorSheet;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class LabelSelectorView {

    private final String jobId;
    private final String placeId;
    private final int[] COLORS = new int[]{
            Color.parseColor("#9c9c9c"),
            Color.parseColor("#d6cf00"),
            Color.parseColor("#00d620"),
            Color.parseColor("#00d6cb"),
            Color.parseColor("#006fd6"),
            Color.parseColor("#8800d6"),
            Color.parseColor("#d600bd"),
            Color.parseColor("#d60000"),
    };
    private final Appointment appointment;
    private ViewGroup root;
    private OnLabelClickListener onLabelSelectedListener;
    private RecyclerView recyclerViewLabels;
    private AppCompatEditText etNewLabel;
    private CustomAlertDialogBuilder dialog;
    private TextView tvNoLabels;
    private int selectedColor;

    public LabelSelectorView(String placeId, String jobId, Appointment app) {
        this.placeId = placeId;
        this.jobId = jobId;
        this.appointment = app;
    }

    public static View buildView(Context context, Appointment appointment) {
        CardView cardViewLabel = (CardView) LayoutInflater.from(context).inflate(R.layout.label, null, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        cardViewLabel.setLayoutParams(layoutParams);

        fillView(cardViewLabel, appointment);

        return cardViewLabel;
    }

    public static void fillView(CardView cardViewLabel, Label label, boolean isPast) {
        TextView tvLabel = cardViewLabel.findViewById(R.id.tvLabel);
        ImageView ivLabel = cardViewLabel.findViewById(R.id.ivLabel);

        String labelName = getDefaultName(cardViewLabel, isPast);
        int labelColor = getDefaultColor(cardViewLabel);

        if (label != null) {
            labelColor = Color.parseColor(label.getColor());
            labelName = label.getName();
        }

        cardViewLabel.setCardBackgroundColor(labelColor);

        int ivLabelColor = getDarkerColor(labelColor);
        ivLabel.setImageDrawable(new ColorDrawable(ivLabelColor));

        if (tvLabel != null) {
            tvLabel.setText(labelName);
            if (isLight(labelColor)) {
                tvLabel.setTextColor(cardViewLabel.getContext().getColor(R.color.black));
            }
        }
    }

    public static int getDefaultColor(CardView cardViewLabel) {
        return cardViewLabel.getContext().getColor(R.color.colorPrimary);
    }

    public static void fillView(CardView cardViewLabel, Appointment app) {
        Label label = app.getLabel();
        if (!app.didAttend()) {
            label = buildAttendedLabel(cardViewLabel.getContext());
        }

        fillView(cardViewLabel, label, isPastAppointment(app));
    }

    private static int getDarkerColor(int color) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * 0.7f);
        int g = Math.round(Color.green(color) * 0.7f);
        int b = Math.round(Color.blue(color) * 0.7f);
        return Color.argb(a,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255));
    }

    public static boolean isLight(int color) {
        return ColorUtils.calculateLuminance(color) > 0.5;
    }

    private static Label buildAttendedLabel(Context context) {
        Label out = new LabelImpl();

        out.setName(context.getString(R.string.label_not_attended));
        out.setColor("#c9c9c9");

        return out;
    }

    private static boolean isPastAppointment(Appointment app) {
        Calendar todayCalendar = Calendar.getInstance();
        return todayCalendar.compareTo(app.getTimeStampStart()) >= 0;
    }

    public static String getDefaultName(CardView cardViewLabel, boolean isPast) {
        if (isPast) {
            return cardViewLabel.getContext().getString(R.string.default_label_text_past);
        } else {
            return cardViewLabel.getContext().getString(R.string.default_label_text);
        }
    }

    public void showChangeLabelDialog(final Context context) {
        root = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.label_selection, null, false);

        String title = context.getString(R.string.select_label);
        recyclerViewLabels = root.findViewById(R.id.recyclerViewLabels);
        etNewLabel = root.findViewById(R.id.etNewLabel);
        tvNoLabels = root.findViewById(R.id.tvNoLabels);

        RecyclerView.LayoutManager layoutManagerServices = new LinearLayoutManager(context);
        recyclerViewLabels.setLayoutManager(layoutManagerServices);

        dialog = new CustomAlertDialogBuilder(context)
                .setView(root)
                .setTitle(title);

        fetchLabels();
        initNewLabel();

        dialog.show();
    }

    public void initNewLabel() {
        AppCompatImageButton btnSave = root.findViewById(R.id.btnSave);
        AppCompatImageButton btnPickColor = root.findViewById(R.id.btnPickColor);
        AppCompatButton btnRemoveLabel = root.findViewById(R.id.btnRemoveLabel);

        Random random = new Random();
        setSelectedColor(COLORS[random.nextInt(COLORS.length)]);

        btnSave.setOnClickListener(v -> handleNewLabelClick());
        btnPickColor.setOnClickListener(view -> showColorPicker());
        btnRemoveLabel.setOnClickListener(view -> removeLabel());
    }

    private void removeLabel() {
        onLabelSelectedListener.onLabelSelected(null);
    }

    private void showColorPicker() {
        final Function1 listener = (new Function1() {
            public Object invoke(Object var1) {
                this.invoke(((Number) var1).intValue());
                return Unit.INSTANCE;
            }

            public final void invoke(int color) {
                setSelectedColor(color);
            }
        });

        new ColorSheet().colorPicker(
                COLORS,
                selectedColor,
                false,
                listener

        ).show(((AppCompatActivity) root.getContext()).getSupportFragmentManager());
    }

    private void setSelectedColor(int color) {
        selectedColor = color;
        ImageView ivSelectedColor = root.findViewById(R.id.ivSelectedColor);
        ivSelectedColor.setImageDrawable(new ColorDrawable(color));
    }

    private void fetchLabels() {
        LabelHandlerImpl.getInstance().getLabels(placeId, jobId, new LabelHandler.OnGetLabelsListener() {
            @Override
            public void onSuccess(List<Label> result) {
                fillLabels(result);
            }

            @Override
            public void onFailure() {
                Toast.makeText(root.getContext(), root.getContext().getString(R.string.error_Fetching_labels), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillLabels(List<Label> result) {
        final List<ViewLabel> viewLabels = new ArrayList<>();

        if (appointment != null && isPastAppointment(appointment)) {
            viewLabels.add(new ViewLabel(buildAttendedLabel(root.getContext()), onLabelSelectedListener));
        } else if (result.size() == 0) {
            displayNoLabels();
            return;
        }

        root.findViewById(R.id.btnRemoveLabel).setVisibility(View.VISIBLE);
        tvNoLabels.setVisibility(View.GONE);
        recyclerViewLabels.setVisibility(View.VISIBLE);

        for (Label label : result) {
            viewLabels.add(new ViewLabel(
                    label,
                    onLabelSelectedListener));
        }

        Handler mainHandler = new Handler(root.getContext().getMainLooper());

        Runnable myRunnable = () -> {
            FlexibleAdapter<ViewLabel> adapter = new FlexibleAdapter<>(viewLabels);
            recyclerViewLabels.setAdapter(adapter);
        };
        mainHandler.post(myRunnable);
    }

    private void createNewLabel(String text, String color, LabelHandler.OnCreateLabelListener listener) {
        LabelHandlerImpl.getInstance().createLabel(placeId, jobId, text, color, listener);
    }

    private void handleNewLabelClick() {
        if (!TextUtils.isEmpty(etNewLabel.getText())) {
            createNewLabel(
                    etNewLabel.getText().toString(),
                    String.format("#%06X", (0xFFFFFF & selectedColor)),
                    new LabelHandler.OnCreateLabelListener() {
                        @Override
                        public void onSuccess(Label label) {
                            fetchLabels();
                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(root.getContext(), root.getContext().getString(R.string.error_creating_label), Toast.LENGTH_SHORT).show();
                        }
                    });

            etNewLabel.setText("");
        } else {
            Toast.makeText(root.getContext(), root.getContext().getString(R.string.input_label_name), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteLabel(Label label) {
        LabelHandlerImpl.getInstance().deleteLabel(placeId, jobId, label.getId(), new LabelHandler.LabelHandlerListener() {
            @Override
            public void onSuccess() {
                fetchLabels();
            }

            @Override
            public void onFailure() {
                Toast.makeText(root.getContext(), root.getContext().getString(R.string.error_deleting_label), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayNoLabels() {
        tvNoLabels.setVisibility(View.VISIBLE);
        recyclerViewLabels.setVisibility(View.INVISIBLE);
        root.findViewById(R.id.btnRemoveLabel).setVisibility(View.INVISIBLE);
    }

    public void setOnLabelSelectedListener(final OnLabelClickListener onLabelSelectedListener) {

        this.onLabelSelectedListener = new OnLabelClickListener() {
            @Override
            public void onLabelSelected(Label label) {
                dialog.dismiss();
                if (onLabelSelectedListener != null) {
                    onLabelSelectedListener.onLabelSelected(label);
                }
            }

            @Override
            public void onDelete(Label label) {
                deleteLabel(label);
                if (onLabelSelectedListener != null) {
                    onLabelSelectedListener.onDelete(label);
                }
            }
        };
    }

    public interface OnLabelClickListener {
        void onLabelSelected(Label label);

        void onDelete(Label label);
    }
}
