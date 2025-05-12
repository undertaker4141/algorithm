// 沒有 import 語句

class Main {

    // --- 自訂雜湊映射節點類別 ---
    static class InnerMapNode {
        String key;
        int count;
        InnerMapNode next;
        InnerMapNode(String key, int count, InnerMapNode next) {
            this.key = key; this.count = count; this.next = next;
        }
    }

    static class CustomInnerMap {
        private InnerMapNode[] buckets;
        private int size;
        private static final int INITIAL_CAPACITY = 32;

        CustomInnerMap() {
            buckets = new InnerMapNode[INITIAL_CAPACITY];
            size = 0;
        }

        private int hash(String key) {
            int h = 0;
            for (int i = 0; i < key.length(); i++) h = 31 * h + key.charAt(i);
            return (h & 0x7FFFFFFF) % buckets.length;
        }

        public void increment(String key) {
            int index = hash(key);
            InnerMapNode current = buckets[index];
            while (current != null) {
                if (key.equals(current.key)) {
                    current.count++;
                    return;
                }
                current = current.next;
            }
            buckets[index] = new InnerMapNode(key, 1, buckets[index]);
            size++;
        }

        public int getSize() { return size; }
        public boolean isEmpty() { return size == 0; }

        public CoWord[] getAllEntries() {
            CoWord[] entries = new CoWord[size];
            int entryIdx = 0;
            for (InnerMapNode bucketNode : buckets) {
                InnerMapNode current = bucketNode;
                while (current != null) {
                    if (entryIdx < size) {
                        entries[entryIdx++] = new CoWord(current.key, current.count);
                    } else {
                        break;
                    }
                    current = current.next;
                }
            }
            if (entryIdx != size) {
                 CoWord[] actualEntries = new CoWord[entryIdx];
                 System.arraycopy(entries, 0, actualEntries, 0, entryIdx);
                 return actualEntries;
            }
            return entries;
        }
    }

    static class OuterMapNode {
        String key;
        CustomInnerMap value;
        OuterMapNode next;
        OuterMapNode(String key, CustomInnerMap value, OuterMapNode next) {
            this.key = key; this.value = value; this.next = next;
        }
    }

    static class CustomOuterMap {
        private OuterMapNode[] buckets;
        private int size;
        private static final int INITIAL_CAPACITY = 1 << 18; // 262,144

         CustomOuterMap() {
             buckets = new OuterMapNode[INITIAL_CAPACITY];
             size = 0;
        }

         private int hash(String key) {
            int h = 0;
            for (int i = 0; i < key.length(); i++) h = 31 * h + key.charAt(i);
            return (h & 0x7FFFFFFF) % buckets.length;
         }

        public CustomInnerMap get(String key) {
            int index = hash(key);
            OuterMapNode current = buckets[index];
            while (current != null) {
                if (key.equals(current.key)) return current.value;
                current = current.next;
            }
            return null;
        }

        public void put(String key, CustomInnerMap value) {
             int index = hash(key);
             buckets[index] = new OuterMapNode(key, value, buckets[index]);
             size++;
         }
    }

    static class CoWord {
        String word;
        int count;
        CoWord(String word, int count) { this.word = word; this.count = count; }
    }

    private static int compareCoWords(CoWord a, CoWord b) {
        if (a.count != b.count) return b.count - a.count;
        return a.word.compareTo(b.word);
    }

