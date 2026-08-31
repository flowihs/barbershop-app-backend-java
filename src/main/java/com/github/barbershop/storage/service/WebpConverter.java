package com.github.barbershop.storage.service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

import com.github.barbershop.storage.exception.InvalidImageFormatException;
import com.github.barbershop.common.error.exception.StorageException;
import org.springframework.web.multipart.MultipartFile;

public class WebpConverter {

    private static final String[] ALLOWED_FORMATS = {"jpg","jpeg","png","webp","bmp","gif"};

    public static byte[] ensureWebp(MultipartFile file) {
        validateIsImage(file);

        if (isWebp(file)) {
            try {
                return file.getBytes();
            } catch (IOException e) {
                throw new InvalidImageFormatException();
            }
        }

        return convertToWebp(file);
    }

    private static void validateIsImage(MultipartFile file) {
        try {
            if (file == null || file.isEmpty() || file.getSize() == 0) {
                throw new InvalidImageFormatException();
            }
            try (InputStream is = file.getInputStream()) {
                BufferedImage img = ImageIO.read(is);
                if (img == null) {
                    throw new InvalidImageFormatException();
                }
            }
        } catch (IOException e) {
            throw new InvalidImageFormatException();
        }
    }

    private static boolean isWebp(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && contentType.equalsIgnoreCase("image/webp")) {
            return true;
        }

        String name = file.getOriginalFilename();
        if (name != null && name.toLowerCase().endsWith(".webp")) {
            return true;
        }

        try (InputStream is = file.getInputStream();
             ImageInputStream iis = ImageIO.createImageInputStream(is)) {

            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("webp");
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(iis);
                    reader.getWidth(0);
                    return true;
                } catch (Exception ex) {
                    return false;
                } finally {
                    reader.dispose();
                }
            }
        } catch (IOException ignored) { }
        return false;
    }

    private static byte[] convertToWebp(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            BufferedImage img = ImageIO.read(is);
            if (img == null) {
                throw new InvalidImageFormatException();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean ok = ImageIO.write(img, "webp", baos);
            if (!ok) {
                throw new StorageException("Модуль записи WebP недоступен");
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new InvalidImageFormatException();
        }
    }
}