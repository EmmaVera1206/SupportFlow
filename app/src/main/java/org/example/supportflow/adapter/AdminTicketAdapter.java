package org.example.supportflow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.example.supportflow.R;
import org.example.supportflow.model.Ticket;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminTicketAdapter extends RecyclerView.Adapter<AdminTicketAdapter.ViewHolder> {

    public interface OnTicketActionListener {
        void onDetalles(Ticket ticket);
        void onAsignarOCambiar(Ticket ticket);
        void onEliminar(Ticket ticket);
    }

    private List<Ticket> tickets;
    private final OnTicketActionListener listener;

    public AdminTicketAdapter(List<Ticket> tickets, OnTicketActionListener listener) {
        this.tickets = tickets;
        this.listener = listener;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ticket ticket = tickets.get(position);

        holder.tvTitulo.setText(valorSeguro(ticket.getTitle()));
        holder.tvDescripcion.setText(valorSeguro(ticket.getDescription()));
        holder.tvCategoria.setText("Categoría: " + valorSeguro(ticket.getCategory()));
        holder.tvPrioridad.setText("Prioridad: " + valorSeguro(ticket.getPriority()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvFecha.setText(sdf.format(new Date(ticket.getCreatedAt())));

        if (ticket.getAssignedTo() == null || ticket.getAssignedTo().isEmpty()) {
            holder.btnAsignar.setText("Asignar Técnico");
        } else {
            holder.btnAsignar.setText("Cambiar Técnico");
        }

        holder.btnDetalles.setOnClickListener(v -> listener.onDetalles(ticket));
        holder.btnAsignar.setOnClickListener(v -> listener.onAsignarOCambiar(ticket));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminar(ticket));
    }

    @Override
    public int getItemCount() {
        return tickets != null ? tickets.size() : 0;
    }

    private String valorSeguro(String value) {
        return (value == null || value.trim().isEmpty()) ? "-" : value;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescripcion, tvCategoria, tvPrioridad, tvFecha;
        Button btnDetalles, btnAsignar, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvPrioridad = itemView.findViewById(R.id.tvPrioridad);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            btnDetalles = itemView.findViewById(R.id.btnDetalles);
            btnAsignar = itemView.findViewById(R.id.btnAsignar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}