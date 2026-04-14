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
        String cleanUrl = cloudinaryUrl.replace("<", "").replace(">", "").trim();
        this.cloudinary = new Cloudinary(cleanUrl);
    }

    public Map uploadFile(MultipartFile file) {
        try {
            // Determine resource type: PDFs should be 'image' in Cloudinary to allow for previewing/thumbnails
            // or 'raw' if you just want the file. 'auto' usually works best.
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "auto", 
                    "flags", "attachment"
            ));
            return uploadResult;
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary upload failed: " + e.getMessage(), e);
        }
    }

    public String getDownloadUrl(String publicId, String resourceType, String format) {
        if (publicId == null || publicId.isEmpty()) return "";
        
        // Use the explicit resource type returned by Cloudinary (important for PDFs)
        return cloudinary.url()
                .resourceType(resourceType != null ? resourceType : "auto")
                .secure(true)
                .format(format != null ? format : "")
                .transformation(new Transformation().flags("attachment"))
                .generate(publicId);
    }

    public String getViewUrl(String publicId, String resourceType, String format) {
        if (publicId == null || publicId.isEmpty()) return "";
        
        return cloudinary.url()
                .resourceType(resourceType != null ? resourceType : "auto")
                .secure(true)
                .format(format != null ? format : "")
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
