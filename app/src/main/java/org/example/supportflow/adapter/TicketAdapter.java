package org.example.supportflow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.example.supportflow.R;
import org.example.supportflow.model.Ticket;


import java.util.ArrayList;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.VH> {

    public interface OnTicketClickListener {
        void onClick(Ticket ticket);
    }
    private final List<Ticket> data = new ArrayList<>();
    private final OnTicketClickListener listener;

    public TicketAdapter(OnTicketClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<Ticket> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Ticket t = data.get(position);
        h.tvTitle.setText(t.getTitle());
        h.tvCategory.setText("Categoría: " + t.getCategory());
        h.tvStatus.setText(t.getStatus());
        h.tvPriority.setText(t.getPriority());

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(t);
        });

    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvStatus, tvPriority;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPriority = itemView.findViewById(R.id.tvPriority);
        }
    }
}