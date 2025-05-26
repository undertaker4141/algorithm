class Main {
    // —— 高效輸入處理模組 ——
    static final int INPUT_BUFFER_CAPACITY = 1 << 20; 
    static byte[] inputBuffer = new byte[INPUT_BUFFER_CAPACITY]; 
    static int inputBufferPosition = 0, inputBufferLength = 0; 

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
    static final int OUTPUT_BUFFER_CAPACITY = 1 << 20; 
    static byte[] outputBuffer = new byte[OUTPUT_BUFFER_CAPACITY]; 
    static int outputBufferPosition = 0; 

    static final byte[] MIN_INT_BYTES = {'-','2','1','4','7','4','8','3','6','4','8'};
    static byte[] tempNumWriteBuffer = new byte[11]; 

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

    // ------ K-D 樹相關實現 ------
    static class KDTNode {
        int[] point;
        KDTNode left;
        KDTNode right;
        int axis; // 分裂維度

        KDTNode(int[] point, int axis) {
            this.point = point;
            this.axis = axis;
            this.left = null;
            this.right = null;
        }
    }

    static KDTNode insertIntoKDT(KDTNode root, int[] point, int totalDimensions, int depth) {
        if (root == null) {
            return new KDTNode(point, depth % totalDimensions);
        }
        int currentAxis = root.axis;
        if (point[currentAxis] < root.point[currentAxis]) {
            root.left = insertIntoKDT(root.left, point, totalDimensions, depth + 1);
        } else {
            root.right = insertIntoKDT(root.right, point, totalDimensions, depth + 1);
        }
        return root;
    }

    static boolean isDominatedByKDT(KDTNode node, int[] candidate, int totalDimensions) {
        if (node == null) {
            return false;
        }
        if (dominates(node.point, candidate, totalDimensions)) {
            return true;
        }
        int currentAxis = node.axis;
        KDTNode nearChild, farChild;
        if (candidate[currentAxis] < node.point[currentAxis]) {
            nearChild = node.left;
            farChild = node.right;
        } else {
            nearChild = node.right;
            farChild = node.left;
        }
        if (isDominatedByKDT(nearChild, candidate, totalDimensions)) {
            return true;
        }
        if (farChild == node.right && node.point[currentAxis] > candidate[currentAxis]) {
            // 不搜索 farChild (剪枝)
        } else {
             if (isDominatedByKDT(farChild, candidate, totalDimensions)) {
                return true;
            }
        }
        return false;
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

    // ------ 快速排序 (Quick Sort) 實現 ------
    static final int INSERTION_SORT_THRESHOLD_QS = 16; 

    static int compareListingsLexicographically(int[] l1, int[] l2, int D) {
        for (int k = 0; k < D; k++) {
            if (l1[k] < l2[k]) return -1;
            if (l1[k] > l2[k]) return 1;
        }
        return 0; 
    }

    static void swap(int[][] arr, int i, int j) {
        int[] temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    static void insertionSortRange(int[][] arr, int left, int right, int D) {
        for (int i = left + 1; i <= right; i++) {
            int[] key = arr[i]; 
            int j = i - 1;
            while (j >= left && compareListingsLexicographically(arr[j], key, D) > 0) {
                arr[j + 1] = arr[j]; 
                j = j - 1;
            }
            arr[j + 1] = key; 
        }
    }
    
    // Lomuto 分割方案，使用三數取中選擇基準元
    static int partition(int[][] arr, int low, int high, int D) {
        int mid = low + (high - low) / 2;

        if (compareListingsLexicographically(arr[low], arr[mid], D) > 0) swap(arr, low, mid);
        if (compareListingsLexicographically(arr[low], arr[high], D) > 0) swap(arr, low, high);
        if (compareListingsLexicographically(arr[mid], arr[high], D) > 0) swap(arr, mid, high);
        int[] pivotValue = arr[mid]; 
        swap(arr, mid, high);

        int i = low - 1; 
        for (int j = low; j < high; j++) { 
            if (compareListingsLexicographically(arr[j], pivotValue, D) <= 0) {
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, high);
        return i + 1;
    }

    static void quickSortRecursive(int[][] arr, int low, int high, int D) {
        if (low < high) {
            if (high - low + 1 < INSERTION_SORT_THRESHOLD_QS) {
                insertionSortRange(arr, low, high, D);
            } else {
                int pivotIndex = partition(arr, low, high, D);
                if (pivotIndex - low < high - pivotIndex) {
                    quickSortRecursive(arr, low, pivotIndex - 1, D);
                    quickSortRecursive(arr, pivotIndex + 1, high, D);
                } else {
                    quickSortRecursive(arr, pivotIndex + 1, high, D);
                    quickSortRecursive(arr, low, pivotIndex - 1, D);
                }
            }
        }
    }

    static void sortListingsByQuickSort(int[][] listings, int numElements, int D) {
        if (listings == null || numElements <= 1) return;
        quickSortRecursive(listings, 0, numElements - 1, D);
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

        if (N > 0) { 
            sortListingsByQuickSort(allListings, N, D); 
        }

        KDTNode ndRoot = null; 
        int[][] outputListings = new int[N][]; 
        int outputCount = 0;

        for (int i = 0; i < N; i++) {
            int[] candidatePoint = allListings[i]; 
            boolean isDominated = false;

            if (ndRoot != null) { 
                isDominated = isDominatedByKDT(ndRoot, candidatePoint, D);
            }

            if (!isDominated) {
                outputListings[outputCount++] = candidatePoint;
                ndRoot = insertIntoKDT(ndRoot, candidatePoint, D, 0); 
            }
        }

        for (int i = 0; i < outputCount; i++) {
            int[] pointToPrint = outputListings[i]; 
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
