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

aca se puede evidenciar codigo :
```

---

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
