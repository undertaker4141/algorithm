class Main {

    static final class CharArrayReadResult {
        final char[] chars;
        final int length;
        CharArrayReadResult(final char[] c, final int l) { chars = c; length = l; }
    }

    static final class CoWord {
        final char[] wordChars;
        final int wordLen;
        int count;
        CoWord(final char[] wc, final int wl, final int c) {
            wordChars = wc;
            wordLen = wl;
            count = c;
        }
    }

    static final class CharArrayUtils {
        private CharArrayUtils() {}

        static int hashCode(final char[] arr, final int len) {
            if (arr == null || len == 0) return 0;
            int h = 0;
            for (int i = 0; i < len; i++) {
                h = 31 * h + arr[i];
            }
            return h;
        }

        static boolean equals(final char[] a, final int lenA, final char[] b, final int lenB) {
            if (a == b && lenA == lenB) return true;
            if (a == null || b == null) return false;
            if (lenA != lenB) return false;
            for (int i = 0; i < lenA; i++) {
                if (a[i] != b[i]) return false;
            }
            return true;
        }

        static int compareTo(final char[] a, final int lenA, final char[] b, final int lenB) {
            final int n = Math.min(lenA, lenB);
            for (int i = 0; i < n; i++) {
                if (a[i] != b[i]) {
                    return a[i] - b[i];
                }
            }
            return lenA - lenB;
        }
    }

    static final class OpenAddressingInnerMapForChars {
        private char[][] keys; private int[] keyLengths; private int[] counts;
        private int capacity; private int size; private int slotsUsed;
        private static final char[] TOMBSTONE = new char[0];
        private static final float LOAD_FACTOR_THRESHOLD = 0.5f;

        public OpenAddressingInnerMapForChars(int initialCapacity) {
            int cap = 16;
            if (initialCapacity > 0) {
                cap = Integer.highestOneBit(initialCapacity - 1);
                if (cap <= 0) cap = initialCapacity;
                cap <<=1;
                if (cap <= 0) cap = 1 << 28;
            }
            if (cap < 16) cap = 16;
            this.capacity = cap;
            this.keys = new char[this.capacity][];
            this.keyLengths = new int[this.capacity];
            this.counts = new int[this.capacity];
        }

        private int hash(final char[] key, final int len) { return (CharArrayUtils.hashCode(key, len) & 0x7FFFFFFF) & (capacity - 1); }

        private void rehash() {final char[][]ok=keys;final int[]ol=keyLengths;final int[]oc=counts;final int oCap=capacity;int nCap=capacity<<1;if(nCap<=0||nCap>(1<<28)){if(capacity<(1<<28))nCap=(1<<28);else throw new RuntimeException("InnerMap rehash limit");}capacity=nCap;keys=new char[capacity][];keyLengths=new int[capacity];counts=new int[capacity];size=0;slotsUsed=0;for(int i=0;i<oCap;i++){if(ok[i]!=null&&ok[i]!=TOMBSTONE)putForRehash(ok[i],ol[i],oc[i]);}}
        private void putForRehash(final char[]k,final int kl,final int v){int i=hash(k,kl);while(keys[i]!=null)i=(i+1)&(capacity-1);keys[i]=k;keyLengths[i]=kl;counts[i]=v;size++;slotsUsed++;}

        public void increment(final char[] key, final int len) {
            if(key==null)throw new IllegalArgumentException();if((float)(slotsUsed+1)/capacity > LOAD_FACTOR_THRESHOLD) rehash();
            int index = hash(key,len); int startIndex = index; int tombstoneIndex = -1;
            while(keys[index]!=null){
                if(keys[index]==TOMBSTONE){ if(tombstoneIndex == -1) tombstoneIndex = index; }
                else if(CharArrayUtils.equals(key, len, keys[index], keyLengths[index])){ counts[index]++; return; }
                index=(index+1)&(capacity-1);
                if(index==startIndex){
                    rehash(); index = hash(key, len); startIndex = index; tombstoneIndex = -1; int rpc = 0;
                    while(keys[index]!=null) {
                        if(keys[index]==TOMBSTONE){ if(tombstoneIndex == -1) tombstoneIndex = index; }
                        else if(CharArrayUtils.equals(key,len,keys[index],keyLengths[index])){ counts[index]++; return; }
                        index = (index + 1) & (capacity-1);
                        if(index == startIndex && ++rpc > 1) throw new RuntimeException("InnerMap full increment after rehash");
                        if(index == startIndex && rpc <=1 ) continue;
                    }
                    break;
                }
            }
            if(tombstoneIndex != -1){ index = tombstoneIndex; } else { slotsUsed++; }
            keys[index]=key; keyLengths[index]=len; counts[index]=1; size++;
        }
        public boolean isEmpty(){return size==0;}
        public CoWord[] getAllEntries(){if(size==0)return new CoWord[0];final CoWord[]e=new CoWord[size];int ei=0;for(int i=0;i<capacity;i++){if(keys[i]!=null&&keys[i]!=TOMBSTONE){if(ei<size)e[ei++]=new CoWord(keys[i],keyLengths[i],counts[i]);else break;}}if(ei!=size){final CoWord[]a=new CoWord[ei];System.arraycopy(e,0,a,0,ei);return a;}return e;}
    }

    static final class OpenAddressingOuterMapForChars {
        private char[][] keys; private int[] keyLengths; private OpenAddressingInnerMapForChars[] values;
        private int capacity; private int size; private int slotsUsed;
        private static final char[] TOMBSTONE = new char[0];
        public static final float LOAD_FACTOR_THRESHOLD = 0.5f;

        public OpenAddressingOuterMapForChars(int initialCapacity) {
            int cap = 16;
            if (initialCapacity > 0) {
                cap = Integer.highestOneBit(initialCapacity - 1); if (cap <= 0) cap = initialCapacity; cap <<=1; if (cap <= 0) cap = 1 << 21;
            }
            if (cap < 16) cap = 16;
            this.capacity=cap;this.keys=new char[cap][];this.keyLengths=new int[cap];this.values=new OpenAddressingInnerMapForChars[cap];
        }
        private int hash(final char[] k, final int l){return(CharArrayUtils.hashCode(k,l)&0x7FFFFFFF)&(capacity-1);}
        private void rehash(){final char[][]ok=keys;final int[]ol=keyLengths;final OpenAddressingInnerMapForChars[]ov=values;final int oCap=capacity;int nCap=capacity<<1;if(nCap<=0||nCap>(1<<28)){if(capacity<(1<<28))nCap=(1<<28);else throw new RuntimeException("OuterMap rehash limit");}capacity=nCap;keys=new char[capacity][];keyLengths=new int[capacity];values=new OpenAddressingInnerMapForChars[capacity];size=0;slotsUsed=0;for(int i=0;i<oCap;i++){if(ok[i]!=null&&ok[i]!=TOMBSTONE)putForRehash(ok[i],ol[i],ov[i]);}}
        private void putForRehash(final char[]k,final int kl,final OpenAddressingInnerMapForChars v){int i=hash(k,kl);while(keys[i]!=null)i=(i+1)&(capacity-1);keys[i]=k;keyLengths[i]=kl;values[i]=v;size++;slotsUsed++;}
        public void put(final char[]k,final int kl,final OpenAddressingInnerMapForChars v){if(k==null)throw new IllegalArgumentException();if((float)(slotsUsed+1)/capacity>LOAD_FACTOR_THRESHOLD)rehash();int i=hash(k,kl);int si=i;int ti=-1;while(keys[i]!=null){if(keys[i]==TOMBSTONE){if(ti==-1)ti=i;}else if(CharArrayUtils.equals(k,kl,keys[i],keyLengths[i])){values[i]=v;return;}i=(i+1)&(capacity-1);if(i==si){rehash();i=hash(k,kl);si=i;ti=-1;int rpc=0;while(keys[i]!=null){if(keys[i]==TOMBSTONE){if(ti==-1)ti=i;}else if(CharArrayUtils.equals(k,kl,keys[i],keyLengths[i])){values[i]=v;return;}i=(i+1)&(capacity-1);if(i==si&& ++rpc>1)throw new RuntimeException("OuterMap full put after rehash");if(i==si && rpc <=1) continue;}}}if(ti!=-1)i=ti;else slotsUsed++;keys[i]=k;keyLengths[i]=kl;values[i]=v;size++;}
        public OpenAddressingInnerMapForChars get(final char[]k, final int kl){if(k==null)return null;int i=hash(k,kl);int si=i;while(keys[i]!=null){if(keys[i]!=TOMBSTONE&&CharArrayUtils.equals(k,kl,keys[i],keyLengths[i]))return values[i];i=(i+1)&(capacity-1);if(i==si)break;}return null;}
    }

    private static int compareCoWords(final CoWord a,final CoWord b){if(a.count!=b.count)return b.count-a.count;return CharArrayUtils.compareTo(a.wordChars,a.wordLen,b.wordChars,b.wordLen);}
    private static void mergeSort(final CoWord[]arr,final int l,final int r){if(l<r){final int m=l+(r-l)/2;mergeSort(arr,l,m);mergeSort(arr,m+1,r);merge(arr,l,m,r);}}
    private static void merge(final CoWord[]a,final int l,final int m,final int r){final int n1=m-l+1,n2=r-m;final CoWord[]L=new CoWord[n1],R=new CoWord[n2];for(int i=0;i<n1;++i)L[i]=a[l+i];for(int j=0;j<n2;++j)R[j]=a[m+1+j];int i=0,j=0,k=l;while(i<n1&&j<n2){if(compareCoWords(L[i],R[j])<=0)a[k++]=L[i++];else a[k++]=R[j++];}while(i<n1)a[k++]=L[i++];while(j<n2)a[k++]=R[j++];}

    public static void main(final String[] args) {
        FastReader reader = null; FastWriter writer = null;
        try {
            reader = new FastReader(); writer = new FastWriter();
            final int N = reader.readInt(); final int M = reader.readInt();
            final OpenAddressingOuterMapForChars coOccurrences = new OpenAddressingOuterMapForChars(1 << 19);

            for(int i=0;i<N;i++){
                final CharArrayReadResult w1_arr = reader.readStringAsChars();
                final CharArrayReadResult w2_arr = reader.readStringAsChars();
                addCoOccurrence(coOccurrences, w1_arr.chars, w1_arr.length, w2_arr.chars, w2_arr.length);
                addCoOccurrence(coOccurrences, w2_arr.chars, w2_arr.length, w1_arr.chars, w1_arr.length);
            }
            for(int i=0;i<M;i++){
                final CharArrayReadResult q_arr = reader.readStringAsChars(); final int K=reader.readInt();
                final OpenAddressingInnerMapForChars im=coOccurrences.get(q_arr.chars, q_arr.length);
                if(im==null||im.isEmpty()){writer.write((byte)'\n');continue;}
                final CoWord[]rs=im.getAllEntries();
                if(rs==null||rs.length==0||K<=0){writer.write((byte)'\n');continue;}
                mergeSort(rs, 0, rs.length - 1);
                final int pt=K<rs.length?K:rs.length;
                for(int j=0;j<pt;j++){writer.print(rs[j].wordChars, rs[j].wordLen);if(j<pt-1)writer.write((byte)' ');}
                writer.write((byte)'\n');
            }
        } catch (final RuntimeException e) { throw e;
        } finally { if (writer != null) { try { writer.flush(); writer.close(); } catch (final RuntimeException e) { throw e; }}}
    }

    static void addCoOccurrence(final OpenAddressingOuterMapForChars oMap, final char[] k1, final int l1, final char[] k2, final int l2) {
        OpenAddressingInnerMapForChars iMap = oMap.get(k1, l1);
        if (iMap == null) {
            iMap = new OpenAddressingInnerMapForChars(64);
            oMap.put(k1, l1, iMap);
        }
        iMap.increment(k2, l2);
    }

    static final class FastReader {
        private final byte[]bf=new byte[1<<16];private int p=0,s=0;
        private char[] charBuf = new char[128];

        public FastReader(){try{fill();}catch(Exception e){throw new RuntimeException(e);}}
        private void fill(){try{s=System.in.read(bf);p=0;}catch(Exception e){throw new RuntimeException(e);}}
        private byte r(){try{if(p>=s){fill();if(s<=0)return-1;}return bf[p++];}catch(Exception e){throw new RuntimeException(e);}}
        public int readInt(){ byte c=r();while(c!=-1&&(c<'0'||c>'9')&&c!='-')c=r();if(c==-1)throw new RuntimeException("EOF for int");boolean n=(c=='-');if(n){c=r();if(c==-1)throw new RuntimeException("EOF after sign for int");}if(c<'0'||c>'9')throw new RuntimeException("Invalid char for int");int res=0;while(c!=-1&&c>='0'&&c<='9'){res=res*10+(c-'0');c=r();}return n?-res:res;}
        public CharArrayReadResult readStringAsChars(){
            byte c=r(); while(c!=-1&&c<=' ')c=r(); if(c==-1)throw new RuntimeException("EOF for string");
            int ct=0;
            while(c!=-1&&c>' '){
                if(ct==charBuf.length){ char[]ncb=new char[charBuf.length*2]; System.arraycopy(charBuf,0,ncb,0,charBuf.length); charBuf=ncb; }
                charBuf[ct++]=(char)c;
                c=r();
            }
            final char[]rc=new char[ct]; System.arraycopy(charBuf,0,rc,0,ct);
            return new CharArrayReadResult(rc,ct);
        }
    }
    static final class FastWriter {
        private final byte[]bf=new byte[1<<16];private int p=0;private final byte[]dg=new byte[20];
        public void write(final byte b){if(p==bf.length)flush();bf[p++]=b;}
        public void print(int x){if(x==0){write((byte)'0');return;}
            if(x==Integer.MIN_VALUE){write((byte)'-');write((byte)'2');write((byte)'1');write((byte)'4');write((byte)'7');write((byte)'4');write((byte)'8');write((byte)'3');write((byte)'6');write((byte)'4');write((byte)'8');return;}
            boolean n=x<0;if(n)x=-x;int l=0;while(x>0){dg[l++]=(byte)(x%10+'0');x/=10;}
            if(n)write((byte)'-');for(int i=l-1;i>=0;i--)write(dg[i]);
        }
        public void print(final char[]a,final int l){for(int i=0;i<l;i++)write((byte)a[i]);}
        public void flush(){if(p>0){try{System.out.write(bf,0,p);p=0;}catch(Exception e){throw new RuntimeException(e);}}}
        public void close(){flush();}
    }
}