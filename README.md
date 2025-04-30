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
- Maven (mot needed if you use the prebuild jar file)

## Installation

if you want to use the prebuild jar file:

Clone the repository

```sh
    git clone https://github.com/Health-RI/FairDataPointSchemaTool.git
    cd FairDataPointSchemaTool/jar/
```

if you want to build the tool yourself using maven:

Clone the repository and build

```sh
    git clone https://github.com/Health-RI/FairDataPointSchemaTool.git
    cd FairDataPointSchemaTool/
    mvn install
```

## Usage

Run the tool with the required configuration file:

```sh
    java -jar FairDataPointSchemaTool-1.0.jar -i /path/to/Properties.yaml -h address_of_fdp -p yourpassword -u username -c command
```

-i path to Properties.yaml, you can use relative location (default is ./Properties.yaml) works if the property file is
located at the some location as the jar file.

-u fdp admin user (default: albert.einstein@example.com)

-p password (default: password)

-c Determine what the tool will do: we have 4 options:

* both -> Schema and resource will be updated. (default option)
* schema -> The schema will be updated
* resource -> Resource descriptions will be updated.
* files -> will create merged turtle files only, (this option is for internal use only.)

## Configuration File (YAML Format)

The tool requires a properties file in YAML format to specify input schemas and resource information.
Below is an example: For most cases the properties doesn't need to be updated.

```yaml
---
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
    - "Attribution.ttl"
    - "Identifier.ttl"
    - "QualityCertificate.ttl"
    - "Relationship.ttl"
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
  Data Service:
    - "DataService.ttl"
    - "Agent.ttl"
    - "Kind.ttl"
parentChild:
#Dataset, Catalog and Data Service all extend from the "Resource" schema.
  Resource:
    - "Dataset"
    - "Catalog"
    - "Data Service"
resources:
  Dataset Series:
    schema: "Dataset Series"
    parentResource: "Dataset"
    parentRelationIri: "http://www.w3.org/ns/dcat#inSeries"
  Sample Distribution:
    schema: "Distribution"
    parentResource: "Dataset"
    parentRelationIri: "http://www.w3.org/ns/adms#sample"
  Analytics Distribution:
    schema: "Distribution"
    parentResource: "Dataset"
    parentRelationIri: "http://healthdataportal.eu/ns/health#analytics"
#by default it fetches the shacl files from github develop branch, but you use local folder but you have to use URL encoding ("file:///path/to/folder/")
inputDir: "https://raw.githubusercontent.com/Health-RI/health-ri-metadata/develop/Formalisation(shacl)/Core/PiecesShape/"
outputDir: "C:\\Users\\PatrickDekker(Health\\IdeaProjects\\health-ri-metadata\\Formalisation(shacl)\\\
  Core\\FairDataPointShape"
schemasToPublish:
  - "Resource"
  - "Catalog"
  - "Dataset"
  - "Dataset Series"
  - "Distribution"
  - "Data Service"
#prefered version number of the schemas, only works if current version is smaller.
#if not,  the current version is used, but patch number is increased (2.0.0 -> 2.0.1)
schemaVersion: "2.0.0"

```

Note the "resourceToPublih" and "Schema" should be identical as the schema name it should replace on the Fair Data
Point! (including space & case)

## License

This project is licensed under the MIT License. See the LICENSE file for details.

## Contributing

Contributions are welcome! Please submit a pull request or open an issue for discussion.

