<template>
  <div class="user-profile-page">
    <a-card title="个人信息设置" :bordered="false">
      <a-form
          ref="formRef"
          :model="userForm"
          layout="vertical"
          @finish="handleSubmit"
        >
        <!-- 用户ID - 只读显示 -->
        <a-form-item label="用户ID" name="id">
          <a-input-group compact class="user-id-group">
            <a-input
              :value="formattedUserId"
              readonly
              placeholder="系统自动生成"
              class="readonly-field user-id-input"
            />
            <a-button
              type="default"
              :disabled="pageLoading || !userForm.id"
              @click="copyUserId"
              class="copy-button"
              title="复制用户ID"
            >
              <CopyOutlined />
            </a-button>
          </a-input-group>
        </a-form-item>

        <!-- 用户头像 -->
        <a-form-item label="头像" name="userAvatar">
          <div class="avatar-upload">
            <a-avatar :size="80" :src="userForm.userAvatar">
              <template #icon>
                <UserOutlined />
              </template>
            </a-avatar>
            <a-upload
              name="avatar"
              list-type="picture-card"
              class="avatar-uploader"
              :show-upload-list="false"
              :custom-request="customUpload"
              :before-upload="beforeUpload"
            >
              <img v-if="userForm.userAvatar" :src="userForm.userAvatar" alt="avatar" style="width: 100%; height: 100%; object-fit: cover;" />
              <div v-else>
                <loading-outlined v-if="uploading"></loading-outlined>
                <plus-outlined v-else></plus-outlined>
                <div class="ant-upload-text">上传头像</div>
              </div>
            </a-upload>
          </div>
        </a-form-item>

        <!-- 用户名 -->
        <a-form-item label="用户名" name="userName">
          <a-input
            v-model:value="userForm.userName"
            placeholder="请输入用户名"
            :maxlength="20"
            show-count
          />
        </a-form-item>

        <!-- 个人简介 -->
        <a-form-item label="个人简介" name="userProfile">
          <a-textarea
            v-model:value="userForm.userProfile"
            placeholder="请输入个人简介"
            :rows="4"
            :maxlength="200"
            show-count
          />
        </a-form-item>

        <!-- 提交按钮 -->
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit" :loading="loading">
              保存修改
            </a-button>
            <a-button @click="resetForm">
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import { LoadingOutlined, PlusOutlined, UserOutlined, CopyOutlined } from '@ant-design/icons-vue'
 import { getLoginUserUsingGet, updateMyInfoUsingPost } from '@/api/userController'
 import { uploadAvatarUsingPost } from '@/api/fileController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

const loginUserStore = useLoginUserStore()
const formRef = ref()
const loading = ref(false)
// 页面初始加载状态
const pageLoading = ref(true)

// 表单数据 - 扩展包含用户ID字段
const userForm = reactive<API.UserUpdateMyInfoRequest & { id?: number }>({
  userName: '',
  userAvatar: '',
  userProfile: '',
  id: undefined  // 新增用户ID字段，仅用于显示
})

// 格式化用户ID显示
const formattedUserId = computed(() => {
  if (pageLoading.value) {
    return '加载中...'
  }
  if (userForm.id && userForm.id > 0) {
    return `${userForm.id}`
  }
  return '系统自动生成'
})

// 上传状态
const uploading = ref(false)



/**
 * 获取当前用户信息
 */
const fetchUserInfo = async () => {
  try {
    pageLoading.value = true
    const res = await getLoginUserUsingGet()
    if (res.data && res.data.code === 0 && res.data.data) {
      const userData = res.data.data
      // 设置用户ID（只读显示）
      userForm.id = userData.id || 0
      // 设置可编辑字段
      userForm.userName = userData.userName || ''
      userForm.userAvatar = userData.userAvatar || ''
      userForm.userProfile = userData.userProfile || ''
    } else {
      message.error('获取用户信息失败')
    }
  } catch (error) {
    message.error('获取用户信息失败')
    console.error('获取用户信息失败:', error)
  } finally {
    // 无论成功还是失败，都结束加载状态
    pageLoading.value = false
  }
}

/**
 * 上传前的验证
 * @param file 上传的文件
 * @returns 是否允许上传
 */
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件!')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

/**
 * 自定义头像上传处理
 * @param options 上传选项
 */
