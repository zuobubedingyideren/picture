<template>
  <div class="user-profile-page">
    <a-card title="个人信息设置" :bordered="false">
      <a-form
          ref="formRef"
          :model="userForm"
          layout="vertical"
          @finish="handleSubmit"
        >
        <!-- 用户头像 -->
        <a-form-item label="头像" name="userAvatar">
          <div class="avatar-upload">
            <a-avatar :size="80" :src="userForm.userAvatar" />
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
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { UploadOutlined, LoadingOutlined, PlusOutlined } from '@ant-design/icons-vue'
 import { getLoginUserUsingGet, updateMyInfoUsingPost } from '@/api/userController'
 import { uploadAvatarUsingPost } from '@/api/fileController'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

const loginUserStore = useLoginUserStore()
const formRef = ref()
const loading = ref(false)

// 表单数据
const userForm = reactive<API.UserUpdateMyInfoRequest>({
  userName: '',
  userAvatar: '',
  userProfile: ''
})

// 上传状态
const uploading = ref(false)

// 表单验证规则
const rules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ]
}

/**
 * 获取当前用户信息
 */
const fetchUserInfo = async () => {
  try {
    const res = await getLoginUserUsingGet()
    if (res.data) {
      const userData = res.data
      userForm.userName = userData.userName || ''
      userForm.userAvatar = userData.userAvatar || ''
      userForm.userProfile = userData.userProfile || ''
    } else {
      message.error('获取用户信息失败')
    }
  } catch (error) {
    message.error('获取用户信息失败')
    console.error('获取用户信息失败:', error)
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
     if (res.data && res.data.code === 0 && res.data.data) {
       // 获取实际的图片URL字符串
       const imageUrl = res.data.data
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
       const errorMsg = res.data?.message || '上传失败'
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
    const res = await updateMyInfoUsingPost({
      userName: userForm.userName,
      userAvatar: userForm.userAvatar,
      userProfile: userForm.userProfile
    })
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
 * 表单验证失败
 * @param errorInfo 错误信息
 */
const onFinishFailed = (errorInfo: any) => {
  console.log('表单验证失败:', errorInfo)
}

/**
 * 重置表单
 */
const resetForm = () => {
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
</style>
