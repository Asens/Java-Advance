# 最长回文子串-动态规划

> [原题链接](  https://leetcode-cn.com/problems/longest-palindromic-substring/  )

 给定一个字符串 `s`，找到 `s` 中最长的回文子串。你可以假设 `s` 的最大长度为 1000。 

> 输入: "babad"
> 输出: "bab"
>
> 输入: "cbbd"
> 输出: "bb"

**其中回文就是从做到右和从右到左读完全一致**

## 暴力法

先看我最开始给出的解法

从第一个字符开始开始遍历每个字符，然后从这个字符开始到最后一个字符，依次判断是否是回文

判断回文的方法就是依次判断第1个和最后1个字符，第2个和倒数第2个...有不同的则不是，直到中间

最后就能找到最长的回文子串了

```
class Solution {
    public String longestPalindrome(String s) {
       if(s.length()==1){
            return s;
        }
        if(isPalindrome(s)){
            return s;
        }
        String max = "";
        for (int i = 0; i < s.length()-1; i++) {
            for (int j = s.length(); j >= i+1 ; j--) {
                if(j-i>max.length() && isPalindrome(s.substring(i,j))){
                    max = s.substring(i,j);
                }
            }
        }
        return max;
    }
    
    private boolean isPalindrome(String s) {
        if(s.length()==1){
            return true;
        }

        for (int i = 0; i < s.length()-1; i++) {
            if(s.charAt(i)!=s.charAt(s.length()-i-1)){
                return false;
            }
            if(i>(s.length()+1)/2){
                return true;
            }
        }
        return true;
    }
}
```

暴力法简单易行，但是效率欠佳，下面介绍动态规划的解法

## 动态规划

在动态规划中，我们记录每一步的结果，为下一步提供指导，首先我们要找到各个步骤之前的关系。

在本题中当中当字符串的中的一个子串是回文，如果这个子串的前后字符相同那么之前的子串加上前后字符同样是回文。

判断每个长度，然后从字符串的开始位置依次判断

当长度大于2时，只需要判断前后的字符以及去除前后字符的之前的位置是否是回文即可。

```
public String longestPalindrome(String s) {
        String max = "";
        int n = s.length();
        boolean[][] temp = new boolean[n][n];
        for (int i = 1; i <= n; i++) {
            for (int j = 0; j < n; j++) {
                int end = j+i-1;
                if (end >= n){
                    break;
                }
                temp[j][end] = (i==1 || i==2 || temp[j+1][end-1])  && 
                			s.charAt(j)==s.charAt(end);
                if(temp[j][end] && i>max.length()){
                    max = s.substring(j,j+i);
                }
            }
        }
        return max;
    }
```



















