package org.example.supportflow.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.example.supportflow.R;
import org.example.supportflow.adapter.TicketAdapter;
import org.example.supportflow.data.TicketRepository;

public class MisTicketsActivity extends AppCompatActivity {

    private final TicketRepository repo = new TicketRepository();
    private TicketAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_tickets);


        // Validar sesión
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Sesión no válida. Inicia sesión de nuevo.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String uid = user.getUid();

        // Recycler
        RecyclerView rv = findViewById(R.id.rvTickets);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // Navegar a crear ticket
        FloatingActionButton fab = findViewById(R.id.fabCrear);
        fab.setOnClickListener(v -> startActivity(new Intent(this, CrearTicketActivity.class)));

        // Escuchar tickets del usuario real
        repo.escucharTicketsPorUsuario(uid, new TicketRepository.TicketsCallback() {
            @Override
            public void onSuccess(java.util.List<org.example.supportflow.model.Ticket> tickets) {
                adapter.submit(tickets);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MisTicketsActivity.this, "Error cargando tickets: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        adapter = new TicketAdapter(ticket -> {
            Intent i = new Intent(this, DetalleTicketActivity.class);
            i.putExtra("TICKET_ID", ticket.getId());
            startActivity(i);
        });
        rv.setAdapter(adapter);

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, org.example.supportflow.ui.auth.LoginActivity.class));
            finish();
        });

    }

}