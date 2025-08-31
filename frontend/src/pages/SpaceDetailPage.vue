<template>
  <div id="spaceDetailPage">
    <!-- 空间信息 -->
    <a-spin :spinning="spaceLoading" tip="正在加载空间信息...">
      <a-flex justify="space-between">
        <h2>{{ space.spaceName }}（{{ SPACE_TYPE_MAP[space.spaceType] }}）</h2>
      <a-space size="middle">
        <a-button
          v-if="canUploadPicture"
          type="primary"
          :href="`/add_picture?spaceId=${id}`"
          target="_blank"
        >
          + 创建图片
        </a-button>
        <a-button
          v-if="canManageSpaceUser && space.spaceType !== 0"
          type="primary"
          ghost
          :icon="h(TeamOutlined)"
          :href="`/spaceUserManage/${id}`"
          target="_blank"
        >
          成员管理
        </a-button>
        <a-button
          v-if="canManageSpaceUser && space.spaceType !== 0"
          type="primary"
          ghost
          :icon="h(BarChartOutlined)"
          :href="`/space_analyze?spaceId=${id}`"
          target="_blank"
        >
          空间分析
        </a-button>
        <a-button v-if="canEditPicture" :icon="h(EditOutlined)" @click="doBatchEdit"> 批量编辑</a-button>
        <a-tooltip
          :title="`占用空间 ${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`"
        >
          <a-progress
            type="circle"
            :size="42"
            :percent="((space.totalSize * 100) / space.maxSize).toFixed(1)"
          />
        </a-tooltip>
      </a-space>
    </a-flex>
    </a-spin>
    <div style="margin-bottom: 16px" />
    <!-- 搜索表单 -->
    <PictureSearchForm :onSearch="onSearch" />
    <div style="margin-bottom: 16px" />
    <!-- 按颜色搜索，跟其他搜索条件独立 -->
    <a-form-item label="按颜色搜索">
      <color-picker format="hex" @pureColorChange="onColorChange" />
    </a-form-item>
    <!-- 图片列表 -->
    <PictureList
      :dataList="dataList"
      :loading="loading"
      :showOp="true"
      :canEdit="canEditPicture"
      :canDelete="canDeletePicture"
      :onReload="fetchData"
    />
    <!-- 分页 -->
    <a-pagination
      style="text-align: right"
      v-model:current="searchParams.current"
      v-model:pageSize="searchParams.pageSize"
      :total="total"
      @change="onPageChange"
    />
    <BatchEditPictureModal
      ref="batchEditPictureModalRef"
      :spaceId="id"
      :pictureList="dataList"
      :onSuccess="onBatchEditPictureSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref, watch } from 'vue'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import {
  listPictureVoByPageUsingPost,
  searchPictureByColorUsingPost,
} from '@/api/pictureController.ts'
import { formatSize } from '@/utils'
import PictureList from '@/components/PictureList.vue'
import PictureSearchForm from '@/components/PictureSearchForm.vue'
import { ColorPicker } from 'vue3-colorpicker'
import 'vue3-colorpicker/style.css'
import BatchEditPictureModal from '@/components/BatchEditPictureModal.vue'
import { BarChartOutlined, EditOutlined, TeamOutlined } from '@ant-design/icons-vue'
import { SPACE_PERMISSION_ENUM, SPACE_TYPE_MAP } from '../constants/space.ts'

interface Props {
  id: string | number
}

const props = defineProps<Props>()
const space = ref<API.SpaceVO>({})
const spaceLoading = ref(false)
const retryCount = ref(0)
const maxRetries = 3

// 验证ID参数是否有效（修复大整数精度丢失问题）
const validateId = (id: string | number): boolean => {
  if (id === null || id === undefined || id === '') {
    console.error('SpaceDetailPage: ID参数为空')
    return false
  }

  // 转换为字符串进行验证，避免大整数精度丢失
  const idStr = String(id)

  // 验证是否为纯数字字符串
  if (!/^\d+$/.test(idStr)) {
    console.error('SpaceDetailPage: ID参数格式无效:', id)
    return false
  }

  // 验证是否为正数
  if (idStr === '0' || idStr.startsWith('0')) {
    console.error('SpaceDetailPage: ID参数不能为0或以0开头:', id)
    return false
  }

  console.log('SpaceDetailPage: ID参数验证通过 (字符串):', idStr)
  console.log('SpaceDetailPage: 原始ID类型:', typeof id, '值:', id)
  return true
}

// 通用权限检查函数
function createPermissionChecker(permission: string) {
  return computed(() => {
    return (space.value.permissionList ?? []).includes(permission)
  })
}

// 定义权限检查
const canManageSpaceUser = createPermissionChecker(SPACE_PERMISSION_ENUM.SPACE_USER_MANAGE)
const canUploadPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_UPLOAD)
const canEditPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDeletePicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)

