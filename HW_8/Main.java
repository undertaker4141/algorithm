// 沒有 import 語句

class Main {

    // --- 用於 FastReader 返回 char[] 和長度 ---
    static class CharArrayReadResult {
        char[] chars;
        int length;
        CharArrayReadResult(char[] c, int l) { chars = c; length = l; }
    }

    // --- CoWord (使用 char[] 和 len) ---
    static class CoWord {
        char[] wordChars;
        int wordLen;
        int count;
        CoWord(char[] wc, int wl, int c) {
            wordChars = wc;
            wordLen = wl;
            count = c;
        }
    }

    // --- char[] 的哈希和比較工具 ---
    static class CharArrayUtils {
        static int hashCode(char[] arr, int len) {
            if (arr == null || len == 0) return 0;
            int h = 0;
            for (int i = 0; i < len; i++) {
                h = 31 * h + arr[i];
            }
            return h;
        }

        static boolean equals(char[] a, int lenA, char[] b, int lenB) {
            if (a == b && lenA == lenB) return true;
            if (a == null || b == null) return false;
            if (lenA != lenB) return false;
            for (int i = 0; i < lenA; i++) {
                if (a[i] != b[i]) return false;
            }
            return true;
        }

        static int compareTo(char[] a, int lenA, char[] b, int lenB) {
            int n = Math.min(lenA, lenB);
            for (int i = 0; i < n; i++) {
                if (a[i] != b[i]) {
                    return a[i] - b[i];
                }
            }
            return lenA - lenB;
        }
    }

    // --- 開放定址線性探測內部 Map (char[] key -> int count) ---
    static class OpenAddressingInnerMapForChars {
        private char[][] keys; private int[] keyLengths; private int[] counts;
        private int capacity; private int size; private int slotsUsed;
        private static final char[] TOMBSTONE = new char[0];
        private static final float LOAD_FACTOR_THRESHOLD = 0.6f; // 保持 164ms 時的 0.6f

        public OpenAddressingInnerMapForChars(int initialCapacity) {
            int cap=16; if(initialCapacity>0)cap=Integer.highestOneBit(initialCapacity-1)<<1; if(cap<=0)cap=16; if(cap<16)cap=16;
            this.capacity = cap;
            this.keys = new char[this.capacity][];
            this.keyLengths = new int[this.capacity];
            this.counts = new int[this.capacity];
            this.size = 0; this.slotsUsed = 0;
        }

        private int hash(char[] key, int len) { return (CharArrayUtils.hashCode(key, len) & 0x7FFFFFFF) & (capacity - 1); }

        private void rehash() {
            char[][] oldKeys = keys; int[] oldKeyLengths = keyLengths; int[] oldCounts = counts; int oCap = capacity;
            capacity <<= 1; if(capacity<=0||capacity>(1<<28)){capacity=oCap;}
            keys = new char[capacity][]; keyLengths = new int[capacity]; counts = new int[capacity];
            size = 0; slotsUsed = 0;
            for (int i = 0; i < oCap; i++) {
                if (oldKeys[i] != null && oldKeys[i] != TOMBSTONE) {
                    putForRehash(oldKeys[i], oldKeyLengths[i], oldCounts[i]);
                }
            }
        }
        private void putForRehash(char[] k, int kl, int v){int i=hash(k,kl);while(keys[i]!=null)i=(i+1)&(capacity-1);keys[i]=k;keyLengths[i]=kl;counts[i]=v;size++;slotsUsed++;}

