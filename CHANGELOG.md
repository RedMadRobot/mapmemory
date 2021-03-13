## [Unreleased]

### Added

- Copying constructor for `MapMemory`.
  Now you can initialize memory with specified content on creation.
- New module `mapmemory-test`.
  Contains utilities helping to test code that uses `MapMemory`.

### Changed

- `MapMemory` now is `MutableMap<String, Any>` instead of `MitableMap<String, Any?>`.
  It is made to prevent NPEs because `ConcurrentHashMap` not supports nullable values.
  You still can store nullable values in memory using delegate.

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
