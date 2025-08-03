# RedisFX

- This is a Jedis based, using JavaFX development of Redis GUI tool, and provides Linux and Windows packages, has a built-in jre environment, decompression can be run.
- If you already have JDK1.8 or more installed on your computer (which requires the JavaFX package), you can also start by running the jar package directly.
- [Download address](https://github.com/tanhuang2016/RedisFX/releases)

## Language
English |  [中文](README.zh-CN.md)

## Feature

1. Multiple redis connections can be managed simultaneously
2. Only single-node redis can be connected
3. The string list hash set zset type is supported
4. Support console mode to use command interaction
5. string Character encoding Optional
6. The string type supports image file display

## TODO

1. Preferences support i18n switching
2. Preferences support font switching
3. Add loading animation
4. Binary data of string type can be imported or exported
5. Backup and restore keys
6. Support for clusters and Sentinels
7. Support for SSH and SSL

## DevBuild

- The development environment is JDK1.8, built on Maven3.8.8
- Developed using Eclipase or Intellij IdeaDeveloped using Eclipase or Intellij Idea
- This project relies only on [Jedis4.3.0](https://github.com/redis/jedis)
- Use [JavaPackager](https://github.com/fvarrui/JavaPackager) plug-in to Packaging(which can be packaged Windows, Linux, Mac installation package)
### Structure

```text
RedisFX
├─rdm-common Common modules, defining common tools, thread pools, etc
├─rdm-redis Redis service module, which constrains the interface required by the ui
├─rdm-redis-imp Redis service implementation module, the implementation of specific client interface
└─rdm-ui Gui module for the entire tool
  └─src
   ├─main
   │  ├─java
   │  │  └─xyz
   │  │   └─hashdog
   │  │     ─rdm
   │  │      └─ui 
   │  │       ├─common Common encapsulation
   │  │       ├─controller View's controller
   │  │       ├─entity Bean
   │  │       ├─exceptions Ui exceptions
   │  │       ├─handler Package processor
   │  │       └─util Ui-related tools
   │  └─resources 
   │     ├─css Ui-related style sheets
   │     ├─fxml View corresponding fxml file
   │     ├─i18n internationalization
   │     └─icon icon
```

## Show

![create.gif](doc%2Fimage%2Fcreate.gif)
![string.gif](doc%2Fimage%2Fstring.gif)
![other.gif](doc%2Fimage%2Fother.gif)

## Supported by

[Jetbrains](https://www.jetbrains.com/)
![https://www.jetbrains.com/](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg?_gl=1*389d01*_ga*MTcyNjk5OTM4MC4xNjg3NzkwNTEy*_ga_9J976DJZ68*MTcwNDk2NjczNi4xMC4xLjE3MDQ5NjY4OTUuMC4wLjA.&_ga=2.205748112.2037968390.1704966739-1726999380.1687790512)