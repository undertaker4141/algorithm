// 沒有 import 語句

class Main {

    // --- CoWord 類別 (保持不變) ---
    static class CoWord {
        String word; int count;
        CoWord(String w, int c) { word = w; count = c; }
    }

    // --- 開放定址線性探測內部 Map (String -> int count) ---
    static class OpenAddressingInnerMap {
        private String[] keys;
        private int[] counts;
        private int capacity;
        private int size; // 實際元素數量
        private int slotsUsed; // 包括 DELETED 的槽位數量
        private static final String TOMBSTONE = new String(""); // 特殊的墓碑標記
        private static final float LOAD_FACTOR_THRESHOLD = 0.6f; // 開放定址的負載因子通常設低一些

        public OpenAddressingInnerMap(int initialCapacity) {
            int cap = 16; // 最小容量
            while (cap < initialCapacity) cap <<= 1; // 調整為2的冪，有利於取模
            this.capacity = cap;
            this.keys = new String[this.capacity];
            this.counts = new int[this.capacity];
            this.size = 0;
            this.slotsUsed = 0;
        }

        private int hash(String key) {
            int h = 0;
            if (key == null) return 0; // 理論上 key 不應為 null
            for (int i = 0; i < key.length(); i++) h = (31 * h + key.charAt(i));
            return (h & 0x7FFFFFFF) & (capacity - 1); // 因為 capacity 是2的冪，可用位運算代替 %
        }

        private void rehash() {
            String[] oldKeys = keys;
            int[] oldCounts = counts;
            int oldCapacity = capacity;

            capacity <<= 1; // 容量翻倍
            if (capacity <= 0) capacity = oldCapacity + (oldCapacity>>1); // 防止溢位後變負或0
            if (capacity <=0) capacity = Integer.MAX_VALUE & (~(1<<31)); // 最後的保護


            keys = new String[capacity];
            counts = new int[capacity];
            size = 0;
            slotsUsed = 0;

            for (int i = 0; i < oldCapacity; i++) {
                if (oldKeys[i] != null && oldKeys[i] != TOMBSTONE) {
                    // 直接插入，因為是從舊表來的，計數是已知的
                    putForRehash(oldKeys[i], oldCounts[i]);
                }
            }
        }
        
        // 專用於 rehash 的 put，不檢查負載因子，直接插入
        private void putForRehash(String key, int countValue) {
            int index = hash(key);
            while (keys[index] != null) { // 新表是空的，所以只找空位
                index = (index + 1) & (capacity - 1);
            }
            keys[index] = key;
            counts[index] = countValue;
            size++;
            slotsUsed++; // rehash時，插入的都是有效元素
        }

        public void increment(String key) {
            if (key == null) throw new IllegalArgumentException("Key cannot be null");
            // 檢查是否需要擴容 (基於 slotsUsed，包括墓碑)
            if ((float) (slotsUsed + 1) / capacity > LOAD_FACTOR_THRESHOLD) {
                rehash();
            }

            int index = hash(key);
            int startIndex = index;
            int tombstoneIndex = -1;

            while (keys[index] != null) {
                if (keys[index] == TOMBSTONE) {
                    if (tombstoneIndex == -1) tombstoneIndex = index;
                } else if (key.equals(keys[index])) {
                    counts[index]++;
                    return;
                }
                index = (index + 1) & (capacity - 1);
                if (index == startIndex) { // 繞回一圈，表滿了 (理論上 rehash 會避免)
                     rehash(); // 強制 rehash
                     // 重新計算 hash 和 index
                     index = hash(key); 
                     startIndex = index;
                     tombstoneIndex = -1; 
                     // 重新查找，因為 rehash 後位置可能改變
                     while(keys[index] != null) {
                         if (keys[index] == TOMBSTONE) { if (tombstoneIndex == -1) tombstoneIndex = index; }
                         else if (key.equals(keys[index])) { counts[index]++; return; }
                         index = (index + 1) & (capacity - 1);
                         if (index == startIndex) throw new RuntimeException("Map full after rehash for increment - should not happen");
                     }
                     // 如果還是沒找到，則插入到新計算的空位
                }
            }

            // 到達空槽或可用的墓碑槽
            if (tombstoneIndex != -1) {
                index = tombstoneIndex; // 使用墓碑槽
                // slotsUsed 不變，因為墓碑被覆蓋
            } else {
                slotsUsed++; // 使用了新的空槽
            }
            
            keys[index] = key;
            counts[index] = 1;
            size++;
        }
        
        public boolean isEmpty() { return size == 0; }

