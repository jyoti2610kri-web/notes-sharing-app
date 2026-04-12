# Notes Sharing Website

A simple Spring Boot application for students to share notes.

## Features
- **User System:** Register and Login (stored in `data/users.json`).
- **Dashboard:** Manage your own uploaded notes.
- **Upload:** Support for PDF and Image uploads (stored in `uploads/`).
- **Search:** Find notes by title or Student ID.
- **Download:** Easy one-click download for any note.
- **No Database:** Uses local JSON files for data persistence.

## Prerequisites
- Java 17 or higher
- Maven installed

## How to Run
1. Open a terminal in this folder.
2. Run the command:
   ```bash
   mvn spring-boot:run
   ```
3. Open your browser and navigate to: `http://localhost:8080`

## Project Structure
- `src/main/java/com/notesharing/model`: Data objects (User, Note).
- `src/main/java/com/notesharing/service`: Business logic for JSON and File handling.
- `src/main/java/com/notesharing/controller`: Routing and request handling.
- `src/main/resources/templates`: HTML views using Thymeleaf.
- `data/`: Folder where your JSON databases are stored.
- `uploads/`: Folder where physical files are stored.
