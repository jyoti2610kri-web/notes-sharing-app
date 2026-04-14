package com.notesharing.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "notes")
public class Note {
    @Id
    private String id;
    private String title;
    private String description;
    
    @Column(length = 2048)
    private String filename; // Stores the URL
    
    private String publicId; // For Cloudinary
    private String resourceType; // For Cloudinary
    
    private String userId;
    private String userName;
    private String category;
    private String uploadDate;
    private String university;

    public Note() {}

    public Note(String id, String title, String description, String filename, String publicId, String resourceType, String userId, String userName, String category, String university) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.filename = filename;
        this.publicId = publicId;
        this.resourceType = resourceType;
        this.userId = userId;
        this.userName = userName;
        this.category = category;
        this.university = university;
        this.uploadDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getUploadDate() { return uploadDate; }
    public void setUploadDate(String uploadDate) { this.uploadDate = uploadDate; }
    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }
}
