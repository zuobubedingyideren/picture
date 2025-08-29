import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getLoginUserUsingGet } from '@/api/userController.ts'

/**
 * 存储登录用户信息的状态
 */
export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })

  /**
   * 远程获取登录用户信息
   */
  async function fetchLoginUser() {
    try {
      const res = await getLoginUserUsingGet()
      console.log('获取登录用户API响应:', res)
      
      // 检查响应格式：后端返回BaseResponse<LoginUserVO>，成功时code为0
      if (res.data && res.data.code === 0 && res.data.data) {
        loginUser.value = res.data.data
        console.log('登录用户信息更新成功:', res.data.data)
      } else {
        // 如果是未登录状态（code为40100），不显示警告，这是正常情况
        if (res.data && res.data.code === 40100) {
          console.log('用户未登录，保持默认状态')
        } else {
          console.warn('获取登录用户信息失败，响应格式异常:', res.data)
        }
        // 保持默认的未登录状态
      }
    } catch (error) {
      // 如果是401未授权错误，不显示警告，这是正常的未登录状态
      if (error.response && error.response.status === 401) {
        console.log('用户未登录，保持默认状态')
      } else {
        console.warn('获取登录用户信息失败:', error)
      }
      // 保持默认的未登录状态
    }
  }

  /**
   * 设置登录用户
   * @param newLoginUser
   */
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  // 返回
  return { loginUser, fetchLoginUser, setLoginUser }
})
