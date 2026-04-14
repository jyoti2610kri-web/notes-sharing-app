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
        // Automatically clean the URL of any accidental brackets or whitespace
        String cleanUrl = cloudinaryUrl.replace("<", "").replace(">", "").trim();
        this.cloudinary = new Cloudinary(cleanUrl);
    }

    /**
     * Uploads any file type (Image or PDF) to Cloudinary.
     * Uses resource_type: "auto" for dynamic handling.
     */
    public Map uploadFile(MultipartFile file) throws IOException {
        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "resource_type", "auto"
        ));
    }

    /**
     * Generates a secure URL for viewing.
     */
    public String getViewUrl(String publicId, String resourceType, String format) {
        return cloudinary.url()
                .resourceType(resourceType)
                .secure(true)
                .format(format)
                .generate(publicId);
    }

    /**
     * Generates a secure URL with the attachment flag for forced download.
     */
    public String getDownloadUrl(String publicId, String resourceType, String format) {
        return cloudinary.url()
                .resourceType(resourceType)
                .secure(true)
                .format(format)
                .transformation(new Transformation().flags("attachment"))
                .generate(publicId);
    }

    public void deleteFile(String publicId, String resourceType) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
    }
}
