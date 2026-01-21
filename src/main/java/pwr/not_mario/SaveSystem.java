package pwr.not_mario;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

public class SaveSystem {
    private static final String SAVE_FILE = "save.txt";

    public static int loadProgress() {
        try {
            File file = new File(SAVE_FILE);
            if (!file.exists()) {
                return 1;
            }
            Scanner scanner = new Scanner(file);
            if (scanner.hasNextInt()) {
                int level = scanner.nextInt();
                scanner.close();
                return level;
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }


    public static void saveProgress(int levelUnlocked) {
        int currentMax = loadProgress();

        if (levelUnlocked > currentMax) {
            try {
                PrintWriter writer = new PrintWriter(SAVE_FILE);
                writer.print(levelUnlocked);
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
