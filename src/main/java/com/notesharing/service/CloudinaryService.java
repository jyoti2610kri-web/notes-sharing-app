package com.notesharing.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(@Value("${cloudinary.url}") String cloudinaryUrl) {
        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }

    public String uploadFile(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "auto"
            ));
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary upload failed", e);
        }
    }
    
    public void deleteFile(String url) {
        try {
            // Extract public ID from URL
            String publicId = extractPublicId(url);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String extractPublicId(String url) {
        // Basic extraction logic: get the part between last / and last .
        int lastSlash = url.lastIndexOf("/");
        int lastDot = url.lastIndexOf(".");
        if (lastSlash != -1 && lastDot != -1 && lastDot > lastSlash) {
            return url.substring(lastSlash + 1, lastDot);
        }
        return "";
    }
}
