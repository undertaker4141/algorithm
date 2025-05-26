class Main { // 類名變更
    // —— 高效輸入處理模組 ——
    static final int INPUT_BUFFER_CAPACITY = 1 << 20; // 常數名變更
    static byte[] inputBuffer = new byte[INPUT_BUFFER_CAPACITY]; // 變數名變更
    static int inputBufferPosition = 0, inputBufferLength = 0; // 變數名變更

    static int fetchByte() throws Exception { // 方法名變更
        if (inputBufferPosition >= inputBufferLength) {
            inputBufferLength = System.in.read(inputBuffer);
            inputBufferPosition = 0;
            if (inputBufferLength <= 0) return -1; // 流結束
        }
        return inputBuffer[inputBufferPosition++] & 0xFF; // 返回讀取的字節
    }

    static int scanInteger() throws Exception { // 方法名變更
        int character, value = 0, multiplier = 1; // 變數名變更
        do {
            character = fetchByte();
        } while (character != -1 && character != '-' && (character < '0' || character > '9')); // 跳過非數字和非負號字符

        if (character == '-') {
            multiplier = -1;
            character = fetchByte();
        }
        for (; character >= '0' && character <= '9'; character = fetchByte()) {
            value = value * 10 + (character - '0'); // 構建數字
        }
        return value * multiplier;
    }

    // —— 高效輸出處理模組 ——
    static final int OUTPUT_BUFFER_CAPACITY = 1 << 20; // 常數名變更
    static byte[] outputBuffer = new byte[OUTPUT_BUFFER_CAPACITY]; // 變數名變更
    static int outputBufferPosition = 0; // 變數名變更

    static void appendInteger(int val) { // 方法名變更
        if (val == 0) {
            outputBuffer[outputBufferPosition++] = '0';
            return;
        }
        if (val < 0) {
            outputBuffer[outputBufferPosition++] = '-';
            val = -val; // 轉為正數處理
        }
        int startIndex = outputBufferPosition; // 記錄數字開始位置
        int count = 0; // 記錄數字長度
        while (val > 0) {
            outputBuffer[outputBufferPosition++] = (byte) ('0' + (val % 10));
            val /= 10;
            count++;
        }
        // 反轉數字字元序列
        int left = startIndex;
        int right = startIndex + count - 1;
        while (left < right) {
            byte temp = outputBuffer[left];
            outputBuffer[left] = outputBuffer[right];
            outputBuffer[right] = temp;
            left++;
            right--;
        }
    }

    static void appendCharacter(char ch) { // 方法名變更
        outputBuffer[outputBufferPosition++] = (byte) ch;
    }

    static void commitOutput() throws Exception { // 方法名變更
        System.out.write(outputBuffer, 0, outputBufferPosition);
    }

    public static void main(String[] args) throws Exception {
        int itemCount = scanInteger(); // 變數名變更 (N)
        int[] itemPrices = new int[itemCount]; // 變數名變更 (prices)
        int[] itemProcessingTimes = new int[itemCount]; // 變數名變更 (times)
        int highestPrice = 0; // 變數名變更 (maxP)

        for (int i = 0; i < itemCount; i++) {
            int p_val = scanInteger(); // 局部變數名變更
            int t_val = scanInteger(); // 局部變數名變更
            itemPrices[i] = p_val;
            itemProcessingTimes[i] = t_val;
            if (p_val > highestPrice) {
                highestPrice = p_val;
            }
        }

        final int UNINITIALIZED_TIME = Integer.MAX_VALUE; // 常數名變更 (INF)
        int[] minProcessingTimeForPrice = new int[highestPrice + 1]; // 變數名變更 (minTime)
        int[] priceVisitMarker = new int[highestPrice + 1]; // 變數名變更 (stamp)
        int currentVisitID = 1; // 變數名變更 (curStamp)
        int[] isCandidateOffer = new int[highestPrice + 1]; // 變數名變更 (keep)

        // 階段2: 更新價格桶內的最小處理時間 (使用延遲初始化策略)
        for (int i = 0; i < itemCount; i++) {
            int price = itemPrices[i];
            int time = itemProcessingTimes[i];
            // 如果該價格桶未被當前訪問ID標記，則初始化
            if (priceVisitMarker[price] != currentVisitID) {
                priceVisitMarker[price] = currentVisitID;
                minProcessingTimeForPrice[price] = UNINITIALIZED_TIME;
            }
            // 更新該價格對應的最小處理時間
            if (time < minProcessingTimeForPrice[price]) {
                minProcessingTimeForPrice[price] = time;
            }
        }

        // 階段3: 前綴掃描以確定候選商品 (優化：分支消除 + 四路迴圈展開)
        int bestTimeOverall = UNINITIALIZED_TIME; // 變數名變更 (best)
        int currentPriceIndex = 0; // 變數名變更 (p)

        // 四路展開處理大部分數據
        for (; currentPriceIndex + 3 <= highestPrice; currentPriceIndex += 4) {
            // 處理第0個價格
            int p0 = currentPriceIndex;
            if (priceVisitMarker[p0] != currentVisitID) { // 延遲初始化
                priceVisitMarker[p0] = currentVisitID;
                minProcessingTimeForPrice[p0] = UNINITIALIZED_TIME;
            }
            int time0 = minProcessingTimeForPrice[p0];
            if (time0 < bestTimeOverall) { // 標準 if 判斷
                bestTimeOverall = time0;
                isCandidateOffer[p0] = 1; // 標記為候選
            }

            // 處理第1個價格
            int p1 = currentPriceIndex + 1;
            if (priceVisitMarker[p1] != currentVisitID) {
                priceVisitMarker[p1] = currentVisitID;
                minProcessingTimeForPrice[p1] = UNINITIALIZED_TIME;
            }
            int time1 = minProcessingTimeForPrice[p1];
            if (time1 < bestTimeOverall) {
                bestTimeOverall = time1;
                isCandidateOffer[p1] = 1;
            }

            // 處理第2個價格
            int p2 = currentPriceIndex + 2;
            if (priceVisitMarker[p2] != currentVisitID) {
                priceVisitMarker[p2] = currentVisitID;
                minProcessingTimeForPrice[p2] = UNINITIALIZED_TIME;
            }
            int time2 = minProcessingTimeForPrice[p2];
            if (time2 < bestTimeOverall) {
                bestTimeOverall = time2;
                isCandidateOffer[p2] = 1;
            }

            // 處理第3個價格
            int p3 = currentPriceIndex + 3;
            if (priceVisitMarker[p3] != currentVisitID) {
                priceVisitMarker[p3] = currentVisitID;
                minProcessingTimeForPrice[p3] = UNINITIALIZED_TIME;
            }
            int time3 = minProcessingTimeForPrice[p3];
            if (time3 < bestTimeOverall) {
                bestTimeOverall = time3;
                isCandidateOffer[p3] = 1;
            }
        }
        // 處理剩餘的數據 (不足四個的部分)
        for (; currentPriceIndex <= highestPrice; currentPriceIndex++) {
            if (priceVisitMarker[currentPriceIndex] != currentVisitID) {
                priceVisitMarker[currentPriceIndex] = currentVisitID;
                minProcessingTimeForPrice[currentPriceIndex] = UNINITIALIZED_TIME;
            }
            int currentTimeVal = minProcessingTimeForPrice[currentPriceIndex]; // 局部變數名變更 (tv)
            if (currentTimeVal < bestTimeOverall) {
                bestTimeOverall = currentTimeVal;
                isCandidateOffer[currentPriceIndex] = 1;
            }
        }

        // 階段4: 按照原始輸入順序輸出結果
        for (int i = 0; i < itemCount; i++) {
            int originalPrice = itemPrices[i]; // 變數名變更 (pp)
            int originalTime = itemProcessingTimes[i]; // 變數名變更 (tt)
            // 檢查是否為候選商品，並且其時間確實是該價格下的最小時間
            if (isCandidateOffer[originalPrice] == 1 && minProcessingTimeForPrice[originalPrice] == originalTime) {
                appendInteger(originalPrice);
                appendCharacter(' ');
                appendInteger(originalTime);
                appendCharacter('\n');
            }
        }
        commitOutput(); // 刷新輸出緩衝區
    }
}