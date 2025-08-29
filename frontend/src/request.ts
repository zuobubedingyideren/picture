import axios from "axios";
import {message} from "ant-design-vue";

// 创建 Axios 实例
const myAxios = axios.create({
  baseURL: 'http://localhost:8123',
  timeout: 60000,
  withCredentials: true,
});

// 全局请求拦截器
myAxios.interceptors.request.use(
  function (config) {
    // Do something before request is sent
    return config
  },
  function (error) {
    // Do something with request error
    return Promise.reject(error)
  },
)

// 全局响应拦截器
myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    // 未登录
    if (data.code === 40100) {
      // 不是获取用户信息的请求，并且用户目前不是已经在用户登录页面，则跳转到登录页面
      if (
        !response.request.responseURL.includes('user/get/login') &&
        !window.location.pathname.includes('/user/login')
      ) {
        message.warning('请先登录')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function (error) {
    // 处理网络错误和HTTP状态码错误
    console.log('Axios请求错误详情:', error)
    
    if (error.response) {
      // 服务器返回了错误状态码
      const status = error.response.status
      const data = error.response.data
      
      console.log('HTTP错误状态码:', status)
      console.log('错误响应数据:', data)
      
      // 根据状态码处理不同的错误
      switch (status) {
        case 401:
          // 未授权，如果不是登录相关请求，则跳转到登录页
          if (!error.config.url.includes('/user/login') && 
              !error.config.url.includes('/user/get/login') &&
              !window.location.pathname.includes('/user/login')) {
            message.warning('登录已过期，请重新登录')
            window.location.href = `/user/login?redirect=${window.location.href}`
          }
          break
        case 403:
          message.error('没有权限访问该资源')
          break
        case 404:
          message.error('请求的资源不存在')
          break
        case 500:
          message.error('服务器内部错误')
          break
        default:
          message.error(`请求失败: ${data?.message || '未知错误'}`)
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      console.error('网络请求超时或无响应:', error.request)
      message.error('网络连接失败，请检查网络设置')
    } else {
      // 请求配置出错
      console.error('请求配置错误:', error.message)
      message.error('请求配置错误')
    }
    
    return Promise.reject(error)
  },
)

export default myAxios;
