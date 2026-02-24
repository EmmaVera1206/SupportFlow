package org.example.supportflow.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.example.supportflow.R;
import org.example.supportflow.adapter.TicketAdapter;
import org.example.supportflow.data.TicketRepository;

public class MisTicketsActivity extends AppCompatActivity {

    private final TicketRepository repo = new TicketRepository();
    private final TicketAdapter adapter = new TicketAdapter();

    private final String DEMO_USER = "demoUser1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_tickets);

        RecyclerView rv = findViewById(R.id.rvTickets);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabCrear);
        fab.setOnClickListener(v -> startActivity(new Intent(this, CrearTicketActivity.class)));

        repo.escucharTicketsPorUsuario(DEMO_USER, new TicketRepository.TicketsCallback() {
            @Override
            public void onSuccess(java.util.List<org.example.supportflow.model.Ticket> tickets) {
                adapter.submit(tickets);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MisTicketsActivity.this, "Error cargando tickets", Toast.LENGTH_SHORT).show();
            }
        });
    }
}