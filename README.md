# VanGogh

适用 Android 开发，用于加载或缓存图片，接口设计参考了 [okhttp](https://github.com/square/okhttp) 和 [picasso](https://github.com/square/picasso)，V2 版大量~~借鉴~~（抄袭）了 [picasso](https://github.com/square/picasso) 的设计，本项目是根据个人需求而定制的，作为学习练习之用，如无特殊需求建议使用 [picasso](https://github.com/square/picasso).

## 1. 特性

* 支持网络图片、本地文件以及 Android 资源文件加载。
* 支持内存、磁盘两级缓存，自动管理缓存大小。
* 支持“暂停/恢复”加载，可与 RecyclerView 和 ListView 等配合使用实现滚动时暂停加载。
* 支持缓存策略配置，可自定义只加载内存、只加载本地、只加载网络或多种策略组合使用，默认任何来源。
* 支持任务加载策略配置。
* 支持自定义图片变形处理等。
* 支持根据图片来源为图片添加水印，便于开发状态观察图片来源，对应关系：内存——绿色，磁盘——蓝色，网络——红色，此功能默认关闭。
* 支持正在加载及加载失败的占位图。

## 2. 用法举例

```java
VanGogh.with(getContext()) //如已设置单例或调用过 VanGogh.with(getContext()) 也可使用 VanGogh.get()
    .load("http://www.xxx.xx/test.png")
    .into(imageView);
```

## 3. 使用方法

(1) 在项目的 build.gradle 中配置仓库地址：

```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

(2) 添加项目依赖：

```groovy
	dependencies {
	        implementation 'com.github.ccolorcat:VanGogh:v2.0.0'
	}
```

## 4. 版本历史

v2.0.0

> 改变了原有的设计，新版本大量~~借鉴~~（抄袭）了 [picasso](https://github.com/square/picasso) 的设计。

v1.1.1

> 1. 新增 content 资源支持，即 Uri.getScheme() 为 content 的内容。
> 2. 新增任务加载策略配置，见 VanGogh.Builder#taskPolicy(boolean mostRecentFirst).
> 3. 新增清除最大尺寸限制（可用于清除全局配置的最大尺寸限制）的方法。
> 4. 新增清除 Transformation 的方法。
> 5. 从内存缓存中获取的图片不再进行 resize 操作。