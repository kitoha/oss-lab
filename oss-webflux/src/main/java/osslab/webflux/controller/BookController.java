package osslab.webflux.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import osslab.webflux.dto.BookDto;
import osslab.webflux.service.BookService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BookDto> postBook(@RequestBody Mono<BookDto> bookDtoMono) {
        return bookService.createBook(bookDtoMono);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<BookDto>> getBook(@PathVariable String id) {
        return bookService.findBookById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Flux<BookDto> getBooks() {
        return bookService.findAllBooks();
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<BookDto>> updateBook(@PathVariable String id,
                                                    @RequestBody Mono<BookDto> bookDtoMono) {
        return bookService.updateBook(id, bookDtoMono)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteBook(@PathVariable String id) {
        return bookService.deleteBook(id);
    }
}