        public void increment(char[] key, int len) {
            if(key==null)throw new IllegalArgumentException("Key cannot be null in increment");
            if((float)(slotsUsed+1)/capacity > LOAD_FACTOR_THRESHOLD) rehash();

            int index = hash(key,len);
            int startIndex = index;
            int tombstoneIndex = -1;

            while(keys[index]!=null){
                if(keys[index]==TOMBSTONE){
                    if(tombstoneIndex == -1) tombstoneIndex = index;
                } else if(CharArrayUtils.equals(key, len, keys[index], keyLengths[index])){
                    counts[index]++;
                    return;
                }
                index=(index+1)&(capacity-1);
                if(index==startIndex){ // Full circle
                    rehash(); // Must rehash if full circle and key not found
                    // Recalculate hash and restart probe
                    index = hash(key, len);
                    startIndex = index;
                    tombstoneIndex = -1; // Reset tombstone after rehash
                    // Continue probing in the new table
                    // This inner loop is to find after rehash, must also check for full circle
                    int rehashProbeCount = 0;
                    while(keys[index]!=null) {
                        if(keys[index]==TOMBSTONE){ if(tombstoneIndex == -1) tombstoneIndex = index; }
                        else if(CharArrayUtils.equals(key,len,keys[index],keyLengths[index])){ counts[index]++; return; }
                        index = (index + 1) & (capacity-1);
                        if(index == startIndex && ++rehashProbeCount > 1) throw new RuntimeException("InnerMapChars full after rehash - probe loop stuck");
                        if(index == startIndex && rehashProbeCount <=1 ) continue; // Allow one full loop after rehash before giving up
                    }
                    // If loop exits, it means an empty slot was found in the rehashed table
                    break; // Break outer while to proceed to insertion
                }
            }
            // Insert into an empty slot or a tombstone
            if(tombstoneIndex != -1){
                index = tombstoneIndex;
                // slotsUsed does not change as we are reusing a tombstone slot
            } else {
                slotsUsed++; // A new slot is being used
            }
            keys[index]=key; keyLengths[index]=len; counts[index]=1; size++;
        }
        public boolean isEmpty(){return size==0;}
        public CoWord[] getAllEntries(){if(size==0)return new CoWord[0];CoWord[]e=new CoWord[size];int ei=0;for(int i=0;i<capacity;i++){if(keys[i]!=null&&keys[i]!=TOMBSTONE){if(ei<size)e[ei++]=new CoWord(keys[i],keyLengths[i],counts[i]);else break;}}if(ei!=size){CoWord[]a=new CoWord[ei];System.arraycopy(e,0,a,0,ei);return a;}return e;}
    }

    // --- 開放定址線性探測外部 Map (char[] key -> InnerMap) ---
    static class OpenAddressingOuterMapForChars {
        private char[][] keys; private int[] keyLengths; private OpenAddressingInnerMapForChars[] values;
        private int capacity; private int size; private int slotsUsed;
        private static final char[] TOMBSTONE = new char[0];
        public static final float LOAD_FACTOR_THRESHOLD = 0.6f; // 保持 164ms 時的 0.6f

        public OpenAddressingOuterMapForChars(int ic){int c=16;if(ic>0)c=Integer.highestOneBit(ic-1)<<1;if(c<=0)c=1<<20;if(c<16)c=16;this.capacity=c;this.keys=new char[c][];this.keyLengths=new int[c];this.values=new OpenAddressingInnerMapForChars[c];this.size=0;this.slotsUsed=0;}
        private int hash(char[] k, int l){return(CharArrayUtils.hashCode(k,l)&0x7FFFFFFF)&(capacity-1);}
        private void rehash(){char[][]ok=keys;int[]ol=keyLengths;OpenAddressingInnerMapForChars[]ov=values;int oCap=capacity;capacity<<=1;if(capacity<=0||capacity>(1<<28)){capacity=oCap;}keys=new char[capacity][];keyLengths=new int[capacity];values=new OpenAddressingInnerMapForChars[capacity];size=0;slotsUsed=0;for(int i=0;i<oCap;i++){if(ok[i]!=null&&ok[i]!=TOMBSTONE)putForRehash(ok[i],ol[i],ov[i]);}}
        private void putForRehash(char[]k,int kl,OpenAddressingInnerMapForChars v){int i=hash(k,kl);while(keys[i]!=null)i=(i+1)&(capacity-1);keys[i]=k;keyLengths[i]=kl;values[i]=v;size++;slotsUsed++;}

