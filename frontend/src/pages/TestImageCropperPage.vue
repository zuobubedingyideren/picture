<template>
  <div id="testImageCropperPage">
    <h2>测试 ImageCropper 组件</h2>
    <p>测试COS图片URL: {{ testImageUrl }}</p>
    
    <a-space direction="vertical" size="large" style="width: 100%">
      <a-button type="primary" @click="openImageCropper">打开图片编辑器</a-button>
      
      <div>
        <h3>直接图片显示测试:</h3>
        <img 
          :src="testImageUrl" 
          style="max-width: 300px; max-height: 200px; border: 1px solid #ccc;"
          @load="onDirectImageLoad"
          @error="onDirectImageError"
        />
      </div>
      
      <div>
        <h3>代理图片显示测试:</h3>
        <img 
          :src="proxyImageUrl" 
          style="max-width: 300px; max-height: 200px; border: 1px solid #ccc;"
          @load="onProxyImageLoad"
          @error="onProxyImageError"
        />
      </div>
    </a-space>
    
    <ImageCropper
      ref="imageCropperRef"
      :imageUrl="testImageUrl"
      :picture="testPicture"
      :onSuccess="onCropSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import ImageCropper from '@/components/ImageCropper.vue'

// 页面加载时的调试信息
onMounted(() => {
  console.log('🔧 TestImageCropperPage 页面已加载')
  console.log('🔧 测试图片URL:', testImageUrl)
  console.log('🔧 代理图片URL:', proxyImageUrl.value)
})

// 测试用的COS图片URL（用户报告的失败URL）
const testImageUrl = 'https://picture-1356335042.cos.ap-chongqing.myqcloud.com/public/1955263849267466241/2025-08-28_8jMkDr6jvDbp0UCc.webp'

// 代理URL
const proxyImageUrl = computed(() => {
  return `/api/picture/proxy?url=${encodeURIComponent(testImageUrl)}`
})

// 测试图片对象
const testPicture = ref<API.PictureVO>({
  id: 1955263849267466241,
  url: testImageUrl,
  name: '测试图片',
  introduction: '用于测试ImageCropper组件的COS图片'
})

const imageCropperRef = ref()

// 打开图片编辑器
const openImageCropper = () => {
  console.log('打开ImageCropper，图片URL:', testImageUrl)
  imageCropperRef.value?.openModal()
}

// 编辑成功回调
const onCropSuccess = (newPicture: API.PictureVO) => {
  console.log('图片编辑成功:', newPicture)
  message.success('图片编辑成功')
}

// 直接图片加载事件
const onDirectImageLoad = () => {
  console.log('✅ 直接COS图片加载成功')
  message.success('直接COS图片加载成功')
}

const onDirectImageError = (event: Event) => {
  console.error('❌ 直接COS图片加载失败:', event)
  message.error('直接COS图片加载失败')
}

// 代理图片加载事件
const onProxyImageLoad = () => {
  console.log('✅ 代理图片加载成功')
  message.success('代理图片加载成功')
}

const onProxyImageError = (event: Event) => {
  console.error('❌ 代理图片加载失败:', event)
  message.error('代理图片加载失败')
}
</script>

<style scoped>
#testImageCropperPage {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

h3 {
  margin-bottom: 10px;
}

img {
  display: block;
  margin-bottom: 10px;
}
</style>