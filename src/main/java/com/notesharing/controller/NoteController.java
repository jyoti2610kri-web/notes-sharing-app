package com.notesharing.controller;

import com.notesharing.model.Note;
import com.notesharing.model.User;
import com.notesharing.repository.NoteRepository;
import com.notesharing.service.CloudinaryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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

        List<String> imageUrls = new ArrayList<>();
        List<String> publicIds = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            
            // Only allow images
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                continue;
            }
            
            Map result = cloudinaryService.uploadImage(file);
            imageUrls.add((String) result.get("secure_url"));
            publicIds.add((String) result.get("public_id"));
        }

        if (!imageUrls.isEmpty()) {
            Note note = new Note(
                UUID.randomUUID().toString(),
                title,
                description,
                imageUrls,
                publicIds,
                user.getId(),
                user.getName(),
                category,
                user.getUniversity()
            );
            noteRepository.save(note);
        }
        
        return "redirect:/dashboard";
    }

    @GetMapping("/view-note/{id}")
    public String viewNotePage(@PathVariable String id, HttpSession session, Model model) {
        if (session.getAttribute("user") == null) return "redirect:/login";

        Note note = noteRepository.findById(id).orElse(null);
        if (note == null) return "redirect:/dashboard";

        model.addAttribute("note", note);
        return "view-note";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadNote(@PathVariable String id, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(403).build();
        }

        Note note = noteRepository.findById(id).orElse(null);
        if (note == null || note.getImageUrls().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            for (String imageUrl : note.getImageUrls()) {
                Image img = new Image(ImageDataFactory.create(new URL(imageUrl)));
                // Scale image to fit page
                img.setAutoScale(true);
                document.add(img);
            }

            document.close();
            byte[] pdfBytes = baos.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + note.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String studentName, 
                         @RequestParam(required = false) String university,
                         @RequestParam(required = false) String category, 
                         HttpSession session, Model model) {
        if (session.getAttribute("user") == null) return "redirect:/login";

        // Check if any search criteria is provided
        boolean searchCriteriaProvided = (studentName != null && !studentName.trim().isEmpty()) ||
                                         (university != null && !university.trim().isEmpty()) ||
                                         (category != null && !category.trim().isEmpty());

        if (!searchCriteriaProvided) {
            model.addAttribute("notes", null);
            return "search";
        }

        List<Note> results = noteRepository.findAll();
        
        // Filtering logic
        if (studentName != null && !studentName.trim().isEmpty()) {
            results = results.stream().filter(n -> n.getUserName().toLowerCase().contains(studentName.toLowerCase())).toList();
            model.addAttribute("studentName", studentName);
        }
        if (university != null && !university.trim().isEmpty()) {
            results = results.stream().filter(n -> n.getUniversity().toLowerCase().contains(university.toLowerCase())).toList();
            model.addAttribute("university", university);
        }
        if (category != null && !category.trim().isEmpty()) {
            results = results.stream().filter(n -> n.getCategory().equals(category)).toList();
            model.addAttribute("selectedCategory", category);
        }

        model.addAttribute("notes", results);
        return "search";
    }

    @GetMapping("/delete/{id}")
    public String deleteNote(@PathVariable String id, HttpSession session) throws IOException {
        User user = (User) session.getAttribute("user");
        Note note = noteRepository.findById(id).filter(n -> n.getUserId().equals(user.getId())).orElse(null);

        if (note != null) {
            for (String pid : note.getPublicIds()) {
                cloudinaryService.deleteImage(pid);
            }
            noteRepository.deleteById(id);
        }
        return "redirect:/dashboard";
    }
}
