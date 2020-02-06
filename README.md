# prosper-1099-parser
This is a simple project to convert PDF files containing Prosper 1099-B transactions to CSV format.

IRS guidelines require transactions for which the cost basis was **not** reported to the IRS to be included as separate line items on Form 8949. Prosper 1099-B transactions fall into this category.

This project can be used to convert the transactions into CSV format that can then be copied and written to a CSV file. The CSV file can then be a) imported into a tax program able to automatically generate Form 8949 (e.g. TaxAct), or b) imported into a spreadsheet to generate a statement as a substitute for Form 8949 that can be attached with Form 8453, or c) given to a tax professional that will know what to do with it.

### Supported Tax Years
- [2017]
- [2018]
- [2019]

### Releases
* v1.0.0 - Supports tax years 2017 and 2018
* v1.1.1 - Supports tax years 2017, 2018, and 2019

### Prerequisites
- [JDK 1.8] is installed
- An internet connection is required to download gradle dependencies

### Steps to run:
1. Download/clone the project. 
1. Open the command prompt and `cd` to the root directory of the project.
1. Run the command `./gradlew clientInstall bootRun` to download client dependencies and start the service (on Windows use `gradlew.bat` instead of `./gradlew`).
1. Browse to: http://localhost:8080

### Tech Used
* [Spring Boot]
* [Gradle]
* [Apache PDFBox]
* [Apache Commons CSV]
* [Bower]
* [Spring Boot Asset Pipeline Gradle Adapter]
* [Client Dependencies Gradle Plugin]
* [JQuery]
* [Materialize]
* [Bootstrap]
* [SLF4J]
* [TestNG]

[2017]: https://www.irs.gov/pub/irs-prior/i8949--2017.pdf
[2018]: https://www.irs.gov/pub/irs-prior/i8949--2018.pdf
[2019]: https://www.irs.gov/pub/irs-pdf/i8949.pdf
[JDK 1.8]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[Spring Boot]: http://projects.spring.io/spring-boot/
[Gradle]: http://gradle.org/
[Apache PDFBox]: https://pdfbox.apache.org/
[Apache Commons CSV]: https://commons.apache.org/proper/commons-csv/
[Bower]: https://github.com/bower/bower
[Spring Boot Asset Pipeline Gradle Adapter]: https://github.com/bertramdev/asset-pipeline/tree/master/asset-pipeline-spring-boot    
[Client Dependencies Gradle Plugin]: https://github.com/craigburke/client-dependencies-gradle
[JQuery]: https://github.com/jquery/jquery
[Materialize]: https://github.com/Dogfalo/materialize
[Bootstrap]: https://github.com/twbs/bootstrap
[SLF4J]: https://github.com/qos-ch/slf4j
[TestNG]: http://github.com/cbeust/testng/
