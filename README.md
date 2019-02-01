<center><h4>中间件应用自升级UI库使用说明手册模板（README.md文档）</h4></center> 



# 关于
应用自升级UI库，基于bfcVersion，封装一层UI界面，提供给应用自升级的界面展示。

## 特性
说明库的突出的特性，比如稳定、高效、快捷

## 项目名称及版本
- 发布版本： 4.0.0
- GitLab项目名： BfcVersionUI
- 库名称：bfc-version-update-ui 	
- 需要Android API >= 15

## 升级清单文档

- 请参考[升级清单文档](./UPDATE.md)

## 功能列表
- 支持检测新版本自动下载弹窗更新
- 支持24小时，120小时不再提醒
- 支持当前版本不再提醒
- 支持强制升级（后台挂网需产品确认，不得随意采用强制升级方式）

## Demo效果图
- 效果图

![自升级.png](http://upload-images.jianshu.io/upload_images/3078916-308072d6f2707212.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
- Demo获取地址  http://172.28.2.93/bfc/BfcVersionUI.git


# 使用

## 配置
  * Gradle单独引用    
添加私有maven配置

#本地库URL
 
```groovy

    compile bfcBuildConfig.deps.'bfc-version-ui'
    
```

>添加依赖前,请配置私有maven库的地址

>如果依赖使用的网络配置, 请参考网络配置使用; 添加compile bfcBuildConfig.deps.'bfc-version-ui'

## 初始化
在Application oncreate中初始化，添加下载初始化代码：

```groovy
   DownloadController.init(this.getApplicationContext());
```

默认初始化BfcVersionManager：
```groovy
     mBfcVersionManager = new BfcVersionManager.Builder().build(this);
```

自定义初始化BfcVersionManager：
```groovy
    mBfcVersionManager = new BfcVersionManager.Builder()
        .setIsAutoUpdate(true)            //设置自动/手动检测更新模式
        .setIsDebug(false)                //测试模式，提供更多log
        .setIUrl(new UrlReleaseImpl())    //使用UrlTestImpl可切换到测试服务器
        .build(this);
```

> 旧版本初始化方式mBfcVersionManager = new BfcVersionManager(this)已弃用，请及时修改

## 核心功能使用说明

- 自升级检测 

```groovy
   mBfcVersionManager.onVersionCheck();
```
>1.检测应用是否有新版本，若服务器存在新版本时，后台自动下载。下载完成后，再次调用mBfcVersionManager.onVersionCheck()弹窗提示更新。<br>
2.手动检测更新时有新版本会立即弹窗，忽略时间规则。<br>
3.根据后台返回信息自动判断是否为强制升级还是普通升级流程。

<br>
详情可查看：<br>
[【应用升级】交互文档](http://172.28.2.93/bfc/BfcVersionUI/blob/dev_hsn/doc/%E3%80%90%E5%BA%94%E7%94%A8%E5%8D%87%E7%BA%A7%E3%80%91PRD%E4%BA%A4%E4%BA%92%E6%96%87%E6%A1%A3-V1.0.0.rp)<br>
[【应用强制升级】交互文档](http://172.28.2.93/bfc/BfcVersionUI/blob/dev_hsn/doc/%E3%80%90%E5%BA%94%E7%94%A8%E5%BC%BA%E5%88%B6%E5%8D%87%E7%BA%A7%E3%80%91PRD%E4%BA%A4%E4%BA%92%E6%96%87%E6%A1%A3-V1.0.0(%E7%BB%88%E7%A8%BF).rp)<br>

- 销毁

```groovy
   mBfcVersionManager.destory();
```
>请在不需要或者退出app调用此方法。可以暂停当前下载任务。

## 常用接口（请在需要时监听）

- 检测到新版本的监听

```groovy
    mBfcVersionManager.setVersionUIListener(new BfcVersionManager.OnNewVersionChecked() {
        @Override
        public void hasNewVersion(String versionCode) {
          //新版本版本号信息回调，可用来显示有新版本可用
        }  
        @Override
        public void onExitApp() {
           //强制升级退出接口，请应用自己完成退出逻辑
        }
    });
```

- 更新对话框按钮点击监听（可用于埋点）

```groovy
    mBfcVersionManager.setBfcVersionDialogListener(new BfcVersionDialogListener() {
        @Override
        public void onPositiveClick() {
            //确认
        }
        @Override
        public void onNegativeClick() {
            //取消或退出
            }
    });
```

- 下载流程监听（可用于通知栏消息弹出）

```groovy
    mBfcVersionManager.setBfcNotificationCallBack(new BfcNotificationCallBack() {
        @Override
        public void beginDownload() {
            //开始下载
        }
        @Override
        public void downloadFailed() {
            //下载失败
        }
    });
```

- 更新状态监听

```groovy
    mBfcVersionManager.setBfcVersionStateListener(new BfcVersionStateListener() {
        @Override
        public void onVersionState(int state) {

        }
    });
```

>state:<br>
1.BfcVersionStateListener.STATE_SILENT_READY:准备强制升级。可以在此做强制升级前预处理。

- 修改为手动升级方式（可用于通知栏消息弹出）

```groovy
     mBfcVersionManager.setAutoUpdate(false);
```


- 是否正在下载apk

```groovy
    mBfcVersionManager.isDownloading()
```

- 获取所有版本更新的下载任务

```groovy
    mBfcVersionManager.getAllDownloadTask()
```

# 常见问题
- Q：如何自己进行自升级测试？

  A：目前不支持自己上传测试升级APP，如果需要自己测试，请RTX联系葛晓丽，发送机器序列号和高版本的应用，可进行测试。（机器序列号请找测试烧录）

- Q：手动更新怎么接入？

  A：请在初始化时setIsAutoUpdate(false)或调用mBfcVersionManager.setAutoUpdate(false)后，再次调用检测功能。
  
  - Q：为什么首次进入不弹窗？

  A：请确认是否已经下载完成，且需要再次进入才会弹出提醒。

# 强制升级配置
在学淘应用挂网管理界面中，找到要发布的应用，点击`版本控制`，会显示如下选择：

![](http://172.28.2.93/bfc/BfcVersionUI/raw/dev_hsn/doc/res/silent.png)

选择要强制升级的版本添加到右侧`强制升级版本信息`框中，保存则完成强制升级配置。后面和平常发布流程一样。

需要注意强制升级的版本`需要是最新版本`。

如果出现`左侧列表中无版本信息`，请反馈给 ***吴言九*** 处理，如下图：

![](http://172.28.2.93/bfc/BfcVersionUI/raw/dev_hsn/doc/res/silent2.png)

# 源码保存地址
GitLab源码地址:  http://172.28.2.93/bfc/BfcVersionUI.git


# 问题、意见反馈
使用过程成遇到任何问题，有任何建议或者意见都可以使用下面这个地址反馈给我们。欢迎大家提出问题，我们将会在最短的时间内解决问题。
**地址：http://172.28.2.93/bfc/BfcVersionUI/issues**

# 最后
希望大家多多使用和提出宝贵意见，大家一起讨论进步，一起完善本库。
联系方式 ： 18030042129  RTX：  黄邵雄
参与开发人员： 黄邵雄

---