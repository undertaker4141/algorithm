class Main {
  public static void main(String[] args) throws java.io.IOException {
      java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
      int n = Integer.parseInt(br.readLine());
      String[] input = br.readLine().split(" ");
      int max = Integer.MIN_VALUE;

      // 每次處理 8 個元素，直接使用巢狀的 Math.max
      int i = 0;
      // 每次處理 20 個元素，直接使用巢狀的 Math.max
      if(n>=20){
      for (; i <= n - 20; i += 20) {
          max = Math.max(max, Math.max(
                  Math.max(
                          Math.max(
                                  Math.max(Integer.parseInt(input[i]), Integer.parseInt(input[i + 1])),
                                  Math.max(Integer.parseInt(input[i + 2]), Integer.parseInt(input[i + 3]))),
                          Math.max(
                                  Math.max(Integer.parseInt(input[i + 4]), Integer.parseInt(input[i + 5])),
                                  Math.max(Integer.parseInt(input[i + 6]), Integer.parseInt(input[i + 7])))),
                  Math.max(
                          Math.max(
                                  Math.max(Integer.parseInt(input[i + 8]), Integer.parseInt(input[i + 9])),
                                  Math.max(Integer.parseInt(input[i + 10]), Integer.parseInt(input[i + 11]))),
                          Math.max(
                                  Math.max(Integer.parseInt(input[i + 12]), Integer.parseInt(input[i + 13])),
                                  Math.max(Integer.parseInt(input[i + 14]), Integer.parseInt(input[i + 15]))))));
          max = Math.max(max, Math.max(
                  Math.max(
                          Math.max(Integer.parseInt(input[i + 16]), Integer.parseInt(input[i + 17])),
                          Math.max(Integer.parseInt(input[i + 18]), Integer.parseInt(input[i + 19]))),
                  max));
      }
    }
    else{
      for (; i <= n - 8; i += 8) {
          max = Math.max(max, Math.max(
                  Math.max(
                          Math.max(Integer.parseInt(input[i]), Integer.parseInt(input[i + 1])),
                          Math.max(Integer.parseInt(input[i + 2]), Integer.parseInt(input[i + 3]))),
                  Math.max(
                          Math.max(Integer.parseInt(input[i + 4]), Integer.parseInt(input[i + 5])),
                          Math.max(Integer.parseInt(input[i + 6]), Integer.parseInt(input[i + 7])))));
      }
    }

      // 處理剩餘的元素
      for (; i < n; i++) {
          max = Math.max(max, Integer.parseInt(input[i]));
      }

      System.out.println(max);
  }
}