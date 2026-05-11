package org.example.supportflow.model;

public class Ticket {
    private String id;
    private String title;
    private String description;
    private String category;
    private String priority;
    private String status;
    private String assignedTo;
    private String assignedToName;
    private long createdAt;

    // ESTA VARIABLE ES LA MÁS IMPORTANTE:
    // Debe llamarse exactamente igual que en Firebase (imageUrl)
    private String imageUrl;

    public Ticket() {
        // Constructor vacío necesario para Firebase
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // GETTER Y SETTER PARA LA IMAGEN
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}