package com.example.grow;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.grow.Adapters.HabitAdapter;
import com.example.grow.Models.Habit;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.type.DateTime;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private View view;

    private String mParam1;
    private String mParam2;

    public MainFragment() {
    }

    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_main, container, false);
        ((TextView)this.view.findViewById(R.id.today_text))
                .setText("Hello, " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName().toString());

        this.view.findViewById(R.id.add_habit).setOnClickListener(x -> {
            startActivity(new Intent(getActivity(), CreateActivity.class));
        });

        fillGrid(this.view);
        return this.view;
    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        // you can set menu header with title icon etc
//        menu.setHeaderTitle("Choose a color");
//        // add menu items
//        menu.add(0, v.getId(), 0, "Yellow");
//        menu.add(0, v.getId(), 0, "Gray");
//        menu.add(0, v.getId(), 0, "Cyan");
//    }

    public void onResume() {
        super.onResume();
        fillGrid(this.view);
    }

    private void fillGrid(View view){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        List<Habit> habits = new ArrayList<>();
        View loading = view.findViewById(R.id.loading_progress_bar);

        db.collection(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Habit habit = new Habit(
                            document.getId(),
                            (String)document.get("Title"),
                            (String)document.get("Flower"),
                            (String)document.get("DaysResults")
                    );

                    habits.add(habit);
                }

                view.findViewById(R.id.no_habits_text).setVisibility(habits.size() > 0 ? View.GONE : View.VISIBLE);

                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this.getActivity(), 2);

                RecyclerView habitsRecycler = view.findViewById(R.id.habits_layout);
                habitsRecycler.setLayoutManager(layoutManager);

                HabitAdapter adapter = new HabitAdapter(this.getActivity(), habits);
                habitsRecycler.setAdapter(adapter);

                registerForContextMenu(habitsRecycler);

                loading.setVisibility(View.GONE);
            }
        });
    }
}