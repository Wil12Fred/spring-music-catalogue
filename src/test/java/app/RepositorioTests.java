package app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import app.dominio.Album;
import app.dominio.Artista;
import app.dominio.Cancion;
import app.dominio.Playlist;
import app.dominio.Usuario;
import app.repositorio.AlbumRepositorio;
import app.repositorio.ArtistaRepositorio;
import app.repositorio.CancionRepositorio;
import app.repositorio.PlaylistRepository;
import app.repositorio.UsuarioRepositorio;

/** Spring Data repositories against PostgreSQL; each test rolls back. */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class RepositorioTests {

	@Autowired ArtistaRepositorio artistas;
	@Autowired AlbumRepositorio albums;
	@Autowired CancionRepositorio canciones;
	@Autowired PlaylistRepository playlists;
	@Autowired UsuarioRepositorio usuarios;

	static Date dia(int anio) {
		return new GregorianCalendar(anio, Calendar.JANUARY, 1).getTime();
	}

	Artista artista(String nombre, boolean activo, Date fecha) {
		Artista a = new Artista();
		a.setNombre(nombre);
		a.setEstado(activo);
		a.setFecha(fecha);
		return artistas.save(a);
	}

	Album album(String nombre, Artista artista, boolean activo, Date fecha) {
		Album al = new Album();
		al.setNombre(nombre);
		al.setArtista(artista);
		al.setEstado(activo);
		al.setFecha(fecha);
		return albums.save(al);
	}

	Cancion cancion(String nombre, Integer reproducciones) {
		Cancion c = new Cancion();
		c.setNombre(nombre);
		c.setNumReproducciones(reproducciones);
		return canciones.save(c);
	}

	static List<Long> idsDeAlbums(List<?> lista) {
		List<Long> ids = new ArrayList<Long>();
		for (Object o : lista) {
			assertTrue("expected an Album, got " + o.getClass().getSimpleName(), o instanceof Album);
			ids.add(((Album) o).getId());
		}
		return ids;
	}

	@Test
	public void artistIsSavedAndFoundByIdAndName() {
		Artista a = artista("Soda Stereo", true, new Date());
		assertTrue(a.getId() != null);
		assertEquals("Soda Stereo", artistas.buscarPorId(a.getId()).getNombre());
		assertEquals(1, artistas.buscarPorNombre("Soda Stereo").size());
		assertTrue(artistas.buscarPorNombre("Nadie").isEmpty());
	}

	@Test
	public void inactiveArtistsAreNotListed() {
		Artista activo = artista("Activo", true, new Date());
		Artista inactivo = artista("Inactivo", false, new Date());
		List<Artista> todos = artistas.buscarTodos();
		assertTrue(todos.contains(activo));
		assertFalse(todos.contains(inactivo));
		assertFalse(artistas.buscarPorEstado().contains(inactivo));
	}

	@Test
	public void artistsByDate() {
		Artista viejo = artista("Viejo", true, dia(2000));
		Artista nuevo = artista("Nuevo", true, dia(2016));
		List<Artista> desde2010 = artistas.buscarPorFecha(dia(2010));
		assertTrue(desde2010.contains(nuevo));
		assertFalse(desde2010.contains(viejo));
	}

	@Test
	public void albumKeepsItsArtist() {
		Artista a = artista("Churrumino", true, new Date());
		Album al = album("larala larala", a, true, new Date());
		assertEquals("Churrumino", albums.buscarPorId(al.getId()).getArtista().getNombre());
		assertEquals(1, albums.buscarPorNombre("larala larala").size());
	}

	@Test
	public void albumsByDateAreAlbums() {
		Artista a = artista("Artista", true, dia(2016));
		Album viejo = album("Viejo", a, true, dia(2000));
		Album nuevo = album("Nuevo", a, true, dia(2016));
		List<Long> ids = idsDeAlbums(albums.buscarPorFecha(dia(2010)));
		assertTrue(ids.contains(nuevo.getId()));
		assertFalse(ids.contains(viejo.getId()));
	}

	@Test
	public void activeAlbumsAreAlbums() {
		Artista a = artista("Artista", true, new Date());
		Album activo = album("Activo", a, true, new Date());
		Album inactivo = album("Inactivo", a, false, new Date());
		List<Long> ids = idsDeAlbums(albums.buscarPorEstado());
		assertTrue(ids.contains(activo.getId()));
		assertFalse(ids.contains(inactivo.getId()));
	}

	@Test
	public void albumsOfAnArtistByPartOfItsName() {
		Artista a = artista("Churrumino", true, new Date());
		Album al = album("Uno", a, true, new Date());
		album("Otro", artista("Otro artista", true, new Date()), true, new Date());
		List<Long> ids = idsDeAlbums(artistas.buscarAlbums("urrum"));
		assertEquals(1, ids.size());
		assertEquals(al.getId(), ids.get(0));
	}

	@Test
	public void songStateIsUpdated() {
		Cancion c = cancion("XwdwD", 1);
		assertTrue(c.getIdCancion() != null);
		c.setEstado(false);
		canciones.save(c);
		assertFalse(canciones.buscarPorId(c.getIdCancion()).getEstado());
		assertFalse(canciones.buscarTodos().contains(c));
		assertEquals(c.getIdCancion(), canciones.buscarPorNombre("XwdwD").getIdCancion());
	}

	@Test
	public void topSongsComeMostPlayedFirst() {
		cancion("cinco", 5);
		cancion("cincuenta", 50);
		cancion("nunca", null);
		cancion("veinte", 20);
		List<String> nombres = new ArrayList<String>();
		for (Cancion c : canciones.cancionTop()) {
			nombres.add(c.getNombre());
		}
		assertEquals("cincuenta", nombres.get(0));
		assertEquals("veinte", nombres.get(1));
		assertEquals("cinco", nombres.get(2));
		assertEquals("nunca", nombres.get(3));
	}

	@Test
	public void userLoginNeedsTheRightPassword() {
		Usuario u = new Usuario();
		u.setNickname("ana");
		u.setContrasena("secreto");
		usuarios.save(u);
		assertEquals(u.getIdUsuario(), usuarios.validarLogin("ana", "secreto").getIdUsuario());
		assertNull(usuarios.validarLogin("ana", "otro"));
	}

	@Test
	public void activePlaylistsOfAUser() {
		Usuario u = new Usuario();
		u.setNickname("ana");
		usuarios.save(u);
		Playlist activa = new Playlist();
		activa.setNombre("Para correr");
		activa.setUsuario(u);
		playlists.save(activa);
		Playlist borrada = new Playlist();
		borrada.setNombre("Vieja");
		borrada.setUsuario(u);
		borrada.setEstado(false);
		playlists.save(borrada);
		List<Playlist> suyas = playlists.getUsuarioPlaylists(u.getIdUsuario());
		assertEquals(1, suyas.size());
		assertEquals("Para correr", suyas.get(0).getNombre());
		assertEquals("Para correr", playlists.buscarPorNombre("Para correr").getNombre());
	}
}
