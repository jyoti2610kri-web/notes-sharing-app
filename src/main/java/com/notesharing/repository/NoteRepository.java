package com.notesharing.repository;

import com.notesharing.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, String> {
    List<Note> findByUserId(String userId);
}
