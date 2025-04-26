class Main {
    public static void main(String[] args) throws Exception {
        // 使用FastIO讀取輸入
        FastReader reader = new FastReader();
        int n = reader.readInt();
        
        // 讀取數組
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = reader.readInt();
        }
        
        // 使用MSD基數排序
        msdRadixSort(arr);
        
        // 輸出結果
        FastWriter writer = new FastWriter();
        for (int i = 0; i < n; i++) {
            writer.print(arr[i]);
            if (i < n - 1) {
                writer.write((byte) ' ');
            }
        }
        writer.write((byte) '\n');
        writer.flush();
    }
    
    // MSD基數排序實現
    private static void msdRadixSort(int[] arr) {
        if (arr == null || arr.length <= 1) return;
        
        // 找出最大值和最小值
        int max = arr[0];
        int min = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > max) max = arr[i];
            if (arr[i] < min) min = arr[i];
        }
        
        // 處理全部都是0的情況
        if (max == 0 && min == 0) return;
        
        // 分開處理負數和非負數
        int[] negatives = new int[arr.length];
        int[] nonNegatives = new int[arr.length];
        int negCount = 0;
        int nonNegCount = 0;
        
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] < 0) {
                negatives[negCount++] = -arr[i]; // 將負數轉為正數處理
            } else {
                nonNegatives[nonNegCount++] = arr[i];
            }
        }
        
        // 縮小數組大小
        int[] negArray = new int[negCount];
        int[] nonNegArray = new int[nonNegCount];
        System.arraycopy(negatives, 0, negArray, 0, negCount);
        System.arraycopy(nonNegatives, 0, nonNegArray, 0, nonNegCount);
        
        // 分別對負數和非負數進行MSD排序
        if (negCount > 0) msdSort(negArray, 0, negCount - 1, findMaxBit(Math.max(-min, max)));
        if (nonNegCount > 0) msdSort(nonNegArray, 0, nonNegCount - 1, findMaxBit(Math.max(-min, max)));
        
        // 合併結果 (負數倒序，非負數正序)
        int index = 0;
        for (int i = negCount - 1; i >= 0; i--) {
            arr[index++] = -negArray[i];
        }
        for (int i = 0; i < nonNegCount; i++) {
            arr[index++] = nonNegArray[i];
        }
    }
    
    // 找出最高有效位
    private static int findMaxBit(int num) {
        int bits = 0;
        while (num > 0) {
            bits++;
            num >>= 1;
        }
        return bits;
    }
    
    // MSD排序實現
    private static void msdSort(int[] arr, int left, int right, int bits) {
        if (left >= right || bits == 0) return;
        
        int pivot = 1 << (bits - 1);
        int i = left, j = right;
        
        // 按照當前位進行分組
        while (i <= j) {
            while (i <= j && (arr[i] & pivot) == 0) i++;
            while (i <= j && (arr[j] & pivot) != 0) j--;
            if (i < j) {
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
                i++;
                j--;
            }
        }
        
        // 對左半部分進行遞歸排序（該位為0的組）
        if (left < j) msdSort(arr, left, j, bits - 1);
        // 對右半部分進行遞歸排序（該位為1的組）
        if (i < right) msdSort(arr, i, right, bits - 1);
    }
}


// FastIO 實現
class FastReader {
    private byte[] buffer = new byte[1 << 16];
    private int pos = 0;
    private int size = 0;
    
    public FastReader() throws Exception {
        fillBuffer();
    }
    
    private void fillBuffer() throws Exception {
        size = System.in.read(buffer);
        pos = 0;
    }
    
    private byte read() throws Exception {
        if (pos >= size) {
            fillBuffer();
            if (size == -1) return -1;
        }
        return buffer[pos++];
    }
    
    public int readInt() throws Exception {
        byte c = read();
        while (c <= ' ') c = read();
        
        boolean neg = (c == '-');
        if (neg) c = read();
        
        int res = 0;
        while (c >= '0' && c <= '9') {
            res = res * 10 + (c - '0');
            c = read();
        }
        
        return neg ? -res : res;
    }
}

class FastWriter {
    private byte[] buffer = new byte[1 << 16];
    private int pos = 0;
    private final byte[] digits = new byte[20];
    
    public void write(byte b) {
        if (pos == buffer.length) flush();
        buffer[pos++] = b;
    }
    
    public void print(int x) {
        if (x == 0) {
            write((byte) '0');
            return;
        }
        
        boolean negative = false;
        if (x < 0) {
            negative = true;
            x = -x;
        }
        
        int length = 0;
        while (x > 0) {
            digits[length++] = (byte) (x % 10 + '0');
            x /= 10;
        }
        
        if (negative) write((byte) '-');
        
        for (int i = length - 1; i >= 0; i--) {
            write(digits[i]);
        }
    }
    
    public void flush() {
        if (pos > 0) {
            System.out.write(buffer, 0, pos);
            pos = 0;
        }
    }
}
