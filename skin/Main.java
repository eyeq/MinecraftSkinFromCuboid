package skin;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        File in = new File("cuboid");
        if(!in.exists()) {
            in.mkdir();
            return;
        }
        File out = new File("skin");
        if(!out.exists()) {
            if(!out.mkdir()) {
                return;
            }
        }
        File[] list = in.listFiles();
        for(Type type : Type.values()) {
            File sub = new File(out, "/TYPE_" + type.name());
            if(!sub.exists()) {
                if(!sub.mkdir()) {
                    continue;
                }
            }
            for(File file : list) {
                String name = file.getName();
                if(name.endsWith(".png")) {
                    System.out.println(name);
                    buildSkin(file, new File(sub, name), type);
                }
            }
        }
    }

    public enum Type {
        SINGLE,
        UPPER,
        LOWER,
        DOUBLE,
        TRIPLE,
    }

    private enum Index {
        UPPER,
        LOWER,
        LEFT,
        RIGHT,
        FRONT,
        BACK,
        NUM
    }

    public static BufferedImage buildSkin(File in, File out, Type type) {
        BufferedImage src = null;
        try {
            src = ImageIO.read(in);
        } catch(Exception e) {
            e.printStackTrace();
        }

        int lenA = 0;
        for(int i = 0; i < src.getWidth(); i++) {
            if(src.getRGB(i, 0) >>> 24 != 0) {
                lenA = i;
                break;
            }
        }
        if(lenA == 0) {
            return null;
        }
        int lenB = src.getWidth() / 2 - lenA;
        int lenC = src.getHeight() - lenA;
        BufferedImage[] srces = new BufferedImage[6];
        srces[Index.UPPER.ordinal()] = src.getSubimage(lenA, 0, lenB, lenA);
        srces[Index.LOWER.ordinal()] = src.getSubimage(lenA + lenB, 0, lenB, lenA);
        srces[Index.RIGHT.ordinal()] = src.getSubimage(0, lenA, lenA, lenC);
        srces[Index.FRONT.ordinal()] = src.getSubimage(lenA, lenA, lenB, lenC);
        srces[Index.LEFT.ordinal()] = src.getSubimage(lenA + lenB, lenA, lenA, lenC);
        srces[Index.BACK.ordinal()] = src.getSubimage(lenA * 2 + lenB, lenA, lenB, lenC);

        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
        Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, image.getHeight(), image.getHeight());
        graphics.fill(rect);
        graphics.setPaintMode();

        drawHead(graphics, srces);
        drawArmsAndLegs(graphics, srces, type);
        drawBody(graphics, srces);

        try {
            ImageIO.write(image, "png", out);
        } catch(IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private static void drawHead(Graphics2D graphics, Image[] srces) {
        Image[] src8 = new Image[Index.NUM.ordinal()];
        for(int i = 0; i < src8.length; i++) {
            src8[i] = srces[i].getScaledInstance(8, 8, Image.SCALE_SMOOTH);
        }
        src8[Index.UPPER.ordinal()] = rotate(src8[Index.UPPER.ordinal()], 8, 8, 180);
        src8[Index.LOWER.ordinal()] = rotate(src8[Index.LOWER.ordinal()], 8, 8, 180);
        src8[Index.LEFT.ordinal()] = rotate(src8[Index.LEFT.ordinal()], 8, 8, 90);
        src8[Index.RIGHT.ordinal()] = rotate(src8[Index.RIGHT.ordinal()], 8, 8, -90);
        src8[Index.BACK.ordinal()] = reverseX(src8[Index.BACK.ordinal()], 8, 8);
        int dx = 0;
        int dy = 0;
        // Head HeadWear
        for(int i = 0; i < 2; i++) {
            graphics.drawImage(src8[Index.FRONT.ordinal()], dx + 8, dy + 0, null);
            graphics.drawImage(src8[Index.BACK.ordinal()], dx + 16, dy + 0, null);
            graphics.drawImage(src8[Index.RIGHT.ordinal()], dx + 0, dy + 8, null);
            graphics.drawImage(src8[Index.LOWER.ordinal()], dx + 8, dy + 8, null);
            graphics.drawImage(src8[Index.LEFT.ordinal()], dx + 16, dy + 8, null);
            graphics.drawImage(src8[Index.UPPER.ordinal()], dx + 24, dy + 8, null);
            dx = 32;
        }
    }

    private static void drawArmsAndLegs(Graphics2D graphics, Image[] srces, Type type) {
        Image[] src4 = new Image[Index.NUM.ordinal()];
        Image[] src4x6 = new Image[Index.NUM.ordinal()];
        Image[] src4x12 = new Image[Index.NUM.ordinal()];
        for(int i = 0; i < src4.length; i++) {
            src4[i] = srces[i].getScaledInstance(4, 4, Image.SCALE_SMOOTH);
            src4x6[i] = srces[i].getScaledInstance(4, 6, Image.SCALE_SMOOTH);
            src4x12[i] = srces[i].getScaledInstance(4, 12, Image.SCALE_SMOOTH);
        }
        int dx = 0;
        int dy = 0;
        // RightLeg RightArm RightLegPants RightArmSleeve
        // LeftLegPants LeftLeg LeftArm LeftArmSleeve
        for(int i = 0; i < 8; i++) {
            graphics.drawImage(rotate(src4[Index.UPPER.ordinal()], 4, 4, 90), dx + 4, dy + 16, null);
            graphics.drawImage(rotate(src4[Index.LOWER.ordinal()], 4, 4, 90), dx + 8, dy + 16, null);
            switch(type) {
            case SINGLE:
            case UPPER:
            case LOWER:
                graphics.drawImage(src4x12[Index.FRONT.ordinal()], dx + 0, dy + 20, null);
                graphics.drawImage(src4x12[Index.LEFT.ordinal()], dx + 4, dy + 20, null);
                graphics.drawImage(src4x12[Index.BACK.ordinal()], dx + 8, dy + 20, null);
                graphics.drawImage(src4x12[Index.RIGHT.ordinal()], dx + 12, dy + 20, null);
                break;
            }
            switch(type) {
            case UPPER:
            case DOUBLE:
                graphics.drawImage(src4x6[Index.FRONT.ordinal()], dx + 0, dy + 20, null);
                graphics.drawImage(src4x6[Index.LEFT.ordinal()], dx + 4, dy + 20, null);
                graphics.drawImage(src4x6[Index.BACK.ordinal()], dx + 8, dy + 20, null);
                graphics.drawImage(src4x6[Index.RIGHT.ordinal()], dx + 12, dy + 20, null);
                break;
            }
            switch(type) {
            case LOWER:
            case DOUBLE:
                graphics.drawImage(src4x6[Index.FRONT.ordinal()], dx + 0, dy + 26, null);
                graphics.drawImage(src4x6[Index.LEFT.ordinal()], dx + 4, dy + 26, null);
                graphics.drawImage(src4x6[Index.BACK.ordinal()], dx + 8, dy + 26, null);
                graphics.drawImage(src4x6[Index.RIGHT.ordinal()], dx + 12, dy + 26, null);
                break;
            }
            switch(type) {
            case TRIPLE:
                for(int j = 0; j < 3; j++) {
                    graphics.drawImage(src4[Index.FRONT.ordinal()], dx + 0, dy + 20 + 4 * j, null);
                    graphics.drawImage(src4[Index.LEFT.ordinal()], dx + 4, dy + 20 + 4 * j, null);
                    graphics.drawImage(src4[Index.BACK.ordinal()], dx + 8, dy + 20 + 4 * j, null);
                    graphics.drawImage(src4[Index.RIGHT.ordinal()], dx + 12, dy + 20 + 4 * j, null);
                }
                break;
            }
            switch(i) {
            case 0:
            case 2:
                dx = 40;
                break;
            case 3:
                src4[Index.UPPER.ordinal()] = reverseY(src4[Index.UPPER.ordinal()], 4, 4);
                src4[Index.LOWER.ordinal()] = reverseY(src4[Index.LOWER.ordinal()], 4, 4);
                Image temp;
                src4[Index.LEFT.ordinal()] = reverseX(src4[Index.LEFT.ordinal()], 4, 4);
                src4[Index.RIGHT.ordinal()] = reverseX(src4[Index.RIGHT.ordinal()], 4, 4);
                temp = src4[Index.FRONT.ordinal()];
                src4[Index.FRONT.ordinal()] = reverseX(src4[Index.BACK.ordinal()], 4, 4);
                src4[Index.BACK.ordinal()] = reverseX(temp, 4, 4);

                src4x6[Index.LEFT.ordinal()] = reverseX(src4x6[Index.LEFT.ordinal()], 4, 6);
                src4x6[Index.RIGHT.ordinal()] = reverseX(src4x6[Index.RIGHT.ordinal()], 4, 6);
                temp = src4x6[Index.FRONT.ordinal()];
                src4x6[Index.FRONT.ordinal()] = reverseX(src4x6[Index.BACK.ordinal()], 4, 6);
                src4x6[Index.BACK.ordinal()] = reverseX(temp, 4, 6);

                src4x12[Index.LEFT.ordinal()] = reverseX(src4x12[Index.LEFT.ordinal()], 4, 12);
                src4x12[Index.RIGHT.ordinal()] = reverseX(src4x12[Index.RIGHT.ordinal()], 4, 12);
                temp = src4x12[Index.FRONT.ordinal()];
                src4x12[Index.FRONT.ordinal()] = reverseX(src4x12[Index.BACK.ordinal()], 4, 12);
                src4x12[Index.BACK.ordinal()] = reverseX(temp, 4, 12);
            case 1:
                dx = 0;
                dy += 16;
                break;
            default:
                dx += 16;
                break;
            }
        }
    }

    private static void drawBody(Graphics2D graphics, Image[] srces) {
        Image[] src8 = new Image[Index.NUM.ordinal()];
        Image[] src4x12 = new Image[Index.NUM.ordinal()];
        for(int i = 0; i < src8.length; i++) {
            src8[i] = srces[i].getScaledInstance(8, 8, Image.SCALE_SMOOTH);
            src4x12[i] = srces[i].getScaledInstance(4, 12, Image.SCALE_SMOOTH);
        }
        int dx = 0;
        int dy = 0;
        // Body Jacket
        for(int i = 0; i < 2; i++) {
            graphics.drawImage(rotate(src8[Index.UPPER.ordinal()], 8, 8, 180).getSubimage(0, 2, 8, 4), dx + 20, dy + 16, null);
            graphics.drawImage(reverseY(src8[Index.LOWER.ordinal()], 8, 8).getSubimage(0, 2, 8, 4), dx + 28, dy + 16, null);

            graphics.drawImage(src4x12[Index.LEFT.ordinal()], dx + 16, dy + 20, null);
            graphics.drawImage(rotate(src8[Index.UPPER.ordinal()], 8, 8, 180).getSubimage(0, 6, 8, 2), dx + 20, dy + 20, null);
            graphics.drawImage(src8[Index.BACK.ordinal()], dx + 20, dy + 22, null);
            graphics.drawImage(createBufferedImage(src8[Index.LOWER.ordinal()]).getSubimage(0, 0, 8, 2), dx + 20, dy + 30, null);

            graphics.drawImage(src4x12[Index.RIGHT.ordinal()], dx + 28, dy + 20, null);
            graphics.drawImage(createBufferedImage(src8[Index.UPPER.ordinal()]).getSubimage(0, 6, 8, 2), dx + 32, dy + 20, null);
            graphics.drawImage(src8[Index.FRONT.ordinal()], dx + 32, dy + 22, null);
            graphics.drawImage(rotate(src8[Index.LOWER.ordinal()], 8, 8, 180).getSubimage(0, 0, 8, 2), dx + 32, dy + 30, null);
            dy = 16;
        }
    }

    public static BufferedImage createBufferedImage(Image image) {
        BufferedImage buff = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics g = buff.getGraphics();
        g.drawImage(image, 0, 0, null);
        return buff;
    }

    public static BufferedImage reverseX(Image image, int width, int height) {
        AffineTransform at = AffineTransform.getScaleInstance(-1d, 1d);
        at.translate(-width, 0);

        BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = ret.createGraphics();
        g.setTransform(at);
        g.drawImage(image, 0, 0, null);
        return ret;
    }

    public static BufferedImage reverseY(Image image, int width, int height) {
        AffineTransform at = AffineTransform.getScaleInstance(1d, -1d);
        at.translate(0, -height);

        BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = ret.createGraphics();
        g.setTransform(at);
        g.drawImage(image, 0, 0, null);
        return ret;
    }

    public static BufferedImage rotate(Image image, int width, int height, double angle) {
        AffineTransform at = new AffineTransform();
        at.rotate(angle * Math.PI / 180.0, width / 2, height / 2);

        BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = ret.createGraphics();
        g.setTransform(at);
        g.drawImage(image, 0, 0, null);
        return ret;
    }
}