        public void put(char[]k,int kl,OpenAddressingInnerMapForChars v){
            if(k==null)throw new IllegalArgumentException("Key cannot be null in put");
            if((float)(slotsUsed+1)/capacity>LOAD_FACTOR_THRESHOLD)rehash();

            int i=hash(k,kl);
            int si=i;
            int ti=-1;

            while(keys[i]!=null){
                if(keys[i]==TOMBSTONE){
                    if(ti==-1)ti=i;
                } else if(CharArrayUtils.equals(k,kl,keys[i],keyLengths[i])){
                    values[i]=v; // Update existing
                    return;
                }
                i=(i+1)&(capacity-1);
                if(i==si){
                    rehash(); i=hash(k,kl); si=i; ti=-1;
                    int rehashProbeCount = 0;
                    while(keys[i]!=null){
                        if(keys[i]==TOMBSTONE){ if(ti==-1)ti=i; }
                        else if(CharArrayUtils.equals(k,kl,keys[i],keyLengths[i])){ values[i]=v; return; }
                        i=(i+1)&(capacity-1);
                        if(i==si && ++rehashProbeCount > 1) throw new RuntimeException("OuterMapChars full after rehash - probe loop stuck");
                        if(i==si && rehashProbeCount <=1) continue;
                    }
                    break; 
                }
            }
            if(ti!=-1) i=ti; else slotsUsed++;
            keys[i]=k;keyLengths[i]=kl;values[i]=v;size++;
        }
        public OpenAddressingInnerMapForChars get(char[]k, int kl){if(k==null)return null;int i=hash(k,kl);int si=i;while(keys[i]!=null){if(keys[i]!=TOMBSTONE&&CharArrayUtils.equals(k,kl,keys[i],keyLengths[i]))return values[i];i=(i+1)&(capacity-1);if(i==si)break;}return null;}
    }

