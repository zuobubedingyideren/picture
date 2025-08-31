<template>
  <div id="contactPage">
    <!-- 作者昵称展示区 -->
    <div class="author-title">
      <h1>{{ authorInfo.nickname }}</h1>
    </div>

    <!-- 联系方式卡片区 -->
    <a-row :gutter="[16, 16]" justify="center">
      <!-- 联系电话卡片 -->
      <a-col :xs="24" :sm="12" :md="8">
        <a-card
          hoverable
          class="contact-card"
          @click="makePhoneCall(authorInfo.phone)"
        >
          <template #cover>
            <div class="card-icon">
              <PhoneOutlined :style="{ fontSize: '48px', color: '#1890ff' }" />
            </div>
          </template>
          <a-card-meta title="联系电话" :description="authorInfo.phone" />
        </a-card>
      </a-col>

      <!-- QQ联系卡片 -->
      <a-col :xs="24" :sm="12" :md="8">
        <a-card
          hoverable
          class="contact-card"
          @click="openQQChat(authorInfo.qq)"
        >
          <template #cover>
            <div class="card-icon">
              <QqOutlined :style="{ fontSize: '48px', color: '#1890ff' }" />
            </div>
          </template>
          <a-card-meta title="QQ联系" :description="authorInfo.qq" />
        </a-card>
      </a-col>

      <!-- 打赏二维码卡片 -->
      <a-col :xs="24" :sm="24" :md="8">
        <a-card hoverable class="contact-card qrcode-card">
          <template #cover>
            <div class="qrcode-container">
              <img
                :src="authorInfo.qrCodeUrl"
                alt="打赏二维码"
                class="qrcode-image"
                @error="handleImageError"
                @click="showQrCodeModal"
              />
            </div>
          </template>
          <a-card-meta title="打赏支持" description="扫码支持作者" />
          <div class="card-action">
            <a-button type="primary" block disabled>
              <GiftOutlined /> 扫码打赏
            </a-button>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 感谢信息 -->
    <div class="thank-you">
      <a-typography-paragraph type="secondary">
        感谢您的关注和支持！如有任何问题或建议，欢迎随时联系我。
      </a-typography-paragraph>
    </div>

    <!-- 二维码弹窗 -->
    <a-modal
      v-model:open="qrCodeModalVisible"
      title="打赏二维码"
      :footer="null"
      :width="400"
      centered
      @cancel="closeQrCodeModal"
    >
      <div class="modal-qrcode-container">
        <img
          :src="authorInfo.qrCodeUrl"
          alt="打赏二维码"
          class="modal-qrcode-image"
        />
        <p class="modal-qrcode-text">扫码支持作者，感谢您的支持！</p>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { PhoneOutlined, QqOutlined, GiftOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import weixinQrCode from '../access/weixin.png'

// 弹窗状态管理
const qrCodeModalVisible = ref(false)

// 作者联系信息配置
interface AuthorContact {
  nickname: string
  phone: string
  qq: string
  qrCodeUrl: string
}

const authorInfo: AuthorContact = {
  nickname: "程序员星",
  phone: "19138349696",
  qq: "3098263664",
  qrCodeUrl: weixinQrCode
}

// 电话拨号功能
const makePhoneCall = (phone: string) => {
  try {
    window.location.href = `tel:${phone}`
  } catch (error) {
    message.error('无法启动拨号功能，请手动拨打电话')
  }
}

// QQ跳转功能
const openQQChat = (qq: string) => {
  try {
    window.open(`https://wpa.qq.com/msgrd?v=3&uin=${qq}&site=qq&menu=yes`, '_blank')
  } catch (error) {
    message.error('无法打开QQ聊天，请手动添加QQ好友')
  }
}

// 二维码加载错误处理
const handleImageError = (event: Event) => {
  const target = event.target as HTMLImageElement
  target.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZjVmNWY1Ii8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCwgc2Fucy1zZXJpZiIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzk5OTk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPuaJk+i1j+S6jOe7tOeggeaaguaXtuS4jeWPr+eUqDwvdGV4dD48L3N2Zz4='
  message.warning('二维码图片加载失败，请稍后重试')
}

// 显示二维码弹窗
const showQrCodeModal = () => {
  qrCodeModalVisible.value = true
}

// 关闭二维码弹窗
const closeQrCodeModal = () => {
  qrCodeModalVisible.value = false
}
</script>

<style scoped>
#contactPage {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: calc(100vh - 200px);
}

.author-title {
  text-align: center;
  margin-bottom: 32px;
}

.author-title h1 {
  font-size: 24px;
  color: #262626;
  margin: 0;
  font-weight: 600;
}

.contact-card {
  height: 100%;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
}

.contact-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 120px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.card-action {
  margin-top: 16px;
}

.qrcode-card .card-icon {
  background: #fff;
}

.qrcode-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 120px;
  padding: 16px;
}

.qrcode-image {
  max-width: 100%;
  max-height: 100%;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s ease;
}

.qrcode-image:hover {
  transform: scale(1.05);
}

/* 弹窗样式 */
.modal-qrcode-container {
  text-align: center;
  padding: 20px 0;
}

.modal-qrcode-image {
  width: 100%;
  max-width: 300px;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.modal-qrcode-text {
  margin-top: 16px;
  color: #666;
  font-size: 14px;
  margin-bottom: 0;
}

.thank-you {
  text-align: center;
  margin-top: 48px;
  padding: 24px;
  background: #fafafa;
  border-radius: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  #contactPage {
    padding: 16px;
  }

  .author-title {
    margin-bottom: 24px;
  }

  .author-title h1 {
    font-size: 20px;
  }

  .card-icon {
    height: 100px;
  }

  .qrcode-container {
    height: 100px;
    padding: 12px;
  }

  .thank-you {
    margin-top: 32px;
    padding: 16px;
  }
}

@media (max-width: 576px) {
  .card-icon {
    height: 80px;
  }

  .qrcode-container {
    height: 80px;
    padding: 8px;
  }
}
</style>
