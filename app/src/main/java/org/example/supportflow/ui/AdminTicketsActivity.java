package org.example.supportflow.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import org.example.supportflow.R;
import org.example.supportflow.adapter.AdminTicketAdapter;
import org.example.supportflow.data.TicketRepository;
import org.example.supportflow.model.Ticket;
import java.util.ArrayList;
import java.util.List;

public class AdminTicketsActivity extends AppCompatActivity {

    private AdminTicketAdapter adapterSinAsignar;
    private AdminTicketAdapter adapterAsignados;
    private final TicketRepository repo = new TicketRepository();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_tickets);

        RecyclerView rvSinAsignar = findViewById(R.id.rvSinAsignar);
        rvSinAsignar.setLayoutManager(new LinearLayoutManager(this));
        adapterSinAsignar = new AdminTicketAdapter(new ArrayList<>(), new AdminTicketAdapter.OnTicketClickListener() {
            @Override
            public void onDetalles(Ticket ticket) {
                abrirDetalle(ticket);
            }
            @Override
            public void onAsignar(Ticket ticket) {
                mostrarDialogoAsignar(ticket);
            }
        }, true);
        rvSinAsignar.setAdapter(adapterSinAsignar);

        RecyclerView rvAsignados = findViewById(R.id.rvAsignados);
        rvAsignados.setLayoutManager(new LinearLayoutManager(this));
        adapterAsignados = new AdminTicketAdapter(new ArrayList<>(), new AdminTicketAdapter.OnTicketClickListener() {
            @Override
            public void onDetalles(Ticket ticket) {
                abrirDetalle(ticket);
            }
            @Override
            public void onAsignar(Ticket ticket) { }
        }, false);
        rvAsignados.setAdapter(adapterAsignados);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnFilter).setOnClickListener(v ->
                startActivity(new Intent(AdminTicketsActivity.this, AdminReportesActivity.class))
        );

        cargarTickets();
    }

    private void abrirDetalle(Ticket ticket) {
        Intent i = new Intent(this, AdminDetalleTicketActivity.class);
        i.putExtra(AdminDetalleTicketActivity.EXTRA_TITLE, ticket.getTitle());
        i.putExtra(AdminDetalleTicketActivity.EXTRA_DESCRIPTION, ticket.getDescription());
        i.putExtra(AdminDetalleTicketActivity.EXTRA_CATEGORY, ticket.getCategory());
        i.putExtra(AdminDetalleTicketActivity.EXTRA_PRIORITY, ticket.getPriority());
        i.putExtra(AdminDetalleTicketActivity.EXTRA_STATUS, ticket.getStatus());
        i.putExtra(AdminDetalleTicketActivity.EXTRA_ASSIGNED_TO, ticket.getAssignedTo());
        i.putExtra(AdminDetalleTicketActivity.EXTRA_CREATED_AT, ticket.getCreatedAt());
        startActivity(i);
    }

    private void mostrarDialogoAsignar(Ticket ticket) {
        db.collection("users")
                .whereEqualTo("role", "TECH")
                .get()
                .addOnSuccessListener(snap -> {
                    List<String> nombres = new ArrayList<>();
                    List<String> uids = new ArrayList<>();

                    for (var doc : snap.getDocuments()) {
                        String name = doc.getString("name");
                        if (name != null) {
                            nombres.add(name);
                            uids.add(doc.getId());
                        }
                    }

                    if (nombres.isEmpty()) {
                        Toast.makeText(this, "No hay técnicos disponibles", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Seleccionar Técnico")
                            .setItems(nombres.toArray(new String[0]), (dialog, which) -> {
                                String tecnicoId = uids.get(which);
                                String tecnicoNombre = nombres.get(which);
                                asignarTecnico(ticket, tecnicoId, tecnicoNombre);
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar técnicos", Toast.LENGTH_SHORT).show()
                );
    }

    private void asignarTecnico(Ticket ticket, String tecnicoId, String tecnicoNombre) {
        db.collection("tickets").document(ticket.getId())
                .update("assignedTo", tecnicoId, "status", "IN_PROGRESS")
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Ticket asignado a " + tecnicoNombre, Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al asignar", Toast.LENGTH_SHORT).show()
                );
    }

    private void cargarTickets() {
        repo.escucharTodosLosTickets(new TicketRepository.TicketsCallback() {
            @Override
            public void onSuccess(List<Ticket> tickets) {
                List<Ticket> sinAsignar = new ArrayList<>();
                List<Ticket> asignados = new ArrayList<>();

                for (Ticket t : tickets) {
                    if (t.getAssignedTo() == null || t.getAssignedTo().isEmpty()) {
                        sinAsignar.add(t);
                    } else {
                        asignados.add(t);
                    }
                }

                adapterSinAsignar.setTickets(sinAsignar);
                adapterAsignados.setTickets(asignados);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminTicketsActivity.this, "Error al cargar tickets", Toast.LENGTH_SHORT).show();
            }
        });
    }
}