import java.util.Scanner;

class Solution {
    public static double solution(Scanner scanner) {
        // 優先使用優化的 FastIO 進行讀取，如果發生異常則退回到使用 Scanner
        try {
            return processWithFastIO();
        } catch (Exception e) {
            // 異常處理：使用 Scanner 作為備用方案
            String inputLine = scanner.nextLine();
            String[] words = inputLine.split(" ");
            WordHashSet uniqueWords = new WordHashSet();
            for (String word : words) {
                uniqueWords.add(word);
            }
            return uniqueWords.size();
        }
    }

    private static double processWithFastIO() throws Exception {
        HyperLogLog hll = new HyperLogLog();
        FastIO fastReader = new FastIO();

        while (fastReader.hasMoreWordsInLine()) {
            int wordHash = fastReader.nextWordHash();
            if (wordHash != 0) { // 0 表示無效單詞或行尾
                hll.add(wordHash);
            }
        }

        return hll.estimate();
    }

    /**
     * 自定義的字串哈希集合，用於儲存不重複的字串
     * 使用鏈式哈希表實現，處理哈希衝突
     */
    static class WordHashSet {
        private static final int INITIAL_CAPACITY = 1 << 16; // 初始容量為 2^16
        private static final int HASH_MASK = INITIAL_CAPACITY - 1; // 用於哈希取模的遮罩
        private HashEntry[] hashTable = new HashEntry[INITIAL_CAPACITY];
        private int elementCount = 0;

        static class HashEntry {
            String word;
            HashEntry nextEntry;

            HashEntry(String word) {
                this.word = word;
            }
        }

        public void add(String word) {
            int hashIndex = word.hashCode() & HASH_MASK;

            if (hashTable[hashIndex] == null) {
                hashTable[hashIndex] = new HashEntry(word);
                elementCount++;
                return;
            }

            HashEntry current = hashTable[hashIndex];
            if (word.equals(current.word)) {
                return;
            }

            while (current.nextEntry != null) {
                current = current.nextEntry;
                if (word.equals(current.word)) {
                    return;
                }
            }

            current.nextEntry = new HashEntry(word);
            elementCount++;
        }

        public int size() {
            return elementCount;
        }
    }

    /**
     * 自定義的整數哈希集合，用於儲存不重複的整數哈希值
     * 使用開放尋址法（二次探測）處理哈希衝突
     */
    static class HashSet {
        private static final int INITIAL_CAPACITY = 1 << 18; // 初始容量為 2^18
        private static final int HASH_MASK = INITIAL_CAPACITY - 1; // 用於哈希取模的遮罩
        private int[] hashTable = new int[INITIAL_CAPACITY];
        private boolean[] isOccupied = new boolean[INITIAL_CAPACITY];
        private int elementCount = 0;

        public HashSet() {
            // 初始化哈希表，標記所有位置為未使用
            for (int i = 0; i < INITIAL_CAPACITY; i++) {
                hashTable[i] = 0;
                isOccupied[i] = false;
            }
        }

        public void add(int hashValue) {
            int hashIndex = (hashValue & HASH_MASK);
            int probeStep = 1 + (hashValue % (HASH_MASK - 1)); // 二次探測步長

            while (true) {
                if (!isOccupied[hashIndex]) {
                    // 找到空位，插入新值
                    hashTable[hashIndex] = hashValue;
                    isOccupied[hashIndex] = true;
                    elementCount++;
                    return;
                } else if (hashTable[hashIndex] == hashValue) {
                    // 值已存在，無需重複插入
                    return;
                }

                // 發生衝突，使用二次探測繼續查找
                hashIndex = (hashIndex + probeStep) & HASH_MASK;
            }
        }

        public int size() {
            return elementCount;
        }
    }

    /**
     * HyperLogLog 演算法實現
     * 使用 14 位元的暫存器，可達到約 0.81% 的標準誤差
     */
    static class HyperLogLog {
        private static final int REGISTER_COUNT = 1 << 14; // 16384 個暫存器
        private static final int REGISTER_MASK = REGISTER_COUNT - 1;
        private static final double ALPHA = 0.7213 / (1 + 1.079 / REGISTER_COUNT);
        private final byte[] registers = new byte[REGISTER_COUNT];

