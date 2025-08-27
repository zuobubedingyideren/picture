<template>
  <div id="userManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" allow-clear />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" allow-clear />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <div style="margin-bottom: 16px" />
    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.dataIndex === 'userName'">
          <div v-if="editingKey === record.id">
            <a-input v-model:value="editingRecord.userName" placeholder="请输入用户名" />
          </div>
          <div v-else>
            {{ record.userName }}
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'userAvatar'">
          <div v-if="editingKey === record.id">
            <a-input v-model:value="editingRecord.userAvatar" placeholder="请输入头像URL" />
            <a-image v-if="editingRecord.userAvatar" :src="editingRecord.userAvatar" :width="60" style="margin-top: 8px" />
          </div>
          <div v-else>
            <a-image :src="record.userAvatar" :width="120" />
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'userProfile'">
          <div v-if="editingKey === record.id">
            <a-textarea v-model:value="editingRecord.userProfile" placeholder="请输入用户简介" :rows="2" />
          </div>
          <div v-else>
            {{ record.userProfile }}
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="editingKey === record.id">
            <a-select v-model:value="editingRecord.userRole" style="width: 120px">
              <a-select-option value="user">普通用户</a-select-option>
              <a-select-option value="admin">管理员</a-select-option>
            </a-select>
          </div>
          <div v-else>
            <div v-if="record.userRole === 'admin'">
              <a-tag color="green">管理员</a-tag>
            </div>
            <div v-else>
              <a-tag color="blue">普通用户</a-tag>
            </div>
          </div>
        </template>
        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <div v-if="editingKey === record.id">
            <a-button type="primary" size="small" @click="doSave(record.id)" style="margin-right: 8px">保存</a-button>
            <a-button size="small" @click="doCancel">取消</a-button>
          </div>
          <div v-else>
            <a-button type="link" size="small" @click="doEdit(record)" style="margin-right: 8px">编辑</a-button>
            <a-button danger size="small" @click="doDelete(record.id)">删除</a-button>
          </div>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteUserUsingPost, listUserVoByPageUsingPost, updateUserUsingPost } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 定义数据
const dataList = ref<API.UserVO[]>([])
const total = ref(0)

// 编辑相关状态
const editingKey = ref<number | null>(null)
const editingRecord = ref<API.UserUpdateRequest>({})
const originalRecord = ref<API.UserVO>({})

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'ascend',
})

// 获取数据
const fetchData = async () => {
  const res = await listUserVoByPageUsingPost({
    ...searchParams,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 页面加载时获取数据，请求一次
onMounted(() => {
  fetchData()
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current,
    pageSize: searchParams.pageSize,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
  }
})

// 表格变化之后，重新获取数据
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  fetchData()
}

// 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteUserUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}

/**
 * 开始编辑用户信息
 * @param record 要编辑的用户记录
 */
const doEdit = (record: API.UserVO) => {
  editingKey.value = record.id
  // 保存原始记录用于取消时恢复
  originalRecord.value = { ...record }
  // 设置编辑记录
  editingRecord.value = {
    id: record.id,
    userName: record.userName,
    userAvatar: record.userAvatar,
    userProfile: record.userProfile,
    userRole: record.userRole
  }
}

/**
 * 保存编辑的用户信息
 * @param id 用户ID
 */
const doSave = async (id: number) => {
  if (!editingRecord.value.id) {
    message.error('用户ID不能为空')
    return
  }
  
  try {
    const res = await updateUserUsingPost(editingRecord.value)
    if (res.data.code === 0) {
      message.success('更新成功')
      // 更新本地数据
      const index = dataList.value.findIndex(item => item.id === id)
      if (index !== -1) {
        dataList.value[index] = {
          ...dataList.value[index],
          userName: editingRecord.value.userName,
          userAvatar: editingRecord.value.userAvatar,
          userProfile: editingRecord.value.userProfile,
          userRole: editingRecord.value.userRole
        }
      }
      // 退出编辑模式
      doCancel()
    } else {
      message.error('更新失败：' + res.data.message)
    }
  } catch (error) {
    message.error('更新失败')
    console.error('更新用户信息失败:', error)
  }
}

/**
 * 取消编辑，恢复原始数据
 */
const doCancel = () => {
  editingKey.value = null
  editingRecord.value = {}
  originalRecord.value = {}
}
</script>
