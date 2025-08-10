# 上传代码到GitHub的简单方法

## 方法一：使用GitHub网页界面（最简单）

### 首次上传
1. 访问您的GitHub仓库
2. 点击 "Add file" → "Upload files"
3. 拖拽整个项目文件夹到页面上
4. 等待上传完成
5. 填写提交信息："Add log viewer feature"
6. 点击 "Commit changes"

### 更新代码
1. 在仓库页面点击要更新的文件
2. 点击编辑按钮（铅笔图标）
3. 修改代码
4. 点击 "Commit changes"

## 方法二：使用GitHub Desktop（推荐）

### 安装GitHub Desktop
1. 下载：https://desktop.github.com/
2. 安装并登录GitHub账号

### 克隆仓库
1. 在GitHub Desktop中点击 "Clone a repository from the Internet"
2. 选择您的仓库
3. 选择本地文件夹

### 上传更新
1. 修改本地文件
2. 在GitHub Desktop中会自动显示更改
3. 填写提交信息
4. 点击 "Commit to main"
5. 点击 "Push origin"

## 方法三：使用Git命令行

### 首次设置
```bash
git config --global user.name "您的用户名"
git config --global user.email "您的邮箱"
```

### 克隆仓库
```bash
git clone https://github.com/您的用户名/vmq-android.git
cd vmq-android
```

### 上传更新
```bash
# 添加所有更改
git add .

# 提交更改
git commit -m "更新说明"

# 推送到GitHub
git push origin main
```

## 自动编译说明

每次您上传代码到GitHub后：
1. GitHub Actions会自动开始编译
2. 编译过程大约需要5-10分钟
3. 编译完成后可以下载APK文件

## 查看编译状态

1. 在仓库页面点击 "Actions"
2. 查看最新的编译任务
3. 绿色✅表示成功，红色❌表示失败
4. 点击任务可以查看详细日志

## 下载APK

### 从Actions下载
1. Actions → 选择成功的编译
2. 滚动到底部的 "Artifacts"
3. 下载 "vmq-debug-apk"

### 从Releases下载
1. 仓库主页 → "Releases"
2. 下载最新版本的APK

## 常见问题

### Q: 编译失败怎么办？
A: 点击失败的编译任务，查看错误日志，通常是代码语法错误

### Q: 如何更新单个文件？
A: 在GitHub网页上直接编辑文件，或使用GitHub Desktop

### Q: 编译需要多长时间？
A: 通常5-10分钟，首次编译可能需要更长时间

### Q: 可以编译多个版本吗？
A: 可以，每次推送代码都会生成新的编译版本