        public CoWord[] getAllEntries() {
            if (size == 0) return new CoWord[0];
            CoWord[] entries = new CoWord[size];
            int entryIdx = 0;
            for (int i = 0; i < capacity; i++) {
                if (keys[i] != null && keys[i] != TOMBSTONE) {
                    if (entryIdx < size) entries[entryIdx++] = new CoWord(keys[i], counts[i]);
                    else break; 
                }
            }
            // 如果 entryIdx != size，說明 size 維護有誤，或遍歷提前終止
            if (entryIdx != size) {
                CoWord[] actual = new CoWord[entryIdx];
                System.arraycopy(entries, 0, actual, 0, entryIdx);
                return actual;
            }
            return entries;
        }
    }

    // --- 開放定址線性探測外部 Map (String -> OpenAddressingInnerMap) ---
    static class OpenAddressingOuterMap {
        private String[] keys;
        private OpenAddressingInnerMap[] values;
        private int capacity;
        private int size;
        private int slotsUsed;
        private static final String TOMBSTONE = new String("");
        private static final float LOAD_FACTOR_THRESHOLD = 0.6f;

        public OpenAddressingOuterMap(int initialCapacity) {
            int cap = 16; while (cap < initialCapacity) cap <<= 1;
            this.capacity = cap;
            this.keys = new String[this.capacity];
            this.values = new OpenAddressingInnerMap[this.capacity];
            this.size = 0; this.slotsUsed = 0;
        }
        private int hash(String key) { /* ... */ int h=0;if(key==null)return 0;for(int i=0;i<key.length();i++)h=(31*h+key.charAt(i));return(h&0x7FFFFFFF)&(capacity-1);}
        
        private void rehash() {
            String[] oldKeys = keys; OpenAddressingInnerMap[] oldValues = values; int oldCapacity = capacity;
            capacity <<= 1; if(capacity <=0) capacity = oldCapacity + (oldCapacity>>1); if(capacity <=0) capacity = Integer.MAX_VALUE & (~(1<<31));
            keys = new String[capacity]; values = new OpenAddressingInnerMap[capacity];
            size = 0; slotsUsed = 0;
            for (int i = 0; i < oldCapacity; i++) {
                if (oldKeys[i] != null && oldKeys[i] != TOMBSTONE) {
                    putForRehash(oldKeys[i], oldValues[i]);
                }
            }
        }
        private void putForRehash(String key, OpenAddressingInnerMap value) {
            int index = hash(key);
            while (keys[index] != null) index = (index + 1) & (capacity - 1);
            keys[index] = key; values[index] = value; size++; slotsUsed++;
        }

        // put 用於首次插入新的 InnerMap
        public void put(String key, OpenAddressingInnerMap value) {
            if (key == null) throw new IllegalArgumentException("Key cannot be null");
            if ((float)(slotsUsed + 1) / capacity > LOAD_FACTOR_THRESHOLD) rehash();

            int index = hash(key);
            int startIndex = index;
            int tombstoneIndex = -1;

            while (keys[index] != null) {
                if (keys[index] == TOMBSTONE) { if (tombstoneIndex == -1) tombstoneIndex = index;}
                else if (key.equals(keys[index])) { values[index] = value; return; } // 更新
                index = (index + 1) & (capacity - 1);
                if (index == startIndex) { 
                    rehash(); 
                    index = hash(key); startIndex = index; tombstoneIndex = -1;
                     while(keys[index] != null) {
                         if (keys[index] == TOMBSTONE) { if (tombstoneIndex == -1) tombstoneIndex = index; }
                         else if (key.equals(keys[index])) { values[index] = value; return; }
                         index = (index + 1) & (capacity - 1);
                         if (index == startIndex) throw new RuntimeException("OuterMap full after rehash for put - should not happen");
                     }
                }
            }
            if (tombstoneIndex != -1) { index = tombstoneIndex;} else { slotsUsed++;}
            keys[index] = key; values[index] = value; size++;
        }

        public OpenAddressingInnerMap get(String key) {
            if (key == null) return null;
            int index = hash(key);
            int startIndex = index;
            while (keys[index] != null) {
                if (keys[index] != TOMBSTONE && key.equals(keys[index])) return values[index];
                index = (index + 1) & (capacity - 1);
                if (index == startIndex) break;
            }
            return null;
        }
    }

    // --- MergeSort (保持不變) ---
    private static int compareCoWords(CoWord a, CoWord b) { if(a.count!=b.count)return b.count-a.count;return a.word.compareTo(b.word);}
    private static void mergeSort(CoWord[]a,int l,int r){if(l<r){int m=l+(r-l)/2;mergeSort(a,l,m);mergeSort(a,m+1,r);merge(a,l,m,r);}}
    private static void merge(CoWord[]a,int l,int m,int r){int n1=m-l+1,n2=r-m;CoWord[]L=new CoWord[n1],R=new CoWord[n2];for(int i=0;i<n1;i++)L[i]=a[l+i];for(int j=0;j<n2;j++)R[j]=a[m+1+j];int i=0,j=0,k=l;while(i<n1&&j<n2){if(compareCoWords(L[i],R[j])<=0)a[k++]=L[i++];else a[k++]=R[j++];}while(i<n1)a[k++]=L[i++];while(j<n2)a[k++]=R[j++];}

