package com.example.sozoapp.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sozoapp.R;
import com.example.sozoapp.adapters.GuardiansRecyclerAdapter;
import com.example.sozoapp.models.Guardians;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class GuardiansFragment extends Fragment implements View.OnClickListener,
        GuardiansRecyclerAdapter.GuardiansRecyclerClickListener {

    private static final String TAG = "GuardiansFragment";

    private ProgressBar mProgressbar;

    private View mGuardiansFragmentView;
    private ArrayList<Guardians> mGuardians = new ArrayList<>();
    private Set<String> mGuardiansIds = new HashSet<>();
    private GuardiansRecyclerAdapter mGuardiansRecyclerAdapter;
    private RecyclerView mGuardiansRecyclerView;
    private ListenerRegistration mGuardiansEventListener;
    private FirebaseFirestore mDb;
    private LinearLayoutManager mLinearLayoutManager;
    private FloatingActionButton mCreateGuardiansButton;

    private Context mContext;

    public GuardiansFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        mGuardiansFragmentView = inflater.inflate(R.layout.fragment_guardians, container, false);

        mProgressbar = mGuardiansFragmentView.findViewById(R.id.progress_bar_guardians);
        mGuardiansRecyclerView = mGuardiansFragmentView.findViewById(R.id.guardians_recycler_view);
        mCreateGuardiansButton =  mGuardiansFragmentView.findViewById(R.id.fab_create_guardians);

        mDb = FirebaseFirestore.getInstance();

        initCreateGuardiansButton();
        initGuardiansRecyclerView();
        displayGuardiansList();



        return mGuardiansFragmentView;
    }

    private void initCreateGuardiansButton() {

        mCreateGuardiansButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                newGuardianDialog();
            }
        });
    }

    private void initGuardiansRecyclerView() {
        mGuardiansRecyclerAdapter = new GuardiansRecyclerAdapter(getContext(), mGuardians);
        mGuardiansRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mGuardiansRecyclerView.setAdapter(mGuardiansRecyclerAdapter);

    }

    private void displayGuardiansList(){

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .build();
        mDb.setFirestoreSettings(settings);

        CollectionReference guardCollectionRef = mDb
                .collection("Guardians");

        mGuardiansEventListener = guardCollectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (e != null){
                    Log.e(TAG, "onEvent: Listen Failed", e );
                }


                if (queryDocumentSnapshots != null){

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots){
                        Guardians guardians = doc.toObject(Guardians.class);
                        if (mGuardiansIds.contains(guardians.getGuardianId())){
                            mGuardiansIds.add(guardians.getGuardianId());
                            mGuardians.add(guardians);
                        }
                    }

                    Log.d(TAG, "onEvent: number of guardians: " + mGuardians.size());
                    mGuardiansRecyclerAdapter.notifyDataSetChanged();
                }
            }
        });
    }


    @Override
    public void onClick(View v) {


    }

    private void newGuardianDialog() {
        AlertDialog.Builder guardianBuilder = new AlertDialog.Builder(getContext());
        guardianBuilder.setTitle("Enter Guardian E-mail: ");

        final EditText guardianEmailField = new EditText(getContext());
        guardianEmailField.setHint("e.g john@gmail.com");
        guardianBuilder.setView(guardianEmailField);

        guardianBuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String groupName = guardianEmailField.getText().toString();

                if (TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(getActivity(), "Plase type in Group Name", Toast.LENGTH_SHORT).show();

                } else
                {



                }
            }
        });

        guardianBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();

            }
        });
    }

    private void searchForGuardian(String guardianEmail) {

        if (guardianEmail.length() == 0){

        } else {

            CollectionReference reference = mDb.collection("Users");
            Query query = mDb.collection("Users")
                    .whereEqualTo(guardianEmail, true);

            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {


                }
            });
        }


    }

    @Override
    public void onGroupSelected(int position) {

    }
}