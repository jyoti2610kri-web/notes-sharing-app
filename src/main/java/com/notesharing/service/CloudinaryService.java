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

    public Map uploadFile(MultipartFile file) {
        try {
            // Upload as 'auto' which handles images/raw (PDFs) appropriately
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "auto",
                    "access_mode", "public"
            ));
            return uploadResult;
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary upload failed", e);
        }
    }

    public String getDownloadUrl(String publicId, String resourceType) {
        if (publicId == null || publicId.isEmpty()) return "";
        
        // For downloads, we apply the attachment flag
        return cloudinary.url()
                .resourceType(resourceType != null ? resourceType : "image")
                .transformation(new Transformation().flags("attachment"))
                .generate(publicId);
    }

    public String getViewUrl(String publicId, String resourceType) {
        if (publicId == null || publicId.isEmpty()) return "";
        
        // If it's a raw file (PDF), we need to ensure the URL is clean for the browser to open
        return cloudinary.url()
                .resourceType(resourceType != null ? resourceType : "image")
                .generate(publicId);
    }
    
    public void deleteFile(String publicId, String resourceType) {
        try {
            if (publicId != null && !publicId.isEmpty()) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType != null ? resourceType : "image"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
