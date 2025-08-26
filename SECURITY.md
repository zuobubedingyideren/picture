# 安全配置指南

## 环境变量配置

为了保护敏感信息，本项目使用环境变量来管理API密钥和访问凭证。请按照以下步骤进行配置：

### 1. 后端配置

#### 开发环境
1. 复制 `backend/src/main/resources/application-example.yml` 为 `application-local.yml`
2. 在 `application-local.yml` 中填入真实的配置值
3. 确保 `application-local.yml` 不会被提交到版本控制（已在 .gitignore 中配置）

#### 生产环境
设置以下系统环境变量：

```bash
# 阿里云AI配置
export ALI_YUN_AI_API_KEY="your-actual-api-key"

# 腾讯云COS配置
export TENCENT_COS_SECRET_ID="your-actual-secret-id"
export TENCENT_COS_SECRET_KEY="your-actual-secret-key"
```

### 2. 需要配置的服务

#### 阿里云AI服务
- **用途**: AI图片处理和分析
- **获取方式**: 登录阿里云控制台 → 人工智能 → 获取API Key
- **环境变量**: `ALI_YUN_AI_API_KEY`

#### 腾讯云COS服务
- **用途**: 图片存储和CDN加速
- **获取方式**: 登录腾讯云控制台 → 对象存储COS → 密钥管理
- **环境变量**: 
  - `TENCENT_COS_SECRET_ID`
  - `TENCENT_COS_SECRET_KEY`

### 3. 安全最佳实践

1. **永远不要**将真实的API密钥提交到版本控制系统
2. **定期轮换**API密钥和访问凭证
3. **使用最小权限原则**，只授予必要的权限
4. **监控API使用情况**，及时发现异常访问
5. **在生产环境中使用专用的密钥管理服务**

### 4. 故障排除

如果遇到配置问题：

1. 检查环境变量是否正确设置
2. 确认API密钥是否有效且未过期
3. 验证服务权限配置是否正确
4. 查看应用日志获取详细错误信息

### 5. 联系支持

如果需要帮助，请：
1. 检查本文档的常见问题
2. 查看项目的 Issue 页面
3. 联系项目维护者

**注意**: 在寻求帮助时，请不要分享真实的API密钥或敏感信息。