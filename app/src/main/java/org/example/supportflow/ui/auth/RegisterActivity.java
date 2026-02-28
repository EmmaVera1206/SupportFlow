package org.example.supportflow.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import org.example.supportflow.R;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText etName = findViewById(R.id.etName);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (pass.length() < 6) {
                Toast.makeText(this, "La contraseña debe tener mínimo 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(result -> {
                        String uid = result.getUser().getUid();

                        Map<String, Object> userDoc = new HashMap<>();
                        userDoc.put("name", name);
                        userDoc.put("email", email);
                        userDoc.put("role", "USER"); // ✅ opción B
                        userDoc.put("createdAt", System.currentTimeMillis());

                        db.collection("users").document(uid)
                                .set(userDoc)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Cuenta creada ✅", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, RoleRouterActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error guardando perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}