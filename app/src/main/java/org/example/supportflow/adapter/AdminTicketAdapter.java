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

    public interface OnTicketClickListener {
        void onDetalles(Ticket ticket);
        void onAsignar(Ticket ticket);
    }

    private List<Ticket> tickets;
    private final OnTicketClickListener listener;
    private final boolean mostrarBotonAsignar;

    public AdminTicketAdapter(List<Ticket> tickets, OnTicketClickListener listener, boolean mostrarBotonAsignar) {
        this.tickets = tickets;
        this.listener = listener;
        this.mostrarBotonAsignar = mostrarBotonAsignar;
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

        holder.tvTitulo.setText(ticket.getTitle());
        holder.tvDescripcion.setText(ticket.getDescription());
        holder.tvCategoria.setText("Categoría: " + ticket.getCategory());

        // Formato de fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.tvFecha.setText(sdf.format(new Date(ticket.getCreatedAt())));

        // Mostrar u ocultar botón asignar
        holder.btnAsignar.setVisibility(mostrarBotonAsignar ? View.VISIBLE : View.GONE);

        holder.btnDetalles.setOnClickListener(v -> listener.onDetalles(ticket));
        holder.btnAsignar.setOnClickListener(v -> listener.onAsignar(ticket));
    }

    @Override
    public int getItemCount() {
        return tickets != null ? tickets.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescripcion, tvCategoria, tvFecha;
        Button btnDetalles, btnAsignar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            btnDetalles = itemView.findViewById(R.id.btnDetalles);
            btnAsignar = itemView.findViewById(R.id.btnAsignar);
        }
    }
}