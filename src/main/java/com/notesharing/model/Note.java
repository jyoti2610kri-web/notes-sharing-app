package com.notesharing.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notes")
public class Note {
    @Id
    private String id;
    private String title;
    private String description;
    
    @ElementCollection
    @CollectionTable(name = "note_images", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "image_url", length = 2048)
    private List<String> imageUrls = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "note_public_ids", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "public_id")
    private List<String> publicIds = new ArrayList<>();
    
    private String userId;
    private String userName;
    private String category;
    private String uploadDate;
    private String university;

    public Note() {}

    public Note(String id, String title, String description, List<String> imageUrls, List<String> publicIds, String userId, String userName, String category, String university) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrls = imageUrls;
        this.publicIds = publicIds;
        this.userId = userId;
        this.userName = userName;
        this.category = category;
        this.university = university;
        this.uploadDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public List<String> getPublicIds() { return publicIds; }
    public void setPublicIds(List<String> publicIds) { this.publicIds = publicIds; }
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
