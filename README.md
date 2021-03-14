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

This library exploits and improves this idea to implement in-memory storage.

There are two simple principles:

- Memory is a singleton, and it is shared between many consumers
- Memory holds data but not knows **what** data it holds

## Usage

Imagine, you have `UsersRepository` used to get users' information from API.
You want to remember last requested user.
Let's store it into memory:

```kotlin
class UsersRepository(
    private val api: Api,
    memory: MapMemory,              // Memory injected into constructor
) {

    var lastUser: User? by memory   // Use delegate to access memory value
        private set

    suspend fun getUser(email: String): User {
        return api.getUser(email)
            .also { lastUser = it } // Save last received user to memory
    }
}
```

Memory is a singleton, but `UsersRepository` is not.
Property `lastUser` is tied to memory lifetime, so it will survive `UsersRepository` recreation.

You can specify default value will be used when value you're trying to read is not written yet.
For example, we don't want nullable `User`, but want to get placeholder object `User.EMPTY` instead:

```kotlin
var lastUser: User by memory { User.EMPTY }
```

### Collections

You can write the following code to store mutable list in memory:

```kotlin
val users: MutableList<User> by memory { mutableListOf() }
```

Boilerplate.
Fortunately there are shorthand accessors to store lists and maps:

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

Feel free to create own accessors if needed.

### Scoped and Shared values

Let's look how MapMemory works under the hood.
We have a class with memory property declared with delegate:

```kotlin
package com.example

class TokenStorage(memory: MapMemory) {
    var authToken: String by memory
}
```

MapMemory is `MutableMap<String, Any>`.
Delegate accesses map value by key retrieved from property name.
There are two types of memory property delegates:
- **Scoped** to the class where the property is declared.
  Property key is combination of class and property name: `com.example.TokenStorage#authToken`
- **Shared** between all classes by the specified key.
  All properties are scoped by default, you can share it with the function `shared`.

Property `authToken` is scoped to class `TokenStorage`, but we can share it:
```kotlin
// It is a good practice to declare constants for shared memory keys.
const val KEY_AUTH_TOKEN = "authToken"

class TokenStorage(memory: MapMemory) {
    var authToken: String by memory.shared(KEY_AUTH_TOKEN)
}

class Authenticator(memory: MapMemory) {
    // Property name may be different
    var savedToken: String by memory.shared(KEY_AUTH_TOKEN)
}
```
Both `TokenStorage` and `Authenticator` will use the same memory value.

> :warning: Keep in mind that it is just an example. 
> In real code it may be more reasonably to inject `TokenStorage` into `Authenticator` instead of sharing memory by key.

### Reactive Style

Reactive subscription to memory values is useful to keep data on all screens up to date.

To use `MapMemory` in reactive style, replace dependency `mapmemory` with one of the following:

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
> Otherwise build will fail due to duplicates in classpath.

MapMemory provides own reactive type - `ReactiveMap`.
It works similar to `MutableMap` but enables you to observe data in reactive manner.
There are methods `getStream(key)` and `getAllStream()` to observe one or all map values accordingly.
You can implement cache-first approach using `ReactiveMap`:

```kotlin
class UsersRepository(
    api: Api,
    memory: MapMemory,
) {
    private val usersCache by memory.reactiveMap<User>()
    
    /** Returns stream of users from cache. */
    fun getUsersStream(): Flow<List<User>> = usersCache.getAllStream()
    
    /** Returns stream of one user from cache. */
    fun getUserStream(email: String): Flow<User> = usersCache.getStream(email)
    
    /** Update users in cache. */
    suspend fun fetchUsers() {
        val users: List<User> = api.getUsers()
        usersCache.replaceAll(users.associateBy { it.email })
    }
}
```

#### Coroutines

`mapmemory-coroutines` adds accessors for coroutines types:

| Accessor            | Default value                           | Description                         |
|---------------------|-----------------------------------------|-------------------------------------|
| `stateFlow()`       | StateFlow with specified `initialValue` | Store stream of values              |
| `sharedFlow()`      | Empty flow                              | Store stream of values              |
| `reactiveMap()`     | Empty map                               | Store values in **reactive map**    |

> :memo: Coroutines reactive map uses `StateFlow` under the hood, so it will not be triggered while content not changed.

#### RxJava

`mapmemory-rxjava2` and `mapmemory-rxjava3` adds accessors for RxJava types:

| Accessor            | Default value   | Description                         |
|---------------------|-----------------|-------------------------------------|
| `behaviorSubject()` | Empty subject   | Store stream of values              |
| `publishSubject()`  | Empty subject   | Store stream of values              |
| `maybe()`           | `Maybe.empty()` | Reactive analog to store "nullable" |
| `reactiveMap()`     | Empty map       | Store values in **reactive map**    |

## Advanced usage

### Memory Lifetime

May be useful to create memory instances with different lifetime.
You can use it to control lifetime of the data stored within.

```kotlin
/** Memory, available during a session and cleared on logout. */
@Singleton
class SessionMemory @Inject constructor() : MapMemory()

/** Memory, available during the app lifetime. */
@Singleton
class AppMemory @Inject constructor() : MapMemory()
```

Keep in mind that you should manually clear `SessionMemory` on logout.

> :memo: Instead of creating subclasses, you can provide MapMemory with [qualifiers].

### Testing

Module `mapmemory-test` provides utilities helping to test code that uses MapMemory.

Using `mapMemoryOf` and `scopedKeyOf` you can build mock memory:

```kotlin
class MemoryConsumer(memory: MapMemory) {
    var someMemoryValue: String by memory
}

val memory = mapMemoryOf(
    scopedKeyOf<MemoryConsumer>("someMemoryValue") to "Mock Value"
)
```

You can also get or set scoped values using function `putScoped` and `getScoped`:

```kotlin
memory.putScoped<MemoryConsumer>("someMemoryValue", "Changed Value")
memory.getScoped<MemoryConsumer>("someMemoryValue")
```

There are alternate syntax using property reference. It can be used when property in class is public:

```kotlin
scopedKeyOf(MemoryConsumer::someMemoryValue)
memory.putScoped(MemoryConsumer::someMemoryValue, "Changed Value")
memory.getScoped(MemoryConsumer::someMemoryValue)
```

## Migration Guide

### Upgrading from v1.1

#### Breaking changes

**Collections accessors**

Now accessors `map` and `list` return delegates to access immutable collections.
You should use `mutableMap` and `mutableList` for mutable versions of collections.

**Closed access to getOrPutProperty**

Extension `getOrPutProperty` become internal (it already was in `internal` package), use operator `MapMemory.invoke` instead.

```diff
-var counter: Int by memory.getOrPutProperty { 0 }
+var counter: Int by memory { 0 }
```

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

## Contributing

Merge requests are welcome.
For major changes, please open an issue first to discuss what you would like to change.

## License

[MIT][license]

[mavenCentral]: https://search.maven.org/search?q=g:com.redmadrobot.mapmemory
[ci]: https://github.com/RedMadRobot/mapmemory/actions
[qualifiers]: https://dagger.dev/dev-guide/#qualifiers
[license]: LICENSE
