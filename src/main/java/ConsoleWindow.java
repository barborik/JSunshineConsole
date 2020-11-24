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
    int columns;
    int rows;
    private long window;
    ByteBuffer data;
    IntBuffer bmpWidth;
    IntBuffer bmpHeight;
    IntBuffer comp;
    char[] chars;
    Color[] colors;
    Color[] bgcolors;
    int keyPressedCode;

    public int getColumns() {
        return this.columns;
    }

    public int getRows() {
        return this.rows;
    }

    public long getWindow() {
        return window;
    }

    public char getChar(int column, int row) {
        return chars[row * this.columns + column];
    }

    public Color getColor(int column, int row) {
        return colors[row * this.columns + column];
    }

    public Color getBackgroundColor(int column, int row) {
        return bgcolors[row * this.columns + column];
    }

    public boolean windowUpdate() {
        chars = new char[chars.length];
        keyPressedCode = 0;
        GLFW.glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
                    keyPressedCode = key;
                }
            }
        });
        GLFW.glfwPollEvents();
        GLFW.glfwSwapBuffers(window);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        return !GLFW.glfwWindowShouldClose(window);
    }

    public void write(int column, int row, String text, Color color, Color background) {
        for (int i = 0; i < text.length(); i++) {
            try {
                chars[row * this.columns + column + i] = text.charAt(i);
                colors[row * this.columns + column + i] = color;
                bgcolors[row * this.columns + column + i] = background;
            } catch (Exception ignored) {
            }
            byte[] bytes = new byte[data.limit()];
            for (int j = 0; j < bytes.length; j += 4) {
                byte red = data.get(j);
                byte green = data.get(j + 1);
                byte blue = data.get(j + 2);
                if (red == 0 && green == 0 && blue == 0) {
                    red = (byte) background.getRed();
                    green = (byte) background.getGreen();
                    blue = (byte) background.getBlue();
                } else {
                    red = (byte) color.getRed();
                    green = (byte) color.getGreen();
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
        return keyPressedCode == keyCode;
    }

    public int getKeyCode() {
        return keyPressedCode;
    }

    public boolean keyPressed() {
        return keyPressedCode != 0;
    }

    public ConsoleWindow(int columns, int rows, String title) {
        this.columns = columns;
        this.rows = rows;
        chars = new char[columns * rows];
        colors = new Color[columns * rows];
        bgcolors = new Color[columns * rows];
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
        GLFW.glfwSwapInterval(0);

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
