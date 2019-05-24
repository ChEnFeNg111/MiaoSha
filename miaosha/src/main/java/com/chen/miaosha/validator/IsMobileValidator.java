package com.chen.miaosha.validator;

import com.chen.miaosha.util.ValidatorUtil;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 *  JSR303 参数校验自定义注解 @IsMobile 的实现类，完成验证功能
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile,String> {

    private  boolean required = false;
    @Override
    public void initialize(IsMobile constraintAnnotation) {
        required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 验证手机格式
        if(required){
            return ValidatorUtil.isMobile(value);
        }else if(StringUtils.isEmpty(value)){
            return true;
        }else {
            return ValidatorUtil.isMobile(value);
        }
    }
}
