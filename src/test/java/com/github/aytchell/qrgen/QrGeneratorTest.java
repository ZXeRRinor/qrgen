package com.github.aytchell.qrgen;

import com.github.aytchell.qrgen.config.ErrorCorrectionLevel;
import com.github.aytchell.qrgen.config.ImageFileType;
import com.github.aytchell.qrgen.config.PixelStyle;
import com.google.zxing.WriterException;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class QrGeneratorTest {
    private static Stream<Integer> imageSizesForSizeTest() {
        final List<Integer> sizes = new ArrayList<>();
        sizes.add(100);
        sizes.add(300);
        sizes.add(650);
        sizes.add(900);
        return sizes.stream();
    }

    @Test
    void cloneIsPossible() {
        // we only check that a clone is produced (not Exception, no null return value)
        final QrGenerator gen = new QrGenerator();
        final QrGenerator clone = gen.clone();
        assertNotNull(clone);
    }

    @ParameterizedTest
    @MethodSource("imageSizesForSizeTest")
    void sizeIsRespected(Integer size) throws IOException, QrGenerationException, QrConfigurationException, WriterException {
        final QrGenerator gen = new QrGenerator()
                .withSize(size, size);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        gen.writeQrCodeToStream(byteArrayOutputStream, "Hello, World!");

        assert(byteArrayOutputStream.size() > 0);
        final BufferedImage img = ImageIO.read(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        assertEquals(size, img.getWidth());
        assertEquals(size, img.getHeight());
    }

    @Test
    void generatorCanCreatePngFiles() throws IOException, QrGenerationException, WriterException {
        final QrGenerator gen = new QrGenerator().ofType(ImageFileType.PNG);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        gen.writeQrCodeToStream(byteArrayOutputStream, "Hello PNG file");

        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        final ImageFileType type = TestUtilities.findOutImageTypeOfStream(inputStream);
        assertEquals(ImageFileType.PNG, type);
    }

    @Test
    void generatorCanCreateBmpFiles() throws IOException, QrGenerationException, WriterException {
        final QrGenerator gen = new QrGenerator().ofType(ImageFileType.BMP);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        gen.writeQrCodeToStream(byteArrayOutputStream, "Hello BMP file");

        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        final ImageFileType type = TestUtilities.findOutImageTypeOfStream(inputStream);
        assertEquals(ImageFileType.BMP, type);
    }

    @Test
    void generatorCanCreateGifFiles() throws IOException, QrGenerationException {
        final QrGenerator gen = new QrGenerator().ofType(ImageFileType.GIF);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        gen.writeQrCodeToStream(byteArrayOutputStream, "Hello GIF file");

        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        final ImageFileType type = TestUtilities.findOutImageTypeOfStream(inputStream);
        assertEquals(ImageFileType.GIF, type);
    }

    @Test
    void generatorCanCreateJpgFiles() throws IOException, QrGenerationException {
        final QrGenerator gen = new QrGenerator().ofType(ImageFileType.JPG);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        gen.writeQrCodeToStream(byteArrayOutputStream, "Hello JPG file");

        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        final ImageFileType type = TestUtilities.findOutImageTypeOfStream(inputStream);
        assertEquals(ImageFileType.JPG, type);
    }

    @Test
    void renderRoundCorners() throws QrGenerationException, IOException, QrConfigurationException {
        final QrGenerator gen = new QrGenerator()
                .ofType(ImageFileType.PNG)
                .withSize(400, 400)
                .withPixelStyle(PixelStyle.ROUND_CORNERS)
                .withMargin(2)
                .withErrorCorrection(ErrorCorrectionLevel.Q);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        gen.writeQrCodeToStream(byteArrayOutputStream, "https://github.com/aytchell/qrgen");
        assert(byteArrayOutputStream.size() > 0);
    }

    private static Stream<SizeAndLevel> maxSizesForLevels() {
        final List<SizeAndLevel> sizes = new ArrayList<>();
        sizes.add(new SizeAndLevel(QrGenerator.MAX_PAYLOAD_SIZE_FOR_L, ErrorCorrectionLevel.L));
        sizes.add(new SizeAndLevel(QrGenerator.MAX_PAYLOAD_SIZE_FOR_M, ErrorCorrectionLevel.M));
        sizes.add(new SizeAndLevel(QrGenerator.MAX_PAYLOAD_SIZE_FOR_Q, ErrorCorrectionLevel.Q));
        sizes.add(new SizeAndLevel(QrGenerator.MAX_PAYLOAD_SIZE_FOR_H, ErrorCorrectionLevel.H));
        return sizes.stream();
    }

    @ParameterizedTest
    @MethodSource("maxSizesForLevels")
    void maxPayloadsWork(SizeAndLevel sizeAndLevel) throws QrGenerationException, IOException {
        generatePayloadWithLvl(sizeAndLevel.size, sizeAndLevel.lvl);
    }

    @ParameterizedTest
    @MethodSource("maxSizesForLevels")
    void toobIgPayloadsFail(SizeAndLevel sizeAndLevel) throws IOException {
        try {
            generatePayloadWithLvl(sizeAndLevel.size + 1, sizeAndLevel.lvl);
            fail("QrGenerator should fail to generate code for too big payload");
        } catch (QrGenerationException e) {
            // as expected
        }
    }

    void generatePayloadWithLvl(int payloadSize, ErrorCorrectionLevel lvl) throws QrGenerationException, IOException {
        final QrGenerator gen = new QrGenerator()
                .withErrorCorrection(lvl);
        final String payload = Payload.getWithLength(payloadSize);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        gen.writeQrCodeToStream(byteArrayOutputStream, payload);
        assert(byteArrayOutputStream.size() > 0);
    }

    @Value
    private static class SizeAndLevel {
        int size;
        ErrorCorrectionLevel lvl;
    }
}
