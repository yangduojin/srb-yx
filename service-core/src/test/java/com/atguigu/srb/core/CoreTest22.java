package com.atguigu.srb.core;


import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.entity.vo.UserInfoVO;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MyApplication.class)
public class CoreTest22 {


    @Test
    public void test(){
        int[] nums1 = { 0, 0, 1, 1, 1, 2, 0, 2, 3, 4 };
        // int[] nums2 = { 9, 8, 9 };
        char[][] num = {
                { '5', '3', '.', '.', '7', '.', '.', '.', '.' },
                { '6', '.', '.', '1', '9', '5', '.', '.', '.' },
                { '.', '9', '8', '.', '.', '.', '.', '6', '.' },
                { '8', '.', '.', '.', '6', '.', '.', '.', '3' },
                { '4', '.', '.', '8', '.', '3', '.', '.', '1' },
                { '7', '.', '.', '.', '2', '.', '.', '.', '6' },
                { '.', '6', '.', '.', '.', '.', '2', '8', '.' },
                { '.', '.', '.', '4', '1', '9', '.', '.', '5' },
                { '.', '.', '.', '.', '8', '.', '.', '7', '9' } };
        boolean result = isValidSudoku(num);
        System.out.println(result);
    }
    

    public static boolean isValidSudoku(char board[][]) {
        int length = board.length;
        // 二维数组line表示的是对应的行中是否有对应的数字，比如line[0][3]
        // 表示的是第0行（实际上是第1行，因为数组的下标是从0开始的）是否有数字3
        int line[][] = new int[length][length];
        int column[][] = new int[length][length];
        int cell[][] = new int[length][length];
        for (int i = 0; i < length; ++i)
            for (int j = 0; j < length; ++j) {
                // 如果还没有填数字，直接跳过
                if (board[i][j] == '.')
                    continue;
                // num是当前格子的数字
                int num = board[i][j] - '0' - 1;
                // k是第几个单元格，9宫格数独横着和竖着都是3个单元格
                int k = i / 3 * 3 + j / 3;
                // 如果当前数字对应的行和列以及单元格，只要一个由数字，说明冲突了，直接返回false。
                // 举个例子，如果line[i][num]不等于0，说明第i（i从0开始）行有num这个数字。
                if (line[i][num] != 0 || column[j][num] != 0 || cell[k][num] != 0)
                    return false;
                // 表示第i行有num这个数字，第j列有num这个数字，对应的单元格内也有num这个数字
                line[i][num] = column[j][num] = cell[k][num] = 1;
            }
        return true;
    }

    @Test
    public void testCopy(){
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserType(1);
        userInfoVO.setNickName("yang");
        userInfoVO.setName("xin");
        userInfoVO.setMobile("110");
        userInfoVO.setHeadImg(null);


        UserInfo userInfo = null;

        BeanUtils.copyProperties(userInfoVO,userInfo);

        System.out.println(userInfo);
    }

}
