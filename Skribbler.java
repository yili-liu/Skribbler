import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.awt.image.*;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;

import javax.imageio.ImageIO;

public class Skribbler {

    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Welcome to Skribbler");
        System.out.println("Simply enter the word (or image link) to be drawn and press enter to exit");

        try {
            String s = br.readLine();
            if (s.length() != 0) {
                if (s.startsWith("https")) {
                    BufferedImage img = ImageIO.read(new URL(s));
                    System.out.println("Url to draw: " + s);
                    Thread.sleep(2000);
                    
                    drawByLines(img);
                } else {
                    final String API_KEY = getKey();
                    s = s.replaceAll(" ", "+");
                    String res = getHttpResponse("https://pixabay.com/api/?key=" + API_KEY + "&q=" + s + "&image_type=photo");
    
                    LinkedList<String> imgUrls = parseJson(res, "webformatURL");
    
                    String urlToDraw = "";
                    BufferedImage img = null;
                    s = "r";
                    while (!imgUrls.isEmpty() && s.length() != 0) {
                        if (s.equals("r")) {
                            urlToDraw = imgUrls.pop();
                            img = ImageIO.read(new URL(urlToDraw));
                        }
    
                        System.out.println("Url to draw: " + urlToDraw);
                        Thread.sleep(2000);
                        drawByLines(img);
    
                        s = br.readLine();
                    }
                }

                br.close();
                System.out.println("Exiting Skribbler");
            }

        } catch (IOException e) {
            System.out.println("An io error has occurred");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("A sleep error has occurred");
            e.printStackTrace();
        }
    }

    private static void drawByLines(BufferedImage img) {
        final int WIDTH = img.getWidth();
        final int HEIGHT = img.getHeight();
        final int X_OFFSET = 500;
        final int Y_OFFSET = 280;
        final int SIDE = 4;

        int[][] toDot = new int[WIDTH / SIDE][HEIGHT / SIDE];
        int[] allRgb = new int[toDot.length * toDot[0].length];

        for (int i = 0; i < toDot.length; i++) {
            for (int j = 0; j < toDot[0].length; j++) {
                int sum = 0;
                for (int k = i * SIDE; k < i * SIDE + SIDE; k++) {
                    for (int l = j * SIDE; l < j * SIDE + SIDE; l++) {
                        int rgb = img.getRGB(k, l);
                        sum += (rgb >> 16) & 255;
                        sum += (rgb >> 8) & 255;
                        sum += (rgb) & 255;
                    }
                }

                toDot[i][j] = sum / SIDE / SIDE / 3;
                allRgb[i * toDot[0].length + j] = toDot[i][j];
            }
        }
        Arrays.sort(allRgb);

        for (int i = 0; i < toDot.length; i++) {
            int px = -1;
            int py = -1;
            for (int j = 0; j < toDot[0].length; j++) {
                if (toDot[i][j] < allRgb[allRgb.length / 3]) {
                    if (px == -1) {
                        px = X_OFFSET + i * SIDE;
                        py = Y_OFFSET + j * SIDE;
                    } else if (j == toDot[0].length - 1 || toDot[i][j + 1] >= allRgb[allRgb.length / 3]) {
                        if (!line(px, py, X_OFFSET + i * SIDE, Y_OFFSET + j * SIDE)) {
                            System.out.println("A robot error has occurred");
                            return;
                        }
                        px = -1;
                        py = -1;
                    }
                }
            }
        }

        System.out.println("Done drawing by lines");
    }

    private static boolean line(int x1, int y1, int x2, int y2) {
        try {
            Robot robot = new Robot();
            robot.mouseMove(x1, y1);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            Thread.sleep(2);
            robot.mouseMove(x2, y2);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            return true;
        } catch (AWTException e) {
            System.out.println("Cannot create java robot");
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            System.out.println("A sleep error has occurred");
            e.printStackTrace();
            return false;
        }
    }
    
    private static LinkedList<String> parseJson(String res, String key) {
        LinkedList<String> values = new LinkedList<String>();
        while (true) {
            int i = res.indexOf(key);
            if (i == -1)
                break;
            res = res.substring(i + key.length());
            String value = res.substring(0, res.indexOf("\","));
            value = value.substring(value.lastIndexOf("\"") + 1);
            values.add(value);
        }
        return values;
    }

    private static String getKey() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("api_key.txt")));
            String s = br.readLine();
            br.close();
            return s;
        } catch (IOException e) {
            return "";
        }
    }

    private static String getHttpResponse(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");

            InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String s = "";
            String line = "";
            while (line != null) {
                s += line;
                line = br.readLine();
            }

            br.close();
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}