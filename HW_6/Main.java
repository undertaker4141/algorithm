class Main {

    public static void main(String[] args) throws Exception {
        InitialReader initialReader = new InitialReader();
        int Q = initialReader.readInt();
        int D = initialReader.readInt();
        initialReader.consumeRestOfLine();

        FullInputReader reader = new FullInputReader(initialReader.getBuffer(), initialReader.getPos(),
                initialReader.getCount());

        int[][] queries = new int[Q][];
        int[] tempQueryLine = new int[D + 1];

        for (int i = 0; i < Q; i++) {
            int queryLen = 0;
            while (true) {
                int docIndex = reader.readIntAndConsumeDelimiter();

                if (docIndex == -1) {
                    break;
                }

                if (queryLen < tempQueryLine.length) {
                    tempQueryLine[queryLen++] = docIndex;
                }

                byte delimiter = reader.getLastDelimiter();
                if (delimiter == '\n' || delimiter == -1 || reader.isEOFReached()) {
                    break;
                }
            }

            if (queryLen > 0) {
                queries[i] = new int[queryLen];
                System.arraycopy(tempQueryLine, 0, queries[i], 0, queryLen);
            } else {
                queries[i] = new int[0];
            }
        }

        FastWordSet[] documentSets = new FastWordSet[D];
        char[] wordBuffer = new char[2001];

        for (int i = 0; i < D; i++) {
            documentSets[i] = new FastWordSet();
            while (true) {
                int wordLen = reader.readWordAndConsumeDelimiter(wordBuffer);

                if (wordLen > 0) {
                    documentSets[i].add(wordBuffer, wordLen);
                } else {
                    break;
                }

                byte delimiter = reader.getLastDelimiter();
                if (delimiter == '\n' || delimiter == -1 || reader.isEOFReached()) {
                    break;
                }
            }
        }

        MyOutputWriter writer = new MyOutputWriter();

        for (int i = 0; i < Q; i++) {
            int[] currentQueryIndices = queries[i];

            if (currentQueryIndices.length == 0) {
                writer.println(0);
                continue;
            }

            int smallestSetDocIndex = -1;
            int minSize = Integer.MAX_VALUE;
            boolean hasInvalidDoc = false;

            for (int j = 0; j < currentQueryIndices.length; j++) {
                int docIndex = currentQueryIndices[j];
                if (docIndex < 1 || docIndex > D) {
                    hasInvalidDoc = true;
                    break;
                }

                int docIdx = docIndex - 1;
                int size = documentSets[docIdx].size();

                if (size == 0) {
                    hasInvalidDoc = true;
                    break;
                }

                if (size < minSize) {
                    minSize = size;
                    smallestSetDocIndex = docIdx;
                }
            }

            if (hasInvalidDoc) {
                writer.println(0);
                continue;
            }

            int resultCount = 0;
            WordEntry[] entries = documentSets[smallestSetDocIndex].getEntries();
            int tableSize = documentSets[smallestSetDocIndex].getTableSize();

            for (int j = 0; j < tableSize; j++) {
                WordEntry entry = entries[j];
                while (entry != null) {
                    boolean inAllDocs = true;

                    for (int k = 0; k < currentQueryIndices.length; k++) {
                        int docIdx = currentQueryIndices[k] - 1;
                        if (docIdx == smallestSetDocIndex) {
                            continue;
                        }

                        if (!documentSets[docIdx].contains(entry)) {
                            inAllDocs = false;
                            break;
                        }
                    }

                    if (inAllDocs) {
                        resultCount++;
                    }

                    entry = entry.next;
                }
            }

            writer.println(resultCount);
        }
        writer.flush();
    }
}

class InitialReader {
    private final byte[] buffer = new byte[128];
    private int pos = 0;
    private int count = 0;
    private boolean initialReadDone = false;

    private void ensureRead() throws Exception {
        if (!initialReadDone) {
            count = System.in.read(buffer);
            initialReadDone = true;
        }
    }

