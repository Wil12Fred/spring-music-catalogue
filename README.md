# spring-music-catalogue

[![CI](https://github.com/Wil12Fred/spring-music-catalogue/actions/workflows/ci.yml/badge.svg)](https://github.com/Wil12Fred/spring-music-catalogue/actions/workflows/ci.yml) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A small Spotify-like music catalogue — artists, albums, songs, playlists,
users and ratings — built with Spring Boot 1.4, Spring Data JPA, PostgreSQL
and JTwig server-side templates. The UI and code are in Spanish.

`spotify.pdf` is the project write-up that was handed in.

## Features

- List / detail / create pages for artists (`/artista`), albums (`/album`),
  songs (`/cancion`), playlists (`/playlist`), people (`/persona`), users
  (`/usuario`), administrators (`/administrador`) and ratings (`/Valoracion`).
- A top-ten of the most played songs (`/top-ten`).
- A login form on the home page (`/validar-usuario?name=&password=`).
- Inactive artists, songs and playlists are hidden from the lists; a song
  name is unique (a second song with the same name is not created).
- Tables are created by Hibernate (`spring.jpa.hibernate.ddl-auto=update`).

## Requirements

- JDK 8 (what CI builds and tests with; Spring Boot 1.4.2 predates Java 9+).
  - Debian/Ubuntu: `sudo apt install openjdk-8-jdk` (or a Temurin 8 build
    where the distribution no longer ships 8)
  - Arch: `sudo pacman -S jdk8-openjdk`
- Maven is not needed: the Maven Wrapper (`./mvnw`) downloads Maven 3.9.9.
- PostgreSQL with a database named `IS1`
  (`postgresql` on Debian/Ubuntu and Arch).

Database settings come from the environment (see `.env.example`):

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/IS1` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | *(empty)* |

## Build

```sh
./mvnw -B package -DskipTests        # target/app-0.0.1-SNAPSHOT.jar
```

## Run

```sh
createdb IS1
export DB_PASSWORD=...
./mvnw spring-boot:run               # or: java -jar target/app-0.0.1-SNAPSHOT.jar
```

Run it from the repository root: the templates are served from
`src/main/webapp` and are not packaged into the jar. CI starts the packaged
app against PostgreSQL 16 after the tests; captured from that run:

```
INFO 2828 --- [main] app.SpotifyApplication : Started SpotifyApplication in 3.236 seconds (JVM running for 3.468)
$ curl -s -o /dev/null -w "GET / -> %{http_code}\n" http://localhost:8080/
GET / -> 200
$ curl -s -X POST -d nombre=Soda+Stereo -o /dev/null -w "POST /artista -> %{http_code}\n" http://localhost:8080/artista
POST /artista -> 200
$ curl -s http://localhost:8080/artista | grep -o "<td>Soda Stereo</td>"
<td>Soda Stereo</td>
```

## Tests

```sh
./mvnw -B verify                     # needs the PostgreSQL database above
```

30 tests, each in a transaction that is rolled back:

- `ControladorTests` — the controllers through MockMvc with the JTwig views
  rendered: home page; artist list, detail, creation and form; album, song,
  person, user and playlist lists show their rows; the top ten comes most
  played first; editing a song stores the change; a second song with the
  same name is not created; the administrator form creates one; the ratings
  list renders; login with a right and a wrong password.
- `RepositorioTests` — the Spring Data queries: find by id, name and date,
  inactive rows excluded, albums by date / state / artist name return albums,
  song state updates, songs ordered by plays (unplayed last), user login,
  a user's active playlists.
- `SpotifyApplicationTests` — the original repository tests.

CI (GitHub Actions) runs them on JDK 8 against a `postgres:16` service, then
starts the jar and requests pages as shown above.

## Project layout

```
src/main/java/app/
  SpotifyApplication.java     Spring Boot entry point
  configuracion/              JTwig view resolver
  controlador/                MVC controllers, one per entity
  dominio/                    JPA entities (Artista, Album, Cancion, Playlist, ...)
  repositorio/                Spring Data repositories
  servicio/                   services used by the controllers
src/main/webapp/WEB-INF/views/   *.twig.html templates
src/test/java/app/            controller and repository tests
mvnw, .mvn/wrapper/           Maven Wrapper
```

## Known limitations

- **Old stack**: Spring Boot 1.4.2 (end of life), Hibernate 5.0 and JTwig 3
  (`com.lyncode`, unmaintained), on JDK 8. The PostgreSQL JDBC driver is
  pinned to 42.7 because the Boot 1.4 default (9.4) cannot authenticate to
  PostgreSQL 14+ (SCRAM-SHA-256).
- Passwords are stored and compared in plain text; there is no session or
  access control — the login only renders the user page.
- The edit forms post a hidden `id` field that the entities do not bind
  (their setters are `setIdCancion`, `setIdAdministrador`, ...), so editing
  from the browser creates a new row (or, for songs, does nothing).
- There is no rating form (`/agregar-Valoracion` has no template), and a
  rating needs a user id that the entity has no setter for.
- The executable jar does not contain the templates; run it from the
  repository root.
- Pages load Bootstrap, jQuery and images from public CDNs over plain HTTP
  in several places.

## Background

Final project of a software engineering course at a university (2016), by
Wilber Cutire Sivincha, Ximena Pocco Lozada, Luis Tong Chabes, Sergio
Valdivia Santisteban and Karelia Vilca Salinas.

## License

MIT — see [LICENSE](LICENSE).
