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
        
        // 使用合併排序
        mergeSort(arr, 0, n - 1);
        
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
    
    // 合併排序實現
    private static void mergeSort(int[] arr, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);
            merge(arr, left, mid, right);
        }
    }
    
    private static void merge(int[] arr, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
        
        int[] L = new int[n1];
        int[] R = new int[n2];
        
        for (int i = 0; i < n1; i++) {
            L[i] = arr[left + i];
        }
        for (int j = 0; j < n2; j++) {
            R[j] = arr[mid + 1 + j];
        }
        
        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) {
                arr[k] = L[i];
                i++;
            } else {
                arr[k] = R[j];
                j++;
            }
            k++;
        }
        
        while (i < n1) {
            arr[k] = L[i];
            i++;
            k++;
        }
        
        while (j < n2) {
            arr[k] = R[j];
            j++;
            k++;
        }
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
