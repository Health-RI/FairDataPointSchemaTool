# SchemaTools

## Overview

This Java tool will configure a Fair Data Point to use the Health-ri metamodel version 2.

## Features

- Reads SHACL files from a specified directory or github
- Use of excel template to create SHACL files.
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
    mvn install:install-file -Dfile=./xls2rdf-lib-3.2.1.jar -DgroupId=fr.sparna.rdf.xls2rdf -DartifactId=xls2rdf-pom -Dversion=3.2.1 -Dpackaging=jar 
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
* template -> will create Shacl files, from Excel templates (this option is for internal use only.)

## Configuration File (YAML Format)

The tool requires a properties file in YAML format to specify input schemas and resource information.
Below is an example: In most cases the property file doesn't need to be updated.

```yaml
---
---
schemas:
  #The FDP shape files are constructed using the files in the PiecesShape folder.
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
  Data Service:
    - "DataService.ttl"
    - "Agent.ttl"
    - "Kind.ttl"
# Some schemas are extending the Resource schema.
parentChild:
  Resource:
    - "Dataset"
    - "Catalog"
    - "Data Service"
#the resources for the FDP, note we are using Parent relation instead of Childeren (as the FDP does)
resources:
  Sample Distribution:
    parentResource: "Dataset"
    parentRelationIri: "http://www.w3.org/ns/adms#sample"
    schema: "Distribution"
  Dataset Series:
    parentResource: "Dataset"
    parentRelationIri: "http://www.w3.org/ns/dcat#inSeries"
    schema: "Dataset Series"
  Analytics Distribution:
    parentResource: "Dataset"
    parentRelationIri: "http://healthdataportal.eu/ns/health#analytics"
    schema: "Distribution"
schemasToPublish:
  - "Resource"
  - "Catalog"
  - "Dataset"
  - "Dataset Series"
  - "Distribution"
  - "Data Service"
schemaVersion: "2.0.0"
#Location of the shapes files used for uploading to FDP.
inputDir: "https://raw.githubusercontent.com/Health-RI/health-ri-metadata/v2.0.0/Formalisation(shacl)/Core/PiecesShape/"
#for use with the template option, uses Excel template to generate shapes files (Health-ri use only!)
templateDir: "C:\\Users\\PatrickDekker(Health\\templates\\"
outputRoot: "C:\\Users\\PatrickDekker(Health\\IdeaProjects\\health-ri-metadata\\Formalisation(shacl)\\\
  Core\\"
piecesDir: "PiecesShape"
fairDataPointDir: "FairDataPointShape"
validationDir: "ValidationShape"


```

Note schema names can have spaces, but the turtle filename will be without spaces.

## License

This project is licensed under the MIT License. See the LICENSE file for details.
This tool uses https://github.com/sparna-git/xls2rdf for conversion of excel templates to shacl.

## Contributing

Contributions are welcome! Please submit a pull request or open an issue for discussion.
