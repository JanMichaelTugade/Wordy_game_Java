package Server_Java.WordyServerHelper;

import Server_Java.WordyImplementation;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class WordyServerMethods {

    public static void generateRandomLettersEvery8Seconds() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                WordyImplementation.randomLetters.add(generateRandomLetters());
            }
        };
        timer.scheduleAtFixedRate(task, 0, 8000);
    }

    public static String generateRandomLetters() {
        String VOWELS = "AEIOU";
        String CONSONANTS = "BCDFGHJKLMNPQRSTVWXYZ";

        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        int vowelCount = 0;
        char lastVowel = '\0';
        int lastVowelCount = 0;

        for (int i = 0; i < 17; i++) {
            if (vowelCount < 5 || vowelCount < 7 && random.nextBoolean()) {
                // Generate a vowel
                char c;
                do {
                    int index = random.nextInt(VOWELS.length());
                    c = VOWELS.charAt(index);
                } while (c == lastVowel && lastVowelCount >= 2);
                sb.append(c);
                vowelCount++;
                if (c == lastVowel) {
                    lastVowelCount++;
                } else {
                    lastVowel = c;
                    lastVowelCount = 1;
                }
            } else {
                // Generate a consonant
                int index = random.nextInt(CONSONANTS.length());
                char c = CONSONANTS.charAt(index);
                sb.append(c);
            }
        }
        return sb.toString();
    }
    public static String generateRandomRoomname() {
        String characters = "abcdefghijklmnopqrstuvwxyzABCSDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }
}
