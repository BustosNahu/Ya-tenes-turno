package com.yatenesturno.activities.help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.yatenesturno.R;
import com.yatenesturno.activities.tutorial_screen.Screen;
import com.yatenesturno.activities.tutorial_screen.ScreenImpl;
import com.yatenesturno.activities.tutorial_screen.TutorialScreenImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ViewTutorialsFragment extends Fragment {

    private final List<List<Screen>> screens = Arrays.asList(
            Arrays.asList(
                    new ScreenImpl(R.drawable.inicio0),
                    new ScreenImpl(R.drawable.inicio1),
                    new ScreenImpl(R.drawable.inicio2),
                    new ScreenImpl(R.drawable.inicio3),
                    new ScreenImpl(R.drawable.inicio4),
                    new ScreenImpl(R.drawable.inicio5)
            ),
            Arrays.asList(
                    new ScreenImpl(R.drawable.inicio6),
                    new ScreenImpl(R.drawable.secciones1),
                    new ScreenImpl(R.drawable.secciones2)
            ),
            Arrays.asList(
                    new ScreenImpl(R.drawable.confserv1),
                    new ScreenImpl(R.drawable.confserv2),
                    new ScreenImpl(R.drawable.confserv3),
                    new ScreenImpl(R.drawable.confserv4),
                    new ScreenImpl(R.drawable.confserv5),
                    new ScreenImpl(R.drawable.confserv6)
            ),
            Arrays.asList(
                    new ScreenImpl(R.drawable.empl1),
                    new ScreenImpl(R.drawable.empl2)
            )

    );
    private List<Integer> helpNameList;

    public ViewTutorialsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        helpNameList = new ArrayList<>();

        helpNameList.add(R.string.home_page_tutorial);
        helpNameList.add(R.string.sections_tutorial);
        helpNameList.add(R.string.service_conf_tutorial);
        helpNameList.add(R.string.employees_tutorial);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_tutorials, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();
    }

    private void initViews() {
        ListView listView = getView().findViewById(R.id.listView);

        listView.setAdapter(new AdapterTutorials(helpNameList));
        listView.setOnItemClickListener((parent, view, position, id) -> startTutorial(position));
    }

    private void startTutorial(int position) {
        List<Screen> screenList = screens.get(position);

        TutorialScreenImpl tutorialScreen = new TutorialScreenImpl();
        tutorialScreen.showTutorial((AppCompatActivity) getActivity(), screenList, true);
    }

    public class AdapterTutorials extends BaseAdapter {

        private final List<Integer> tutorialsNameList;

        public AdapterTutorials(List<Integer> list) {
            this.tutorialsNameList = list;
        }

        @Override
        public int getCount() {
            return tutorialsNameList.size();
        }

        @Override
        public Object getItem(int position) {
            return tutorialsNameList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.settings_layout, parent, false);
            }
            ((TextView) convertView.findViewById(R.id.labelSetting)).setText(getString(tutorialsNameList.get(position)));

            return convertView;
        }
    }
}