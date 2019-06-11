package com.example.sozoapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sozoapp.R;
import com.example.sozoapp.models.Guardians;

import java.util.ArrayList;

public class GuardiansRecyclerAdapter extends RecyclerView.Adapter<GuardiansRecyclerAdapter.ViewHolder> {

    Context mContext;
    private ArrayList<Guardians> mGuardians;
    private GuardiansRecyclerClickListener mGroupRecyclerClickListener;




    public GuardiansRecyclerAdapter(Context context, ArrayList<Guardians> guardians){
        this.mContext = context;
        this.mGuardians = guardians;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_guardians_list_tem, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener
    {

        TextView guardianName;
        GuardiansRecyclerClickListener clickListener;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            guardianName = itemView.findViewById(R.id.guardians_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {


        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.guardianName.setText(mGuardians.get(position).getGuardianName());
    }

    @Override
    public int getItemCount() {
        return mGuardians.size();
    }

    public interface GuardiansRecyclerClickListener{
        void onGroupSelected(int position);
    }
}
