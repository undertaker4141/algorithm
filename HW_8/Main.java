// 沒有 import 語句

class Main {

    // --- 用於 FastReader 返回 char[] 和長度 ---
    static class CharArrayReadResult { /* ... */ char[] chars; int length; CharArrayReadResult(char[] c, int l) { chars = c; length = l; }}

    // --- CoWord (使用 char[] 和 len) ---
    static class CoWord { /* ... */ char[] wordChars; int wordLen; int count; CoWord(char[] wc, int wl, int c) { wordChars = wc; wordLen = wl; count = c; }}

    // --- char[] 的哈希和比較工具 ---
    static class CharArrayUtils { /* ... (hashCode, equals, compareTo 不變) ... */
        static int hashCode(char[]a,int l){if(a==null||l==0)return 0;int h=0;for(int i=0;i<l;i++)h=31*h+a[i];return h;}
        static boolean equals(char[]a,int la,char[]b,int lb){if(a==b&&la==lb)return true;if(a==null||b==null)return false;if(la!=lb)return false;for(int i=0;i<la;i++)if(a[i]!=b[i])return false;return true;}
        static int compareTo(char[]a,int la,char[]b,int lb){int n=Math.min(la,lb);for(int i=0;i<n;i++)if(a[i]!=b[i])return a[i]-b[i];return la-lb;}
    }

    // --- 開放定址線性探測內部 Map (char[] key -> int count) ---
    static class OpenAddressingInnerMapForChars {
        private char[][] keys; private int[] keyLengths; private int[] counts;
        private int capacity; private int size; private int slotsUsed;
        // *** 修正 TOMBSTONE ***
        private static final char[] TOMBSTONE = new char[0]; // 特殊的 char[] 實例作為墓碑
        private static final float LOAD_FACTOR_THRESHOLD = 0.6f;

        public OpenAddressingInnerMapForChars(int ic) { /* ...構造函數不變... */ int c=16;if(ic>0)c=Integer.highestOneBit(ic-1)<<1;if(c<=0)c=16;if(c<16)c=16;this.capacity=c;this.keys=new char[c][];this.keyLengths=new int[c];this.counts=new int[c];this.size=0;this.slotsUsed=0;}
        private int hash(char[] k, int l){return(CharArrayUtils.hashCode(k,l)&0x7FFFFFFF)&(capacity-1);}

        private void rehash() {
            char[][] oldKeys=keys; int[] ol=keyLengths; int[] oc=counts; int oCap=capacity;
            capacity<<=1; if(capacity<=0||capacity>(1<<28)){capacity=oCap;}
            keys=new char[capacity][]; keyLengths=new int[capacity]; counts=new int[capacity];
            size=0; slotsUsed=0;
            for(int i=0;i<oCap;i++){
                // *** 修正 TOMBSTONE 比較 ***
                if(oldKeys[i]!=null && oldKeys[i]!=TOMBSTONE) putForRehash(oldKeys[i],ol[i],oc[i]);
            }
        }
        private void putForRehash(char[] k, int kl, int v){int i=hash(k,kl);while(keys[i]!=null)i=(i+1)&(capacity-1);keys[i]=k;keyLengths[i]=kl;counts[i]=v;size++;slotsUsed++;}

        public void increment(char[] key, int len) {
            if(key==null)throw new IllegalArgumentException();if((float)(slotsUsed+1)/capacity>LOAD_FACTOR_THRESHOLD)rehash();
            int i=hash(key,len);int si=i;int ti=-1;
            while(keys[i]!=null){
                // *** 修正 TOMBSTONE 比較 ***
                if(keys[i]==TOMBSTONE){if(ti==-1)ti=i;}
                else if(CharArrayUtils.equals(key,len,keys[i],keyLengths[i])){counts[i]++;return;}
                i=(i+1)&(capacity-1);
                if(i==si){rehash();i=hash(key,len);si=i;ti=-1;
                    while(keys[i]!=null){
                        // *** 修正 TOMBSTONE 比較 ***
                        if(keys[i]==TOMBSTONE){if(ti==-1)ti=i;}
                        else if(CharArrayUtils.equals(key,len,keys[i],keyLengths[i])){counts[i]++;return;}
                        i=(i+1)&(capacity-1);if(i==si)throw new RuntimeException("InnerMapChars full increment");
                    }
                }
            }
            if(ti!=-1)i=ti;else slotsUsed++;
            keys[i]=key;keyLengths[i]=len;counts[i]=1;size++;
        }
        public boolean isEmpty(){return size==0;}
        public CoWord[] getAllEntries(){if(size==0)return new CoWord[0];CoWord[]e=new CoWord[size];int ei=0;for(int i=0;i<capacity;i++){
            // *** 修正 TOMBSTONE 比較 ***
            if(keys[i]!=null&&keys[i]!=TOMBSTONE){if(ei<size)e[ei++]=new CoWord(keys[i],keyLengths[i],counts[i]);else break;}}
            if(ei!=size){CoWord[]a=new CoWord[ei];System.arraycopy(e,0,a,0,ei);return a;}return e;
        }
    }

