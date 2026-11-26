# IS1-Spotify – music catalogue web app (Spring Boot)

Final project for **Ingeniería de Software I** (2016-II) at Universidad Católica
San Pablo: a small Spotify-like catalogue built with Spring Boot 1.4,
Spring Data JPA, PostgreSQL and JTwig templates. The UI and code are in Spanish.

It provides list / detail / create pages for:

- artists (`/artista`), albums (`/album`), songs (`/cancion`), playlists (`/playlist`)
- people, users and administrators (`/persona`, `/usuario`, `/administrador`)
- song ratings (`/Valoracion`) and a top-ten list (`/top-ten`)

`spotify.pdf` is the project write-up that was handed in.

Team: Wilber Cutire Sivincha, Ximena Pocco Lozada, Luis Tong Chabes,
Sergio Valdivia Santisteban, Karelia Vilca Salinas.

## Build status

**BUILD NOT VERIFIED** – the project has no Maven wrapper and Maven is not
installed on the machine where this cleanup was done, so neither the compile nor
the tests were run. Known risk found by reading `pom.xml`: the `postgresql`
dependency has its version written in the `<scope>` element
(`<scope>9.1-901-1.jdbc4</scope>`); if Maven rejects it, change it to
`<scope>runtime</scope>` (the version is managed by the Spring Boot parent).

## Requirements

- JDK 8 (Spring Boot 1.4.2)
- Maven 3
- PostgreSQL with a database named `IS1`; tables are created by Hibernate
  (`spring.jpa.hibernate.ddl-auto=update`)

## Configuration

Database settings come from the environment (defaults in brackets), see
`.env.example`:

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/IS1` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | *(empty)* |

## Run

```sh
createdb IS1
export DB_PASSWORD=...
mvn spring-boot:run        # http://localhost:8080/
mvn test                   # the tests also need the PostgreSQL database
```

## Layout

```
src/main/java/app/
  SpotifyApplication.java     Spring Boot entry point
  configuracion/              JTwig view resolver
  controlador/                MVC controllers, one per entity
  dominio/                    JPA entities (Artista, Album, Cancion, Playlist, ...)
  repositorio/                Spring Data repositories
  servicio/                   services used by the controllers
src/main/webapp/WEB-INF/views/   *.twig.html templates
src/test/java/app/SpotifyApplicationTests.java   repository tests against the DB
```
