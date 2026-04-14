package com.notesharing.controller;

import com.notesharing.model.Note;
import com.notesharing.model.User;
import com.notesharing.repository.NoteRepository;
import com.notesharing.service.CloudinaryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class NoteController {
    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping("/")
    public String index(HttpSession session) {
        return session.getAttribute("user") != null ? "redirect:/dashboard" : "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        model.addAttribute("notes", noteRepository.findByUserId(user.getId()));
        model.addAttribute("user", user);
        return "dashboard";
    }

    @GetMapping("/upload")
    public String uploadPage(HttpSession session) {
        return session.getAttribute("user") == null ? "redirect:/login" : "upload";
    }

    @PostMapping("/upload")
    public String processUpload(@RequestParam String title, @RequestParam String description, 
                                @RequestParam String category, @RequestParam List<MultipartFile> files, 
                                HttpSession session) throws IOException {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            
            // Cloudinary Upload Logic
            Map result = cloudinaryService.uploadFile(file);
            
            // Store results in the database
            Note note = new Note(
                UUID.randomUUID().toString(),
                files.size() > 1 ? title + " (Part " + (files.indexOf(file) + 1) + ")" : title,
                description,
                (String) result.get("secure_url"),
                (String) result.get("public_id"),
                (String) result.get("resource_type"),
                (String) result.get("format"),
                user.getId(),
                user.getName(),
                category,
                user.getUniversity()
            );
            noteRepository.save(note);
        }
        return "redirect:/dashboard";
    }

    /**
     * Download Link Generation
     */
    @GetMapping("/download/{id}")
    public String downloadFile(@PathVariable String id) {
        Note note = noteRepository.findById(id).orElse(null);
        if (note == null) return "redirect:/dashboard";
        
        return "redirect:" + cloudinaryService.getDownloadUrl(note.getPublicId(), note.getResourceType(), note.getFormat());
    }

    /**
     * View Note Page
     */
    @GetMapping("/view-note/{id}")
    public String viewNotePage(@PathVariable String id, HttpSession session, Model model) {
        if (session.getAttribute("user") == null) return "redirect:/login";

        Note note = noteRepository.findById(id).orElse(null);
        if (note == null) return "redirect:/dashboard";

        model.addAttribute("note", note);
        model.addAttribute("viewUrl", cloudinaryService.getViewUrl(note.getPublicId(), note.getResourceType(), note.getFormat()));
        model.addAttribute("isPdf", "pdf".equalsIgnoreCase(note.getFormat()));
        return "view-note";
    }

    @GetMapping("/delete/{id}")
    public String deleteNote(@PathVariable String id, HttpSession session) throws IOException {
        User user = (User) session.getAttribute("user");
        Note note = noteRepository.findById(id).filter(n -> n.getUserId().equals(user.getId())).orElse(null);

        if (note != null) {
            cloudinaryService.deleteFile(note.getPublicId(), note.getResourceType());
            noteRepository.deleteById(id);
        }
        return "redirect:/dashboard";
    }
}
