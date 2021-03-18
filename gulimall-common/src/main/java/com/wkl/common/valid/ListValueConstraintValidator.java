package com.wkl.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {

    //取出允许的值，然后和提交的值进行判断
    private Set<Integer> set = new HashSet<>();
    /**
     * 初始化方法，获取到注解标注注解的详细信息，指定通过校验的值就在这里面，可以和下面的方法配合，取出提交的值
     * @param constraintAnnotation
     */
    @Override
    public void initialize(ListValue constraintAnnotation) {
        //取出指定允许通过的值
        int[] vals = constraintAnnotation.vals();
        for (int val : vals) {
            set.add(val);
        }
    }

    /**
     * 是否校验成功
     * @param integer 这个integer就是注解发过来需要校验的值，和上个方法配合可盘判断
     * @param constraintValidatorContext
     * @return
     */
    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        //判断提交的值是否在允许列表中
        return set.contains(integer);
    }
}
