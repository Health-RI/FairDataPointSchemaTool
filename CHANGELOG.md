# Changelog

All notable changes to this project will be documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [v1.0.1] - 2026-01-07

### Added
- feat: add docker setup with compose support (#19) by @kburger in bb98181
- ci: implement sonar for java by @Alexander Harms in 3c4d720
- feat: update to v2.0.0 release of the datamodel, using the release tag on github by @PatrickDekkerHealthRI in 4dfea65
- feat: update to v2.0.0 release of the datamodel, using the release tag on github by @PatrickDekkerHealthRI in 659aa65
- feat: improve Properties.yaml by using root folder property. fixed logging and closing of resource in the xls2rdf library by @PatrickDekkerHealthRI in 410d207
- feat: add option to write one shacl with all pieces merged by @PatrickDekkerHealthRI in c8e512e
- feat: use shacl-play to import excel-templates by @PatrickDekkerHealthRI in 624d378
- feat: add Properties for Health-RI v1 by @Alexander Harms in b90a13c


### Changed
- check if this is sufficient by @Hans-christian in a29b159
- Added puch to docker hub by @Hans-christian in defd2f3
- docs: update README.md by @Alexander Harms in fd87701
- chore(deps): bump ch.qos.logback:logback-core by @dependabot[bot] in 4281cdf
- Change inputDir to use master branch by @Quinten in 129ef37
- chore: add some unittest by @PatrickDekkerHealthRI in ee6f9e5
- chore: update pom.xml add version to maven plugins by @PatrickDekkerHealthRI in 8902292
- chore: update github action to use java 21 by @PatrickDekkerHealthRI in 53ba4c0
- ci: adds install step to workflow by @Alexander Harms in 0b50e2d
- ci: update pom.xml by @Alexander Harms in 237f8ed
- ci: updates testing and release workflow by @Alexander Harms in b4842ec
- chore(deps): bump org.apache.poi:poi from 4.1.0 to 4.1.1 by @dependabot[bot] in 0c9ee91
- chore(deps): bump org.apache.poi:poi-ooxml from 4.1.0 to 5.4.0 by @dependabot[bot] in 9d42297
- Update Properties.yaml by @Hannah Neikes in d07e32f
- Update README.md to v2.0.0 of the Health-ri-data model by @Patrick Dekker in 7cb8987
- chore: update pre build jar and properties.yaml by @PatrickDekkerHealthRI in 83bb8e4


### Fixed
- fix release by @Hans-christian in fa04edf
- fix: added test cases by @PatrickDekkerHealthRI in 2858743
- fix(ci): test on PR ready to review by @Alexander Harms in 23945ca
- fix: enable JaCoCo for code coverage by @PatrickDekkerHealthRI in 020407a
- fix: update filename in unittest. So they also work on case-sensitive enviroments, like linux by @PatrickDekkerHealthRI in d12c82b
- fix: copy Properties.yaml to jar directory by @PatrickDekkerHealthRI in 37563b9
- fix: build fix missing xls2rdf library in build by @PatrickDekkerHealthRI in f3c4010
- fix: update dependencies add option to add resource with explicit schema some code cleanup as well by @PatrickDekkerHealthRI in d0e061b
- fix: properties.yaml resource datasetserie uri by @Patrick Dekker in ea8b67f
- fix: properties.yaml resource uri for dataset by @Patrick Dekker in 58b7081
- fix: update readme to reflect current version of the tool and fix datasetseries resource IRI by @PatrickDekkerHealthRI in 043c9e7


