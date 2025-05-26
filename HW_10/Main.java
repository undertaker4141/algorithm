class Main {
    // —— 高效輸入處理模組 ——
    static final int INPUT_BUFFER_CAPACITY = 1 << 20; // 輸入緩衝區大小 (1MB)
    static byte[] inputBuffer = new byte[INPUT_BUFFER_CAPACITY]; // 輸入緩衝區
    static int inputBufferPosition = 0, inputBufferLength = 0; // 輸入緩衝區當前位置及已讀取長度

    static int fetchByte() throws Exception {
        if (inputBufferPosition >= inputBufferLength) {
            inputBufferLength = System.in.read(inputBuffer);
            inputBufferPosition = 0;
            if (inputBufferLength <= 0) return -1;
        }
        return inputBuffer[inputBufferPosition++] & 0xFF;
    }

    static int scanInteger() throws Exception {
        int character, value = 0, multiplier = 1;
        do {
            character = fetchByte();
        } while (character != -1 && character != '-' && (character < '0' || character > '9'));

        if (character == '-') {
            multiplier = -1;
            character = fetchByte();
        }
        // 假設輸入總是合法的數字序列
        for (; character >= '0' && character <= '9'; character = fetchByte()) {
            value = value * 10 + (character - '0');
        }
        return value * multiplier;
    }

    // —— 高效輸出處理模組 ——
    static final int OUTPUT_BUFFER_CAPACITY = 1 << 20; // 輸出緩衝區大小 (1MB)
    static byte[] outputBuffer = new byte[OUTPUT_BUFFER_CAPACITY]; // 輸出緩衝區
    static int outputBufferPosition = 0; // 輸出緩衝區當前寫入位置

    static final byte[] MIN_INT_BYTES = {'-','2','1','4','7','4','8','3','6','4','8'};
    static byte[] tempNumWriteBuffer = new byte[11]; // 最大10位數字 + 1個符號

    static void appendInteger(int val) throws Exception {
        if (val == Integer.MIN_VALUE) {
            if (outputBufferPosition + MIN_INT_BYTES.length > OUTPUT_BUFFER_CAPACITY) {
                commitOutput();
            }
            for (int i = 0; i < MIN_INT_BYTES.length; i++) {
                if (outputBufferPosition >= OUTPUT_BUFFER_CAPACITY) commitOutput();
                outputBuffer[outputBufferPosition++] = MIN_INT_BYTES[i];
            }
            return;
        }

        if (val == 0) {
            if (outputBufferPosition >= OUTPUT_BUFFER_CAPACITY) commitOutput();
            outputBuffer[outputBufferPosition++] = '0';
            return;
        }

        int charPos = 10; 
        boolean isNegative = false;
        if (val < 0) {
            isNegative = true;
            val = -val; 
        }

        while (val > 0) {
            tempNumWriteBuffer[charPos--] = (byte)('0' + (val % 10));
            val /= 10;
        }

        if (isNegative) {
            tempNumWriteBuffer[charPos--] = '-';
        }

        int len = 10 - charPos; 
        
        if (outputBufferPosition + len > OUTPUT_BUFFER_CAPACITY) {
            commitOutput();
        }

        for (int i = 0; i < len; i++) {
            if (outputBufferPosition >= OUTPUT_BUFFER_CAPACITY) commitOutput(); 
            outputBuffer[outputBufferPosition++] = tempNumWriteBuffer[charPos + 1 + i];
        }
    }

    static void appendCharacter(char ch) throws Exception {
        if (outputBufferPosition >= OUTPUT_BUFFER_CAPACITY) {
            commitOutput();
        }
        outputBuffer[outputBufferPosition++] = (byte) ch;
    }

    static void commitOutput() throws Exception {
        if (outputBufferPosition > 0) {
            System.out.write(outputBuffer, 0, outputBufferPosition);
            outputBufferPosition = 0;
        }
    }

    static boolean dominates(int[] l1, int[] l2, int D) { // l1 是否支配 l2
        boolean strictlyBetterInOneDim = false;
        for (int i = 0; i < D; i++) {
            if (l1[i] > l2[i]) { 
                return false;
            }
            if (l1[i] < l2[i]) { 
                strictlyBetterInOneDim = true;
            }
        }
        return strictlyBetterInOneDim;
    }

    // ------ 自行實作的排序演算法 (合併排序) ------
    static int compareListingsLexicographically(int[] l1, int[] l2, int D) {
        for (int k = 0; k < D; k++) {
            if (l1[k] < l2[k]) return -1;
            if (l1[k] > l2[k]) return 1;
        }
        return 0; 
    }

    static void merge(int[][] arr, int[][] temp, int left, int mid, int right, int D) {
        for (int i = left; i <= right; i++) {
            temp[i] = arr[i]; // 複製參考
        }

        int i = left;     
        int j = mid + 1;  
        int k = left;     

        while (i <= mid && j <= right) {
            if (compareListingsLexicographically(temp[i], temp[j], D) <= 0) { 
                arr[k++] = temp[i++];
            } else {
                arr[k++] = temp[j++];
            }
        }
        while (i <= mid) arr[k++] = temp[i++]; 
        while (j <= right) arr[k++] = temp[j++];
    }
    
    static void standardMergeSortRecursive(int[][] arr, int[][] temp, int left, int right, int D) {
        if (left < right) {
            int mid = left + (right - left) / 2; // 防溢出
            standardMergeSortRecursive(arr, temp, left, mid, D);
            standardMergeSortRecursive(arr, temp, mid + 1, right, D);
            merge(arr, temp, left, mid, right, D);
        }
    }

    static void sortListings(int[][] listings, int numElements, int D) {
        if (listings == null || numElements <= 1) return; 
        int[][] temp = new int[numElements][]; 
        standardMergeSortRecursive(listings, temp, 0, numElements - 1, D); 
    }

    // ------ 主邏輯 ------
    public static void main(String[] args) throws Exception {
        int N = scanInteger();
        int D = scanInteger();

        int[][] allListings = new int[N][D];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < D; j++) {
                allListings[i][j] = scanInteger();
            }
        }

        // 新增步驟：對所有讀取的資料進行初始排序
        if (N > 0) { // 只有在有資料時才排序
            sortListings(allListings, N, D);
        }

        // 迭代式篩選非支配點
        // nonDominatedReferences 儲存指向 allListings 中非支配點的實際 int[] 參考
        // 由於 allListings 本身已經排序，這裡的 nonDominatedReferences 陣列將會引用已排序的 allListings 中的元素
        int[][] nonDominatedReferences = new int[N][]; 
        int nonDominatedCount = 0;

        // candidatePoint 將從已排序的 allListings 中選取
        for (int i = 0; i < N; i++) {
            int[] candidatePoint = allListings[i]; 
            boolean isCandidateDominated = false;

            // 1. 檢查 candidatePoint 是否被已有的非支配點支配
            for (int k = 0; k < nonDominatedCount; k++) {
                if (dominates(nonDominatedReferences[k], candidatePoint, D)) {
                    isCandidateDominated = true;
                    break;
                }
            }

            if (!isCandidateDominated) {
                // candidatePoint 未被支配，將其加入非支配集，並移除被它支配的點
                int newNdsWriteIndex = 0; 
                for (int k = 0; k < nonDominatedCount; k++) {
                    if (!dominates(candidatePoint, nonDominatedReferences[k], D)) {
                        nonDominatedReferences[newNdsWriteIndex++] = nonDominatedReferences[k]; 
                    }
                }
                nonDominatedReferences[newNdsWriteIndex] = candidatePoint; // 加入新的非支配點
                nonDominatedCount = newNdsWriteIndex + 1;
            }
        }

        // nonDominatedReferences[0...nonDominatedCount-1] 包含了所有非支配點的參考
        // 由於 candidatePoint 是從已排序的 allListings 中選取的，
        // 並且 nonDominatedReferences 中的點在被加入時也是非支配的，
        // nonDominatedReferences 本身可能不完全按字典序排列（因為移除和加入的順序問題）。
        // 因此，最終仍需要對 nonDominatedReferences 進行排序以確保輸出順序。
        if (nonDominatedCount > 0) { // 只有在有非支配點時才排序
             sortListings(nonDominatedReferences, nonDominatedCount, D);
        }

        // 輸出結果
        for (int i = 0; i < nonDominatedCount; i++) {
            int[] pointToPrint = nonDominatedReferences[i]; 
            for (int k = 0; k < D; k++) {
                appendInteger(pointToPrint[k]);
                if (k < D - 1) {
                    appendCharacter(' ');
                }
            }
            appendCharacter('\n');
        }
        commitOutput(); 
    }
}
