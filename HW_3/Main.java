import java.util.Scanner;

public class Main {
    private static final int MAX_WORDS = 10001;
    private static final int[] HASH_TABLE = new int[MAX_WORDS];
    private static final boolean[] IS_WORD1 = new boolean[MAX_WORDS];
    private static final boolean[] IS_WORD2 = new boolean[MAX_WORDS];
    private static final int MASK = 0x7fffffff;
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        double threshold = scanner.nextDouble();
        int n = scanner.nextInt();
        scanner.nextLine();
        
        String[] documents = new String[n];
        for (int i = 0; i < n; i++) {
            documents[i] = scanner.nextLine().toLowerCase();
        }
        
        int count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (calculateJaccard(documents[i], documents[j]) > threshold) {
                    count++;
                }
            }
        }
        
        System.out.println(count);
    }
    
    private static void resetArrays(int size) {
        for (int i = 0; i < size; i++) {
            HASH_TABLE[i] = 0;
            IS_WORD1[i] = false;
            IS_WORD2[i] = false;
        }
    }
    
    private static double calculateJaccard(String doc1, String doc2) {
        int maxHash = 1;
        int uniqueCount = 0;
        int intersection = 0;
        
        // 處理第一個文件
        int wordHash = 0;
        int len1 = doc1.length();
        for (int i = 0; i < len1; i++) {
            char c = doc1.charAt(i);
            if (c == ' ' || i == len1 - 1) {
                if (i == len1 - 1 && c != ' ') {
                    wordHash = ((wordHash * 31) + c) & MASK;
                }
                if (wordHash != 0) {
                    int hash = wordHash % MAX_WORDS;
                    maxHash = Math.max(maxHash, hash + 1);
                    if (HASH_TABLE[hash] == wordHash) {
                        IS_WORD1[hash] = true;
                    } else if (HASH_TABLE[hash] == 0) {
                        HASH_TABLE[hash] = wordHash;
                        IS_WORD1[hash] = true;
                        uniqueCount++;
                    } else {
                        int probe = 1;
                        int newHash = hash;
                        while (true) {
                            newHash = (hash + probe) % MAX_WORDS;
                            maxHash = Math.max(maxHash, newHash + 1);
                            if (HASH_TABLE[newHash] == 0) {
                                HASH_TABLE[newHash] = wordHash;
                                IS_WORD1[newHash] = true;
                                uniqueCount++;
                                break;
                            } else if (HASH_TABLE[newHash] == wordHash) {
                                IS_WORD1[newHash] = true;
                                break;
                            }
                            probe++;
                            if (probe >= MAX_WORDS) break;
                        }
                    }
                }
                wordHash = 0;
            } else {
                wordHash = ((wordHash * 31) + c) & MASK;
            }
        }
        
        // 處理第二個文件
        wordHash = 0;
        int len2 = doc2.length();
        for (int i = 0; i < len2; i++) {
            char c = doc2.charAt(i);
            if (c == ' ' || i == len2 - 1) {
                if (i == len2 - 1 && c != ' ') {
                    wordHash = ((wordHash * 31) + c) & MASK;
                }
                if (wordHash != 0) {
                    int hash = wordHash % MAX_WORDS;
                    maxHash = Math.max(maxHash, hash + 1);
                    if (HASH_TABLE[hash] == wordHash) {
                        if (!IS_WORD2[hash]) {
                            IS_WORD2[hash] = true;
                            if (IS_WORD1[hash]) intersection++;
                        }
                    } else if (HASH_TABLE[hash] == 0) {
                        HASH_TABLE[hash] = wordHash;
                        IS_WORD2[hash] = true;
                        uniqueCount++;
                    } else {
                        int probe = 1;
                        int newHash = hash;
                        boolean found = false;
                        while (true) {
                            newHash = (hash + probe) % MAX_WORDS;
                            maxHash = Math.max(maxHash, newHash + 1);
                            if (HASH_TABLE[newHash] == wordHash) {
                                if (!IS_WORD2[newHash]) {
                                    IS_WORD2[newHash] = true;
                                    if (IS_WORD1[newHash]) intersection++;
                                }
                                found = true;
                                break;
                            } else if (HASH_TABLE[newHash] == 0) {
                                HASH_TABLE[newHash] = wordHash;
                                IS_WORD2[newHash] = true;
                                uniqueCount++;
                                found = true;
                                break;
                            }
                            probe++;
                            if (probe >= MAX_WORDS) break;
                        }
                        if (!found) uniqueCount++;
                    }
                }
                wordHash = 0;
            } else {
                wordHash = ((wordHash * 31) + c) & MASK;
            }
        }
        
        // 重置陣列（只重置使用過的部分）
        resetArrays(maxHash);
        
        return uniqueCount == 0 ? 0 : intersection / (double) uniqueCount;
    }
}
