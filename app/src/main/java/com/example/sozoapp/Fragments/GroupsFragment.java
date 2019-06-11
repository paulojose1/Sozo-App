package com.example.sozoapp.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sozoapp.R;
import com.example.sozoapp.adapters.GroupRecyclerAdapter;
import com.example.sozoapp.models.CreateUser;
import com.example.sozoapp.models.Groups;
import com.example.sozoapp.models.UserLocation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class GroupsFragment extends Fragment implements View.OnClickListener,
        GroupRecyclerAdapter.GroupRecyclerClickListener {



    private static final String TAG = "GroupsFragment";

    private ProgressBar mProgressBar;

    private View mGroupFragmentView;
    private ArrayList<Groups> mGroups = new ArrayList<>();
    private Set<String> mGroupIds = new HashSet<>();
    private GroupRecyclerAdapter mGroupRecyclerAdapter;
    private RecyclerView mGroupRecyclerView;
    private ListenerRegistration mGroupEventListener;
    private FirebaseFirestore mDb;
    private LinearLayoutManager mLinearLayoutManager;
    private FloatingActionButton mCreateGroupButton;
    private ArrayList<CreateUser> mUserList = new ArrayList<>();
    private ArrayList<UserLocation> mUserLocations = new ArrayList<>();

    private  Context mContext;

    public GroupsFragment(){

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


        mGroupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);

        mProgressBar = mGroupFragmentView.findViewById(R.id.progressBar);
        mGroupRecyclerView = mGroupFragmentView.findViewById(R.id.group_recycler_view);
        mCreateGroupButton = mGroupFragmentView.findViewById(R.id.fab_create_group);

        mDb = FirebaseFirestore.getInstance();


        initCreateGroupButton();
        initGroupRecyclerView();
        displayGorupList();


        return mGroupFragmentView;
    }

    private void initCreateGroupButton(){

        mCreateGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createNewGroupDialog();
            }
        });

    }

    private void initGroupRecyclerView(){
        mGroupRecyclerAdapter = new GroupRecyclerAdapter(getContext(), mGroups);
        mGroupRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mGroupRecyclerView.setAdapter(mGroupRecyclerAdapter);
    }

    private void displayGorupList() {

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .build();

        mDb.setFirestoreSettings(settings);


        CollectionReference groupCollectionsRef = mDb
                .collection("Groups");

        mGroupEventListener = groupCollectionsRef.addSnapshotListener(new EventListener<QuerySnapshot>()
        {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (e != null){
                    Log.e(TAG, "onEvent: Listen Failed", e);
                }

                if (queryDocumentSnapshots != null){

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots){

                        Groups groups = doc.toObject(Groups.class);
                        if (!mGroupIds.contains(groups.getGroupId())){
                            mGroupIds.add(groups.getGroupId());
                            mGroups.add(groups);
                        }
                    }
                    Log.d(TAG, "onEvent: number of groups: " + mGroups.size());
                    mGroupRecyclerAdapter.notifyDataSetChanged();
                }
            }
        });




    }

    private void createNewGroupDialog()
    {
        AlertDialog.Builder groupBuilder = new AlertDialog.Builder(getContext());
        groupBuilder.setTitle("Enter Group Name: ");

        final EditText groupNameField = new EditText(getContext());
        groupNameField.setHint("e.g Family");
        groupBuilder.setView(groupNameField);

        groupBuilder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(getActivity(), "Plase type in Group Name", Toast.LENGTH_SHORT).show();

                } else
                {

                    createGroup(groupName);

                }
            }
        });

        groupBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();

            }
        });

        groupBuilder.show();



    }

    private void createGroup(String groupName)
    {
        final Groups groups = new Groups();

        groups.setTitle(groupName);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .build();
        mDb.setFirestoreSettings(settings);

        DocumentReference newGroupRef = mDb
                .collection("Groups")
                .document();

        groups.setGroupId(newGroupRef.getId());

        newGroupRef.set(groups).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    Toast.makeText(getContext(), groupName + " Created successfully!", Toast.LENGTH_SHORT).show();
                }
                else {
                    mGroupFragmentView.findViewById(android.R.id.content);
                    Snackbar.make(mGroupFragmentView, "Something Went Wrong.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void inflateGroupUserListFragment() {

        GroupUserListFragment fragment = GroupUserListFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(getString(R.string.intent_user_list), mUserList);
        bundle.putParcelableArrayList(getString(R.string.intent_user_locations), mUserLocations);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up);
        transaction.replace(R.id.user_list_container, fragment, getString(R.string.fragment_user_list));
        transaction.addToBackStack(getString(R.string.fragment_user_list));
        transaction.commit();
    }


    @Override
    public void onClick(View v) {




    }

    @Override
    public void onGroupSelected(int position) {


        navGroupFragment(mGroups.get(position));

    }

    private void navGroupFragment(Groups groups) {


        Intent intent = new Intent(mContext, GroupUserListFragment.class);
        intent.putExtra("intent group", groups);
        startActivity(intent);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case android.R.id.home:{
                GroupUserListFragment fragment =
                        (GroupUserListFragment) getFragmentManager().findFragmentByTag(getString(R.string.fragment_user_list));
                if(fragment != null){
                    if(fragment.isVisible()){
                        getFragmentManager().popBackStack();
                        return true;
                    }
                }
                //finish();
                return true;
            }
            case R.id.action_settings:{
                inflateGroupUserListFragment();
                return true;
            }

            default:{
                return super.onOptionsItemSelected(item);
            }
        }

    }


}
