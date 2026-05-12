package org.example.supportflow.ui.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import org.example.supportflow.R;
import org.example.supportflow.adapter.AdminTicketAdapter;
import org.example.supportflow.data.TicketRepository;
import org.example.supportflow.model.Ticket;
import org.example.supportflow.ui.auth.LoginActivity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AdminTicketsActivity extends AppCompatActivity {

    private AdminTicketAdapter adapterSinAsignar;
    private AdminTicketAdapter adapterAsignados;

    private final TicketRepository repo = new TicketRepository();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ListenerRegistration ticketsListener;

    private final List<Ticket> todosLosTickets = new ArrayList<>();

    private String filtroPrioridad = "Todas";
    private String filtroEstado = "Todos";
    private String filtroCategoria = "Todas";
    private String filtroTecnico = "Todos";

    private LinearLayout layoutFiltrosAdmin;
    private Spinner spFilterPriorityAdmin;
    private Spinner spFilterStatusAdmin;
    private Spinner spFilterCategoryAdmin;
    private Spinner spFilterTechnicianAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_tickets);

        RecyclerView rvSinAsignar = findViewById(R.id.rvSinAsignar);
        rvSinAsignar.setLayoutManager(new LinearLayoutManager(this));
        adapterSinAsignar = new AdminTicketAdapter(
                new ArrayList<>(),
                new AdminTicketAdapter.OnTicketActionListener() {
                    @Override
                    public void onDetalles(Ticket ticket) {
                        abrirDetalle(ticket);
                    }

                    @Override
                    public void onAsignarOCambiar(Ticket ticket) {
                        mostrarDialogoAsignar(ticket);
                    }

                    @Override
                    public void onEliminar(Ticket ticket) {
                        confirmarEliminarTicket(ticket);
                    }
                }
        );
        rvSinAsignar.setAdapter(adapterSinAsignar);

        RecyclerView rvAsignados = findViewById(R.id.rvAsignados);
        rvAsignados.setLayoutManager(new LinearLayoutManager(this));
        adapterAsignados = new AdminTicketAdapter(
                new ArrayList<>(),
                new AdminTicketAdapter.OnTicketActionListener() {
                    @Override
                    public void onDetalles(Ticket ticket) {
                        abrirDetalle(ticket);
                    }

                    @Override
                    public void onAsignarOCambiar(Ticket ticket) {
                        mostrarDialogoAsignar(ticket);
                    }

                    @Override
                    public void onEliminar(Ticket ticket) {
                        confirmarEliminarTicket(ticket);
                    }
                }
        );
        rvAsignados.setAdapter(adapterAsignados);

        layoutFiltrosAdmin = findViewById(R.id.layoutFiltrosAdmin);
        spFilterPriorityAdmin = findViewById(R.id.spFilterPriorityAdmin);
        spFilterStatusAdmin = findViewById(R.id.spFilterStatusAdmin);
        spFilterCategoryAdmin = findViewById(R.id.spFilterCategoryAdmin);
        spFilterTechnicianAdmin = findViewById(R.id.spFilterTechnicianAdmin);

        configurarSpinnersBase();
        configurarEventosFiltros();

        findViewById(R.id.btnSalir).setOnClickListener(v -> finish());

        ImageButton btnFilter = findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> {
            if (layoutFiltrosAdmin.getVisibility() == View.VISIBLE) {
                layoutFiltrosAdmin.setVisibility(View.GONE);
            } else {
                layoutFiltrosAdmin.setVisibility(View.VISIBLE);
            }
        });

        ImageButton btnReportes = findViewById(R.id.btnReportesAdmin);
        btnReportes.setOnClickListener(v ->
                startActivity(new Intent(AdminTicketsActivity.this, AdminReportesActivity.class))
        );

        findViewById(R.id.btnUsuariosAdmin).setOnClickListener(v ->
                startActivity(new Intent(AdminTicketsActivity.this, AdminUsuariosActivity.class))
        );

        findViewById(R.id.btnClearFiltersAdmin).setOnClickListener(v -> limpiarFiltros());

        findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> hacerLogoutSeguro());

        cargarTickets();
    }

    private void configurarSpinnersBase() {
        String[] prioridades = {"Todas", "LOW", "MEDIUM", "HIGH", "CRITICAL"};
        String[] estados = {"Todos", "OPEN", "IN_PROGRESS", "RESOLVED"};

        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                prioridades
        );
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterPriorityAdmin.setAdapter(priorityAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                estados
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterStatusAdmin.setAdapter(statusAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Todas"}
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterCategoryAdmin.setAdapter(categoryAdapter);

        ArrayAdapter<String> technicianAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Todos"}
        );
        technicianAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterTechnicianAdmin.setAdapter(technicianAdapter);
    }

    private void configurarEventosFiltros() {
        spFilterPriorityAdmin.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filtroPrioridad = spFilterPriorityAdmin.getSelectedItem().toString();
                aplicarFiltros();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        spFilterStatusAdmin.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filtroEstado = spFilterStatusAdmin.getSelectedItem().toString();
                aplicarFiltros();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        spFilterCategoryAdmin.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filtroCategoria = spFilterCategoryAdmin.getSelectedItem().toString();
                aplicarFiltros();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        spFilterTechnicianAdmin.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filtroTecnico = spFilterTechnicianAdmin.getSelectedItem().toString();
                aplicarFiltros();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    // MÉTODO MODIFICADO PARA INCLUIR LA IMAGEN
    private void abrirDetalle(Ticket ticket) {
        Intent i = new Intent(this, AdminDetalleTicketActivity.class);
        i.putExtra(AdminDetalleTicketActivity.EXTRA_TICKET_ID, ticket.getId());
        i.putExtra(AdminDetalleTicketActivity.EXTRA_TITLE, ticket.getTitle());
        i.putExtra(AdminDetalleTicketActivity.EXTRA_DESCRIPTION, ticket.getDescription());
        i.putExtra(AdminDetalleTicketActivity.EXTRA_CATEGORY, ticket.getCategory());
        i.putExtra(AdminDetalleTicketActivity.EXTRA_PRIORITY, ticket.getPriority());
        i.putExtra(AdminDetalleTicketActivity.EXTRA_STATUS, ticket.getStatus());
        i.putExtra(AdminDetalleTicketActivity.EXTRA_ASSIGNED_TO, ticket.getAssignedTo());
        i.putExtra(AdminDetalleTicketActivity.EXTRA_ASSIGNED_TO_NAME, ticket.getAssignedToName());
        i.putExtra(AdminDetalleTicketActivity.EXTRA_CREATED_AT, ticket.getCreatedAt());

        // Esta es la línea clave que envía la URL de Cloudinary a la siguiente pantalla
        i.putExtra(AdminDetalleTicketActivity.EXTRA_IMAGE_URL, ticket.getImageUrl());

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
                        if (name != null && !name.trim().isEmpty()) {
                            nombres.add(name.trim());
                            uids.add(doc.getId());
                        }
                    }

                    if (nombres.isEmpty()) {
                        Toast.makeText(this, "No hay técnicos disponibles", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String titulo = (ticket.getAssignedTo() == null || ticket.getAssignedTo().isEmpty())
                            ? "Asignar Técnico"
                            : "Cambiar Técnico";

                    new AlertDialog.Builder(this)
                            .setTitle(titulo)
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
                .update(
                        "assignedTo", tecnicoId,
                        "assignedToName", tecnicoNombre,
                        "status", "IN_PROGRESS"
                )
                .addOnSuccessListener(unused -> {
                    String mensaje = (ticket.getAssignedTo() == null || ticket.getAssignedTo().isEmpty())
                            ? "Ticket asignado a " + tecnicoNombre
                            : "Técnico cambiado a " + tecnicoNombre;
                    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al asignar/cambiar técnico", Toast.LENGTH_SHORT).show()
                );
    }

    private void confirmarEliminarTicket(Ticket ticket) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar ticket")
                .setMessage("¿Seguro que deseas eliminar este ticket?\n\n" + ticket.getTitle())
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarTicket(ticket))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarTicket(Ticket ticket) {
        db.collection("tickets").document(ticket.getId())
                .delete()
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Ticket eliminado correctamente", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al eliminar ticket", Toast.LENGTH_SHORT).show()
                );
    }

    private void cargarTickets() {
        ticketsListener = repo.escucharTodosLosTickets(new TicketRepository.TicketsCallback() {
            @Override
            public void onSuccess(List<Ticket> tickets) {
                todosLosTickets.clear();
                todosLosTickets.addAll(tickets);
                actualizarOpcionesDinamicas();
                aplicarFiltros();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminTicketsActivity.this, "Error al cargar tickets", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarOpcionesDinamicas() {
        Set<String> categorias = new LinkedHashSet<>();
        Set<String> tecnicos = new LinkedHashSet<>();

        categorias.add("Todas");
        tecnicos.add("Todos");

        for (Ticket t : todosLosTickets) {
            if (t.getCategory() != null && !t.getCategory().trim().isEmpty()) {
                categorias.add(t.getCategory().trim());
            }

            if (t.getAssignedToName() != null && !t.getAssignedToName().trim().isEmpty()) {
                tecnicos.add(t.getAssignedToName().trim());
            }
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>(categorias)
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterCategoryAdmin.setAdapter(categoryAdapter);

        ArrayAdapter<String> technicianAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new ArrayList<>(tecnicos)
        );
        technicianAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFilterTechnicianAdmin.setAdapter(technicianAdapter);

        restaurarSeleccionSiExiste(spFilterCategoryAdmin, filtroCategoria, "Todas");
        restaurarSeleccionSiExiste(spFilterTechnicianAdmin, filtroTecnico, "Todos");
    }

    private void restaurarSeleccionSiExiste(Spinner spinner, String valor, String fallback) {
        if (spinner == null || spinner.getAdapter() == null) return;
        android.widget.Adapter adapter = spinner.getAdapter();
        int count = adapter.getCount();

        for (int i = 0; i < count; i++) {
            Object item = adapter.getItem(i);
            if (item != null && item.toString().equals(valor)) {
                spinner.setSelection(i);
                return;
            }
        }

        for (int i = 0; i < count; i++) {
            Object item = adapter.getItem(i);
            if (item != null && item.toString().equals(fallback)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void aplicarFiltros() {
        List<Ticket> sinAsignar = new ArrayList<>();
        List<Ticket> asignados = new ArrayList<>();

        for (Ticket t : todosLosTickets) {
            boolean coincidePrioridad =
                    "Todas".equals(filtroPrioridad) ||
                            (t.getPriority() != null && t.getPriority().equals(filtroPrioridad));

            boolean coincideEstado =
                    "Todos".equals(filtroEstado) ||
                            (t.getStatus() != null && t.getStatus().equals(filtroEstado));

            boolean coincideCategoria =
                    "Todas".equals(filtroCategoria) ||
                            (t.getCategory() != null && t.getCategory().equals(filtroCategoria));

            boolean coincideTecnico =
                    "Todos".equals(filtroTecnico) ||
                            (t.getAssignedToName() != null && t.getAssignedToName().equals(filtroTecnico));

            if (!(coincidePrioridad && coincideEstado && coincideCategoria && coincideTecnico)) {
                continue;
            }

            if (t.getAssignedTo() == null || t.getAssignedTo().isEmpty()) {
                sinAsignar.add(t);
            } else {
                asignados.add(t);
            }
        }

        adapterSinAsignar.setTickets(sinAsignar);
        adapterAsignados.setTickets(asignados);
    }

    private void limpiarFiltros() {
        filtroPrioridad = "Todas";
        filtroEstado = "Todos";
        filtroCategoria = "Todas";
        filtroTecnico = "Todos";

        spFilterPriorityAdmin.setSelection(0);
        spFilterStatusAdmin.setSelection(0);
        spFilterCategoryAdmin.setSelection(0);
        spFilterTechnicianAdmin.setSelection(0);

        aplicarFiltros();
    }

    private void removerListeners() {
        if (ticketsListener != null) {
            ticketsListener.remove();
            ticketsListener = null;
        }
    }

    private void hacerLogoutSeguro() {
        removerListeners();
        FirebaseAuth.getInstance().signOut();

        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        removerListeners();
        super.onDestroy();
    }
}