    // --- 開放定址線性探測外部 Map (char[] key -> InnerMap) ---
    static class OpenAddressingOuterMapForChars {
        private char[][] keys; private int[] keyLengths; private OpenAddressingInnerMapForChars[] values;
        private int capacity; private int size; private int slotsUsed;
        // *** 修正 TOMBSTONE ***
        private static final char[] TOMBSTONE = new char[0]; // 特殊的 char[] 實例作為墓碑
        public static final float LOAD_FACTOR_THRESHOLD = 0.6f;

        public OpenAddressingOuterMapForChars(int ic){/* ...構造函數不變... */ int c=16;if(ic>0)c=Integer.highestOneBit(ic-1)<<1;if(c<=0)c=1<<20;if(c<16)c=16;this.capacity=c;this.keys=new char[c][];this.keyLengths=new int[c];this.values=new OpenAddressingInnerMapForChars[c];this.size=0;this.slotsUsed=0;}
        private int hash(char[] k, int l){return(CharArrayUtils.hashCode(k,l)&0x7FFFFFFF)&(capacity-1);}
        private void rehash(){char[][]ok=keys;int[]ol=keyLengths;OpenAddressingInnerMapForChars[]ov=values;int oCap=capacity;capacity<<=1;if(capacity<=0||capacity>(1<<28)){capacity=oCap;}keys=new char[capacity][];keyLengths=new int[capacity];values=new OpenAddressingInnerMapForChars[capacity];size=0;slotsUsed=0;for(int i=0;i<oCap;i++){
            // *** 修正 TOMBSTONE 比較 ***
            if(ok[i]!=null&&ok[i]!=TOMBSTONE)putForRehash(ok[i],ol[i],ov[i]);}}
        private void putForRehash(char[]k,int kl,OpenAddressingInnerMapForChars v){int i=hash(k,kl);while(keys[i]!=null)i=(i+1)&(capacity-1);keys[i]=k;keyLengths[i]=kl;values[i]=v;size++;slotsUsed++;}
        public void put(char[]k,int kl,OpenAddressingInnerMapForChars v){if(k==null)throw new IllegalArgumentException();if((float)(slotsUsed+1)/capacity>LOAD_FACTOR_THRESHOLD)rehash();int i=hash(k,kl);int si=i;int ti=-1;while(keys[i]!=null){
            // *** 修正 TOMBSTONE 比較 ***
            if(keys[i]==TOMBSTONE){if(ti==-1)ti=i;}
            else if(CharArrayUtils.equals(k,kl,keys[i],keyLengths[i])){values[i]=v;return;}
            i=(i+1)&(capacity-1);if(i==si){rehash();i=hash(k,kl);si=i;ti=-1;while(keys[i]!=null){
                // *** 修正 TOMBSTONE 比較 ***
                if(keys[i]==TOMBSTONE){if(ti==-1)ti=i;}
                else if(CharArrayUtils.equals(k,kl,keys[i],keyLengths[i])){values[i]=v;return;}
                i=(i+1)&(capacity-1);if(i==si)throw new RuntimeException();}}}
            if(ti!=-1)i=ti;else slotsUsed++;keys[i]=k;keyLengths[i]=kl;values[i]=v;size++;}
        public OpenAddressingInnerMapForChars get(char[]k, int kl){if(k==null)return null;int i=hash(k,kl);int si=i;while(keys[i]!=null){
            // *** 修正 TOMBSTONE 比較 (這是出錯的行) ***
            if(keys[i]!=TOMBSTONE && CharArrayUtils.equals(k,kl,keys[i],keyLengths[i]))return values[i];
            i=(i+1)&(capacity-1);if(i==si)break;}return null;}
    }

    // --- MergeSort (比較 CoWord, 不變) ---
    private static int compareCoWords(CoWord a,CoWord b){if(a.count!=b.count)return b.count-a.count;return CharArrayUtils.compareTo(a.wordChars,a.wordLen,b.wordChars,b.wordLen);}
    private static void mergeSort(CoWord[]arr,int l,int r){if(l<r){int m=l+(r-l)/2;mergeSort(arr,l,m);mergeSort(arr,m+1,r);merge(arr,l,m,r);}}
    private static void merge(CoWord[]a,int l,int m,int r){int n1=m-l+1,n2=r-m;CoWord[]L=new CoWord[n1],R=new CoWord[n2];for(int i=0;i<n1;++i)L[i]=a[l+i];for(int j=0;j<n2;++j)R[j]=a[m+1+j];int i=0,j=0,k=l;while(i<n1&&j<n2){if(compareCoWords(L[i],R[j])<=0)a[k++]=L[i++];else a[k++]=R[j++];}while(i<n1)a[k++]=L[i++];while(j<n2)a[k++]=R[j++];}