        public HyperLogLog() {
            // 初始化所有暫存器為 0
            for (int i = 0; i < REGISTER_COUNT; i++) {
                registers[i] = 0;
            }
        }

        public void add(int hash) {
            // 使用低 14 位元作為暫存器索引
            int registerIndex = hash & REGISTER_MASK;
            // 使用剩餘位元計算前導零的數量
            int leadingZeros = countLeadingZeros(hash >>> 14);
            // 更新對應暫存器的值
            registers[registerIndex] = (byte) Math.max(registers[registerIndex], leadingZeros);
        }

        public double estimate() {
            double sum = 0;
            int zeroCount = 0;

            // 計算調和平均數
            for (int i = 0; i < REGISTER_COUNT; i++) {
                if (registers[i] == 0) {
                    zeroCount++;
                }
                sum += 1.0 / (1 << registers[i]);
            }

            // 計算基數估計值
            double estimate = ALPHA * REGISTER_COUNT * REGISTER_COUNT / sum;

            // 小範圍修正
            if (estimate <= 2.5 * REGISTER_COUNT) {
                if (zeroCount > 0) {
                    estimate = REGISTER_COUNT * Math.log((double) REGISTER_COUNT / zeroCount);
                }
            }
            // 大範圍修正
            else if (estimate > (1L << 32) / 30.0) {
                estimate = -Math.pow(2, 32) * Math.log(1 - estimate / Math.pow(2, 32));
            }

            return Math.round(estimate);
        }

        private int countLeadingZeros(int value) {
            if (value == 0) return 32;
            int n = 0;
            if ((value & 0xFFFF0000) == 0) { n += 16; value <<= 16; }
            if ((value & 0xFF000000) == 0) { n += 8;  value <<= 8;  }
            if ((value & 0xF0000000) == 0) { n += 4;  value <<= 4;  }
            if ((value & 0xC0000000) == 0) { n += 2;  value <<= 2;  }
            if ((value & 0x80000000) == 0) { n += 1;  value <<= 1;  }
            return n;
        }
    }

    /**
     * 優化的快速輸入輸出類，專門用於高效讀取單詞
     * 使用緩衝區和位運算優化讀取效能
     */
    static class FastIO {
        private byte[] buffer = new byte[1 << 24]; // 16MB 緩衝區
        private int currentIndex;
        private int totalBytesRead;
        private boolean isEndOfLine = false;

        public FastIO() throws Exception {
            totalBytesRead = System.in.read(buffer);
            currentIndex = 0;
        }

        public boolean hasMoreWordsInLine() {
            return !isEndOfLine && currentIndex < totalBytesRead;
        }

        /**
         * 讀取下一個單詞並計算其哈希值
         * 使用質數 999999937 進行哈希計算，避免建立 String 物件
         */
        public int nextWordHash() throws Exception {
            int wordHash = 0;
            byte currentByte;

            // 跳過前導空格
            while ((currentByte = readNextByte()) == ' ')
                ;

            if (currentByte == '\n' || currentByte == '\r' || currentByte == -1) {
                isEndOfLine = true;
                return 0;
            }

            // 使用九位質數進行哈希計算
            final int HASH_PRIME = 999999937;
            do {
                wordHash = wordHash * HASH_PRIME + currentByte;
                currentByte = readNextByte();
            } while (currentByte != ' ' && currentByte != '\n' && currentByte != '\r' && currentByte != -1);

            if (currentByte < 0 || currentByte == '\n' || currentByte == '\r') {
                isEndOfLine = true;
            }

            return wordHash;
        }

        private byte readNextByte() throws Exception {
            if (currentIndex >= totalBytesRead) {
                totalBytesRead = System.in.read(buffer);
                currentIndex = 0;
                if (totalBytesRead == -1)
                    return -1;
            }
            return buffer[currentIndex++];
        }
    }
}