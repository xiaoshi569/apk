# V免签APK编译详细指南

## 🎯 编译前准备

### 1. 确认项目完整性
确保您的项目包含以下文件：
- `build.gradle` (根目录)
- `app/build.gradle`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradlew` 和 `gradlew.bat`
- 所有源代码文件

### 2. 系统要求
- **Java JDK 8或更高版本**
- **至少4GB内存**
- **10GB可用磁盘空间**

## 📱 方法一：Android Studio编译（推荐新手）

### 步骤1：安装Android Studio
1. 访问 https://developer.android.com/studio
2. 下载适合您系统的版本
3. 安装时选择"Standard"安装类型
4. 等待SDK组件下载完成

### 步骤2：导入项目
1. 打开Android Studio
2. 选择 "Open an existing Android Studio project"
3. 浏览到您的项目文件夹
4. 选择包含 `build.gradle` 的根目录
5. 点击 "OK"

### 步骤3：等待项目同步
```
首次导入会自动下载依赖，请耐心等待
底部状态栏会显示 "Gradle sync in progress..."
```

### 步骤4：解决可能的问题
如果出现错误：
1. **SDK版本问题**：
   - File → Project Structure → Project
   - 设置 Compile Sdk Version 为 26
   
2. **Build Tools问题**：
   - Tools → SDK Manager → SDK Tools
   - 安装 Android SDK Build-Tools 26.0.0

### 步骤5：编译APK
```
方法1：菜单编译
Build → Build Bundle(s) / APK(s) → Build APK(s)

方法2：Gradle面板
右侧 Gradle 面板 → app → Tasks → build → assembleDebug
```

### 步骤6：找到APK
编译成功后：
```
项目目录/app/build/outputs/apk/debug/app-debug.apk
```

## 💻 方法二：命令行编译（推荐有经验用户）

### Windows用户

#### 1. 检查Java环境
```cmd
java -version
javac -version
```
如果没有安装Java，下载安装JDK 8+

#### 2. 编译APK
```cmd
# 进入项目目录
cd /path/to/your/project

# 编译Debug版本
gradlew.bat assembleDebug

# 编译Release版本
gradlew.bat assembleRelease
```

### Mac/Linux用户

#### 1. 检查Java环境
```bash
java -version
javac -version
```

#### 2. 给gradlew执行权限
```bash
chmod +x gradlew
```

#### 3. 编译APK
```bash
# 编译Debug版本
./gradlew assembleDebug

# 编译Release版本
./gradlew assembleRelease
```

## 🔧 常见问题解决

### 问题1：Gradle下载失败
```bash
# 解决方案：使用国内镜像
# 编辑 gradle/wrapper/gradle-wrapper.properties
# 将distributionUrl改为：
distributionUrl=https\://mirrors.cloud.tencent.com/gradle/gradle-5.6.4-all.zip
```

### 问题2：依赖下载失败
```gradle
// 在 build.gradle 中添加国内仓库
allprojects {
    repositories {
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/jcenter' }
        google()
        jcenter()
    }
}
```

### 问题3：内存不足
```bash
# 在项目根目录创建 gradle.properties 文件
org.gradle.jvmargs=-Xmx2048m -XX:MaxPermSize=512m
org.gradle.parallel=true
org.gradle.daemon=true
```

### 问题4：编译版本不匹配
```gradle
// 确保 app/build.gradle 中版本一致
android {
    compileSdkVersion 26
    buildToolsVersion "26.0.0"
    defaultConfig {
        targetSdkVersion 26
    }
}
```

## 🚀 快速编译脚本

### Windows批处理脚本
创建 `build.bat`：
```batch
@echo off
echo 开始编译V免签APK...
echo.

echo 检查Java环境...
java -version
if errorlevel 1 (
    echo 错误：未找到Java环境，请先安装JDK
    pause
    exit /b 1
)

echo.
echo 开始编译...
gradlew.bat clean assembleDebug

if errorlevel 1 (
    echo 编译失败！
    pause
    exit /b 1
)

echo.
echo 编译成功！APK位置：
echo app\build\outputs\apk\debug\app-debug.apk
echo.
pause
```

### Mac/Linux脚本
创建 `build.sh`：
```bash
#!/bin/bash
echo "开始编译V免签APK..."

echo "检查Java环境..."
if ! command -v java &> /dev/null; then
    echo "错误：未找到Java环境，请先安装JDK"
    exit 1
fi

echo "给gradlew执行权限..."
chmod +x gradlew

echo "开始编译..."
./gradlew clean assembleDebug

if [ $? -eq 0 ]; then
    echo "编译成功！APK位置："
    echo "app/build/outputs/apk/debug/app-debug.apk"
else
    echo "编译失败！"
    exit 1
fi
```

## 📦 APK签名（可选）

### 生成签名文件
```bash
# 使用keytool生成keystore
keytool -genkey -v -keystore vmq-release-key.keystore -alias vmq -keyalg RSA -keysize 2048 -validity 10000
```

### 配置签名
在 `app/build.gradle` 中添加：
```gradle
android {
    signingConfigs {
        release {
            storeFile file('vmq-release-key.keystore')
            storePassword 'your_store_password'
            keyAlias 'vmq'
            keyPassword 'your_key_password'
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}
```

## ✅ 验证APK

### 检查APK信息
```bash
# 使用aapt查看APK信息
aapt dump badging app-debug.apk

# 检查APK大小（应该在5-10MB左右）
ls -lh app-debug.apk
```

### 安装测试
```bash
# 使用adb安装到手机
adb install app-debug.apk

# 或者直接将APK传输到手机安装
```

## 🎉 编译完成

编译成功后，您将得到：
- **app-debug.apk**：调试版本，可直接安装测试
- **app-release.apk**：发布版本（如果编译了release）

现在您可以将APK安装到手机上，测试新增的日志查看功能了！
