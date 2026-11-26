package app;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import app.dominio.Album;
import app.dominio.Artista;
import app.dominio.Cancion;
import app.dominio.Persona;
import app.dominio.Playlist;
import app.dominio.Usuario;
import app.repositorio.AdministradorRepositorio;
import app.repositorio.AlbumRepositorio;
import app.repositorio.ArtistaRepositorio;
import app.repositorio.CancionRepositorio;
import app.repositorio.PersonaRepositorio;
import app.repositorio.PlaylistRepository;
import app.repositorio.UsuarioRepositorio;

/** The MVC controllers through MockMvc, rendering the JTwig views; each test rolls back. */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ControladorTests {

	@Autowired MockMvc mvc;
	@Autowired ArtistaRepositorio artistas;
	@Autowired AlbumRepositorio albums;
	@Autowired CancionRepositorio canciones;
	@Autowired AdministradorRepositorio administradores;
	@Autowired PersonaRepositorio personas;
	@Autowired UsuarioRepositorio usuarios;
	@Autowired PlaylistRepository playlists;

	Artista artista(String nombre) {
		Artista a = new Artista();
		a.setNombre(nombre);
		return artistas.save(a);
	}

	Cancion cancion(String nombre, Integer reproducciones) {
		Cancion c = new Cancion();
		c.setNombre(nombre);
		c.setNumReproducciones(reproducciones);
		c.setFecha(new Date());
		return canciones.save(c);
	}

	@Test
	public void homePage() throws Exception {
		mvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("home"));
	}

	@Test
	public void artistListShowsTheArtists() throws Exception {
		artista("Soda Stereo");
		mvc.perform(get("/artista"))
				.andExpect(status().isOk())
				.andExpect(view().name("artistas"))
				.andExpect(content().string(containsString("Soda Stereo")));
	}

	@Test
	public void artistDetail() throws Exception {
		Artista a = artista("Los Prisioneros");
		mvc.perform(get("/artista").param("id", a.getId().toString()))
				.andExpect(status().isOk())
				.andExpect(view().name("artista"))
				.andExpect(model().attribute("artista", hasProperty("nombre", is("Los Prisioneros"))))
				.andExpect(content().string(containsString("Los Prisioneros")));
	}

	@Test
	public void createArtist() throws Exception {
		mvc.perform(post("/artista").param("nombre", "Libido"))
				.andExpect(status().isOk())
				.andExpect(view().name("artista"))
				.andExpect(model().attribute("artista", hasProperty("nombre", is("Libido"))));
		assertEquals(1, artistas.buscarPorNombre("Libido").size());
	}

	@Test
	public void newArtistForm() throws Exception {
		mvc.perform(get("/agregar-artista"))
				.andExpect(status().isOk())
				.andExpect(view().name("agregar-artista"));
	}

	@Test
	public void albumListShowsTheAlbums() throws Exception {
		Album al = new Album();
		al.setNombre("Signos");
		al.setArtista(artista("Soda Stereo"));
		albums.save(al);
		mvc.perform(get("/album"))
				.andExpect(status().isOk())
				.andExpect(view().name("albums"))
				.andExpect(content().string(containsString("Signos")));
	}

	@Test
	public void songListShowsTheSongs() throws Exception {
		cancion("De musica ligera", 3);
		mvc.perform(get("/cancion"))
				.andExpect(status().isOk())
				.andExpect(view().name("canciones"))
				.andExpect(content().string(containsString("De musica ligera")));
	}

	@Test
	public void topTenIsMostPlayedFirst() throws Exception {
		cancion("poco", 1);
		cancion("mucho", 100);
		cancion("medio", 10);
		mvc.perform(get("/top-ten"))
				.andExpect(status().isOk())
				.andExpect(view().name("canciones"))
				.andExpect(model().attribute("canciones", contains(
						hasProperty("nombre", is("mucho")),
						hasProperty("nombre", is("medio")),
						hasProperty("nombre", is("poco")))));
	}

	@Test
	public void editingASongSavesTheChange() throws Exception {
		Cancion c = cancion("Persiana americana", 1);
		mvc.perform(post("/cancion")
				.param("idCancion", c.getIdCancion().toString())
				.param("nombre", "Persiana americana")
				.param("numReproducciones", "42"))
				.andExpect(status().isOk())
				.andExpect(view().name("cancion"));
		assertEquals(Integer.valueOf(42), canciones.buscarPorId(c.getIdCancion()).getNumReproducciones());
	}

	@Test
	public void aSecondSongWithTheSameNameIsNotCreated() throws Exception {
		cancion("Unica", 1);
		mvc.perform(post("/cancion").param("nombre", "Unica"))
				.andExpect(status().isOk());
		assertEquals(1, canciones.buscarTodos().size());
	}

	@Test
	public void createAdministratorFromItsForm() throws Exception {
		int antes = administradores.buscarTodos().size();
		mvc.perform(post("/administrador"))
				.andExpect(status().isOk())
				.andExpect(view().name("administradores"));
		assertEquals(antes + 1, administradores.buscarTodos().size());
	}

	@Test
	public void ratingsList() throws Exception {
		mvc.perform(get("/Valoracion"))
				.andExpect(status().isOk())
				.andExpect(view().name("valoraciones"));
	}

	@Test
	public void personUserAndPlaylistLists() throws Exception {
		Persona p = new Persona();
		p.setNombre("Gustavo");
		personas.save(p);
		Usuario u = new Usuario();
		u.setNickname("gcerati");
		usuarios.save(u);
		Playlist pl = new Playlist();
		pl.setNombre("Favoritas");
		pl.setUsuario(u);
		playlists.save(pl);
		mvc.perform(get("/persona")).andExpect(status().isOk())
				.andExpect(view().name("personas"))
				.andExpect(content().string(containsString("Gustavo")));
		mvc.perform(get("/usuario")).andExpect(status().isOk())
				.andExpect(view().name("usuarios"))
				.andExpect(content().string(containsString("gcerati")));
		mvc.perform(get("/playlist")).andExpect(status().isOk())
				.andExpect(view().name("playlists"))
				.andExpect(content().string(containsString("Favoritas")));
	}

	@Test
	public void loginFromTheHomePage() throws Exception {
		Usuario u = new Usuario();
		u.setNickname("ana");
		u.setContrasena("secreto");
		usuarios.save(u);
		mvc.perform(get("/validar-usuario").param("name", "ana").param("password", "secreto"))
				.andExpect(status().isOk())
				.andExpect(view().name("usuario"))
				.andExpect(model().attribute("usuario", hasProperty("nickname", is("ana"))));
		mvc.perform(get("/validar-usuario").param("name", "ana").param("password", "mal"))
				.andExpect(status().isOk())
				.andExpect(view().name("home"));
	}
}
