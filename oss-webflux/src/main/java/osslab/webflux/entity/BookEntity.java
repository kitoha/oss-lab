package osslab.webflux.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookEntity {
  private String id;
  private String title;
  private String author;
}
