# VanGogh

适用 Android 开发，用于加载或缓存图片。

## 1. 特性

* 支持网络图片、本地文件以及 Android 资源文件加载。
* 支持内存、磁盘两级缓存，自动管理缓存大小。
* 支持“暂停/恢复”加载，可与 RecyclerView 和 ListView 等配合使用实现滚动时暂停加载。
* 支持加载策略，可自定义只加载内存、只加载本地、只加载网络或多种策略组合使用，默认任何来源。
* 支持自定义对图片变形处理等。
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
	        implementation 'com.github.ccolorcat:VanGogh:v1.0.0'
	}
```
