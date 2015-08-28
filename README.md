# Entrada Health


## Develop
This project is Migrated toAndroid Studio on 17/07/2015 and development continued on Android studio.


## Build
Builds are done using Gradle.

    ./gradlew clean build

There are several build variants, to see all available build tasks use:

    ./gradlew tasks

## Build Variants
There are only two build variants. i,e development/debug and release.
  See EntradaHealth/build.gradle

## Developer Orientation
Singletons are typically accessed via EntradaApplication.
The most important one is Database, it provides access to the local sqlite databases. and H2 database was used for security.

### ServerConfig
Most configuration data is fetched from the server and stored in APISerivce.

### Scheudle Module:

Include the below functionality.

Resource Names Popup:
1. User select any resource names from the popup and click on “Done” button, the schedule list will populated with appointments for the selected resources for today. if they don’t have any appointments, an empty list will be displayed.

Schedule appointment List:
1. Displays the schedules for selected date.

Calendar:
1. User able to select the different date from calendar and view the schedules.

Filter:
1. Filter the schedules whose appointment status is "200"

Search:
1. Filter the Schedules based on the Patient Name and MRN.


