package org.example.supportflow.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import org.example.supportflow.R;
import org.example.supportflow.adapter.CommentAdapter;
import org.example.supportflow.model.Comment;
import org.example.supportflow.model.Ticket;
import org.example.supportflow.ui.auth.LoginActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetalleTicketActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CommentAdapter commentAdapter;
    private String ticketId;
    private ImageView ivEvidence;

    private ListenerRegistration ticketListener;
    private ListenerRegistration commentsListener;

    private String currentUserName = "Usuario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_ticket);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Sesión no válida.", Toast.LENGTH_SHORT).show();
            irALogin();
            return;
        }

        String uid = user.getUid();

        ticketId = getIntent().getStringExtra("TICKET_ID");
        if (ticketId == null || ticketId.isEmpty()) {
            Toast.makeText(this, "Ticket inválido.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null && !name.trim().isEmpty()) {
                            currentUserName = name.trim();
                        }
                    }
                });

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvDesc = findViewById(R.id.tvDesc);
        TextView tvMeta = findViewById(R.id.tvMeta);
        TextView tvAssigned = findViewById(R.id.tvAssigned);
        ivEvidence = findViewById(R.id.ivEvidence);

        RecyclerView rvComments = findViewById(R.id.rvComments);
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter();
        rvComments.setAdapter(commentAdapter);

        EditText etComment = findViewById(R.id.etComment);
        Button btnSend = findViewById(R.id.btnSendComment);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        Button btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> hacerLogoutSeguro());
        }

        ticketListener = db.collection("tickets").document(ticketId)
                .addSnapshotListener((doc, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error leyendo ticket", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (doc != null && doc.exists()) {
                        Ticket t = doc.toObject(Ticket.class);
                        if (t != null) {
                            tvTitle.setText(t.getTitle());
                            tvDesc.setText(t.getDescription());

                            String meta = "Categoría: " + t.getCategory()
                                    + " | Prioridad: " + t.getPriority()
                                    + " | Estado: " + t.getStatus();
                            tvMeta.setText(meta);

                            String assignedName = (t.getAssignedToName() != null && !t.getAssignedToName().trim().isEmpty())
                                    ? t.getAssignedToName()
                                    : "Sin asignar";
                            tvAssigned.setText("Técnico asignado: " + assignedName);

                            if (t.getImageUrl() != null && !t.getImageUrl().isEmpty()) {
                                ivEvidence.setVisibility(View.VISIBLE);
                                Glide.with(this)
                                        .load(t.getImageUrl())
                                        .placeholder(R.drawable.ic_launcher_foreground)
                                        .error(R.drawable.ic_launcher_foreground)
                                        .into(ivEvidence);
                            } else {
                                ivEvidence.setVisibility(View.GONE);
                            }
                        }
                    }
                });

        commentsListener = db.collection("tickets").document(ticketId)
                .collection("comments")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error leyendo comentarios", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<Comment> list = new ArrayList<>();
                    if (snap != null) {
                        for (var d : snap.getDocuments()) {
                            Comment c = d.toObject(Comment.class);
                            if (c != null) {
                                c.setId(d.getId());
                                list.add(c);
                            }
                        }
                    }
                    commentAdapter.submit(list);
                });

        btnSend.setOnClickListener(v -> {
            String msg = etComment.getText().toString().trim();
            if (msg.isEmpty()) {
                Toast.makeText(this, "Escribe un comentario", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> comment = new HashMap<>();
            comment.put("message", msg);
            comment.put("authorId", uid);
            comment.put("authorName", currentUserName);
            comment.put("createdAt", System.currentTimeMillis());

            db.collection("tickets").document(ticketId)
                    .collection("comments")
                    .add(comment)
                    .addOnSuccessListener(r -> {
                        etComment.setText("");
                        Toast.makeText(this, "Comentario enviado ✅", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(err ->
                            Toast.makeText(this, "Error enviando comentario: " + err.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void removerListeners() {
        if (ticketListener != null) {
            ticketListener.remove();
            ticketListener = null;
        }

        if (commentsListener != null) {
            commentsListener.remove();
            commentsListener = null;
        }
    }

    private void hacerLogoutSeguro() {
        removerListeners();
        FirebaseAuth.getInstance().signOut();
        irALogin();
    }

    private void irALogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        removerListeners();
        super.onDestroy();
    }
}