por: Axel Bedoya y Sara Zuluaga
## Descripción General

Este proyecto implementa un **sistema de registro de personas** para fines de votación. Permite registrar personas, validar su elegibilidad y manejar posibles errores como duplicados, menores de edad, personas fallecidas o identificaciones inválidas.

Se aplican **pruebas de integración y unitarias** utilizando **JUnit 4** y una base de datos **H2 en memoria**, asegurando que el sistema funcione correctamente antes de desplegarlo.

---

## Tecnologías Utilizadas

* **Java 17**: lenguaje principal del proyecto.
* **Maven**: gestión de dependencias y ejecución de builds/tests.
* **Spring Boot 2.7.18**: para la capa web y facilidad de pruebas.
* **JUnit 4**: pruebas unitarias y de integración.
* **Mockito**: creación de mocks para pruebas.
* **H2 Database**: base de datos en memoria para pruebas de integración.
* **JaCoCo**: cobertura de pruebas opcional.
* **Lombok**: para reducir código boilerplate.

---

## Estructura del Proyecto

```
src/
├─ main/
│  ├─ java/edu/unisabana/tyvs/registry/
│  │  ├─ application/usecase/Registry.java
│  │  ├─ domain/model/Person.java
│  │  ├─ domain/model/RegisterResult.java
│  │  └─ infrastructure/persistence/RegistryRepository.java
│  └─ resources/
└─ test/
   ├─ java/edu/unisabana/tyvs/registry/application/usecase/
   │  ├─ RegistryTest.java
   │  ├─ RegistryWithMockTest.java
   │  └─ AppTest.java
   └─ resources/


```

---

## el codigo se ve asi: 
```
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
```


## Casos de Uso

1. **Registrar persona válida**

   * Una persona con edad válida, viva y con identificación correcta se registra correctamente.

2. **Evitar duplicados**

   * No se permite registrar dos personas con la misma identificación.

3. **Rechazar menores de edad**

   * Personas menores de 18 años no pueden registrarse.

4. **Rechazar personas fallecidas**

   * Personas marcadas como fallecidas no pueden registrarse.

5. **Rechazar identificaciones inválidas**

   * Identificaciones negativas o nulas no son válidas.

6. **Simulación de error de conexión a la base de datos**

   * El sistema maneja correctamente fallos de conexión a la base de datos.

---

## Ejecución de Pruebas

Para correr las pruebas:

```bash
mvn test
```

* Todos los tests unitarios y de integración se ejecutan automáticamente.
* La base de datos H2 se inicializa en memoria para cada ejecución de test.

---

## Resultados de Pruebas

* **Tests ejecutados**: 8
* **Éxitos**: 8
* **Fallos**: 0
* **Errores**: 0
<img width="1523" height="824" alt="Captura de pantalla 2025-11-16 185243" src="https://github.com/user-attachments/assets/9b7f184f-040e-4281-b683-07e78e3f64a5" />

---
