# Special Union
## Description

In this task, you are given a set of `D` documents. Each document contains a sequence of words (already preprocessed: cleaned, space-separated, and without punctuation).

You are also given `Q` queries. Each query specifies a subset of documents, and your goal is to count how many distinct words appear in all documents of that query.

Your task is to compute and output the intersection size for each query — i.e., the number of words that are shared across all documents listed in that query.

## Input
The first line contains two integers:

`Q` and `D` — the number of queries and the number of documents 

The next `Q` lines each contain a list of integers (1-indexed), representing the document indices involved in that query

The next `D` lines each contain a single cleaned document string — consisting of words separated by single spaces, with no punctuation, and containing only uppercase and lowercase English letters.

All word comparisons are case-sensitive, and there may be duplicate words within or a cross documents.

## Output
Output `Q` lines.

Each line should contain a single integer — the number of distinct words that appear in all the documents of the corresponding query.

The output must match exactly, with no trailing newline on the last line.

## Example
### Input 1
```
2 3
1 2
2 3
apple banana
cat dog
dog giraffe
```
### Output 1
```
0
1
```

### Input 2
```
2 3
1 2
1 2 3
apple banana
banana orange apple 
orange banana 
```

### Output 2
```
2
1
```

### Input 3
```
2 3
1 2 3
1 3
apple banana
banana cherry
orange apple
```

### Output 3
```
0
1
```