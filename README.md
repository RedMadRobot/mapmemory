# MapMemory <GitHub path="RedMadRobot/mapmemory"/>

[![Version](https://img.shields.io/maven-central/v/com.redmadrobot.mapmemory/mapmemory?style=flat-square)][mavenCentral]
[![Build Status](https://img.shields.io/github/workflow/status/RedMadRobot/mapmemory/CI/main?style=flat-square)][ci]
[![License](https://img.shields.io/github/license/RedMadRobot/mapmemory?style=flat-square)][license]

Simple in-memory cache conception built on `Map`.

---
<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [Installation](#installation)
- [Conception](#conception)
- [Usage](#usage)
  - [Collections](#collections)
  - [Scoped and Shared values](#scoped-and-shared-values)
  - [Reactive Style](#reactive-style)
- [Advanced usage](#advanced-usage)
  - [Memory Lifetime](#memory-lifetime)
  - [Testing](#testing)
- [Migration Guide](#migration-guide)
  - [Upgrading from v1.1](#upgrading-from-v11)
- [Contributing](#contributing)
- [License](#license)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Installation

Add dependencies:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.redmadrobot.mapmemory:mapmemory:2.0-rc1")

    // or if you want to work with memory in reactive style, add one of
    implementation("com.redmadrobot.mapmemory:mapmemory-coroutines:2.0-rc1")
    implementation("com.redmadrobot.mapmemory:mapmemory-rxjava2:2.0-rc1")
    implementation("com.redmadrobot.mapmemory:mapmemory-rxjava3:2.0-rc1")

    // if you want to test code that uses MapMemory
    testImplementation("com.redmadrobot.mapmemory:mapmemory-test:2.0-rc1")
}
```

## Conception

Kotlin provides delegates to access values in map:

```kotlin
val map = mapOf("answer" to 42)
val answer: Int by map
println(answer) // 42
```

This library uses this idea to implement in-memory storage.

There are two simple principles:

- **MapMemory** is a singleton, and it is shared between many consumers
- **MapMemory** holds data but not knows **what** data it holds

## Usage

> :memo: If you use any kind of DI framework, you should provide `MapMemory` with `Singleton` scope:
>
> ```kotlin
> @Provides
> @Singleton
> fun provideMapMemory(): MapMemory = MapMemory()
> ```
> If you don't, you should create a single instance of `MapMemory` and use it everywhere.

Imagine, you have `UsersRepository` used to get users' information from API.
You want to remember the last requested user.
Let's store it into a `MapMemory`:

```kotlin
class UsersRepository(
    private val api: Api,
    memory: MapMemory,              // (1) Inject MapMemory into constructor
) {

    var lastUser: User? by memory   // (2) Declare in-memory property using delegate
        private set

    suspend fun getUser(email: String): User {
        return api.getUser(email)
            .also { lastUser = it } // (3) Use the property
    }
}
```
`MapMemory` is a singleton, but `UsersRepository` is not.
Property `lastUser` is tied to `MapMemory` lifetime, so it will survive `UsersRepository` recreation.

You can specify the default value that will be used when the value you're trying to read is not written yet.
For example, we don't want nullable `User`, but want to get placeholder object `User.EMPTY` instead:

```kotlin
var lastUser: User by memory { User.EMPTY }
```

### Collections

You can write the following code to store mutable list in `MapMemory`:

```kotlin
val users: MutableList<User> by memory { mutableListOf() }
```

Boilerplate.
Fortunately, there are shorthand accessors to store lists and maps:

```kotlin
val users by memory.mutableList<User>()
```

Accessors `mutableList` and `mutableMap` use concurrent collections under the hood.

| Accessor        | Default value      | Description           |
|-----------------|--------------------|-----------------------|
| `map()`         | Empty map          | Store map             |
| `mutableMap()`  | Empty mutable map  | Store values in map   |
| `list()`        | Empty list         | Store list            |
| `mutableList()` | Empty mutable list | Store values in list  |

Feel free to create your accessors if needed.

### Scoped and Shared values

Let's look at how MapMemory works under the hood.
We have a class with in-memory property declared with delegate:

```kotlin
package com.example

class TokenStorage(memory: MapMemory) {
    var authToken: String by memory
}
```

`MapMemory` is `MutableMap<String, Any>`.
Delegate accesses map value by key retrieved from property name.
This behavior differs for two types of in-memory property delegates:
- **Scoped** to the class where the property is declared.
  Property key is combination of class and property name: `com.example.TokenStorage#authToken`
- **Shared** between all classes by the specified key.
  All properties are scoped by default, you can share it with the function `shared`.

Property `authToken` is scoped to class `TokenStorage`, but we can share it:

```kotlin
// It is a good practice to declare constants for shared keys.
const val KEY_AUTH_TOKEN = "authToken"

class TokenStorage(memory: MapMemory) {
    var authToken: String by memory.shared(KEY_AUTH_TOKEN)
}

class Authenticator(memory: MapMemory) {
    // Property name may be different
    var savedToken: String by memory.shared(KEY_AUTH_TOKEN)
}
```

Both `TokenStorage` and `Authenticator` will use the same value.

> :warning: Keep in mind that it is just an example.
> In real code it may be more reasonable to inject `TokenStorage` into `Authenticator` instead of sharing in-memory property by key.

### Reactive Style

Reactive subscription to values is useful to keep data on all screens up to date.

To use MapMemory in reactive style, replace dependency `mapmemory` with one of the following:

- `mapmemory-coroutines`
- `mapmemory-rxjava2`
- `mapmemory-rxjava3`

These modules provide accessors for reactive types:

```kotlin
// with coroutines
val selectedOption: MutableStateFlow<Option> by memory.stateFlow(Option.DEFAULT)

// with RxJava
val selectedOption: BehaviorSubject<Option> by memory.behaviorSubject()
```

> :warning: You can use only one of these dependencies at the same time
> Otherwise build will fail due to duplicates in the classpath.

MapMemory provides the type `ReactiveMutableMap`.
It works similar to `MutableMap` but enables you to observe data in a reactive manner.
There are methods to observe one or all map values.
You can implement cache-first approach using `ReactiveMutableMap`:

<details open>
  <summary>Coroutines</summary>

  ```kotlin
  class UsersRepository(
      api: Api,
      memory: MapMemory,
  ) {
      private val usersCache by memory.reactiveMutableMap<String, User>()
  
      /** Returns stream of users from cache. */
      fun getUsersFlow(): Flow<List<User>> = usersCache.valuesFlow
  
      /** Returns stream of one user from cache. */
      fun getUserFlow(id: String): Flow<User> = usersCache.getValueFlow(id)
  
      /** Update users in cache. */
      suspend fun fetchUsers() {
          val users: List<User> = api.getUsers()
          usersCache.replaceAll(users.associateBy { it.id })
      }
  }
  ```

</details>

<details>
  <summary>JxJava</summary>

  ```kotlin
  class UsersRepository(
      api: Api,
      memory: MapMemory,
  ) {
      private val usersCache by memory.reactiveMutableMap<String, User>()
  
      /** Returns stream of users from cache. */
      fun getUsersObservable(): Observable<List<User>> = usersCache.valuesObservable
  
      /** Returns stream of one user from cache. */
      fun getUserObservable(id: String): Observable<User> = usersCache.getValueObservable(id)
  
      /** Update users in cache. */
      fun fetchUsers() {
          val users: List<User> = api.getUsers()
          usersCache.replaceAll(users.associateBy { it.id })
      }
  }
  ```

</details>

#### Coroutines

`mapmemory-coroutines` adds accessors for coroutines types:

| Accessor               | Default value                           | Description                      |
|------------------------|-----------------------------------------|----------------------------------|
| `stateFlow()`          | StateFlow with specified `initialValue` | Store stream of values           |
| `sharedFlow()`         | Empty flow                              | Store stream of values           |
| `reactiveMutableMap()` | Empty map                               | Store values in **reactive map** |

> :memo: Coroutines implementation of reactive map uses `SharedFlow` under the hood, so it will be triggered even if its content is not changed.

#### RxJava

`mapmemory-rxjava2` and `mapmemory-rxjava3` adds accessors for RxJava types:

| Accessor               | Default value   | Description                         |
|------------------------|-----------------|-------------------------------------|
| `behaviorSubject()`    | Empty subject   | Store stream of values              |
| `publishSubject()`     | Empty subject   | Store stream of values              |
| `maybe()`              | `Maybe.empty()` | Reactive analog to store "nullable" |
| `reactiveMutableMap()` | Empty map       | Store values in **reactive map**    |

## Advanced usage

### MapMemory Lifetime

It may be useful to create `MapMemory` instances with a different lifetime.
You can use it to control the lifetime of the data stored within.

```kotlin
/** MapMemory, available during a session and cleared on logout. */
@Singleton
class SessionMemory @Inject constructor() : MapMemory()

/** MapMemory, available during the app lifetime. */
@Singleton
class AppMemory @Inject constructor() : MapMemory()
```

Keep in mind that you should manually clear `SessionMemory` on logout.

> :memo: Instead of creating subclasses, you can provide MapMemory with [qualifiers].

#### KAPT: 'IllegalStateException: Couldn't find declaration file' on delegate with inline getValue operator

There is the bug in Kotlin Compiler that affects MapMemory if you create subclasses - [KT-46317](https://youtrack.jetbrains.com/issue/KT-46317).
You can use module `mapmemory-kapt-bug-workaround` as a workaround:

```kotlin
dependencies {
    implementation("com.redmadrobot.mapmemory:mapmemory-kapt-bug-workaround:[latest-version]")
}
```

```diff
- val someValue: String by memory
+ val someValue: String by memory.value()
```

### Testing

Module `mapmemory-test` provides utilities helping to test code that uses MapMemory.

Imagine you want to build memory filled with mock data for the following class:

```kotlin
package com.example

class UserCache(memory: MapMemory) {
    var name: String by memory
    var ages: Int by memory
}
```

You can put it by key:

```kotlin
val memory = MapMemory()
memory["com.example.UserCache#name"] = "John Doe"
memory["com.example.UserCache#ages"] = 42
```

It is easy to make mistake and this approach requires knowing how MapMemory work under the hood.
Using `mapMemoryOf` and `scopedKeyOf` you can build mock `MapMemory` much easier:

```kotlin
val memory = mapMemoryOf(
    scopedKeyOf(UserCache::name) to "John Doe",
    scopedKeyOf(UserCache::ages) to 42,
)
```

You can also get or set scoped values using type-safe functions `putScoped` and `getScoped`:

```kotlin
memory.putScoped(UserCache::name, "Jane Doe")
memory.getScoped(UserCache::name)
```

There are alternate syntax to use if properties in class are private and can't be accessed via reference:

```kotlin
scopedKeyOf<UserStorage>("name")
memory.putScoped<UserStorage>("name", "Jane Doe")
memory.getScoped<UserStorage>("name")
```

## Migration Guide

### Upgrading from v1.1

#### Potentially breaking changes

**Collections accessors**

Now accessors `map` and `list` return delegates to access immutable collections.
You should use `mutableMap` and `mutableList` for mutable versions of collections.

**Closed access to getOrPutProperty**

Extension `getOrPutProperty` become internal (it already was in `internal` package), use operator `MapMemory.invoke` instead.

```diff
-var counter: Int by memory.getOrPutProperty { 0 }
+var counter: Int by memory { 0 }
```

**Scoped and Shared values**

Read ["Scoped and Shared values"](#scoped-and-shared-values) section.
If you are sharing properties between classes by name, you should make these properties shared.

#### API Changes

**Accessor `.nullable()` is deprecated**

Accessor `nullable()` is not needed now.
You can just declare a nullable field:

```diff
-val selectedOption: String? by memory.nullable()
+val selectedOption: String? by memory
```

**`.withDefault { ... }` is banned from use**

`withDefault` is no more compatible with MapMemory, so you should use the operator `invoke` instead:

```diff
-var counter: Int by memory.withDefault { 0 }
+var counter: Int by memory { 0 }
```

#### ReactiveMap -> ReactiveMutableMap

```diff
-var users by memory.reactiveMap<User>()
+var users by memory.reactiveMutableMap<String, User>()
```

**Naming changes**

Word `stream` in methods names replaced with implementation-specific words to make API clearer.

Coroutines:
- `getStream` -> `getFlow` and `getValueFlow`
- `getAllStream` -> `valuesFlow`

RxJava:
- `getStream` -> `getValueObservable`
- `getAllStream` -> `valuesObservable`

## Contributing

Merge requests are welcome.
For major changes, please open an issue first to discuss what you would like to change.

## License

[MIT][license]

[mavenCentral]: https://search.maven.org/search?q=g:com.redmadrobot.mapmemory
[ci]: https://github.com/RedMadRobot/mapmemory/actions
[qualifiers]: https://dagger.dev/dev-guide/#qualifiers
[license]: LICENSE
