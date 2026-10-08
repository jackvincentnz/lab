package lab.mops.ai.domain.chat;

import lab.libs.ddd.domain.BaseRepository;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends BaseRepository<Chat, ChatId> {
  @Query("SELECT id FROM chat WHERE id = :id FOR UPDATE")
  ChatId lockById(ChatId id);
}
