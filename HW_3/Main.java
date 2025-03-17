import java.util.Scanner;

public class Main {
    private static final int MAX_WORDS = 10001;
    private static final int[] HASH_TABLE = new int[MAX_WORDS];
    private static final byte[] WORD_FLAGS = new byte[MAX_WORDS];
    private static final int MASK = 0x7fffffff;
    private static final int PRIME = 16777619;
    private static char[] buffer = new char[1024];
    private static int[] wordHashes = new int[1024];
    private static int wordCount;
    
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
            String doc1 = documents[i];
            extractWords(doc1);
            int[] hashes1 = new int[wordCount];
            System.arraycopy(wordHashes, 0, hashes1, 0, wordCount);
            int count1 = wordCount;
            
            for (int j = i + 1; j < n; j++) {
                if (calculateJaccard(hashes1, count1, documents[j]) > threshold) {
                    count++;
                }
            }
        }
        
        System.out.println(count);
    }
    
    private static void extractWords(String doc) {
        wordCount = 0;
        int wordStart = 0;
        int hash = 0;
        int len = doc.length();
        
        for (int i = 0; i < len; i++) {
            char c = doc.charAt(i);
            if (c == ' ' || i == len - 1) {
                if (i == len - 1 && c != ' ') {
                    hash = ((hash * PRIME) + c) & MASK;
                }
                if (hash != 0) {
                    wordHashes[wordCount++] = hash;
                }
                hash = 0;
                wordStart = i + 1;
            } else {
                hash = ((hash * PRIME) + c) & MASK;
            }
        }
    }
    
    private static double calculateJaccard(int[] hashes1, int count1, String doc2) {
        // 重置雜湊表
        int lastUsedHash = 0;
        
        // 處理第一個文件的雜湊值
        for (int i = 0; i < count1; i++) {
            int wordHash = hashes1[i];
            int hash = wordHash % MAX_WORDS;
            lastUsedHash = Math.max(lastUsedHash, hash);
            
            if (HASH_TABLE[hash] == wordHash) {
                WORD_FLAGS[hash] = 1;
                continue;
            }
            
            if (HASH_TABLE[hash] == 0) {
                HASH_TABLE[hash] = wordHash;
                WORD_FLAGS[hash] = 1;
                continue;
            }
            
            int newHash = hash;
            while (true) {
                if (++newHash >= MAX_WORDS) newHash = 0;
                lastUsedHash = Math.max(lastUsedHash, newHash);
                if (HASH_TABLE[newHash] == 0) {
                    HASH_TABLE[newHash] = wordHash;
                    WORD_FLAGS[newHash] = 1;
                    break;
                }
                if (HASH_TABLE[newHash] == wordHash) {
                    WORD_FLAGS[newHash] = 1;
                    break;
                }
                if (newHash == hash) break;
            }
        }
        
        // 處理第二個文件
        int uniqueCount = count1;
        int intersection = 0;
        int wordHash = 0;
        int len = doc2.length();
        
        for (int i = 0; i < len; i++) {
            char c = doc2.charAt(i);
            if (c == ' ' || i == len - 1) {
                if (i == len - 1 && c != ' ') {
                    wordHash = ((wordHash * PRIME) + c) & MASK;
                }
                if (wordHash != 0) {
                    int hash = wordHash % MAX_WORDS;
                    lastUsedHash = Math.max(lastUsedHash, hash);
                    
                    if (HASH_TABLE[hash] == wordHash) {
                        if (WORD_FLAGS[hash] == 1) {
                            WORD_FLAGS[hash] = 3;
                            intersection++;
                        }
                        wordHash = 0;
                        continue;
                    }
                    
                    if (HASH_TABLE[hash] == 0) {
                        uniqueCount++;
                        wordHash = 0;
                        continue;
                    }
                    
                    int newHash = hash;
                    boolean found = false;
                    while (true) {
                        if (++newHash >= MAX_WORDS) newHash = 0;
                        lastUsedHash = Math.max(lastUsedHash, newHash);
                        if (HASH_TABLE[newHash] == wordHash) {
                            if (WORD_FLAGS[newHash] == 1) {
                                WORD_FLAGS[newHash] = 3;
                                intersection++;
                            }
                            found = true;
                            break;
                        }
                        if (HASH_TABLE[newHash] == 0) {
                            uniqueCount++;
                            found = true;
                            break;
                        }
                        if (newHash == hash) break;
                    }
                    if (!found) uniqueCount++;
                }
                wordHash = 0;
            } else {
                wordHash = ((wordHash * PRIME) + c) & MASK;
            }
        }
        
        // 重置使用過的部分
        lastUsedHash = Math.min(lastUsedHash + 1, MAX_WORDS);
        for (int i = 0; i < lastUsedHash; i++) {
            HASH_TABLE[i] = 0;
            WORD_FLAGS[i] = 0;
        }
        
        return uniqueCount == 0 ? 0 : intersection / (double) uniqueCount;
    }
}

