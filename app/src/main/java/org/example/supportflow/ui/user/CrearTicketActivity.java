package org.example.supportflow.ui.user;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.example.supportflow.R;
import org.example.supportflow.data.TicketRepository;

public class CrearTicketActivity extends AppCompatActivity {

    private final TicketRepository repo = new TicketRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_ticket);

        // Validar sesión
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Sesión no válida. Inicia sesión de nuevo.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String uid = user.getUid();

        // UI refs
        EditText etTitle = findViewById(R.id.etTitle);
        EditText etDesc  = findViewById(R.id.etDescription);
        EditText etCat   = findViewById(R.id.etCategory);
        EditText etPri   = findViewById(R.id.etPriority);
        Button btnCrear  = findViewById(R.id.btnCrear);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Acción crear
        btnCrear.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc  = etDesc.getText().toString().trim();
            String cat   = etCat.getText().toString().trim();
            String pri   = etPri.getText().toString().trim().toUpperCase(); // LOW/MEDIUM/HIGH/CRITICAL

            if (title.isEmpty() || desc.isEmpty() || cat.isEmpty() || pri.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            repo.crearTicket(title, desc, cat, pri, uid, new TicketRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(CrearTicketActivity.this, "Ticket creado ✅", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(CrearTicketActivity.this, "Error al crear: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
