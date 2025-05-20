class Main {
    // —— 極限輸入緩衝 ——
    static final int IN_BUF = 1 << 20;
    static byte[] inBuf = new byte[IN_BUF];
    static int inPos = 0, inLen = 0;
    static int read() throws Exception {
        if (inPos >= inLen) {
            inLen = System.in.read(inBuf);
            inPos = 0;
            if (inLen <= 0) return -1;
        }
        return inBuf[inPos++] & 0xFF;
    }
    static int readInt() throws Exception {
        int c, x = 0, sign = 1;
        do { c = read(); } while (c != -1 && c != '-' && (c < '0' || c > '9'));
        if (c == '-') { sign = -1; c = read(); }
        for (; c >= '0' && c <= '9'; c = read()) x = x * 10 + (c - '0');
        return x * sign;
    }

    // —— 極限輸出緩衝 ——
    static final int OUT_BUF = 1 << 20;
    static byte[] outBuf = new byte[OUT_BUF];
    static int outPos = 0;
    static void writeInt(int v) {
        if (v == 0) { outBuf[outPos++] = '0'; return; }
        if (v < 0) { outBuf[outPos++] = '-'; v = -v; }
        int start = outPos, len = 0;
        while (v > 0) {
            outBuf[outPos++] = (byte)('0' + (v % 10)); v /= 10; len++;
        }
        for (int i = 0; i < len/2; i++) {
            byte t = outBuf[start+i]; outBuf[start+i] = outBuf[start+len-1-i]; outBuf[start+len-1-i] = t;
        }
    }
    static void writeChar(char c) { outBuf[outPos++] = (byte)c; }
    static void flushOut() throws Exception { System.out.write(outBuf, 0, outPos); }

    public static void main(String[] args) throws Exception {
        int N = readInt();
        int[] prices = new int[N], times = new int[N];
        int maxP = 0;
        for (int i = 0; i < N; i++) {
            int p = readInt(), t = readInt();
            prices[i] = p; times[i] = t;
            if (p > maxP) maxP = p;
        }

        final int INF = Integer.MAX_VALUE;
        int[] minTime = new int[maxP + 1];
        int[] stamp = new int[maxP + 1];
        int curStamp = 1;
        int[] keep = new int[maxP + 1];

        // 2. 桶更新（延遲初始化）
        for (int i = 0; i < N; i++) {
            int p = prices[i], t = times[i];
            if (stamp[p] != curStamp) { stamp[p] = curStamp; minTime[p] = INF; }
            if (t < minTime[p]) minTime[p] = t;
        }

        // 3. 前綴掃描（分支消除 + 四路展開）
        int best = INF;
        int p = 0;
        for (; p + 3 <= maxP; p += 4) {
            int idx0 = p;
            if (stamp[idx0] != curStamp) { stamp[idx0] = curStamp; minTime[idx0] = INF; }
            int t0 = minTime[idx0]; int less0 = (t0 < best) ? 1 : 0;
            best = less0 * t0 + (1 - less0) * best;
            keep[idx0] |= less0;

            int idx1 = p + 1;
            if (stamp[idx1] != curStamp) { stamp[idx1] = curStamp; minTime[idx1] = INF; }
            int t1 = minTime[idx1]; int less1 = (t1 < best) ? 1 : 0;
            best = less1 * t1 + (1 - less1) * best;
            keep[idx1] |= less1;

            int idx2 = p + 2;
            if (stamp[idx2] != curStamp) { stamp[idx2] = curStamp; minTime[idx2] = INF; }
            int t2 = minTime[idx2]; int less2 = (t2 < best) ? 1 : 0;
            best = less2 * t2 + (1 - less2) * best;
            keep[idx2] |= less2;

            int idx3 = p + 3;
            if (stamp[idx3] != curStamp) { stamp[idx3] = curStamp; minTime[idx3] = INF; }
            int t3 = minTime[idx3]; int less3 = (t3 < best) ? 1 : 0;
            best = less3 * t3 + (1 - less3) * best;
            keep[idx3] |= less3;
        }
        for (; p <= maxP; p++) {
            if (stamp[p] != curStamp) { stamp[p] = curStamp; minTime[p] = INF; }
            int tv = minTime[p]; int less = (tv < best) ? 1 : 0;
            best = less * tv + (1 - less) * best;
            keep[p] |= less;
        }

        // 4. 原序輸出
        for (int i = 0; i < N; i++) {
            int pp = prices[i], tt = times[i];
            if (keep[pp] == 1 && minTime[pp] == tt) {
                writeInt(pp); writeChar(' '); writeInt(tt); writeChar('\n');
            }
        }
        flushOut();
    }
}