    // --- Main 邏輯 ---
    public static void main(String[] args) {
        FastReader reader = null; FastWriter writer = null;
        try {
            reader = new FastReader(); writer = new FastWriter();
            int N = reader.readInt(); int M = reader.readInt();

            // 估算 OuterMap 容量，N最大10^5，詞對最多2*10^5個詞
            OpenAddressingOuterMap coOccurrences = new OpenAddressingOuterMap(N * 2 / 5 * 2); //  N*2*0.4 -> 0.8N, 再乘2確保初始容量

            for (int i = 0; i < N; i++) {
                String word1 = reader.readString(); String word2 = reader.readString();
                addCoOccurrence(coOccurrences, word1, word2);
                addCoOccurrence(coOccurrences, word2, word1);
            }

            for (int i = 0; i < M; i++) {
                String queryWord = reader.readString(); int K = reader.readInt();
                OpenAddressingInnerMap innerMap = coOccurrences.get(queryWord);

                if (innerMap == null || innerMap.isEmpty()) {
                     writer.write((byte) '\n'); continue;
                }
                CoWord[] results = innerMap.getAllEntries();
                if (results == null || results.length == 0) {
                    writer.write((byte)'\n'); continue;
                }
                mergeSort(results, 0, results.length - 1);
                int countToPrint = K < results.length ? K : results.length;
                for (int j = 0; j < countToPrint; j++) {
                    writer.print(results[j].word);
                    if (j < countToPrint - 1) writer.write((byte) ' ');
                }
                writer.write((byte) '\n');
            }
        } catch (RuntimeException e) { throw e;
        } finally { if (writer != null) { try { writer.flush(); writer.close(); } catch (RuntimeException e) { throw e; }}}
    }

    static void addCoOccurrence(OpenAddressingOuterMap outerMap, String w1, String w2) {
        OpenAddressingInnerMap iMap = outerMap.get(w1);
        if (iMap == null) {
            // 估算 InnerMap 容量，一個詞的共現詞不會太多？設個幾十到一百
            iMap = new OpenAddressingInnerMap(64);
            outerMap.put(w1, iMap);
        }
        iMap.increment(w2);
    }

    // --- FastReader 和 FastWriter (保持之前能AC的版本) ---
    static class FastReader { /* ... 與之前版本相同 ... */
        private final byte[] buffer = new byte[1 << 16]; private int pos = 0, size = 0;
        public FastReader() { try { fillBuffer(); } catch (Exception e) { throw new RuntimeException("FR init", e); } }
        private void fillBuffer() { try { size = System.in.read(buffer); pos = 0; } catch (Exception e) { throw new RuntimeException("FR fill", e); } }
        private byte read() { try { if (pos >= size) { fillBuffer(); if (size <= 0) return -1; } return buffer[pos++]; } catch (Exception e) { throw new RuntimeException("FR read", e); } }
        public int readInt() { byte c=read(); while(c!=-1&&(c<'0'||c>'9')&&c!='-')c=read(); if(c==-1)throw new RuntimeException("EOF int"); boolean n=(c=='-'); if(n){c=read();if(c==-1)throw new RuntimeException("EOF sign");} if(c<'0'||c>'9')throw new RuntimeException("Invalid int"); int r=0; while(c!=-1&&c>='0'&&c<='9'){r=r*10+(c-'0');c=read();} return n?-r:r; }
        public String readString() { byte c=read(); while(c!=-1&&c<=' ')c=read(); if(c==-1)throw new RuntimeException("EOF str"); byte[]b=new byte[64]; int cnt=0; while(c!=-1&&c>' '){if(cnt==b.length){byte[]nb=new byte[b.length*2];System.arraycopy(b,0,nb,0,b.length);b=nb;}b[cnt++]=c;c=read();} return new String(b,0,cnt); }
    }
    static class FastWriter { /* ... 與之前版本相同 ... */
        private final byte[] buffer = new byte[1 << 16]; private int pos = 0; private final byte[] digits = new byte[20];
        public void write(byte b) { if (pos == buffer.length) flush(); buffer[pos++] = b; }
        public void print(int x) { if(x==0){write((byte)'0');return;}if(x==Integer.MIN_VALUE){print("-2147483648");return;}boolean n=x<0;if(n)x=-x;int l=0;while(x>0){digits[l++]=(byte)(x%10+'0');x/=10;}if(n)write((byte)'-');for(int i=l-1;i>=0;i--)write(digits[i]);}
        public void print(String s) { for(int i=0,sl=s.length();i<sl;i++)write((byte)s.charAt(i));}
        public void flush() { if(pos>0){try{System.out.write(buffer,0,pos);pos=0;}catch(Exception e){throw new RuntimeException("FW flush",e);}}}
        public void close() { flush(); }
    }
}