    // --- Main 邏輯 (不變) ---
    public static void main(String[] args) { /* ...與上一版本相同... */ FastReader r=null;FastWriter w=null;try{r=new FastReader();w=new FastWriter();int N=r.readInt(),M=r.readInt();OpenAddressingOuterMapForChars o=new OpenAddressingOuterMapForChars(1<<19);for(int i=0;i<N;i++){CharArrayReadResult w1=r.readStringAsChars(),w2=r.readStringAsChars();addCoOccurrence(o,w1.chars,w1.length,w2.chars,w2.length);addCoOccurrence(o,w2.chars,w2.length,w1.chars,w1.length);}for(int i=0;i<M;i++){CharArrayReadResult q=r.readStringAsChars();int K=r.readInt();OpenAddressingInnerMapForChars im=o.get(q.chars,q.length);if(im==null||im.isEmpty()){w.write((byte)'\n');continue;}CoWord[]rs=im.getAllEntries();if(rs==null||rs.length==0||K<=0){w.write((byte)'\n');continue;}mergeSort(rs,0,rs.length-1);int pt=K<rs.length?K:rs.length;for(int j=0;j<pt;j++){w.print(rs[j].wordChars,rs[j].wordLen);if(j<pt-1)w.write((byte)' ');}w.write((byte)'\n');}}catch(RuntimeException e){throw e;}finally{if(w!=null){try{w.flush();w.close();}catch(RuntimeException e){throw e;}}}}
    static void addCoOccurrence(OpenAddressingOuterMapForChars o,char[]k1,int l1,char[]k2,int l2){OpenAddressingInnerMapForChars i=o.get(k1,l1);if(i==null){i=new OpenAddressingInnerMapForChars(64);o.put(k1,l1,i);}i.increment(k2,l2);}

    // --- FastReader 和 FastWriter (不變) ---
    static class FastReader { /* ... */ private final byte[]bf=new byte[1<<16];private int p=0,s=0;private char[]cb=new char[128];public FastReader(){try{fill();}catch(Exception e){throw new RuntimeException(e);}}private void fill(){try{s=System.in.read(bf);p=0;}catch(Exception e){throw new RuntimeException(e);}}private byte r(){try{if(p>=s){fill();if(s<=0)return-1;}return bf[p++];}catch(Exception e){throw new RuntimeException(e);}}public int readInt(){byte c=r();while(c!=-1&&(c<'0'||c>'9')&&c!='-')c=r();if(c==-1)throw new RuntimeException();boolean n=(c=='-');if(n){c=r();if(c==-1)throw new RuntimeException("EOF after sign for int");}if(c<'0'||c>'9')throw new RuntimeException("Invalid char for int");int res=0;while(c!=-1&&c>='0'&&c<='9'){res=res*10+(c-'0');c=r();}return n?-res:res;}public CharArrayReadResult readStringAsChars(){byte c=r();while(c!=-1&&c<=' ')c=r();if(c==-1)throw new RuntimeException("EOF for string");int ct=0;while(c!=-1&&c>' '){if(ct==cb.length){char[]ncb=new char[cb.length*2];System.arraycopy(cb,0,ncb,0,cb.length);cb=ncb;}cb[ct++]=(char)c;c=r();}char[]rc=new char[ct];System.arraycopy(cb,0,rc,0,ct);return new CharArrayReadResult(rc,ct);}}
    static class FastWriter { /* ... */ private final byte[]bf=new byte[1<<16];private int p=0;private final byte[]dg=new byte[20];public void write(byte b){if(p==bf.length)flush();bf[p++]=b;}public void print(int x){if(x==0){write((byte)'0');return;}if(x==Integer.MIN_VALUE){write((byte)'-');write((byte)'2');write((byte)'1');write((byte)'4');write((byte)'7');write((byte)'4');write((byte)'8');write((byte)'3');write((byte)'6');write((byte)'4');write((byte)'8');return;}boolean n=x<0;if(n)x=-x;int l=0;while(x>0){dg[l++]=(byte)(x%10+'0');x/=10;}if(n)write((byte)'-');for(int i=l-1;i>=0;i--)write(dg[i]);}public void print(char[]a,int l){for(int i=0;i<l;i++)write((byte)a[i]);}public void flush(){if(p>0){try{System.out.write(bf,0,p);p=0;}catch(Exception e){throw new RuntimeException(e);}}}public void close(){flush();}}
}