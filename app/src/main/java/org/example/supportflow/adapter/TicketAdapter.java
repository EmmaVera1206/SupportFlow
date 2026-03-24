package org.example.supportflow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.example.supportflow.R;
import org.example.supportflow.model.Ticket;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        h.tvTitle.setText(valorSeguro(t.getTitle()));
        h.tvDescription.setText(valorSeguro(t.getDescription()));

        h.tvCategory.setText("Categoría:");
        h.tvCategoryValue.setText(valorSeguro(t.getCategory()));

        h.tvStatus.setText(formatearEstado(t.getStatus()));
        h.tvPriority.setText(formatearPrioridad(t.getPriority()));

        if (t.getCreatedAt() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            h.tvDate.setText(sdf.format(new Date(t.getCreatedAt())));
        } else {
            h.tvDate.setText("--/--/----");
        }

        aplicarEstiloStatus(h.tvStatus, t.getStatus());
        aplicarEstiloPrioridad(h.tvPriority, t.getPriority());

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(t);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private String valorSeguro(String value) {
        return (value == null || value.trim().isEmpty()) ? "-" : value;
    }

    private String formatearEstado(String status) {
        if (status == null) return "Estado";
        switch (status) {
            case "OPEN":
                return "Abierto";
            case "IN_PROGRESS":
                return "En proceso";
            case "RESOLVED":
                return "Resuelto";
            default:
                return status;
        }
    }

    private String formatearPrioridad(String priority) {
        if (priority == null) return "Prioridad";
        switch (priority) {
            case "LOW":
                return "Baja";
            case "MEDIUM":
                return "Media";
            case "HIGH":
                return "Alta";
            case "CRITICAL":
                return "Crítica";
            default:
                return priority;
        }
    }

    private void aplicarEstiloStatus(TextView tv, String status) {
        if (status == null) {
            tv.setBackgroundColor(0xFF7C6DFF);
            return;
        }

        switch (status) {
            case "OPEN":
                tv.setBackgroundColor(0xFF7C6DFF);
                break;
            case "IN_PROGRESS":
                tv.setBackgroundColor(0xFF4CAF50);
                break;
            case "RESOLVED":
                tv.setBackgroundColor(0xFFFF9800);
                break;
            default:
                tv.setBackgroundColor(0xFF7C6DFF);
                break;
        }
    }

    private void aplicarEstiloPrioridad(TextView tv, String priority) {
        if (priority == null) {
            tv.setBackgroundColor(0xFFA4D96C);
            return;
        }

        switch (priority) {
            case "LOW":
                tv.setBackgroundColor(0xFFBDBDBD);
                break;
            case "MEDIUM":
                tv.setBackgroundColor(0xFF9FA8DA);
                break;
            case "HIGH":
                tv.setBackgroundColor(0xFFE6D34C);
                break;
            case "CRITICAL":
                tv.setBackgroundColor(0xFFE53935);
                break;
            default:
                tv.setBackgroundColor(0xFFA4D96C);
                break;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvCategory, tvCategoryValue, tvStatus, tvPriority, tvDate;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvCategoryValue = itemView.findViewById(R.id.tvCategoryValue);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPriority = itemView.findViewById(R.id.tvPriority);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}