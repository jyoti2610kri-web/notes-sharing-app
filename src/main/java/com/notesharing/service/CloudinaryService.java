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
        System.out.println("Generating download URL for: " + url);
        if (url == null || url.isEmpty()) return url;
        
        String publicId = extractPublicId(url);
        String resourceType = extractResourceType(url);
        System.out.println("Extracted PublicId: " + publicId + ", ResourceType: " + resourceType);
        
        String generatedUrl = cloudinary.url()
                .resourceType(resourceType)
                .transformation(new Transformation().flags("attachment"))
                .generate(publicId);
        System.out.println("Generated URL: " + generatedUrl);
        return generatedUrl;
    }

    public String getViewUrl(String url) {
        System.out.println("Generating view URL for: " + url);
        if (url == null || url.isEmpty()) return url;
        
        String publicId = extractPublicId(url);
        String resourceType = extractResourceType(url);
        System.out.println("Extracted PublicId: " + publicId + ", ResourceType: " + resourceType);
        
        String generatedUrl = cloudinary.url()
                .resourceType(resourceType)
                .generate(publicId);
        System.out.println("Generated URL: " + generatedUrl);
        return generatedUrl;
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
            if (startIdx < url.length() && url.charAt(startIdx) == 'v') {
                int nextSlash = url.indexOf("/", startIdx);
                if (nextSlash != -1) {
                    String potentialVersion = url.substring(startIdx + 1, nextSlash);
                    if (potentialVersion.matches("\\d+")) {
                        startIdx = nextSlash + 1;
                    }
                }
            }
            
            int lastDot = url.lastIndexOf(".");
            if (lastDot > startIdx) {
                return url.substring(startIdx, lastDot);
            }
            return url.substring(startIdx);
        } catch (Exception e) {
            System.err.println("Error extracting public ID: " + e.getMessage());
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
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.contains("/raw/")) return "raw";
        if (lowerUrl.contains("/video/")) return "video";
        // If the URL ends in .pdf or contains /pdf/, Cloudinary often treats it as 'image' 
        // for processing, but if it was a raw upload, it stays 'raw'.
        if (lowerUrl.endsWith(".pdf") && !lowerUrl.contains("/image/")) return "raw";
        
        return "image"; // Default for most images and processed PDFs
    }
}
