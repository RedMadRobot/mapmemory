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
  - [Reactive Style](#reactive-style)
  - [Memory Scopes](#memory-scopes)
  - [Avoid `ClassCastException`](#avoid-classcastexception)
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

Delegate accesses memory values by key retrieved from property name.
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

To use `MapMemory` in reactive style, replace dependency `mapmemory` with `mapmemory-rxjava2` or `mapmemory-coroutines`.

> You can't use both map `mapmemory-rxjava2` and `mapmemory-coroutines` at the same time because you will get duplicates in classpath.

Both of reactive implementations contain `RactiveMap`.
`ReactiveMap` works similar to `MutableMap` but enables you to observe data in reactive manner.
It has methods `getStream(key)` and `getAllStream()` to observe one or all map values accordingly.

With reactive in-memory cache you will always have actual data on screen.
Also, you can separate **subscription** to data and **request** of data to manage it easier.

### RxJava

`mapmemory-rxjava2` and `mapmemory-rxjava3` adds accessors for RxJava types:

| Accessor            | Default value   | Description                         |
|---------------------|-----------------|-------------------------------------|
| `behaviorSubject()` | Empty subject   | Store stream of values              |
| `publishSubject()`  | Empty subject   | Store stream of values              |
| `maybe()`           | `Maybe.empty()` | Reactive analog to store "nullable" |
| `reactiveMap()`     | Empty map       | Store values in **reactive map**    |

Example of cache-first approach with reactive subscription:

```kotlin
class CardsRepository(memory: MapMemory) {

    private val cardsCache: ReactiveMap<Card> by memory.reactiveMap()

    /** Returns observable for cards in cache. */
    fun getCardsStream(): Observable<List<Card>> = cardsCache.getAllStream()

    /** Updates cards in cache. */
    fun fetchCards(): Completable {
        return Single.defer {/* get cards from network */}
            .doOnSuccess { cardsCache.replaceAll(it) }
            .ignoreResult()
    }
}

class CardsViewModel(private val repository: CardsRepository) : ViewModel() {

    init {
        // Subscribe to cache
        repository.getCardsStream()
            .subscribe {/* update cards on screen */}
    }

    fun onRefresh() {
        repository.fetchCards()
            .subscribe(
                {/* success! hide progress */},
                {/* fail. hide progress and show error */}
            )
    }
}
```

### Coroutines

`mapmemory-coroutines` adds accessors for coroutines types:

| Accessor            | Default value                           | Description                         |
|---------------------|-----------------------------------------|-------------------------------------|
| `stateFlow()`       | StateFlow with specified `initialValue` | Store stream of values              |
| `sharedFlow()`      | Empty flow                              | Store stream of values              |
| `reactiveMap()`     | Empty map                               | Store values in **reactive map**    |

Example of cache-first approach with reactive subscription:

```kotlin
class CardsRepository(private val api: CardsApi, memory: MapMemory) {

    private val cardsCache: ReactiveMap<Card> by memory.reactiveMap()

    /** Returns flow for cards in cache. */
    fun getCardsStream(): Flow<List<Card>> = cardsCache.getAllStream()

    /** Updates cards in cache. */
    suspend fun fetchCards() {
        val cards = api.getCards()
        cardsCache.replaceAll(cards.associateBy { it.id })
    }
}

class CardsViewModel(private val repository: CardsRepository) : ViewModel() {

    init {
        // Subscribe to cache
        repository.getCardsStream()
            .onEach {/* update cards on screen */}
            .launchIn(viewModelScope)
    }

    fun onRefresh() {
        viewModelScope.launch {
            repository.fetchCards()
            // success! hide progress
        }
    }
}
```

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

## Contributing

Merge requests are welcome.
For major changes, please open an issue first to discuss what you would like to change.

## License

[MIT][license]

[mavenCentral]: https://search.maven.org/search?q=g:com.redmadrobot.mapmemory
[ci]: https://github.com/RedMadRobot/mapmemory/actions
[qualifiers]: https://dagger.dev/dev-guide/#qualifiers
[license]: LICENSE