    private byte readByte() throws Exception {
        ensureRead();
        if (pos >= count)
            return -1;
        return buffer[pos++];
    }

    public int readInt() throws Exception {
        byte b;
        while (true) {
            b = readByte();
            if (b >= '0' && b <= '9')
                break;
            if (b == -1)
                return -1;
        }

        int value = b - '0';
        while (true) {
            b = readByte();
            if (b < '0' || b > '9')
                break;
            value = value * 10 + (b - '0');
        }

        if (b != -1)
            pos--;
        return value;
    }

    public void consumeRestOfLine() throws Exception {
        byte b;
        while ((b = readByte()) != '\n') {
            if (b == -1)
                break;
        }
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public int getPos() {
        return pos;
    }

    public int getCount() {
        return count;
    }
}

class FullInputReader {
    private byte[] buffer;
    private int pos;
    private int count;
    private boolean eofReached;
    private byte lastDelimiter;

    private static final int BUFFER_SIZE = 1 << 21;

    public FullInputReader(byte[] initialBuffer, int initialPos, int initialCount) throws Exception {
        buffer = new byte[BUFFER_SIZE];
        int remainingBytes = initialCount - initialPos;
        if (remainingBytes > 0) {
            System.arraycopy(initialBuffer, initialPos, buffer, 0, remainingBytes);
            count = remainingBytes;
        } else {
            count = 0;
        }
        pos = 0;
        eofReached = false;
        lastDelimiter = 0;
        if (count < buffer.length / 2) {
            fillBuffer();
        }
    }

    private boolean fillBuffer() throws Exception {
        if (eofReached)
            return false;

        if (pos > 0) {
            if (count > pos) {
                System.arraycopy(buffer, pos, buffer, 0, count - pos);
                count -= pos;
            } else {
                count = 0;
            }
            pos = 0;
        }

        int bytesRead = 0;
        if (count < buffer.length) {
            bytesRead = System.in.read(buffer, count, buffer.length - count);
        }

        if (bytesRead > 0) {
            count += bytesRead;
            return true;
        } else if (bytesRead == -1) {
            eofReached = true;
            return false;
        } else {
            return false;
        }
    }

    private byte readByte() throws Exception {
        if (pos >= count) {
            if (eofReached)
                return -1;
            if (!fillBuffer()) {
                return -1;
            }
            if (pos >= count) {
                eofReached = true;
                return -1;
            }
        }
        return buffer[pos++];
    }

    public int readIntAndConsumeDelimiter() throws Exception {
        lastDelimiter = 0;
        byte b;
        while (true) {
            b = readByte();
            if (b == -1) {
                eofReached = true;
                return -1;
            }
            if (b > ' ')
                break;
        }

        if (b < '0' || b > '9') {
            pos--;
            return -1;
        }

        int value = b - '0';
        while (true) {
            b = readByte();
            if (b < '0' || b > '9')
                break;
            value = value * 10 + (b - '0');
        }

        lastDelimiter = b;
        if (b == -1)
            eofReached = true;

        return value;
    }

    public int readWordAndConsumeDelimiter(char[] wordBuffer) throws Exception {
        lastDelimiter = 0;
        int len = 0;
        byte b;

        while (true) {
            b = readByte();
            if (b == -1) {
                eofReached = true;
                lastDelimiter = -1;
                return 0;
            }
            if (b == '\n') {
                lastDelimiter = '\n';
                return 0;
            }
            if (b != ' ')
                break;
        }

        do {
            if (len < wordBuffer.length) {
                wordBuffer[len++] = (char) b;
            }

            b = readByte();
        } while (b > ' ' && b != '\n');

        lastDelimiter = b;
        if (b == -1)
            eofReached = true;

        return len;
    }

    public byte getLastDelimiter() {
        return lastDelimiter;
    }

    public boolean isEOFReached() {
        return eofReached;
    }
}

class MyOutputWriter {
    private byte[] buffer = new byte[1 << 18];
    private int pos = 0;
    private final byte[] T = new byte[11];

