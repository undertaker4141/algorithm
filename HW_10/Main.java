class Main {
    // —— 高效輸入處理模組 ——
    static final int INPUT_BUFFER_CAPACITY = 1 << 20; // 輸入緩衝區大小 (1MB)
    static byte[] inputBuffer = new byte[INPUT_BUFFER_CAPACITY]; // 輸入緩衝區
    static int inputBufferPosition = 0, inputBufferLength = 0; // 輸入緩衝區當前位置及已讀取長度

    // 從緩衝區讀取一個字節
    static int fetchByte() throws Exception {
        if (inputBufferPosition >= inputBufferLength) { // 如果緩衝區已空
            inputBufferLength = System.in.read(inputBuffer); // 從標準輸入讀取數據到緩衝區
            inputBufferPosition = 0; // 重置讀取位置
            if (inputBufferLength <= 0) return -1; // 輸入流結束
        }
        return inputBuffer[inputBufferPosition++] & 0xFF; // 返回讀取的字節 (並轉為無符號)
    }

    // 從輸入流讀取一個整數
    static int scanInteger() throws Exception {
        int character, value = 0, multiplier = 1; // character: 當前讀取字元, value: 數值, multiplier: 正負號
        // 跳過非數字和非負號的字元
        do {
            character = fetchByte();
        } while (character != -1 && character != '-' && (character < '0' || character > '9'));

        if (character == '-') { // 處理負號
            multiplier = -1;
            character = fetchByte(); // 讀取下一個字元
        }
        // 構建數字
        for (; character >= '0' && character <= '9'; character = fetchByte()) {
            value = value * 10 + (character - '0');
        }
        return value * multiplier;
    }

    // —— 高效輸出處理模組 ——
    static final int OUTPUT_BUFFER_CAPACITY = 1 << 20; // 輸出緩衝區大小 (1MB)
    static byte[] outputBuffer = new byte[OUTPUT_BUFFER_CAPACITY]; // 輸出緩衝區
    static int outputBufferPosition = 0; // 輸出緩衝區當前寫入位置

    // 將整數添加到輸出緩衝區
    static void appendInteger(int val) throws Exception { // 可能拋出 Exception 是因為 commitOutput
        if (val == 0) { // 單獨處理 0
            if (outputBufferPosition >= OUTPUT_BUFFER_CAPACITY) {
                commitOutput();
            }
            outputBuffer[outputBufferPosition++] = '0';
            return;
        }
        
        if (val < 0) { // 處理負數
            if (outputBufferPosition >= OUTPUT_BUFFER_CAPACITY) {
                commitOutput();
            }
            outputBuffer[outputBufferPosition++] = '-';
            val = -val; // 轉為正數處理 (注意：Integer.MIN_VALUE 直接取負會溢出，但題目數值範圍通常不會這麼極端)
        }

        int tempVal = val;
        int numDigits = 0;
        if (val == 0) numDigits = 1; // 0 是一位數
        else {
            while(tempVal > 0) {
                tempVal /= 10;
                numDigits++;
            }
        }
        
        // 檢查是否有足夠空間存放整個數字，若不足則先提交
        if (outputBufferPosition + numDigits >= OUTPUT_BUFFER_CAPACITY) {
            commitOutput();
        }

        // 將數字的每一位反向存入緩衝區
        // (從個位數開始，存到 outputBufferPosition + numDigits - 1 的位置)
        int currentPos = outputBufferPosition + numDigits - 1;
        tempVal = val; // 重新獲取 val 的值
        if (tempVal == 0 && numDigits == 1) { // 再次處理0，如果它是唯一的數字
             outputBuffer[outputBufferPosition] = '0';
        } else {
            while (tempVal > 0) {
                outputBuffer[currentPos--] = (byte) ('0' + (tempVal % 10));
                tempVal /= 10;
            }
        }
        outputBufferPosition += numDigits; // 更新緩衝區位置
    }

    // 將字元添加到輸出緩衝區
    static void appendCharacter(char ch) throws Exception { // 可能拋出 Exception 是因為 commitOutput
        if (outputBufferPosition >= OUTPUT_BUFFER_CAPACITY) {
            commitOutput(); // 如果緩衝區滿了，先提交
        }
        outputBuffer[outputBufferPosition++] = (byte) ch;
    }

    // 提交輸出緩衝區的內容到標準輸出
    static void commitOutput() throws Exception {
        if (outputBufferPosition > 0) {
            System.out.write(outputBuffer, 0, outputBufferPosition);
            outputBufferPosition = 0; // 重置寫入位置
        }
    }

    /**
     * 檢查 l1 是否支配 l2 (l1 dominates l2)
     * l1 支配 l2 的條件是：對於所有維度 k，l1[k] <= l2[k]，並且至少在一個維度 j 上 l1[j] < l2[j]。
     *
     * @param l1 潛在的支配者
     * @param l2 潛在的被支配者
     * @param D  維度數量
     * @return 如果 l1 支配 l2，返回 true，否則 false
     */
    static boolean dominates(int[] l1, int[] l2, int D) {
        boolean strictlyBetterInOneDim = false; // 是否至少在一個維度上嚴格更優
        for (int i = 0; i < D; i++) {
            if (l1[i] > l2[i]) { // l1 在這個維度上比 l2 差
                return false;
            }
            if (l1[i] < l2[i]) { // l1 在這個維度上比 l2 嚴格更優
                strictlyBetterInOneDim = true;
            }
        }
        // 如果執行到這裡，代表 l1 在所有維度上都不劣於 l2。
        // 若至少在一個維度上嚴格更優，則 l1 支配 l2。
        return strictlyBetterInOneDim;
    }

    // ------ 自行實作的排序演算法 (合併排序) ------

    /**
     * 比較兩個資料列的字典序。
     * @param l1 資料列1
     * @param l2 資料列2
     * @param D 維度
     * @return -1 如果 l1 < l2, 0 如果 l1 == l2, 1 如果 l1 > l2
     */
    static int compareListingsLexicographically(int[] l1, int[] l2, int D) {
        for (int k = 0; k < D; k++) {
            if (l1[k] < l2[k]) return -1;
            if (l1[k] > l2[k]) return 1;
        }
        return 0; // 完全相同
    }

    /**
     * 合併排序中的合併步驟
     * @param arr 待排序的陣列
     * @param temp 輔助陣列，用於儲存 arr 中待合併部分的副本
     * @param left 左邊界索引
     * @param mid 中間索引
     * @param right 右邊界索引
     * @param D 維度
     */
    static void merge(int[][] arr, int[][] temp, int left, int mid, int right, int D) {
        // 將 arr[left...right] 的內容複製到 temp 陣列的相應位置
        // 注意：這裡複製的是 int[] 的參考
        for (int i = left; i <= right; i++) {
            temp[i] = arr[i];
        }

        int i = left;     // 指向 temp 左半部分的起始 (temp[left...mid])
        int j = mid + 1;  // 指向 temp 右半部分的起始 (temp[mid+1...right])
        int k = left;     // 指向 arr 中合併後存放的位置 (arr[left...right])

        // 開始合併
        while (i <= mid && j <= right) {
            if (compareListingsLexicographically(temp[i], temp[j], D) <= 0) { // temp[i] <= temp[j]
                arr[k++] = temp[i++];
            } else {
                arr[k++] = temp[j++];
            }
        }

        // 如果左半部分還有剩餘元素，複製到 arr
        while (i <= mid) {
            arr[k++] = temp[i++];
        }
        // 如果右半部分還有剩餘元素，複製到 arr (正常情況下，其中一個 while 會執行完畢)
        while (j <= right) {
            arr[k++] = temp[j++];
        }
    }

    /**
     * 遞迴的合併排序函數
     * @param arr 待排序的陣列
     * @param temp 輔助陣列
     * @param left 左邊界索引
     * @param right 右邊界索引
     * @param D 維度
     */
    static void mergeSortRecursive(int[][] arr, int[][] temp, int left, int right, int D) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSortRecursive(arr, temp, left, mid, D);
            mergeSortRecursive(arr, temp, mid + 1, right, D);
            merge(arr, temp, left, mid, right, D);
        }
    }

    /**
     * 對資料列表進行排序 (合併排序的入口)
     * @param listings 包含所有資料列的二維陣列
     * @param numElements 要排序的元素數量 (listings 中實際有效的資料列數量)
     * @param D 維度
     */
    static void sortListings(int[][] listings, int numElements, int D) {
        if (listings == null || numElements <= 1) {
            return; // 不需要排序
        }
        // 創建一個與 listings[0...numElements-1] 相同大小的輔助陣列 (儲存參考)
        int[][] temp = new int[numElements][]; 
        mergeSortRecursive(listings, temp, 0, numElements - 1, D);
    }


    // ------ 主邏輯 ------
    public static void main(String[] args) throws Exception {
        int N = scanInteger(); // 資料筆數
        int D = scanInteger(); // 維度數量

        // 1. 讀取所有資料
        int[][] allListings = new int[N][D];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < D; j++) {
                allListings[i][j] = scanInteger();
            }
        }

        // 2. 篩選非支配點
        // isDominated[i] 為 true 表示第 i 筆資料被其他資料支配
        boolean[] isDominated = new boolean[N];
        int nonDominatedCount = 0; // 用於計算非支配點的數量

        for (int i = 0; i < N; i++) {
            // 對於每一筆 candidate 資料 allListings[i]
            for (int j = 0; j < N; j++) {
                if (i == j) continue; // 不和自己比較

                // 檢查 allListings[j] (challenger) 是否支配 allListings[i] (candidate)
                if (dominates(allListings[j], allListings[i], D)) {
                    isDominated[i] = true; // candidate 被支配
                    break; // 不需要再和其他 challenger 比較
                }
            }
            if (!isDominated[i]) {
                nonDominatedCount++;
            }
        }

        // 3. 收集所有非支配點
        int[][] nonDominatedListings = new int[nonDominatedCount][D];
        int currentIndex = 0;
        for (int i = 0; i < N; i++) {
            if (!isDominated[i]) {
                // 複製資料 (System.arraycopy 不能用，因為沒有 import)
                for (int k = 0; k < D; k++) {
                    nonDominatedListings[currentIndex][k] = allListings[i][k];
                }
                currentIndex++;
            }
        }

        // 4. 對非支配點列表進行字典序排序
        sortListings(nonDominatedListings, nonDominatedCount, D);

        // 5. 輸出結果
        for (int i = 0; i < nonDominatedCount; i++) {
            for (int k = 0; k < D; k++) {
                appendInteger(nonDominatedListings[i][k]);
                if (k < D - 1) {
                    appendCharacter(' ');
                }
            }
            appendCharacter('\n');
        }
        commitOutput(); // 提交最後的輸出緩衝區內容
    }
}
