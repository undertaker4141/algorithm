import java.util.Scanner;

public class Main {
    private static final int HASH_SIZE = 10007; // 保持原始大小

    // 自定義簡單位圖實現
    static class BitMap {
        private final long[] bits;
        private int count; // 計數設置的位數
        
        public BitMap(int size) {
            // 將位大小向上取整到64的倍數，除以64轉換為long數組長度
            this.bits = new long[(size + 63) / 64];
            this.count = 0;
        }
        
        // 設置指定位
        public void set(int index) {
            if (!get(index)) {
                int arrayIndex = index / 64;
                int bitIndex = index % 64;
                bits[arrayIndex] |= (1L << bitIndex);
                count++;
            }
        }
        
        // 檢查指定位是否設置
        public boolean get(int index) {
            int arrayIndex = index / 64;
            int bitIndex = index % 64;
            return (bits[arrayIndex] & (1L << bitIndex)) != 0;
        }
        
        // 計算與另一個位圖的交集大小
        public int intersectionSize(BitMap other) {
            int result = 0;
            for (int i = 0; i < bits.length; i++) {
                // 使用Long.bitCount計算兩個long相與後的1位數量
                if (i < other.bits.length) {
                    result += Long.bitCount(bits[i] & other.bits[i]);
                }
            }
            return result;
        }
        
        // 計算與另一個位圖的並集大小
        public int unionSize(BitMap other) {
            // 由於我們已知各自的1位數量及交集大小，可以直接計算並集大小
            return count + other.count - intersectionSize(other);
        }
        
        // 獲取設置的位數量
        public int getCount() {
            return count;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        double threshold = scanner.nextDouble();
        scanner.nextLine();

        int n = scanner.nextInt();
        scanner.nextLine();

        BitMap[] documentBitmaps = new BitMap[n];
        String[] wordTable = new String[HASH_SIZE];
        
        // 處理每個文檔
        for (int i = 0; i < n; i++) {
            String line = scanner.nextLine().toLowerCase();
            String[] words = line.split(" ");
            
            // 為每個文檔創建位圖
            documentBitmaps[i] = new BitMap(HASH_SIZE);
            
            for (String word : words) {
                if (word.isEmpty())
                    continue;
                
                // 計算哈希值
                int hash = hashFunction(word);
                hash = (hash % HASH_SIZE + HASH_SIZE) % HASH_SIZE;
                
                // 開放定址法處理碰撞
                while (wordTable[hash] != null && !wordTable[hash].equals(word)) {
                    hash = (hash + 1) % HASH_SIZE;
                }
                
                if (wordTable[hash] == null) {
                    wordTable[hash] = word;
                }
                
                // 在位圖中設置位
                documentBitmaps[i].set(hash);
            }
        }
        
        // 計算文檔對的相似度
        int count = 0;
        for (int i = 0; i < n - 1; i++) {
            BitMap docA = documentBitmaps[i];
            int sizeA = docA.getCount();
            
            if (sizeA == 0) {
                for (int j = i + 1; j < n; j++) {
                    if (documentBitmaps[j].getCount() == 0 && 1.0 > threshold) {
                        count++;
                    }
                }
                continue;
            }
            
            for (int j = i + 1; j < n; j++) {
                BitMap docB = documentBitmaps[j];
                int sizeB = docB.getCount();
                
                if (sizeB == 0) continue;
                
                // 文檔大小比例剪枝
                double sizeRatio = (double) Math.min(sizeA, sizeB) / Math.max(sizeA, sizeB);
                if (sizeRatio <= threshold) continue;
                
                // 計算交集
                int intersection = docA.intersectionSize(docB);
                
                // 計算相似度
                int union = sizeA + sizeB - intersection;
                double similarity = (double) intersection / union;
                
                if (similarity > threshold) {
                    count++;
                }
            }
        }
        
        System.out.println(count);
        scanner.close();
    }

    private static int hashFunction(String s) {
        int hash = 0;
        for (int i = 0; i < s.length(); i++) {
            hash = 31 * hash + s.charAt(i);
        }
        return hash;
    }
}