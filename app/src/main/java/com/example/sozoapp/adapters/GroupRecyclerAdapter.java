package com.example.sozoapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sozoapp.R;
import com.example.sozoapp.models.Groups;

import java.util.ArrayList;

public class GroupRecyclerAdapter extends RecyclerView.Adapter<GroupRecyclerAdapter.ViewHolder> {

    Context mContext;
    private ArrayList<Groups> mGroups;
    private GroupRecyclerClickListener mGroupRecyclerClickListener;




    public GroupRecyclerAdapter(Context context, ArrayList<Groups> groups){
        this.mContext = context;
        this.mGroups = groups;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_group_list_item, parent, false);
         ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener
    {

        TextView groupTitle;
        GroupRecyclerClickListener clickListener;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupTitle = itemView.findViewById(R.id.group_title);


        }

        @Override
        public void onClick(View v) {



        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.groupTitle.setText(mGroups.get(position).getTitle());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {




            }
        });
    }

    private void navGroupFragment(Groups groups) {





    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }

    public interface GroupRecyclerClickListener{
         void onGroupSelected(int position);
    }
}
