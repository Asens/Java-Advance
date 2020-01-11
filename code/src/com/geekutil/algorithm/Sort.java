public class Sort {

    @Test
    public void pwd() throws InterruptedException {
        int[] arr = {5,1,234,24,8,543,453,45,7,234,23,42,34,234,23,544,53,2,4,3,2,32,4,234,24,234,3,54,2};
        quickSort(arr);
        display(arr);
    }

    private void quickSort(int[] arr){
        quickMiddle(0,arr.length-1,arr);
    }

    private void quickMiddle(int start,int end,int[] arr){
        if(start>end){
            return;
        }
        int i=start,j=end;
        int first = arr[start];
        while (i<j){
            while (j>i && arr[j]>=first){
                j--;
            }

            while (i<j && arr[i]<=first){
                i++;
            }

            if(i<j) {
                int t = arr[i];
                arr[i] = arr[j];
                arr[j] = t;
            }
        }

        arr[start] = arr[i];
        arr[i] = first;

        quickMiddle(start,j-1,arr);
        quickMiddle(j+1,end,arr);
    }

    private void bubbleSort(int[] arr){
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length-i-1; j++) {
                if(arr[j]>arr[j+1]){
                    int t = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = t;
                }
            }

        }
    }

    private void selectionSort(int[] arr){
        for (int i = 0; i < arr.length; i++) {
            int min = Integer.MAX_VALUE;
            int t = i;
            for (int j = i; j < arr.length; j++) {
                if(arr[j]<min){
                    min = arr[j];
                    t = j;
                }
            }
            arr[t] = arr[i];
            arr[i] = min;
        }
    }

    private void display(int[] arr) {
        for (int value : arr) {
            System.out.print(value + ",");
        }
    }
}