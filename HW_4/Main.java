class Main {
    static{
        System.setProperty("java.awt.headless", "true");
        System.setProperty("sun.rmi.dgc.server.gcInterval", "Long.MAX_VALUE");
        System.setProperty("sun.rmi.dgc.client.gcInterval", "Long.MAX_VALUE");
        // 禁用 JIT 編譯器優化
        System.setProperty("java.compiler", "NONE");
        // 設置記憶體管理
        System.setProperty("java.lang.Integer.IntegerCache.high", "127");
        // 設置 IO 優化
        System.setProperty("java.nio.channels.spi.SelectorProvider", "sun.nio.ch.PollSelectorProvider");
        // 設置線程優先級
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    }
    public static void main(String[] args) throws Exception {
        // 使用迷你版 IO 讀取文件數量
        FastReader miniReader = new FastReader();
        int docCount = miniReader.nextInt();
        miniReader.skipNewLine();

        // 建立完整 IO 處理器
        FastScanner scanner = new FastScanner(miniReader.getBuffer(), miniReader.getPosition(), miniReader.getBytesRead());
        WordTrie wordTree = new WordTrie();

        // 處理第一個文件的單詞
        while (scanner.hasMoreWordsInLine()) {
            scanner.nextWord();
            if (scanner.wordLength > 0) {
                wordTree.addWord(scanner.wordBuffer, scanner.wordLength);
            }
        }

        // 處理剩餘文件
        for (int docIndex = 1; docIndex < docCount; docIndex++) {
            scanner.skipNewLine();
     
            // 標記當前文件中出現的單詞
            while (scanner.hasMoreWordsInLine()) {
                scanner.nextWord();
                if (scanner.wordLength > 0) {
                    wordTree.markWord(scanner.wordBuffer, scanner.wordLength);
                }
            }
            
            // 刪除未在當前文件出現的單詞路徑
            wordTree.removeUnusedPaths();
        }
        
        // 尋找並輸出共同單詞
        wordTree.findCommonWords(docCount, scanner.output);
        scanner.output.flush();
    }
}

// 迷你高效讀取器 - 專用於讀取文件數量
class FastReader {
    private byte[] buffer = new byte[64];
    private int position;
    private int bytesRead;

    public FastReader() throws Exception {
        bytesRead = System.in.read(buffer);
        position = 0;
    }

    public int nextInt() throws Exception {
        int number = 0;
        byte character;

        while ((character = readByte()) <= ' ')
            ;

        do {
            number = number * 10 + (character - '0');
        } while ((character = readByte()) >= '0' && character <= '9');

        return number;
    }

    public void skipNewLine() throws Exception {
        byte character;
        while ((character = readByte()) <= ' ') {
            if (character == -1)
                break;
        }

        if (character > ' ') {
            position--;
        }
    }

    private byte readByte() throws Exception {
        if (position >= bytesRead) {
            bytesRead = System.in.read(buffer);
            if (bytesRead == -1)
                return -1;
            position = 0;
        }

        return buffer[position++];
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public int getPosition() {
        return position;
    }

    public int getBytesRead() {
        return bytesRead;
    }
}

// 高效文件掃描器
class FastScanner {
    private byte[] buffer = new byte[5000000];
    private int position;
    private int bytesRead;

    char[] wordBuffer = new char[2000];
    int wordLength = 0;
    private boolean lineEnded = false;

    FastOutput output;

    public FastScanner() throws Exception {
        bytesRead = System.in.read(buffer);
        position = 0;
        output = new FastOutput();
    }

    public FastScanner(byte[] miniBuffer, int miniPosition, int miniBytesRead) throws Exception {
        buffer = new byte[5000000];

        if (miniPosition < miniBytesRead) {
            int remainingBytes = miniBytesRead - miniPosition;
            System.arraycopy(miniBuffer, miniPosition, buffer, 0, remainingBytes);
            bytesRead = remainingBytes;
        } else {
            bytesRead = 0;
        }

        if (bytesRead == 0) {
            bytesRead = System.in.read(buffer);
        } else {
            int moreBytes = System.in.read(buffer, bytesRead, buffer.length - bytesRead);
            if (moreBytes > 0) {
                bytesRead += moreBytes;
            }
        }

        position = 0;
        output = new FastOutput();
    }

