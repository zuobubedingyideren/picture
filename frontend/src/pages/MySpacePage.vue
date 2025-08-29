<template>
  <div id="mySpacePage">
    <div v-if="loading" style="text-align: center; padding: 50px;">
      <a-spin size="large" />
      <p style="margin-top: 16px;">正在跳转，请稍后...</p>
    </div>
    <div v-else-if="error" style="text-align: center; padding: 50px;">
      <a-result
        status="error"
        title="加载失败"
        :sub-title="error"
      >
        <template #extra>
          <a-button type="primary" @click="checkUserSpace">重试</a-button>
          <a-button @click="router.push('/add_space')">创建空间</a-button>
        </template>
      </a-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { listSpaceVoByPageUsingPost } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import { onMounted, ref } from 'vue'
import { SPACE_TYPE_ENUM } from '@/constants/space.ts'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const loading = ref(true)
const error = ref('')

// 检查用户是否有个人空间
const checkUserSpace = async () => {
  try {
    loading.value = true
    error.value = ''
    
    // 首先尝试获取登录用户信息
    await loginUserStore.fetchLoginUser()
    
    // 用户未登录，则直接跳转到登录页面
    const loginUser = loginUserStore.loginUser
    console.log('当前登录用户:', loginUser)
    
    if (!loginUser?.id) {
      console.log('用户未登录，跳转到登录页面')
      router.replace('/user/login')
      return
    }
    
    console.log('正在查询用户空间...', { userId: loginUser.id })
    
    // 如果用户已登录，会获取该用户已创建的空间
    const res = await listSpaceVoByPageUsingPost({
      userId: loginUser.id,
      current: 1,
      pageSize: 1,
      spaceType: SPACE_TYPE_ENUM.PRIVATE,
    })
    
    console.log('API响应:', res)
    
    if (res.data) {
      // 如果有，则进入第一个空间
      if (res.data.records?.length > 0) {
        const space = res.data.records[0]
        console.log('找到用户空间，跳转到:', `/space/${space.id}`)
        router.replace(`/space/${space.id}`)
      } else {
        // 如果没有，则跳转到创建空间页面
        console.log('用户没有空间，跳转到创建页面')
        router.replace('/add_space')
        message.warn('请先创建空间')
      }
    } else {
      throw new Error('获取空间信息失败')
    }
  } catch (err: any) {
    console.error('检查用户空间失败:', err)
    console.error('错误详情:', {
      message: err.message,
      response: err.response,
      status: err.response?.status,
      data: err.response?.data
    })
    error.value = err.message || err.response?.data?.message || '网络连接失败，请检查后端服务是否正常运行'
    message.error('加载我的空间失败: ' + error.value)
  } finally {
    loading.value = false
  }
}

// 在页面加载时检查用户空间
onMounted(() => {
  checkUserSpace()
})
</script>