// -------- 获取空间详情 --------
const fetchSpaceDetail = async (isRetry = false) => {
  // 参数验证
  if (!validateId(props.id)) {
    message.error('空间ID参数无效，请检查URL中的ID是否正确')
    return
  }

  // 使用字符串ID，避免大整数精度丢失
  const spaceId = String(props.id)
  console.log('SpaceDetailPage: 开始获取空间详情')
  console.log('SpaceDetailPage: 原始ID:', props.id, '类型:', typeof props.id)
  console.log('SpaceDetailPage: 传递给API的ID:', spaceId, '类型:', typeof spaceId)
  console.log('SpaceDetailPage: 重试次数:', retryCount.value)

  // 验证是否发生精度丢失
  if (typeof props.id === 'string' && Number(props.id).toString() !== props.id) {
    console.warn('SpaceDetailPage: 检测到大整数精度丢失风险，使用字符串传递ID')
    console.warn('SpaceDetailPage: Number转换结果:', Number(props.id))
    console.warn('SpaceDetailPage: 原始字符串:', props.id)
  }

  spaceLoading.value = true

  try {
    const res = await getSpaceVoByIdUsingGet({
      id: spaceId,
    })

    console.log('SpaceDetailPage: API响应:', res)

    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
      retryCount.value = 0 // 成功后重置重试次数
      console.log('SpaceDetailPage: 空间详情获取成功:', space.value)
    } else {
      const errorMsg = res.data.message || '未知错误'
      console.error('SpaceDetailPage: API返回错误:', res.data)

      if (res.data.code === 40400) {
        message.error(`空间不存在或已被删除 (ID: ${spaceId})`)
      } else if (res.data.code === 40300) {
        message.error('没有权限访问该空间')
      } else {
        message.error(`获取空间详情失败: ${errorMsg}`)
      }
    }
  } catch (e: any) {
    console.error('SpaceDetailPage: 请求异常:', e)

    let errorMessage = '获取空间详情失败'

    if (e.code === 'NETWORK_ERROR' || e.message?.includes('Network Error')) {
      errorMessage = '网络连接失败，请检查网络连接'
    } else if (e.response?.status === 404) {
      errorMessage = `空间不存在 (ID: ${spaceId})`
    } else if (e.response?.status === 403) {
      errorMessage = '没有权限访问该空间'
    } else if (e.response?.status >= 500) {
      errorMessage = '服务器内部错误，请稍后重试'
    } else {
      errorMessage = `请求失败: ${e.message || '未知错误'}`
    }

    // 重试机制
    if (!isRetry && retryCount.value < maxRetries) {
      retryCount.value++
      console.log(`SpaceDetailPage: 准备重试 (${retryCount.value}/${maxRetries})`)
      setTimeout(() => {
        fetchSpaceDetail(true)
      }, 1000 * retryCount.value) // 递增延迟重试
    } else {
      message.error(errorMessage)
      if (retryCount.value >= maxRetries) {
        message.warning('已达到最大重试次数，请手动刷新页面重试')
      }
    }
  } finally {
    spaceLoading.value = false
  }
}

onMounted(() => {
  console.log('SpaceDetailPage onMounted 被调用，路由参数 id:', props.id)
  console.log('当前路由信息:', window.location.href)
  console.log('ID类型:', typeof props.id, '值:', props.id)
  fetchSpaceDetail()
})

// --------- 获取图片列表 --------

// 定义数据
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

// 搜索条件
const searchParams = ref<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 获取数据
const fetchData = async () => {
     loading.value = true
  // 使用字符串ID，避免大整数精度丢失
  const params = {
    spaceId: String(props.id),
    ...searchParams.value,
  }
  console.log('SpaceDetailPage: fetchData - 传递给API的spaceId:', params.spaceId, '类型:', typeof params.spaceId)

  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

// 页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
})

// 分页参数
const onPageChange = (page: number, pageSize: number) => {
  searchParams.value.current = page
  searchParams.value.pageSize = pageSize
  fetchData()
}

// 搜索
const onSearch = (newSearchParams: API.PictureQueryRequest) => {
  console.log('new', newSearchParams)

  searchParams.value = {
    ...searchParams.value,
    ...newSearchParams,
    current: 1,
  }
  console.log('searchparams', searchParams.value)
  fetchData()
}

// 按照颜色搜索
const onColorChange = async (color: string) => {
  loading.value = true
  const spaceId = String(props.id)
  console.log('SpaceDetailPage: onColorChange - 传递给API的spaceId:', spaceId, '类型:', typeof spaceId)

  const res = await searchPictureByColorUsingPost({
    picColor: color,
    spaceId: spaceId,
  })
  if (res.data.code === 0 && res.data.data) {
    const data = res.data.data ?? []
    dataList.value = data
    total.value = data.length
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

// ---- 批量编辑图片 -----
const batchEditPictureModalRef = ref()

// 批量编辑图片成功
const onBatchEditPictureSuccess = () => {
  fetchData()
}

// 打开批量编辑图片弹窗
const doBatchEdit = () => {
  if (batchEditPictureModalRef.value) {
    batchEditPictureModalRef.value.openModal()
  }
}

// 空间 id 改变时，必须重新获取数据
watch(
  () => props.id,
  (newSpaceId) => {
    fetchSpaceDetail()
    fetchData()
  },
)
</script>

<style scoped>
#spaceDetailPage {
  margin-bottom: 16px;
}
</style>
