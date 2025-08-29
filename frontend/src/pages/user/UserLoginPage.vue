<template>
  <div id="userLoginPage">
    <h2 class="title">鱼皮云图库 - 用户登录</h2>
    <div class="desc">企业级智能协同云图库</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码长度不能小于 8 位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>
      <div class="tips">
        没有账号？
        <RouterLink to="/user/register">去注册</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">登录</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { userLoginUsingPost } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'
import router from '@/router' // 用于接受表单输入的值

// 用于接受表单输入的值
const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const loginUserStore = useLoginUserStore()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  try {
    const res = await userLoginUsingPost(values)
    console.log('登录API响应:', res)
    
    // 检查响应格式：后端返回BaseResponse<LoginUserVO>，成功时code为0
    if (res.data && res.data.code === 0 && res.data.data) {
      // 登录成功，把登录态保存到全局状态中
      await loginUserStore.fetchLoginUser()
      message.success('登录成功，欢迎回来！')
      router.push({
        path: '/',
        replace: true,
      })
    } else {
      // 登录失败，根据错误码提供具体的错误信息
      let errorMsg = '登录失败，请稍后重试'
      
      if (res.data) {
        switch (res.data.code) {
          case 40000:
            errorMsg = res.data.message || '用户不存在或密码错误，请检查账号密码'
            break
          case 40001:
            errorMsg = '参数错误，请检查输入信息'
            break
          case 40100:
            errorMsg = '未登录，请重新登录'
            break
          case 40300:
            errorMsg = '无权限访问'
            break
          case 50000:
            errorMsg = '服务器内部错误，请稍后重试'
            break
          default:
            errorMsg = res.data.message || '登录失败，请稍后重试'
        }
      }
      
      console.warn('登录失败:', res.data)
      message.error(errorMsg)
    }
  } catch (error) {
    console.error('登录请求异常:', error)
    
    // 网络异常或其他错误
    if (error.response) {
      // 服务器返回了错误状态码
      const status = error.response.status
      let errorMsg = '服务器错误，请稍后重试'
      
      switch (status) {
        case 400:
          errorMsg = '请求参数错误，请检查输入信息'
          break
        case 401:
          errorMsg = '认证失败，请检查账号密码'
          break
        case 403:
          errorMsg = '访问被拒绝，请联系管理员'
          break
        case 404:
          errorMsg = '服务不存在，请联系技术支持'
          break
        case 500:
          errorMsg = '服务器内部错误，请稍后重试'
          break
        case 502:
        case 503:
        case 504:
          errorMsg = '服务暂时不可用，请稍后重试'
          break
        default:
          errorMsg = error.response.data?.message || `服务器错误 (${status})，请稍后重试`
      }
      
      message.error(errorMsg)
    } else if (error.request) {
      // 请求发出但没有收到响应
      message.error('网络连接失败，请检查网络连接后重试')
    } else {
      // 其他错误
      message.error('登录失败，请稍后重试')
    }
  }
}
</script>

<style scoped>
#userLoginPage {
  max-width: 360px;
  margin: 0 auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  color: #bbb;
  text-align: right;
  font-size: 13px;
  margin-bottom: 16px;
}
</style>
