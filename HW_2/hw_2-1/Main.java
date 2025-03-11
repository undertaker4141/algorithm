class Main {
    static class Pair {
        int i, j;
        Pair(int i, int j) {
            this.i = i;
            this.j = j;
        }
    }
    
    static class DynamicArray {
        private Pair[] data;
        private int size;
        
        public DynamicArray() {
            data = new Pair[2];  // 初始容量
            size = 0;
        }
        
        public void add(Pair pair) {
            if (size == data.length) {
                Pair[] newData = new Pair[data.length * 2];
                System.arraycopy(data, 0, newData, 0, data.length);
                data = newData;
            }
            data[size++] = pair;
        }
        
        public int size() {
            return size;
        }
        
        public Pair get(int index) {
            return data[index];
        }
    }
    
    static class HashTable {
        private DynamicArray[] table;
        private static final int SIZE = 2097152;  // 2^21
        private static final long MOD = 2097151;  // 2^21 - 1
        
        public HashTable() {
            table = new DynamicArray[SIZE];
        }
        
        private int hash(long key) {
            return (int)((key + 2000000000) & MOD);  // 處理負數
        }
        
        public void add(long sum, int i, int j) {
            int index = hash(sum);
            if (table[index] == null) {
                table[index] = new DynamicArray();
            }
            table[index].add(new Pair(i, j));
        }
        
        public DynamicArray get(long sum) {
            int index = hash(sum);
            return table[index] == null ? new DynamicArray() : table[index];
        }
    }
    
    public static void main(String[] args) {
        java.util.Scanner sc = new java.util.Scanner(System.in);
        int n = sc.nextInt();
        long[] nums = new long[n];
        
        // 讀取輸入
        for (int i = 0; i < n; i++) {
            nums[i] = sc.nextLong();
        }
        
        // 排序數組
        sort(nums);
        
        int counter = 0;
        // 固定前兩個數字
        for (int i = 0; i < n - 3; i++) {
            for (int j = i + 1; j < n - 2; j++) {
                // 使用雙指針尋找剩下兩個數字
                int left = j + 1;
                int right = n - 1;
                long target = -(nums[i] + nums[j]);
                
                while (left < right) {
                    long sum = nums[left] + nums[right];
                    if (sum == target) {
                        counter++;
                        left++;
                        right--;
                    } else if (sum < target) {
                        left++;
                    } else {
                        right--;
                    }
                }
            }
        }
        
        System.out.println(counter);
    }
    
    // 實作快速排序
    private static void sort(long[] nums) {
        quickSort(nums, 0, nums.length - 1);
    }
    
    private static void quickSort(long[] nums, int low, int high) {
        if (low < high) {
            int pi = partition(nums, low, high);
            quickSort(nums, low, pi - 1);
            quickSort(nums, pi + 1, high);
        }
    }
    
    private static int partition(long[] nums, int low, int high) {
        long pivot = nums[high];
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            if (nums[j] < pivot) {
                i++;
                long temp = nums[i];
                nums[i] = nums[j];
                nums[j] = temp;
            }
        }
        
        long temp = nums[i + 1];
        nums[i + 1] = nums[high];
        nums[high] = temp;
        
        return i + 1;
    }
}