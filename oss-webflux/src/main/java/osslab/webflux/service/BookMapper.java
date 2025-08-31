package osslab.webflux.service;

import osslab.webflux.dto.BookDto;
import osslab.webflux.entity.BookEntity;

public class BookMapper {

    public static BookEntity dtoToEntity(BookDto dto) {
        if (dto == null) {
            return null;
        }
        return BookEntity.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .build();
    }

    public static BookDto entityToDto(BookEntity entity) {
        if (entity == null) {
            return null;
        }
        return BookDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .author(entity.getAuthor())
                .build();
    }
}
