# 角色权限系统使用说明

## 🎯 已实现的功能

### 1. 五种用户角色

| 角色代码 | 角色名称 | 描述 | 权限 |
|---------|---------|------|------|
| `SUPER_ADMIN` | 超级管理员 | 系统超级管理员 | 拥有所有权限 |
| `HOST` | 主持人 | 比赛主持人 | 控制比赛流程 |
| `JUDGE` | 评委 | 比赛评委 | 评判主观题 |
| `PLAYER` | 选手 | 参赛选手 | 参加比赛答题 |
| `VIEWER` | 观众 | 观看比赛 | 仅观看 |

### 2. 不同角色的菜单和页面

#### 超级管理员 (SUPER_ADMIN)
**侧边栏菜单**：
- 工作台 (`/dashboard`)
- 用户管理 (`/users`)
- 题库管理 (`/questions`)
- 比赛管理 (`/sessions`)
- 系统设置 (`/settings`)

**默认首页**：`/dashboard`

#### 主持人 (HOST)
**侧边栏菜单**：
- 我的比赛 (`/host/sessions`)
- 系统设置 (`/settings`)

**可访问页面**：
- `/host/sessions` - 比赛列表
- `/host/control/:id` - 比赛控制台
- `/settings` - 设置

**默认首页**：`/host/sessions`

#### 选手 (PLAYER)
**侧边栏菜单**：
- 我的比赛 (`/player/sessions`)
- 系统设置 (`/settings`)

**可访问页面**：
- `/player/sessions` - 可参加的比赛列表
- `/player/compete/:id` - 比赛答题页面（待实现）
- `/settings` - 设置

**默认首页**：`/player/sessions`

#### 评委 (JUDGE)
**侧边栏菜单**：
- 我的比赛 (`/judge/sessions`)
- 系统设置 (`/settings`)

**默认首页**：`/judge/sessions`

#### 观众 (VIEWER)
**侧边栏菜单**：
- 观看比赛 (`/viewer/sessions`)

**默认首页**：`/viewer/sessions`

## 🔧 如何测试角色系统

### 方法1：使用角色切换器（开发测试）

1. 访问系统设置页面：`http://localhost:3000/settings`
2. 在页面顶部可以看到"角色切换 (开发测试)"卡片
3. 点击不同的角色按钮，系统会自动切换角色并刷新页面
4. 观察侧边栏菜单和可访问页面的变化

### 方法2：模拟不同角色登录

在 `src/stores/user.ts` 的登录方法中，手动修改返回的角色：

```typescript
async login(data: LoginRequest) {
  const res = await apiLogin(data)
  const { token, ...userInfo } = res.data
  
  // 模拟不同角色（仅用于测试）
  userInfo.roles = ['HOST'] // 改为 HOST, PLAYER, JUDGE, VIEWER 等
  
  this.token = token
  this.userInfo = userInfo
  
  localStorage.setItem('token', token)
  
  return res
}
```

## 📋 权限配置文件

角色权限配置在 `src/config/roles.ts` 中定义，包括：

- `ROLE_CONFIG` - 每个角色的详细配置
- `hasPermission()` - 检查用户是否有指定权限
- `canAccessRoute()` - 检查用户是否可以访问指定路由
- `getDefaultRoute()` - 获取用户的默认首页

## 🔐 路由守卫

路由守卫在 `src/router/index.ts` 中实现，会：

1. 检查用户是否登录
2. 检查用户角色
3. 验证路由访问权限
4. 没有权限时跳转到默认页面

## ✅ 已实现的页面

### 主持人专属页面

1. **比赛列表** (`src/views/host/sessions.vue`)
   - 显示所有比赛
   - 进行中的比赛可以进入控制台
   - 可以打开大屏展示

2. **比赛控制台** (`src/views/host/control.vue`)
   - 复用 `sessions/control.vue` 的所有功能
   - 返回按钮跳转到 `/host/sessions`

### 选手专属页面

1. **比赛列表** (`src/views/player/sessions.vue`)
   - 只显示待开始、进行中、暂停的比赛
   - 进行中的比赛可以进入答题
   - 未开始的比赛显示"未开始"按钮

## 🎨 UI 特性

1. **用户信息显示**
   - 右上角显示用户名和角色
   - 角色标签：管理员、主持人、评委、选手、观众

2. **动态菜单**
   - 根据角色显示不同的侧边栏菜单
   - 管理员看到完整菜单
   - 其他角色只看到授权的菜单项

3. **路由保护**
   - 访问未授权页面自动跳转到默认首页
   - 登录后根据角色跳转到对应的默认页面

## 🚀 下一步开发

### 待实现的页面

1. **选手答题页面** (`/player/compete/:id`)
   - 实时答题界面
   - 抢答功能
   - 倒计时显示

2. **评委评分页面** (`/judge/review/:id`)
   - 查看主观题答案
   - 打分和评价
   - 提交评分

3. **观众观看页面** (`/viewer/watch/:id`)
   - 实时查看比赛进度
   - 查看排行榜
   - 不能操作

## 💡 使用建议

1. **开发阶段**：使用角色切换器快速测试不同角色的界面
2. **测试阶段**：创建不同角色的测试账号进行集成测试
3. **生产环境**：移除角色切换器组件，角色由后端API返回

## 🔗 相关文件

- `src/config/roles.ts` - 角色权限配置
- `src/router/index.ts` - 路由配置和守卫
- `src/stores/user.ts` - 用户状态管理
- `src/layouts/AdminLayout.vue` - 主布局（动态菜单）
- `src/components/RoleSwitcher.vue` - 角色切换器
- `src/views/host/sessions.vue` - 主持人比赛列表
- `src/views/player/sessions.vue` - 选手比赛列表
