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
        int axis;

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



        if (farChild == node.right) { 
            if (node.point[currentAxis] <= candidate[currentAxis]) { 
                 if (isDominatedByKDT(farChild, candidate, totalDimensions)) {
                    return true;
                }
            }

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

    // ------ 合併排序 (用於初始排序) ------
    static int compareListingsLexicographically(int[] l1, int[] l2, int D) {
        for (int k = 0; k < D; k++) {
            if (l1[k] < l2[k]) return -1;
            if (l1[k] > l2[k]) return 1;
        }
        return 0; 
    }

    static void merge(int[][] arr, int[][] temp, int left, int mid, int right, int D_total) {
        for (int i = left; i <= right; i++) {
            temp[i] = arr[i]; 
        }
        int i = left, j = mid + 1, k = left;
        while (i <= mid && j <= right) {
            if (compareListingsLexicographically(temp[i], temp[j], D_total) <= 0) { 
                arr[k++] = temp[i++];
            } else {
                arr[k++] = temp[j++];
            }
        }
        while (i <= mid) arr[k++] = temp[i++]; 
        while (j <= right) arr[k++] = temp[j++];
    }
    
    static void standardMergeSortRecursive(int[][] arr, int[][] temp, int left, int right, int D_total) {
        if (left < right) {
            int mid = left + (right - left) / 2; 
            standardMergeSortRecursive(arr, temp, left, mid, D_total);
            standardMergeSortRecursive(arr, temp, mid + 1, right, D_total);
            merge(arr, temp, left, mid, right, D_total);
        }
    }
    
    static void sortListingsByMergeSort(int[][] listings, int numElements, int D_total) {
        if (listings == null || numElements <= 1) return; 
        int[][] temp = new int[numElements][]; 
        standardMergeSortRecursive(listings, temp, 0, numElements - 1, D_total); 
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
            sortListingsByMergeSort(allListings, N, D); 
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
