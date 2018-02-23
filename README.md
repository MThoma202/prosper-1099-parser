# prosper-1099-parser
This is a simple project to convert PDF files containing Prosper 1099-B transactions to CSV format.

### Prerequisites
- [JDK 1.8]
- An internet connection to download gradle dependencies

### Steps to run:
1. Download/clone the project. 
1. Open command prompt and go to the root location of the downloaded project.
1. Run the command `./gradlew bootRun` to start the service
1. HTTP POST a PDF file to:  `http://localhost:8080/convertPdfToCsv`

E.g.
```
curl --request POST \
  --url http://localhost:8080/convertPdfToCsv \
  --header 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
  --form file=@/path/2017_Prosper_1099-B.pdf
```

### Optional query params:
Query Param|Default Value|Description
---|---|---
includeShortTerm|true|Set to false to exclude short-term transactions from the CSV.
includeLongTerm|true|Set to false to exclude long-term transactions from the CSV.

### Tech
* [Spring Boot]
* [Gradle]
* [Apache PDFBox]
* [Apache Commons CSV]

License
----
MIT

[JDK 1.8]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[Spring Boot]: http://projects.spring.io/spring-boot/
[Gradle]: http://gradle.org/
[Apache PDFBox]: https://pdfbox.apache.org/
[Apache Commons CSV]: https://commons.apache.org/proper/commons-csv/
