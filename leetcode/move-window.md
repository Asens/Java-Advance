# 最长子串-滑动窗口

> [原题链接]( https://leetcode-cn.com/problems/longest-substring-without-repeating-characters/ )

给定一个字符串，请你找出其中不含有重复字符的 **最长子串** 的长度。 

> 示例 :
>
> 输入: "abcabcbb"
> 输出: 3 
> 解释: 因为无重复字符的最长子串是 "abc"，所以其长度为 3。

### **暴力**

这是一个比较经典的问题，查找无重复最长子串。

先看我最开始给出的解法

思路比较简单，借助Set，我可以很快的判断一个字符是否在一个集合里

从第一个字符开始开始遍历每个字符，然后从这个字符开始到最后一个字符，依次加入Set

如果Set没有就加入，一旦出现重复判断之前的最长长度和当前的最长长度，取较大值，Set清空

```java
class Solution {
    public int lengthOfLongestSubstring(String s) {
        Set<Integer> t = new HashSet<>();
        int max = 0;
        for (int i = 0; i < s.length(); i++) {
            for (int j = i; j < s.length(); j++) {
                int a = s.charAt(j);
                if (!t.contains(a)) {
                    t.add(a);
                } else {
                    max = t.size() > max ? t.size() : max;
                    t.clear();
                    break;
                }
            }
        }
        if(t.size() > max){
            max = t.size();
        }
        return max;
    }
}
```

### 滑动窗口

先看解法

```java
class Solution {
    public int lengthOfLongestSubstring(String s) {
        int max = 0;
        Set<Character> t = new HashSet<>();
        for (int i = 0, j = 0; i < s.length(); ) {
            if (j >= s.length()) {
                break;
            }
            if (!t.contains(s.charAt(j))) {
                t.add(s.charAt(j));
                max = Math.max(max, j - i +1);
                j++;
            }else{
                t.remove(s.charAt(i++));
            }
        }
        return max;
    }
}
```

暴力法有个明显的问题，比如我判断 abcabcbb

在第一字符时判断

```
a
ab
abc
abca
```

在第二个字符时判断

```
b
bc
bca
```

会有大量重复且不必要的判断

那么怎么避免呢

当我判断第一个字符到 `abca` 时，不在完全退回第二个字符重新判断，而是保留右侧的配置，左侧右移一位，那么现在就成了不重复时右侧游标右移，存在重复字符时，左侧游标右移，同时记录中间的最长长度

左侧游标和右侧游标依次滑动右移，就仿佛是一个会移动的窗口，中间的某个最大长度即为不含有重复字符的最长子串

### 滑动窗口优化

当我们遇到一个字符串`pasdsad`，使用上述算法

```
p
pa
pas
pasd
asd
sd
d
ds
```

因为下一个字符是`s`，所以左侧游标移动了3次才移除了当前窗口内`s`

但是如果直接能跳到`s`位置呢

那么又会减少很多次操作

```
class Solution {
    public int lengthOfLongestSubstring(String s) {
        int n = s.length(), ans = 0;
        Map<Character, Integer> map = new HashMap<>();
        for (int j = 0, i = 0; j < n; j++) {
            if (map.containsKey(s.charAt(j))) {
                i = Math.max(map.get(s.charAt(j)), i);
            }
            ans = Math.max(ans, j - i + 1);
            map.put(s.charAt(j), j + 1);
        }
        return ans;
    }
}
```

通过Map映射记录上一个重复字符的位置，发生重复是可使左侧游标直接跳到之前的重复位置。