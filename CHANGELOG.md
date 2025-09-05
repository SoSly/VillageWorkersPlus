# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased](https://github.com/SoSly/VillageWorkersPlus/tree/main)

### Fixed
- fixed critical performance issue causing high CPU load
- fixed infinite loop in TargetKnownWorkerGoal when workers couldn't be selected
- fixed porters being unable to target workers who need help (hungry/toolless workers)
- fixed porters not detecting missing tools when workers lose them (now checks inventory directly)

## 0.1.0-alpha

### Added
- added a new village worker profession: the porter, purchased from the cartographer
