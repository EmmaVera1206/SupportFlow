package org.example.supportflow.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import org.example.supportflow.ui.user.MisTicketsActivity;

public class RoleRouterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "No existe perfil de usuario", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                        return;
                    }

                    String role = doc.getString("role");
                    if (role == null) role = "USER";

                    // ✅ Por ahora todos pueden entrar a MisTickets, luego separas:
                    Intent i = new Intent(this, MisTicketsActivity.class);
                    i.putExtra("ROLE", role);
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error leyendo rol: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}