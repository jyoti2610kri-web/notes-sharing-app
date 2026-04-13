package com.notesharing.controller;

import com.notesharing.model.Note;
import com.notesharing.model.User;
import com.notesharing.repository.NoteRepository;
import com.notesharing.service.CloudinaryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class NoteController {
    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping("/")
    public String index(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        List<Note> userNotes = noteRepository.findByUserId(user.getId());
        model.addAttribute("notes", userNotes);
        model.addAttribute("user", user);
        return "dashboard";
    }

    @GetMapping("/upload")
    public String uploadPage(HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        return "upload";
    }

    @PostMapping("/upload")
    public String processUpload(@RequestParam String title, @RequestParam String description, 
                                @RequestParam String category, @RequestParam MultipartFile file, 
                                HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        String fileUrl = cloudinaryService.uploadFile(file);
        Note note = new Note(UUID.randomUUID().toString(), title, description, fileUrl, 
                             user.getId(), user.getName(), category, user.getUniversity());
        noteRepository.save(note);
        return "redirect:/dashboard";
    }

    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable String id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Note note = noteRepository.findById(id)
                .filter(n -> n.getUserId().equals(user.getId()))
                .orElse(null);
        
        if (note == null) return "redirect:/dashboard";
        
        model.addAttribute("note", note);
        return "edit";
    }

    @PostMapping("/edit")
    public String processEdit(@RequestParam String id, @RequestParam String title, 
                              @RequestParam String description, @RequestParam String category, 
                              HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Note note = noteRepository.findById(id)
                .filter(n -> n.getUserId().equals(user.getId()))
                .orElse(null);

        if (note != null) {
            note.setTitle(title);
            note.setDescription(description);
            note.setCategory(category);
            noteRepository.save(note);
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String studentName, 
                         @RequestParam(required = false) String university,
                         @RequestParam(required = false) String category, 
                         HttpSession session, Model model) {
        if (session.getAttribute("user") == null) return "redirect:/login";

        List<Note> results = null;
        
        boolean hasSearch = (studentName != null && !studentName.trim().isEmpty()) || 
                            (university != null && !university.trim().isEmpty()) || 
                            (category != null && !category.trim().isEmpty());

        if (hasSearch) {
            results = noteRepository.findAll();
            
            if (studentName != null && !studentName.trim().isEmpty()) {
                String lowerName = studentName.toLowerCase().trim();
                results = results.stream()
                        .filter(n -> n.getUserName() != null && n.getUserName().toLowerCase().contains(lowerName))
                        .collect(Collectors.toList());
            }

            if (university != null && !university.trim().isEmpty()) {
                String lowerUni = university.toLowerCase().trim();
                results = results.stream()
                        .filter(n -> n.getUniversity() != null && n.getUniversity().toLowerCase().contains(lowerUni))
                        .collect(Collectors.toList());
            }
            
            if (category != null && !category.trim().isEmpty()) {
                results = results.stream()
                        .filter(n -> category.equals(n.getCategory()))
                        .collect(Collectors.toList());
            }
        }

        model.addAttribute("notes", results);
        model.addAttribute("studentName", studentName);
        model.addAttribute("university", university);
        model.addAttribute("selectedCategory", category);
        return "search";
    }

    @GetMapping("/download/{id}")
    public String downloadFile(@PathVariable String id) {
        Note note = noteRepository.findById(id).orElse(null);
        if (note == null) return "redirect:/search";
        
        return "redirect:" + cloudinaryService.getDownloadUrl(note.getFilename());
    }

    @GetMapping("/view/{id}")
    public String viewFile(@PathVariable String id) {
        Note note = noteRepository.findById(id).orElse(null);
        if (note == null) return "redirect:/search";
        return "redirect:" + cloudinaryService.getViewUrl(note.getFilename());
    }

    @GetMapping("/view-note/{id}")
    public String viewNotePage(@PathVariable String id, HttpSession session, Model model) {
        if (session.getAttribute("user") == null) return "redirect:/login";

        Note note = noteRepository.findById(id).orElse(null);

        if (note == null) return "redirect:/search";

        model.addAttribute("note", note);
        model.addAttribute("viewUrl", cloudinaryService.getViewUrl(note.getFilename()));
        model.addAttribute("isPdf", note.getFilename().toLowerCase().contains(".pdf"));
        return "view-note";
    }

    @GetMapping("/delete/{id}")
    public String deleteNote(@PathVariable String id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        Note note = noteRepository.findById(id)
                .filter(n -> n.getUserId().equals(user.getId()))
                .orElse(null);

        if (note != null) {
            cloudinaryService.deleteFile(note.getFilename());
            noteRepository.deleteById(id);
        }
        return "redirect:/dashboard";
    }
}
