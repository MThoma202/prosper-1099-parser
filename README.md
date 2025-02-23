# prosper-1099-parser
This is a simple project to convert PDF files containing Prosper 1099-B transactions to CSV format.

IRS guidelines require transactions for which the cost basis was **not** reported to the IRS to be included as separate line items on Form 8949. Prosper 1099-B transactions fall into this category.

This project can be used to convert the transactions into CSV format that can then be copied and written to a CSV file.

The CSV file can then be:
1. Imported into TaxAct to automatically generate Form 8949
    - [instructions here](https://www.taxact.com/support/881/2017/how-to-import-stock-information-using-a-csv-file-from-your-broker) to import the CSV file into TaxAct
2. Converted to TXF format and imported into TurboTax to automatically generate form 8949
    - [instructions here](https://github.com/MThoma202/prosper-1099-parser/issues/9) to convert from CSV to TXF format
    - [instructions here](https://ttlc.intuit.com/community/entering-importing/help/how-do-i-import-from-the-txf-file/00/25642) to import the TXF file into TurboTax
3. Imported into a spreadsheet to generate a statement as a substitute for Form 8949, that can be attached with [Form 8453](https://www.irs.gov/pub/irs-pdf/f8453.pdf)
4. Given to a tax professional that will know what to do with it

For option 1, use [this referral link](https://refer.taxact.com/s/MarkThomas14) to save 20% off TaxAct. This is the option I've been using for the last few years and it works great.
For option 2, use [this referral link](https://turbo.tax/tm7e7s3c) to save 20% off TurboTax.

### Supported Tax Years
- [2017]
- [2018]
- [2019]
- [2020]
- [2021]
- [2022]
- [2023]
- [2024]
- 2025+ (the parser should continue working for future years assuming the PDF file format doesn't change)

### Prerequisites
- [JDK 17] is installed
- An internet connection is required to download gradle dependencies

### Steps to run:
1. Download/clone the project. 
2. Open the command prompt and `cd` to the root directory of the project.
3. Run the command `./gradlew clientInstall bootRun` to download client dependencies and start the service (on Windows use `gradlew.bat` instead of `./gradlew`).
  - Alternatively, run the command `./gradlew clean build` to build the project, then `cd` to the build/libs directory and start the application using `java -jar *.jar --server.port=8080`
4. Browse to: http://localhost:8080

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

### Troubleshooting:
#### Application fails to start with error: The Tomcat connector configured to listen on port 8080 failed to start. The port may already be in use or the connector may be misconfigured.
This can happen when port 8080 is already in use by another application.
1. From the command prompt `cd` to the build/libs directory and start the application using `java -jar *.jar --server.port=8181` or some other unused port
2. Browse to http://localhost:8181 or put whatever port you used

[2017]: https://www.irs.gov/pub/irs-prior/i8949--2017.pdf
[2018]: https://www.irs.gov/pub/irs-prior/i8949--2018.pdf
[2019]: https://www.irs.gov/pub/irs-prior/i8949--2019.pdf
[2020]: https://www.irs.gov/pub/irs-prior/i8949--2020.pdf
[2021]: https://www.irs.gov/pub/irs-prior/i8949--2021.pdf
[2022]: https://www.irs.gov/pub/irs-prior/i8949--2022.pdf
[2023]: https://www.irs.gov/pub/irs-prior/i8949--2023.pdf
[2024]: https://www.irs.gov/pub/irs-prior/i8949--2024.pdf
[JDK 17]: https://adoptium.net/temurin/releases/?version=17
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
