package com.luusean.studentscannerlab;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.luusean.studentscannerlab.database.EventObject;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private List<EventObject> listEvents;
    private LayoutInflater inflater;

    public EventAdapter(Context context, List<EventObject> listEvents) {
        this.listEvents = listEvents;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.lvi_event, parent, false);
        return new EventAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventAdapter.ViewHolder holder, int position) {
        EventObject e = listEvents.get(position);
        holder.txtEventName.setText(e.getName());
        holder.txtVenue.setText("Hội trường A.08.20");
    }

    @Override
    public int getItemCount() {
        return listEvents.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtEventName;
        TextView txtVenue;
        ViewHolder(View itemView) {
            super(itemView);
            txtEventName = itemView.findViewById(R.id.txt_event_name);
            txtVenue = itemView.findViewById(R.id.txt_venue);
        }
    }
}
