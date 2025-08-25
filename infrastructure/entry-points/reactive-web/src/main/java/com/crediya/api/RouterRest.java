package com.crediya.api;

import com.crediya.api.config.UserPath;
import com.crediya.api.dto.CreateUserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springdoc.core.annotations.RouterOperation;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
@Tag(name = "User API", description = "Operaciones relacionadas con usuarios")
public class RouterRest {

    private final Handler userHandler;
    private final UserPath userPath;

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/usuarios",
                    produces = {"application/json"},
                    method = org.springframework.web.bind.annotation.RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "listenSaveUser",
                    operation = @Operation(
                            operationId = "createUser",
                            summary = "Crear un usuario",
                            description = "Crea un nuevo usuario en el sistema",
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "Información del usuario a crear",
                                    content = @Content(
                                            schema = @Schema(implementation = CreateUserDTO.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Usuario creado exitosamente",
                                            content = @Content(
                                                    schema = @Schema(implementation = CreateUserDTO.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Error de validación en los datos enviados",
                                            content = @Content(schema = @Schema(example = """
                                                    {
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "El correo electrónico no tiene un formato válido",
                                                      "path":  "/api/v1/usuarios"
                                                    }
                                                    """))
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Error interno del servidor",
                                            content = @Content(schema = @Schema(example = """
                                                    {
                                                      "status": 500,
                                                      "error": "Internal Server Error",
                                                      "message": "Ocurrió un error inesperado",
                                                      "path":  "/api/v1/usuarios"
                                                    }
                                                    """))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/usuarios",
                    produces = {"application/json"},
                    method = org.springframework.web.bind.annotation.RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "listenGetAllUsers",
                    operation = @Operation(
                            operationId = "getAllUsers",
                            summary = "Obtener todos los usuarios",
                            description = "Devuelve la lista de todos los usuarios registrados",
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Lista de usuarios obtenida exitosamente",
                                            content = @Content(
                                                    array = @ArraySchema(schema = @Schema(implementation = CreateUserDTO.class))
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Error interno del servidor",
                                            content = @Content(schema = @Schema(example = """
                                                    {
                                                      "status": 500,
                                                      "error": "Internal Server Error",
                                                      "message": "Ocurrió un error inesperado",
                                                      "path":  "/api/v1/usuarios"
                                                    }
                                                    """))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/usuarios",
                    produces = {"application/json"},
                    method = org.springframework.web.bind.annotation.RequestMethod.PUT,
                    beanClass = Handler.class,
                    beanMethod = "listenUpdateUser",
                    operation = @Operation(
                            operationId = "updateUser",
                            summary = "Actualizar usuario",
                            description = "Actualiza la información de un usuario existente",
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "Datos actualizados del usuario",
                                    content = @Content(
                                            schema = @Schema(implementation = CreateUserDTO.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Usuario actualizado exitosamente",
                                            content = @Content(
                                                    schema = @Schema(implementation = CreateUserDTO.class)
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Error de validación en los datos enviados",
                                            content = @Content(schema = @Schema(example = """
                                                    {
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "message": "Datos inválidos",
                                                      "path":  "/api/v1/usuarios"
                                                    }
                                                    """))
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Error interno del servidor",
                                            content = @Content(schema = @Schema(example = """
                                                    {
                                                      "status": 500,
                                                      "error": "Internal Server Error",
                                                      "message": "Ocurrió un error inesperado",
                                                      "path":  "/api/v1/usuarios"
                                                    }
                                                    """))
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction() {
        return route(POST(userPath.getUsers()), userHandler::listenSaveUser)
                .andRoute(GET(userPath.getUsers()), userHandler::listenGetAllUsers)
                .andRoute(PUT(userPath.getUsers()), userHandler::listenUpdateUser);
    }
}