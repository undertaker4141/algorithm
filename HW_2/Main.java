class Main {
  // 優化的 HashSet 結構，使用陣列實現
  static class CustomSet {
    private boolean[] used;
    private int count;
    private static final int MAX_HASH = 20000000;  // 根據可能的組合數調整大小
    
    public CustomSet() {
      used = new boolean[MAX_HASH];
      count = 0;
    }
    
    // 優化的雜湊函數，直接使用數字計算
    private int hash(long a, long b, long c, long d) {
      return Math.abs((int)((a * 73856093 + b * 19349663 + c * 83492791 + d) % MAX_HASH));
    }
    
    // 優化的新增方法
    public void add(long a, long b, long c, long d) {
      int h = hash(a, b, c, d);
      if (!used[h]) {
        used[h] = true;
        count++;
      }
    }
    
    public int size() {
      return count;
    }
  }
  
  // 優化的快速排序，使用三數取中位數
  static void quickSort(long[] arr, int low, int high) {
    while (low < high) {
      if (high - low <= 10) {  // 小數組使用插入排序
        insertionSort(arr, low, high);
        break;
      }
      int pi = partition(arr, low, high);
      if (pi - low < high - pi) {
        quickSort(arr, low, pi - 1);
        low = pi + 1;
      } else {
        quickSort(arr, pi + 1, high);
        high = pi - 1;
      }
    }
  }
  
  // 插入排序用於小數組
  static void insertionSort(long[] arr, int low, int high) {
    for (int i = low + 1; i <= high; i++) {
      long key = arr[i];
      int j = i - 1;
      while (j >= low && arr[j] > key) {
        arr[j + 1] = arr[j];
        j--;
      }
      arr[j + 1] = key;
    }
  }
  
  // 優化的分割函數，使用三數取中位數
  static int partition(long[] arr, int low, int high) {
    int mid = (low + high) >>> 1;
    // 三數取中
    if (arr[mid] < arr[low]) swap(arr, low, mid);
    if (arr[high] < arr[low]) swap(arr, low, high);
    if (arr[high] < arr[mid]) swap(arr, mid, high);
    
    swap(arr, mid, high - 1);  // 將 pivot 藏到倒數第二個位置
    long pivot = arr[high - 1];
    
    int i = low;
    int j = high - 1;
    while (true) {
      while (arr[++i] < pivot);
      while (arr[--j] > pivot);
      if (i >= j) break;
      swap(arr, i, j);
    }
    swap(arr, i, high - 1);  // 恢復 pivot
    return i;
  }
  
  static void swap(long[] arr, int i, int j) {
    long temp = arr[i];
    arr[i] = arr[j];
    arr[j] = temp;
  }
  
  public static void main(String[] args) {
    java.util.Scanner sc = new java.util.Scanner(System.in);
    int n = sc.nextInt();
    
    long[] nums = new long[n];
    for (int i = 0; i < n; i++) {
      nums[i] = sc.nextLong();
    }
    
    quickSort(nums, 0, n - 1);
    CustomSet set = new CustomSet();
    
    // 加入更多剪枝條件
    for (int i = 0; i < n - 3; i++) {
      if (i > 0 && nums[i] == nums[i-1]) continue;
      if (nums[i] * 4 > 0) break;  // 最小的數乘4都大於0，不可能有解
      
      for (int j = i + 1; j < n - 2; j++) {
        if (j > i + 1 && nums[j] == nums[j-1]) continue;
        if (nums[i] + nums[j] * 3 > 0) break;  // 剪枝
        
        int left = j + 1;
        int right = n - 1;
        long target = -nums[i] - nums[j];
        
        while (left < right) {
          long sum = nums[left] + nums[right];
          
          if (sum == target) {
            set.add(nums[i], nums[j], nums[left], nums[right]);
            left++;
            right--;
            
            while (left < right && nums[left] == nums[left-1]) left++;
            while (left < right && nums[right] == nums[right+1]) right--;
          } else if (sum < target) {
            left++;
          } else {
            right--;
          }
        }
      }
    }
    
    System.out.println(set.size());
    sc.close();
  }
}
  