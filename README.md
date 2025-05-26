# SaarniLearningAssignment

**Language:** English | Suomi (see below)

---

## Overview

This Spring Boot application processes CSV files containing course completion data. It summarizes the results into structured JSON files for further use or reporting.

- Supports manual file drops and REST-based uploads
- Automatically parses, validates, and summarizes course data
- Generates JSON reports (per user, course, and result)
- Invalid files are moved to an error folder

---

## How to Run the Application

```bash
mvn clean install
mvn spring-boot:run
```

The app creates and uses the following folders:

| Folder            | Purpose                         |
|-------------------|---------------------------------|
| data/input        | Drop CSV files here             |
| data/done         | Successfully processed files    |
| data/output       | JSON summary folders            |
| data/error        | Files that failed validation    |

These are auto-created on startup if missing.

---

## Uploading a File

### Via Swagger UI

Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) and use the POST /v1/records endpoint to upload a CSV file.

### Manually

Copy any .csv file into the data/input/ directory. The app checks for new files every 10 seconds.

---

## Example CSV Format

```csv
Etunimi,Sukunimi,E-mail,Kurssin nimi,Kurssi alkaa,Kurssi päättyy,Status,Arvosana,Kurssin suorituspäivämäärä
esimerkki,esimerkkinen,esimerkki@example.com,esimerkki.nyt,2022-10-18,2022-12-31,completed,1,2022-10-22
```

---

## JSON Output Structure

Each processed CSV generates a folder inside data/output/ containing:

- courses.json – Summarized course info
- users.json – Per-user completion statistics
- course_results.json – Flattened list of individual results

---

## Requirements

To build and run this application, ensure you have the following installed:

### Java
- Java 21 (LTS)
- Use `java -version` to confirm

### Build Tool
- Apache Maven 3.9+
- Use `mvn -version` to confirm

### Frameworks
- Spring Boot 3.5.0
  - Compatible with Jakarta EE 10
  - Used with modules: web, data-jpa, devtools, etc.
- Jackson Databind (for JSON serialization)
- SpringDoc OpenAPI 2.1.0 (for Swagger UI)

### Dependencies
All dependencies are declared in pom.xml and automatically fetched by Maven.

---

## SAARNILEARNINGASSIGNMENT (Suomeksi)

---

## Yleiskuvaus

Spring Boot -sovellus, joka lukee kurssisuorituksia CSV-tiedostoista ja tuottaa yhteenvetoja JSON-muodossa.

- Tukee tiedoston pudottamista kansioon tai lähettämistä REST-rajapinnan kautta
- Jäsentää ja suodattaa tiedot automaattisesti
- Luo kolmenlaisia JSON-raportteja
- Siirtää virheelliset tiedostot erilliseen virhekansioon

---

## Sovelluksen käynnistys

```bash
mvn clean install
mvn spring-boot:run
```

Sovellus luo seuraavat kansiot:

| Kansio           | Tarkoitus                        |
|------------------|----------------------------------|
| data/input       | Tänne pudotetaan CSV-tiedostot   |
| data/done        | Onnistuneesti käsitellyt         |
| data/output      | JSON-muotoiset tulosteet         |
| data/error       | Virheelliset tiedostot           |

---

## Tiedoston lähetys

### Swagger-käyttöliittymä

Avaa [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) ja käytä POST /v1/records rajapintaa.

### Manuaalisesti

Pudota CSV-tiedosto data/input-kansioon. Sovellus tarkistaa kansion 10 sekunnin välein.

---

## CSV Esimerkki

```csv
Etunimi,Sukunimi,E-mail,Kurssin nimi,Kurssi alkaa,Kurssi päättyy,Status,Arvosana,Kurssin suorituspäivämäärä
Elli,Hurlen,elli@example.com,Kiertotalous.nyt,2022-10-18,2022-12-31,completed,1,2022-10-22
```

---

## Tuotetut JSON-tiedostot

Käsitellystä CSV:stä syntyy alikansio data/output-kansioon. Se sisältää:

- courses.json – Kurssikohtainen yhteenveto
- users.json – Käyttäjäkohtainen yhteenveto
- course_results.json – Suorituskohtainen lista

---

## Asetukset

Voit muokata asetuksia tiedostossa:
```
src/main/resources/application.properties
```
