# MapMemory <GitHub path="RedMadRobot/mapmemory"/>
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

Add the dependency:
```kotlin
repositories {
    jcenter()
}

dependencies {
    implementation("com.redmadrobot.mapmemory:mapmemory:1.0")
    // or if you want to work with memory in reactive style
    implementation("com.redmadrobot.mapmemory:mapmemory-rxjava2:1.0")
}
```

## Conception

Kotlin provides delegates for access to map entries.
This library exploits and improves this idea to implement in-memory storage.

There are two simple principles:
- Memory is a singleton, and it can be shared between many consumers
- Memory holds data but not knows **what** data it holds

## Usage

```kotlin
class CardsInMemoryStorage(memory: MapMemory) {

    private val cards: MutableMap<String, Card> by memory.map()

    operator fun get(id: String): Card? = cards[id]
    operator fun set(id: String, card: Card) {
        cards[id] = card
    }
}
```

There are default accessors available:

| Accessor     | Default value | Description           |
|--------------|---------------|-----------------------|
| `nullable()` | `null`        | Store nullable values |
| `map()`      | Empty map     | Store values in map   |
| `list()`     | Empty list    | Store values in list  |

You can create own accessor if need.

You, also, can use delegate without any functions:
```kotlin
var unsafeValue: String by memory
```
Be careful, there no default values, and you will get `NoSuchElementException` if you try read value before it was written.

### Reactive Style

To use `MapMemory` in reactive style, add replace dependency `mapmemory` with `mapmemory-rxjava2`.

It adds accessors for RxJava types:

| Accessor            | Default value   | Description                         |
|---------------------|-----------------|-------------------------------------|
| `behaviorSubject()` | Empty subject   | Store stream of values              |
| `publishSubject()`  | Empty subject   | Store stream of values              |
| `maybe()`           | `Maybe.empty()` | Reactive analog to store "nullable" |
| `reactiveMap()`     | Empty map       | Store values in **reactive map**    |

`ReactiveMap` works similar to `MutableMap` but enables you to observe data in reactive manner.
It has methods `getStream(key)` and `getAllStream()` to observe one or all map values accordingly.

With reactive in-memory cache you will always have actual data.
Also you can separate subscription to data and request of data to manage it easier.
```kotlin
class CardsRepository(memory: MapMemory) {

    private val cardsCache: ReactiveCache<Card> by memory.reactiveCache()

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

### Memory Scopes

May be useful to create memory scopes.
You can use it to control data lifetime.
```kotlin
/** Memory, available during a session and cleared on logout. */
@Singleton
class SessionMemory @Inject constructor() : MapMemory()

/** Memory, available during the app lifetime. */
@Singleton
class AppMemory @Inject constructor() : MapMemory()
```
Keep in mind that you should manually clear `SessionMemory` on logout.

Instead of creating subclass, you can provide MapMemory with [qualifiers]. 

### Avoid `ClassCastException`

It can be caused if you have declared fields with the same name but different types in several places.
Note that field name used as a key to access a value in `MapMemory`.
To avoid `ClassCastException`, use unique names for fields.

This snippet demonstrates how it works:
```kotlin
class StringsStorage(memory: MapMemory) {
    var values: MutableList<String> by memory.list()
}

class IntsStorage(memory: MapMemory) {
    var values: MutableList<Int> by memory.list() // The same name as in StringsStorage
}

val strings = StringsStorage(memory)
val ints = IntsStorage(memory)

strings.values.add("A")
ints.values.add(1)

println(memory["values"]) // [A, 1]
```

## Contributing
Merge requests are welcome.
For major changes, please open an issue first to discuss what you would like to change.

## License

[MIT][license]

[qualifiers]: https://dagger.dev/dev-guide/#qualifiers
[license]: LICENSE
