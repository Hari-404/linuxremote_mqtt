package com.example.linuxremote;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MyRecycler extends RecyclerView.Adapter<MyRecycler.ViewHolder> {

    private Context context;
    private SendData sendData;
    private List<String> names;
    private List<String> ip;

    public MyRecycler(Context context, List<String> mNames, List<String> ip) {
        this.names = new ArrayList<>();
        this.ip = new ArrayList<>();
        this.context = context;
        this.names = mNames;
        this.ip = ip;
        sendData = (SendData) context;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_list, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        viewHolder.name.setText(names.get(i));
        viewHolder.ip.setText(ip.get(i));
        viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData.connectToService(viewHolder.ip.getText().toString());
            }
        });

    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout linearLayout;
        TextView name, ip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.recycler_linearLayout);
            name = itemView.findViewById(R.id.recycler_machineName);
            ip = itemView.findViewById(R.id.recycler_machine_ip);
        }
    }

}
