package com.picturebackend.api.imagesearch;

import com.picturebackend.api.imagesearch.model.ImageSearchResult;
import com.picturebackend.api.imagesearch.sub.GetImageFirstUrlApi;
import com.picturebackend.api.imagesearch.sub.GetImageListApi;
import com.picturebackend.api.imagesearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * packageName: com.picturebackend.api.imagesearch
 *
 * @author: idpeng
 * @version: 1.0
 * @className: ImageSearchApiFacade
 * @date: 2025/8/23 22:33
 * @description: 图片搜索服务（门面模式）
 */
@Slf4j
public class ImageSearchApiFacade {

    /**
     * 通过图像URL搜索相似图片
     * 调用百度以图搜图接口，获取相似图片列�?
     *
     * @param imageUrl 图片的URL地址
     * @return 相似图片搜索结果列表
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://picture-1356335042.cos.ap-chongqing.myqcloud.com/public/1955263849267466241/2025-08-21_4DjueShOTnofOW8x.webp";
        List<ImageSearchResult> resultList = searchImage(imageUrl);
        System.out.println("结果列表" + resultList);
    }
}
