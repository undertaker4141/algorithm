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
        // 檢查第一個數字字元是否有效，避免空字串或只有'-'的情況導致錯誤
        if (character < '0' || character > '9') {
            // 根據題目特性，這裡可能表示一個錯誤的輸入格式或需要特殊處理
            // 但標準的 scanInteger 通常會繼續，如果沒有數字則 value 為 0
            // 為了競賽，假設輸入總是合法的數字序列
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

    // 用於 appendInteger 的靜態資源
    static final byte[] MIN_INT_BYTES = {'-','2','1','4','7','4','8','3','6','4','8'};
    static byte[] tempNumWriteBuffer = new byte[11]; // 最大10位數字 + 1個符號

    static void appendInteger(int val) throws Exception {
        if (val == Integer.MIN_VALUE) {
            if (outputBufferPosition + MIN_INT_BYTES.length > OUTPUT_BUFFER_CAPACITY) {
                commitOutput();
            }
            // 手動複製 MIN_INT_BYTES 到 outputBuffer
            for (int i = 0; i < MIN_INT_BYTES.length; i++) {
                 // 為了極致安全，在循環內部再次檢查，儘管外部檢查後理論上空間足夠
                 // 但若 MIN_INT_BYTES.length > OUTPUT_BUFFER_CAPACITY (不可能)，則會出問題
                 // 這裡假設 OUTPUT_BUFFER_CAPACITY 遠大於11
                if (outputBufferPosition >= OUTPUT_BUFFER_CAPACITY) commitOutput(); // 極端情況
                outputBuffer[outputBufferPosition++] = MIN_INT_BYTES[i];
            }
            return;
        }

        if (val == 0) {
            if (outputBufferPosition >= OUTPUT_BUFFER_CAPACITY) commitOutput();
            outputBuffer[outputBufferPosition++] = '0';
            return;
        }

        int charPos = 10; // 從 tempNumWriteBuffer 的末尾開始填充
        boolean isNegative = false;
        if (val < 0) {
            isNegative = true;
            val = -val; // 對於非 Integer.MIN_VALUE 的負數，轉換為正數是安全的
        }

        // 此時 val > 0
        while (val > 0) {
            tempNumWriteBuffer[charPos--] = (byte)('0' + (val % 10));
            val /= 10;
        }

        if (isNegative) {
            tempNumWriteBuffer[charPos--] = '-';
        }

        int len = 10 - charPos; // 數字字串的實際長度
        
        if (outputBufferPosition + len > OUTPUT_BUFFER_CAPACITY) {
            commitOutput();
        }

        // 手動將 tempNumWriteBuffer 中的有效數字部分複製到 outputBuffer
        // 有效部分是從 tempNumWriteBuffer[charPos + 1] 開始，長度為 len
        for (int i = 0; i < len; i++) {
            // 同樣，為了極致安全，在循環內部檢查
            if (outputBufferPosition >= OUTPUT_BUFFER_CAPACITY) commitOutput(); // 極端情況
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
            if (l1[i] > l2[i]) { // 如果 l1 在任何維度上比 l2 差，則 l1 不能支配 l2
                return false;
            }
            if (l1[i] < l2[i]) { // l1 在此維度上嚴格優於 l2
                strictlyBetterInOneDim = true;
            }
        }
        // 如果循環完成，代表 l1 在所有維度上都不劣於 l2。
        // 若至少在一個維度上嚴格更優，則 l1 支配 l2。
        return strictlyBetterInOneDim;
    }

    // ------ 自行實作的排序演算法 (合併排序) ------
    static int compareListingsLexicographically(int[] l1, int[] l2, int D) {
        for (int k = 0; k < D; k++) {
            if (l1[k] < l2[k]) return -1;
            if (l1[k] > l2[k]) return 1;
        }
        return 0; // 完全相同
    }

    static void merge(int[][] arr, int[][] temp, int left, int mid, int right, int D) {
        // 將 arr[left...right] 的內容複製到 temp 陣列的相應位置 (複製參考)
        for (int i = left; i <= right; i++) {
            temp[i] = arr[i];
        }

        int i = left;     // 指向 temp 左半部分的起始
        int j = mid + 1;  // 指向 temp 右半部分的起始
        int k = left;     // 指向 arr 中合併後存放的位置

        while (i <= mid && j <= right) {
            if (compareListingsLexicographically(temp[i], temp[j], D) <= 0) { // temp[i] <= temp[j]
                arr[k++] = temp[i++];
            } else {
                arr[k++] = temp[j++];
            }
        }
        while (i <= mid) arr[k++] = temp[i++]; // 複製左半邊剩餘的
        // 右半邊剩餘的無需處理，因為它們已在 arr 的正確位置 (如果 temp 是 arr 的副本)
        // 但如果 temp 是獨立的，則需要複製: while (j <= right) arr[k++] = temp[j++];
        // 鑑於我們的 temp 是 arr 的一部分的副本，這裡的邏輯是正確的，
        // 但更標準的 merge 會複製右邊剩餘的，以防萬一。
        // 為了安全和標準，補上右邊的複製 (儘管在此特定實現中可能非必需，但無害)
         while (j <= right) arr[k++] = temp[j++];

    }

    static void mergeSortRecursive(int[][] arr, int[][] temp, int left, int right, int D) {
        if (left < right) {
            int mid = left + (right - left) / 2; // 防溢出
            mergeSortRecursive(arr, temp, left, mid, D);
            mergeSortRecursive(arr, temp, mid + 1, right, D);
            // 只有在子問題確實發生了排序（即 left < mid 或 mid+1 < right）後才合併
            // 但標準做法是總是合併，除非 left >= right
            if (left < mid || (mid + 1) < right || (left == mid && mid+1 == right) ) { // 確保至少有兩個元素需要比較
                 merge(arr, temp, left, mid, right, D);
            } else if (left == mid && mid + 1 == right) { // 只有兩個元素的情況
                 if (compareListingsLexicographically(arr[left], arr[right], D) > 0) {
                    int[] swapTemp = arr[left];
                    arr[left] = arr[right];
                    arr[right] = swapTemp;
                 }
            }
            // 上述 merge 條件優化可能過於複雜且易錯，恢復標準 merge 呼叫
            // merge(arr, temp, left, mid, right, D);
        }
    }
    // 標準的 mergeSortRecursive 應該如下：
    static void standardMergeSortRecursive(int[][] arr, int[][] temp, int left, int right, int D) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            standardMergeSortRecursive(arr, temp, left, mid, D);
            standardMergeSortRecursive(arr, temp, mid + 1, right, D);
            merge(arr, temp, left, mid, right, D);
        }
    }


    static void sortListings(int[][] listings, int numElements, int D) {
        if (listings == null || numElements <= 1) return; // 無需排序
        // 輔助陣列 temp 的大小應該是 numElements
        // 並且它應該在 sortListings 函數內部創建，或者作為參數傳遞（如果要在外部管理）
        // 當前實現中，temp 在 mergeSortRecursive 內部創建，這是不對的，應該在 sortListings 創建一次
        int[][] temp = new int[numElements][]; // 輔助陣列，儲存參考
        standardMergeSortRecursive(listings, temp, 0, numElements - 1, D); // 使用標準版本
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
        // nonDominatedReferences 儲存指向 allListings 中非支配點的實際 int[] 參考
        int[][] nonDominatedReferences = new int[N][]; 
        int nonDominatedCount = 0;

        for (int i = 0; i < N; i++) {
            int[] candidatePoint = allListings[i]; 
            boolean isCandidateDominated = false;

            // 1. 檢查 candidatePoint 是否被已有的非支配點支配
            // 從後往前檢查可能稍微有利於快速找到支配者（如果有的話），但差異不大
            for (int k = 0; k < nonDominatedCount; k++) {
            // for (int k = nonDominatedCount - 1; k >= 0; k--) { // 從後往前試驗
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
        // 對這些參考指向的實際資料進行排序
        sortListings(nonDominatedReferences, nonDominatedCount, D);

        // 輸出結果
        for (int i = 0; i < nonDominatedCount; i++) {
            int[] pointToPrint = nonDominatedReferences[i]; // 獲取參考
            for (int k = 0; k < D; k++) {
                appendInteger(pointToPrint[k]);
                if (k < D - 1) {
                    appendCharacter(' ');
                }
            }
            appendCharacter('\n');
        }
        commitOutput(); // 提交最後的輸出緩衝區內容
    }
}
