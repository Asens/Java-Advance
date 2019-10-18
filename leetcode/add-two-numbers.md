# 两数相加





解法

```
public class ListNode {
        int val;
        ListNode next;
        ListNode(int x) {
            val = x;
        }
    }

    @Test
    public void main(){
        ListNode l1 = new ListNode(2);
        l1.next = new ListNode(4);
        l1.next.next = new ListNode(3);

        ListNode l2 = new ListNode(5);
        l2.next = new ListNode(6);
        l2.next.next = new ListNode(4);
        ListNode r = addTwoNumbers(l1,l2);
        System.out.println();
    }

    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode r = null, tail=null;
        boolean overTen = false;
        do{
            int s1 = l1==null?0:l1.val;
            int s2 = l2==null?0:l2.val;
            int i = s1 + s2;
            if(overTen){
                i++;
            }
            if(i>9){
                overTen = true;
                i = i-10;
            }else{
                overTen = false;
            }

            if(r==null){
                r = new ListNode(i);
                tail = r;
            }else{
                tail.next = new ListNode(i);
                tail=tail.next;
            }

            if(l1!=null){
                l1 = l1.next;
            }

            if(l2!=null){
                l2 = l2.next;
            }
        } while (l1!=null || l2!=null);

        if(overTen){
            tail.next = new ListNode(1);
        }
        return r;
    }
```