    private static void mergeSort(CoWord[] arr, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);
            merge(arr, left, mid, right);
        }
    }

    private static void merge(CoWord[] arr, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
        CoWord[] L = new CoWord[n1];
        CoWord[] R = new CoWord[n2];
        for (int i = 0; i < n1; ++i) L[i] = arr[left + i];
        for (int j = 0; j < n2; ++j) R[j] = arr[mid + 1 + j];
        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (compareCoWords(L[i], R[j]) <= 0) arr[k++] = L[i++];
            else arr[k++] = R[j++];
        }
        while (i < n1) arr[k++] = L[i++];
        while (j < n2) arr[k++] = R[j++];
    }

    // main 方法不再需要 throws Exception
    public static void main(String[] args) {
        FastReader reader = null;
        FastWriter writer = null;
        try {
            reader = new FastReader(); // 建構子可能拋出 RuntimeException (包裝後的)
            writer = new FastWriter(); // 建構子可能拋出 RuntimeException

            int N = reader.readInt();
            int M = reader.readInt();

            CustomOuterMap coOccurrences = new CustomOuterMap();

            for (int i = 0; i < N; i++) {
                String word1 = reader.readString();
                String word2 = reader.readString();
                addCoOccurrence(coOccurrences, word1, word2);
                addCoOccurrence(coOccurrences, word2, word1);
            }

            for (int i = 0; i < M; i++) {
                String queryWord = reader.readString();
                int K = reader.readInt();

                CustomInnerMap innerMap = coOccurrences.get(queryWord);

                if (innerMap == null || innerMap.isEmpty()) {
                     writer.write((byte) '\n');
                     continue;
                }

                CoWord[] results = innerMap.getAllEntries();
                if (results == null || results.length == 0) {
                    writer.write((byte)'\n');
                    continue;
                }

                mergeSort(results, 0, results.length - 1);

                int countToPrint = K < results.length ? K : results.length;
                for (int j = 0; j < countToPrint; j++) {
                    writer.print(results[j].word);
                    if (j < countToPrint - 1) {
                        writer.write((byte) ' ');
                    }
                }
                writer.write((byte) '\n');
            }
        } catch (RuntimeException e) {
            // 如果 Fast I/O 內部發生錯誤並拋出 RuntimeException，這裡可以捕獲
            // 在競賽中，通常讓程式直接崩潰即可，除非有特殊處理要求
            // System.err.println("A runtime error occurred: " + e.getMessage());
            // e.printStackTrace(); // 進行調試
            throw e; // 或者重新拋出，讓評測系統記錄錯誤
        } finally {
            if (writer != null) {
                try {
                    writer.flush(); // flush 仍然可能拋出 RuntimeException
                    writer.close(); // 如果實現了 AutoCloseable
                } catch (RuntimeException e) {
                    // System.err.println("Error flushing/closing writer: " + e.getMessage());
                    throw e; // 或者重新拋出
                }
            }
        }
    }

    static void addCoOccurrence(CustomOuterMap outerMap, String word1, String word2) {
        CustomInnerMap innerMap = outerMap.get(word1);
        if (innerMap == null) {
            innerMap = new CustomInnerMap();
            outerMap.put(word1, innerMap);
        }
        innerMap.increment(word2);
    }

     // --- FastReader 類別 ---
    static class FastReader {
        private final byte[] buffer = new byte[1 << 16];
        private int pos = 0, size = 0;

        // 建構子不再聲明 throws java.io.IOException
        // 而是在內部 try-catch 並 rethrow RuntimeException
        public FastReader() {
            try {
                fillBuffer();
            } catch (Exception e) { // 捕獲任何來自 System.in.read 的異常
                throw new RuntimeException("Failed to initialize FastReader", e);
            }
        }

        private void fillBuffer() {
            try {
                size = System.in.read(buffer);
                pos = 0;
            } catch (Exception e) { // 捕獲 System.in.read 的異常
                throw new RuntimeException("Failed to fill buffer in FastReader", e);
            }
        }

        private byte read() {
            try {
                if (pos >= size) {
                    fillBuffer();
                    if (size <= 0) return -1; // EOF or error after trying to fill
                }
                return buffer[pos++];
            } catch (Exception e) { // 捕獲來自 fillBuffer 或 buffer 操作的異常
                 throw new RuntimeException("Error during read operation in FastReader", e);
            }
        }

        public int readInt() {
            byte c = read();
            // 跳過非數字和非負號，並處理EOF
            while (c != -1 && (c < '0' || c > '9') && c != '-') {
                c = read();
            }
            if (c == -1) throw new RuntimeException("End of input while expecting integer (FastReader)");

            boolean neg = (c == '-');
            if (neg) {
                c = read();
                if (c == -1) throw new RuntimeException("End of input after negative sign (FastReader)");
            }
            // 確保負號後是數字
            if (c < '0' || c > '9') {
                throw new RuntimeException("Invalid integer format: non-digit after sign or at start (FastReader)");
            }

            int res = 0;
            while (c != -1 && c >= '0' && c <= '9') {
                res = res * 10 + (c - '0');
                c = read();
            }
            return neg ? -res : res;
        }

         public String readString() {
            byte c = read();
            while (c != -1 && c <= ' ') c = read(); // 跳過前導空白
            if (c == -1) throw new RuntimeException("End of input while expecting string (FastReader)");

            byte[] buf = new byte[64]; // 初始緩衝區
            int cnt = 0;
            while (c != -1 && c > ' ') { // 讀取直到空白或EOF
                if (cnt == buf.length) {
                     byte[] newBuf = new byte[buf.length * 2];
                     System.arraycopy(buf, 0, newBuf, 0, buf.length);
                     buf = newBuf;
                }
                buf[cnt++] = c;
                c = read();
            }
            return new String(buf, 0, cnt);
        }

        // 可選：如果 FastReader 也需要被關閉（例如，如果它管理的是檔案而不是 System.in）
        // public void close() { /* 如果有需要關閉的資源 */ }
    }

    // --- FastWriter 類別 ---
     static class FastWriter { // 可以選擇實現 AutoCloseable
        private final byte[] buffer = new byte[1 << 16];
        private int pos = 0;
        private final byte[] digits = new byte[20];

        // 建構子是空的，不需要初始化 I/O
        public FastWriter() {}

        public void write(byte b) {
            // flush() 內部會處理異常並包裝成 RuntimeException
            if (pos == buffer.length) flush();
            buffer[pos++] = b;
        }

        public void print(int x) {
             if (x == 0) { write((byte) '0'); return; }
             if (x == Integer.MIN_VALUE) { print("-2147483648"); return; }

             boolean negative = x < 0;
             if (negative) x = -x;

             int length = 0;
             while (x > 0) {
                 digits[length++] = (byte) (x % 10 + '0');
                 x /= 10;
             }
             if (negative) write((byte) '-');
             for (int i = length - 1; i >= 0; i--) write(digits[i]);
        }

         public void print(String s) {
             byte[] bytes = s.getBytes();
             for (byte b : bytes) write(b); // write 方法內部處理緩衝區滿
         }

        public void flush() {
            if (pos > 0) {
                try {
                    System.out.write(buffer, 0, pos);
                    pos = 0;
                } catch (Exception e) { // 捕獲來自 System.out.write 的任何異常
                    throw new RuntimeException("Failed to flush FastWriter buffer", e);
                }
            }
        }

        // 如果實現 AutoCloseable，需要 close 方法
        public void close() {
            flush(); // close 時確保 flush
        }
    }
}