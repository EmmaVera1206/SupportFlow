package org.example.supportflow.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.example.supportflow.ui.admin.AdminTicketsActivity;
import org.example.supportflow.ui.tecnico.TecnicoAsignadosActivity;
import org.example.supportflow.ui.user.MisTicketsActivity;

public class RoleRouterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            irALogin();
            return;
        }

        user.reload()
                .addOnSuccessListener(unused -> {
                    FirebaseUser refreshedUser = auth.getCurrentUser();

                    if (refreshedUser == null) {
                        irALogin();
                        return;
                    }

                    if (!refreshedUser.isEmailVerified()) {
                        Toast.makeText(
                                this,
                                "Verifica tu correo antes de continuar.",
                                Toast.LENGTH_LONG
                        ).show();
                        auth.signOut();
                        irALogin();
                        return;
                    }

                    String uid = refreshedUser.getUid();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(doc -> {
                                if (!doc.exists()) {
                                    Toast.makeText(this, "No existe perfil de usuario", Toast.LENGTH_SHORT).show();
                                    auth.signOut();
                                    irALogin();
                                    return;
                                }

                                String role = doc.getString("role");
                                if (role == null) role = "USER";

                                Intent i;
                                switch (role) {
                                    case "TECH":
                                        i = new Intent(this, TecnicoAsignadosActivity.class);
                                        break;
                                    case "ADMIN":
                                        i = new Intent(this, AdminTicketsActivity.class);
                                        break;
                                    default:
                                        i = new Intent(this, MisTicketsActivity.class);
                                        break;
                                }

                                i.putExtra("ROLE", role);
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error leyendo rol: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                auth.signOut();
                                irALogin();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error validando usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    auth.signOut();
                    irALogin();
                });
    }

    private void irALogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}