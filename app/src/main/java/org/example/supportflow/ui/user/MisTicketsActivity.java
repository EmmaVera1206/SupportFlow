package org.example.supportflow.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import org.example.supportflow.R;
import org.example.supportflow.adapter.TicketAdapter;
import org.example.supportflow.data.TicketRepository;
import org.example.supportflow.model.Ticket;
import org.example.supportflow.ui.auth.LoginActivity;

import java.util.ArrayList;
import java.util.List;

public class MisTicketsActivity extends AppCompatActivity {

    private final TicketRepository repo = new TicketRepository();
    private TicketAdapter adapter;
    private ListenerRegistration ticketsListener;

    private final List<Ticket> todosLosTickets = new ArrayList<>();

    private LinearLayout layoutFiltrosUser;
    private Spinner spFilterAsignacionUser;
    private Spinner spFilterStatusUser;

    private String filtroAsignacion = "Todos";
    private String filtroStatus = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_tickets);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Sesión no válida. Inicia sesión de nuevo.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String uid = user.getUid();

        RecyclerView rv = findViewById(R.id.rvTickets);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TicketAdapter(ticket -> {
            Intent i = new Intent(this, DetalleTicketActivity.class);
            i.putExtra("TICKET_ID", ticket.getId());
            startActivity(i);
        });

        rv.setAdapter(adapter);

        layoutFiltrosUser = findViewById(R.id.layoutFiltrosUser);
        spFilterAsignacionUser = findViewById(R.id.spFilterAsignacionUser);
        spFilterStatusUser = findViewById(R.id.spFilterStatusUser);

        configurarFiltros();

        FloatingActionButton fab = findViewById(R.id.fabCrear);
        fab.setOnClickListener(v -> startActivity(new Intent(this, CrearTicketActivity.class)));

        ticketsListener = repo.escucharTicketsPorUsuario(uid, new TicketRepository.TicketsCallback() {
            @Override
            public void onSuccess(List<Ticket> tickets) {
                todosLosTickets.clear();
                todosLosTickets.addAll(tickets);
                aplicarFiltros();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MisTicketsActivity.this, "Error cargando tickets: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ImageButton btnFilter = findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> {
            if (layoutFiltrosUser.getVisibility() == View.VISIBLE) {
                layoutFiltrosUser.setVisibility(View.GONE);
            } else {
                layoutFiltrosUser.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.btnClearFiltersUser).setOnClickListener(v -> {
            filtroAsignacion = "Todos";
            filtroStatus = "Todos";
            spFilterAsignacionUser.setSelection(0);
            spFilterStatusUser.setSelection(0);
            aplicarFiltros();
        });

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            if (ticketsListener != null) {
                ticketsListener.remove();
                ticketsListener = null;
            }

            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(MisTicketsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void configurarFiltros() {
        String[] opcionesAsignacion = {"Todos", "Asignados", "No asignados"};
        String[] opcionesStatus = {"Todos", "OPEN", "IN_PROGRESS", "RESOLVED"};

        ArrayAdapter<String> adapterAsignacion = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                opcionesAsignacion
        );
        adapterAsignacion.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterAsignacionUser.setAdapter(adapterAsignacion);

        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                opcionesStatus
        );
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterStatusUser.setAdapter(adapterStatus);

        spFilterAsignacionUser.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filtroAsignacion = spFilterAsignacionUser.getSelectedItem().toString();
                aplicarFiltros();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        spFilterStatusUser.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filtroStatus = spFilterStatusUser.getSelectedItem().toString();
                aplicarFiltros();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    private void aplicarFiltros() {
        List<Ticket> filtrados = new ArrayList<>();

        for (Ticket t : todosLosTickets) {
            boolean coincideAsignacion;

            switch (filtroAsignacion) {
                case "Asignados":
                    coincideAsignacion = t.getAssignedTo() != null && !t.getAssignedTo().trim().isEmpty();
                    break;
                case "No asignados":
                    coincideAsignacion = t.getAssignedTo() == null || t.getAssignedTo().trim().isEmpty();
                    break;
                default:
                    coincideAsignacion = true;
                    break;
            }

            boolean coincideStatus =
                    "Todos".equals(filtroStatus) ||
                            (t.getStatus() != null && t.getStatus().equals(filtroStatus));

            if (coincideAsignacion && coincideStatus) {
                filtrados.add(t);
            }
        }

        adapter.submit(filtrados);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ticketsListener != null) {
            ticketsListener.remove();
            ticketsListener = null;
        }
    }
}