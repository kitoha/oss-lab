package osslab.webflux.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class BookRouter {

    @Bean
    public RouterFunction<ServerResponse> routeBooks(BookHandler bookHandler) {
        return RouterFunctions.route()
                .path("/v2/books", builder -> builder
                        .POST("", bookHandler::createBook)
                        .GET("/{id}", bookHandler::getBook)
                        .GET("", bookHandler::getAllBooks)
                        .PUT("/{id}", bookHandler::updateBook)
                        .DELETE("/{id}", bookHandler::deleteBook)
                ).build();
    }
}
