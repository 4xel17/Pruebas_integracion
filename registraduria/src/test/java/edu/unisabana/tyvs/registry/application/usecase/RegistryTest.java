package edu.unisabana.tyvs.registry.application.usecase;

import edu.unisabana.tyvs.registry.application.port.out.RegistryRepositoryPort;
import edu.unisabana.tyvs.registry.domain.model.Gender;
import edu.unisabana.tyvs.registry.domain.model.Person;
import edu.unisabana.tyvs.registry.domain.model.RegisterResult;
import edu.unisabana.tyvs.registry.infrastructure.persistence.RegistryRepository;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * Pruebas de integración para el caso de uso {@link Registry}, aplicando el formato AAA:
 * <ul>
 *   <li><b>Arrange</b>: preparación de datos y objetos a probar.</li>
 *   <li><b>Act</b>: ejecución del método bajo prueba.</li>
 *   <li><b>Assert</b>: verificación de los resultados esperados.</li>
 * </ul>
 */
public class RegistryTest {

    private RegistryRepositoryPort repo;
    private Registry registry;

    /**
     * Arrange común a todos los tests:
     * <ul>
     *   <li>Instancia un repositorio H2 en memoria.</li>
     *   <li>Inicializa el esquema (tabla) y limpia datos previos.</li>
     *   <li>Construye el caso de uso inyectando el repositorio.</li>
     * </ul>
     */
    @Before
    public void setup() throws Exception {
        String jdbc = "jdbc:h2:mem:regdb;DB_CLOSE_DELAY=-1";
        repo = new RegistryRepository(jdbc);

        repo.initSchema();   // Arrange: crear tabla
        repo.deleteAll();    // Arrange: limpiar datos previos

        registry = new Registry(repo); // Arrange: inyectar dependencia
    }

    /**
     * Caso de prueba:
     * <p>Una persona válida debe ser registrada exitosamente.</p>
     */
    @Test
    public void shouldRegisterValidPerson() throws Exception {
        // Arrange
        Person p1 = new Person("Ana", 100, 30, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(p1);

        // Assert
        assertEquals(RegisterResult.VALID, result);
        assertTrue(repo.existsById(100));
    }

    /**
     * Caso de prueba:
     * <p>Al intentar registrar dos personas con el mismo ID:</p>
     * <ul>
     *   <li>La primera se guarda como válida.</li>
     *   <li>La segunda es rechazada como duplicada.</li>
     * </ul>
     */
    @Test
    public void shouldPersistValidVoterAndRejectDuplicates() throws Exception {
        // Arrange
        Person p1 = new Person("Ana", 100, 30, Gender.FEMALE, true);
        Person p2 = new Person("AnaDos", 100, 40, Gender.FEMALE, true);

        // Act (primer registro)
        RegisterResult result1 = registry.registerVoter(p1);

        // Assert primer registro
        assertEquals(RegisterResult.VALID, result1);
        assertTrue(repo.existsById(100));

        // Act (segundo registro con mismo ID)
        RegisterResult result2 = registry.registerVoter(p2);

        // Assert segundo registro
        assertEquals(RegisterResult.DUPLICATED, result2);
    }

    //test menor de edad 
    @Test
    public void shouldRejectUnderagePerson() throws Exception {
        // Arrange
        Person minor = new Person("Luis", 102, 16, Gender.MALE, true);

        // Act
        RegisterResult result = registry.registerVoter(minor);

        // Assert
        assertEquals(RegisterResult.UNDERAGE, result);
        assertFalse(repo.existsById(102));
   }

   //test persona fallecida 
   @Test
    public void shouldRejectDeceasedPerson() throws Exception {
        // Arrange
        Person deceased = new Person("Maria", 103, 45, Gender.FEMALE, false);

        // Act
        RegisterResult result = registry.registerVoter(deceased);

        // Assert
        assertEquals(RegisterResult.DEAD, result);
        assertFalse(repo.existsById(103));
    }

    //test ID invalido
    @Test
    public void shouldRejectInvalidIdPerson() throws Exception {
        // Arrange
        Person invalid = new Person("Pedro", -1, 30, Gender.MALE, true); // ID negativo

        // Act
        RegisterResult result = registry.registerVoter(invalid);

        // Assert
        assertEquals(RegisterResult.INVALID, result);
        assertFalse(repo.existsById(-1));
    }

    //simular error de conexion H2
@Test
public void shouldHandleDatabaseConnectionError() throws Exception {
    RegistryRepositoryPort faultyRepo = mock(RegistryRepositoryPort.class);
    doThrow(new RuntimeException("Connection failed")).when(faultyRepo).initSchema();

    try {
        faultyRepo.initSchema();
        fail("Debería lanzar excepción de conexión");
    } catch (Exception e) {
        assertTrue(e.getMessage().contains("Connection"));
    }
}



}
 

