package com.px.picturebackend.utils;

import java.awt.*;
import java.util.regex.Pattern;

/**
 * packageName: com.picturebackend.utils
 *
 * @author: idpeng
 * @version: 1.0
 * @className: ColorSimilarUtils
 * @date: 2025/8/24 10:58
 * @description: 工具类：计算颜色相似度，支持多种算法和颜色格�?
 */
public class ColorSimilarUtils {
    
    // 十六进制颜色格式验证正则表达式
    private static final Pattern HEX_PATTERN = Pattern.compile("^#?([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
    
    // 最大RGB距离（用于归一化）
    private static final double MAX_RGB_DISTANCE = Math.sqrt(3 * 255 * 255);
    
    private ColorSimilarUtils() {
        // 工具类不需要实例化
    }

    /**
     * 使用欧氏距离算法计算两个颜色的相似度
     * 这是最基础的颜色相似度计算方法
     *
     * @param color1 第一个颜色对象
     * @param color2 第二个颜色对象
     * @return 相似度评分（0到1之间，1为完全相同，0为完全不同）
     */
    public static double calculateSimilarity(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            throw new IllegalArgumentException("颜色对象不能为null");
        }
        
        return calculateEuclideanSimilarity(color1, color2);
    }

    /**
     * 根据十六进制颜色代码计算相似度
     * 支持多种十六进制格式：#FFFFFF、FFFFFF、#FFF、FFF
     *
     * @param hexColor1 第一个颜色的十六进制代码（如 "#FF0000" 或 "FF0000"）
     * @param hexColor2 第二个颜色的十六进制代码（如 "#FE0101" 或 "FE0101"）
     * @return 相似度评分（0到1之间，1为完全相同，0为完全不同）
     */
    public static double calculateSimilarity(String hexColor1, String hexColor2) {
        if (hexColor1 == null || hexColor2 == null) {
            throw new IllegalArgumentException("十六进制颜色代码不能为null");
        }
        
        Color color1 = parseHexColor(hexColor1);
        Color color2 = parseHexColor(hexColor2);
        return calculateSimilarity(color1, color2);
    }
    
    /**
     * 根据RGB值计算颜色相似度
     *
     * @param r1 第一个颜色的红色分量（0-255）
     * @param g1 第一个颜色的绿色分量（0-255）
     * @param b1 第一个颜色的蓝色分量（0-255）
     * @param r2 第二个颜色的红色分量（0-255）
     * @param g2 第二个颜色的绿色分量（0-255）
     * @param b2 第二个颜色的蓝色分量（0-255）
     * @return 相似度评分（0到1之间，1为完全相同，0为完全不同）
     */
    public static double calculateSimilarity(int r1, int g1, int b1, int r2, int g2, int b2) {
        validateRgbValue(r1, "r1");
        validateRgbValue(g1, "g1");
        validateRgbValue(b1, "b1");
        validateRgbValue(r2, "r2");
        validateRgbValue(g2, "g2");
        validateRgbValue(b2, "b2");
        
        Color color1 = new Color(r1, g1, b1);
        Color color2 = new Color(r2, g2, b2);
        return calculateSimilarity(color1, color2);
    }

    /**
     * 使用加权欧氏距离算法计算颜色相似度
     * 考虑人眼对不同颜色分量的敏感度差异
     *
     * @param color1 第一个颜色对象
     * @param color2 第二个颜色对象
     * @return 相似度评分（0到1之间，1为完全相同，0为完全不同）
     */
    public static double calculateWeightedSimilarity(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            throw new IllegalArgumentException("颜色对象不能为null");
        }
        
        int r1 = color1.getRed();
        int g1 = color1.getGreen();
        int b1 = color1.getBlue();
        
        int r2 = color2.getRed();
        int g2 = color2.getGreen();
        int b2 = color2.getBlue();
        
        // 人眼对绿色最敏感，红色次之，蓝色最不敏感
        double rWeight = 0.3;
        double gWeight = 0.59;
        double bWeight = 0.11;
        
        // 计算加权欧氏距离
        double weightedDistance = Math.sqrt(
            rWeight * Math.pow(r1 - r2, 2) +
            gWeight * Math.pow(g1 - g2, 2) +
            bWeight * Math.pow(b1 - b2, 2)
        );
        
        // 计算最大可能的加权距离用于归一化
        double maxWeightedDistance = Math.sqrt(
            rWeight * 255 * 255 +
            gWeight * 255 * 255 +
            bWeight * 255 * 255
        );
        
