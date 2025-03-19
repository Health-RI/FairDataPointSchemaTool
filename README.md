# SchemaTools

## Overview

This Java tool will configure a Fair Data Point to use the Health-ri metamodel version 2.

## Features

- Reads SHACL files from a specified directory or github
- Combines multiple SHACL files based on schema definitions
- Uploads the combined data to a Fair Data Point
- Configurable via a YAML properties file

## Requirements

- Java (JDK 17 or higher)
- Maven

## Installation

You can skip this part, if you download the v1.0. release from github. The jar folder will contain the pre-build tool.
Clone the repository and build the project using Maven:

```sh
    git clone https://github.com/Health-RI/FairDataPointSchemaTool.git
    cd FairDataPointSchemaTool
    mvn clean install
```

## Usage

Run the tool with the required configuration file:

```sh
    java -jar target/FairDataPointSchemaTool-1.0.jar -i /path/to/Properties.yaml -h address_of_fdp -p yourpassword -u username -c command
```

-i path to Properties.yaml, if this files is located you can use relative location (default is ./Properties.yaml)

-u fdp admin user (default: albert.einstein@example.com)

-p password (default: password)

-c Determine what the tool will do: we have 4 options:

* both -> Schema and resource will be updated. (default option)
* schema -> The schema will be updated
* resource -> Resource descriptions will be updated.
* files -> will create merged turtle files only, this option is for internal use only.)

## Configuration File (YAML Format)

The tool requires a properties file in YAML format to specify input schemas and resource information.
Below is an example: For most cases the properties doesn't need to be updated.

```yaml
---
schemas:
  Catalog:
    - "Catalog.ttl"
    - "Agent.ttl"
    - "Kind.ttl"
    - "PeriodOfTime.ttl"
  Dataset:
    - "Dataset.ttl"
    - "Agent.ttl"
    - "Kind.ttl"
    - "PeriodOfTime.ttl"
  Dataset Series:
    - "DatasetSeries.ttl"
    - "Agent.ttl"
    - "Kind.ttl"
  Resource:
    - "Resource.ttl"
  Distribution:
    - "Distribution.ttl"
    - "PeriodOfTime.ttl"
    - "Checksum.ttl"
  Project:
    - "Project.ttl"
    - "Agent.ttl"
  Study:
    - "Study.ttl"
  Data Service:
    - "DataService.ttl"
    - "Agent.ttl"
    - "Kind.ttl"
parentChild:
  #dataset, catalog, data Service and Study all extend from resource. 
  Resource:
    - "Dataset"
    - "Catalog"
    - "Data Service"
    - "Project"
    - "Study"
resources:
  #note the FDP client, works with resources & childeren. This property file specify parents instead!
  Project:
    parentResource: "FAIR Data Point"
    parentRelationIri: "http://www.example.com/project"
  Study:
    parentResource: "Project"
    parentRelationIri: "http://www.example.com/study"
  Dataset Series:
    parentResource: "FAIR Data Point"
    parentRelationIri: "http://www.example.com/study"
inputDir: "https://raw.githubusercontent.com/Health-RI/health-ri-metadata/v2.0.0-beta.2/Formalisation(shacl)/Core/PiecesShape/"
outputDir: "C:\\Users\\PatrickDekker(Health\\IdeaProjects\\health-ri-metadata\\Formalisation(shacl)\\\
  Core\\FairDataPointShape"
#list of schemas we want to update.
schemasToPublish:
  - "Resource"
  - "Catalog"
  - "Dataset"
  - "Dataset Series"
  - "Distribution"
  - "Data Service"
  - "Project"
  - "Study"
#prefered version number of the schemas, only works if current version is smaller.
#if not the current version is used, but patch number is increased (2.0.0 -> 2.0.1)
schemaVersion: "2.0.0"

```

Note the "resourceToPublih" and "Schema" should be identical as the schema name it should replace on the Fair Data
Point! (including space & case)

## License

This project is licensed under the MIT License. See the LICENSE file for details.

## Contributing

Contributions are welcome! Please submit a pull request or open an issue for discussion.

