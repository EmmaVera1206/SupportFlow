package org.example.supportflow.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_tickets);

        // RecyclerView Sin Asignar
        RecyclerView rvSinAsignar = findViewById(R.id.rvSinAsignar);
        rvSinAsignar.setLayoutManager(new LinearLayoutManager(this));
        adapterSinAsignar = new AdminTicketAdapter(new ArrayList<>(), new AdminTicketAdapter.OnTicketClickListener() {
            @Override
            public void onDetalles(Ticket ticket) {
                Toast.makeText(AdminTicketsActivity.this, "Detalles: " + ticket.getTitle(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onAsignar(Ticket ticket) {
                Toast.makeText(AdminTicketsActivity.this, "Asignar: " + ticket.getTitle(), Toast.LENGTH_SHORT).show();
            }
        }, true);
        rvSinAsignar.setAdapter(adapterSinAsignar);

        // RecyclerView Asignados
        RecyclerView rvAsignados = findViewById(R.id.rvAsignados);
        rvAsignados.setLayoutManager(new LinearLayoutManager(this));
        adapterAsignados = new AdminTicketAdapter(new ArrayList<>(), new AdminTicketAdapter.OnTicketClickListener() {
            @Override
            public void onDetalles(Ticket ticket) {
                Toast.makeText(AdminTicketsActivity.this, "Detalles: " + ticket.getTitle(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onAsignar(Ticket ticket) { }
        }, false);
        rvAsignados.setAdapter(adapterAsignados);

        // Botón regresar
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // ✅ Botón filtro abre pantalla de Reportes
        findViewById(R.id.btnFilter).setOnClickListener(v -> {
            startActivity(new Intent(AdminTicketsActivity.this, AdminReportesActivity.class));
        });

        // Cargar tickets
        cargarTickets();
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