package com.yupi.yupicturebackend.utils;

import java.awt.Color;
import java.util.regex.Pattern;

/**
 * packageName: com.yupi.yupicturebackend.utils
 *
 * @author: idpeng
 * @version: 2.0
 * @className: ColorTransformUtils
 * @date: 2025/8/24 13:58
 * @description: 颜色转换工具类，专门处理腾讯云COS数据万象返回的RGB颜色格式转换
 */
public class ColorTransformUtils {
    
    // 十六进制颜色格式验证正则表达式
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^0x[0-9A-Fa-f]{1,6}$");
    
    private ColorTransformUtils() {
        // 工具类不需要实例化
    }

    /**
     * 获取标准颜色（将腾讯云COS数据万象返回的RGB色值转为标准6位十六进制格式）
     * 腾讯云返回格式：0xRRGGBB，但可能缺少前导零，需要补全为完整的6位RGB格式
     *
     * @param color 腾讯云返回的十六进制颜色代码（如：0x736246、0x80e0等）
     * @return 标准的六位十六进制颜色代码（如：#736246、#0800e0等）
     * @throws IllegalArgumentException 当输入格式无效时抛出异常
     */
    public static String getStandardColor(String color) {
        if (color == null || color.trim().isEmpty()) {
            throw new IllegalArgumentException("颜色值不能为空");
        }
        
        String trimmedColor = color.trim();
        
        // 验证输入格式
        if (!HEX_COLOR_PATTERN.matcher(trimmedColor).matches()) {
            throw new IllegalArgumentException("无效的颜色格式，期望格式：0xRRGGBB，实际输入：" + color);
        }
        
        // 移除0x前缀
        String hexValue = trimmedColor.substring(2);
        
        // 补全为6位十六进制（左侧补零）
        while (hexValue.length() < 6) {
            hexValue = "0" + hexValue;
        }
        
        // 返回标准格式（带#前缀）
        return "#" + hexValue.toUpperCase();
    }
    
    /**
     * 将腾讯云返回的RGB色值转换为Color对象
     *
     * @param color 腾讯云返回的十六进制颜色代码（如：0x736246）
     * @return Color对象
     * @throws IllegalArgumentException 当输入格式无效时抛出异常
     */
    public static Color parseToColor(String color) {
        String standardColor = getStandardColor(color);
        // 移除#前缀并解析为整数
        int rgb = Integer.parseInt(standardColor.substring(1), 16);
        return new Color(rgb);
    }
    
    /**
     * 将腾讯云返回的RGB色值转换为RGB数组
     *
     * @param color 腾讯云返回的十六进制颜色代码（如：0x736246）
     * @return RGB数组，格式为[R, G, B]，每个值范围0-255
     * @throws IllegalArgumentException 当输入格式无效时抛出异常
     */
    public static int[] parseToRgbArray(String color) {
        Color colorObj = parseToColor(color);
        return new int[]{colorObj.getRed(), colorObj.getGreen(), colorObj.getBlue()};
    }
    
    /**
     * 将腾讯云返回的RGB色值转换为RGB字符串表示
     *
     * @param color 腾讯云返回的十六进制颜色代码（如：0x736246）
     * @return RGB字符串，格式为"rgb(r, g, b)"
     * @throws IllegalArgumentException 当输入格式无效时抛出异常
     */
    public static String parseToRgbString(String color) {
        int[] rgb = parseToRgbArray(color);
        return String.format("rgb(%d, %d, %d)", rgb[0], rgb[1], rgb[2]);
    }
    
    /**
     * 验证腾讯云返回的颜色格式是否有效
     *
     * @param color 待验证的颜色字符串
     * @return 如果格式有效返回true，否则返回false
     */
    public static boolean isValidColor(String color) {
        if (color == null || color.trim().isEmpty()) {
            return false;
        }
        return HEX_COLOR_PATTERN.matcher(color.trim()).matches();
    }
    
    /**
     * 将标准十六进制颜色代码转换为腾讯云格式
     *
     * @param standardColor 标准十六进制颜色代码（如：#736246、736246）
     * @return 腾讯云格式的颜色代码（如：0x736246）
     * @throws IllegalArgumentException 当输入格式无效时抛出异常
     */
    public static String toTencentFormat(String standardColor) {
        if (standardColor == null || standardColor.trim().isEmpty()) {
            throw new IllegalArgumentException("颜色值不能为空");
        }
        
        String trimmedColor = standardColor.trim();
        
        // 移除#前缀（如果存在）
        if (trimmedColor.startsWith("#")) {
            trimmedColor = trimmedColor.substring(1);
        }
        
        // 验证是否为有效的6位十六进制
        if (!trimmedColor.matches("^[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("无效的十六进制颜色格式，期望6位十六进制，实际输入：" + standardColor);
        }
        
        return "0x" + trimmedColor.toUpperCase();
    }
    
    // ==================== 测试和示例代码 ====================
    
    /**
     * 主方法：用于测试颜色转换功能
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        System.out.println("=== 腾讯云COS数据万象颜色转换工具测试 ===");
        
        // 测试用例
        String[] testColors = {
            "0x736246",  // 标准6位
            "0x80e0",    // 4位，需要补零
            "0xff0000",  // 红色
            "0x0",       // 最短格式
            "0x123abc"   // 混合大小写
        };
        
        for (String testColor : testColors) {
            try {
                System.out.println("\n原始颜色: " + testColor);
                
                // 转换为标准格式
                String standardColor = getStandardColor(testColor);
                System.out.println("标准格式: " + standardColor);
                
                // 转换为RGB数组
                int[] rgb = parseToRgbArray(testColor);
                System.out.printf("RGB数组: [%d, %d, %d]\n", rgb[0], rgb[1], rgb[2]);
                
                // 转换为RGB字符串
                String rgbString = parseToRgbString(testColor);
                System.out.println("RGB字符串: " + rgbString);
                
                // 转换回腾讯云格式
                String tencentFormat = toTencentFormat(standardColor);
                System.out.println("腾讯云格式: " + tencentFormat);
                
            } catch (Exception e) {
                System.out.println("转换失败: " + e.getMessage());
            }
        }
        
        // 测试格式验证
        System.out.println("\n=== 格式验证测试 ===");
        String[] validationTests = {"0x736246", "0x80e0", "invalid", null, "", "#736246"};
        for (String test : validationTests) {
            boolean isValid = isValidColor(test);
            System.out.printf("颜色 '%s' 是否有效: %s\n", test, isValid ? "是" : "否");
        }
        
        System.out.println("\n=== 测试完成 ===");
    }
}