        return 1.0 - (weightedDistance / maxWeightedDistance);
    }
    
    /**
     * 使用Delta E CIE76算法计算颜色相似度
     * 这是更符合人眼感知的颜色差异计算方法
     *
     * @param color1 第一个颜色对象
     * @param color2 第二个颜色对象
     * @return 相似度评分（0到1之间，1为完全相同，0为完全不同）
     */
    public static double calculateDeltaESimilarity(Color color1, Color color2) {
        if (color1 == null || color2 == null) {
            throw new IllegalArgumentException("颜色对象不能为null");
        }
        
        // 将RGB转换为LAB色彩空间
        double[] lab1 = rgbToLab(color1.getRed(), color1.getGreen(), color1.getBlue());
        double[] lab2 = rgbToLab(color2.getRed(), color2.getGreen(), color2.getBlue());
        
        // 计算Delta E
        double deltaL = lab1[0] - lab2[0];
        double deltaA = lab1[1] - lab2[1];
        double deltaB = lab1[2] - lab2[2];
        
        double deltaE = Math.sqrt(deltaL * deltaL + deltaA * deltaA + deltaB * deltaB);
        
        // Delta E的最大值约为100，将其转换为0-1的相似度
        return Math.max(0, 1.0 - (deltaE / 100.0));
    }
    
    /**
     * 判断两个颜色是否相似（基于阈值）
     *
     * @param color1 第一个颜色对象
     * @param color2 第二个颜色对象
     * @param threshold 相似度阈值（0到1之间，默认建议0.8）
     * @return 如果相似度大于等于阈值则返回true，否则返回false
     */
    public static boolean isSimilar(Color color1, Color color2, double threshold) {
        if (threshold < 0 || threshold > 1) {
            throw new IllegalArgumentException("阈值必须在0到1之间");
        }
        return calculateSimilarity(color1, color2) >= threshold;
    }
    
    /**
     * 使用默认阈值0.8判断两个颜色是否相似
     *
     * @param color1 第一个颜色对象
     * @param color2 第二个颜色对象
     * @return 如果相似度大于等于0.8则返回true，否则返回false
     */
    public static boolean isSimilar(Color color1, Color color2) {
        return isSimilar(color1, color2, 0.8);
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 使用欧氏距离计算颜色相似度的核心实现
     *
     * @param color1 第一个颜色对象
     * @param color2 第二个颜色对象
     * @return 相似度评分（0到1之间，1为完全相同，0为完全不同）
     */
    private static double calculateEuclideanSimilarity(Color color1, Color color2) {
        int r1 = color1.getRed();
        int g1 = color1.getGreen();
        int b1 = color1.getBlue();
        
        int r2 = color2.getRed();
        int g2 = color2.getGreen();
        int b2 = color2.getBlue();
        
        // 计算欧氏距离
        double distance = Math.sqrt(
            Math.pow(r1 - r2, 2) + 
            Math.pow(g1 - g2, 2) + 
            Math.pow(b1 - b2, 2)
        );
        
        // 归一化到0-1范围
        return 1.0 - (distance / MAX_RGB_DISTANCE);
    }
    
    /**
     * 解析十六进制颜色字符串为Color对象
     *
     * @param hexColor 十六进制颜色字符串（如#FF0000或FF0000）
     * @return Color对象
     */
    private static Color parseHexColor(String hexColor) {
        String cleanHex = hexColor.trim();
        
        // 验证格式
        if (!HEX_PATTERN.matcher(cleanHex).matches()) {
            throw new IllegalArgumentException("无效的十六进制颜色格式: " + hexColor);
        }
        
        // 移除#�?
        if (cleanHex.startsWith("#")) {
            cleanHex = cleanHex.substring(1);
        }
        
        // 处理3位十六进制格式（如FFF -> FFFFFF）
        if (cleanHex.length() == 3) {
            cleanHex = String.valueOf(cleanHex.charAt(0)) + cleanHex.charAt(0) +
                      cleanHex.charAt(1) + cleanHex.charAt(1) +
                      cleanHex.charAt(2) + cleanHex.charAt(2);
        }
        
        try {
            int rgb = Integer.parseInt(cleanHex, 16);
            return new Color(rgb);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无法解析十六进制颜色: " + hexColor, e);
        }
    }
    
    /**
     * 验证RGB值是否在有效范围（0到255）
     *
     * @param value RGB分量值
     * @param paramName 参数名称（用于错误信息）
     */
    private static void validateRgbValue(int value, String paramName) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException(paramName + "必须在0到255之间，当前值为 " + value);
        }
    }
    
    /**
     * 将RGB颜色转换为LAB色彩空间
     *
     * @param r 红色分量值（0到255）
     * @param g 绿色分量值（0到255）
     * @param b 蓝色分量值（0到255）
     * @return LAB色彩空间的数组[L, A, B]
     */
    private static double[] rgbToLab(int r, int g, int b) {
        // 首先转换为XYZ色彩空间
        double[] xyz = rgbToXyz(r, g, b);
        
        // 然后转换为LAB色彩空间
        return xyzToLab(xyz[0], xyz[1], xyz[2]);
    }
    
    /**
     * 将RGB转换为XYZ色彩空间
     *
     * @param r 红色分量值（0到255）
     * @param g 绿色分量值（0到255）
     * @param b 蓝色分量值（0到255）
     * @return XYZ色彩空间的数组[X, Y, Z]
     */
    private static double[] rgbToXyz(int r, int g, int b) {
        // 归一化RGB值到0-1
        double rNorm = r / 255.0;
        double gNorm = g / 255.0;
        double bNorm = b / 255.0;
        
        // 应用gamma校正
        rNorm = (rNorm > 0.04045) ? Math.pow((rNorm + 0.055) / 1.055, 2.4) : rNorm / 12.92;
        gNorm = (gNorm > 0.04045) ? Math.pow((gNorm + 0.055) / 1.055, 2.4) : gNorm / 12.92;
        bNorm = (bNorm > 0.04045) ? Math.pow((bNorm + 0.055) / 1.055, 2.4) : bNorm / 12.92;
        
        // 转换为XYZ（使用sRGB色彩空间的转换矩阵）
        double x = rNorm * 0.4124564 + gNorm * 0.3575761 + bNorm * 0.1804375;
        double y = rNorm * 0.2126729 + gNorm * 0.7151522 + bNorm * 0.0721750;
        double z = rNorm * 0.0193339 + gNorm * 0.1191920 + bNorm * 0.9503041;
        
        return new double[]{x * 100, y * 100, z * 100};
    }
    
    /**
     * 将XYZ转换为LAB色彩空间
     *
     * @param x X分量
     * @param y Y分量
     * @param z Z分量
     * @return LAB色彩空间的数组[L, A, B]
     */
    private static double[] xyzToLab(double x, double y, double z) {
        // D65标准光源的白色点
        double xn = 95.047;
        double yn = 100.000;
        double zn = 108.883;
        
        double fx = labF(x / xn);
        double fy = labF(y / yn);
        double fz = labF(z / zn);
        
        double l = 116 * fy - 16;
        double a = 500 * (fx - fy);
        double b = 200 * (fy - fz);
        
        return new double[]{l, a, b};
    }
    
    /**
     * LAB转换中使用的辅助函数
     *
     * @param t 输入值
     * @return 转换后的输出值
     */
    private static double labF(double t) {
        return (t > 0.008856) ? Math.pow(t, 1.0 / 3.0) : (7.787 * t + 16.0 / 116.0);
    }
    
    // ==================== 测试和示例代码 ====================
    
    /**
     * 主方法：用于测试各种颜色相似度计算算法
     *
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        System.out.println("=== 颜色相似度计算工具测试 ===");
        
        // 测试颜色
        Color red1 = new Color(255, 0, 0);     // 纯红色
        Color red2 = new Color(254, 1, 1);     // 接近红色
        Color blue = new Color(0, 0, 255);     // 纯蓝色
        
        System.out.println("\n1. 基础欧氏距离算法测试:");
        double similarity1 = calculateSimilarity(red1, red2);
        double similarity2 = calculateSimilarity(red1, blue);
        System.out.printf("红色(255,0,0) vs 红色(254,1,1): %.4f\n", similarity1);
        System.out.printf("红色(255,0,0) vs 蓝色(0,0,255): %.4f\n", similarity2);
        
        System.out.println("\n2. 加权欧氏距离算法测试:");
        double weightedSim1 = calculateWeightedSimilarity(red1, red2);
        double weightedSim2 = calculateWeightedSimilarity(red1, blue);
        System.out.printf("红色(255,0,0) vs 红色(254,1,1): %.4f\n", weightedSim1);
        System.out.printf("红色(255,0,0) vs 蓝色(0,0,255): %.4f\n", weightedSim2);
        
        System.out.println("\n3. Delta E算法测试:");
        double deltaESim1 = calculateDeltaESimilarity(red1, red2);
        double deltaESim2 = calculateDeltaESimilarity(red1, blue);
        System.out.printf("红色(255,0,0) vs 红色(254,1,1): %.4f\n", deltaESim1);
        System.out.printf("红色(255,0,0) vs 蓝色(0,0,255): %.4f\n", deltaESim2);
        
        System.out.println("\n4. 十六进制颜色测试:");
        double hexSimilarity1 = calculateSimilarity("#FF0000", "#FE0101");
        double hexSimilarity2 = calculateSimilarity("FF0000", "0000FF");
        double hexSimilarity3 = calculateSimilarity("#F00", "#FFF");
        System.out.printf("#FF0000 vs #FE0101: %.4f\n", hexSimilarity1);
        System.out.printf("FF0000 vs 0000FF: %.4f\n", hexSimilarity2);
        System.out.printf("#F00 vs #FFF: %.4f\n", hexSimilarity3);
        
        System.out.println("\n5. RGB值直接计算测试:");
        double rgbSimilarity = calculateSimilarity(255, 0, 0, 254, 1, 1);
        System.out.printf("RGB(255,0,0) vs RGB(254,1,1): %.4f\n", rgbSimilarity);
        
        System.out.println("\n6. 相似性判断测试:");
        boolean similar1 = isSimilar(red1, red2, 0.9);
        boolean similar2 = isSimilar(red1, blue, 0.9);
        System.out.printf("红色vs接近红色 (阈值0.9): %s\n", similar1 ? "相似" : "不相似");
        System.out.printf("红色vs蓝色 (阈值0.9): %s\n", similar2 ? "相似" : "不相似");
        
        System.out.println("\n=== 测试完成 ===");
    }
}
