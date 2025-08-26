// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 健康测试接口 GET /api/health */
export async function healthUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseString_>('/api/health', {
    method: 'GET',
    ...(options || {}),
  })
}
