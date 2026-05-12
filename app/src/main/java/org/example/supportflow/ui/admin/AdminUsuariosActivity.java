package org.example.supportflow.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import org.example.supportflow.R;
import org.example.supportflow.adapter.AdminUsuarioAdapter;
import org.example.supportflow.ui.auth.LoginActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminUsuariosActivity extends AppCompatActivity {

    private RecyclerView rvUsuarios;
    private AdminUsuarioAdapter adapter;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration usersListener;
    private String currentAdminUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_usuarios);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            irALogin();
            return;
        }

        currentAdminUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rvUsuarios = findViewById(R.id.rvUsuariosAdmin);
        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminUsuarioAdapter(new ArrayList<>(), currentAdminUid, new AdminUsuarioAdapter.OnUserActionListener() {
            @Override
            public void onCambiarRol(AdminUsuarioAdapter.UsuarioAdminItem usuario, String nuevoRol) {
                actualizarRolUsuario(usuario.uid, nuevoRol);
            }
        });

        rvUsuarios.setAdapter(adapter);

        findViewById(R.id.btnBackUsuariosAdmin).setOnClickListener(v -> finish());

        Button btnLogout = findViewById(R.id.btnCerrarSesionUsuariosAdmin);
        btnLogout.setOnClickListener(v -> {
            removerListener();
            FirebaseAuth.getInstance().signOut();
            irALogin();
        });

        escucharUsuarios();
    }

    private void escucharUsuarios() {
        usersListener = db.collection("users")
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error cargando usuarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<AdminUsuarioAdapter.UsuarioAdminItem> lista = new ArrayList<>();

                    if (snap != null) {
                        for (var doc : snap.getDocuments()) {
                            String uid = doc.getId();
                            String name = doc.getString("name");
                            String email = doc.getString("email");
                            String role = doc.getString("role");

                            if (role == null || role.trim().isEmpty()) role = "USER";

                            lista.add(new AdminUsuarioAdapter.UsuarioAdminItem(
                                    uid,
                                    name != null ? name : "Sin nombre",
                                    email != null ? email : "Sin correo",
                                    role
                            ));
                        }
                    }

                    adapter.setUsuarios(lista);
                });
    }

    private void actualizarRolUsuario(String uid, String nuevoRol) {
        if (uid.equals(currentAdminUid)) {
            Toast.makeText(this, "No puedes cambiar tu propio rol desde aquí", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(uid)
                .update("role", nuevoRol)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Rol actualizado a " + nuevoRol, Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar rol: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void removerListener() {
        if (usersListener != null) {
            usersListener.remove();
            usersListener = null;
        }
    }

    private void irALogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        removerListener();
        super.onDestroy();
    }
}