const customUpload = async (options: any) => {
  const { file, onSuccess, onError } = options

  uploading.value = true
  try {
     const res = await uploadAvatarUsingPost({}, file)
     // 检查响应状态和数据
     if (res.data && (res.data as any).code === 0 && (res.data as any).data) {
       // 获取实际的图片URL字符串
       const imageUrl = (res.data as any).data
       // 验证URL是否有效
       if (typeof imageUrl === 'string' && imageUrl.trim()) {
         userForm.userAvatar = imageUrl
         onSuccess(res, file)
         message.success('头像上传成功')
       } else {
         onError(new Error('获取图片URL失败'))
         message.error('获取图片URL失败')
       }
     } else {
       const errorMsg = (res.data as any)?.message || '上传失败'
       onError(new Error(errorMsg))
       message.error(errorMsg)
     }
  } catch (error) {
    onError(error)
    message.error('头像上传失败')
    console.error('头像上传失败:', error)
  } finally {
    uploading.value = false
  }
}

/**
 * 提交表单，更新用户信息
 */
const handleSubmit = async () => {
  loading.value = true
  try {
    // 构建更新数据，明确排除用户ID字段
    const updateData: API.UserUpdateMyInfoRequest = {
      userName: userForm.userName,
      userAvatar: userForm.userAvatar,
      userProfile: userForm.userProfile
      // 注意：不包含 id 字段，确保用户ID不被修改
    }
    const res = await updateMyInfoUsingPost(updateData)
    if (res.data) {
      message.success('个人信息更新成功')
      // 刷新全局状态中的用户信息
      await loginUserStore.fetchLoginUser()
    } else {
      message.error('更新失败，请重试')
    }
  } catch (error) {
    message.error('更新失败，请重试')
    console.error('更新用户信息失败:', error)
  } finally {
    loading.value = false
  }
}



/**
 * 复制用户ID到剪贴板
 */
const copyUserId = async () => {
  try {
    if (!userForm.id) {
      message.warning('用户ID不存在，无法复制')
      return
    }

    const userIdText = userForm.id.toString()

    // 使用现代剪贴板API
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(userIdText)
    } else {
      // 降级方案：使用传统方法
      const textArea = document.createElement('textarea')
      textArea.value = userIdText
      textArea.style.position = 'fixed'
      textArea.style.left = '-999999px'
      textArea.style.top = '-999999px'
      document.body.appendChild(textArea)
      textArea.focus()
      textArea.select()
      document.execCommand('copy')
      textArea.remove()
    }

    // 显示成功提示，2秒后自动消失
    message.success('复制成功', 2)
  } catch (error) {
    console.error('复制失败:', error)
    message.error('复制失败，请手动复制')
  }
}

/**
 * 重置表单
 */
const resetForm = () => {
  // 重新获取用户信息（包括用户ID）
  fetchUserInfo()
}

// 页面加载时获取用户信息
onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.user-profile-page {
  max-width: 600px;
  margin: 0 auto;
  padding: 24px;
}

.avatar-upload {
  display: flex;
  align-items: center;
}

.ant-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 只读字段样式 */
.readonly-field {
  background-color: #ffffff !important;
  color: #666 !important;
  cursor: not-allowed !important;
}

.readonly-field:hover {
  background-color: #ffffff !important;
  border-color: #d9d9d9 !important;
}

.readonly-field:focus {
  background-color: #ffffff !important;
  border-color: #d9d9d9 !important;
  box-shadow: none !important;
}

/* 用户ID输入组合样式 */
.user-id-group {
  display: flex;
  width: 100%;
}

.user-id-input {
  flex: 1;
  border-top-right-radius: 0 !important;
  border-bottom-right-radius: 0 !important;
}

.copy-button {
  border-top-left-radius: 0 !important;
  border-bottom-left-radius: 0 !important;
  border-left: 0 !important;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  padding: 0 8px;
  flex-shrink: 0;
}

.copy-button:hover:not(:disabled) {
  background-color: #f0f0f0;
  border-color: #40a9ff;
  color: #40a9ff;
  z-index: 2;
}

.copy-button:disabled {
  background-color: #f5f5f5;
  border-color: #d9d9d9;
  color: #bfbfbf;
  cursor: not-allowed;
}

/* Ant Design 输入组合样式覆盖 */
.user-id-group.ant-input-group-compact {
  display: flex !important;
}

.user-id-group.ant-input-group-compact .ant-input {
  border-right: 0;
  flex: 1;
}

.user-id-group.ant-input-group-compact .ant-btn {
  border-left: 0;
  flex-shrink: 0;
}
</style>
