import java.util.Scanner;

public class Main {
    private static final int HASH_SIZE = 10007; // 增大哈希表大小以減少碰撞

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        double threshold = scanner.nextDouble();
        scanner.nextLine();

        int n = scanner.nextInt();
        scanner.nextLine();

        int[][] documents = new int[n][];
        int[] docSizes = new int[n];

        String[] wordTable = new String[HASH_SIZE];

        for (int i = 0; i < n; i++) {
            String line = scanner.nextLine().toLowerCase();
            String[] words = line.split(" ");

            int[] tempIndices = new int[words.length];
            int uniqueCount = 0;

            boolean[] existsInDoc = new boolean[HASH_SIZE]; // 使用布林陣列加速檢查

            for (String word : words) {
                if (word.isEmpty())
                    continue;

                int hash = hashFunction(word);
                while (hash < 0)
                    hash += HASH_SIZE;
                while (hash >= HASH_SIZE)
                    hash -= HASH_SIZE;

                // 開放定址法處理碰撞
                while (wordTable[hash] != null && !wordTable[hash].equals(word)) {
                    hash = (hash + 1) % HASH_SIZE;
                }

                if (wordTable[hash] == null) {
                    wordTable[hash] = word;
                }

                // 直接檢查布林陣列
                if (!existsInDoc[hash]) {
                    existsInDoc[hash] = true;
                    tempIndices[uniqueCount++] = hash;
                }
            }

            documents[i] = new int[uniqueCount];
            System.arraycopy(tempIndices, 0, documents[i], 0, uniqueCount);
            docSizes[i] = uniqueCount;

            sort(documents[i]); // 優化後的排序
        }

        int count = 0;
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                if (docSizes[i] == 0 && docSizes[j] == 0) {
                    if (1.0 > threshold)
                        count++;
                    continue;
                }
                if (docSizes[i] == 0 || docSizes[j] == 0)
                    continue;

                double sizeRatio = (double) Math.min(docSizes[i], docSizes[j])
                        / Math.max(docSizes[i], docSizes[j]);
                if (sizeRatio <= threshold)
                    continue;

                int intersection = computeIntersectionSize(documents[i], documents[j]);
                int union = docSizes[i] + docSizes[j] - intersection;
                double similarity = (double) intersection / union;

                if (similarity > threshold)
                    count++;
            }
        }

        System.out.println(count);
        scanner.close();
    }

    private static int hashFunction(String s) {
        int hash = 0;
        for (int i = 0; i < s.length(); i++) {
            hash = 31 * hash + s.charAt(i);
        }
        return hash % HASH_SIZE;
    }

    // 優化排序：小數組用插入排序
    private static void sort(int[] arr) {
        if (arr.length <= 7)
            insertionSort(arr);
        else
            quickSort(arr, 0, arr.length - 1);
    }

    private static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pivot = partition(arr, low, high);
            quickSort(arr, low, pivot - 1);
            quickSort(arr, pivot + 1, high);
        }
    }

    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, high);
        return i + 1;
    }

    private static void insertionSort(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            int key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
        }
    }

    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    private static int computeIntersectionSize(int[] a, int[] b) {
        int i = 0, j = 0, count = 0;
        while (i < a.length && j < b.length) {
            if (a[i] < b[j])
                i++;
            else if (a[i] > b[j])
                j++;
            else {
                count++;
                i++;
                j++;
            }
        }
        return count;
    }
}