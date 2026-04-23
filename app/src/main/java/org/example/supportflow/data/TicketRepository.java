package org.example.supportflow.data;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;

import org.example.supportflow.model.Ticket;

import java.util.HashMap;
import java.util.Map;

public class TicketRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface SimpleCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface TicketsCallback {
        void onSuccess(java.util.List<Ticket> tickets);
        void onError(Exception e);
    }

    public interface TicketCallback {
        void onSuccess(Ticket ticket);
        void onNotFound();
        void onError(Exception e);
    }

    public void crearTicket(String title, String description, String category, String priority, String createdBy, String imageUrl, SimpleCallback cb) {
        Map<String, Object> t = new HashMap<>();
        t.put("title", title);
        t.put("description", description);
        t.put("category", category);
        t.put("priority", priority);
        t.put("status", "OPEN");
        t.put("createdAt", System.currentTimeMillis());
        t.put("createdBy", createdBy);
        t.put("assignedTo", null);
        t.put("closedAt", null);
        t.put("imageUrl", imageUrl);

        db.collection("tickets")
                .add(t)
                .addOnSuccessListener(doc -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }

    public ListenerRegistration escucharTicketsPorUsuario(String createdBy, TicketsCallback cb) {
        return db.collection("tickets")
                .whereEqualTo("createdBy", createdBy)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        cb.onError(e);
                        return;
                    }

                    java.util.List<Ticket> list = new java.util.ArrayList<>();
                    if (snap != null) {
                        for (var doc : snap.getDocuments()) {
                            Ticket t = doc.toObject(Ticket.class);
                            if (t != null) {
                                t.setId(doc.getId());
                                list.add(t);
                            }
                        }
                    }
                    cb.onSuccess(list);
                });
    }

    public ListenerRegistration escucharTicketPorId(String ticketId, TicketCallback cb) {
        return db.collection("tickets")
                .document(ticketId)
                .addSnapshotListener((doc, e) -> {
                    if (e != null) {
                        cb.onError(e);
                        return;
                    }
                    if (doc == null || !doc.exists()) {
                        cb.onNotFound();
                        return;
                    }
                    Ticket t = doc.toObject(Ticket.class);
                    if (t != null) {
                        t.setId(doc.getId());
                        cb.onSuccess(t);
                    } else {
                        cb.onNotFound();
                    }
                });
    }

    public ListenerRegistration escucharTodosLosTickets(TicketsCallback cb) {
        return db.collection("tickets")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        cb.onError(e);
                        return;
                    }

                    java.util.List<Ticket> list = new java.util.ArrayList<>();
                    if (snap != null) {
                        for (var doc : snap.getDocuments()) {
                            Ticket t = doc.toObject(Ticket.class);
                            if (t != null) {
                                t.setId(doc.getId());
                                list.add(t);
                            }
                        }
                    }
                    cb.onSuccess(list);
                });
    }
}