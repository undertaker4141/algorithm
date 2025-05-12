## Problem: Co-occurrence Pair

You are given a list of word pairs, where each pair represents a co-occurrence between two words.

Your task is to process multiple queries. For each query, you are given a word and an integer K.

You need to find the top K words that most frequently co-occur with the query word in the given list of pairs.

If two or more words have the same co-occurrence count, sort them in ascending alphabetical order.

### Input

- The first line contains two integers N and M:  
  - N (1 ≤ N ≤ 10⁵): the number of word pairs.  
  - M (1 ≤ M ≤ 1000): the number of queries.
- The next N lines each contain two strings word1 and word2:  
  Each line indicates that word1 and word2 co-occur.  
  All words are lowercase English strings with only alphabetic characters.
- The next M lines each contain a query consisting of a string query_word and an integer K:
  - query_word is the word to search for.
  - K (1 ≤ K ≤ 100): the number of top co-occurring words to return.

### Output

- Output M lines.
- For each query, print the top K words that co-occur with the query, based on frequency.
- Words should be ordered by:
  1. Higher co-occurrence frequency (descending),
  2. Alphabetical order if frequencies are equal.
- Output words in a single line, separated by a single space.

---

### Sample Input 1
```
6 1
wolf deer
bear wolf
tiger mouse
fox mouse
bear fox
rat deer
wolf 1
```

### Sample Output 1
```
bear
```

### Sample Input 2
```
8 2
cat fox
fox cat
rat lion
rat dog
lion cat
lion cat
fox deer
lion cat
cat 2
dog 1
```

### Sample Output 2
```
lion fox
rat
```