<template>
  <a-modal
    class="image-cropper"
    v-model:visible="visible"
    title="编辑图片"
    :footer="false"
    @cancel="closeModal"
  >
    <!-- 图片裁切组件 -->
    <vue-cropper
          ref="cropperRef"
          :img="actualImageUrl || 'https://picsum.photos/400/300'"
          :auto-crop="true"
          :auto-crop-width="300"
          :auto-crop-height="300"
          :fixed="false"
          :can-move="true"
          :can-move-box="true"
          :can-scale="true"
          :center-box="true"
          :high="true"
          :info="true"
          :max-img-size="3000"
          :enlarge="1"
          :mode="'contain'"
          :output-size="1"
          :output-type="'jpeg'"
          @imgLoad="onImageLoad"
          @imgError="onImageError"
          style="height: 400px"
        />
    <div style="margin-bottom: 16px" />
    <!-- 图片操作 -->
    <div class="image-cropper-actions">
      <a-space>
        <a-button @click="rotateLeft">向左旋转</a-button>
        <a-button @click="rotateRight">向右旋转</a-button>
        <a-button @click="changeScale(1)">放大</a-button>
        <a-button @click="changeScale(-1)">缩小</a-button>
        <a-button type="primary" :loading="loading" @click="handleConfirm">确认</a-button>
      </a-space>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue'
import { uploadPictureUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

interface Props {
  imageUrl?: string
  picture?: API.PictureVO
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()

// 实际使用的图片URL（可能是原始URL或代理URL）
const actualImageUrl = ref('')

// 监听图片URL变化，实现fallback机制
watch(
  () => props.imageUrl,
  async (newUrl) => {
    console.debug('ImageCropper: 图片URL变化', newUrl)
    if (newUrl) {
      // 如果是COS图片，直接使用代理URL
      if (newUrl.includes('cos.ap-chongqing.myqcloud.com')) {
        console.debug('ImageCropper: 检测到COS图片，直接使用代理URL')
        const proxyUrl = `http://localhost:8123/api/picture/proxy?url=${encodeURIComponent(newUrl)}`
        console.debug('ImageCropper: 代理URL', proxyUrl)
        actualImageUrl.value = proxyUrl
      } else {
        actualImageUrl.value = newUrl
      }
    } else {
      actualImageUrl.value = ''
    }
  },
  { immediate: true }
)

// 获取图片裁切器的引用
const cropperRef = ref()

// 图片加载成功事件
const onImageLoad = () => {
  console.log('ImageCropper: vue-cropper Image loaded successfully')
  console.log('ImageCropper: Loaded actualImageUrl:', actualImageUrl.value)
}

// 图片加载错误处理，增强fallback逻辑
const onImageError = () => {
  console.debug('ImageCropper: 图片加载失败', actualImageUrl.value)
  
  // 如果代理URL失败，使用默认图片
  if (actualImageUrl.value && actualImageUrl.value.includes('/api/picture/proxy')) {
    console.debug('ImageCropper: 代理URL失败，使用默认图片')
    actualImageUrl.value = 'https://via.placeholder.com/300x200/cccccc/666666?text=Image+Load+Failed'
  }
  // 如果是其他URL失败，也使用默认图片
  else {
    console.debug('ImageCropper: 图片加载失败，使用默认图片')
    actualImageUrl.value = 'https://via.placeholder.com/300x200/cccccc/666666?text=Image+Load+Failed'
  }
}

// 缩放比例
const changeScale = (num) => {
  cropperRef.value?.changeScale(num)
}

// 向左旋转
const rotateLeft = () => {
  cropperRef.value.rotateLeft()
}

// 向右旋转
const rotateRight = () => {
  cropperRef.value.rotateRight()
}

// 确认裁切
const handleConfirm = () => {
  cropperRef.value.getCropBlob((blob: Blob) => {
    // blob 为已经裁切好的文件
    const fileName = (props.picture?.name || 'image') + '.png'
    const file = new File([blob], fileName, { type: blob.type })
    // 上传图片
    handleUpload({ file })
  })
}

const loading = ref(false)

/**
 * 上传图片
 * @param file
 */
const handleUpload = async ({ file }: any) => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = props.picture ? { id: props.picture.id } : {}
    params.spaceId = props.spaceId
    const res = await uploadPictureUsingPost(params, {}, file)
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
      closeModal();
    } else {
      message.error('图片上传失败，' + res.data.message)
    }
  } catch (error) {
    console.error('图片上传失败', error)
    message.error('图片上传失败，' + error.message)
  }
  loading.value = false
}

// 是否可见
const visible = ref(false)

// 打开弹窗
const openModal = () => {
  console.log('ImageCropper: openModal called')
  console.log('ImageCropper: props.imageUrl:', props.imageUrl)
  console.log('ImageCropper: actualImageUrl.value:', actualImageUrl.value)
  console.log('ImageCropper: cropperRef available:', !!cropperRef.value)
  
  // 如果没有图片URL，使用测试图片
  if (!props.imageUrl) {
    console.warn('ImageCropper: No imageUrl provided, using test image')
    const testImageUrl = 'https://picsum.photos/400/300'
    console.log('ImageCropper: Using test image:', testImageUrl)
    actualImageUrl.value = testImageUrl
  }
  
  visible.value = true
  
  // 延迟一下确保DOM更新完成
  setTimeout(() => {
    console.log('ImageCropper: Modal opened, current actualImageUrl:', actualImageUrl.value)
  }, 100)
}

// 关闭弹窗
const closeModal = () => {
  visible.value = false
}

// 暴露函数给父组件
defineExpose({
  openModal,
})
</script>

<style>
.image-cropper {
  text-align: center;
}

.image-cropper .vue-cropper {
  width: 100% !important;
  height: 400px !important;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
}

.image-cropper .vue-cropper .cropper-container {
  width: 100% !important;
  height: 100% !important;
}

/* 移除裁剪框的黑色边框 */
.image-cropper .vue-cropper .cropper-drag-box {
  border: none !important;
}

.image-cropper .vue-cropper .cropper-move {
  border: none !important;
}

.image-cropper .vue-cropper .cropper-modal {
  border: none !important;
}
</style>
