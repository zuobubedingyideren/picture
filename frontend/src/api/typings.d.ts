declare namespace API {
  type AI_ = {
    parameters?: Parameters
    /** 图片ID，用于标识需要进行扩图处理的原始图片 */
    pictureId?: number
  }

  type BaseResponseBoolean_ = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseCreateOutPaintingTaskResponse_ = {
    code?: number
    data?: CreateOutPaintingTaskResponse
    message?: string
  }

  type BaseResponseGetOutPaintingTaskResponse_ = {
    code?: number
    data?: GetOutPaintingTaskResponse
    message?: string
  }

  type BaseResponseInt_ = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponseListImageSearchResult_ = {
    code?: number
    data?: ImageSearchResult[]
    message?: string
  }

  type BaseResponseListPictureVO_ = {
    code?: number
    data?: PictureVO[]
    message?: string
  }

  type BaseResponseListSpace_ = {
    code?: number
    data?: Space[]
    message?: string
  }

  type BaseResponseListSpaceCategoryAnalyzeResponse_ = {
    code?: number
    data?: SpaceCategoryAnalyzeResponse[]
    message?: string
  }

  type BaseResponseListSpaceLevel_ = {
    code?: number
    data?: SpaceLevel[]
    message?: string
  }

  type BaseResponseListSpaceSizeAnalyzeResponse_ = {
    code?: number
    data?: SpaceSizeAnalyzeResponse[]
    message?: string
  }

  type BaseResponseListSpaceTagAnalyzeResponse_ = {
    code?: number
    data?: SpaceTagAnalyzeResponse[]
    message?: string
  }

  type BaseResponseListSpaceUserAnalyzeResponse_ = {
    code?: number
    data?: SpaceUserAnalyzeResponse[]
    message?: string
  }

  type BaseResponseListSpaceUserVO_ = {
    code?: number
    data?: SpaceUserVO[]
    message?: string
  }

  type BaseResponseLoginUserVO_ = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponseLong_ = {
    code?: number
    data?: number
    message?: string
  }

  type BaseResponsePagePicture_ = {
    code?: number
    data?: PagePicture_
    message?: string
  }

  type BaseResponsePagePictureVO_ = {
    code?: number
    data?: PagePictureVO_
    message?: string
  }

  type BaseResponsePageSpace_ = {
    code?: number
    data?: PageSpace_
    message?: string
  }

  type BaseResponsePageSpaceVO_ = {
    code?: number
    data?: PageSpaceVO_
    message?: string
  }

  type BaseResponsePageUserVO_ = {
    code?: number
    data?: PageUserVO_
    message?: string
  }

  type BaseResponsePicture_ = {
    code?: number
    data?: Picture
    message?: string
  }

  type BaseResponsePictureTagCategory_ = {
    code?: number
    data?: PictureTagCategory
    message?: string
  }

  type BaseResponsePictureVO_ = {
    code?: number
    data?: PictureVO
    message?: string
  }

  type BaseResponseSpace_ = {
    code?: number
    data?: Space
    message?: string
  }

  type BaseResponseSpaceUsageAnalyzeResponse_ = {
    code?: number
    data?: SpaceUsageAnalyzeResponse
    message?: string
  }

  type BaseResponseSpaceUser_ = {
    code?: number
    data?: SpaceUser
    message?: string
  }

  type BaseResponseSpaceVO_ = {
    code?: number
    data?: SpaceVO
    message?: string
  }

  type BaseResponseString_ = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseUser_ = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO_ = {
    code?: number
    data?: UserVO
    message?: string
  }

  type CreateOutPaintingTaskResponse = {
    code?: string
    message?: string
    output?: Output
    requestId?: string
  }

  type DeleteRequest = {
    id?: number
  }

  type GetOutPaintingTaskResponse = {
    output?: Output1
    requestId?: string
  }

  type getPictureByIdUsingGETParams = {
    /** id */
    id?: number
  }

  type getPictureOutPaintingTaskUsingGETParams = {
    /** taskId */
    taskId?: string
  }

  type getPictureVOByIdUsingGETParams = {
    /** id */
    id?: number
  }

  type getSpaceByIdUsingGETParams = {
    /** id */
    id?: number
  }

  type getSpaceVOByIdUsingGETParams = {
    /** id */
    id?: number
  }

  type getUserByIdUsingGETParams = {
    /** id */
    id?: number
  }

  type getUserVOByIdUsingGETParams = {
    /** id */
    id?: number
  }

  type ImageSearchResult = {
    /** 来源地址 */
    fromUrl?: string
    /** 缩略图地址 */
    thumbUrl?: string
  }

  type LoginUserVO = {
    createTime?: string
    id?: number
    updateTime?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }

  type Output = {
    taskId?: string
    taskStatus?: string
  }

  type Output1 = {
    code?: string
    endTime?: string
    message?: string
    outputImageUrl?: string
    scheduledTime?: string
    submitTime?: string
    taskId?: string
    taskMetrics?: TaskMetrics
    taskStatus?: string
  }

  type PagePicture_ = {
    current?: number
    pages?: number
    records?: Picture[]
    size?: number
    total?: number
  }

  type PagePictureVO_ = {
    current?: number
    pages?: number
    records?: PictureVO[]
    size?: number
    total?: number
  }

  type PageSpace_ = {
    current?: number
    pages?: number
    records?: Space[]
    size?: number
    total?: number
  }

  type PageSpaceVO_ = {
    current?: number
    pages?: number
    records?: SpaceVO[]
    size?: number
    total?: number
  }

  type PageUserVO_ = {
    current?: number
    pages?: number
    records?: UserVO[]
    size?: number
    total?: number
  }

  type Parameters = {
    addWatermark?: boolean
    bestQuality?: boolean
    bottomScale?: number
    leftScale?: number
    limitImageSize?: boolean
    n?: number
    outputRatio?: string
    rightScale?: number
    seed?: number
    topScale?: number
  }

  type Picture = {
    category?: string
    createTime?: string
    editTime?: string
    id?: number
    introduction?: string
    isDelete?: number
    name?: string
    picColor?: string
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: number
    picWidth?: number
    reviewMessage?: string
    reviewStatus?: number
    reviewTime?: string
    reviewerId?: number
    spaceId?: number
    tags?: string
    thumbnailUrl?: string
    updateTime?: string
    url?: string
    userId?: number
  }

  type PictureEditByBatchRequest = {
    /** 分类 */
    category?: string
    /** 命名规则 */
    nameRule?: string
    /** 图片ID列表 */
    pictureIdList: number[]
    /** 空间ID */
    spaceId: number
    /** 标签列表 */
    tags?: string[]
  }

  type PictureEditRequest = {
    /** 图片分类 */
    category?: string
    /** 图片ID */
    id?: number
    /** 图片简介 */
    introduction?: string
    /** 图片名称 */
    name?: string
    /** 图片标签列表 */
    tags?: string[]
  }

  type PictureQueryRequest = {
    /** 图片分类 */
    category?: string
    current?: number
    /** 编辑时间结束值 */
    endEditTime?: string
    /** 图片ID */
    id?: number
    /** 图片简介 */
    introduction?: string
    /** 图片名称 */
    name?: string
    /** 是否只查询 spaceId 为 null 的数据 */
    nullSpaceId?: boolean
    pageSize?: number
    /** 图片格式（如jpg、png等） */
    picFormat?: string
    /** 图片高度（像素） */
    picHeight?: number
    /** 图片宽高比 */
    picScale?: number
    /** 文件大小（字节） */
    picSize?: number
    /** 图片宽度（像素） */
    picWidth?: number
    /** 审核反馈信息 */
    reviewMessage?: string
    /** 审核状态：0-待审核; 1-通过; 2-拒绝 */
    reviewStatus?: number
    /** 审核人员ID */
    reviewerId?: number
    /** 搜索关键词（支持名称和简介模糊搜索） */
    searchText?: string
    sortField?: string
    sortOrder?: string
    /** 空间ID */
    spaceId?: number
    /** 编辑时间起始值 */
    startEditTime?: string
    /** 图片标签列表 */
    tags?: string[]
    /** 上传用户ID */
    userId?: number
  }

  type PictureReviewRequest = {
    /** 图片ID */
    id?: number
    /** 审核信息 */
    reviewMessage?: string
    /** 审核状态：0-待审核, 1-通过, 2-拒绝 */
    reviewStatus?: number
  }

  type PictureTagCategory = {
    categoryList?: string[]
    tagList?: string[]
  }

  type PictureUploadByBatchRequest = {
    /** 抓取数量 */
    count?: number
    /** 名称前缀 */
    namePrefix?: string
    /** 搜索词 */
    searchText?: string
  }

  type PictureUploadRequest = {
    /** 文件地址 */
    fileUrl?: string
    /** 图片ID */
    id?: number
    /** 图片名称 */
    picName?: string
    /** 空间ID */
    spaceId?: number
  }

  type PictureVO = {
    category?: string
    createTime?: string
    editTime?: string
    id?: number
    introduction?: string
    name?: string
    permissionList?: string[]
    picColor?: string
    picFormat?: string
    picHeight?: number
    picScale?: number
    picSize?: number
    picWidth?: number
    spaceId?: number
    tags?: string[]
    thumbnailUrl?: string
    updateTime?: string
    url?: string
    user?: UserVO
    userId?: number
  }

  type Pinyin__ = {
    /** 图片分类 */
    category?: string
    /** 图片ID，用于唯一标识一张图片 */
    id?: number
    /** 图片简介 */
    introduction?: string
    /** 图片名称 */
    name?: string
    /** 图片标签列表 */
    tags?: string[]
  }

  type Pinyin_2 = {
    current?: number
    /** id */
    id?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    /** 空间级别：0-普通版 1-专业版 2-旗舰版 */
    spaceLevel?: number
    /** 空间名称 */
    spaceName?: string
    spaceType?: number
    /** 用户 id */
    userId?: number
  }

  type SearchPictureByColorRequest = {
    /** 图片颜色 */
    picColor?: string
    /** 空间ID */
    spaceId?: number
  }

  type SearchPictureByPictureRequest = {
    /** 图片id */
    pictureId?: number
  }

  type Space = {
    createTime?: string
    editTime?: string
    id?: number
    isDelete?: number
    maxCount?: number
    maxSize?: number
    spaceLevel?: number
    spaceName?: string
    spaceType?: number
    totalCount?: number
    totalSize?: number
    updateTime?: string
    userId?: number
  }

  type SpaceAddRequest = {
    /** 空间等级 */
    spaceLevel?: number
    /** 空间名称 */
    spaceName?: string
    /** 空间类型 */
    spaceType?: number
  }

  type SpaceCategoryAnalyzeRequest = {
    /** 是否查询所有数据 */
    queryAll?: boolean
    /** 是否查询公开空间 */
    queryPublic?: boolean
    /** 空间ID */
    spaceId?: number
  }

  type SpaceCategoryAnalyzeResponse = {
    category?: string
    count?: number
    totalSize?: number
  }

  type SpaceEditRequest = {
    /** 空间ID */
    id: number
    /** 空间名称 */
    spaceName: string
  }

  type SpaceLevel = {
    /** 该级别下最大文件数量限制 */
    maxCount?: number
    /** 该级别下最大存储空间大小（单位：字节 */
    maxSize?: number
    /** 空间级别的描述文本 */
    text?: string
    /** 空间级别的值 */
    value?: number
  }

  type SpaceRankAnalyzeRequest = {
    topN?: number
  }

  type SpaceSizeAnalyzeRequest = {
    /** 是否查询所有数据 */
    queryAll?: boolean
    /** 是否查询公开空间 */
    queryPublic?: boolean
    /** 空间ID */
    spaceId?: number
  }

  type SpaceSizeAnalyzeResponse = {
    count?: number
    sizeRange?: string
  }

  type SpaceTagAnalyzeRequest = {
    /** 是否查询所有数据 */
    queryAll?: boolean
    /** 是否查询公开空间 */
    queryPublic?: boolean
    /** 空间ID */
    spaceId?: number
  }

  type SpaceTagAnalyzeResponse = {
    count?: number
    tag?: string
  }

  type SpaceUpdateRequest = {
    /** 空间ID */
    id: number
    /** 空间图片的最大数量 */
    maxCount: number
    /** 空间图片的最大总大小（单位：字节） */
    maxSize: number
    /** 空间级别：0-普通版 1-专业版 2-旗舰版 */
    spaceLevel: number
    /** 空间名称 */
    spaceName: string
  }

  type SpaceUsageAnalyzeRequest = {
    /** 是否查询所有数据 */
    queryAll?: boolean
    /** 是否查询公开空间 */
    queryPublic?: boolean
    /** 空间ID */
    spaceId?: number
  }

  type SpaceUsageAnalyzeResponse = {
    /** 文件数量使用比例 */
    countUsageRatio?: number
    /** 用户最大允许的文件数量 */
    maxCount?: number
    /** 用户最大允许的存储空间大小（字节） */
    maxSize?: number
    /** 存储空间使用比例 */
    sizeUsageRatio?: number
    /** 用户已使用的文件数量 */
    usedCount?: number
    /** 用户已使用的存储空间大小（字节） */
    userSize?: number
  }

  type SpaceUser = {
    createTime?: string
    id?: number
    spaceId?: number
    spaceRole?: string
    updateTime?: string
    userId?: number
  }

  type SpaceUserAddRequest = {
    spaceId?: number
    spaceRole?: string
    userId?: number
  }

  type SpaceUserAnalyzeRequest = {
    /** 是否查询所有数据 */
    queryAll?: boolean
    /** 是否查询公开空间 */
    queryPublic?: boolean
    /** 空间ID */
    spaceId?: number
    timeDimension?: string
    userId?: number
  }

  type SpaceUserAnalyzeResponse = {
    count?: number
    period?: string
  }

  type SpaceUserEditRequest = {
    id?: number
    spaceRole?: string
  }

  type SpaceUserQueryRequest = {
    id?: number
    spaceId?: number
    spaceRole?: string
    userId?: number
  }

  type SpaceUserVO = {
    createTime?: string
    id?: number
    space?: SpaceVO
    spaceId?: number
    spaceRole?: string
    updateTime?: string
    user?: UserVO
    userId?: number
  }

  type SpaceVO = {
    createTime?: string
    editTime?: string
    id?: number
    maxCount?: number
    maxSize?: number
    permissionList?: string[]
    spaceLevel?: number
    spaceName?: string
    spaceType?: number
    totalCount?: number
    totalSize?: number
    updateTime?: string
    user?: UserVO
    userId?: number
  }

  type TaskMetrics = {
    failed?: number
    succeeded?: number
    total?: number
  }

  type testDownloadFileUsingGETParams = {
    /** filepath */
    filepath?: string
  }

  type uploadPictureUsingPOSTParams = {
    /** 文件地址 */
    fileUrl?: string
    /** 图片ID */
    id?: number
    /** 图片名称 */
    picName?: string
    /** 空间ID */
    spaceId?: number
  }

  type User = {
    createTime?: string
    editTime?: string
    id?: number
    isDelete?: number
    updateTime?: string
    userAccount?: string
    userAvatar?: string
    userName?: string
    userPassword?: string
    userProfile?: string
    userRole?: string
  }

  type UserAddRequest = {
    /** 账号 */
    userAccount?: string
    /** 用户头像 */
    userAvatar?: string
    /** 用户昵称 */
    userName?: string
    /** 用户简介 */
    userProfile?: string
    /** 用户角色: user, admin */
    userRole?: string
  }

  type UserLoginRequest = {
    /** 账号 */
    userAccount?: string
    /** 密码 */
    userPassword?: string
  }

  type UserQueryRequest = {
    current?: number
    /** 用户ID */
    id?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    /** 用户账号 */
    userAccount?: string
    /** 用户昵称 */
    userName?: string
    /** 用户简介 */
    userProfile?: string
    /** 用户角色：user/admin/ban */
    userRole?: string
  }

  type UserRegisterRequest = {
    /** 确认密码 */
    checkPassword?: string
    /** 账号 */
    userAccount?: string
    /** 密码 */
    userPassword?: string
  }

  type UserUpdateRequest = {
    /** 用户ID */
    id?: number
    /** 用户头像 */
    userAvatar?: string
    /** 用户昵称 */
    userName?: string
    /** 用户简介 */
    userProfile?: string
    /** 用户角色：user/admin */
    userRole?: string
  }

  type UserVO = {
    createTime?: string
    id?: number
    userAccount?: string
    userAvatar?: string
    userName?: string
    userProfile?: string
    userRole?: string
  }
}
