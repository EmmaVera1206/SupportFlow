package org.example.supportflow.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.example.supportflow.R;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegistrate = findViewById(R.id.tvRegistrate);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Completa email y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);

            auth.signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(result -> {
                        FirebaseUser user = auth.getCurrentUser();

                        if (user == null) {
                            btnLogin.setEnabled(true);
                            Toast.makeText(this, "No se pudo iniciar sesión", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        user.reload()
                                .addOnSuccessListener(unused -> {
                                    FirebaseUser refreshedUser = auth.getCurrentUser();

                                    if (refreshedUser == null) {
                                        btnLogin.setEnabled(true);
                                        Toast.makeText(this, "No se pudo validar la sesión", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    if (!refreshedUser.isEmailVerified()) {
                                        auth.signOut();
                                        btnLogin.setEnabled(true);
                                        Toast.makeText(
                                                this,
                                                "Debes verificar tu correo antes de iniciar sesión.",
                                                Toast.LENGTH_LONG
                                        ).show();
                                        return;
                                    }

                                    Intent intent = new Intent(this, RoleRouterActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    auth.signOut();
                                    btnLogin.setEnabled(true);
                                    Toast.makeText(
                                            this,
                                            "No se pudo validar el estado del correo: " + e.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        btnLogin.setEnabled(true);
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        tvRegistrate.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Escribe tu email primero", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Correo de recuperación enviado ✅", Toast.LENGTH_LONG).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }
}