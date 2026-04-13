package com.notesharing.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
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
                    "resource_type", "auto",
                    "access_mode", "public"
            ));
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary upload failed", e);
        }
    }

    public String getDownloadUrl(String url) {
        if (url == null || url.isEmpty()) return url;
        
        String publicId = extractPublicId(url);
        String resourceType = extractResourceType(url);
        
        return cloudinary.url()
                .resourceType(resourceType)
                .signed(true)
                .transformation(new Transformation().flags("attachment"))
                .generate(publicId);
    }

    public String getViewUrl(String url) {
        if (url == null || url.isEmpty()) return url;
        
        String publicId = extractPublicId(url);
        String resourceType = extractResourceType(url);
        
        return cloudinary.url()
                .resourceType(resourceType)
                .signed(true)
                .generate(publicId);
    }
    
    public void deleteFile(String url) {
        try {
            String publicId = extractPublicId(url);
            String resourceType = extractResourceType(url);
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String extractPublicId(String url) {
        try {
            // Find where the public ID starts. It's after /upload/ or /upload/v12345/
            String searchStr = "/upload/";
            int startIdx = url.indexOf(searchStr);
            if (startIdx == -1) return "";
            
            startIdx += searchStr.length();
            
            // Check if there's a version number like v12345/
            // Versions are digits after 'v'
            if (url.charAt(startIdx) == 'v') {
                int nextSlash = url.indexOf("/", startIdx);
                String potentialVersion = url.substring(startIdx + 1, nextSlash);
                if (potentialVersion.matches("\\d+")) {
                    startIdx = nextSlash + 1;
                }
            }
            
            int lastDot = url.lastIndexOf(".");
            if (lastDot > startIdx) {
                return url.substring(startIdx, lastDot);
            }
            return url.substring(startIdx);
        } catch (Exception e) {
            // Fallback to basic logic if something goes wrong
            int lastSlash = url.lastIndexOf("/");
            int lastDot = url.lastIndexOf(".");
            if (lastSlash != -1 && lastDot != -1 && lastDot > lastSlash) {
                return url.substring(lastSlash + 1, lastDot);
            }
            return "";
        }
    }

    private String extractResourceType(String url) {
        if (url.contains("/raw/")) return "raw";
        if (url.contains("/video/")) return "video";
        return "image"; // Default
    }
}
