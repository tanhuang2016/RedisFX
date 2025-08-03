# RedisFX

- 这是一个基于Jedis，使用JavaFX开发的Redis GUI工具，并提供了Linux和Windows的包，已内置jre环境，解压即可运行。
- 如果您的电脑已经安装了JDK1.8以上（需要JavaFX包），也可以直接运行jar包启动。
- [下载地址](https://github.com/tanhuang2016/RedisFX/releases)

## 语言
[English](README.en-US.md)  | 中文

## 特性

1. 可同时管理多个redis连接
2. 只支持单机redis的连接
3. 支持string list hash set zset类型
4. 支持控制台方式使用命令交互
5. string类型支持字符编码可选
6. string类型支持图片文件的展示

## 规划

1. 首选项支持国际化的切换
2. 首选项支持字体的切换
3. 添加loading动画
4. string类型的二进制数据支持导入导出
5. 对key的备份与还原
6. 对集群和哨兵的支持
7. 对SSH、SSL的支持

## 开发环境

- 开发环境为JDK1.8，基于Maven3.8.8构建
- 使用Eclipase或Intellij Idea开发
- 本项目只依赖的[Jedis4.3.0](https://github.com/redis/jedis)
- 使用[JavaPackager](https://github.com/fvarrui/JavaPackager)插件进行打包操作(可打包windows、Linux、Mac安装包)
### 项目结构

```text
RedisFX
├─rdm-common 公共模块，定义通用工具，线程池等
├─rdm-redis redis服务模块，对ui需要的接口进行约束
├─rdm-redis-imp redis 服务实现模块，具体客户端接口的实现
└─rdm-ui 整个工具的gui模块
  └─src
   ├─main
   │  ├─java
   │  │  └─xyz
   │  │   └─hashdog
   │  │     ─rdm
   │  │      └─ui 
   │  │       ├─common 公共封装
   │  │       ├─controller view的控制层
   │  │       ├─entity 实体类bean
   │  │       ├─exceptions ui异常
   │  │       ├─handler 封装处理器
   │  │       └─util ui相关的工具
   │  └─resources 
   │     ├─css ui相关样式表
   │     ├─fxml 视图对应的fxml文件
   │     ├─i18n 国际化
   │     └─icon 图标
```

## 展示

![create.gif](doc%2Fimage%2Fcreate.gif)
![string.gif](doc%2Fimage%2Fstring.gif)
![other.gif](doc%2Fimage%2Fother.gif)

## 感谢支持

[Jetbrains](https://www.jetbrains.com/)
![https://www.jetbrains.com/](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg?_gl=1*389d01*_ga*MTcyNjk5OTM4MC4xNjg3NzkwNTEy*_ga_9J976DJZ68*MTcwNDk2NjczNi4xMC4xLjE3MDQ5NjY4OTUuMC4wLjA.&_ga=2.205748112.2037968390.1704966739-1726999380.1687790512)