    public boolean hasMoreWordsInLine() {
        return !lineEnded;
    }

    public void nextWord() throws Exception {
        wordLength = 0;
        byte character;

        character = readByte();
        while (character == ' ') {
            character = readByte();
        }

        if (character == '\n' || character == -1) {
            lineEnded = true;
            return;
        }

        do {
            wordBuffer[wordLength++] = (char) character;
            character = readByte();
        } while (character != ' ' && character != '\n' && character != -1);

        if (character == '\n' || character == -1) {
            lineEnded = true;
        }
    }

    public void skipNewLine() throws Exception {
        byte character = readByte();
        if (character == '\n') {
            lineEnded = false;
        } else {
            position--;
            lineEnded = false;
        }
    }

    private byte readByte() throws Exception {
        if (position >= bytesRead) {
            bytesRead = System.in.read(buffer);
            if (bytesRead == -1)
                return -1;
            position = 0;
        }

        return buffer[position++];
    }
}

// 高效輸出緩衝區
class FastOutput {
    private byte[] buffer = new byte[5000000];
    private int bufferPosition = 0;

    void print(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (bufferPosition == buffer.length)
                flush();
            buffer[bufferPosition++] = (byte) text.charAt(i);
        }
    }

    void printChar(char character) {
        if (bufferPosition == buffer.length)
            flush();
        buffer[bufferPosition++] = (byte) character;
    }

    void printCharArray(char[] array, int length) {
        for (int i = 0; i < length; i++) {
            if (bufferPosition == buffer.length)
                flush();
            buffer[bufferPosition++] = (byte) array[i];
        }
    }

    void flush() {
        System.out.write(buffer, 0, bufferPosition);
        bufferPosition = 0;
    }
}

// 字典樹節點
class TrieNode {
    int frequency;
    TrieNode[] children;

    TrieNode() {
        this.frequency = 0;
        this.children = new TrieNode[60];
    }
}

// 字典樹實現
class WordTrie {
    private TrieNode root;
    private char[] wordBuilder;
    private int wordPosition;
    private boolean[] seen;

    public WordTrie() {
        root = new TrieNode();
        wordBuilder = new char[2000];
        wordPosition = 0;
        seen = new boolean[60];
    }

    // 添加單詞（用於第一個文件）
    public void addWord(char[] word, int length) {
        TrieNode current = root;
        for (int i = 0; i < length; i++) {
            int charIndex = word[i] - 'A';
            if (charIndex < 0 || charIndex >= 60)
                continue;

            if (current.children[charIndex] == null) {
                current.children[charIndex] = new TrieNode();
            }
            current = current.children[charIndex];
        }
        current.frequency++;
    }

    // 標記單詞（用於後續文件）
    public void markWord(char[] word, int length) {
        TrieNode current = root;
        for (int i = 0; i < length; i++) {
            int charIndex = word[i] - 'A';
            if (charIndex < 0 || charIndex >= 60)
                return;

            if (current.children[charIndex] == null)
                return;
            current = current.children[charIndex];

            if (i == 0) {
                seen[charIndex] = true;
            }
        }
        current.frequency++;
    }

    // 刪除未使用的路徑
    public void removeUnusedPaths() {
        for (int i = 0; i < 60; i++) {
            if (seen[i]) {
                seen[i] = false;
            } else {
                root.children[i] = null;
            }
        }
    }
    
    // 尋找共同單詞
    public void findCommonWords(int targetFrequency, FastOutput output) {
        traverseTrie(root, targetFrequency, output);
    }
    
    // 遞迴遍歷樹
    private void traverseTrie(TrieNode node, int targetFrequency, FastOutput output) {
        if (node == null)
            return;

        if (node.frequency == targetFrequency) {
            output.printCharArray(wordBuilder, wordPosition);
            output.printChar(' ');
        }

        for (int i = 0; i < 60; i++) {
            if (node.children[i] != null) {
                wordBuilder[wordPosition++] = (char) ('A' + i);
                traverseTrie(node.children[i], targetFrequency, output);
                wordPosition--;
            }
        }
    }
}