package osslab.webflux.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import osslab.webflux.dto.BookDto;
import osslab.webflux.service.BookService;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class BookHandler {
    private final BookService bookService;

    public Mono<ServerResponse> createBook(ServerRequest request) {
        Mono<BookDto> bookDtoMono = request.bodyToMono(BookDto.class);
        return bookService.createBook(bookDtoMono)
                .flatMap(book -> ServerResponse.created(URI.create("/v2/books/" + book.getId())).bodyValue(book));
    }

    public Mono<ServerResponse> getBook(ServerRequest request) {
        String id = request.pathVariable("id");
        return bookService.findBookById(id)
                .flatMap(book -> ServerResponse.ok().bodyValue(book))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getAllBooks(ServerRequest request) {
        return ServerResponse.ok().body(bookService.findAllBooks(), BookDto.class);
    }

    public Mono<ServerResponse> updateBook(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<BookDto> bookDtoMono = request.bodyToMono(BookDto.class);
        return bookService.updateBook(id, bookDtoMono)
                .flatMap(book -> ServerResponse.ok().bodyValue(book))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteBook(ServerRequest request) {
        String id = request.pathVariable("id");
        return bookService.deleteBook(id)
                .then(ServerResponse.noContent().build());
    }
}
