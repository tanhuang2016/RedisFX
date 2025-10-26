

<img src="doc/image/redis-log.svg" width="300" height="100" alt="Redis Logo">

# RedisFX
> 使用JavaFX开发的Redis GUI工具
[下载地址](https://github.com/tanhuang2016/RedisFX/releases)
<br>

[![LICENSE](https://img.shields.io/github/license/tanhuang2016/RedisFX)](LICENSE)
[![Release](https://img.shields.io/github/release/tanhuang2016/RedisFX.svg)](https://github.com/tanhuang2016/RedisFX/releases)
[![Download](https://img.shields.io/github/downloads/tanhuang2016/RedisFX/total.svg)](https://github.com/tanhuang2016/RedisFX/releases)
[![STARS](https://img.shields.io/github/stars/tanhuang2016/RedisFX)](https://github.com/tanhuang2016/RedisFX/)
[![GitHub forks](https://img.shields.io/github/forks/tanhuang2016/RedisFX)](https://github.com/tanhuang2016/RedisFX/fork)
[![GitHub issues](https://img.shields.io/github/issues/tanhuang2016/RedisFX)](https://github.com/tanhuang2016/RedisFX/issues)
## 语言
[English](README.en-US.md)  | 中文

## 特性
- ✅ SSH、SSL 协议支持
- ✅ Cluster、Sentinel 模式支持
- ✅ String、List、Hash、Set、Zset、JSON、Stream 类型支持
- ✅ 控制台、命令监控、发布订阅、信息报表
- ✅ 多主题、多语言、其他个性化配置
- ✅ 支持Windows(x86)、Linux(x86和arm)、MacOS(x86和arm)

## 还存在的问题
- ⚠️ 内存占用偏大
- ⚠️ 部分交互功能使用UI线程导致响应缓慢
- ⚠️ 其他待优化问题...

## 规划
- ⬜ ⭐⭐⭐ 代码重构、尝试ZGC优化内存占用、提升性能
- ⬜ ⭐⭐⭐ 交互优化、惰性加载设计
- ⬜ ⭐⭐ 构建跨平台执行包完善、尝试GraalVM原生镜像打包
- ⬜ ⭐⭐ 操作细节优化、完善现有功能设计
- ⬜ ⭐ 更多的个性化配置支持
- ⬜ ⭐ 为键值解析提供自定义插件扩展能力



## 开发环境
- [v1.x](https://github.com/tanhuang2016/RedisFX/tree/freeze/v1.0.4)使用JDK1.8原生JavaFX开发(已封版)
- [v2.x](https://github.com/tanhuang2016/RedisFX/tree/release-2.x)使用JDK21基于[v1.x](https://github.com/tanhuang2016/RedisFX/tree/freeze/v1.0.4)开发，提供了漂亮的UI库且功能更完善(持续更新中)

## 功能效果图展示
![String.png](doc/image/String.png)
![List.png](doc/image/List.png)
![Hash.png](doc/image/Hash.png)
![Set.png](doc/image/Set.png)
![Zset.png](doc/image/Zset.png)
![Json.png](doc/image/Json.png)
![Stream.png](doc/image/Stream.png)
![Report.png](doc/image/Report.png)
![Console.png](doc/image/Console.png)
![PubSub.png](doc/image/PubSub.png)
![Monitor.png](doc/image/Monitor.png)


## 感谢支持
都滑到最后了、谢谢大佬给个Star吧 🙏🙏🙏


## 贡献者  ✨

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/Tan000321"><img src="https://avatars.githubusercontent.com/u/115800442?v=4?s=100" width="100px;" alt="Tan000321"/><br /><sub><b>Tan000321</b></sub></a><br /><a href=" https://github.com/tanhuang2016/RedisFX/tanhuang2016/RedisFX/issues?q=author%3ATan000321" title="Bug reports">🐛</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/crazyweeds"><img src="https://avatars.githubusercontent.com/u/16688520?v=4?s=100" width="100px;" alt="crazyweeds"/><br /><sub><b>crazyweeds</b></sub></a><br /><a href=" https://github.com/tanhuang2016/RedisFX/tanhuang2016/RedisFX/issues?q=author%3Acrazyweeds" title="Bug reports">🐛</a></td>
    </tr>
  </tbody>
  <tfoot>
    <tr>
      <td align="center" size="13px" colspan="7">
        <img src="https://raw.githubusercontent.com/all-contributors/all-contributors-cli/1b8533af435da9854653492b1327a23a4dbd0a10/assets/logo-small.svg">
          <a href="https://all-contributors.js.org/docs/en/bot/usage">Add your contributions</a>
        </img>
      </td>
    </tr>
  </tfoot>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->