    // --- MergeSort (比較 CoWord, 不變) ---
    private static int compareCoWords(CoWord a, CoWord b) {
        if (a.count != b.count) return b.count - a.count;
        return CharArrayUtils.compareTo(a.wordChars, a.wordLen, b.wordChars, b.wordLen);
    }
    private static void mergeSort(CoWord[] arr, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);
            merge(arr, left, mid, right);
        }
    }
    private static void merge(CoWord[] arr, int l, int m, int r) {
        int n1 = m - l + 1; int n2 = r - m;
        CoWord[] L = new CoWord[n1]; CoWord[] R = new CoWord[n2];
        for (int i = 0; i < n1; ++i) L[i] = arr[l + i];
        for (int j = 0; j < n2; ++j) R[j] = arr[m + 1 + j];
        int i = 0, j = 0, k = l;
        while (i < n1 && j < n2) {
            if (compareCoWords(L[i], R[j]) <= 0) arr[k++] = L[i++];
            else arr[k++] = R[j++];
        }
        while (i < n1) arr[k++] = L[i++];
        while (j < n2) arr[k++] = R[j++];
    }

    // --- Main 邏輯 (不變) ---
    public static void main(String[] args) {
        FastReader reader = null; FastWriter writer = null;
        try {
            reader = new FastReader(); writer = new FastWriter();
            int N = reader.readInt(); int M = reader.readInt();
            OpenAddressingOuterMapForChars coOccurrences = new OpenAddressingOuterMapForChars(1 << 19); // 保持 OuterMap 容量

            for(int i=0;i<N;i++){
                CharArrayReadResult w1_arr = reader.readStringAsChars();
                CharArrayReadResult w2_arr = reader.readStringAsChars();
                addCoOccurrence(coOccurrences, w1_arr.chars, w1_arr.length, w2_arr.chars, w2_arr.length);
                addCoOccurrence(coOccurrences, w2_arr.chars, w2_arr.length, w1_arr.chars, w1_arr.length);
            }
            for(int i=0;i<M;i++){
                CharArrayReadResult q_arr = reader.readStringAsChars(); int K=reader.readInt();
                OpenAddressingInnerMapForChars im=coOccurrences.get(q_arr.chars, q_arr.length);
                if(im==null||im.isEmpty()){writer.write((byte)'\n');continue;}
                CoWord[]rs=im.getAllEntries();
                if(rs==null||rs.length==0||K<=0){writer.write((byte)'\n');continue;}
                mergeSort(rs, 0, rs.length - 1);
                int pt=K<rs.length?K:rs.length;
                for(int j=0;j<pt;j++){writer.print(rs[j].wordChars, rs[j].wordLen);if(j<pt-1)writer.write((byte)' ');}
                writer.write((byte)'\n');
            }
        } catch (RuntimeException e) { throw e;
        } finally { if (writer != null) { try { writer.flush(); writer.close(); } catch (RuntimeException e) { throw e; }}}
    }

    static void addCoOccurrence(OpenAddressingOuterMapForChars oMap, char[] k1, int l1, char[] k2, int l2) {
        OpenAddressingInnerMapForChars iMap = oMap.get(k1, l1);
        if (iMap == null) { iMap = new OpenAddressingInnerMapForChars(64); oMap.put(k1, l1, iMap); } // InnerMap 容量 64
        iMap.increment(k2, l2);
    }

    // --- FastReader 和 FastWriter (修正 FastWriter 的 print(int) ) ---
    static class FastReader {
        private final byte[]bf=new byte[1<<16];private int p=0,s=0;
        private char[] charBuf = new char[128];

        public FastReader(){try{fill();}catch(Exception e){throw new RuntimeException(e);}}
        private void fill(){try{s=System.in.read(bf);p=0;}catch(Exception e){throw new RuntimeException(e);}}
        private byte r(){try{if(p>=s){fill();if(s<=0)return-1;}return bf[p++];}catch(Exception e){throw new RuntimeException(e);}}
        public int readInt(){ byte c=r();while(c!=-1&&(c<'0'||c>'9')&&c!='-')c=r();if(c==-1)throw new RuntimeException("EOF for int");boolean n=(c=='-');if(n){c=r();if(c==-1)throw new RuntimeException("EOF after sign for int");}if(c<'0'||c>'9')throw new RuntimeException("Invalid char for int");int res=0;while(c!=-1&&c>='0'&&c<='9'){res=res*10+(c-'0');c=r();}return n?-res:res;}

        public CharArrayReadResult readStringAsChars() {
            byte c = r(); while (c != -1 && c <= ' ') c = r(); if (c == -1) throw new RuntimeException("EOF for string");
            int cnt = 0;
            while (c != -1 && c > ' ') {
                if (cnt == charBuf.length) {
                    char[] newCharBuf = new char[charBuf.length * 2];
                    System.arraycopy(charBuf, 0, newCharBuf, 0, charBuf.length);
                    charBuf = newCharBuf;
                }
                charBuf[cnt++] = (char)c;
                c = r();
            }
            char[] resultChars = new char[cnt];
            System.arraycopy(charBuf, 0, resultChars, 0, cnt);
            return new CharArrayReadResult(resultChars, cnt);
        }
    }
    static class FastWriter {
        private final byte[]bf=new byte[1<<16];private int p=0;private final byte[]dg=new byte[20];
        public void write(byte b){if(p==bf.length)flush();bf[p++]=b;}
        public void print(int x){ // Corrected Integer.MIN_VALUE handling
            if(x==0){write((byte)'0');return;}
            if(x==Integer.MIN_VALUE){
                write((byte)'-');write((byte)'2');write((byte)'1');write((byte)'4');
                write((byte)'7');write((byte)'4');write((byte)'8');write((byte)'3');
                write((byte)'6');write((byte)'4');write((byte)'8');
                return;
            }
            boolean n=x<0;if(n)x=-x;int l=0;while(x>0){dg[l++]=(byte)(x%10+'0');x/=10;}
            if(n)write((byte)'-');for(int i=l-1;i>=0;i--)write(dg[i]);
        }
        public void print(char[] arr, int len) {
            for (int i = 0; i < len; i++) {
                write((byte) arr[i]);
            }
        }
        public void flush(){if(p>0){try{System.out.write(bf,0,p);p=0;}catch(Exception e){throw new RuntimeException(e);}}}public void close(){flush();}}
    }