# SchemaTools

## Overview

This Java tool reads multiple SHACL files, combines them, and uploads them to a Fair Data Point (FDP). The tool is built
using Maven and requires a YAML configuration file to specify input directories and schemas.

## Features

- Reads SHACL files from a specified directory
- Combines multiple SHACL files based on schema definitions
- Uploads the combined data to a Fair Data Point
- Configurable via a YAML properties file

## Requirements

- Java (JDK 11 or higher)
- Maven

## Installation

Clone the repository and build the project using Maven:

```sh
    git clone https://github.com/Health-RI/FairDataPointSchemaTool.git
    cd FairDataPointSchemaTool
    mvn clean install
```

## Usage

Run the tool with the required configuration file:

```sh
    java -jar target/FairDataPointSchemaTool-1.0-SNAPSHOT.jar -i /path/to/Properties.yaml -p yourpassword -c command
```

the -c command is determine if we update the Schema, Resources or both.

## Configuration File (YAML Format)

The tool requires a properties file in YAML format to specify input directories, schemas, and FDP connection details.
Below is an example: In most cases you can use the property file in the FairDataPointSchemaTool, but update InputDir,
fdpUrl en fdpUsername

```yaml
---
#Note this is the folder containing the 'core' shacls.
inputDir: "C:\\Users\\PatrickDekker(Health\\IdeaProjects\\health-ri-metadata\\Formalisation(shacl)\\\
  Core\\PiecesShape\\"
fdpUrl: "http://localhost:80"
fdpUsername: "albert.einstein@example.com"
#this section describes what "core" shacl files are needed for this schema. 
schemas:
  Project:
    - "Project.ttl"
    - "Agent.ttl"
  Study:
    - "Study.ttl"
  Dataset:
    - "Dataset.ttl"
    - "Agent.ttl"
    - "Kind.ttl"
    - "PeriodOfTime.ttl"
  Distribution:
    - "Distribution.ttl"
    - "PeriodOfTime.ttl"
    - "Checksum.ttl"
  Resource:
    - "Resource.ttl"
  Catalog:
    - "Catalog.ttl"
    - "Agent.ttl"
    - "Kind.ttl"
    - "PeriodOfTime.ttl"
  DatasetSeries:
    - "DatasetSeries.ttl"
    - "Agent.ttl"
    - "Kind.ttl"
  DataService:
    - "DataService.ttl"
    - "Agent.ttl"
    - "Kind.ttl"
parentChild:
  #Resource schema is parent for the following schema's
  Resource:
    - "Dataset"
    - "Catalog"
    - "DataService"
    - "Project"
    - "Study"
resourcesToPublish:
  - "Resource"
  - "Catalog"
  - "Dataset"
  - "DatasetSeries"
  - "Distribution"
  - "DataService"
  - "Project"
  - "Study"
```

## License

This project is licensed under the MIT License. See the LICENSE file for details.

## Contributing

Contributions are welcome! Please submit a pull request or open an issue for discussion.

