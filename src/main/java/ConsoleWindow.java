import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConsoleWindow {
    private long window;
    private int keyPressedCode = 0;
    ByteBuffer data;
    IntBuffer bmpWidth;
    IntBuffer bmpHeight;
    IntBuffer comp;

    public long getWindow() {
        return window;
    }

    public boolean windowUpdate() {
        GLFW.glfwPollEvents();
        GLFW.glfwSwapBuffers(window);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        return !GLFW.glfwWindowShouldClose(window);
    }

    public void write(int column, int row, String text, Color color, Color background) {
        for (int i = 0; i < text.length(); i++) {
            byte[] bytes = new byte[data.limit()];
            for (int j = 0; j < bytes.length; j += 4) {
                byte red = data.get(j);
                byte green = data.get(j + 1);
                byte blue = data.get(j + 2);
                if (red == 0) {
                    red = (byte) background.getRed();
                } else if (red == -1) {
                    red = (byte) color.getRed();
                }
                if (green == 0) {
                    green = (byte) background.getGreen();
                } else if (green == -1) {
                    green = (byte) color.getGreen();
                }
                if (blue == 0) {
                    blue = (byte) background.getBlue();
                } else if (blue == -1) {
                    blue = (byte) color.getBlue();
                }
                bytes[j] = red;
                bytes[j + 1] = green;
                bytes[j + 2] = blue;
                bytes[j + 3] = data.get(j + 3);
            }
            ByteBuffer data2 = ByteBuffer.allocateDirect(bytes.length);
            data2.put(bytes);
            data2.flip();

            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 8 * text.charAt(i));
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 8, 12, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data2);

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0, 0);
            GL11.glVertex2f(column * 8 + i * 8, row * 12);

            GL11.glTexCoord2f(1, 0);
            GL11.glVertex2f(column * 8 + 8 + i * 8, row * 12);

            GL11.glTexCoord2f(1, 1);
            GL11.glVertex2f(column * 8 + 8 + i * 8, row * 12 + 12);

            GL11.glTexCoord2f(0, 1);
            GL11.glVertex2f(column * 8 + i * 8, row * 12 + 12);
            GL11.glEnd();

        }
    }

    public void write(int column, int row, String text, Color color) {
        this.write(column, row, text, color, Color.BLACK);
    }

    public void write(int column, int row, String text) {
        this.write(column, row, text, Color.white);
    }

    public boolean keyIsDown(int keyCode) {
        GLFW.glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
                keyPressedCode = key;
            }
        });

        int tempKeyCode = keyPressedCode;
        keyPressedCode = 0;
        return tempKeyCode == keyCode;
    }

    public int getKeyCode() {
        GLFW.glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
                    keyPressedCode = key;
                }
            }
        });

        int tempKeyCode = keyPressedCode;
        keyPressedCode = 0;
        return tempKeyCode;
    }

    public ConsoleWindow(int columns, int rows, String title) {

        if (!GLFW.glfwInit()) {
            System.err.println("ERROR: GLFW not initialized");
            return;
        }

        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
        this.window = GLFW.glfwCreateWindow(columns * 8, rows * 12, title, 0, 0);
        GLFW.glfwShowWindow(window);

        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        bmpWidth = BufferUtils.createIntBuffer(1);
        bmpHeight = BufferUtils.createIntBuffer(1);
        comp = BufferUtils.createIntBuffer(1);

        try {
            BufferedImage image = ImageIO.read(getClass().getResourceAsStream("dsc8x12.bmp"));
            Path fontPath = Files.createTempFile("tempFont", ".bmp");
            ImageIO.write(image, "BMP", new File(String.valueOf(fontPath)));
            data = STBImage.stbi_load(String.valueOf(fontPath), bmpWidth, bmpHeight, comp, 4);
        } catch (IOException e) {
            e.printStackTrace();
        }

        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glOrtho(0, columns * 8, rows * 12, 0, 1, -1);

        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, bmpWidth.get());
        bmpWidth.rewind();
    }
}
