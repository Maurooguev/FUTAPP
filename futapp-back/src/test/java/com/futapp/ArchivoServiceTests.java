package com.futapp;

import com.futapp.service.ArchivoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchivoServiceTests {
    @TempDir Path carpeta;

    @Test
    void guardaUnaImagenConNombreSeguro() throws IOException {
        ArchivoService service = new ArchivoService(carpeta.toString());
        MockMultipartFile imagen = new MockMultipartFile("archivo", "foto.png", "image/png",
                png(20, 20));

        String url = service.guardarImagen(imagen);

        assertTrue(url.matches("/uploads/[a-f0-9-]+\\.png"));
        assertTrue(Files.exists(carpeta.resolve(url.substring("/uploads/".length()))));
    }

    @Test
    void rechazaArchivosQueNoSonImagen() {
        ArchivoService service = new ArchivoService(carpeta.toString());
        MockMultipartFile archivo = new MockMultipartFile("archivo", "datos.txt", "text/plain", new byte[]{1});

        assertThrows(IllegalArgumentException.class, () -> service.guardarImagen(archivo));
    }

    @Test
    void rechazaUnArchivoFalsoAunqueDeclareSerImagen() {
        ArchivoService service = new ArchivoService(carpeta.toString());
        MockMultipartFile archivo = new MockMultipartFile("archivo", "falso.png", "image/png", "no soy una imagen".getBytes());
        assertThrows(IllegalArgumentException.class, () -> service.guardarImagen(archivo));
    }

    @Test
    void rechazaUnaCabeceraValidaConContenidoTruncado() {
        ArchivoService service = new ArchivoService(carpeta.toString());
        byte[] truncado = new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a,
                0, 0, 0, 13, 'I', 'H', 'D', 'R', 0, 0, 0, 1, 0, 0, 0, 1};
        MockMultipartFile archivo = new MockMultipartFile("archivo", "truncado.png", "image/png", truncado);
        assertThrows(IllegalArgumentException.class, () -> service.guardarImagen(archivo));
    }

    @Test
    void rechazaCuandoElFormatoDeclaradoNoCoincide() throws IOException {
        ArchivoService service = new ArchivoService(carpeta.toString());
        MockMultipartFile archivo = new MockMultipartFile("archivo", "foto.jpg", "image/jpeg", png(20, 20));
        assertThrows(IllegalArgumentException.class, () -> service.guardarImagen(archivo));
    }

    @Test
    void rechazaResolucionesExcesivas() throws IOException {
        ArchivoService service = new ArchivoService(carpeta.toString());
        MockMultipartFile archivo = new MockMultipartFile("archivo", "larga.png", "image/png", png(4097, 1));
        assertThrows(IllegalArgumentException.class, () -> service.guardarImagen(archivo));
    }

    @Test
    void aceptaUnContenedorWebpEstaticoBienFormado() {
        ArchivoService service = new ArchivoService(carpeta.toString());
        MockMultipartFile archivo = new MockMultipartFile("archivo", "foto.webp", "image/webp", webpEstatico());
        String url = service.guardarImagen(archivo);
        assertTrue(url.endsWith(".webp"));
        assertTrue(Files.exists(carpeta.resolve(url.substring("/uploads/".length()))));
    }

    @Test
    void rechazaWebpSinDatosDeImagen() {
        ArchivoService service = new ArchivoService(carpeta.toString());
        byte[] incompleto = java.util.Arrays.copyOf(webpEstatico(), 30);
        escribirLittleEndian32(incompleto, 4, 22);
        MockMultipartFile archivo = new MockMultipartFile("archivo", "incompleto.webp", "image/webp", incompleto);
        assertThrows(IllegalArgumentException.class, () -> service.guardarImagen(archivo));
    }

    private byte[] png(int ancho, int alto) throws IOException {
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(imagen, "png", salida);
        return salida.toByteArray();
    }

    private byte[] webpEstatico() {
        byte[] data = new byte[48];
        System.arraycopy("RIFF".getBytes(java.nio.charset.StandardCharsets.US_ASCII), 0, data, 0, 4);
        escribirLittleEndian32(data, 4, 40);
        System.arraycopy("WEBPVP8X".getBytes(java.nio.charset.StandardCharsets.US_ASCII), 0, data, 8, 8);
        escribirLittleEndian32(data, 16, 10);
        data[24] = 19; data[27] = 19;
        System.arraycopy("VP8 ".getBytes(java.nio.charset.StandardCharsets.US_ASCII), 0, data, 30, 4);
        escribirLittleEndian32(data, 34, 10);
        data[41] = (byte) 0x9d; data[42] = 0x01; data[43] = 0x2a;
        data[44] = 20; data[46] = 20;
        return data;
    }

    private void escribirLittleEndian32(byte[] data, int offset, int value) {
        data[offset] = (byte) value;
        data[offset + 1] = (byte) (value >> 8);
        data[offset + 2] = (byte) (value >> 16);
        data[offset + 3] = (byte) (value >> 24);
    }
}
