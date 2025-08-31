package osslab.webflux.service;

import org.springframework.stereotype.Service;
import osslab.webflux.dto.BookDto;
import osslab.webflux.entity.BookEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BookService {

    private final Map<String, BookEntity> bookDatabase = new ConcurrentHashMap<>();

    public Mono<BookDto> createBook(Mono<BookDto> bookDtoMono) {
        return bookDtoMono.map(bookDto -> {
            String id = UUID.randomUUID().toString();
            BookEntity bookEntity = BookEntity.builder()
                    .id(id)
                    .title(bookDto.getTitle())
                    .author(bookDto.getAuthor())
                    .build();
            bookDatabase.put(id, bookEntity);
            return BookMapper.entityToDto(bookEntity);
        });
    }

    public Mono<BookDto> findBookById(String id) {
        return Mono.justOrEmpty(bookDatabase.get(id))
                .map(BookMapper::entityToDto);
    }

    public Flux<BookDto> findAllBooks() {
        return Flux.fromIterable(bookDatabase.values())
                .map(BookMapper::entityToDto);
    }

    public Mono<BookDto> updateBook(String id, Mono<BookDto> bookDtoMono) {
        return bookDtoMono.flatMap(bookDto -> {
            if (bookDatabase.containsKey(id)) {
                BookEntity updatedEntity = BookEntity.builder()
                        .id(id)
                        .title(bookDto.getTitle())
                        .author(bookDto.getAuthor())
                        .build();
                bookDatabase.put(id, updatedEntity);
                return Mono.just(BookMapper.entityToDto(updatedEntity));
            } else {
                return Mono.empty();
            }
        });
    }

    public Mono<Void> deleteBook(String id) {
        return Mono.fromRunnable(() -> bookDatabase.remove(id));
    }
}
