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
    java -jar target/FairDataPointSchemaTool-1.0-SNAPSHOT.jar -i /path/to/Properties.yaml -p yourpassword
```

## Configuration File (YAML Format)

The tool requires a properties file in YAML format to specify input directories, schemas, and FDP connection details.
Below is an example:

```yaml
---
inputDir: "C:\\Users\\PatrickDekker(Health\\IdeaProjects\\health-ri-metadata\\Formalisation(shacl)\\Core\\PiecesShape\\"
schemas:
  #here define the resource that will be uploaded to fdp, you don't have to update this..
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
#address of the fairdatapoint
fdpUrl: "http://localhost:80"
#username of the admin user, password is set in the command line.
fdpUsername: "albert.einstein@example.com"
#not used right now
parentChild:
  Resource:
    - "Dataset"
    - "Catalog"
#the following schema will be update/publish to the FDP. Note it will create a new version with only the patch number update
resourcesToPublish:
  - "Project"
  - "Study"
```

## License

This project is licensed under the MIT License. See the LICENSE file for details.

## Contributing

Contributions are welcome! Please submit a pull request or open an issue for discussion.

