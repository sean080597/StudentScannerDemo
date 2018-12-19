package com.luusean.studentscannerlab.event;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.luusean.studentscannerlab.R;
import com.luusean.studentscannerlab.database.EventObject;
import com.luusean.studentscannerlab.student.Student;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private List<EventObject> listEvents;
    private LayoutInflater inflater;
    private Context context;

    public EventAdapter(Context context, List<EventObject> listEvents) {
        this.listEvents = listEvents;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.lvi_event, parent, false);
        return new EventAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventAdapter.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        EventObject e = listEvents.get(position);
        holder.txtEventName.setText(e.getName());
        holder.txtVenue.setText(R.string.hall_a0820);

        holder.linear_show_event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EventShowActivity.class);
                intent.putExtra("event_id", listEvents.get(position).getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listEvents.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtEventName;
        TextView txtVenue;
        LinearLayout linear_show_event;
        ViewHolder(View itemView) {
            super(itemView);
            txtEventName = itemView.findViewById(R.id.txt_event_name);
            txtVenue = itemView.findViewById(R.id.txt_venue);
            linear_show_event = itemView.findViewById(R.id.linear_show_event);
        }
    }
}
