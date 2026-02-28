package org.example.supportflow.model;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.supportflow.data.TicketRepository.SimpleCallback;

public class Comment {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String id;
    private String message;
    private String authorId;
    private String authorName;
    private long createdAt;

    public Comment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // --- Sprint C: Comentarios ---
    public interface CommentsCallback {
        void onSuccess(List<Comment> comments);
        void onError(Exception e);
    }

    public void escucharComentarios(String ticketId, CommentsCallback cb) {
        db.collection("tickets")
                .document(ticketId)
                .collection("comments")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        cb.onError(e);
                        return;
                    }
                    List<Comment> list = new ArrayList<>();
                    if (snap != null) {
                        for (var doc : snap.getDocuments()) {
                            Comment c = doc.toObject(Comment.class);
                            if (c != null) {
                                c.setId(doc.getId());
                                list.add(c);
                            }
                        }
                    }
                    cb.onSuccess(list);
                });
    }

    public void agregarComentario(String ticketId, String message, String authorId, String authorName, SimpleCallback cb) {
        Map<String, Object> comment = new HashMap<>();
        comment.put("message", message);
        comment.put("authorId", authorId);
        comment.put("authorName", authorName);
        comment.put("createdAt", System.currentTimeMillis());

        db.collection("tickets")
                .document(ticketId)
                .collection("comments")
                .add(comment)
                .addOnSuccessListener(doc -> cb.onSuccess())
                .addOnFailureListener(cb::onError);
    }
}
