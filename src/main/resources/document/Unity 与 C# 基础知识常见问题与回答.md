# Unity 与 C# 基础知识常见问题与回答

#### 问 1：Unity 的生命周期方法（如 `Awake`、`Start`、`Update`、`FixedUpdate`、`OnEnable`、`OnDisable`）有什么区别，什么时候用哪一个？

**答**：

* `Awake()`：在脚本实例被加载时调用（无论脚本是否启用），常用于初始化非依赖于其它对象的内部数据或注册静态引用。它在 `OnEnable` 之前执行。
* `OnEnable()`：当脚本或游戏对象被启用时调用（包括场景加载后、手动启用、或激活父对象）。适合注册事件监听器、启动协程（如果在启用时需要）等。
* `Start()`：在第一次 `Update()` 之前调用，且仅当脚本启用时才会调用。适合进行依赖其它对象已初始化的设置（例如引用通过 `Find`/Inspector 指定的组件）。
* `Update()`：每帧调用一次，用于处理帧率相关的逻辑（输入检测、非物理的移动、UI 更新）。不要在此做重型计算或固定步长物理处理。
* `FixedUpdate()`：以固定时间步（物理时间步）调用，用于物理计算和使用 `Rigidbody` 的移动（例如使用 `AddForce` 或设置 `velocity`）。不要把输入读取放到 `FixedUpdate`，除非你在同步物理和输入。
* `OnDisable()`：当脚本或对象被禁用/销毁时调用，用于反注册事件、停止协程、释放资源。

**常见模式**：在 `Awake()` 做内部初始化，在 `Start()` 获取或校验对外依赖（例如其他组件或单例），在 `Update()` 做每帧逻辑，在 `FixedUpdate()` 做物理更新，使用 `OnEnable/OnDisable` 管理订阅或协程生命周期。

#### 问 2：C# 中 `struct` 与 `class` 在 Unity 中的使用注意事项？

**答**：

* `class` 是引用类型（reference type），在堆上分配，变量是引用。适用于大多数复杂对象、带有可变状态或需要继承/多态的情况。
* `struct` 是值类型（value type），通常在栈上分配（或作为字段内联），复制时会按值拷贝。适合小而不变的数据（如小的向量、坐标对），但 Unity 已经提供了 `Vector3` 等优化好的 struct。
* 注意性能陷阱：大量小 `struct` 数组、装箱（boxing）或把 `struct` 放入 `List<object>` 等会导致分配和拷贝。对大数据结构或需要引用语义的对象，应使用 `class`。
* 在设计组件时：`MonoBehaviour` 必须是 `class`（继承自 `MonoBehaviour`），不能用 `struct`。如果要在组件里保存数据且需要频繁修改，优先用 `class` 或使用 `NativeArray`/`Burst`/`Jobs` 的原生容器来优化。

#### 问 3：如何避免在 Update 中触发垃圾回收（GC）？

**答**：

* 避免在 `Update()` 中产生临时对象：不要在 `Update()` 中频繁 new 字符串、数组、List、LINQ 操作或装箱操作。
* 预分配对象池（object pool）：对需反复创建/销毁的实体（子弹、特效）使用对象池，避免频繁 GC。
* 避免 `foreach` 在可变集合上产生分配（尤其在旧的 .NET/IL2CPP 后端），使用 `for` 循环或 `List<T>.Count` 缓存索引。
* 使用 `StringBuilder` 拼接大量字符串；避免 `ToString()` 在热路径中频繁调用。
* 在需要高性能时考虑 Unity 的 Job System、Burst 和原生容器（`NativeArray`, `NativeList`）来减少 managed 分配。

#### 问 4：C# 的事件（`event`）与委托（`delegate`）在 Unity 中如何安全使用，尤其是生命周期管理？

**答**：

* `delegate` 是一种函数指针类型，`event` 是对 `delegate` 的封装，用于发布/订阅模型。
* 在 Unity 中订阅事件时，应在 `OnEnable`/`Start` 中 `+=` 订阅，在 `OnDisable`/`OnDestroy` 中 `-=` 取消订阅，防止游戏对象销毁后仍被回调导致 `NullReferenceException` 或内存泄露。
* 对于静态事件尤其要小心：静态事件持有引用会阻止对象被回收。始终确保静态事件上的取消订阅。
* 使用弱引用或 `UnityAction`（配合 `UnityEvent`）在某些场景能更安全地管理生命周期，但仍需手动管理取消订阅以避免意外回调。

#### 问 5：如何组织项目文件夹（Assets）与命名空间以利于团队协作？

**答**：

* 目录结构建议按功能（Feature）或模块划分，而不是按类型：例如 `Assets/Gameplay/Player`、`Assets/Gameplay/Enemies`、`Assets/Systems/Audio`，便于多人并行工作与版本控制合并。
* 使用一致的命名空间（namespace）：例如 `YourGame.Player`、`YourGame.Systems.Audio`，避免默认无命名空间导致类名冲突。
* 把第三方插件放在 `Assets/Plugins` 或 `Assets/ThirdParty`，并记录版本与修改。
* 在大型项目中，将可复用代码放入 `Packages`（Unity Package Manager）或自建本地 package，方便模块化与版本控制。
* 保持每个脚本单一职责，文件名与类名一致，避免多个类写在同一个文件里（团队协作更清晰）。
