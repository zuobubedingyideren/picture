/**
 * 图片工具函数
 * 处理图片URL的代理转换，解决跨域问题
 */

/**
 * 将腾讯云COS图片URL转换为代理URL
 * @param originalUrl 原始图片URL
 * @returns 转换后的代理URL
 */
export function convertImageUrl(originalUrl: string): string {
  if (!originalUrl) {
    return originalUrl
  }

  // 检查是否是腾讯云COS的URL
  const cosPattern = /https:\/\/picture-1356335042\.cos\.ap-chongqing\.myqcloud\.com/

  if (cosPattern.test(originalUrl)) {
    // 将腾讯云COS的URL转换为代理URL
    return originalUrl.replace(
      'https://picture-1356335042.cos.ap-chongqing.myqcloud.com',
      '/cos-proxy'
    )
  }

  // 如果不是腾讯云COS的URL，直接返回原URL
  return originalUrl
}

/**
 * 批量转换图片URL数组
 * @param urls 图片URL数组
 * @returns 转换后的URL数组
 */
export function convertImageUrls(urls: string[]): string[] {
  return urls.map(url => convertImageUrl(url))
}
