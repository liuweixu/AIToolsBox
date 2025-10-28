# 文章二 — Unity 开发流程常见问题与回答（从原型到发布）

#### 问 1：做一个游戏原型（Prototype）时应优先实现哪些部分？

**答**：
原型阶段目标是验证核心玩法。优先实现：

1. **核心交互**（玩家能做什么，操控是否顺手）——例如玩家移动、主要攻击或解谜交互。
2. **胜负条件/反馈**（什么时候算成功/失败，玩家如何知道）——视觉/音效反馈非常重要。
3. **最低可玩的场景**（单一关卡或场景即可）以便快速迭代。
4. **调参接口**（让设计师随时调整数值）——可用 `ScriptableObject` 或调试面板。
5. **快速测试循环**：保证从修改代码到看到效果的循环尽可能短。
   避免在原型阶段花太多精力在美术或性能优化，除非这直接影响玩法验证。

#### 问 2：如何使用 `ScriptableObject` 进行配置和数据驱动设计？

**答**：

* `ScriptableObject` 是一种可序列化的资产，适合存放游戏配置（武器参数、关卡数据、全局设置等），它的优点是不随场景卸载、易于在编辑器中调整、能被多个对象共享。
* 常见用法：创建 `WeaponData : ScriptableObject`，包含伤害、射速、弹道等字段；游戏中的武器组件引用该 `ScriptableObject`。这样可以在运行时不改代码即可调整数值。
* 注意：`ScriptableObject` 保存的是引用，运行时修改会影响资产本身（除非你用 `Instantiate` 创建副本），因此要谨慎。对于临时运行时状态，最好在实例化时复制数据或只修改对象内的私有副本。
* 配合自定义编辑器可以构建易用的设计器面板，提高内容制作效率。

#### 问 3：如何高效进行版本控制与多人协作（Git + Unity）？

**答**：

* 使用 `.gitignore` 忽略 `Library/`、`Temp/`、`obj/` 等自动生成目录，只保留 `Assets/`、`ProjectSettings/`、`Packages/`。
* 将大文件（如未压缩的音频或视频）放到 LFS（Git LFS）。
* 尽量把场景（`.unity`）拆分为多个子场景（Subscenes / additive scenes）或使用 Prefab 化，减少多人同时修改同一文件的冲突。
* 通过 Unity 的 `Prefab`、`Addressable` 以及 `Packages` 把可独立工作的模块隔离出来，降低合并冲突。
* 建立分支策略（feature 分支、pull request、CI）并在 CI 中运行自动化构建与简单的静态代码检查。

#### 问 4：什么是 Addressables，为什么比传统 Resources 更好？

**答**：

* Addressables 是 Unity 的资源管理系统，支持异步加载、按需加载、资源分组、依赖管理和更灵活的构建打包策略。相较于 `Resources.Load`，Addressables 提供更好的内存控制（避免一次性加载大量资源）、更好的构建体积管理和更灵活的远程/本地资源分发能力。
* 使用 Addressables 时，你给资源一个地址（或标签），在运行时通过地址异步加载并在不需要时释放。特别适合大型项目、热更或需要 CDN 分发内容的场景。
* 学习成本略高，但从长期维护角度更推荐 Addressables。

#### 问 5：打包发布时常见问题与解决方法（比如构建尺寸太大、平台兼容性）？

**答**：
常见策略与排查点：

* **构建尺寸大**：压缩纹理（使用适合平台的压缩格式）、剔除未使用的资源（检查 Resources 文件夹）、使用 Addressables 分包、去掉调试符号、启用 Managed Stripping Level（谨慎）。
* **平台兼容性**：确认插件/原生库是否支持目标平台（Windows、Android、iOS 等），并在 Player Settings 中配置正确的架构（ARM/ARM64/x86）。
* **运行时崩溃**：查看日志（Android 的 `adb logcat`、iOS 的 device logs），用 Development Build + Script Debugging 定位问题。
* **性能问题**：使用 Profiler（Frame Debugger、CPU/GPU Profiler、Memory Profiler）定位热点，先优化热路径（Update 中的逻辑、GC 分配、Draw Call 数），合理使用静态合批/动态合批、Occlusion Culling。
* **证书/签名问题（移动端）**：确保 keystore、provision profiles、bundle identifier 对应无误，构建前清理旧签名缓存。

