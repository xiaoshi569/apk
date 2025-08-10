# GitHub Actions自动编译V免签APK指南

## 🎯 概述
使用GitHub Actions可以让您无需安装任何开发工具，直接在云端编译APK文件。

## 📋 准备工作清单

### ✅ 需要准备的文件
确保您的项目包含以下文件：
- [ ] `app/src/main/java/com/vone/vmq/MainActivity.java`
- [ ] `app/src/main/java/com/vone/vmq/NeNotificationService2.java`
- [ ] `app/src/main/java/com/vone/vmq/LogViewActivity.java`
- [ ] `app/src/main/res/layout/activity_main.xml`
- [ ] `app/src/main/res/layout/activity_log_view.xml`
- [ ] `app/build.gradle`
- [ ] `build.gradle`
- [ ] `gradle/wrapper/gradle-wrapper.properties`
- [ ] `gradlew` 和 `gradlew.bat`
- [ ] `.github/workflows/build.yml`

## 🚀 详细步骤

### 第一步：创建GitHub账号和仓库

1. **注册GitHub账号**
   - 访问 https://github.com
   - 点击 "Sign up" 注册账号
   - 验证邮箱

2. **创建新仓库**
   - 登录后点击右上角 "+" → "New repository"
   - Repository name: `vmq-android`
   - Description: `V免签Android监控端 - 支持应用内日志查看`
   - 选择 "Public"
   - 点击 "Create repository"

### 第二步：上传项目文件

#### 方法A：网页上传（推荐新手）

1. **准备文件**
   - 将所有项目文件放在一个文件夹中
   - 确保包含 `.github` 文件夹和 `workflows/build.yml`

2. **上传文件**
   - 在新建的仓库页面，点击 "uploading an existing file"
   - 选择所有项目文件（可以拖拽整个文件夹）
   - 等待上传完成

3. **提交更改**
   - Commit message: `Initial commit - V免签 with log viewer`
   - 点击 "Commit changes"

#### 方法B：使用GitHub Desktop

1. **下载GitHub Desktop**
   - 访问 https://desktop.github.com/
   - 下载并安装

2. **克隆仓库**
   - 打开GitHub Desktop
   - File → Clone repository
   - 选择您刚创建的仓库

3. **复制文件**
   - 将项目文件复制到克隆的本地文件夹
   - GitHub Desktop会自动检测更改

4. **提交并推送**
   - 填写提交信息
   - 点击 "Commit to main"
   - 点击 "Push origin"

### 第三步：触发自动编译

上传完成后，GitHub Actions会自动开始编译：

1. **查看编译状态**
   - 在仓库页面点击 "Actions" 标签
   - 您会看到 "Build V免签 APK" 工作流正在运行

2. **编译过程**
   ```
   ⏳ 正在运行... (黄色圆圈)
   ✅ 编译成功 (绿色对勾)
   ❌ 编译失败 (红色叉号)
   ```

3. **手动触发编译**（如果需要）
   - Actions → "Build V免签 APK"
   - 点击 "Run workflow" → "Run workflow"

### 第四步：下载APK文件

#### 从Actions下载
1. **进入Actions页面**
2. **点击最新的成功编译**（绿色✅）
3. **滚动到页面底部**，找到 "Artifacts"
4. **下载APK**：
   - `vmq-debug-apk` - 调试版本（推荐日常使用）
   - `vmq-release-apk` - 发布版本

#### 从Releases下载
1. **在仓库主页点击 "Releases"**
2. **下载最新版本**
3. **选择APK文件**：
   - `app-debug.apk` - 调试版本
   - `app-release-unsigned.apk` - 发布版本

## 🔧 编译配置说明

### 自动编译触发条件
- ✅ 推送代码到 main 分支
- ✅ 创建 Pull Request
- ✅ 手动触发

### 编译产物
- **Debug APK**: 包含调试信息，文件较大，适合测试
- **Release APK**: 优化版本，文件较小，适合发布

### 自动发布
- 当推送到 main 分支时，会自动创建 Release
- Release 包含编译好的APK文件
- 版本号格式：`v1.0-{编译次数}`

## 📱 APK安装说明

### 安装步骤
1. **下载APK文件**到手机
2. **允许安装未知来源应用**：
   - 设置 → 安全 → 未知来源 (开启)
   - 或在安装时选择"允许"
3. **点击APK文件**进行安装
4. **安装完成**后打开应用

### 新功能验证
安装后验证新功能：
- ✅ 主界面有"支付宝调试"按钮
- ✅ 主界面有"查看日志"按钮
- ✅ 点击"查看日志"能打开日志界面
- ✅ 进行支付宝收款测试，查看日志记录

## 🛠️ 故障排除

### 编译失败常见原因

1. **文件缺失**
   ```
   错误：找不到 build.gradle
   解决：确保上传了所有必要文件
   ```

2. **语法错误**
   ```
   错误：Java compilation failed
   解决：检查Java代码语法
   ```

3. **依赖问题**
   ```
   错误：Could not resolve dependencies
   解决：检查网络连接，重新触发编译
   ```

### 查看详细错误日志
1. **点击失败的编译任务**
2. **展开失败的步骤**
3. **查看错误信息**
4. **根据错误信息修复代码**

### 常见解决方案
- **重新触发编译**：Actions → Run workflow
- **检查文件完整性**：确保所有文件都已上传
- **更新代码**：修复语法错误后重新提交

## 📊 编译时间说明

- **首次编译**：10-15分钟（需要下载依赖）
- **后续编译**：5-8分钟（有缓存加速）
- **编译队列**：如果GitHub繁忙，可能需要等待

## 🎉 成功标志

编译成功后您会看到：
- ✅ Actions页面显示绿色对勾
- 📦 Artifacts中有APK文件
- 🏷️ Releases中有新版本
- 📱 APK可以正常安装和运行

## 💡 使用技巧

1. **定期备份**：将重要代码保存到本地
2. **版本管理**：每次重要更新都提交到GitHub
3. **测试验证**：下载APK后先在测试设备上验证
4. **日志查看**：使用新增的日志功能调试问题

现在您可以完全通过GitHub在线编译APK，无需安装任何开发工具！
