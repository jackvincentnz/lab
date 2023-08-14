package nz.geek.jack.journal.adapter.gql;

import static org.assertj.core.api.Assertions.assertThat;

import nz.geek.jack.journal.domain.Entry;
import org.junit.jupiter.api.Test;

class EntryMapperTest {

  EntryMapper mapper = new EntryMapper();

  @Test
  void map_mapsId() {
    var entry = Entry.newEntry("My message");

    var result = mapper.map(entry);

    assertThat(result.getId()).isEqualTo(entry.getId().toString());
  }

  @Test
  void map_mapsMessage() {
    var entry = Entry.newEntry("My message");

    var result = mapper.map(entry);

    assertThat(result.getMessage()).isEqualTo(entry.getMessage());
  }

  @Test
  void map_mapsCreatedAt() {
    var entry = Entry.newEntry("My message");

    var result = mapper.map(entry);

    assertThat(result.getCreatedAt()).isEqualTo(entry.getCreatedAt().toString());
  }

  @Test
  void map_doesntMapAuthor() {
    var entry = Entry.newEntry("My message");

    var result = mapper.map(entry);

    assertThat(result.getAuthor()).isNull();
  }
}
