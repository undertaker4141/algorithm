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
        for (; character >= '0' && character <= '9'; character = fetchByte()) {
            value = value * 10 + (character - '0');
        }
        return value * multiplier;
    }

    // —— 高效輸出處理模組 ——
    static final int OUTPUT_BUFFER_CAPACITY = 1 << 20; // 輸出緩衝區大小 (1MB)
    static byte[] outputBuffer = new byte[OUTPUT_BUFFER_CAPACITY]; // 輸出緩衝區
    static int outputBufferPosition = 0; // 輸出緩衝區當前寫入位置

    static void appendInteger(int val) throws Exception {
        if (val == Integer.MIN_VALUE) {
            // 直接處理 Integer.MIN_VALUE 的情況
            String minIntStr = "-2147483648";
            // 檢查是否有足夠空間，若不足則先提交
            // 這裡假設字串不是很長，一次提交後空間足夠
            if (outputBufferPosition + minIntStr.length() > OUTPUT_BUFFER_CAPACITY) {
                commitOutput();
            }
            // 再次檢查，確保緩衝區足夠（理論上 commitOutput 後應該足夠）
            if (outputBufferPosition + minIntStr.length() > OUTPUT_BUFFER_CAPACITY) {
                // 如果還是不夠，表示字串太長或緩衝區太小，這是個問題
                // 在競賽環境下，通常假設緩衝區夠大
            }

            for (int k = 0; k < minIntStr.length(); ++k) {
                 // 逐字元添加，並在每次添加前檢查緩衝區是否已滿
                 if (outputBufferPosition >= OUTPUT_BUFFER_CAPACITY) {
                     commitOutput();
                 }
                 outputBuffer[outputBufferPosition++] = (byte)minIntStr.charAt(k);
            }
            return;
        }

        if (val == 0) {
            if (outputBufferPosition >= OUTPUT_BUFFER_CAPACITY) commitOutput();
            outputBuffer[outputBufferPosition++] = '0';
            return;
        }
        
        boolean isNegative = false;
        if (val < 0) {
            isNegative = true;
            val = -val; // 對於非 Integer.MIN_VALUE 的負數，轉換為正數是安全的
        }

        int tempVal = val;
        int numDigits = 0;
        // val 此時必為正數 (因為 0 和 MIN_VALUE 已處理)
        while(tempVal > 0) {
            tempVal /= 10;
            numDigits++;
        }
        
        int charsToAppend = numDigits + (isNegative ? 1 : 0);
        if (outputBufferPosition + charsToAppend > OUTPUT_BUFFER_CAPACITY) {
            commitOutput();
        }
        // 再次檢查，確保緩衝區足夠
        if (outputBufferPosition + charsToAppend > OUTPUT_BUFFER_CAPACITY) {
            // 錯誤處理或假設
        }

        if (isNegative) {
            outputBuffer[outputBufferPosition++] = '-';
        }

        int currentWritePos = outputBufferPosition + numDigits - 1;
        tempVal = val; // val 是正數
        while (tempVal > 0) {
            outputBuffer[currentWritePos--] = (byte) ('0' + (tempVal % 10));
            tempVal /= 10;
        }
        outputBufferPosition += numDigits;
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

    static boolean dominates(int[] l1, int[] l2, int D) {
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
        int i = left, j = mid + 1, k = left;
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

    static void mergeSortRecursive(int[][] arr, int[][] temp, int left, int right, int D) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSortRecursive(arr, temp, left, mid, D);
            mergeSortRecursive(arr, temp, mid + 1, right, D);
            merge(arr, temp, left, mid, right, D);
        }
    }

    static void sortListings(int[][] listings, int numElements, int D) {
        if (listings == null || numElements <= 1) return;
        int[][] temp = new int[numElements][]; // 輔助陣列，儲存參考
        mergeSortRecursive(listings, temp, 0, numElements - 1, D);
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

        // 優化：迭代式篩選非支配點
        int[][] nonDominatedReferences = new int[N][]; // 儲存指向 allListings 中非支配點的參考
        int nonDominatedCount = 0;

        for (int i = 0; i < N; i++) {
            int[] candidatePoint = allListings[i]; // 當前考慮的點 (參考)
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
                int newNdsWriteIndex = 0; // 用於重寫 nonDominatedReferences 陣列的指針
                for (int k = 0; k < nonDominatedCount; k++) {
                    // 如果 nonDominatedReferences[k] 不被 candidatePoint 支配，則保留它
                    if (!dominates(candidatePoint, nonDominatedReferences[k], D)) {
                        nonDominatedReferences[newNdsWriteIndex] = nonDominatedReferences[k]; // 複製參考
                        newNdsWriteIndex++;
                    }
                }
                // 將 candidatePoint (的參考) 加入到更新後的非支配集中
                nonDominatedReferences[newNdsWriteIndex] = candidatePoint;
                nonDominatedCount = newNdsWriteIndex + 1;
            }
        }

        // nonDominatedReferences[0...nonDominatedCount-1] 包含了所有非支配點的參考
        // 對這些參考指向的實際資料進行排序
        sortListings(nonDominatedReferences, nonDominatedCount, D);

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
