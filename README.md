# AtletismoAZN API

API REST para la gestión de atletismo desarrollada con Spring Boot. Esta aplicación proporciona endpoints para manejar recursos relacionados con el atletismo.

## 🚀 Tecnologías

- **Java** - Lenguaje de programación principal
- **Spring Boot** - Framework para desarrollo de aplicaciones Java
- **Maven** - Gestión de dependencias y construcción del proyecto
- **JUnit 5** - Framework de testing
- **Mockito** - Mocking para tests unitarios
- **Jackson** - Serialización/deserialización JSON

## 📋 Requisitos

- Java 8 o superior
- Maven 3.6 o superior

## 🛠️ Instalación

1. Clona el repositorio:
```bash
git clone https://github.com/Davidnd99/atletismoAZN_API.git
cd atletismoAZN_API
```

2. Compila el proyecto:
```bash
mvn clean compile
```

3. Ejecuta los tests:
```bash
mvn test
```

4. Ejecuta la aplicación:
```bash
mvn spring-boot:run
```

5. La API estará disponible en `http://localhost:8080` y podrás acceder a los endpoints definidos usando Swagger UI en `http://localhost:8080/swagger-ui.html`.

## 📁 Estructura del Proyecto
```arduino
RunningApp/
├─ pom.xml                     # POM raíz (aggregator): módulos, versiones, plugins comunes (JaCoCo, etc.)
├─ running-boot/
│  ├─ pom.xml                  # POM del arranque Spring Boot
│  └─ src/
│     ├─ main/
│     │  ├─ java/
│     │  │  └─ com/running/service/boot/
│     │  │     ├─ RunningServiceBoot.java     # Clase @SpringBootApplication
│     │  │     ├─ config/…                    # Configuración (Beans, Jackson, etc.)
│     │  │     └─ security/…                  # (Opcional) Seguridad
│     │  └─ resources/
│     │     ├─ application.yml
│     │     ├─ banner.txt
│     │     └─ firebase/…                     # Credenciales/config Firebase
│     └─ test/
│        └─ java/…                            # Tests de integración del arranque (si los hay)
│
├─ running-core/
│  ├─ pom.xml
│  └─ src/
│     ├─ main/
│     │  └─ java/com/running/
│     │     ├─ model/…                        # Entidades JPA y DTOs
│     │     ├─ repository/…                   # Repositorios Spring Data
│     │     └─ service/…                      # Servicios de dominio (lógica de negocio)
│     └─ test/
│        └─ java/com/running/
│           ├─ service/…                      # Unit tests de servicios puros
│           └─ repository/…                   # Tests de repos (si aplican con @DataJpaTest)
│
├─ running-endpoint/
│  ├─ pom.xml
│  └─ src/
│     ├─ main/
│     │  └─ java/com/running/endpoint/api/…   # Controladores REST (@RestController)
│     └─ test/
│        └─ java/com/running/endpoint/api/…   # Unit tests de controladores (MockMvc standalone/@WebMvcTest)
│
```