    public void print(int value) {
        if (value == 0) {
            write((byte) '0');
            return;
        }

        int T_pos = 0;

        if (value < 0) {
            write((byte) '-');
            if (value == Integer.MIN_VALUE) {
                write((byte) '2');
                write((byte) '1');
                write((byte) '4');
                write((byte) '7');
                write((byte) '4');
                write((byte) '8');
                write((byte) '3');
                write((byte) '6');
                write((byte) '4');
                write((byte) '8');
                return;
            }
            value = -value;
        }

        do {
            T[T_pos++] = (byte) ((value % 10) + '0');
            value /= 10;
        } while (value > 0);

        while (T_pos > 0) {
            write(T[--T_pos]);
        }
    }

    public void println(int value) {
        print(value);
        write((byte) '\n');
    }

    private void write(byte b) {
        if (pos == buffer.length) {
            flush();
        }
        buffer[pos++] = b;
    }

    public void flush() {
        if (pos > 0) {
            System.out.write(buffer, 0, pos);
            System.out.flush();
            pos = 0;
        }
    }
}

class WordEntry {
    final char[] word;
    final int len;
    final int hash;
    WordEntry next;

    WordEntry(char[] src, int len, int hash) {
        this.word = new char[len];
        System.arraycopy(src, 0, this.word, 0, len);
        this.len = len;
        this.hash = hash;
        this.next = null;
    }
}

class FastWordSet {
    private static final int INITIAL_CAPACITY = 128;
    private static final float LOAD_FACTOR = 0.75f;

    private WordEntry[] table;
    private int size;
    private int threshold;
    private int mask;

    public FastWordSet() {
        int capacity = INITIAL_CAPACITY;
        table = new WordEntry[capacity];
        size = 0;
        threshold = (int) (capacity * LOAD_FACTOR);
        mask = capacity - 1;
    }

    private int hash(char[] word, int len) {
        int h = 0x811c9dc5;
        for (int i = 0; i < len; i++) {
            h ^= word[i];
            h *= 0x01000193;
        }
        return h;
    }

    public boolean add(char[] word, int len) {
        int h = hash(word, len);
        int i = h & mask;

        for (WordEntry e = table[i]; e != null; e = e.next) {
            if (e.hash == h && e.len == len) {
                boolean same = true;
                for (int j = 0; j < len; j++) {
                    if (word[j] != e.word[j]) {
                        same = false;
                        break;
                    }
                }
                if (same) {
                    return false;
                }
            }
        }

        WordEntry newEntry = new WordEntry(word, len, h);
        newEntry.next = table[i];
        table[i] = newEntry;

        if (++size > threshold) {
            resize();
        }

        return true;
    }

    private void resize() {
        int newCapacity = table.length * 2;
        int newMask = newCapacity - 1;
        WordEntry[] newTable = new WordEntry[newCapacity];

        for (int j = 0; j < table.length; j++) {
            WordEntry e = table[j];
            while (e != null) {
                WordEntry next = e.next;
                int i = e.hash & newMask;
                e.next = newTable[i];
                newTable[i] = e;
                e = next;
            }
        }

        table = newTable;
        mask = newMask;
        threshold = (int) (newCapacity * LOAD_FACTOR);
    }

    public boolean contains(WordEntry entry) {
        int i = entry.hash & mask;

        for (WordEntry e = table[i]; e != null; e = e.next) {
            if (e.hash == entry.hash && e.len == entry.len) {
                boolean same = true;
                for (int j = 0; j < entry.len; j++) {
                    if (entry.word[j] != e.word[j]) {
                        same = false;
                        break;
                    }
                }
                if (same) {
                    return true;
                }
            }
        }
        return false;
    }

    public WordEntry[] getEntries() {
        return table;
    }

    public int getTableSize() {
        return table.length;
    }

    public int size() {
        return size;
    }
}