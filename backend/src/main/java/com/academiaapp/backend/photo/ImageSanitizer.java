package com.academiaapp.backend.photo;

import com.academiaapp.backend.config.BadRequestException;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;

@Component
public class ImageSanitizer {

    private static final long MAX_PIXELS = 25_000_000L;

    public record SanitizedImage(byte[] bytes, String contentType) {
    }

    public SanitizedImage sanitize(byte[] original) {
        boolean jpeg = isJpeg(original);
        boolean png = isPng(original);
        if (!jpeg && !png) {
            throw new BadRequestException("Formato não suportado: envie uma imagem JPEG ou PNG");
        }

        BufferedImage image = decode(original);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            if (jpeg) {
                writeJpeg(image, out);
                return new SanitizedImage(out.toByteArray(), "image/jpeg");
            }
            if (!ImageIO.write(image, "png", out)) {
                throw new IllegalStateException("Não foi possível gravar a imagem PNG");
            }
            return new SanitizedImage(out.toByteArray(), "image/png");
        } catch (IOException ex) {
            throw new UncheckedIOException("Falha ao processar a imagem", ex);
        }
    }

    private BufferedImage decode(byte[] original) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(original))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new BadRequestException("Arquivo de imagem inválido");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                if (pixels > MAX_PIXELS) {
                    throw new BadRequestException("A imagem é grande demais (máximo de 25 megapixels)");
                }
                return reader.read(0);
            } finally {
                reader.dispose();
            }
        } catch (IOException | IllegalArgumentException ex) {
            throw new BadRequestException("Arquivo de imagem inválido");
        }
    }

    private void writeJpeg(BufferedImage source, ByteArrayOutputStream out) throws IOException {
        BufferedImage rgb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = rgb.createGraphics();
        graphics.drawImage(source, 0, 0, Color.WHITE, null);
        graphics.dispose();

        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.9f);

        try (ImageOutputStream imageOutput = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(imageOutput);
            writer.write(null, new IIOImage(rgb, null, null), param);
        } finally {
            writer.dispose();
        }
    }

    private boolean isJpeg(byte[] data) {
        return data.length >= 3
                && (data[0] & 0xFF) == 0xFF
                && (data[1] & 0xFF) == 0xD8
                && (data[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] data) {
        int[] signature = { 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
        if (data.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((data[i] & 0xFF) != signature[i]) {
                return false;
            }
        }
        return true;
    }
}