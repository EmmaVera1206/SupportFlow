package org.example.supportflow;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SupportFlow";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.btnTestFirestore);

        btn.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            String titulo = "Ticket de prueba";
            String descripcion = "Esto es una descripción de prueba";
            String categoria = "Soporte Técnico";
            String prioridad = "HIGH";

            Map<String, Object> ticket = new HashMap<>();
            ticket.put("title", titulo);
            ticket.put("description", descripcion);
            ticket.put("category", categoria);
            ticket.put("priority", prioridad); // "HIGH"
            ticket.put("status", "OPEN");
            ticket.put("createdAt", System.currentTimeMillis());
            ticket.put("createdBy", "demoUser1"); // luego FirebaseAuth UID
            ticket.put("assignedTo", null);
            ticket.put("closedAt", null);

            db.collection("tickets")
                    .add(ticket)
                    .addOnSuccessListener(docRef ->
                            Log.d(TAG, "✅ Ticket creado con ID: " + docRef.getId()))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "❌ Error creando ticket", e));
        });
    }
}