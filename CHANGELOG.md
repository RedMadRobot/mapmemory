## [Unreleased]

### Scoped and shared values (#1)

Now there are two types of memory values: scoped (to class) and shared.

All memory values by default are scoped to the class where it's declared.
Scoping prevents from unintended sharing of properties with the same name between classes.
This snippet demonstrates the problem:

```kotlin
package com.example

class StringsStorage(memory: MapMemory) {
    var values: MutableList<String> by memory.list()
}

class IntsStorage(memory: MapMemory) {
    var values: MutableList<Int> by memory.list() // The same field name as in StringsStorage
}

val strings = StringsStorage(memory)
val ints = IntsStorage(memory)

strings.values.add("A")
ints.values.add(1)

println(memory) 
```

Output:

```
For unscoped fields (old behaviour):
{values: [A, 1]}

For scoped fields (new behavior):
{com.example.StringsStorage#values: [A], com.example.IntsStorage#values: [1]}
```

You can make memory field **shared** using extension `shared(key: String)`:

```kotlin
// It is recommended to create constants for shared properties keys
const val KEY_SERVER_HOST = "serverHost"

class ServerConfig(memory: MapMemory) {
    var host: String by memory.shared(KEY_SERVER_HOST)
}

class DebugPanelConfig(memory: MapMemory) {
    var serverHost: String by memory.shared(KEY_SERVER_HOST)
}
```

### Removed `.nullable()` and `.withDefault { ... }`

Accessor `nullable()` is not needed now.
You can just declare a nullable field:
```diff
-val selectedOption: String? by memory.nullable()
+val selectedOption: String? by memory
```

`withDefault` is no more compatible with MapMemory, so you should use the operator `invoke` instead:
```diff
-var counter: Int by memory.withDefault { 0 }
+var counter: Int by memory { 0 }
```

### Mutable collections accessors
> **BREAKING CHANGE**

Now accessors `map` and `list` return delegates to access immutable collections.
You should use `mutableMap` and `mutableList` for mutable versions of collections.

### Added

- Copying constructor for `MapMemory`.
  Now you can initialize memory with specified content on creation.
- New module `mapmemory-test`.
  Contains utilities helping to test code that uses `MapMemory`.

### Changed

- `MapMemory` now is `MutableMap<String, Any>` instead of `MutableMap<String, Any?>`.
  It is made to prevent NPEs because `ConcurrentHashMap` not supports nullable values.
  You still can store nullable values in memory using a delegate.

## [1.1] (2021-02-26)

### Added

- Method `ReactiveMap.getValue` (#2)

### Changed

- ReactiveMap constructor now is public (#4)

### Housekeeping

- Updated Kotlin to 1.4.30
- Updated Gradle to 6.8
- Updated Detekt to 1.15.0
- Migrated from JCenter to Maven Central (#5)

## 1.0

Public release

[unreleased]: https://github.com/RedMadRobot/mapmemory/compare/v1.1...main
[1.1]: https://github.com/RedMadRobot/mapmemory/compare/v1.0...v1.1
