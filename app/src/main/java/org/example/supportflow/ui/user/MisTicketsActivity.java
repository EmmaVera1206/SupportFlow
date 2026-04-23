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
import com.google.firebase.firestore.ListenerRegistration;

import org.example.supportflow.R;
import org.example.supportflow.adapter.TicketAdapter;
import org.example.supportflow.data.TicketRepository;
import org.example.supportflow.model.Ticket;
import org.example.supportflow.ui.auth.LoginActivity;

import java.util.List;

public class MisTicketsActivity extends AppCompatActivity {

    private final TicketRepository repo = new TicketRepository();
    private TicketAdapter adapter;
    private ListenerRegistration ticketsListener;

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

        FloatingActionButton fab = findViewById(R.id.fabCrear);
        fab.setOnClickListener(v -> startActivity(new Intent(this, CrearTicketActivity.class)));

        ticketsListener = repo.escucharTicketsPorUsuario(uid, new TicketRepository.TicketsCallback() {
            @Override
            public void onSuccess(List<Ticket> tickets) {
                adapter.submit(tickets);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MisTicketsActivity.this, "Error cargando tickets: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ticketsListener != null) {
            ticketsListener.remove();
            ticketsListener = null;
        }
    }
}